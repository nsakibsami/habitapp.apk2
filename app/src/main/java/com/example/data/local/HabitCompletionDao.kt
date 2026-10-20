package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.HabitCompletion
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitCompletionDao {
    @Query("SELECT * FROM habit_completions WHERE userId = :userId AND scheduledDate = :date")
    fun getCompletionsForDate(userId: String, date: String): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completions WHERE userId = :userId AND habitId = :habitId")
    fun getCompletionsForHabit(userId: String, habitId: String): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completions WHERE userId = :userId AND habitId = :habitId AND scheduledDate = :date LIMIT 1")
    suspend fun getCompletion(userId: String, habitId: String, date: String): HabitCompletion?

    @Query("SELECT * FROM habit_completions WHERE userId = :userId ORDER BY scheduledDate DESC")
    fun getAllCompletions(userId: String): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completions WHERE userId = :userId AND scheduledDate >= :startDate AND scheduledDate <= :endDate")
    fun getCompletionsInRange(userId: String, startDate: String, endDate: String): Flow<List<HabitCompletion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: HabitCompletion)

    @Query("DELETE FROM habit_completions WHERE userId = :userId AND habitId = :habitId AND scheduledDate = :date")
    suspend fun deleteCompletion(userId: String, habitId: String, date: String)

    @Query("SELECT COUNT(*) FROM habit_completions WHERE userId = :userId")
    fun getTotalCompletionsCount(userId: String): Flow<Int>

    @Query("DELETE FROM habit_completions WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)
}
