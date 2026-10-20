package com.example.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.notification.NotificationHelper
import java.util.Calendar

class HabitReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getStringExtra(NotificationHelper.EXTRA_HABIT_ID) ?: return
        val habitName = intent.getStringExtra(NotificationHelper.EXTRA_HABIT_NAME) ?: "Habit"
        val category = intent.getStringExtra("extra_category") ?: "Wellness"

        if (intent.action == NotificationHelper.ACTION_SNOOZE) {
            // Snooze 15 minutes
            val snoozeCalendar = Calendar.getInstance().apply {
                add(Calendar.MINUTE, 15)
            }
            val snoozeIntent = Intent(context, HabitReminderReceiver::class.java).apply {
                putExtra(NotificationHelper.EXTRA_HABIT_ID, habitId)
                putExtra(NotificationHelper.EXTRA_HABIT_NAME, habitName)
                putExtra("extra_category", category)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                (habitId + "_resnooze").hashCode(),
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            try {
                alarmManager?.set(
                    AlarmManager.RTC_WAKEUP,
                    snoozeCalendar.timeInMillis,
                    pendingIntent
                )
            } catch (_: Exception) {
            }
            return
        }

        NotificationHelper.showNotification(
            context = context,
            habitId = habitId,
            habitName = habitName,
            category = category
        )
    }
}
