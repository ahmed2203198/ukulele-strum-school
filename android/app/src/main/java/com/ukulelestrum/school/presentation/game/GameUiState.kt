package com.ukulelestrum.school.presentation.game

import com.ukulelestrum.school.domain.model.*

enum class LessonPhase { Studying, Checking, AnswerFeedback, Completed }
data class GameUiState(val loading: Boolean = true, val lesson: Lesson? = null, val ordinal: Int = 1, val chords: List<Chord> = emptyList(), val chordIndex: Int = 0, val pattern: StrumPattern? = null, val plan: PracticePlan? = null, val phase: LessonPhase = LessonPhase.Studying, val answers: List<String> = emptyList(), val correctCount: Int = 0, val selectedAnswer: String? = null, val feedbackCorrect: Boolean = false, val result: SessionResult? = null, val saveStatus: SaveStatus = SaveStatus.Saved, val settings: SoundSettings = SoundSettings(), val previewing: Boolean = false, val audioError: Boolean = false)
