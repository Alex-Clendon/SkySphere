package com.skysphere.skysphere

import com.skysphere.skysphere.ui.news.NewsRepository
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*

class NewsRepositoryTest {

    private val newsRepository = NewsRepository()

    @Test
    fun testFetchGlobalWeatherNews_returnsNonEmptyList() = runBlocking {
        val news = newsRepository.fetchGlobalWeatherNews()
        assertFalse("Global weather news should not be empty", news.isEmpty())
    }

    @Test
    fun testFetchLocalWeatherNews_returnsNonEmptyList() = runBlocking {
        val news = newsRepository.fetchLocalWeatherNews()
        assertFalse("Local weather news should not be empty", news.isEmpty())
    }

    @Test
    fun testFetchNewsFromApi_containsExpectedFields() = runBlocking {
        val news = newsRepository.fetchGlobalWeatherNews()
        assertFalse("News should not be empty", news.isEmpty())
        val firstArticle = news.first()
        assertNotNull("Title should not be null", firstArticle.title)
        assertNotNull("URL should not be null", firstArticle.url)
        assertNotNull("Published date should not be null", firstArticle.publishedAt)
        assertNotNull("Article content should not be empty", firstArticle.description)

    }

    @Test
    fun testFetchGlobalAndLocalNews_returnDifferentResults() = runBlocking {
        val globalNews = newsRepository.fetchGlobalWeatherNews()
        val localNews = newsRepository.fetchLocalWeatherNews()
        assertNotEquals("Global and local news should be different", globalNews, localNews)
    }

}