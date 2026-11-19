package com.ukulelestrum.school.presentation.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ukulelestrum.school.core.di.ServiceLocator
import com.ukulelestrum.school.core.navigation.Screen
import com.ukulelestrum.school.presentation.common.UiEvent
import com.ukulelestrum.school.presentation.game.*
import com.ukulelestrum.school.presentation.gameover.GameOverRoute
import com.ukulelestrum.school.presentation.menu.MenuRoute
import com.ukulelestrum.school.presentation.practice.*
import com.ukulelestrum.school.presentation.settings.SettingsRoute
import com.ukulelestrum.school.presentation.splash.SplashRoute
import com.ukulelestrum.school.presentation.strumpatterns.*
import com.ukulelestrum.school.presentation.tuning.*

@Composable
fun AppRoot() {
    val app: AppViewModel = viewModel(factory = ServiceLocator.factory)
    val game: GameViewModel = viewModel(factory = ServiceLocator.factory)
    val practice: PracticeViewModel = viewModel(factory = ServiceLocator.factory)
    val patterns: StrumPatternsViewModel = viewModel(factory = ServiceLocator.factory)
    val tuning: TuningViewModel = viewModel(factory = ServiceLocator.factory)
    val screen by app.screen.collectAsStateWithLifecycle()
    val state by app.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val pauseAll: () -> Unit = { game.pause(); practice.pause(); patterns.pause(); tuning.pause() }
    BackHandler(enabled = screen != Screen.Menu && screen != Screen.Splash) { pauseAll(); app.back() }
    AnimatedContent(targetState = screen,
        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(180)) }, label = "destination") { destination ->
        val current = destination == screen
        val callback: (UiEvent) -> Unit = { event ->
            if (app.screen.value == destination) {
                when (event) {
                    UiEvent.Haptic -> haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    is UiEvent.Navigate -> { pauseAll(); app.navigate(event.screen) }
                    UiEvent.Back -> { pauseAll(); app.back() }
                    UiEvent.Menu -> { pauseAll(); app.menu() }
                    UiEvent.Replay -> {
                        pauseAll()
                        when (app.uiState.value.backStack.lastOrNull()) {
                            is Screen.Game -> game.prepareReplay()
                            is Screen.Practice -> practice.prepareReplay()
                            else -> Unit
                        }
                        app.replay()
                    }
                    UiEvent.ResetCourse -> { pauseAll(); game.clearCourse(); practice.clearCourse(); app.menu() }
                }
            }
        }
        val modifier = if (current) Modifier else Modifier.clearAndSetSemantics {}.pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
            }
        }
        Box(modifier.fillMaxSize()) {
            when (destination) {
                Screen.Splash -> SplashRoute(app::finishSplash)
                Screen.Menu -> MenuRoute(callback)
                is Screen.Game -> GameRoute(destination.lessonId, state.replaySerial, callback)
                is Screen.GameOver -> GameOverRoute(destination.resultId, callback)
                is Screen.StrumPatterns -> StrumPatternsRoute(destination.patternId, callback)
                is Screen.Practice -> PracticeRoute(destination.planId, destination.patternId, destination.tempo, state.replaySerial, callback)
                Screen.Tuning -> TuningRoute(callback)
                Screen.Settings -> SettingsRoute(callback)
            }
        }
    }
}
