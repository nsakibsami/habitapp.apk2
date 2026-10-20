package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Achievement
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements WHERE userId = :userId ORDER BY unlockedAt DESC")
    fun getAchievements(userId: String): Flow<List<Achievement>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAchievement(achievement: Achievement): Long

    @Query("SELECT COUNT(*) FROM achievements WHERE userId = :userId")
    fun getUnlockedCount(userId: String): Flow<Int>

    @Query("DELETE FROM achievements WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)
}
