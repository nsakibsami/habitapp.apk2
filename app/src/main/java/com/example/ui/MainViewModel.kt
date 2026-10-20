package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.HabitPilotApplication
import com.example.data.model.Achievement
import com.example.data.model.CoachMessage
import com.example.data.model.Habit
import com.example.data.model.HabitCompletion
import com.example.data.model.UserProfile
import com.example.data.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

data class DailyChartPoint(
    val dayName: String, // e.g. "Mon", "Tue"
    val dateString: String,
    val completedCount: Int,
    val totalScheduled: Int
)

data class CategoryStat(
    val category: String,
    val count: Int,
    val percentage: Int,
    val colorHex: String
)

data class HeatmapDay(
    val dateString: String,
    val dayOfMonth: Int,
    val level: Int // 0: none, 1: 1-2, 2: 3-4, 3: 5+
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as HabitPilotApplication
    val repository = HabitRepository(app.database, app.applicationContext)

    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val todayDateString: String = sdf.format(Date())

    private val _selectedDate = MutableStateFlow(todayDateString)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _isCoachThinking = MutableStateFlow(false)
    val isCoachThinking: StateFlow<Boolean> = _isCoachThinking.asStateFlow()

    val userProfile: StateFlow<UserProfile?> = repository.currentProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeHabits: StateFlow<List<Habit>> = repository.getActiveHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedHabits: StateFlow<List<Habit>> = repository.getArchivedHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCompletions: StateFlow<List<HabitCompletion>> = repository.getAllCompletions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievements: StateFlow<List<Achievement>> = repository.getAchievements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val coachMessages: StateFlow<List<CoachMessage>> = repository.getCoachMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Ensure default profile if none exists
        viewModelScope.launch {
            repository.currentProfile.collect { profile ->
                if (profile == null) {
                    val defaultProfile = UserProfile(
                        id = "user_default_id",
                        displayName = "Alex",
                        email = "alex@habitpilot.app",
                        isOnboarded = false
                    )
                    repository.saveProfile(defaultProfile)
                }
            }
        }
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun toggleHabitCompletion(habitId: String, dateString: String = _selectedDate.value) {
        viewModelScope.launch {
            repository.toggleHabitCompletion(habitId, dateString)
        }
    }

    fun createHabit(
        name: String,
        description: String,
        category: String,
        iconName: String,
        colorHex: String,
        frequencyType: String,
        weekdays: List<Int>,
        reminderEnabled: Boolean,
        reminderTime: String?,
        targetValue: Float,
        targetUnit: String,
        contributesToStreaks: Boolean
    ) {
        viewModelScope.launch {
            val habit = Habit(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                description = description.trim(),
                category = category,
                iconName = iconName,
                colorHex = colorHex,
                frequencyType = frequencyType,
                scheduledWeekdays = weekdays,
                reminderEnabled = reminderEnabled,
                reminderTime = reminderTime,
                targetValue = targetValue,
                targetUnit = targetUnit,
                contributesToStreaks = contributesToStreaks,
                startDate = todayDateString
            )
            repository.createHabit(habit)
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            repository.updateHabit(habit)
        }
    }

    fun archiveHabit(habitId: String) {
        viewModelScope.launch {
            repository.setHabitArchived(habitId, true)
        }
    }

    fun restoreHabit(habitId: String) {
        viewModelScope.launch {
            repository.setHabitArchived(habitId, false)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    fun completeOnboarding(
        displayName: String,
        email: String,
        goals: List<String>,
        wakeTime: String,
        workSchedule: String,
        reminderHours: String,
        coachingTone: String,
        seedStarterHabits: Boolean
    ) {
        viewModelScope.launch {
            val updated = UserProfile(
                id = "user_default_id",
                email = email.ifBlank { "alex@habitpilot.app" },
                displayName = displayName.ifBlank { "Pilot" },
                isOnboarded = true,
                goals = goals,
                wakeTime = wakeTime,
                workSchedule = workSchedule,
                reminderHours = reminderHours,
                coachingTone = coachingTone,
                isPro = false,
                subscriptionPlan = "free"
            )
            repository.saveProfile(updated)
            if (seedStarterHabits) {
                repository.seedStarterHabits(goals)
            }
        }
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveProfile(profile)
        }
    }

    fun upgradeToPro(plan: String = "pro_monthly") {
        viewModelScope.launch {
            repository.updateSubscription(isPro = true, plan = plan)
        }
    }

    fun cancelProSubscription() {
        viewModelScope.launch {
            repository.updateSubscription(isPro = false, plan = "free")
        }
    }

    fun sendCoachMessage(userText: String) {
        if (userText.isBlank()) return
        viewModelScope.launch {
            _isCoachThinking.value = true
            try {
                repository.sendCoachMessage(userText.trim())
            } finally {
                _isCoachThinking.value = false
            }
        }
    }

    fun clearCoachChat() {
        viewModelScope.launch {
            repository.clearCoachChat()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            val resetProfile = UserProfile(
                id = "user_default_id",
                displayName = "Pilot",
                email = "pilot@habitpilot.app",
                isOnboarded = false
            )
            repository.saveProfile(resetProfile)
        }
    }

    // Analytics helpers
    fun getWeeklyChartData(habits: List<Habit>, completions: List<HabitCompletion>): List<DailyChartPoint> {
        val list = mutableListOf<DailyChartPoint>()
        val cal = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

        val completionSet = completions.map { "${it.habitId}_${it.scheduledDate}" }.toSet()

        // Generate past 7 days in chronological order
        val tempDays = mutableListOf<Calendar>()
        for (i in 0 until 7) {
            val dayCal = cal.clone() as Calendar
            dayCal.add(Calendar.DAY_OF_YEAR, -i)
            tempDays.add(dayCal)
        }
        tempDays.reverse()

        for (dayCal in tempDays) {
            val dateStr = sdf.format(dayCal.time)
            val dayName = dayFormat.format(dayCal.time)
            val dow = dayCal.get(Calendar.DAY_OF_WEEK).let { if (it == Calendar.SUNDAY) 7 else it - 1 }

            var scheduled = 0
            var completed = 0

            habits.forEach { habit ->
                if (repository.isHabitScheduledOnDay(habit, dow)) {
                    scheduled++
                    if (completionSet.contains("${habit.id}_$dateStr")) {
                        completed++
                    }
                }
            }

            list.add(DailyChartPoint(dayName, dateStr, completed, if (scheduled == 0) 1 else scheduled))
        }

        return list
    }

    fun getCategoryBreakdown(habits: List<Habit>, completions: List<HabitCompletion>): List<CategoryStat> {
        if (completions.isEmpty()) return emptyList()
        val habitMap = habits.associateBy { it.id }
        val categoryCounts = mutableMapOf<String, Int>()

        completions.forEach { comp ->
            val cat = habitMap[comp.habitId]?.category ?: "Other"
            categoryCounts[cat] = (categoryCounts[cat] ?: 0) + 1
        }

        val total = completions.size
        val colors = mapOf(
            "Productivity" to "#27452D",
            "Physical wellness" to "#8BCF42",
            "Mental wellness" to "#8860D0",
            "Learning" to "#3898EC",
            "Lifestyle" to "#E7AA43",
            "Custom" to "#20B2AA"
        )

        return categoryCounts.map { (cat, count) ->
            val pct = ((count.toFloat() / total) * 100).toInt()
            CategoryStat(cat, count, pct, colors[cat] ?: "#8BCF42")
        }.sortedByDescending { it.count }
    }

    fun get30DayHeatmap(completions: List<HabitCompletion>): List<HeatmapDay> {
        val list = mutableListOf<HeatmapDay>()
        val cal = Calendar.getInstance()
        val countByDate = completions.groupingBy { it.scheduledDate }.eachCount()

        val days = mutableListOf<Calendar>()
        for (i in 0 until 28) {
            val dayCal = cal.clone() as Calendar
            dayCal.add(Calendar.DAY_OF_YEAR, -i)
            days.add(dayCal)
        }
        days.reverse()

        days.forEach { dayCal ->
            val dateStr = sdf.format(dayCal.time)
            val dayOfMonth = dayCal.get(Calendar.DAY_OF_MONTH)
            val count = countByDate[dateStr] ?: 0
            val level = when {
                count == 0 -> 0
                count in 1..2 -> 1
                count in 3..4 -> 2
                else -> 3
            }
            list.add(HeatmapDay(dateStr, dayOfMonth, level))
        }
        return list
    }
}
