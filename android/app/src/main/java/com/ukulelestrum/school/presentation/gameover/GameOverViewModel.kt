package com.ukulelestrum.school.presentation.gameover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ukulelestrum.school.domain.model.*
import com.ukulelestrum.school.domain.repository.CourseRepository
import com.ukulelestrum.school.presentation.common.UiEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


class GameOverViewModel(private val course: CourseRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(GameOverUiState())
    val uiState = _uiState.asStateFlow()
    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    private var resultId: String? = null
    init {
        viewModelScope.launch { course.progress.collect { refresh() } }
        viewModelScope.launch { course.saveStatus.collect { status -> _uiState.update { it.copy(saveStatus = status) } } }
    }
    fun open(id: String) {
        resultId = id
        viewModelScope.launch { runCatching { course.load() }; refresh() }
    }
    private fun refresh() {
        if (resultId == null) return
        val progress = course.progress.value
        val result = progress.latestResult?.takeIf { it.id == resultId }
        val lesson = course.lessons.find { it.id == result?.sourceId }
        val index = course.lessons.indexOf(lesson)
        val next = course.lessons.getOrNull(index + 1)?.takeIf { result?.kind == ResultKind.LessonComplete && index >= 0 }
        _uiState.update { it.copy(loading = false, result = result,
            titleKey = lesson?.titleKey ?: course.plans.find { plan -> plan.id == result?.sourceId }?.nameKey,
            nextTitleKey = next?.titleKey, courseComplete = progress.completed.size == course.lessons.size,
            completedLessons = progress.completed.size, bestScore = progress.bestScores[result?.sourceId] ?: 0) }
    }
    fun retrySave() { _uiState.value.result?.let { result -> viewModelScope.launch { runCatching { course.saveResult(result) } } } }
    fun event(event: UiEvent) { viewModelScope.launch { _events.send(event) } }
}
