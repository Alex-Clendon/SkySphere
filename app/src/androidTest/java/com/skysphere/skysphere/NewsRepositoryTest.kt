package com.skysphere.skysphere

import com.skysphere.skysphere.ui.news.NewsRepository
import org.junit.Test
import org.junit.Assert.*

class NewsRepositoryTest {

    @Test
    fun testGetLocalWeatherNews_returnsNonEmptyList() {
        val newsRepository = NewsRepository()
        val localNews = newsRepository.getLocalWeatherNews()
        assertFalse(localNews.isEmpty())
    }

    @Test
    fun testGetGlobalWeatherNews_returnsNonEmptyList() {
        val newsRepository = NewsRepository()
        val globalNews = newsRepository.getGlobalWeatherNews()
        assertFalse(globalNews.isEmpty())
    }



}
