package com.skysphere.skysphere.API

import com.skysphere.skysphere.services.weather.json.ApiResults
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherAPI {
    @GET("v1/forecast")
    fun getWeatherData(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String,
        @Query("hourly") hourly: String,
        @Query("daily") daily: String,
        @Query("timezone") timezone: String,
        @Query("forecast_days") forecastDays: Int
    ): Call<ApiResults>

    @GET("v1/forecast")
    fun getWeatherDataWidget(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String,
        @Query("timezone") timezone: String
    ): Call<WeatherData>
}