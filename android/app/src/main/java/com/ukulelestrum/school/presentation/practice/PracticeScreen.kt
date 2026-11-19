package com.ukulelestrum.school.presentation.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ukulelestrum.school.R
import com.ukulelestrum.school.core.di.ServiceLocator
import com.ukulelestrum.school.core.theme.*
import com.ukulelestrum.school.core.ui.*
import com.ukulelestrum.school.presentation.common.*

@Composable
fun PracticeRoute(planId: String, patternId: String, tempo: Int, replay: Int, onEvent: (UiEvent) -> Unit) {
    val model: PracticeViewModel = viewModel(factory = ServiceLocator.factory)
    val state by model.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(planId, patternId, tempo, replay) { model.open(planId, patternId, tempo, replay) }
    LaunchedEffect(Unit) { model.events.collect(onEvent) }
    PauseOnLeave(model::pause)
    PracticeScreen(state, model::choosePlan, model::tempo, model::start, model::pause, model::restart, model::end, model::visualOnly, model::retrySave,
        { model.pause(); onEvent(UiEvent.Back) }, { model.pause(); onEvent(UiEvent.Menu) })
}

@Composable
fun PracticeScreen(state: PracticeUiState, onPlan: (Int) -> Unit, onTempo: (Int) -> Unit, onStart: () -> Unit, onPause: () -> Unit, onRestart: () -> Unit, onEnd: () -> Unit, onVisual: () -> Unit, onRetry: () -> Unit, onBack: () -> Unit, onMenu: () -> Unit) {
    StudioPage {
        ScreenHeader(stringResource(R.string.practice_title), onBack, onMenu)
        Text(stringResource(R.string.practice_eyebrow), style = MaterialTheme.typography.labelSmall, color = Leaf)
        if (state.plan == null) {
            if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
            else { StatusMessage(stringResource(R.string.catalog_error)); SecondaryButton(R.string.menu, onMenu) }
        } else {
            val plan = state.plan
            Text(stringResource(contentRes(plan.nameKey)), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.plan_details, plan.chords.size, state.tempo), style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton(R.string.previous_plan, { onPlan(-1) }, Modifier.weight(1f), enabled = !state.running)
                SecondaryButton(R.string.next_plan, { onPlan(1) }, Modifier.weight(1f), enabled = !state.running)
            }
            StudioCard {
                state.chords.getOrNull(state.barIndex)?.let { ChordDiagram(it) }
                state.chords.getOrNull(state.barIndex + 1)?.let { Text(stringResource(R.string.next_chord, stringResource(contentRes(it.nameKey))), style = MaterialTheme.typography.bodyMedium) }
                val status = stringResource(R.string.bar_beat, state.barIndex + 1, plan.chords.size, state.beatIndex + 1)
                Row(Modifier.fillMaxWidth().semantics { stateDescription = status }, horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
                    repeat(4) { index -> Box(Modifier.size(16.dp).background(if (state.phase == PracticePhase.Playing && index == state.beatIndex) Leaf else Mist.copy(alpha = 0.20f), CircleShape)) }
                }
                Text(if (state.phase == PracticePhase.CountIn) stringResource(R.string.count_in, state.countInBeat) else status, style = MaterialTheme.typography.bodyMedium)
            }
            when (state.phase) {
                PracticePhase.Preparing -> { StatusMessage(stringResource(R.string.preparing_audio)); LinearProgressIndicator(Modifier.fillMaxWidth()) }
                PracticePhase.CountIn, PracticePhase.Playing -> {
                    SecondaryButton(R.string.pause, onPause, Modifier.fillMaxWidth(), image = R.drawable.icon_pause)
                    SecondaryButton(R.string.end_session, onEnd, Modifier.fillMaxWidth())
                }
                PracticePhase.Paused -> {
                    StatusMessage(stringResource(R.string.paused))
                    PrimaryButton(R.string.resume, onStart)
                    SecondaryButton(R.string.restart, onRestart, Modifier.fillMaxWidth())
                    SecondaryButton(R.string.end_session, onEnd, Modifier.fillMaxWidth())
                }
                PracticePhase.Finished -> {
                    StatusMessage(stringResource(R.string.finished))
                    SecondaryButton(R.string.restart, onRestart, Modifier.fillMaxWidth())
                }
                PracticePhase.Ready -> {
                    Text(stringResource(if (!state.settings.soundEnabled || state.settings.volumePercent == 0 || state.visualOnly) R.string.visual_practice_hint else R.string.practice_hint), style = MaterialTheme.typography.bodyMedium)
                    PrimaryButton(R.string.start_practice, onStart)
                }
            }
            if (!state.settings.soundEnabled || state.settings.volumePercent == 0 || state.visualOnly) StatusMessage(stringResource(R.string.visual_mode))
            if (state.audioError) {
                StatusMessage(stringResource(R.string.audio_error))
                SecondaryButton(R.string.retry_audio, onStart)
                SecondaryButton(R.string.visual_only, onVisual)
            }
            state.pattern?.let { StudioCard { StrumPatternBoard(it, if (state.phase == PracticePhase.Playing) state.slotIndex else -1) } }
            TempoStepper(state.tempo, onTempo, enabled = !state.running)
            Text(stringResource(R.string.active_time, state.activeMillis / 1000), style = MaterialTheme.typography.bodyMedium)
            plan.lyricsKey?.let { StudioCard { Text(stringResource(contentRes(it)), style = MaterialTheme.typography.bodyLarge) } }
            Text(stringResource(R.string.practice_disclaimer), style = MaterialTheme.typography.bodyMedium)
            SaveConfirmation(state.saveStatus, onRetry)
        }
    }
}
