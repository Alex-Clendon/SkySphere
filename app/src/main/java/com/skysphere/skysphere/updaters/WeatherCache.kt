package com.skysphere.skysphere.updaters

import java.util.concurrent.TimeUnit
import com.skysphere.skysphere.services.weather.json.WeatherResults

class WeatherCache {
    private var lastFetchTime: Long = 0
    var cachedWeatherResults: WeatherResults? = null

    fun isCacheValid(): Boolean {
        val currentTime = System.currentTimeMillis()
        val timeDifference = currentTime - lastFetchTime
        // Check if the cache is valid (less than 15 minutes)
        return TimeUnit.MILLISECONDS.toMinutes(timeDifference) < 15
    }

    fun updateCache(weatherResults: WeatherResults) {
        cachedWeatherResults = weatherResults
        lastFetchTime = System.currentTimeMillis()
    }
}
