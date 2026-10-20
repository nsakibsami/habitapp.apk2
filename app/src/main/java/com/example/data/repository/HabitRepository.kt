package com.example.data.repository

import android.content.Context
import com.example.ai.GeminiCoachClient
import com.example.data.local.AppDatabase
import com.example.data.model.Achievement
import com.example.data.model.CoachMessage
import com.example.data.model.Habit
import com.example.data.model.HabitCompletion
import com.example.data.model.UserProfile
import com.example.notification.NotificationHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class HabitRepository(
    private val database: AppDatabase,
    private val context: Context
) {
    private val userDao = database.userProfileDao()
    private val habitDao = database.habitDao()
    private val completionDao = database.habitCompletionDao()
    private val achievementDao = database.achievementDao()
    private val coachDao = database.coachMessageDao()

    val currentProfile: Flow<UserProfile?> = userDao.getCurrentProfile()

    fun getActiveHabits(userId: String = "user_default_id"): Flow<List<Habit>> =
        habitDao.getActiveHabits(userId)

    fun getArchivedHabits(userId: String = "user_default_id"): Flow<List<Habit>> =
        habitDao.getArchivedHabits(userId)

    fun getCompletionsForDate(userId: String = "user_default_id", date: String): Flow<List<HabitCompletion>> =
        completionDao.getCompletionsForDate(userId, date)

    fun getAllCompletions(userId: String = "user_default_id"): Flow<List<HabitCompletion>> =
        completionDao.getAllCompletions(userId)

    fun getCompletionsForHabit(userId: String = "user_default_id", habitId: String): Flow<List<HabitCompletion>> =
        completionDao.getCompletionsForHabit(userId, habitId)

    fun getAchievements(userId: String = "user_default_id"): Flow<List<Achievement>> =
        achievementDao.getAchievements(userId)

    fun getCoachMessages(userId: String = "user_default_id"): Flow<List<CoachMessage>> =
        coachDao.getMessages(userId)

    suspend fun getHabitById(habitId: String): Habit? =
        habitDao.getHabitById(habitId)

    suspend fun saveProfile(profile: UserProfile) {
        userDao.insertOrUpdate(profile)
    }

    suspend fun updateSubscription(isPro: Boolean, plan: String, userId: String = "user_default_id") {
        userDao.updateSubscription(userId, isPro, plan)
    }

    suspend fun createHabit(habit: Habit) {
        habitDao.insertHabit(habit)
        if (habit.reminderEnabled) {
            NotificationHelper.scheduleHabitReminder(context, habit)
        }
    }

    suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit)
        if (habit.reminderEnabled) {
            NotificationHelper.scheduleHabitReminder(context, habit)
        } else {
            NotificationHelper.cancelHabitReminder(context, habit.id)
        }
    }

    suspend fun setHabitArchived(habitId: String, isArchived: Boolean) {
        habitDao.setArchived(habitId, isArchived)
        if (isArchived) {
            NotificationHelper.cancelHabitReminder(context, habitId)
        }
    }

    suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteHabit(habit)
        NotificationHelper.cancelHabitReminder(context, habit.id)
    }

    suspend fun toggleHabitCompletion(
        habitId: String,
        dateString: String,
        userId: String = "user_default_id"
    ): Boolean {
        val existing = completionDao.getCompletion(userId, habitId, dateString)
        val habit = habitDao.getHabitById(habitId)

        return if (existing != null) {
            completionDao.deleteCompletion(userId, habitId, dateString)
            false
        } else {
            val newCompletion = HabitCompletion(
                id = UUID.randomUUID().toString(),
                habitId = habitId,
                userId = userId,
                scheduledDate = dateString,
                completedAt = System.currentTimeMillis(),
                completionValue = habit?.targetValue ?: 1f
            )
            completionDao.insertCompletion(newCompletion)
            checkAndAwardAchievements(userId, habitId)
            true
        }
    }

    private suspend fun checkAndAwardAchievements(userId: String, habitId: String) {
        val allCompletions = completionDao.getAllCompletions(userId).firstOrNull() ?: emptyList()
        val totalCount = allCompletions.size

        // Milestone 1: First completion
        if (totalCount >= 1) {
            achievementDao.insertAchievement(
                Achievement(
                    id = "ach_first_completion",
                    userId = userId,
                    type = "FIRST_COMPLETION",
                    title = "First Step Taken",
                    description = "Completed your first habit. The journey begins with one step.",
                    icon = "flag"
                )
            )
        }

        // Milestone: 100 completions
        if (totalCount >= 100) {
            achievementDao.insertAchievement(
                Achievement(
                    id = "ach_100_completions",
                    userId = userId,
                    type = "COMPLETIONS_100",
                    title = "Century Milestone",
                    description = "Completed 100 habits across your journey. Incredible commitment!",
                    icon = "trophy"
                )
            )
        }

        // Check streaks for this habit
        val habit = habitDao.getHabitById(habitId)
        if (habit != null && habit.contributesToStreaks) {
            val streak = calculateHabitCurrentStreak(habit, allCompletions.filter { it.habitId == habit.id })
            if (streak >= 3) {
                achievementDao.insertAchievement(
                    Achievement(
                        id = "ach_streak_3",
                        userId = userId,
                        type = "STREAK_3",
                        title = "Momentum Ignited",
                        description = "Achieved a 3-day streak on ${habit.name}!",
                        icon = "flame"
                    )
                )
            }
            if (streak >= 7) {
                achievementDao.insertAchievement(
                    Achievement(
                        id = "ach_streak_7",
                        userId = userId,
                        type = "STREAK_7",
                        title = "One Week Strong",
                        description = "7 consecutive scheduled days of ${habit.name}!",
                        icon = "star"
                    )
                )
            }
            if (streak >= 14) {
                achievementDao.insertAchievement(
                    Achievement(
                        id = "ach_streak_14",
                        userId = userId,
                        type = "STREAK_14",
                        title = "Unbreakable Habit",
                        description = "14 scheduled sessions completed without burnout.",
                        icon = "shield"
                    )
                )
            }
            if (streak >= 30) {
                achievementDao.insertAchievement(
                    Achievement(
                        id = "ach_streak_30",
                        userId = userId,
                        type = "STREAK_30",
                        title = "30-Day Master",
                        description = "A full month of dedicated consistency!",
                        icon = "crown"
                    )
                )
            }
        }
    }

    fun calculateHabitCurrentStreak(habit: Habit, completions: List<HabitCompletion>): Int {
        if (completions.isEmpty()) return 0
        val completionDates = completions.map { it.scheduledDate }.toSet()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val calendar = Calendar.getInstance()
        var streak = 0
        var daysChecked = 0

        // If today is scheduled and completed, start with today.
        // If today is scheduled and not yet completed, start checking from yesterday.
        // If today is not scheduled, start from yesterday.
        val todayStr = sdf.format(calendar.time)
        val todayDow = calendar.get(Calendar.DAY_OF_WEEK).let { if (it == Calendar.SUNDAY) 7 else it - 1 }
        val isTodayScheduled = isHabitScheduledOnDay(habit, todayDow)

        if (isTodayScheduled && completionDates.contains(todayStr)) {
            streak++
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        } else if (!isTodayScheduled) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        } else {
            // Scheduled today but not yet completed: don't break streak yet; count back from yesterday
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        while (daysChecked < 365) {
            daysChecked++
            val dateStr = sdf.format(calendar.time)
            val dow = calendar.get(Calendar.DAY_OF_WEEK).let { if (it == Calendar.SUNDAY) 7 else it - 1 }

            if (isHabitScheduledOnDay(habit, dow)) {
                if (completionDates.contains(dateStr)) {
                    streak++
                } else {
                    // Missed a scheduled day
                    break
                }
            }
            // Non-scheduled day (rest day) does NOT break the streak!
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        return streak
    }

    fun calculateHabitBestStreak(habit: Habit, completions: List<HabitCompletion>): Int {
        if (completions.isEmpty()) return 0
        val completionDates = completions.map { it.scheduledDate }.toSet()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Check over the past 90 days
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -90)

        var bestStreak = 0
        var currentRunningStreak = 0

        for (i in 0..90) {
            val dateStr = sdf.format(calendar.time)
            val dow = calendar.get(Calendar.DAY_OF_WEEK).let { if (it == Calendar.SUNDAY) 7 else it - 1 }

            if (isHabitScheduledOnDay(habit, dow)) {
                if (completionDates.contains(dateStr)) {
                    currentRunningStreak++
                    if (currentRunningStreak > bestStreak) {
                        bestStreak = currentRunningStreak
                    }
                } else {
                    currentRunningStreak = 0
                }
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return bestStreak
    }

    fun isHabitScheduledOnDay(habit: Habit, dayOfWeek1to7: Int): Boolean {
        return when (habit.frequencyType) {
            "DAILY" -> true
            "SPECIFIC_DAYS" -> habit.scheduledWeekdays.contains(dayOfWeek1to7)
            "DAYS_PER_WEEK" -> true
            else -> true
        }
    }

    suspend fun sendCoachMessage(
        userMessage: String,
        userId: String = "user_default_id"
    ): CoachMessage {
        val userMsg = CoachMessage(
            id = UUID.randomUUID().toString(),
            userId = userId,
            role = "user",
            content = userMessage,
            timestamp = System.currentTimeMillis()
        )
        coachDao.insertMessage(userMsg)

        val habits = habitDao.getActiveHabits(userId).firstOrNull() ?: emptyList()
        val profile = userDao.getCurrentProfileSync() ?: UserProfile()
        val allCompletions = completionDao.getAllCompletions(userId).firstOrNull() ?: emptyList()

        // calculate 7-day rate
        val completionRate = calculateWeeklyCompletionRate(habits, allCompletions)

        val previousMessages = coachDao.getMessages(userId).firstOrNull() ?: emptyList()
        val history = previousMessages.map { it.role to it.content }

        val replyText = GeminiCoachClient.getCoachResponse(
            userMessage = userMessage,
            activeHabits = habits,
            completionRate = completionRate,
            coachingTone = profile.coachingTone,
            conversationHistory = history
        )

        val modelMsg = CoachMessage(
            id = UUID.randomUUID().toString(),
            userId = userId,
            role = "model",
            content = replyText,
            timestamp = System.currentTimeMillis()
        )
        coachDao.insertMessage(modelMsg)
        return modelMsg
    }

    suspend fun clearCoachChat(userId: String = "user_default_id") {
        coachDao.clearMessagesForUser(userId)
    }

    suspend fun clearAllData(userId: String = "user_default_id") {
        habitDao.deleteAllForUser(userId)
        completionDao.deleteAllForUser(userId)
        achievementDao.deleteAllForUser(userId)
        coachDao.clearMessagesForUser(userId)
        userDao.deleteProfile(userId)
    }

    fun calculateWeeklyCompletionRate(habits: List<Habit>, completions: List<HabitCompletion>): Int {
        if (habits.isEmpty()) return 0
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        var scheduledCount = 0
        var completedCount = 0

        val completionSet = completions.map { "${it.habitId}_${it.scheduledDate}" }.toSet()

        for (i in 0 until 7) {
            val dateStr = sdf.format(calendar.time)
            val dow = calendar.get(Calendar.DAY_OF_WEEK).let { if (it == Calendar.SUNDAY) 7 else it - 1 }

            habits.forEach { habit ->
                if (isHabitScheduledOnDay(habit, dow)) {
                    scheduledCount++
                    if (completionSet.contains("${habit.id}_$dateStr")) {
                        completedCount++
                    }
                }
            }
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        return if (scheduledCount == 0) 0 else ((completedCount.toFloat() / scheduledCount) * 100).toInt()
    }

    suspend fun seedStarterHabits(goals: List<String>, userId: String = "user_default_id") {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val starterList = mutableListOf<Habit>()

        starterList.add(
            Habit(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = "Morning Stretch & Hydrate",
                description = "Drink a large glass of water and stretch for 5 minutes.",
                iconName = "water",
                colorHex = "#8BCF42",
                category = "Physical wellness",
                frequencyType = "DAILY",
                reminderEnabled = true,
                reminderTime = "07:30",
                targetValue = 1f,
                targetUnit = "session",
                startDate = today
            )
        )

        starterList.add(
            Habit(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = "Read 10 Pages",
                description = "Read non-fiction or inspiring fiction before screen time.",
                iconName = "book",
                colorHex = "#3898EC",
                category = "Learning",
                frequencyType = "DAILY",
                reminderEnabled = true,
                reminderTime = "20:30",
                targetValue = 10f,
                targetUnit = "pages",
                startDate = today
            )
        )

        starterList.add(
            Habit(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = "25-Minute Deep Focus",
                description = "One Pomodoro block of undisturbed deep work.",
                iconName = "timer",
                colorHex = "#27452D",
                category = "Productivity",
                frequencyType = "SPECIFIC_DAYS",
                scheduledWeekdays = listOf(1, 2, 3, 4, 5),
                reminderEnabled = true,
                reminderTime = "10:00",
                targetValue = 25f,
                targetUnit = "min",
                startDate = today
            )
        )

        starterList.add(
            Habit(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = "Evening Wind-Down",
                description = "Put phone on charger 30 minutes before sleep.",
                iconName = "bed",
                colorHex = "#8860D0",
                category = "Mental wellness",
                frequencyType = "DAILY",
                reminderEnabled = true,
                reminderTime = "22:00",
                targetValue = 1f,
                targetUnit = "routine",
                startDate = today
            )
        )

        habitDao.insertHabits(starterList)
        starterList.forEach { NotificationHelper.scheduleHabitReminder(context, it) }
    }
}
