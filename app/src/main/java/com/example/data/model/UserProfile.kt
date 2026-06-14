package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val gender: String = "Male",
    val age: Int = 25,
    val heightCm: Int = 175,
    val weightKg: Int = 70,
    val activityLevel: String = "Moderately Active",
    val hydrationGoal: String = "General Health",
    val climate: String = "Mild",
    val healthConditions: String = "None",
    val wakeUpTime: String = "07:00",
    val sleepTime: String = "23:00",
    val dailyGoalMl: Int = 2500,
    val currentLevel: Int = 1,
    val currentXp: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val initialSetupFinished: Boolean = false,
    val reminderMode: String = "Smart AI",
    val chosenTheme: String = "Ocean Theme",
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val unitSystem: String = "ml",
    val lastLogDate: String = ""
)
