package com.skysphere.skysphere.ui.details

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.skysphere.skysphere.R
import com.skysphere.skysphere.WeatherViewModel
import com.skysphere.skysphere.data.weather.WeatherResults
import com.skysphere.skysphere.databinding.FragmentDetailsBinding
import com.skysphere.skysphere.ui.adapters.DailyWeatherAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DetailsFragment : Fragment() {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var viewModel: WeatherViewModel // Hilt will provide this
    private var weatherResults: WeatherResults? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.weatherResults.observe(this) { results ->
            weatherResults = results
            getData()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout using view binding
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        activity?.window?.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.background_white)



        return binding.root
    }

    private fun getData() {
        weatherResults?.let {
            // Weather
            it.current?.weatherType?.let { weatherType ->
                weatherType.lottieAnimRes?.let { lottieFileName ->
                    binding.ivWeatherState.setAnimation(lottieFileName)
                    binding.ivWeatherState.playAnimation()
                }
            }
            binding.tvWeatherState.text = it.current?.weatherText
            binding.tvUvIndex.text = it.daily?.uvIndex?.get(0).toString() + " " + it.daily?.uvIndexText?.get(0)
            binding.tvHumidity.text = it.current?.relativeHumidity?.toString() + "%"
            binding.tvVisibility.text = String.format("%.1f", it.current?.visibility) + " " + it.current?.visibilityUnit
            // Temperature
            binding.tvCurrentTemp.text =  String.format("%.1f", it.current?.temperature) + it.current?.tempUnit
            binding.tvApparentTemp.text = String.format("%.1f", it.current?.apparentTemperature) + it.current?.tempUnit
            binding.tvMaxTemp.text = String.format("%.1f", it.daily?.temperatureMax?.get(0)) + it.current?.tempUnit
            binding.tvMinTemp.text = String.format("%.1f", it.daily?.temperatureMin?.get(0)) + it.current?.tempUnit
            // Wind
            binding.tvWindSpeed.text =  String.format("%.1f", it.current?.windSpeed) + it.current?.windSpeedUnit
            binding.tvWindDegrees.text = it.current?.windDegrees.toString() + "°"
            binding.tvWindDirection.text = it.current?.windDirection
        } ?: run {

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear binding reference to avoid memory leaks
        activity?.window?.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.gradient_end)
    }
}