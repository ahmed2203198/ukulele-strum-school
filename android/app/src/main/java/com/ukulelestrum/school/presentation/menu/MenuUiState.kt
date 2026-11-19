package com.ukulelestrum.school.presentation.menu

import com.ukulelestrum.school.domain.model.*

enum class LoadStatus { Loading, Ready, ReadError }
data class MenuUiState(val loadStatus: LoadStatus = LoadStatus.Loading, val lessons: List<Lesson> = emptyList(), val progress: CourseProgress = CourseProgress(), val nextLesson: Lesson? = null, val prerequisiteKey: String? = null)
