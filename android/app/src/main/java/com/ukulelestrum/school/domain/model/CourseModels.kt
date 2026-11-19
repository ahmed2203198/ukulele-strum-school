package com.ukulelestrum.school.domain.model

data class Chord(val id: String, val nameKey: String, val frets: List<Int>, val fingers: List<Int>, val descriptionKey: String, val tipsKey: String)
data class AnswerOption(val id: String, val textKey: String)
data class KnowledgeQuestion(val key: String, val options: List<AnswerOption>, val correctId: String)
data class Lesson(val id: String, val titleKey: String, val minutes: Int, val chordIds: List<String>, val patternId: String, val planId: String, val questions: List<KnowledgeQuestion>, val guideKey: String)
data class StrumPattern(val id: String, val nameKey: String, val slots: List<Int>, val guideKey: String, val spokenKey: String)
data class PracticePlan(val id: String, val nameKey: String, val chords: List<String>, val tempo: Int, val patternId: String, val lyricsKey: String? = null)
data class SoundSettings(val soundEnabled: Boolean = true, val volumePercent: Int = 70, val hapticsEnabled: Boolean = true)
data class LessonCheckpoint(val lessonId: String, val answers: List<String> = emptyList(), val checking: Boolean = false, val attemptId: String = "")
data class PracticeCheckpoint(val sessionId: String, val planId: String, val patternId: String, val tempo: Int, val chartFrame: Long = 0, val activeMillis: Long = 0)
enum class ResultKind { LessonComplete, LessonNeedsReview, PracticeComplete, PracticeEndedEarly }
data class SessionResult(val id: String, val sourceId: String, val kind: ResultKind, val correct: Int = 0, val answered: Int = 0, val correctionKey: String? = null, val completedBars: Int = 0, val totalBars: Int = 0, val activeMillis: Long = 0, val sessionId: String = id) {
    val score: Int get() = correct * 25
    val isLesson: Boolean get() = kind == ResultKind.LessonComplete || kind == ResultKind.LessonNeedsReview
}
data class CourseProgress(val completed: Set<String> = emptySet(), val bestScores: Map<String, Int> = emptyMap(), val lastLesson: String = "lesson_c", val selectedPattern: String = "steady_fours", val tempo: Int = 60, val practiceMillis: Long = 0, val practiceSessions: Int = 0, val latestResult: SessionResult? = null, val accountedSessions: Map<String, Long> = emptyMap(), val finishedSessions: Set<String> = emptySet(), val lessonCheckpoint: LessonCheckpoint? = null, val practiceCheckpoint: PracticeCheckpoint? = null)
enum class SaveStatus { Saved, Saving, Failed }
