package com.ukulelestrum.school.domain.usecase

import com.ukulelestrum.school.domain.model.*

class CompletePracticeUseCase {
    operator fun invoke(progress: CourseProgress, result: SessionResult): CourseProgress {
        val accounted = progress.accountedSessions[result.sessionId] ?: 0L
        val additional = (result.activeMillis - accounted).coerceAtLeast(0)
        val finished = result.kind == ResultKind.PracticeComplete && result.sessionId !in progress.finishedSessions
        return progress.copy(
            practiceMillis = progress.practiceMillis + additional,
            practiceSessions = progress.practiceSessions + if (finished) 1 else 0,
            accountedSessions = progress.accountedSessions + (result.sessionId to maxOf(accounted, result.activeMillis)),
            finishedSessions = if (finished) progress.finishedSessions + result.sessionId else progress.finishedSessions,
            latestResult = result,
            practiceCheckpoint = null
        )
    }
}
