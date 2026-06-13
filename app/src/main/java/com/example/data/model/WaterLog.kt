package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_logs")
data class WaterLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val volumeMl: Int,
    val containerType: String, // Espresso Cup, Tea Cup, Glass, Large Glass, Bottle, Sports Bottle, Large Bottle, Custom
    val timestamp: Long = System.currentTimeMillis(),
    val logDate: String // "YYYY-MM-DD" style
)
