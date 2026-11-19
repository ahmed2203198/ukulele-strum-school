package com.ukulelestrum.school.presentation.practice

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.ukulelestrum.school.core.config.GameConfig
import com.ukulelestrum.school.core.navigation.Screen
import com.ukulelestrum.school.domain.audio.*
import com.ukulelestrum.school.domain.model.*
import com.ukulelestrum.school.domain.repository.*
import com.ukulelestrum.school.presentation.common.UiEvent
import java.util.UUID
import kotlin.math.roundToLong
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

class PracticeViewModel(private val course: CourseRepository, private val preferences: SettingsRepository, private val audio: AudioPlayback, private val savedState: SavedStateHandle) : ViewModel() {
    private val _uiState = MutableStateFlow(PracticeUiState())
    val uiState = _uiState.asStateFlow()
    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    private var job: Job? = null
    private var sessionId = UUID.randomUUID().toString()
    private var openedKey: String? = null
    private var replayToken = -1
    private var forceFresh = false
    init {
        viewModelScope.launch { preferences.settings.collect { settings ->
            val previous = _uiState.value.settings
            _uiState.update { it.copy(settings = settings) }
            if ((previous.soundEnabled != settings.soundEnabled || previous.volumePercent != settings.volumePercent) && _uiState.value.running) pause()
        } }
        viewModelScope.launch { course.saveStatus.collect { status -> _uiState.update { it.copy(saveStatus = status) } } }
    }
    fun open(planId: String, patternId: String, tempo: Int, replay: Int) {
        val key = "$planId:$patternId:$tempo"
        if (openedKey == key && replayToken == replay && !forceFresh) return
        val fresh = forceFresh || (openedKey == key && replayToken != replay)
        forceFresh = false
        val retained = _uiState.value
        val restoring = savedState.get<String>("route") == key
        val selectedPlanId = if (fresh) retained.plan?.id ?: course.progress.value.latestResult?.takeIf { !it.isLesson }?.sourceId ?: planId
            else if (restoring) savedState.get<String>("plan") ?: planId else planId
        openedKey = key
        replayToken = replay
        savedState["route"] = key
        viewModelScope.launch {
            runCatching { course.load() }
            val checkpoint = course.progress.value.practiceCheckpoint?.takeIf { it.planId == selectedPlanId && !fresh }
            val plan = course.plans.find { it.id == selectedPlanId }
            if (plan == null) { _uiState.update { it.copy(loading = false, plan = null) }; return@launch }
            val selectedPattern = if (fresh) retained.pattern?.id ?: savedState.get<String>("pattern") ?: plan.patternId
                else if (restoring) savedState.get<String>("pattern") ?: patternId else patternId
            val selectedTempo = if (fresh) { if (retained.plan != null) retained.tempo else savedState.get<Int>("tempo") ?: plan.tempo }
                else if (restoring) savedState.get<Int>("tempo") ?: tempo else tempo
            configure(plan, checkpoint?.patternId ?: selectedPattern, checkpoint?.tempo ?: selectedTempo)
            if (checkpoint != null) {
                sessionId = checkpoint.sessionId
                updatePosition(checkpoint.chartFrame, PracticePhase.Paused)
            } else if (restoring && !fresh) {
                course.progress.value.latestResult?.takeIf { !it.isLesson && it.sourceId == plan.id }?.let { result ->
                    _uiState.update { it.copy(phase = PracticePhase.Finished, result = result, activeMillis = result.activeMillis,
                        chartFrame = result.activeMillis * GameConfig.SAMPLE_RATE / 1000, completedBars = result.completedBars,
                        barIndex = result.completedBars.coerceAtMost((plan.chords.size - 1).coerceAtLeast(0))) }
                }
            }
        }
    }
    private fun configure(plan: PracticePlan, patternId: String, tempo: Int) {
        sessionId = UUID.randomUUID().toString()
        savedState["plan"] = plan.id
        savedState["pattern"] = patternId
        savedState["tempo"] = tempo
        _uiState.update { PracticeUiState(loading = false, plan = plan,
            planChoices = course.plans.filter { it.id in setOf("plan_circle", "plan_lanai", "plan_sunset", plan.id) },
            chords = plan.chords.mapNotNull { id -> course.chords.find { it.id == id } },
            pattern = course.patterns.find { it.id == patternId } ?: course.patterns.firstOrNull(), tempo = (tempo.coerceIn(40, 120) / 4) * 4,
            settings = it.settings, saveStatus = it.saveStatus) }
    }
    fun choosePlan(delta: Int) {
        val state = _uiState.value
        if (state.running || state.planChoices.isEmpty()) return
        val index = state.planChoices.indexOfFirst { it.id == state.plan?.id }
        val plan = state.planChoices[(index + delta + state.planChoices.size) % state.planChoices.size]
        configure(plan, plan.patternId, plan.tempo)
        persistCheckpoint()
    }
    fun tempo(value: Int) {
        if (_uiState.value.running) return
        _uiState.update { it.copy(tempo = value.coerceIn(40, 120), chartFrame = 0, activeMillis = 0, completedBars = 0, phase = PracticePhase.Ready, result = null) }
        sessionId = UUID.randomUUID().toString()
        savedState["tempo"] = _uiState.value.tempo
        persistCheckpoint()
        val state = _uiState.value
        viewModelScope.launch { runCatching { course.savePreferences(state.pattern?.id ?: "steady_fours", state.tempo) } }
    }
    fun restart() {
        pause()
        _uiState.value.plan?.let { configure(it, _uiState.value.pattern?.id ?: it.patternId, _uiState.value.tempo) }
        persistCheckpoint()
    }
    fun visualOnly() { _uiState.update { it.copy(visualOnly = true, audioError = false) }; start() }
    fun start() {
        val state = _uiState.value
        if (state.running || state.phase == PracticePhase.Finished) return
        val plan = state.plan ?: return
        val pattern = state.pattern ?: return
        val previousJob = job
        previousJob?.cancel()
        _uiState.update { it.copy(phase = PracticePhase.Preparing, audioError = false) }
        job = viewModelScope.launch {
            previousJob?.join()
            val framesPerBeat = GameConfig.SAMPLE_RATE * 60.0 / state.tempo
            val countInFrames = (framesPerBeat * 4).roundToLong()
            val totalFrames = (framesPerBeat * 4 * plan.chords.size).roundToLong()
            val startFrame = ((state.chartFrame / framesPerBeat).toLong() * framesPerBeat).roundToLong().coerceAtMost(totalFrames)
            val audible = state.settings.soundEnabled && state.settings.volumePercent > 0 && !state.visualOnly
            try {
                if (audible) audio.prepare(AudioRequest.Rhythm(state.chords, pattern, state.tempo, startFrame), state.settings.volumePercent, "practice")
                val clockStart = SystemClock.elapsedRealtimeNanos()
                var savedBar = state.completedBars
                while (isActive) {
                    val elapsedFrames = if (audible) audio.positionFrames("practice") else ((SystemClock.elapsedRealtimeNanos() - clockStart) / 1_000_000_000.0 * GameConfig.SAMPLE_RATE).toLong()
                    if (elapsedFrames < countInFrames) {
                        _uiState.update { it.copy(phase = PracticePhase.CountIn, countInBeat = (elapsedFrames / framesPerBeat).toInt().coerceIn(0, 3) + 1) }
                    } else {
                        val chartFrame = (startFrame + elapsedFrames - countInFrames).coerceAtMost(totalFrames)
                        updatePosition(maxOf(state.chartFrame, chartFrame), PracticePhase.Playing)
                        val bars = _uiState.value.completedBars
                        if (bars > savedBar) { savedBar = bars; persistCheckpoint() }
                        if (chartFrame >= totalFrames) {
                            audio.stop("practice")
                            finish(true)
                            break
                        }
                    }
                    delay(33)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update { it.copy(phase = PracticePhase.Paused, audioError = true) }
                persistCheckpoint()
            } finally {
                audio.stop("practice")
            }
        }
    }
    private fun updatePosition(frame: Long, phase: PracticePhase) {
        _uiState.update { state ->
            val beat = GameConfig.SAMPLE_RATE * 60.0 / state.tempo
            val totalBars = state.plan?.chords?.size ?: 1
            val totalFrames = (beat * 4 * totalBars).roundToLong()
            val bars = if (frame >= totalFrames) totalBars else (frame / (beat * 4)).toInt().coerceIn(0, totalBars)
            state.copy(phase = phase, chartFrame = frame, activeMillis = frame * 1000 / GameConfig.SAMPLE_RATE,
                completedBars = bars, barIndex = bars.coerceAtMost(totalBars - 1), beatIndex = ((frame / beat).toInt() % 4), slotIndex = ((frame / (beat / 2)).toInt() % 8))
        }
    }
    private fun persistCheckpoint() {
        val state = _uiState.value
        val plan = state.plan ?: return
        if (state.phase == PracticePhase.Finished) return
        val checkpoint = PracticeCheckpoint(sessionId, plan.id, state.pattern?.id ?: plan.patternId, state.tempo, state.chartFrame, state.activeMillis)
        viewModelScope.launch { runCatching { course.savePracticeCheckpoint(checkpoint) } }
    }
    fun pause() {
        if (_uiState.value.running) {
            job?.cancel()
            _uiState.update { it.copy(phase = PracticePhase.Paused) }
            persistCheckpoint()
        }
    }
    fun end() {
        if (_uiState.value.phase == PracticePhase.Finished) return
        job?.cancel()
        _uiState.update { it.copy(phase = PracticePhase.Paused) }
        viewModelScope.launch { audio.stop("practice"); finish(false) }
    }
    private suspend fun finish(complete: Boolean) {
        val state = _uiState.value
        val plan = state.plan ?: return
        if (state.phase == PracticePhase.Finished) return
        val result = SessionResult(UUID.randomUUID().toString(), plan.id,
            if (complete) ResultKind.PracticeComplete else ResultKind.PracticeEndedEarly,
            completedBars = state.completedBars, totalBars = plan.chords.size, activeMillis = state.activeMillis, sessionId = sessionId)
        _uiState.update { it.copy(phase = PracticePhase.Finished, result = result) }
        runCatching { course.saveResult(result) }
        _events.send(UiEvent.Navigate(Screen.GameOver(result.id)))
    }
    fun retrySave() { val result = _uiState.value.result; if (result == null) persistCheckpoint() else viewModelScope.launch { runCatching { course.saveResult(result) } } }
    fun prepareReplay() { forceFresh = true }
    fun clearCourse() {
        pause()
        job?.cancel()
        while (_events.tryReceive().isSuccess) Unit
        openedKey = null
        savedState.remove<String>("route")
        savedState.remove<String>("plan")
        savedState.remove<String>("pattern")
        savedState.remove<Int>("tempo")
        _uiState.update { PracticeUiState(settings = it.settings) }
    }
}
