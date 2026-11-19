package com.ukulelestrum.school.presentation.app

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ukulelestrum.school.core.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class AppViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    private fun decode(value: String): Screen = when {
        value == "menu" -> Screen.Menu
        value.startsWith("game:") -> Screen.Game(value.substringAfter(':'))
        value.startsWith("result:") -> Screen.GameOver(value.substringAfter(':'))
        value.startsWith("patterns:") -> Screen.StrumPatterns(value.substringAfter(':'))
        value.startsWith("practice:") -> value.split(':').let { Screen.Practice(it.getOrElse(1) { "plan_circle" }, it.getOrElse(2) { "steady_fours" }, it.getOrNull(3)?.toIntOrNull() ?: 60) }
        value == "tuning" -> Screen.Tuning
        value == "settings" -> Screen.Settings
        else -> Screen.Splash
    }
    private fun encode(screen: Screen): String = when (screen) {
        Screen.Splash -> "splash"
        Screen.Menu -> "menu"
        is Screen.Game -> "game:${screen.lessonId}"
        is Screen.GameOver -> "result:${screen.resultId}"
        is Screen.StrumPatterns -> "patterns:${screen.patternId}"
        is Screen.Practice -> "practice:${screen.planId}:${screen.patternId}:${screen.tempo}"
        Screen.Tuning -> "tuning"
        Screen.Settings -> "settings"
    }
    private val initial = savedState.get<ArrayList<String>>("destinations")?.map(::decode).orEmpty()
    private val _uiState = MutableStateFlow(if (initial.isEmpty()) AppUiState() else AppUiState(initial.last(), initial.dropLast(1).filter { it != Screen.Splash }))
    val uiState = _uiState.asStateFlow()
    private val _screen = MutableStateFlow(_uiState.value.screen)
    val screen = _screen.asStateFlow()

    private fun publish(state: AppUiState) {
        _uiState.update { state }
        _screen.value = state.screen
        savedState["destinations"] = ArrayList((state.backStack + state.screen).map(::encode))
    }
    fun finishSplash() {
        if (_screen.value == Screen.Splash) publish(AppUiState(Screen.Menu))
    }
    fun navigate(destination: Screen) {
        val state = _uiState.value
        if (state.screen == destination || state.screen == Screen.Splash) return
        if (destination == Screen.Menu) menu() else publish(state.copy(screen = destination, backStack = state.backStack + state.screen))
    }
    fun back() {
        val state = _uiState.value
        val previous = state.backStack.lastOrNull() ?: return
        publish(state.copy(screen = previous, backStack = state.backStack.dropLast(1)))
    }
    fun menu() = publish(_uiState.value.copy(screen = Screen.Menu, backStack = emptyList()))
    fun replay() {
        val state = _uiState.value
        if (state.screen is Screen.GameOver && state.backStack.isNotEmpty()) publish(state.copy(
            screen = state.backStack.last(), backStack = state.backStack.dropLast(1), replaySerial = state.replaySerial + 1
        ))
    }
}
