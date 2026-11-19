package com.ukulelestrum.school.presentation.strumpatterns

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun StrumPatternsRoute(patternId: String, onEvent: (UiEvent) -> Unit) {
    val model: StrumPatternsViewModel = viewModel(factory = ServiceLocator.factory)
    val state by model.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(patternId) { model.open(patternId) }
    LaunchedEffect(Unit) { model.events.collect(onEvent) }
    PauseOnLeave(model::pause)
    StrumPatternsScreen(state, model::select, model::tempo, model::play, model::practice, model::visualOnly,
        { model.pause(); onEvent(UiEvent.Back) }, { model.pause(); onEvent(UiEvent.Menu) })
}

@Composable
fun StrumPatternsScreen(state: StrumPatternsUiState, onSelect: (String) -> Unit, onTempo: (Int) -> Unit, onPlay: () -> Unit, onPractice: () -> Unit, onVisual: () -> Unit, onBack: () -> Unit, onMenu: () -> Unit) {
    StudioPage(2) {
        ScreenHeader(stringResource(R.string.patterns_title), onBack, onMenu)
        Text(stringResource(R.string.patterns_eyebrow), style = MaterialTheme.typography.labelSmall, color = Leaf)
        state.patterns.forEach { pattern ->
            val selected = state.selected?.id == pattern.id
            val description = stringResource(if (selected) R.string.selected else R.string.not_selected)
            OutlinedButton({ onSelect(pattern.id) }, Modifier.fillMaxWidth().heightIn(min = 56.dp).semantics { stateDescription = description },
                colors = ButtonDefaults.outlinedButtonColors(containerColor = if (selected) Leaf else Forest.copy(alpha = 0.7f), contentColor = if (selected) Forest else Mist)) {
                Text(stringResource(contentRes(pattern.nameKey + "_action")))
            }
        }
        state.selected?.let { StudioCard { StrumPatternBoard(it, state.activeSlot) } }
        TempoStepper(state.tempo, onTempo)
        if (state.phase == PatternPhase.Preparing) { LinearProgressIndicator(Modifier.fillMaxWidth()); StatusMessage(stringResource(R.string.preparing_audio)) }
        if (state.countIn > 0) Text(stringResource(R.string.count_in, state.countIn), style = MaterialTheme.typography.bodyLarge)
        StatusMessage(stringResource(when (state.phase) { PatternPhase.Playing -> R.string.playing; PatternPhase.Paused -> R.string.paused; else -> R.string.idle }))
        if (state.visualOnly || !state.settings.soundEnabled || state.settings.volumePercent == 0) StatusMessage(stringResource(R.string.visual_mode))
        PrimaryButton(if (state.phase == PatternPhase.Playing || state.phase == PatternPhase.Preparing) R.string.stop else R.string.play_pattern, onPlay)
        if (state.audioError) {
            StatusMessage(stringResource(R.string.audio_error))
            SecondaryButton(R.string.retry_audio, onPlay)
            SecondaryButton(R.string.visual_only, onVisual)
        }
        SecondaryButton(R.string.practice, onPractice, Modifier.fillMaxWidth())
    }
}
