package com.ukulelestrum.school.presentation.strumpatterns

import com.ukulelestrum.school.domain.model.*

enum class PatternPhase { Idle, Preparing, Playing, Paused }
data class StrumPatternsUiState(val patterns: List<StrumPattern> = emptyList(), val selected: StrumPattern? = null, val tempo: Int = 60, val phase: PatternPhase = PatternPhase.Idle, val activeSlot: Int = -1, val countIn: Int = 0, val settings: SoundSettings = SoundSettings(), val audioError: Boolean = false, val visualOnly: Boolean = false)
