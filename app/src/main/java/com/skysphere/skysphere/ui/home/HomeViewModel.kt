package com.skysphere.skysphere.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skysphere.skysphere.WeatherData
import com.skysphere.skysphere.WeatherType
import kotlinx.coroutines.launch
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    private val _weatherIcon = MutableLiveData<Int>()
    val weatherIcon: LiveData<Int> = _weatherIcon

    private val _temperature = MutableLiveData<String>()
    val temperature: LiveData<String> = _temperature

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    init {
        fetchWeatherData()
    }

    //private val weatherAPI = MainActivity.retrofit.create(WeatherAPI::class.java)

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchWeatherData() {
        viewModelScope.launch {
            try {
                //val response = weatherAPI.getWeatherData(40.90, 174.89, "weather_code,temperature_2m")
                //processWeatherData(response)
            } catch (e: Exception) {
                _text.value = "Error: ${e.message}"
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun processWeatherData(weatherData: WeatherData) {
        val currentHour = LocalDateTime.now().hour

        val weatherCodes = weatherData.hourly.weather_code
        val temperature = weatherData.hourly.temperature_2m

        val currentWeatherCode = weatherCodes.getOrNull(currentHour) ?: 0
        val currentTemperature = temperature.getOrNull(currentHour) ?: 0

        val weatherType = WeatherType.fromWMO(currentWeatherCode)

        _weatherIcon.value = weatherType.iconRes
        _temperature.value = "$currentTemperature"
    }

}