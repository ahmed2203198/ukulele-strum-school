package com.ukulelestrum.school.presentation.gameover

import com.ukulelestrum.school.domain.model.*

data class GameOverUiState(val loading: Boolean = true, val result: SessionResult? = null, val titleKey: String? = null, val nextTitleKey: String? = null, val courseComplete: Boolean = false, val completedLessons: Int = 0, val bestScore: Int = 0, val saveStatus: SaveStatus = SaveStatus.Saved)
