package com.ukulelestrum.school.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ukulelestrum.school.core.config.GameConfig
import com.ukulelestrum.school.core.navigation.Screen
import com.ukulelestrum.school.domain.audio.*
import com.ukulelestrum.school.domain.model.*
import com.ukulelestrum.school.domain.repository.*
import com.ukulelestrum.school.domain.usecase.EvaluateAnswerUseCase
import com.ukulelestrum.school.presentation.common.UiEvent
import java.util.UUID
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

class GameViewModel(private val course: CourseRepository, private val preferences: SettingsRepository, private val audio: AudioPlayback) : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState = _uiState.asStateFlow()
    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    private var answerJob: Job? = null
    private var audioJob: Job? = null
    private var openedId: String? = null
    private var replayToken = -1
    private var attemptId = UUID.randomUUID().toString()
    private var pendingResultId: String? = null
    private var resultSaveJob: Job? = null
    private var forceFresh = false
    private val evaluate = EvaluateAnswerUseCase()
    init {
        viewModelScope.launch { preferences.settings.collect { settings ->
            _uiState.update { it.copy(settings = settings) }
            if (!settings.soundEnabled || settings.volumePercent == 0) stopAudio()
        } }
        viewModelScope.launch { course.saveStatus.collect { status -> _uiState.update { it.copy(saveStatus = status) } } }
    }
    fun open(id: String, replay: Int) {
        if (openedId == id && replayToken == replay && !forceFresh) {
            pendingResultId?.let { resultId ->
                pendingResultId = null
                viewModelScope.launch { resultSaveJob?.join(); _events.send(UiEvent.Navigate(Screen.GameOver(resultId))) }
            }
            return
        }
        val fresh = forceFresh || (openedId == id && replayToken != replay)
        forceFresh = false
        pendingResultId = null
        openedId = id
        replayToken = replay
        answerJob?.cancel()
        stopAudio()
        viewModelScope.launch {
            runCatching { course.load() }
            val lesson = course.lessons.find { it.id == id }
            val checkpoint = course.progress.value.lessonCheckpoint?.takeIf { it.lessonId == id && !fresh }
            val answers = checkpoint?.answers.orEmpty()
            attemptId = checkpoint?.attemptId?.takeIf { it.isNotEmpty() } ?: UUID.randomUUID().toString()
            val allCorrect = lesson != null && answers.withIndex().all { (index, answer) -> evaluate(lesson.questions[index], answer) }
            val resumable = checkpoint?.checking == true && allCorrect && answers.size < 4
            val previousResult = course.progress.value.latestResult?.takeIf { it.isLesson && it.sourceId == id && checkpoint == null && !fresh }
            _uiState.update { it.copy(
                loading = false, lesson = lesson, ordinal = course.lessons.indexOfFirst { it.id == id } + 1,
                chords = lesson?.chordIds.orEmpty().distinct().mapNotNull { chordId -> course.chords.find { it.id == chordId } },
                chordIndex = 0, pattern = course.patterns.find { it.id == lesson?.patternId }, plan = course.plans.find { it.id == lesson?.planId },
                phase = if (resumable) LessonPhase.Checking else if (previousResult != null) LessonPhase.Completed else LessonPhase.Studying,
                answers = if (resumable) answers else emptyList(), correctCount = previousResult?.correct ?: if (resumable) answers.size else 0,
                selectedAnswer = null, result = previousResult, audioError = false
            ) }
            if (checkpoint?.checking == true && answers.isNotEmpty() && (!allCorrect || answers.size == 4) && lesson != null) {
                val correctCount = answers.withIndex().takeWhile { (index, answer) -> evaluate(lesson.questions[index], answer) }.size
                _uiState.update { it.copy(phase = LessonPhase.AnswerFeedback, answers = answers,
                    correctCount = correctCount, feedbackCorrect = allCorrect) }
                completeAttempt(true)
            }
        }
    }
    fun selectChord(index: Int) { stopAudio(); _uiState.update { it.copy(chordIndex = index.coerceIn(0, (it.chords.size - 1).coerceAtLeast(0))) } }
    fun startCheck() {
        stopAudio()
        answerJob?.cancel()
        attemptId = UUID.randomUUID().toString()
        pendingResultId = null
        _uiState.update { it.copy(phase = LessonPhase.Checking, answers = emptyList(), correctCount = 0, selectedAnswer = null, result = null) }
        checkpoint()
    }
    fun review() {
        answerJob?.cancel()
        _uiState.update { it.copy(phase = LessonPhase.Studying, answers = emptyList(), correctCount = 0, selectedAnswer = null) }
        checkpoint()
    }
    private fun checkpoint() {
        val state = _uiState.value
        val lesson = state.lesson ?: return
        val checkpoint = LessonCheckpoint(lesson.id, state.answers, state.phase != LessonPhase.Studying, attemptId)
        viewModelScope.launch { runCatching { course.saveLessonCheckpoint(checkpoint) } }
    }
    fun answer(id: String) {
        val state = _uiState.value
        val lesson = state.lesson ?: return
        if (state.phase != LessonPhase.Checking) return
        val question = lesson.questions.getOrNull(state.answers.size) ?: return
        if (question.options.none { it.id == id }) return
        val correct = evaluate(question, id)
        val nextAnswers = state.answers + id
        val count = state.correctCount + if (correct) 1 else 0
        _uiState.update { it.copy(phase = LessonPhase.AnswerFeedback, answers = nextAnswers, correctCount = count, selectedAnswer = id, feedbackCorrect = correct) }
        checkpoint()
        answerJob = viewModelScope.launch {
            delay(GameConfig.ANSWER_FEEDBACK_MS)
            if (!correct || nextAnswers.size == 4) {
                completeAttempt(true)
            } else _uiState.update { it.copy(phase = LessonPhase.Checking, selectedAnswer = null) }
        }
    }
    private fun completeAttempt(navigate: Boolean) {
        val state = _uiState.value
        val lesson = state.lesson ?: return
        if (state.phase == LessonPhase.Completed) return
        val question = lesson.questions.getOrNull(state.answers.lastIndex)
        val passed = state.correctCount == 4
        val result = SessionResult(attemptId, lesson.id,
            if (passed) ResultKind.LessonComplete else ResultKind.LessonNeedsReview,
            state.correctCount, state.answers.size,
            if (passed) null else question?.let { q -> q.options.find { it.id == q.correctId }?.textKey })
        _uiState.update { it.copy(phase = LessonPhase.Completed, result = result, selectedAnswer = null) }
        pendingResultId = if (navigate) null else result.id
        resultSaveJob = viewModelScope.launch {
            runCatching { course.saveResult(result) }
            if (navigate) {
                if (passed && _uiState.value.settings.hapticsEnabled) _events.send(UiEvent.Haptic)
                _events.send(UiEvent.Navigate(Screen.GameOver(result.id)))
            }
        }
    }
    fun preview() {
        val state = _uiState.value
        if (state.previewing) { stopAudio(); return }
        if (!state.settings.soundEnabled || state.settings.volumePercent == 0) return
        val chord = state.chords.getOrNull(state.chordIndex) ?: return
        val previousJob = audioJob
        previousJob?.cancel()
        audioJob = viewModelScope.launch {
            previousJob?.join()
            _uiState.update { it.copy(previewing = true, audioError = false) }
            try {
                audio.prepare(AudioRequest.ChordPreview(chord), state.settings.volumePercent, "lesson")
                delay(1600)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update { it.copy(audioError = true) }
            } finally {
                audio.stop("lesson")
                _uiState.update { it.copy(previewing = false) }
            }
        }
    }
    fun stopAudio() { audioJob?.cancel(); _uiState.update { it.copy(previewing = false) } }
    fun pause() {
        stopAudio()
        if (_uiState.value.phase == LessonPhase.AnswerFeedback) {
            answerJob?.cancel()
            if (_uiState.value.feedbackCorrect && _uiState.value.answers.size < 4) {
                _uiState.update { it.copy(phase = LessonPhase.Checking, selectedAnswer = null) }
            } else completeAttempt(false)
        }
    }
    fun navigate(screen: Screen) { pause(); viewModelScope.launch { _events.send(UiEvent.Navigate(screen)) } }
    fun retrySave() { val result = _uiState.value.result; if (result == null) checkpoint() else viewModelScope.launch { runCatching { course.saveResult(result) } } }
    fun prepareReplay() { forceFresh = true }
    fun clearCourse() {
        pause()
        answerJob?.cancel()
        resultSaveJob?.cancel()
        while (_events.tryReceive().isSuccess) Unit
        openedId = null
        pendingResultId = null
        _uiState.update { GameUiState(settings = it.settings) }
    }
}
