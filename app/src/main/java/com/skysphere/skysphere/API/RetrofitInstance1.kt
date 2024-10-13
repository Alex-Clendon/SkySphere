package com.skysphere.skysphere.API

import retrofit2.Retrofit
import kotlinx.serialization.json.Json
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType

object RetrofitInstance1 {
    private const val BASE_URL = "https://api.open-meteo.com/"

    val instance: WeatherAPI by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                Json.asConverterFactory(
                    "application/json; charset=UTF8".toMediaType()))
            .build()

        retrofit.create(WeatherAPI::class.java)
    }
}