package com.skysphere.skysphere.data

import android.util.Log
import com.skysphere.skysphere.data.dao.CurrentWeatherDao
import com.skysphere.skysphere.data.dao.DailyWeatherDao
import com.skysphere.skysphere.data.dao.HourlyWeatherDao
import com.skysphere.skysphere.data.entities.CurrentWeatherEntity
import com.skysphere.skysphere.data.entities.DailyWeatherEntity
import com.skysphere.skysphere.data.entities.HourlyWeatherEntity
import com.skysphere.skysphere.data.weather.WeatherCurrent
import com.skysphere.skysphere.data.weather.WeatherDaily
import com.skysphere.skysphere.data.weather.WeatherHourly
import com.skysphere.skysphere.data.weather.WeatherResults
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
        Log.d("Database Operation:", "Data Stored in database")
    }

    fun getWeatherDataFromDatabase(): WeatherResults {
        // Get hourly and daily data from database
        val currentWeather = currentWeatherDao.getLatestCurrentWeather()
        val hourlyWeatherList = hourlyWeatherDao.getHourlyWeather()
        val dailyWeatherList = dailyWeatherDao.getDailyWeather()

        // Retrieve current data
        val current = currentWeather?.let {
            WeatherCurrent(  // Change this to your actual class that holds current weather data
                temperature = it.temperature,
                apparentTemperature = it.apparentTemperature,
                relativeHumidity = it.humidity,
                weatherCode = it.weatherCode,
                windSpeed = it.windSpeed,
                windDirection = it.windDirection,
                visibility = it.visibility,
                time = it.time
            )
        }

        // Retrieve hourly data
        val hourly = hourlyWeatherList?.let {
            WeatherHourly(
                time = it.map { it.time },
                temperature = hourlyWeatherList.map { it.temperature },
                apparentTemperature = hourlyWeatherList.map { it.apparentTemperature },
                precipitationProbability = hourlyWeatherList.map { it.precipitationProbability },
                precipitation = hourlyWeatherList.map { it.precipitation },
                weatherCode = hourlyWeatherList.map { it.weatherCode },
                isDay = hourlyWeatherList.map { it.isDay }
            )
        }

        // Retrieve daily data
        val daily = dailyWeatherList?.let {
            WeatherDaily(
                time = it.map { it.time },
                weatherCode = dailyWeatherList.map { it.weatherCode },
                temperatureMax = dailyWeatherList.map { it.temperatureMax },
                temperatureMin = dailyWeatherList.map { it.temperatureMin },
                precipitationProbability = dailyWeatherList.map { it.precipitationProbability },
                precipitationSum = dailyWeatherList.map { it.precipitationSum },
                apparentTemperatureMax = dailyWeatherList.map { it.apparentTemperatureMax },
                apparentTemperatureMin = dailyWeatherList.map { it.apparentTemperatureMin },
                sunrise = dailyWeatherList.map { it.sunrise },
                sunset = dailyWeatherList.map { it.sunset },
                sunshineDuration = dailyWeatherList.map { it.sunshineDuration },
                uvIndexMax = dailyWeatherList.map { it.uvIndexMax }
            )
        }

        Log.d("Database Operation:", "Weather retrieved from database")
        return WeatherResults(current = current, hourly = hourly, daily = daily)
    }
}