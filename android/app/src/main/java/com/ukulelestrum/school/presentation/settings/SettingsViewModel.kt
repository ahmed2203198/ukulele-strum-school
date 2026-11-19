package com.ukulelestrum.school.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ukulelestrum.school.domain.model.*
import com.ukulelestrum.school.domain.repository.*
import com.ukulelestrum.school.presentation.common.UiEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*


class SettingsViewModel(private val preferences: SettingsRepository, private val course: CourseRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()
    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    private var confirmationJob: Job? = null
    init { load() }
    private fun load() { viewModelScope.launch {
        val success = runCatching { preferences.load() }.isSuccess
        _uiState.update { it.copy(loading = false, readError = !success, settings = preferences.settings.value) }
    } }
    fun sound(enabled: Boolean) = save(_uiState.value.settings.copy(soundEnabled = enabled))
    fun haptics(enabled: Boolean) = save(_uiState.value.settings.copy(hapticsEnabled = enabled))
    fun volume(value: Int) = save(_uiState.value.settings.copy(volumePercent = value.coerceIn(0, 100)))
    fun retry() { if (_uiState.value.readError) load() else save(_uiState.value.settings) }
    private fun save(value: SoundSettings) {
        _uiState.update { it.copy(settings = value, saveStatus = SaveStatus.Saving, showSaved = false) }
        viewModelScope.launch {
            val success = runCatching { preferences.saveSettings(value) }.isSuccess
            if (_uiState.value.settings == value) _uiState.update { it.copy(saveStatus = if (success) SaveStatus.Saved else SaveStatus.Failed, showSaved = success) }
            if (success) {
                if (value.hapticsEnabled) _events.send(UiEvent.Haptic)
                confirmationJob?.cancel()
                confirmationJob = viewModelScope.launch { delay(1200); _uiState.update { it.copy(showSaved = false) } }
            }
        }
    }
    fun event(event: UiEvent) { viewModelScope.launch { _events.send(event) } }
    fun showReset() { _uiState.update { it.copy(showReset = true, resetError = false) } }
    fun dismissReset() { if (!_uiState.value.resetting) _uiState.update { it.copy(showReset = false) } }
    fun reset() {
        if (_uiState.value.resetting) return
        _uiState.update { it.copy(resetting = true, resetError = false) }
        viewModelScope.launch {
            val success = runCatching { course.reset() }.isSuccess
            _uiState.update { it.copy(resetting = false, resetError = !success, showReset = !success) }
            if (success) _events.send(UiEvent.ResetCourse)
        }
    }
}
