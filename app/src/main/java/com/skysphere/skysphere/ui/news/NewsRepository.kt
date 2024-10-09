package com.skysphere.skysphere.ui.news

class NewsRepository {

    fun getLocalWeatherNews(): List<NewsArticle> {
        return listOf(NewsArticle("Sample Local News", "Sample Content", "Local"))
    }

    fun getGlobalWeatherNews(): List<NewsArticle> {
        return listOf(NewsArticle("Sample Global News", "Sample Content", "Global"))
    }
}
