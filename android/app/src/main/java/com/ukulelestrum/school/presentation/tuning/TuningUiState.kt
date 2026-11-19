package com.ukulelestrum.school.presentation.tuning

import com.ukulelestrum.school.domain.model.*

enum class TonePhase { Idle, Preparing, Playing, Error }
data class TuningUiState(val selectedString: Int = 0, val settings: SoundSettings = SoundSettings(), val phase: TonePhase = TonePhase.Idle)
