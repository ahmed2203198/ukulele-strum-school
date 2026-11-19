package com.ukulelestrum.school.presentation.app

import com.ukulelestrum.school.core.navigation.Screen

data class AppUiState(val screen: Screen = Screen.Splash, val backStack: List<Screen> = emptyList(), val replaySerial: Int = 0)
