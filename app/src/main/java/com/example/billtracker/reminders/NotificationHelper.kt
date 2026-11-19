package com.example.billtracker.reminders

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.billtracker.MainActivity
import com.example.billtracker.R

object NotificationHelper {

    const val CHANNEL_ID = "bill_reminders"

    /** Android 8.0+ requires every notification to belong to a channel. Safe to call repeatedly. */
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Bill reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for bills that are due soon or overdue"
            }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    /** Android 13+ (API 33) needs the POST_NOTIFICATIONS runtime permission; older versions don't. */
    fun hasPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission") // the permission is checked by hasPermission() just below
    fun showBillReminder(context: Context, billId: Int, message: String) {
        if (!hasPermission(context)) return

        // Tapping the notification opens the app.
        val openApp = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            billId,
            openApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(ReminderMessages.TITLE)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Using the bill id as the notification id means tomorrow's reminder REPLACES today's
        // for the same bill instead of stacking duplicates.
        NotificationManagerCompat.from(context).notify(billId, notification)
    }
}
