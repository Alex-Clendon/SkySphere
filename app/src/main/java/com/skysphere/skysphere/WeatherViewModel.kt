package com.skysphere.skysphere
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skysphere.skysphere.data.WeatherRepository
import com.skysphere.skysphere.data.weather.WeatherResults
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
private val repository: WeatherRepository
) : ViewModel() {
    private val _weatherResults = MutableLiveData<WeatherResults?>()
    val weatherResults: LiveData<WeatherResults?> get() = _weatherResults

    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchWeatherData() {
        viewModelScope.launch(Dispatchers.IO) {
            val weatherResults = repository.getWeatherDataFromDatabase()
            _weatherResults.postValue(weatherResults) // Update LiveData on the main thread
            Log.d("Database Operation:", "Weather Fetched From storage:")
        }
    }
}

