package com.skysphere.skysphere.ui.news

import android.util.Log
import com.skysphere.skysphere.API.Article
import com.skysphere.skysphere.API.RetrofitInstance


class NewsRepository {
    // Instance of the API service to make network calls
    private val newsApiService = RetrofitInstance.newsApi

    // Fetches global weather news from the API
    suspend fun fetchGlobalWeatherNews(): List<Article> {
        return try {
            // Make API call to get news articles about global weather
            val response = newsApiService.getEverything(
                query = "Global Weather",
                sortBy = "publishedAt" // Sort articles by publication date
            )
            // Log number of articles fetched
            Log.d("NewsRepository", "Fetched ${response.articles.size} global weather articles")
            // Return the list of articles
            response.articles
        } catch (e: Exception) {
            // Log any errors that occur during the API call
            Log.e("NewsRepository", "Error fetching global weather news", e)
            // Return an empty list if there's an error
            emptyList()
        }
    }

    // Fetches local weather news from the API
    suspend fun fetchLocalWeatherNews(): List<Article> {
        return try {
            // Make API call to get news articles about New Zealand weather
            val response = newsApiService.getEverything(
                query = "new zealand weather",
                sortBy = "publishedAt" // Sort articles by publication date
            )
            // Log number of articles fetched
            Log.d("NewsRepository", "Fetched ${response.articles.size} local weather articles")
            // Return the list of articles
            response.articles
        } catch (e: Exception) {
            // Log any errors that occur during the API call
            Log.e("NewsRepository", "Error fetching local weather news", e)
            // Return an empty list if there's an error
            emptyList()
        }
    }
}