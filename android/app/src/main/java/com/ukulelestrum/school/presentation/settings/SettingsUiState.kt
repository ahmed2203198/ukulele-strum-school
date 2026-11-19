package com.ukulelestrum.school.presentation.settings

import com.ukulelestrum.school.domain.model.*

data class SettingsUiState(val loading: Boolean = true, val settings: SoundSettings = SoundSettings(), val saveStatus: SaveStatus = SaveStatus.Saved, val showSaved: Boolean = false, val showReset: Boolean = false, val resetting: Boolean = false, val resetError: Boolean = false, val readError: Boolean = false)
