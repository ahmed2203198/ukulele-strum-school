package com.ukulelestrum.school.presentation.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ukulelestrum.school.core.navigation.Screen
import com.ukulelestrum.school.domain.model.*
import com.ukulelestrum.school.domain.repository.CourseRepository
import com.ukulelestrum.school.domain.usecase.EvaluateAnswerUseCase
import com.ukulelestrum.school.presentation.common.UiEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


class MenuViewModel(private val course: CourseRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(MenuUiState(lessons = course.lessons))
    val uiState = _uiState.asStateFlow()
    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    private val evaluation = EvaluateAnswerUseCase()
    init {
        viewModelScope.launch { course.progress.collect { progress ->
            val next = course.lessons.firstOrNull { it.id !in progress.completed && evaluation.unlocked(course.lessons, progress.completed, it.id) }
                ?: course.lessons.find { it.id == progress.lastLesson } ?: course.lessons.firstOrNull()
            _uiState.update { it.copy(progress = progress, nextLesson = next, prerequisiteKey = null) }
        } }
        refresh()
    }
    fun refresh() { viewModelScope.launch {
        _uiState.update { it.copy(loadStatus = LoadStatus.Loading) }
        val success = runCatching { course.load() }.isSuccess
        _uiState.update { it.copy(loadStatus = if (success) LoadStatus.Ready else LoadStatus.ReadError) }
    } }
    fun play() { _uiState.value.nextLesson?.let { select(it.id) } }
    fun select(id: String) {
        val index = course.lessons.indexOfFirst { it.id == id }
        if (index < 0) return
        if (!evaluation.unlocked(course.lessons, _uiState.value.progress.completed, id)) {
            _uiState.update { it.copy(prerequisiteKey = course.lessons[index - 1].titleKey) }
            return
        }
        viewModelScope.launch {
            runCatching { course.selectLesson(id) }
            _events.send(UiEvent.Navigate(Screen.Game(id)))
        }
    }
    fun navigate(screen: Screen) { viewModelScope.launch { _events.send(UiEvent.Navigate(screen)) } }
}
