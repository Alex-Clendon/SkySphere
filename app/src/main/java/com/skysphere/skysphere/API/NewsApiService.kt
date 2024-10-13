package com.skysphere.skysphere.API

import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("v2/everything")
    suspend fun getEverything(
        @Query("q") query: String,
        @Query("sortBy") sortBy: String,
        @Query("apiKey") apiKey: String = RetrofitInstance.NEWS_API_KEY
    ): NewsResponse
}
