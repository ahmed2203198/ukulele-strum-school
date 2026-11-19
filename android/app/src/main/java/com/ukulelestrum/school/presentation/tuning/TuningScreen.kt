package com.ukulelestrum.school.presentation.tuning

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ukulelestrum.school.R
import com.ukulelestrum.school.core.di.ServiceLocator
import com.ukulelestrum.school.core.navigation.Screen
import com.ukulelestrum.school.core.theme.*
import com.ukulelestrum.school.core.ui.*
import com.ukulelestrum.school.presentation.common.*

@Composable
fun TuningRoute(onEvent: (UiEvent) -> Unit) {
    val model: TuningViewModel = viewModel(factory = ServiceLocator.factory)
    val state by model.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { model.events.collect(onEvent) }
    PauseOnLeave(model::pause)
    TuningScreen(state, model::select, model::play, model::pause,
        { model.event(UiEvent.Back) }, { model.event(UiEvent.Menu) },
        { model.event(UiEvent.Navigate(Screen.Settings)) })
}

@Composable
fun TuningScreen(state: TuningUiState, onSelect: (Int) -> Unit, onPlay: () -> Unit, onStop: () -> Unit, onBack: () -> Unit, onMenu: () -> Unit, onSettings: () -> Unit) {
    val notes = listOf(R.string.tone_g, R.string.tone_c, R.string.tone_e, R.string.tone_a)
    val frequencies = listOf(R.string.frequency_g, R.string.frequency_c, R.string.frequency_e, R.string.frequency_a)
    val note = stringResource(notes[state.selectedString])
    val transition = rememberInfiniteTransition(label = "sound")
    val pulse by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(1600, easing = LinearEasing)), label = "wave")
    StudioPage(2) {
        ScreenHeader(stringResource(R.string.tuning_title), onBack, onMenu)
        Text(stringResource(R.string.tuning_eyebrow), style = MaterialTheme.typography.labelSmall, color = Leaf)
        StudioCard { Text(stringResource(R.string.tuning_explanation), style = MaterialTheme.typography.bodyLarge) }
        Box(Modifier.fillMaxWidth().height(144.dp), contentAlignment = Alignment.Center) {
            if (state.phase == TonePhase.Playing) Canvas(Modifier.size(144.dp)) {
                repeat(2) { index ->
                    val phase = (pulse + index * 0.5f) % 1f
                    drawCircle(Leaf.copy(alpha = (1 - phase) * 0.5f), (30 + 40 * phase).dp.toPx(), style = Stroke(2.dp.toPx()))
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(note, style = MaterialTheme.typography.displaySmall)
                Text(stringResource(frequencies[state.selectedString]), style = MaterialTheme.typography.bodyLarge)
            }
        }
        repeat(2) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(2) { column ->
                    val index = row * 2 + column
                    val selected = index == state.selectedString
                    val description = stringResource(if (selected) R.string.selected else R.string.not_selected)
                    val accessible = stringResource(R.string.tone_description, stringResource(notes[index]), index + 1, stringResource(frequencies[index]))
                    OutlinedButton({ onSelect(index) }, Modifier.weight(1f).heightIn(min = 80.dp).semantics { stateDescription = description; contentDescription = accessible },
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = if (selected) Leaf else Forest.copy(alpha = 0.6f), contentColor = if (selected) Forest else Mist)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(notes[index]), style = MaterialTheme.typography.titleLarge)
                            Text(stringResource(R.string.string_position, index + 1), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
        val enabled = state.settings.soundEnabled && state.settings.volumePercent > 0
        StatusMessage(stringResource(if (state.phase == TonePhase.Playing) R.string.playing_reference else R.string.tone_stopped, note))
        if (state.phase == TonePhase.Preparing) LinearProgressIndicator(Modifier.fillMaxWidth())
        Text(stringResource(R.string.tuning_tip), style = MaterialTheme.typography.bodyMedium)
        PrimaryButton(R.string.play_tone, onPlay, enabled = enabled && state.phase != TonePhase.Preparing)
        SecondaryButton(R.string.stop, onStop, Modifier.fillMaxWidth(), enabled = state.phase == TonePhase.Playing || state.phase == TonePhase.Preparing)
        if (!enabled) { StatusMessage(stringResource(R.string.enable_sound)); SecondaryButton(R.string.sound_settings, onSettings, Modifier.fillMaxWidth()) }
        if (state.phase == TonePhase.Error) { StatusMessage(stringResource(R.string.audio_error)); SecondaryButton(R.string.retry_audio, onPlay) }
    }
}
