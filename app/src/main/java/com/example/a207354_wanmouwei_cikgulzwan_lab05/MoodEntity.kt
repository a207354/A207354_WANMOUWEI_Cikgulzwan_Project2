package com.example.a207354_wanmouwei_cikgulzwan_lab05

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "moods")
data class MoodEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val mood: String,
    val note: String
)