package com.ukulelestrum.school.core.di

import android.content.Context
import com.ukulelestrum.school.data.audio.AudioTrackPlayback
import com.ukulelestrum.school.data.local.PreferencesStore
import com.ukulelestrum.school.data.repository.CourseRepositoryImpl

object ServiceLocator {
    private var instance: AppViewModelFactory? = null
    val factory: AppViewModelFactory get() = checkNotNull(instance)

    @Synchronized
    fun initialize(context: Context) {
        if (instance != null) return
        val storage = CourseRepositoryImpl(PreferencesStore(context.applicationContext))
        instance = AppViewModelFactory(storage, storage, AudioTrackPlayback())
    }
}
