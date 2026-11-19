package com.ukulelestrum.school.data.local

import android.content.Context
import com.ukulelestrum.school.domain.model.SoundSettings

class PreferencesStore(context: Context) {
    private val appContext = context.applicationContext
    private val preferences by lazy { appContext.getSharedPreferences("ukulele_strum_school_preferences", Context.MODE_PRIVATE) }

    fun readProgress(): String? = preferences.getString("course_progress", null)
    fun readSettings(): SoundSettings = SoundSettings(
        runCatching { preferences.getBoolean("sound_enabled", true) }.getOrDefault(true),
        runCatching { preferences.getInt("volume_percent", 70) }.getOrDefault(70).coerceIn(0, 100),
        runCatching { preferences.getBoolean("haptics_enabled", true) }.getOrDefault(true)
    )
    fun writeProgress(value: String) {
        check(preferences.edit().putInt("schema_version", 1).putString("course_progress", value).commit())
    }
    fun writeSettings(value: SoundSettings) {
        check(preferences.edit().putBoolean("sound_enabled", value.soundEnabled)
            .putInt("volume_percent", value.volumePercent).putBoolean("haptics_enabled", value.hapticsEnabled).commit())
    }
}
