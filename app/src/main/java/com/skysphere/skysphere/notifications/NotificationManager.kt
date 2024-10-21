package com.skysphere.skysphere.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.skysphere.skysphere.MainActivity
import com.skysphere.skysphere.R

object NotificationManager {
    // Including the channel id and notification id here
    private const val CHANNEL_ID = "weather_channel"
    private const val SEVERE_WEATHER_NOTIFICATION_ID = 1
    private const val RAIN_FORECAST_NOTIFICATION_ID = 2
    private const val DAILY_SUMMARY_NOTIFICATION_ID = 3

    // Function to show a severe weather notification
    fun showSevereWeatherNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the notification channel for severe weather alerts
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Severe Weather Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Create the intent for the severe weather notification
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Build and show the severe weather notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Severe Weather Alert")
            .setContentText("Severe weather conditions expected. Tap for more information.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(SEVERE_WEATHER_NOTIFICATION_ID, notification)
    }
}