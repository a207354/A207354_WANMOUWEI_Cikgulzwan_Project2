package com.example.a207354_wanmouwei_cikgulzwan_lab05

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {

    @Insert
    suspend fun insertMood(mood: MoodEntity)

    @Update
    suspend fun updateMood(mood: MoodEntity)

    @Query("SELECT * FROM moods ORDER BY id DESC")
    fun getAllMoods(): Flow<List<MoodEntity>>

    @Query("DELETE FROM moods WHERE id = :moodId")
    suspend fun deleteMood(moodId: Int)
}