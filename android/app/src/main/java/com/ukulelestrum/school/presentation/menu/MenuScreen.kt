package com.ukulelestrum.school.presentation.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
fun MenuRoute(onEvent: (UiEvent) -> Unit) {
    val model: MenuViewModel = viewModel(factory = ServiceLocator.factory)
    val state by model.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { model.events.collect(onEvent) }
    MenuScreen(state, model::play, model::select, model::navigate, model::refresh)
}

@Composable
fun MenuScreen(state: MenuUiState, onPlay: () -> Unit, onLesson: (String) -> Unit, onNavigate: (Screen) -> Unit, onRetry: () -> Unit) {
    StudioPage {
        Text(stringResource(R.string.course_eyebrow), style = MaterialTheme.typography.labelSmall, color = Leaf)
        Text(stringResource(R.string.course_title), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
        StudioCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.hero_title), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.course_schedule), style = MaterialTheme.typography.bodyMedium, color = Mist.copy(alpha = 0.85f))
                }
                Image(painterResource(R.drawable.logo), null, Modifier.size(88.dp), contentScale = ContentScale.Fit)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf(painterResource(R.drawable.sprite_1), painterResource(R.drawable.sprite_2), painterResource(R.drawable.sprite_3),
                    painterResource(R.drawable.sprite_4), painterResource(R.drawable.sprite_5)).forEach { painter ->
                    Image(painter, null, Modifier.size(36.dp), contentScale = ContentScale.Fit)
                }
            }
        }
        if (state.progress.completed.isEmpty()) StatusMessage(stringResource(R.string.first_chord))
        else StatusMessage(stringResource(R.string.course_progress, state.progress.completed.size, state.lessons.size))
        state.prerequisiteKey?.let { StatusMessage(stringResource(R.string.prerequisite, stringResource(contentRes(it)))) }
        state.nextLesson?.let { Text(stringResource(R.string.next_lesson, stringResource(contentRes(it.titleKey))), style = MaterialTheme.typography.bodyLarge) }
        PrimaryButton(R.string.play, onPlay, enabled = state.nextLesson != null && state.loadStatus != LoadStatus.Loading)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SecondaryButton(R.string.strum_patterns, { onNavigate(Screen.StrumPatterns(state.progress.selectedPattern)) }, Modifier.weight(1f))
            SecondaryButton(R.string.practice, { onNavigate(Screen.Practice()) }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SecondaryButton(R.string.tuning, { onNavigate(Screen.Tuning) }, Modifier.weight(1f))
            SecondaryButton(R.string.settings, { onNavigate(Screen.Settings) }, Modifier.weight(1f))
        }
        if (state.progress.completed.isNotEmpty() && state.progress.practiceSessions > 0) Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(stringResource(R.string.count_value, state.progress.completed.size), R.string.lessons_stat, Modifier.weight(1f))
            StatCard(stringResource(R.string.count_value, state.progress.practiceSessions), R.string.sessions_stat, Modifier.weight(1f))
        }
        if (state.loadStatus == LoadStatus.ReadError) {
            StatusMessage(stringResource(R.string.read_error))
            SecondaryButton(R.string.retry, onRetry)
        }
        if (state.loadStatus == LoadStatus.Loading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            StatusMessage(stringResource(R.string.loading))
        }
        Text(stringResource(R.string.path_title), style = MaterialTheme.typography.titleLarge)
        state.lessons.forEachIndexed { index, lesson ->
            val completed = lesson.id in state.progress.completed
            val unlocked = index == 0 || state.lessons[index - 1].id in state.progress.completed
            val status = stringResource(if (completed) R.string.completed else if (unlocked) R.string.available else R.string.locked)
            StudioCard(Modifier.semantics { stateDescription = status }) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.ordinal, index + 1), color = if (unlocked) Leaf else Mist.copy(alpha = 0.55f), style = MaterialTheme.typography.headlineMedium)
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(contentRes(lesson.titleKey)), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.lesson_metadata, lesson.minutes, status), style = MaterialTheme.typography.labelSmall)
                    }
                }
                SecondaryButton(if (completed) R.string.review else if (unlocked) R.string.open_lesson else R.string.locked_button, { onLesson(lesson.id) })
            }
        }
        if (state.lessons.isEmpty()) { StatusMessage(stringResource(R.string.catalog_error)); SecondaryButton(R.string.retry, onRetry) }
        Spacer(Modifier.height(12.dp))
    }
}
