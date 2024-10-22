package com.skysphere.skysphere.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.skysphere.skysphere.API.WeatherType
import com.skysphere.skysphere.MainActivity
import com.skysphere.skysphere.R
import com.skysphere.skysphere.data.WeatherResults

object NotificationManager {
    // Including the channel id and notification id here
    private const val CHANNEL_ID = "weather_channel"
    private const val SEVERE_WEATHER_NOTIFICATION_ID = 1
    private const val RAIN_FORECAST_NOTIFICATION_ID = 2
    private const val DAILY_SUMMARY_NOTIFICATION_ID = 3

    // Function to show a severe weather notification
    fun showSevereWeatherNotification(context: Context, weatherCode: Int?) {
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
            .setContentText("Severe weather conditions: ${WeatherType.fromWMO(weatherCode).weatherDesc} are expected. Tap for more information.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(SEVERE_WEATHER_NOTIFICATION_ID, notification)
    }

    // Function to show the rain forecast notification

    fun showRainForecastNotification(context: Context, time: Pair<Int?, String>) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the notification channel for rain forecast notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Rain Forecast Notification",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Create the intent for the rain forecast notification
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Build and show the severe weather notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Current Rain Forecast")
            .setContentText("${WeatherType.fromWMO(time.first).weatherDesc} is expected at ${time.second}. Tap for more information.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(RAIN_FORECAST_NOTIFICATION_ID, notification)
    }

    fun showDailySummaryNotification(context: Context, weatherResults: WeatherResults?) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the notification channel for daily notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Daily Weather Summary",
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
            .setContentTitle("Daily Weather Summary")
            .setContentText("Expand to view.")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Current temperature is ${weatherResults?.current?.temperature}, " +
                        "feels like ${weatherResults?.current?.apparentTemperature}. " +
                        "High today is ${weatherResults?.daily?.temperatureMax?.get(0)}, " +
                        "Low today is ${weatherResults?.daily?.temperatureMax?.get(0)}. " +
                        "Conditions today will be mostly ${WeatherType.fromWMO(weatherResults?.daily?.weatherCode?.get(0)).weatherDesc}. " +
                        "Tap for more information "))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(DAILY_SUMMARY_NOTIFICATION_ID, notification)
    }

}