package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey
    val id: String = "user_default_id",
    val email: String = "alex@example.com",
    val displayName: String = "Alex",
    val isOnboarded: Boolean = false,
    val goals: List<String> = emptyList(),
    val wakeTime: String = "07:00",
    val workSchedule: String = "09:00 - 17:00",
    val reminderHours: String = "Morning (08:00)",
    val timezone: String = "UTC",
    val coachingTone: String = "Encouraging",
    val isPro: Boolean = false,
    val subscriptionPlan: String = "free",
    val quietHoursStart: String = "22:00",
    val quietHoursEnd: String = "07:00",
    val notificationsEnabled: Boolean = true
)

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey
    val id: String,
    val userId: String = "user_default_id",
    val name: String,
    val description: String = "",
    val iconName: String = "check",
    val colorHex: String = "#8BCF42",
    val category: String = "Productivity",
    val frequencyType: String = "DAILY", // "DAILY", "SPECIFIC_DAYS", "DAYS_PER_WEEK"
    val scheduledWeekdays: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7), // 1=Mon, ..., 7=Sun
    val weeklyTarget: Int = 7,
    val reminderEnabled: Boolean = false,
    val reminderTime: String? = "08:00",
    val targetValue: Float = 1f,
    val targetUnit: String = "times",
    val contributesToStreaks: Boolean = true,
    val startDate: String,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "habit_completions",
    indices = [
        Index(value = ["habitId", "scheduledDate"], unique = true),
        Index(value = ["userId"]),
        Index(value = ["scheduledDate"])
    ]
)
data class HabitCompletion(
    @PrimaryKey
    val id: String,
    val habitId: String,
    val userId: String = "user_default_id",
    val scheduledDate: String, // "YYYY-MM-DD"
    val completedAt: Long = System.currentTimeMillis(),
    val completionValue: Float = 1f,
    val notes: String? = null
)

@Entity(
    tableName = "achievements",
    indices = [
        Index(value = ["userId", "type"], unique = true)
    ]
)
data class Achievement(
    @PrimaryKey
    val id: String,
    val userId: String = "user_default_id",
    val type: String,
    val title: String,
    val description: String,
    val icon: String,
    val unlockedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "coach_messages",
    indices = [
        Index(value = ["conversationId"]),
        Index(value = ["timestamp"])
    ]
)
data class CoachMessage(
    @PrimaryKey
    val id: String,
    val conversationId: String = "default_conversation",
    val userId: String = "user_default_id",
    val role: String, // "user" or "model"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
