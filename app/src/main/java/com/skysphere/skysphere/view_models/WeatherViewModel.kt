package com.skysphere.skysphere.view_models
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skysphere.skysphere.data.WeatherResults
import com.skysphere.skysphere.data.repositories.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/*
    Shared View Model that shares live data to the fragments
 */
@HiltViewModel
class WeatherViewModel @Inject constructor(
private val repository: WeatherRepository
) : ViewModel() {
    private val _weatherResults = MutableLiveData<WeatherResults?>()
    val weatherResults: LiveData<WeatherResults?> get() = _weatherResults

    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchWeatherData() {    // Function to update model with database results
        viewModelScope.launch(Dispatchers.IO) {
            val weatherResults = repository.getWeatherDataFromDatabase()
            _weatherResults.postValue(weatherResults)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getData(): WeatherResults? {
        return repository.getWeatherDataFromDatabase()
    }

}

