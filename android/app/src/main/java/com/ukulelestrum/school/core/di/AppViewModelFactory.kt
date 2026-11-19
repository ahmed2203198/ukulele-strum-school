package com.ukulelestrum.school.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.ukulelestrum.school.domain.audio.AudioPlayback
import com.ukulelestrum.school.domain.repository.*
import com.ukulelestrum.school.presentation.app.AppViewModel
import com.ukulelestrum.school.presentation.game.GameViewModel
import com.ukulelestrum.school.presentation.gameover.GameOverViewModel
import com.ukulelestrum.school.presentation.menu.MenuViewModel
import com.ukulelestrum.school.presentation.practice.PracticeViewModel
import com.ukulelestrum.school.presentation.settings.SettingsViewModel
import com.ukulelestrum.school.presentation.splash.SplashViewModel
import com.ukulelestrum.school.presentation.strumpatterns.StrumPatternsViewModel
import com.ukulelestrum.school.presentation.tuning.TuningViewModel

class AppViewModelFactory(private val course: CourseRepository, private val settings: SettingsRepository, private val audio: AudioPlayback) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val model: ViewModel = when (modelClass) {
            AppViewModel::class.java -> AppViewModel(extras.createSavedStateHandle())
            SplashViewModel::class.java -> SplashViewModel()
            MenuViewModel::class.java -> MenuViewModel(course)
            GameViewModel::class.java -> GameViewModel(course, settings, audio)
            GameOverViewModel::class.java -> GameOverViewModel(course)
            StrumPatternsViewModel::class.java -> StrumPatternsViewModel(course, settings, audio, extras.createSavedStateHandle())
            PracticeViewModel::class.java -> PracticeViewModel(course, settings, audio, extras.createSavedStateHandle())
            TuningViewModel::class.java -> TuningViewModel(settings, audio, extras.createSavedStateHandle())
            SettingsViewModel::class.java -> SettingsViewModel(settings, course)
            else -> error(modelClass.name)
        }
        return model as T
    }
}
