package com.skysphere.skysphere.API

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherAPI {
    @GET("v1/forecast")
    fun getWeatherData(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String,
        @Query("daily") daily: String,
        @Query("timezone") timezone: String,
        @Query("hourly") hourly: String

    ): Call<WeatherData>
}