package com.ukulelestrum.school.presentation.strumpatterns

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
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*


class StrumPatternsViewModel(private val course: CourseRepository, private val preferences: SettingsRepository, private val audio: AudioPlayback, private val savedState: SavedStateHandle) : ViewModel() {
    private val _uiState = MutableStateFlow(StrumPatternsUiState(patterns = course.patterns, selected = course.patterns.firstOrNull()))
    val uiState = _uiState.asStateFlow()
    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    private var job: Job? = null
    private var openedId: String? = null
    init {
        viewModelScope.launch { runCatching { preferences.load() }; preferences.settings.collect { settings ->
            if (_uiState.value.phase == PatternPhase.Playing && _uiState.value.settings != settings) pause()
            _uiState.update { it.copy(settings = settings) }
        } }
    }
    fun open(id: String) {
        if (openedId == id) return
        val selectedId = if (savedState.get<String>("route") == id) savedState.get<String>("pattern") ?: id else id
        openedId = id
        savedState["route"] = id
        viewModelScope.launch {
            runCatching { course.load() }
            _uiState.update { it.copy(selected = course.patterns.find { pattern -> pattern.id == selectedId } ?: it.selected,
                tempo = (savedState.get<Int>("tempo") ?: course.progress.value.tempo).coerceIn(40, 120)) }
        }
    }
    fun select(id: String) {
        stop()
        _uiState.update { it.copy(selected = course.patterns.find { pattern -> pattern.id == id } ?: it.selected) }
        saveSelection()
    }
    fun tempo(value: Int) { stop(); _uiState.update { it.copy(tempo = value.coerceIn(40, 120)) }; saveSelection() }
    private fun saveSelection() {
        val state = _uiState.value
        savedState["pattern"] = state.selected?.id
        savedState["tempo"] = state.tempo
        viewModelScope.launch { runCatching { course.savePreferences(state.selected?.id ?: "steady_fours", state.tempo) } }
    }
    fun visualOnly() { _uiState.update { it.copy(visualOnly = true) }; play() }
    fun play() {
        val state = _uiState.value
        if (state.phase == PatternPhase.Playing || state.phase == PatternPhase.Preparing) { stop(); return }
        val pattern = state.selected ?: return
        val chord = course.chords.firstOrNull() ?: return
        val previousJob = job
        previousJob?.cancel()
        _uiState.update { it.copy(phase = PatternPhase.Preparing, audioError = false) }
        job = viewModelScope.launch {
            previousJob?.join()
            val beat = GameConfig.SAMPLE_RATE * 60.0 / state.tempo
            val audible = state.settings.soundEnabled && state.settings.volumePercent > 0 && !state.visualOnly
            try {
                if (audible) audio.prepare(AudioRequest.Rhythm(listOf(chord, chord), pattern, state.tempo), state.settings.volumePercent, "patterns")
                val started = SystemClock.elapsedRealtimeNanos()
                while (isActive) {
                    val frame = if (audible) audio.positionFrames("patterns") else ((SystemClock.elapsedRealtimeNanos() - started) / 1_000_000_000.0 * GameConfig.SAMPLE_RATE).toLong()
                    if (frame >= beat * 12 - 1) break
                    _uiState.update { it.copy(phase = PatternPhase.Playing, activeSlot = if (frame < beat * 4) -1 else (((frame - beat * 4) / (beat / 2)).toInt() % 8), countIn = if (frame < beat * 4) (frame / beat).toInt() + 1 else 0) }
                    delay(33)
                }
                _uiState.update { it.copy(phase = PatternPhase.Idle, activeSlot = -1, countIn = 0) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update { it.copy(phase = PatternPhase.Paused, audioError = true, activeSlot = -1) }
            } finally {
                audio.stop("patterns")
            }
        }
    }
    fun stop() { job?.cancel(); _uiState.update { it.copy(phase = PatternPhase.Idle, activeSlot = -1, countIn = 0) } }
    fun pause() { if (_uiState.value.phase == PatternPhase.Playing || _uiState.value.phase == PatternPhase.Preparing) { job?.cancel(); _uiState.update { it.copy(phase = PatternPhase.Paused, activeSlot = -1, countIn = 0) } } }
    fun practice() {
        pause()
        val state = _uiState.value
        viewModelScope.launch { _events.send(UiEvent.Navigate(Screen.Practice("plan_circle", state.selected?.id ?: "steady_fours", state.tempo))) }
    }
}
