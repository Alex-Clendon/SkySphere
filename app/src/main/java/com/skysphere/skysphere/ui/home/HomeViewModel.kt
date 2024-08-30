package com.skysphere.skysphere.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skysphere.skysphere.RetrofitInstance
import com.skysphere.skysphere.WeatherData
import com.skysphere.skysphere.WeatherResponse
import com.skysphere.skysphere.WeatherType
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    private val _weatherData = MutableLiveData<WeatherData>()
    val weatherData: LiveData<WeatherData> = _weatherData

    private val _weatherType = MutableLiveData<WeatherType>()
    val weatherType: LiveData<WeatherType> = _weatherType

    fun  fetchDataFromAPI() {
        viewModelScope.launch {
            try{
                val weatherService = RetrofitInstance.instance
            } catch(e: Exception){
                _text.value = "Error fetching data: ${e.message}"
            }
        }
    }

    private fun parseWeatherData(jsonObject: JSONObject): WeatherData {
        return WeatherData(
            weatherCode = jsonObject.getJSONObject("hourly").getJSONArray("weather_code").getInt(0)
        )
    }

}