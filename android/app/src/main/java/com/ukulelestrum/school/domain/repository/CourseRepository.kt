package com.ukulelestrum.school.domain.repository

import com.ukulelestrum.school.domain.model.*
import kotlinx.coroutines.flow.StateFlow

interface CourseRepository {
    val progress: StateFlow<CourseProgress>
    val saveStatus: StateFlow<SaveStatus>
    val lessons: List<Lesson>
    val chords: List<Chord>
    val patterns: List<StrumPattern>
    val plans: List<PracticePlan>
    suspend fun load()
    suspend fun selectLesson(id: String)
    suspend fun savePreferences(patternId: String, tempo: Int)
    suspend fun saveLessonCheckpoint(checkpoint: LessonCheckpoint?)
    suspend fun savePracticeCheckpoint(checkpoint: PracticeCheckpoint?)
    suspend fun saveResult(result: SessionResult)
    suspend fun reset()
}

interface SettingsRepository {
    val settings: StateFlow<SoundSettings>
    suspend fun load()
    suspend fun saveSettings(settings: SoundSettings)
}
