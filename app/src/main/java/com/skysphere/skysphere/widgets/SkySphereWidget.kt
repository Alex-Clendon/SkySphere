package com.skysphere.skysphere.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import com.skysphere.skysphere.API.RetrofitInstance
import com.skysphere.skysphere.API.WeatherData
import com.skysphere.skysphere.API.WeatherType
import com.skysphere.skysphere.MainActivity
import com.skysphere.skysphere.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Implementation of App Widget functionality.
 */

class SkySphereWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    // Retrieving the stored location of the user
    val sharedPrefs = context.getSharedPreferences("custom_location_prefs", Context.MODE_PRIVATE)
    val latitude = sharedPrefs.getFloat("latitude", 0f).toDouble()
    val longitude = sharedPrefs.getFloat("longitude", 0f).toDouble()

    val weatherService = RetrofitInstance.getInstance(false)

    //Calls the API and assigns the views declared above as the data retrieved from the API. Takes in the latitude and longitude of the user.
    weatherService.getWeatherDataWidget(latitude, longitude, "weather_code,temperature_2m", "auto") // Calls the getWeatherData function and parses the user location variables, and other variables needed from the API.
        .enqueue(object : Callback<WeatherData> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<WeatherData>, response: Response<WeatherData>) {
                // Checks to see if we got a response from the API
                if (response.isSuccessful) {
                    val temperature = response.body()?.current?.temperature_2m?.toString() ?: "N/A"
                    val weatherState =
                        response.body()?.current?.weather_code?.let { WeatherType.fromWMO(it).weatherDesc }
                            ?: "N/A"
                    val weatherIconRes =
                        WeatherType.fromWMO(response.body()?.current?.weather_code ?: 0).iconRes

                    // Construct the RemoteViews object
                    val views = RemoteViews(context.packageName, R.layout.sky_sphere_widget)

                    // Opens the app when Widget is clicked on
                    val pendingIntent: PendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        Intent(context, MainActivity::class.java),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    views.setOnClickPendingIntent(R.id.main_layout, pendingIntent)

                    // Update the widget view
                    views.setTextViewText(R.id.temperatureTextView, temperature)
                    views.setTextViewText(R.id.weatherStateTextView, weatherState)
                    views.setImageViewResource(R.id.weatherIconImageView, weatherIconRes)

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
            override fun onFailure(call: Call<WeatherData>, t: Throwable)
            {
                // Will finish it off later in sprint 2. Still under development
            }
        })
}
