package com.example.expensetracker.data.helper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi

object NotificationHelper {

    const val CHANNEL_ID = "saving_reminder"

    @RequiresApi(Build.VERSION_CODES.O)
    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Saving Reminder",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
