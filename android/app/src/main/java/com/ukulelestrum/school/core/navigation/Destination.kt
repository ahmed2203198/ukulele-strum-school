package com.ukulelestrum.school.core.navigation

sealed interface Screen {
    data object Splash : Screen
    data object Menu : Screen
    data class Game(val lessonId: String) : Screen
    data class GameOver(val resultId: String) : Screen
    data class StrumPatterns(val patternId: String = "steady_fours") : Screen
    data class Practice(val planId: String = "plan_circle", val patternId: String = "steady_fours", val tempo: Int = 60) : Screen
    data object Tuning : Screen
    data object Settings : Screen
}
