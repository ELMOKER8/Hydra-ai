package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.HydrationDao
import com.example.data.model.Achievement
import com.example.data.model.UserProfile
import com.example.data.model.WaterLog

@Database(entities = [UserProfile::class, WaterLog::class, Achievement::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun hydrationDao(): HydrationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hydra_ai_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
