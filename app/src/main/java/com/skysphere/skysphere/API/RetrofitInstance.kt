package com.skysphere.skysphere.API

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.serialization.json.Json
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType

object RetrofitInstance {
    private const val BASE_URL = "https://api.open-meteo.com/"


    fun getInstance(useKotlinxSerialization: Boolean): WeatherAPI {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .apply {
                if (useKotlinxSerialization) { // Choose between Gson and Kotlinx Serialization at runtime
                    addConverterFactory(
                        Json { ignoreUnknownKeys = true } // Ignore unknown keys
                            .asConverterFactory("application/json".toMediaType())
                    )
                } else {
                    addConverterFactory(GsonConverterFactory.create())
                }
            }
            .build()

        return retrofit.create(WeatherAPI::class.java)
    }

    val newsApi: NewsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(NEWS_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsApiService::class.java)
    }
}

