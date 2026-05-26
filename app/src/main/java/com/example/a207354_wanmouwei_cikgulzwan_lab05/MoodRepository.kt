package com.example.a207354_wanmouwei_cikgulzwan_lab05

import kotlinx.coroutines.flow.Flow

class MoodRepository(
    private val dao: MoodDao
) {

    val allMoods: Flow<List<MoodEntity>> =
        dao.getAllMoods()

    suspend fun insert(mood: MoodEntity) {
        dao.insertMood(mood)
    }

    suspend fun delete(moodId: Int) {
        dao.deleteMood(moodId)
    }

    suspend fun update(mood: MoodEntity) {
        dao.updateMood(mood)
    }
}