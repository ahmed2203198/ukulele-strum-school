package com.ukulelestrum.school.domain.usecase

import com.ukulelestrum.school.domain.model.*

class EvaluateAnswerUseCase {
    operator fun invoke(question: KnowledgeQuestion, answerId: String): Boolean = question.correctId == answerId
    fun unlocked(lessons: List<Lesson>, completed: Set<String>, id: String): Boolean {
        val index = lessons.indexOfFirst { it.id == id }
        return index == 0 || (index > 0 && lessons[index - 1].id in completed)
    }
}
