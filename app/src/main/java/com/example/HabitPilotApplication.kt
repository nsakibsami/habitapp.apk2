package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.notification.NotificationHelper

class HabitPilotApplication : Application() {
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
    }
}
