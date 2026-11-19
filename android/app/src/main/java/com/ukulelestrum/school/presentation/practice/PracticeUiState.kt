package com.ukulelestrum.school.presentation.practice

import com.ukulelestrum.school.domain.model.*

enum class PracticePhase { Ready, Preparing, CountIn, Playing, Paused, Finished }
data class PracticeUiState(val loading: Boolean = true, val plan: PracticePlan? = null, val planChoices: List<PracticePlan> = emptyList(), val chords: List<Chord> = emptyList(), val pattern: StrumPattern? = null, val tempo: Int = 60, val phase: PracticePhase = PracticePhase.Ready, val chartFrame: Long = 0, val activeMillis: Long = 0, val completedBars: Int = 0, val barIndex: Int = 0, val beatIndex: Int = 0, val slotIndex: Int = -1, val countInBeat: Int = 1, val settings: SoundSettings = SoundSettings(), val visualOnly: Boolean = false, val audioError: Boolean = false, val saveStatus: SaveStatus = SaveStatus.Saved, val result: SessionResult? = null) {
    val running: Boolean get() = phase == PracticePhase.Playing || phase == PracticePhase.CountIn || phase == PracticePhase.Preparing
}
