package com.example.expensetracker.data

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.expensetracker.R
import com.example.expensetracker.data.db.AppDatabase
import com.example.expensetracker.data.helper.NotificationHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SavingReminderWorker(
    context: Context, params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {

        val db = AppDatabase.getInstance(applicationContext)

        val today = SimpleDateFormat(
            "yyyy-MM-dd", Locale.getDefault()
        ).format(Date())

        val hasSavedToday = db.transactionDao().hasSavingsToday(today)

        if (!hasSavedToday) {
            showNotification()
        }

        return Result.success()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification() {

        val notification = NotificationCompat.Builder(
            applicationContext, NotificationHelper.CHANNEL_ID
        ).setSmallIcon(R.drawable.ic_other).setContentTitle("Future You ⚠")
            .setContentText("You skipped saving today!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT).build()

        NotificationManagerCompat.from(applicationContext).notify(1001, notification)
    }
}
