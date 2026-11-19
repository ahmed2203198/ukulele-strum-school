package com.ukulelestrum.school.presentation.tuning

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ukulelestrum.school.domain.audio.*
import com.ukulelestrum.school.domain.model.SoundSettings
import com.ukulelestrum.school.domain.repository.SettingsRepository
import com.ukulelestrum.school.presentation.common.UiEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


class TuningViewModel(private val preferences: SettingsRepository, private val audio: AudioPlayback, private val savedState: SavedStateHandle) : ViewModel() {
    private val _uiState = MutableStateFlow(TuningUiState(selectedString = (savedState.get<Int>("string") ?: 0).coerceIn(0, 3)))
    val uiState = _uiState.asStateFlow()
    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    private var job: Job? = null
    init {
        viewModelScope.launch {
            runCatching { preferences.load() }
            preferences.settings.collect { settings ->
                _uiState.update { it.copy(settings = settings) }
                if (!settings.soundEnabled || settings.volumePercent == 0) pause()
            }
        }
    }
    fun select(index: Int) {
        pause()
        val selected = index.coerceIn(0, 3)
        savedState["string"] = selected
        _uiState.update { it.copy(selectedString = selected) }
    }
    fun play() {
        val state = _uiState.value
        if (!state.settings.soundEnabled || state.settings.volumePercent == 0) return
        val previous = job
        previous?.cancel()
        _uiState.update { it.copy(phase = TonePhase.Preparing) }
        job = viewModelScope.launch {
            previous?.join()
            try {
                audio.prepare(AudioRequest.Tone(listOf(67, 60, 64, 69)[state.selectedString]), state.settings.volumePercent, "tuning")
                _uiState.update { it.copy(phase = TonePhase.Playing) }
                delay(1200)
                _uiState.update { it.copy(phase = TonePhase.Idle) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update { it.copy(phase = TonePhase.Error) }
            } finally {
                audio.stop("tuning")
            }
        }
    }
    fun pause() { job?.cancel(); _uiState.update { it.copy(phase = TonePhase.Idle) } }
    fun event(event: UiEvent) { pause(); viewModelScope.launch { _events.send(event) } }
}
