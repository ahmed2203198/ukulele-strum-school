package com.ukulelestrum.school.presentation.common

import com.ukulelestrum.school.core.navigation.Screen

sealed interface UiEvent {
    data class Navigate(val screen: Screen) : UiEvent
    data object Back : UiEvent
    data object Menu : UiEvent
    data object Replay : UiEvent
    data object Haptic : UiEvent
    data object ResetCourse : UiEvent
}
