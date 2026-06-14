package com.example.data.dao

import androidx.room.*
import com.example.data.model.Achievement
import com.example.data.model.UserProfile
import com.example.data.model.WaterLog
import kotlinx.coroutines.flow.Flow

@Dao
interface HydrationDao {

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileDirect(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)

    @Update
    suspend fun updateUserProfile(profile: UserProfile)

    @Query("SELECT * FROM water_logs ORDER BY timestamp DESC")
    fun getAllWaterLogsFlow(): Flow<List<WaterLog>>

    @Query("SELECT * FROM water_logs WHERE logDate = :date ORDER BY timestamp DESC")
    fun getWaterLogsForDateFlow(date: String): Flow<List<WaterLog>>

    @Query("SELECT * FROM water_logs WHERE logDate = :date ORDER BY timestamp DESC")
    suspend fun getWaterLogsForDateDirect(date: String): List<WaterLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(log: WaterLog): Long

    @Delete
    suspend fun deleteWaterLog(log: WaterLog)

    @Query("DELETE FROM water_logs WHERE id = :id")
    suspend fun deleteWaterLogById(id: Long)

    @Query("DELETE FROM water_logs")
    suspend fun clearAllWaterLogs()

    @Query("DELETE FROM water_logs WHERE logDate = :date")
    suspend fun deleteWaterLogsForDate(date: String)

    @Query("SELECT * FROM achievements ORDER BY id ASC")
    fun getAllAchievementsFlow(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements")
    suspend fun getAllAchievementsDirect(): List<Achievement>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAchievements(achievements: List<Achievement>)

    @Update
    suspend fun updateAchievement(achievement: Achievement)
}
