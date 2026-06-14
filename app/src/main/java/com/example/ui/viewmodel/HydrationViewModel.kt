package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiClient
import com.example.data.database.AppDatabase
import com.example.data.model.Achievement
import com.example.data.model.UserProfile
import com.example.data.model.WaterLog
import com.example.data.repository.HydrationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class HydrationViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "HydrationViewModel"
    private val repository: HydrationRepository

    // Reactive Flows from Database
    val userProfile: StateFlow<UserProfile?>
    val allWaterLogs: StateFlow<List<WaterLog>>
    val achievements: StateFlow<List<Achievement>>
    
    // Water logs for today
    private val _todayDateStr = MutableStateFlow(getFormattedDate(System.currentTimeMillis()))
    val todayWaterLogs: StateFlow<List<WaterLog>>

    // Onboarding Form States
    var onboardingStep by mutableIntStateOf(1)
        private set
    var tempProfile by mutableStateOf(UserProfile())
        private set

    // AI Coaching States
    var aiInsightText by mutableStateOf("Initializing your Smart AI Hydration Coach...")
        private set
    var isLoadingInsight by mutableStateOf(false)
        private set
    
    // Active UI Messages (Toast / Snackbars)
    var gamificationMessage by mutableStateOf<String?>(null)
        private set

    init {
        val database = AppDatabase.getDatabase(application)
        repository = HydrationRepository(database.hydrationDao())

        // Hook up database states
        userProfile = repository.userProfile.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        allWaterLogs = repository.allWaterLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        achievements = repository.achievements.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        todayWaterLogs = _todayDateStr.flatMapLatest { date ->
            repository.getLogsForDate(date)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Initial launch preparations or seeds
        viewModelScope.launch {
            repository.seedAchievementsIfNeeded()
            generateDailyCoachInsight()
        }
    }

    // --- Onboarding Navigation & Form bindings ---
    fun updateName(name: String) {
        tempProfile = tempProfile.copy(name = name)
    }

    fun updateGender(gender: String) {
        tempProfile = tempProfile.copy(gender = gender)
    }

    fun updateAge(age: Int) {
        tempProfile = tempProfile.copy(age = age)
    }

    fun updateHeight(height: Int) {
        tempProfile = tempProfile.copy(heightCm = height)
    }

    fun updateWeight(weight: Int) {
        tempProfile = tempProfile.copy(weightKg = weight)
    }

    fun updateActivityLevel(level: String) {
        tempProfile = tempProfile.copy(activityLevel = level)
    }

    fun updateHydrationGoal(goal: String) {
        tempProfile = tempProfile.copy(hydrationGoal = goal)
    }

    fun updateClimate(climate: String) {
        tempProfile = tempProfile.copy(climate = climate)
    }

    fun updateHealthConditions(conditions: String) {
        tempProfile = tempProfile.copy(healthConditions = conditions)
    }

    fun updateWakeTime(time: String) {
        tempProfile = tempProfile.copy(wakeUpTime = time)
    }

    fun updateSleepTime(time: String) {
        tempProfile = tempProfile.copy(sleepTime = time)
    }

    fun nextOnboardingStep() {
        if (onboardingStep < 11) {
            onboardingStep++
        } else {
            completeOnboarding()
        }
    }

    fun previousOnboardingStep() {
        if (onboardingStep > 1) {
            onboardingStep--
        }
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            repository.saveUserProfile(tempProfile)
            generateDailyCoachInsight()
        }
    }

    fun resetData() {
        viewModelScope.launch {
            repository.clearAllData()
            onboardingStep = 1
            tempProfile = UserProfile()
            generateDailyCoachInsight()
        }
    }

    fun clearTodayLogs() {
        viewModelScope.launch {
            repository.clearWaterLogsForDate(_todayDateStr.value)
            generateDailyCoachInsight()
        }
    }

    // --- Hydration Logging ---
    fun logWaterIntake(volumeMl: Int, containerType: String) {
        viewModelScope.launch {
            val message = repository.logWater(volumeMl, containerType)
            gamificationMessage = message
            generateDailyCoachInsight() // Re-evaluate coach insight on progress change
        }
    }

    fun clearGamificationMessage() {
        gamificationMessage = null
    }

    fun deleteWaterLog(log: WaterLog) {
        viewModelScope.launch {
            repository.deleteWaterLog(log)
            generateDailyCoachInsight()
        }
    }

    fun updateTheme(themeName: String) {
        viewModelScope.launch {
            val current = userProfile.value
            if (current != null) {
                repository.updateUserProfileDirect(current.copy(chosenTheme = themeName))
            }
        }
    }

    fun updatePreferences(reminderMode: String, soundEnabled: Boolean, vibrationEnabled: Boolean, unitSystem: String) {
        viewModelScope.launch {
            val current = userProfile.value
            if (current != null) {
                val updatedGoal = repository.calculateDailyTargetMl(current.copy(unitSystem = unitSystem))
                repository.updateUserProfileDirect(
                    current.copy(
                        reminderMode = reminderMode,
                        soundEnabled = soundEnabled,
                        vibrationEnabled = vibrationEnabled,
                        unitSystem = unitSystem,
                        dailyGoalMl = updatedGoal
                    )
                )
            }
        }
    }

    fun updateDailyGoalOverride(ml: Int) {
        viewModelScope.launch {
            val current = userProfile.value
            if (current != null) {
                repository.updateUserProfileDirect(current.copy(dailyGoalMl = ml))
            }
        }
    }

    // --- AI Coaching & Chat Assistant ---
    fun generateDailyCoachInsight() {
        val currentProfile = userProfile.value ?: tempProfile
        val loggedVolume = todayWaterLogs.value.sumOf { it.volumeMl }

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        // Compose dynamic details for the coach
        val expected = if (hour < 8) 0 else ((currentProfile.dailyGoalMl / 16.0) * (hour - 7)).toInt()

        viewModelScope.launch {
            isLoadingInsight = true
            val prompt = """
                You are Hydra AI, an expert, encouraging, futuristic and biological smart hydration coach.
                The user's current progress is $loggedVolume ml towards their target of ${currentProfile.dailyGoalMl} ml.
                The current hour is $hour:00.
                Based on normal waking hours, local expectation for this hour is $expected ml of intake.
                User facts: Name: ${currentProfile.name}, Goal: ${currentProfile.hydrationGoal}, Climate: ${currentProfile.climate}, Activity Level: ${currentProfile.activityLevel}.
                Generate a 1-sentence or 2-sentence micro-coaching motivation prompt tailored specifically to this. Keep it biological, futuristic or friendly!
            """.trimIndent()

            val rawResult = GeminiClient.generateHydrationInsight(prompt)
            aiInsightText = rawResult
            isLoadingInsight = false
        }
    }

    fun askAiCoach(question: String, onResponse: (String) -> Unit) {
        val currentProfile = userProfile.value ?: tempProfile
        val loggedVolume = todayWaterLogs.value.sumOf { it.volumeMl }
        val totalVolumeAcrossDatabase = allWaterLogs.value.sumOf { it.volumeMl }

        viewModelScope.launch {
            val prompt = """
                You are Hydra AI, the user's smart hydration assistant coach.
                User profile details:
                - Name: ${currentProfile.name}
                - Age: ${currentProfile.age}
                - Height: ${currentProfile.heightCm} cm, Weight: ${currentProfile.weightKg} kg
                - Purpose: ${currentProfile.hydrationGoal}
                - Activity level: ${currentProfile.activityLevel}
                - Active climate: ${currentProfile.climate}
                - Health conditions: ${currentProfile.healthConditions}
                - Current progress today: $loggedVolume ml out of ${currentProfile.dailyGoalMl} ml.
                - Lifetime logged volume: $totalVolumeAcrossDatabase ml.
                
                The user asks: "$question"
                Provide a professional, highly encouraging health expert response. Mention biological facts where helpful.
            """.trimIndent()

            val response = GeminiClient.generateHydrationInsight(prompt)
            onResponse(response)
        }
    }

    // --- History Exporting ---
    fun getExportString(): String {
        val logs = allWaterLogs.value
        if (logs.isEmpty()) return "No water logs recorded yet."
        
        val builder = StringBuilder()
        builder.append("ID,Date,Time,Volume(ml),Container\n")
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        logs.forEach { log ->
            builder.append("${log.id},${log.logDate},${sdf.format(Date(log.timestamp))},${log.volumeMl},${log.containerType}\n")
        }
        return builder.toString()
    }

    private fun getFormattedDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
