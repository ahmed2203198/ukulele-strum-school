package com.ukulelestrum.school.presentation.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.ukulelestrum.school.core.navigation.Screen
import com.ukulelestrum.school.core.theme.*
import com.ukulelestrum.school.core.ui.*
import com.ukulelestrum.school.presentation.common.*

@Composable
fun GameRoute(lessonId: String, replay: Int, onEvent: (UiEvent) -> Unit) {
    val model: GameViewModel = viewModel(factory = ServiceLocator.factory)
    val state by model.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(lessonId, replay) { model.open(lessonId, replay) }
    LaunchedEffect(Unit) { model.events.collect(onEvent) }
    PauseOnLeave(model::pause)
    GameScreen(state, model::startCheck, model::answer, model::review, model::selectChord, model::preview, model::navigate,
        { model.pause(); onEvent(UiEvent.Back) }, { model.pause(); onEvent(UiEvent.Menu) }, model::retrySave)
}

@Composable
fun GameScreen(state: GameUiState, onCheck: () -> Unit, onAnswer: (String) -> Unit, onReview: () -> Unit, onChord: (Int) -> Unit, onPreview: () -> Unit, onNavigate: (Screen) -> Unit, onBack: () -> Unit, onMenu: () -> Unit, onRetrySave: () -> Unit) {
    StudioPage {
        ScreenHeader(stringResource(R.string.lesson_number, state.ordinal), onBack, onMenu)
        if (state.loading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        } else if (state.lesson == null) {
            StatusMessage(stringResource(R.string.catalog_error))
            SecondaryButton(R.string.menu, onMenu)
        } else {
            val lesson = state.lesson
            Text(stringResource(contentRes(lesson.titleKey)), style = MaterialTheme.typography.headlineMedium)
            if (state.phase == LessonPhase.Checking || state.phase == LessonPhase.AnswerFeedback) {
                val questionIndex = if (state.phase == LessonPhase.AnswerFeedback) state.answers.size - 1 else state.answers.size
                val question = lesson.questions.getOrNull(questionIndex)
                if (question != null) {
                    Text(stringResource(R.string.question_progress, questionIndex + 1, lesson.questions.size), color = Leaf, style = MaterialTheme.typography.labelLarge)
                    StudioCard {
                        Text(stringResource(contentRes(question.key)), style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
                    }
                    question.options.forEach { option ->
                        val selected = state.selectedAnswer == option.id
                        val answerState = stringResource(if (selected) { if (state.feedbackCorrect) R.string.correct_feedback else R.string.needs_review } else R.string.answer_available)
                        OutlinedButton({ onAnswer(option.id) }, Modifier.fillMaxWidth().heightIn(min = 54.dp).semantics { stateDescription = answerState },
                            enabled = state.phase == LessonPhase.Checking, shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected && state.feedbackCorrect) Leaf else Mist.copy(alpha = 0.30f)),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = if (selected) Lagoon else Forest.copy(alpha = 0.70f), contentColor = Mist, disabledContentColor = Mist.copy(alpha = 0.75f))) {
                            Text(stringResource(contentRes(option.textKey)), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    if (state.phase == LessonPhase.AnswerFeedback) StatusMessage(stringResource(if (state.feedbackCorrect) R.string.correct_feedback else R.string.wrong_feedback))
                    SecondaryButton(R.string.review_lesson, onReview)
                }
            } else {
                if (state.phase == LessonPhase.Completed) {
                    StatusMessage(stringResource(R.string.finished_attempt))
                    state.result?.let { StatusMessage(stringResource(R.string.check_score, it.correct)) }
                }
                StudioCard {
                    state.chords.getOrNull(state.chordIndex)?.let { ChordDiagram(it) }
                    if (state.chords.size > 1) state.chords.forEachIndexed { index, chord ->
                        OutlinedButton({ onChord(index) }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                            Text(stringResource(contentRes(chord.nameKey + "_action")))
                        }
                    }
                }
                Text(stringResource(R.string.check_hint), style = MaterialTheme.typography.bodyMedium)
                PrimaryButton(if (state.phase == LessonPhase.Completed) R.string.try_check_again else R.string.start_check, onCheck)
                StudioCard {
                    Text(stringResource(contentRes(lesson.guideKey)), style = MaterialTheme.typography.bodyLarge)
                    state.plan?.lyricsKey?.let { Text(stringResource(contentRes(it)), style = MaterialTheme.typography.bodyLarge) }
                    if (lesson.id == "lesson_island") state.pattern?.let { StrumPatternBoard(it) }
                }
                if (state.settings.soundEnabled && state.settings.volumePercent > 0) SecondaryButton(if (state.previewing) R.string.stop else R.string.hear_chord, onPreview, Modifier.fillMaxWidth())
                else { StatusMessage(stringResource(R.string.sound_off)); SecondaryButton(R.string.sound_settings, { onNavigate(Screen.Settings) }) }
                if (state.audioError) { StatusMessage(stringResource(R.string.audio_error)); SecondaryButton(R.string.retry_audio, onPreview) }
                SecondaryButton(R.string.strum_patterns, { onNavigate(Screen.StrumPatterns(lesson.patternId)) }, Modifier.fillMaxWidth())
                state.plan?.let { plan -> SecondaryButton(R.string.practice, { onNavigate(Screen.Practice(plan.id, plan.patternId, plan.tempo)) }, Modifier.fillMaxWidth()) }
                SecondaryButton(R.string.tuning, { onNavigate(Screen.Tuning) }, Modifier.fillMaxWidth())
                SaveConfirmation(state.saveStatus, onRetrySave)
            }
        }
    }
}
