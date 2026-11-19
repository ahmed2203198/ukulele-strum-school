package com.ukulelestrum.school.presentation.settings

import androidx.compose.foundation.layout.*
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
import com.ukulelestrum.school.core.ui.*
import com.ukulelestrum.school.presentation.common.*
import com.ukulelestrum.school.presentation.dialog.ResetProgressDialog

@Composable
fun SettingsRoute(onEvent: (UiEvent) -> Unit) {
    val model: SettingsViewModel = viewModel(factory = ServiceLocator.factory)
    val state by model.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { model.events.collect(onEvent) }
    SettingsScreen(state, model::sound, model::volume, model::haptics, model::showReset, model::dismissReset, model::reset, model::retry,
        { model.event(UiEvent.Back) }, { model.event(UiEvent.Menu) })
}

@Composable
fun SettingsScreen(state: SettingsUiState, onSound: (Boolean) -> Unit, onVolume: (Int) -> Unit, onHaptics: (Boolean) -> Unit, onShowReset: () -> Unit, onDismiss: () -> Unit, onReset: () -> Unit, onRetry: () -> Unit, onBack: () -> Unit, onMenu: () -> Unit) {
    StudioPage(2) {
        ScreenHeader(stringResource(R.string.settings_title), onBack, onMenu)
        if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (state.readError) { StatusMessage(stringResource(R.string.read_error)); SecondaryButton(R.string.retry, onRetry) }
        StudioCard {
            Text(stringResource(R.string.sound_title), style = MaterialTheme.typography.titleLarge)
            val soundDescription = stringResource(if (state.settings.soundEnabled) R.string.on else R.string.off)
            val soundLabel = stringResource(R.string.sound_label)
            Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).semantics { stateDescription = soundDescription }, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.sound_label), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                Switch(state.settings.soundEnabled, onSound, Modifier.semantics { contentDescription = soundLabel; stateDescription = soundDescription }, enabled = !state.loading)
            }
            Text(stringResource(R.string.volume), style = MaterialTheme.typography.titleMedium)
            StatusMessage(stringResource(R.string.volume_value, state.settings.volumePercent))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton(R.string.quieter, { onVolume(state.settings.volumePercent - 10) }, Modifier.weight(1f), enabled = state.settings.soundEnabled && state.settings.volumePercent > 0 && !state.loading)
                SecondaryButton(R.string.louder, { onVolume(state.settings.volumePercent + 10) }, Modifier.weight(1f), enabled = state.settings.soundEnabled && state.settings.volumePercent < 100 && !state.loading)
            }
            if (!state.settings.soundEnabled) Text(stringResource(R.string.sound_off), style = MaterialTheme.typography.bodyMedium)
            val hapticDescription = stringResource(if (state.settings.hapticsEnabled) R.string.on else R.string.off)
            val hapticLabel = stringResource(R.string.haptics)
            Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).semantics { stateDescription = hapticDescription }, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.haptics), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                Switch(state.settings.hapticsEnabled, onHaptics, Modifier.semantics { contentDescription = hapticLabel; stateDescription = hapticDescription }, enabled = !state.loading)
            }
            Text(stringResource(R.string.haptics_hint), style = MaterialTheme.typography.bodyMedium)
            if (state.showSaved) StatusMessage(stringResource(R.string.saved))
            SaveConfirmation(state.saveStatus, onRetry)
        }
        StudioCard {
            Text(stringResource(R.string.course_data), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.local_data), style = MaterialTheme.typography.bodyLarge)
            SecondaryButton(R.string.reset_progress, onShowReset, Modifier.fillMaxWidth(), enabled = !state.loading)
        }
        SecondaryButton(R.string.menu, onMenu, Modifier.fillMaxWidth(), image = R.drawable.icon_home)
    }
    if (state.showReset) ResetProgressDialog(state.resetting, state.resetError, onDismiss, onReset)
}
