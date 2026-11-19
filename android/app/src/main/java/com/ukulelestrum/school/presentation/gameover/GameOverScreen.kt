package com.ukulelestrum.school.presentation.gameover

import androidx.compose.foundation.Canvas
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ukulelestrum.school.R
import com.ukulelestrum.school.core.di.ServiceLocator
import com.ukulelestrum.school.core.theme.*
import com.ukulelestrum.school.core.ui.*
import com.ukulelestrum.school.domain.model.ResultKind
import com.ukulelestrum.school.presentation.common.*

@Composable
fun GameOverRoute(resultId: String, onEvent: (UiEvent) -> Unit) {
    val model: GameOverViewModel = viewModel(factory = ServiceLocator.factory)
    val state by model.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(resultId) { model.open(resultId) }
    LaunchedEffect(Unit) { model.events.collect(onEvent) }
    GameOverScreen(state, { model.event(UiEvent.Replay) }, { model.event(UiEvent.Menu) }, { model.event(UiEvent.Back) }, model::retrySave)
}

@Composable
fun GameOverScreen(state: GameOverUiState, onReplay: () -> Unit, onMenu: () -> Unit, onBack: () -> Unit, onRetry: () -> Unit) {
    StudioPage(2) {
        SecondaryButton(R.string.back, onBack)
        if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        else if (state.result == null) {
            StatusMessage(stringResource(R.string.result_missing))
            PrimaryButton(R.string.menu, onMenu)
        } else {
            val result = state.result
            val success = result.kind == ResultKind.LessonComplete || result.kind == ResultKind.PracticeComplete
            val reveal = remember(result.id) { MutableTransitionState(false).apply { targetState = success } }
            val transition = updateTransition(reveal, label = "result")
            val settle by transition.animateFloat(transitionSpec = { spring(dampingRatio = 0.9f, stiffness = 400f) }, label = "settle") { if (it) 1f else 0.96f }
            val check by transition.animateFloat(transitionSpec = { tween(240) }, label = "check") { if (it) 1f else 0f }
            val rings by transition.animateFloat(transitionSpec = { tween(700) }, label = "rings") { if (it) 1f else 0f }
            Column(Modifier.fillMaxWidth().graphicsLayer { scaleX = if (success) settle else 1f; scaleY = if (success) settle else 1f }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Box(Modifier.size(144.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.fillMaxSize()) {
                        drawCircle(Leaf.copy(alpha = 0.12f))
                        drawCircle(Leaf.copy(alpha = 0.28f), style = Stroke(1.dp.toPx()))
                        if (success) {
                            repeat(2) { index ->
                                drawCircle(Leaf.copy(alpha = (1f - rings) * 0.24f), size.width * (0.22f + rings * 0.23f + index * 0.05f), style = Stroke(2.dp.toPx()))
                            }
                            val path = Path().apply { moveTo(size.width * 0.32f, size.height * 0.52f); lineTo(size.width * 0.45f, size.height * 0.65f); lineTo(size.width * 0.70f, size.height * 0.36f) }
                            drawPath(path, Leaf.copy(alpha = check), style = Stroke(6.dp.toPx()))
                        } else drawCircle(Mist.copy(alpha = 0.18f), size.width * 0.35f, Offset(size.width / 2, size.height / 2), style = Stroke(3.dp.toPx()))
                    }
                    if (!success) Image(painterResource(R.drawable.logo), null, Modifier.size(88.dp), contentScale = ContentScale.Fit)
                }
                Text(stringResource(when (result.kind) {
                    ResultKind.LessonComplete -> R.string.lesson_done
                    ResultKind.LessonNeedsReview -> R.string.lesson_review
                    ResultKind.PracticeComplete -> R.string.practice_complete
                    ResultKind.PracticeEndedEarly -> R.string.practice_saved
                }), style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center, modifier = Modifier.semantics { heading(); liveRegion = LiveRegionMode.Polite })
            }
            StudioCard {
                state.titleKey?.let { Text(stringResource(contentRes(it)), style = MaterialTheme.typography.titleLarge) }
                if (result.isLesson) {
                    Text(stringResource(R.string.knowledge_check), style = MaterialTheme.typography.labelSmall, color = Leaf)
                    StatusMessage(stringResource(R.string.check_score, result.correct))
                    if (result.correct == 0) Text(stringResource(R.string.first_review), style = MaterialTheme.typography.bodyLarge)
                    result.correctionKey?.let { Text(stringResource(R.string.correction, stringResource(contentRes(it))), style = MaterialTheme.typography.bodyLarge) }
                    if (state.bestScore > 0) Text(stringResource(R.string.best_score, state.bestScore))
                    state.nextTitleKey?.let { Text(stringResource(R.string.newly_available, stringResource(contentRes(it)))) }
                    if (state.courseComplete) Text(stringResource(R.string.course_done))
                } else {
                    StatusMessage(stringResource(R.string.practice_summary, result.completedBars, result.totalBars, result.activeMillis / 1000))
                    if (result.completedBars == 0) Text(stringResource(R.string.practice_start))
                    Text(stringResource(if (result.kind == ResultKind.PracticeComplete) R.string.practice_result_disclaimer else R.string.practice_disclaimer), style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (!result.isLesson && result.completedBars > 0 && result.activeMillis >= 1000) Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(stringResource(R.string.count_value, result.completedBars), R.string.bars_stat, Modifier.weight(1f))
                StatCard(stringResource(R.string.count_value, result.activeMillis / 1000), R.string.seconds_stat, Modifier.weight(1f))
            }
            SaveConfirmation(state.saveStatus, onRetry)
            Text(stringResource(R.string.result_hint), style = MaterialTheme.typography.bodyMedium)
            PrimaryButton(R.string.play_again, onReplay)
            SecondaryButton(R.string.menu, onMenu, Modifier.fillMaxWidth(), image = R.drawable.icon_home)
        }
    }
}
