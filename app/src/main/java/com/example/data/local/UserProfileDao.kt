package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE id = :userId LIMIT 1")
    fun getUserProfile(userId: String): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles LIMIT 1")
    fun getCurrentProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles LIMIT 1")
    suspend fun getCurrentProfileSync(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: UserProfile)

    @Update
    suspend fun update(profile: UserProfile)

    @Query("UPDATE user_profiles SET isPro = :isPro, subscriptionPlan = :plan WHERE id = :userId")
    suspend fun updateSubscription(userId: String, isPro: Boolean, plan: String)

    @Query("DELETE FROM user_profiles WHERE id = :userId")
    suspend fun deleteProfile(userId: String)
}
