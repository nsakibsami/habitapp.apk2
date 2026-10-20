package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Habit
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE userId = :userId AND isArchived = 0 ORDER BY createdAt DESC")
    fun getActiveHabits(userId: String): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE userId = :userId AND isArchived = 1 ORDER BY createdAt DESC")
    fun getArchivedHabits(userId: String): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllHabits(userId: String): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :habitId LIMIT 1")
    suspend fun getHabitById(habitId: String): Habit?

    @Query("SELECT COUNT(*) FROM habits WHERE userId = :userId AND isArchived = 0")
    fun getActiveHabitsCount(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<Habit>)

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("UPDATE habits SET isArchived = :isArchived WHERE id = :habitId")
    suspend fun setArchived(habitId: String, isArchived: Boolean)

    @Query("DELETE FROM habits WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)
}
