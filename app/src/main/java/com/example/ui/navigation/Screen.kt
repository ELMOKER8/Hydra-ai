package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("logo_splash")
    object Onboarding : Screen("onboarding_questions")
    object Main : Screen("main_dashboard_hub")
}

sealed class MainTab(val route: String, val title: String) {
    object Dashboard : MainTab("dashboard_progress", "Dashboard")
    object Statistics : MainTab("clinical_statistics", "Metrics")
    object AiCoach : MainTab("ai_clinical_insights", "AI Coach")
    object Achievements : MainTab("gamified_achievements", "Medals")
    object History : MainTab("hydration_history", "Logs")
    object Settings : MainTab("user_preferences", "Settings")
}
