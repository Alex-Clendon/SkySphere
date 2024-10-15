package com.skysphere.skysphere.data

import com.skysphere.skysphere.data.dao.CurrentWeatherDao
import com.skysphere.skysphere.data.dao.DailyWeatherDao
import com.skysphere.skysphere.data.dao.HourlyWeatherDao
import com.skysphere.skysphere.data.entities.CurrentWeatherEntity
import com.skysphere.skysphere.data.entities.DailyWeatherEntity
import com.skysphere.skysphere.data.entities.HourlyWeatherEntity
import com.skysphere.skysphere.services.weather.WeatherService
import javax.inject.Inject

class WeatherRepository @Inject constructor(
    private val currentWeatherDao: CurrentWeatherDao,
    private val hourlyWeatherDao: HourlyWeatherDao,
    private val dailyWeatherDao: DailyWeatherDao,
    private val weatherService: WeatherService
) {
    suspend fun fetchAndStoreWeatherData() {
        val response = weatherService.getWeather()

        response?.let {
            // Insert current weather data
            val currentWeather = CurrentWeatherEntity(
                temperature = it.current?.temperature,
                apparentTemperature = it.current?.apparentTemperature,
                humidity = it.current?.relativeHumidity,
                weatherCode = it.current?.weatherCode,
                windSpeed = it.current?.windSpeed,
                windDirection = it.current?.windDirection,
                visibility = it.current?.visibility,
                time = it.current?.time ?: "",
                timestamp = System.currentTimeMillis()
            )
            currentWeatherDao.insertCurrentWeather(currentWeather)

            // Insert hourly weather data
            val hourlyWeatherList = it.hourly?.time?.mapIndexed { index, time ->
                HourlyWeatherEntity(
                    time = time,
                    temperature = it.hourly?.temperature?.getOrNull(index),
                    apparentTemperature = it.hourly?.apparentTemperature?.getOrNull(index),
                    precipitationProbability = it.hourly?.precipitationProbability?.getOrNull(index),
                    precipitation = it.hourly?.precipitation?.getOrNull(index),
                    weatherCode = it.hourly?.weatherCode?.getOrNull(index),
                    isDay = it.hourly?.isDay?.getOrNull(index),
                    timestamp = System.currentTimeMillis()
                )
            } ?: emptyList()
            hourlyWeatherDao.insertHourlyWeather(hourlyWeatherList)

            // Insert daily weather data
            val dailyWeatherList = it.daily?.time?.mapIndexed { index, time ->
                DailyWeatherEntity(
                    time = time,
                    weatherCode = it.daily?.weatherCode?.getOrNull(index),
                    temperatureMax = it.daily?.temperatureMax?.getOrNull(index),
                    temperatureMin = it.daily?.temperatureMin?.getOrNull(index),
                    precipitationProbability = it.daily?.precipitationProbability?.getOrNull(index),
                    precipitationSum = it.daily?.precipitationSum?.getOrNull(index),
                    apparentTemperatureMax = it.daily?.apparentTemperatureMax?.getOrNull(index),
                    apparentTemperatureMin = it.daily?.apparentTemperatureMin?.getOrNull(index),
                    sunrise = it.daily?.sunrise?.getOrNull(index),
                    sunset = it.daily?.sunset?.getOrNull(index),
                    sunshineDuration = it.daily?.sunshineDuration?.getOrNull(index),
                    uvIndexMax = it.daily?.uvIndexMax?.getOrNull(index),
                    timestamp = System.currentTimeMillis()
                )
            } ?: emptyList()
            dailyWeatherDao.insertDailyWeather(dailyWeatherList)
        }
    }

    suspend fun getCachedCurrentWeather(): CurrentWeatherEntity? = currentWeatherDao.getLatestCurrentWeather()

    suspend fun getCachedHourlyWeather(): List<HourlyWeatherEntity>? = hourlyWeatherDao.getHourlyWeather()

    suspend fun getCachedDailyWeather(): List<DailyWeatherEntity>? = dailyWeatherDao.getDailyWeather()
}