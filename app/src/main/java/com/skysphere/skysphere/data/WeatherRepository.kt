package com.skysphere.skysphere.data

import android.content.Context
import android.util.Log
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
import javax.inject.Inject
import kotlin.math.roundToInt

class WeatherRepository @Inject constructor(
    private val currentWeatherDao: CurrentWeatherDao,
    private val hourlyWeatherDao: HourlyWeatherDao,
    private val dailyWeatherDao: DailyWeatherDao,
    private val weatherService: WeatherService,
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

    fun getWeatherDataFromDatabase(): WeatherResults {
        // Get hourly and daily data from database
        val currentWeather = currentWeatherDao.getLatestCurrentWeather()
        val hourlyWeatherList = hourlyWeatherDao.getHourlyWeather()
        val dailyWeatherList = dailyWeatherDao.getDailyWeather()

        // Retrieve current data
        val current = currentWeather?.let {
            WeatherCurrent(  // Change this to your actual class that holds current weather data
                temperature = convertTemperature(it.temperature),
                apparentTemperature = convertTemperature(it.apparentTemperature),
                tempUnit = setUnit(),
                relativeHumidity = it.humidity,
                weatherCode = it.weatherCode,
                weatherType = WeatherType.fromWMO(it.weatherCode),
                weatherText = WeatherType.fromWMO(it.weatherCode).weatherDesc,
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
                temperature = hourlyWeatherList.map { convertTemperature(it.temperature) },
                apparentTemperature = hourlyWeatherList.map { convertTemperature(it.apparentTemperature) },
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
                temperatureMax = dailyWeatherList.map { convertTemperature(it.temperatureMax) },
                temperatureMin = dailyWeatherList.map { convertTemperature(it.temperatureMin) },
                precipitationProbability = dailyWeatherList.map { it.precipitationProbability },
                precipitationSum = dailyWeatherList.map { it.precipitationSum },
                apparentTemperatureMax = dailyWeatherList.map { convertTemperature(it.apparentTemperatureMax) },
                apparentTemperatureMin = dailyWeatherList.map { convertTemperature(it.apparentTemperatureMin) },
                sunrise = dailyWeatherList.map { it.sunrise },
                sunset = dailyWeatherList.map { it.sunset },
                sunshineDuration = dailyWeatherList.map { it.sunshineDuration },
                uvIndexMax = dailyWeatherList.map { it.uvIndexMax }
            )
        }

        Log.d("Database Operation:", "Weather retrieved from database")
        return WeatherResults(current = current, hourly = hourly, daily = daily)
    }

    private fun convertTemperature(temperature: Double?): Int? {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val tempUnit = sharedPreferences.getString("temperature_unit", "Celsius")

        if(tempUnit != "Celsius")
        {
            if (temperature != null) {
                return ((temperature * 9/5) + 32).roundToInt()
            }
        }
        return temperature?.roundToInt()
    }

    private fun setUnit(): String {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val tempUnit = sharedPreferences.getString("temperature_unit", "Celsius")

        if(tempUnit != "Celsius")
        {
            return "°F"
        }
        return "°C"
    }
}