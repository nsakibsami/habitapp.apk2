package com.example.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.Habit
import com.example.receiver.HabitReminderReceiver
import java.util.Calendar

object NotificationHelper {
    const val CHANNEL_ID = "habitpilot_channel"
    const val CHANNEL_NAME = "Habit Reminders"
    const val ACTION_COMPLETE = "com.example.habitpilot.ACTION_COMPLETE"
    const val ACTION_SNOOZE = "com.example.habitpilot.ACTION_SNOOZE"
    const val EXTRA_HABIT_ID = "extra_habit_id"
    const val EXTRA_HABIT_NAME = "extra_habit_name"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily reminders and gentle motivation for your habits"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showNotification(
        context: Context,
        habitId: String,
        habitName: String,
        category: String = "Habit"
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_HABIT_ID, habitId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            habitId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Snooze 15 min
        val snoozeIntent = Intent(context, HabitReminderReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_HABIT_ID, habitId)
            putExtra(EXTRA_HABIT_NAME, habitName)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (habitId + "_snooze").hashCode(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("A small step counts: $habitName")
            .setContentText("Take a few moments for your $category habit. Keep it simple.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_popup_reminder, "Snooze 15m", snoozePendingIntent)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(habitId.hashCode(), builder.build())
    }

    fun scheduleHabitReminder(context: Context, habit: Habit) {
        if (!habit.reminderEnabled || habit.reminderTime == null) {
            cancelHabitReminder(context, habit.id)
            return
        }

        val parts = habit.reminderTime.split(":")
        if (parts.size != 2) return
        val hour = parts[0].toIntOrNull() ?: 8
        val minute = parts[1].toIntOrNull() ?: 0

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, HabitReminderReceiver::class.java).apply {
            putExtra(EXTRA_HABIT_ID, habit.id)
            putExtra(EXTRA_HABIT_NAME, habit.name)
            putExtra("extra_category", habit.category)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habit.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        try {
            alarmManager?.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (_: Exception) {
            // Gracefully handle alarm restrictions if exact alarm permission isn't available
        }
    }

    fun cancelHabitReminder(context: Context, habitId: String) {
        val intent = Intent(context, HabitReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habitId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
