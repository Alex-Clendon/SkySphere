package com.skysphere.skysphere.data

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.skysphere.skysphere.API.WeatherType
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
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

class WeatherRepository @Inject constructor(
    private val currentWeatherDao: CurrentWeatherDao,
    private val hourlyWeatherDao: HourlyWeatherDao,
    private val dailyWeatherDao: DailyWeatherDao,
    private val weatherService: WeatherService,
    private val settingsManager: SettingsManager,
    @ApplicationContext private val context: Context // Injecting the application context
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun getWeatherDataFromDatabase(): WeatherResults {
        // Get hourly and daily data from database
        val currentWeather = currentWeatherDao.getLatestCurrentWeather()
        val hourlyWeatherList = hourlyWeatherDao.getHourlyWeather()
        val dailyWeatherList = dailyWeatherDao.getDailyWeather()

        // Retrieve current data
        val current = currentWeather?.let {
            WeatherCurrent(
                temperature = ConversionHelper.convertTemperature(it.temperature, settingsManager.getTemperatureUnit()),
                apparentTemperature = ConversionHelper.convertTemperature(it.apparentTemperature, settingsManager.getTemperatureUnit()),
                tempUnit = settingsManager.getTemperatureSymbol(),
                relativeHumidity = it.humidity,
                weatherCode = it.weatherCode,
                weatherType = WeatherType.fromWMO(it.weatherCode),
                weatherText = WeatherType.fromWMO(it.weatherCode).weatherDesc,
                windSpeed = it.windSpeed,
                windDirection = it.windDirection,
                visibility = it.visibility,
                time = it.time,
                date = ConversionHelper.convertToDate(it.time)
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
                weatherText = hourlyWeatherList.map { WeatherType.fromWMO(it.weatherCode).weatherDesc },
                isDay = hourlyWeatherList.map { it.isDay }
            )
        }

        // Retrieve daily data
        val daily = dailyWeatherList?.let {
            WeatherDaily(
                time = it.map { it.time },
                weatherCode = dailyWeatherList.map { it.weatherCode },
                weatherText = dailyWeatherList.map { WeatherType.fromWMO(it.weatherCode).weatherDesc },
                temperatureMax = dailyWeatherList.map { ConversionHelper.convertTemperature(it.temperatureMax, settingsManager.getTemperatureUnit()) },
                temperatureMin = dailyWeatherList.map { ConversionHelper.convertTemperature(it.temperatureMin, settingsManager.getTemperatureUnit()) },
                precipitationProbability = dailyWeatherList.map { it.precipitationProbability },
                precipitationSum = dailyWeatherList.map { it.precipitationSum },
                apparentTemperatureMax = dailyWeatherList.map { ConversionHelper.convertTemperature(it.apparentTemperatureMax, settingsManager.getTemperatureUnit()) },
                apparentTemperatureMin = dailyWeatherList.map { ConversionHelper.convertTemperature(it.apparentTemperatureMin, settingsManager.getTemperatureUnit()) },
                sunrise = dailyWeatherList.map { it.sunrise },
                sunset = dailyWeatherList.map { it.sunset },
                sunshineDuration = dailyWeatherList.map { it.sunshineDuration },
                uvIndexMax = dailyWeatherList.map { it.uvIndexMax },
                day = dailyWeatherList.map { ConversionHelper.convertToDay(it.time) }
            )
        }

        Log.d("Database Operation:", "Weather retrieved from database")
        return WeatherResults(current = current, hourly = hourly, daily = daily)
    }
}