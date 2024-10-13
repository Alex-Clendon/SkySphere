package com.skysphere.skysphere.API

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val WEATHER_BASE_URL = "https://api.open-meteo.com/"
    private const val NEWS_BASE_URL = "https://newsapi.org/"
    const val NEWS_API_KEY = "859c9a4fd0c341b0b1d0c4036c496704"

    val newsApi: NewsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(NEWS_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsApiService::class.java)
    }

    val weatherAPI: WeatherAPI by lazy {
        Retrofit.Builder()
            .baseUrl(WEATHER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherAPI::class.java)
    }
}