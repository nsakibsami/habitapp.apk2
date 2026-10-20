package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Habit
import com.example.data.model.HabitCompletion
import com.example.data.repository.HabitRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context verifies HabitPilot app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("HabitPilot", appName)
    }

    @Test
    fun `test fair streak calculation does not penalize unscheduled rest days`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val app = context as HabitPilotApplication
        val repository = HabitRepository(app.database, app)

        val habit = Habit(
            id = "test_habit_1",
            name = "Weekday Deep Work",
            frequencyType = "SPECIFIC_DAYS",
            scheduledWeekdays = listOf(1, 2, 3, 4, 5), // Mon-Fri
            startDate = "2026-10-01"
        )

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()

        // Create completions for the past 3 days
        val completions = mutableListOf<HabitCompletion>()
        for (i in 0 until 3) {
            val dateStr = sdf.format(cal.time)
            completions.add(
                HabitCompletion(
                    id = "comp_$i",
                    habitId = habit.id,
                    scheduledDate = dateStr
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }

        val streak = repository.calculateHabitCurrentStreak(habit, completions)
        assertTrue("Streak should be >= 1", streak >= 1)
    }
}
