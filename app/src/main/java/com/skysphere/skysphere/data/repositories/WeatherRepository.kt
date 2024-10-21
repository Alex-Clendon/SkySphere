package com.skysphere.skysphere.data.repositories

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.skysphere.skysphere.API.WeatherType
import com.skysphere.skysphere.data.ConversionHelper
import com.skysphere.skysphere.data.SettingsManager
import com.skysphere.skysphere.data.dao.weather.CurrentWeatherDao
import com.skysphere.skysphere.data.dao.weather.DailyWeatherDao
import com.skysphere.skysphere.data.dao.weather.HourlyWeatherDao
import com.skysphere.skysphere.data.entities.weather.CurrentWeatherEntity
import com.skysphere.skysphere.data.entities.weather.DailyWeatherEntity
import com.skysphere.skysphere.data.entities.weather.HourlyWeatherEntity
import com.skysphere.skysphere.data.WeatherCurrent
import com.skysphere.skysphere.data.WeatherDaily
import com.skysphere.skysphere.data.WeatherHourly
import com.skysphere.skysphere.data.WeatherResults
import com.skysphere.skysphere.services.weather.WeatherService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.math.roundToInt

/*
    Repository class that stores data into and retrieves data from the local database
 */
class WeatherRepository @Inject constructor(
    private val currentWeatherDao: CurrentWeatherDao,
    private val hourlyWeatherDao: HourlyWeatherDao,
    private val dailyWeatherDao: DailyWeatherDao,
    private val weatherService: WeatherService,
    private val settingsManager: SettingsManager,
    @ApplicationContext private val context: Context // Injecting the application context
) {
    suspend fun fetchAndStoreWeatherData() {

        // Make API Call
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
                precipitationProbability = it.current?.precipitationProbability,
                precipitationSum = it.current?.precipitation,
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
            // Clear previous data, as the primary key (time) was always different, so it would infinitely store lists of data
            hourlyWeatherDao.clearHourlyWeather()
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
                    windSpeed = it.daily?.windSpeed?.getOrNull(index),
                    windDegrees = it.daily?.windDirection?.getOrNull(index),
                    apparentTemperatureMax = it.daily?.apparentTemperatureMax?.getOrNull(index),
                    apparentTemperatureMin = it.daily?.apparentTemperatureMin?.getOrNull(index),
                    sunrise = it.daily?.sunrise?.getOrNull(index),
                    sunset = it.daily?.sunset?.getOrNull(index),
                    sunshineDuration = it.daily?.sunshineDuration?.getOrNull(index),
                    uvIndexMax = it.daily?.uvIndexMax?.getOrNull(index),
                    timestamp = System.currentTimeMillis(),
                    visibility = it.daily?.visibility?.getOrNull(index)
                )
            } ?: emptyList()
            // Clear previous data, as the primary key (time) was always different, so it would infinitely store lists of data
            dailyWeatherDao.clearDailyWeather()
            dailyWeatherDao.insertDailyWeather(dailyWeatherList)
        }

        // Store fetch time to update the last updated text in the home fragment
        val sharedPreferences = context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
        val currentTime = System.currentTimeMillis()
        sharedPreferences.edit().putLong("last_execution_time", currentTime).apply()
        
    }

    /*
        Retrieves stored data from local database
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getWeatherDataFromDatabase(): WeatherResults {

        // Get hourly and daily data from database
        val currentWeather = currentWeatherDao.getLatestCurrentWeather()
        val hourlyWeatherList = hourlyWeatherDao.getHourlyWeather()
        val dailyWeatherList = dailyWeatherDao.getDailyWeather()

        // Retrieve current data
        val current = currentWeather?.let {
            WeatherCurrent(
                temperature = ConversionHelper.convertTemperature(
                    it.temperature,
                    settingsManager.getTemperatureUnit()
                ),
                apparentTemperature = ConversionHelper.convertTemperature(
                    it.apparentTemperature,
                    settingsManager.getTemperatureUnit()
                ),
                tempUnit = settingsManager.getTemperatureSymbol(),
                roundedTemperature = ConversionHelper.convertRoundedTemperature(
                    it.temperature,
                    settingsManager.getTemperatureUnit()
                ),
                roundedApparentTemperature = ConversionHelper.convertRoundedTemperature(
                    it.apparentTemperature,
                    settingsManager.getTemperatureUnit()
                ),
                relativeHumidity = it.humidity,
                weatherCode = it.weatherCode,
                weatherType = WeatherType.fromWMO(it.weatherCode),
                weatherText = WeatherType.fromWMO(it.weatherCode).weatherDesc,
                windSpeed = ConversionHelper.convertWindSpeed(
                    it.windSpeed,
                    settingsManager.getWindSpeedUnit()
                ),
                windSpeedUnit = settingsManager.getWindSpeedUnit(),
                windDegrees = it.windDirection?.roundToInt(),
                windDirection = ConversionHelper.convertWindDirection(it.windDirection),
                precipitationProbability = it.precipitationProbability,
                precipitation = ConversionHelper.convertPrecipitation(
                    it.precipitationSum,
                    settingsManager.getPrecipitationUnit()
                ),
                precipitationUnit = settingsManager.getPrecipitationUnit(),
                visibility = ConversionHelper.convertVisibility(
                    it.visibility,
                    settingsManager.getVisibilityUnit()
                ),
                visibilityUnit = settingsManager.getVisibilityUnit(),
                time = it.time,
                date = ConversionHelper.convertToDate(it.time),
                updatedTime = ConversionHelper.convertTime(context)
            )
        }

        // Retrieve hourly data
        val hourly = hourlyWeatherList?.let {
            WeatherHourly(
                time = it.map { it.time },
                temperature = hourlyWeatherList.map {
                    ConversionHelper.convertTemperature(
                        it.temperature,
                        settingsManager.getTemperatureUnit()
                    )
                },
                apparentTemperature = hourlyWeatherList.map {
                    ConversionHelper.convertTemperature(
                        it.apparentTemperature,
                        settingsManager.getTemperatureUnit()
                    )
                },
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
                weatherType = dailyWeatherList.map { WeatherType.fromWMO(it.weatherCode) },
                temperatureMax = dailyWeatherList.map {
                    ConversionHelper.convertTemperature(
                        it.temperatureMax,
                        settingsManager.getTemperatureUnit()
                    )
                },
                temperatureMin = dailyWeatherList.map {
                    ConversionHelper.convertTemperature(
                        it.temperatureMin,
                        settingsManager.getTemperatureUnit()
                    )
                },
                roundedTemperatureMax = dailyWeatherList.map {
                    ConversionHelper.convertRoundedTemperature(
                        it.temperatureMax,
                        settingsManager.getTemperatureUnit()
                    )
                },
                roundedTemperatureMin = dailyWeatherList.map {
                    ConversionHelper.convertRoundedTemperature(
                        it.temperatureMin,
                        settingsManager.getTemperatureUnit()
                    )
                },
                precipitationProbability = dailyWeatherList.map { it.precipitationProbability },
                precipitationSum = dailyWeatherList.map { it.precipitationSum },
                windSpeed = dailyWeatherList.map {
                    ConversionHelper.convertWindSpeed(
                        it.windSpeed,
                        settingsManager.getWindSpeedUnit()
                    )
                },
                windDegrees = dailyWeatherList.map { it.windDegrees?.roundToInt() },
                windDirection = dailyWeatherList.map { ConversionHelper.convertWindDirection(it.windDegrees) },
                apparentTemperatureMax = dailyWeatherList.map {
                    ConversionHelper.convertRoundedTemperature(
                        it.apparentTemperatureMax,
                        settingsManager.getTemperatureUnit()
                    )
                },
                apparentTemperatureMin = dailyWeatherList.map {
                    ConversionHelper.convertRoundedTemperature(
                        it.apparentTemperatureMin,
                        settingsManager.getTemperatureUnit()
                    )
                },
                sunrise = dailyWeatherList.map { ConversionHelper.convertSunHours(it.sunrise) },
                sunset = dailyWeatherList.map { ConversionHelper.convertSunHours(it.sunset) },
                sunshineDuration = dailyWeatherList.map { ConversionHelper.convertDuration(it.sunshineDuration) },
                uvIndex = dailyWeatherList.map { it.uvIndexMax?.toInt() },
                uvIndexText = dailyWeatherList.map { ConversionHelper.convertUV(it.uvIndexMax) },
                day = dailyWeatherList.map { ConversionHelper.convertToDay(it.time) },
                visibility = dailyWeatherList.map {
                    ConversionHelper.convertVisibility(
                        it.visibility,
                        settingsManager.getVisibilityUnit()
                    )
                }
            )
        }
        return WeatherResults(current = current, hourly = hourly, daily = daily)
    }
}