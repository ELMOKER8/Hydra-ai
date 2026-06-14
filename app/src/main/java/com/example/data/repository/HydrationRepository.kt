package com.example.data.repository

import android.util.Log
import com.example.data.dao.HydrationDao
import com.example.data.model.Achievement
import com.example.data.model.UserProfile
import com.example.data.model.WaterLog
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class HydrationRepository(private val hydrationDao: HydrationDao) {

    private val TAG = "HydrationRepository"

    val userProfile: Flow<UserProfile?> = hydrationDao.getUserProfileFlow()
    val allWaterLogs: Flow<List<WaterLog>> = hydrationDao.getAllWaterLogsFlow()
    val achievements: Flow<List<Achievement>> = hydrationDao.getAllAchievementsFlow()

    fun getLogsForDate(date: String): Flow<List<WaterLog>> {
        return hydrationDao.getWaterLogsForDateFlow(date)
    }

    suspend fun seedAchievementsIfNeeded() {
        val existing = hydrationDao.getAllAchievementsDirect()
        if (existing.isEmpty()) {
            Log.i(TAG, "Database empty. Seeding default hydration achievements.")
            val defaultAchievements = listOf(
                Achievement("first_drink", "First Drink", "Log your first container of water", false, null, 50),
                Achievement("rookie", "Hydration Rookie", "Drink a total of 5,000ml of water", false, null, 100),
                Achievement("streak_3", "3-Day Streak", "Keep your hydration goals met for 3 days in a row", false, null, 100),
                Achievement("streak_7", "7-Day Streak", "Keep your hydration goals met for 7 days in a row", false, null, 150),
                Achievement("streak_14", "14-Day Streak", "Keep your hydration goals met for 14 days in a row", false, null, 200),
                Achievement("streak_30", "30-Day Streak", "Keep your hydration goals met for 30 days in a row", false, null, 300),
                Achievement("streak_100", "100-Day Streak", "Keep your hydration goals met for 100 days in a row", false, null, 500),
                Achievement("master", "Hydration Master", "Reach Level 10 of Hydration Skills", false, null, 200),
                Achievement("champion", "Water Champion", "Reach Level 25 of Hydration Skills", false, null, 500),
                Achievement("perfect_week", "Perfect Week", "Log 100% of your daily water requirement every day for a week", false, null, 250),
                Achievement("perfect_month", "Perfect Month", "Log 100% of your daily water requirement every day for a month", false, null, 500),
                Achievement("early_bird", "Early Bird", "Log a container of water before 7:00 AM", false, null, 50),
                Achievement("night_owl", "Night Owl", "Log a container of water after 11:00 PM", false, null, 50),
                Achievement("consistency_king", "Consistency King", "Log your water intake 5 times in a single day", false, null, 100),
                Achievement("legend", "Hydration Legend", "Log a cumulative total of 50,000ml of water", false, null, 500)
            )
            hydrationDao.insertAchievements(defaultAchievements)
        }
    }

    fun calculateDailyTargetMl(profile: UserProfile): Int {
        val base = 35 * profile.weightKg

        val activityModifier = when (profile.activityLevel) {
            "Sedentary" -> 0
            "Lightly Active" -> 300
            "Moderately Active" -> 500
            "Very Active" -> 800
            "Athlete" -> 1200
            else -> 500
        }

        val climateModifier = when (profile.climate) {
            "Cold" -> 0
            "Mild" -> 200
            "Hot" -> 500
            "Extreme Heat" -> 1000
            else -> 200
        }

        val goalModifier = when (profile.hydrationGoal) {
            "General Health" -> 0
            "Better Skin" -> 250
            "Weight Loss" -> 300
            "Muscle Growth" -> 500
            "Athletic Performance" -> 700
            else -> 0
        }

        val wakeModifier = if (isAwakeLongerThan16(profile.wakeUpTime, profile.sleepTime)) 300 else 0

        return base + activityModifier + climateModifier + goalModifier + wakeModifier
    }

    private fun isAwakeLongerThan16(wake: String, sleep: String): Boolean {
        return try {
            val wakeParts = wake.split(":").map { it.trim().toInt() }
            val sleepParts = sleep.split(":").map { it.trim().toInt() }
            val wakeMins = wakeParts[0] * 60 + wakeParts[1]
            var sleepMins = sleepParts[0] * 60 + sleepParts[1]
            if (sleepMins < wakeMins) {
                sleepMins += 24 * 60
            }
            val awakeDurationMins = sleepMins - wakeMins
            awakeDurationMins > 16 * 60
        } catch (e: Exception) {
            false
        }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        val calculatedGoal = calculateDailyTargetMl(profile)
        val finalProfile = profile.copy(
            dailyGoalMl = calculatedGoal,
            initialSetupFinished = true
        )
        hydrationDao.insertUserProfile(finalProfile)
        seedAchievementsIfNeeded()
    }

    suspend fun updateUserProfileDirect(profile: UserProfile) {
        hydrationDao.insertUserProfile(profile)
    }

    suspend fun logWater(
        volumeMl: Int,
        containerType: String,
        timestamp: Long = System.currentTimeMillis()
    ): String {
        val dateString = getFormattedDate(timestamp)
        val log = WaterLog(
            volumeMl = volumeMl,
            containerType = containerType,
            timestamp = timestamp,
            logDate = dateString
        )

        hydrationDao.insertWaterLog(log)

        var profile = hydrationDao.getUserProfileDirect() ?: UserProfile()
        var xpEarned = 5
        var levelUpOccurred = false
        val unlockedMessages = mutableListOf<String>()

        val todayLogsBefore = hydrationDao.getWaterLogsForDateDirect(dateString).filter { it.id != log.id }
        val todayIntakeBefore = todayLogsBefore.sumOf { it.volumeMl }
        val todayIntakeAfter = todayIntakeBefore + volumeMl
        val goalMetBefore = todayIntakeBefore >= profile.dailyGoalMl
        val goalMetAfter = todayIntakeAfter >= profile.dailyGoalMl

        if (!goalMetBefore && goalMetAfter) {
            xpEarned += 25
            unlockedMessages.add("🎉 Met Daily Hydration Goal! (+25 XP)")
            
            val yesterdayString = getFormattedDate(timestamp - 24 * 60 * 60 * 1000)
            if (profile.lastLogDate == yesterdayString) {
                val nextStreak = profile.currentStreak + 1
                val maxStreak = maxOf(nextStreak, profile.longestStreak)
                profile = profile.copy(
                    currentStreak = nextStreak,
                    longestStreak = maxStreak,
                    lastLogDate = dateString
                )
            } else if (profile.lastLogDate != dateString) {
                profile = profile.copy(
                    currentStreak = 1,
                    longestStreak = maxOf(1, profile.longestStreak),
                    lastLogDate = dateString
                )
            }
        }

        var finalXp = profile.currentXp + xpEarned
        var finalLevel = profile.currentLevel
        val levelBoundary = 100
        
        while (finalXp >= levelBoundary && finalLevel < 100) {
            finalXp -= levelBoundary
            finalLevel += 1
            levelUpOccurred = true
        }

        if (levelUpOccurred) {
            unlockedMessages.add("⚡ LEVEL UP! You are now Level $finalLevel!")
        }

        profile = profile.copy(
            currentXp = finalXp,
            currentLevel = finalLevel
        )

        val allLogs = hydrationDao.getWaterLogsForDateDirect(dateString)
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val achievementsList = hydrationDao.getAllAchievementsDirect()

        suspend fun unlock(id: String) {
            val ach = achievementsList.find { it.id == id }
            if (ach != null && !ach.isUnlocked) {
                hydrationDao.updateAchievement(ach.copy(isUnlocked = true, unlockedTimestamp = timestamp))
                profile = profile.copy(currentXp = (profile.currentXp + ach.xpReward) % 100, currentLevel = profile.currentLevel + (profile.currentXp + ach.xpReward) / 100)
                unlockedMessages.add("🏆 Milestone: Unlocked ${ach.title}! (+${ach.xpReward} XP)")
            }
        }

        unlock("first_drink")

        if (hour < 7) {
            unlock("early_bird")
        }

        if (hour >= 23) {
            unlock("night_owl")
        }

        if (allLogs.size >= 5) {
            unlock("consistency_king")
        }

        val totalVolumeAcrossDatabase = hydrationDao.getWaterLogsForDateDirect("%").sumOf { it.volumeMl }
        if (totalVolumeAcrossDatabase >= 5000) {
            unlock("rookie")
        }
        if (totalVolumeAcrossDatabase >= 50000) {
            unlock("legend")
        }

        if (profile.currentStreak >= 3) unlock("streak_3")
        if (profile.currentStreak >= 7) unlock("streak_7")
        if (profile.currentStreak >= 14) unlock("streak_14")
        if (profile.currentStreak >= 30) unlock("streak_30")
        if (profile.currentStreak >= 100) unlock("streak_100")

        if (profile.currentLevel >= 10) unlock("master")
        if (profile.currentLevel >= 25) unlock("champion")

        hydrationDao.insertUserProfile(profile)

        return if (unlockedMessages.isNotEmpty()) {
            unlockedMessages.joinToString("\n")
        } else {
            "+$xpEarned XP. Droppy appreciates the drink!"
        }
    }

    suspend fun deleteWaterLog(log: WaterLog) {
        hydrationDao.deleteWaterLog(log)
    }

    suspend fun clearWaterLogsForDate(date: String) {
        hydrationDao.deleteWaterLogsForDate(date)
    }

    suspend fun clearAllData() {
        hydrationDao.clearAllWaterLogs()
        val defaultProfile = UserProfile(id = 1, initialSetupFinished = false)
        hydrationDao.insertUserProfile(defaultProfile)
    }

    private fun getFormattedDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
