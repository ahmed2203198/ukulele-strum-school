package com.ukulelestrum.school.data.repository

import com.ukulelestrum.school.data.local.CheckpointCodec
import com.ukulelestrum.school.data.local.PreferencesStore
import com.ukulelestrum.school.data.sample.SampleData
import com.ukulelestrum.school.domain.model.*
import com.ukulelestrum.school.domain.repository.*
import com.ukulelestrum.school.domain.usecase.CompletePracticeUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class CourseRepositoryImpl(private val store: PreferencesStore) : CourseRepository, SettingsRepository {
    private val mutex = Mutex()
    private var loaded = false
    private val _progress = MutableStateFlow(CourseProgress())
    override val progress = _progress.asStateFlow()
    private val _settings = MutableStateFlow(SoundSettings())
    override val settings = _settings.asStateFlow()
    private val _saveStatus = MutableStateFlow(SaveStatus.Saved)
    override val saveStatus = _saveStatus.asStateFlow()
    override val lessons = SampleData.lessons
    override val chords = SampleData.chords
    override val patterns = SampleData.patterns
    override val plans = SampleData.plans

    override suspend fun load() = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (!loaded) {
                _settings.value = store.readSettings()
                _progress.value = CheckpointCodec.decode(store.readProgress())
                loaded = true
            }
        }
    }
    private suspend fun change(transform: (CourseProgress) -> CourseProgress) = withContext(Dispatchers.IO) {
        mutex.withLock {
            _saveStatus.value = SaveStatus.Saving
            _progress.value = transform(_progress.value)
            try {
                store.writeProgress(CheckpointCodec.encode(_progress.value))
                _saveStatus.value = SaveStatus.Saved
            } catch (error: Exception) {
                _saveStatus.value = SaveStatus.Failed
                throw error
            }
        }
    }
    override suspend fun selectLesson(id: String) = change { it.copy(lastLesson = id) }
    override suspend fun savePreferences(patternId: String, tempo: Int) = change { it.copy(selectedPattern = patternId, tempo = tempo.coerceIn(40, 120)) }
    override suspend fun saveLessonCheckpoint(checkpoint: LessonCheckpoint?) = change { it.copy(lessonCheckpoint = checkpoint) }
    override suspend fun savePracticeCheckpoint(checkpoint: PracticeCheckpoint?) = change { progress ->
        if (checkpoint == null) progress.copy(practiceCheckpoint = null)
        else if (progress.latestResult?.sessionId == checkpoint.sessionId) progress
        else {
            val accounted = progress.accountedSessions[checkpoint.sessionId] ?: 0L
            progress.copy(practiceCheckpoint = checkpoint,
                practiceMillis = progress.practiceMillis + (checkpoint.activeMillis - accounted).coerceAtLeast(0),
                accountedSessions = progress.accountedSessions + (checkpoint.sessionId to maxOf(accounted, checkpoint.activeMillis)))
        }
    }
    override suspend fun saveResult(result: SessionResult) = change { progress ->
        if (result.isLesson) progress.copy(
            completed = if (result.kind == ResultKind.LessonComplete) progress.completed + result.sourceId else progress.completed,
            bestScores = progress.bestScores + (result.sourceId to maxOf(progress.bestScores[result.sourceId] ?: 0, result.score)),
            latestResult = result, lessonCheckpoint = null
        ) else CompletePracticeUseCase()(progress, result)
    }
    override suspend fun saveSettings(settings: SoundSettings) = withContext(Dispatchers.IO) {
        mutex.withLock {
            _settings.value = settings.copy(volumePercent = settings.volumePercent.coerceIn(0, 100))
            store.writeSettings(_settings.value)
        }
    }
    override suspend fun reset() = withContext(Dispatchers.IO) {
        mutex.withLock {
            store.writeProgress(CheckpointCodec.encode(CourseProgress()))
            _progress.value = CourseProgress()
            _saveStatus.value = SaveStatus.Saved
        }
    }
}
