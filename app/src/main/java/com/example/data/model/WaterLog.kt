package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_logs")
data class WaterLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val volumeMl: Int,
    val containerType: String,
    val timestamp: Long = System.currentTimeMillis(),
    val logDate: String
)
