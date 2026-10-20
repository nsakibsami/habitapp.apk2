package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CoachMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface CoachMessageDao {
    @Query("SELECT * FROM coach_messages WHERE userId = :userId ORDER BY timestamp ASC")
    fun getMessages(userId: String): Flow<List<CoachMessage>>

    @Query("SELECT * FROM coach_messages WHERE conversationId = :convId ORDER BY timestamp ASC")
    fun getMessagesForConversation(convId: String): Flow<List<CoachMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: CoachMessage)

    @Query("DELETE FROM coach_messages WHERE userId = :userId")
    suspend fun clearMessagesForUser(userId: String)
}
