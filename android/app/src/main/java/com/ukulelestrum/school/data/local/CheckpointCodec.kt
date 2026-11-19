package com.ukulelestrum.school.data.local

import com.ukulelestrum.school.data.sample.SampleData
import com.ukulelestrum.school.domain.model.*
import org.json.JSONArray
import org.json.JSONObject

object CheckpointCodec {
    private fun JSONObject.strings(key: String): List<String> {
        val array = optJSONArray(key) ?: return emptyList()
        return (0 until array.length()).map { array.optString(it) }
    }
    private fun JSONObject.longMap(key: String): Map<String, Long> {
        val value = optJSONObject(key) ?: return emptyMap()
        return value.keys().asSequence().associateWith { value.optLong(it).coerceAtLeast(0) }
    }
    fun encode(value: CourseProgress): String = JSONObject().apply {
        put("completed_lesson_ids", JSONArray(value.completed.toList()))
        put("best_score_by_lesson", JSONObject(value.bestScores))
        put("last_selected_lesson_id", value.lastLesson)
        put("selected_pattern_id", value.selectedPattern)
        put("preferred_tempo_bpm", value.tempo)
        put("total_practice_millis", value.practiceMillis)
        put("completed_practice_sessions", value.practiceSessions)
        put("accounted_sessions", JSONObject(value.accountedSessions))
        put("finished_sessions", JSONArray(value.finishedSessions.toList()))
        value.latestResult?.let { r -> put("latest_result", JSONObject().apply {
            put("id", r.id); put("source", r.sourceId); put("kind", r.kind.name)
            put("correct", r.correct); put("answered", r.answered); put("correction", r.correctionKey)
            put("bars", r.completedBars); put("total", r.totalBars); put("millis", r.activeMillis); put("session", r.sessionId)
        }) }
        value.lessonCheckpoint?.let { c -> put("active_lesson_checkpoint", JSONObject().apply {
            put("id", c.lessonId); put("answers", JSONArray(c.answers)); put("checking", c.checking); put("attempt", c.attemptId)
        }) }
        value.practiceCheckpoint?.let { c -> put("active_practice_checkpoint", JSONObject().apply {
            put("id", c.sessionId); put("plan", c.planId); put("pattern", c.patternId); put("tempo", c.tempo)
            put("frame", c.chartFrame); put("millis", c.activeMillis)
        }) }
    }.toString()

    fun decode(raw: String?): CourseProgress {
        if (raw == null) return CourseProgress()
        val o = JSONObject(raw)
        val ids = SampleData.lessons.map { it.id }.toSet()
        val patterns = SampleData.patterns.map { it.id }.toSet()
        val result = o.optJSONObject("latest_result")?.let { r -> runCatching {
            val kind = ResultKind.valueOf(r.getString("kind"))
            val correct = r.optInt("correct").coerceIn(0, 4)
            val source = r.getString("source")
            require(source in ids || SampleData.plans.any { it.id == source })
            val total = r.optInt("total").coerceIn(0, 8)
            val answered = r.optInt("answered").coerceIn(correct, 4)
            if (kind == ResultKind.LessonComplete) require(correct == 4 && answered == 4)
            if (kind == ResultKind.LessonNeedsReview) require(correct < 4 && answered == correct + 1)
            if (kind == ResultKind.PracticeComplete) require(total > 0 && r.optInt("bars") == total)
            SessionResult(r.getString("id"), source, kind, correct, answered,
                r.optString("correction").takeIf { it.isNotEmpty() && it != "null" },
                r.optInt("bars").coerceIn(0, total), total, r.optLong("millis").coerceAtLeast(0), r.getString("session"))
        }.getOrNull() }
        val lessonCheckpoint = o.optJSONObject("active_lesson_checkpoint")?.let { c ->
            val lesson = SampleData.lessons.find { it.id == c.optString("id") }
            lesson?.let {
                val answers = c.strings("answers").take(4)
                if (answers.withIndex().all { (i, id) -> lesson.questions[i].options.any { it.id == id } })
                    LessonCheckpoint(lesson.id, answers, c.optBoolean("checking"), c.optString("attempt")) else null
            }
        }
        val practiceCheckpoint = o.optJSONObject("active_practice_checkpoint")?.let { c ->
            val plan = SampleData.plans.find { it.id == c.optString("plan") }
            if (plan == null || c.optString("pattern") !in patterns) null else {
                val tempo = (c.optInt("tempo", 60).coerceIn(40, 120) / 4) * 4
                val maxFrame = (plan.chords.size * 4.0 * 60 / tempo * 44100).toLong()
                PracticeCheckpoint(c.optString("id"), plan.id, c.optString("pattern"), tempo,
                    c.optLong("frame").coerceIn(0, maxFrame), c.optLong("millis").coerceIn(0, maxFrame * 1000 / 44100))
            }
        }
        return CourseProgress(
            completed = o.strings("completed_lesson_ids").filter { it in ids }.toSet(),
            bestScores = o.longMap("best_score_by_lesson").filter { it.key in ids && it.value in 0L..100L && it.value % 25L == 0L }.mapValues { it.value.toInt() },
            lastLesson = o.optString("last_selected_lesson_id").takeIf { it in ids } ?: "lesson_c",
            selectedPattern = o.optString("selected_pattern_id").takeIf { it in patterns } ?: "steady_fours",
            tempo = (o.optInt("preferred_tempo_bpm", 60).coerceIn(40, 120) / 4) * 4,
            practiceMillis = o.optLong("total_practice_millis").coerceAtLeast(0),
            practiceSessions = o.optInt("completed_practice_sessions").coerceAtLeast(0), latestResult = result,
            accountedSessions = o.longMap("accounted_sessions"), finishedSessions = o.strings("finished_sessions").toSet(),
            lessonCheckpoint = lessonCheckpoint, practiceCheckpoint = practiceCheckpoint
        )
    }
}
