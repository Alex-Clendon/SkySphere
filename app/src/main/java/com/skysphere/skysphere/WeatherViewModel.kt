package com.skysphere.skysphere
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.skysphere.skysphere.services.weather.json.ApiResults
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
) : ViewModel() {
    private val _weatherResults = MutableLiveData<ApiResults?>()
    val weatherResults: LiveData<ApiResults?> get() = _weatherResults

    fun setWeatherResults(results: ApiResults) {
        _weatherResults.postValue(results)
    }
}

