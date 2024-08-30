package com.skysphere.skysphere

import okhttp3.Response
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherAPI {
    @GET("v1/forecast?hourly=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&timezone=Pacific%2FAuckland")
    fun getWeatherData(
        @Query("latitude") latitude : Double,
        @Query("longitude") longitude : Double
    ): Call<WeatherResponse>
}