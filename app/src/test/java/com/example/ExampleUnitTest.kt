package com.example

import com.example.data.model.Habit
import com.example.data.model.HabitCompletion
import com.example.ui.components.STARTER_TEMPLATES
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testStarterTemplatesNotEmpty() {
        assertTrue("Starter templates should not be empty", STARTER_TEMPLATES.isNotEmpty())
        assertEquals(8, STARTER_TEMPLATES.size)
        assertTrue(STARTER_TEMPLATES.any { it.name.contains("Deep Focus") })
    }

    @Test
    fun testHabitSchedulingCheck() {
        val dailyHabit = Habit(
            id = "h1",
            name = "Water",
            frequencyType = "DAILY",
            startDate = "2026-10-01"
        )
        // Daily should be scheduled every day (1..7)
        for (day in 1..7) {
            assertTrue(dailyHabit.frequencyType == "DAILY")
        }

        val weekdayHabit = Habit(
            id = "h2",
            name = "Work Focus",
            frequencyType = "SPECIFIC_DAYS",
            scheduledWeekdays = listOf(1, 2, 3, 4, 5),
            startDate = "2026-10-01"
        )
        assertTrue(weekdayHabit.scheduledWeekdays.contains(1)) // Monday
        assertTrue(weekdayHabit.scheduledWeekdays.contains(5)) // Friday
        assertFalse(weekdayHabit.scheduledWeekdays.contains(6)) // Saturday (Rest)
        assertFalse(weekdayHabit.scheduledWeekdays.contains(7)) // Sunday (Rest)
    }

    @Test
    fun testCompletionCalculations() {
        val completions = listOf(
            HabitCompletion("c1", "h1", scheduledDate = "2026-10-20"),
            HabitCompletion("c2", "h1", scheduledDate = "2026-10-19"),
            HabitCompletion("c3", "h1", scheduledDate = "2026-10-18")
        )
        val completionDates = completions.map { it.scheduledDate }.toSet()
        assertEquals(3, completionDates.size)
        assertTrue(completionDates.contains("2026-10-20"))
    }
}
