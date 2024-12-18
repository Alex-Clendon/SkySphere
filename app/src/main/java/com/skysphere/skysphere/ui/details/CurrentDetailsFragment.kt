package com.skysphere.skysphere.ui.details

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.skysphere.skysphere.R
import com.skysphere.skysphere.data.WeatherResults
import com.skysphere.skysphere.databinding.FragmentCurrentDetailsBinding
import com.skysphere.skysphere.view_models.WeatherViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CurrentDetailsFragment : Fragment() {

    private var _binding: FragmentCurrentDetailsBinding? = null
    private val binding get() = _binding!!
    /*
        Inject data using Hilt
     */
    @Inject
    lateinit var viewModel: WeatherViewModel
    private var weatherResults: WeatherResults? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Observe the live data from the shared view model
        viewModel.weatherResults.observe(this) { results ->
            weatherResults = results
            setData()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout using view binding
        _binding = FragmentCurrentDetailsBinding.inflate(inflater, container, false)
        activity?.window?.navigationBarColor =
            ContextCompat.getColor(requireContext(), R.color.background_white)



        return binding.root
    }

    // Initializes UI components using binding
    private fun setData() {
        weatherResults?.let {
            // Weather
            it.current?.weatherType?.let { weatherType ->
                weatherType.lottieAnimRes?.let { lottieFileName ->
                    binding.ivWeatherState.setAnimation(lottieFileName)
                    binding.ivWeatherState.playAnimation()
                }
            }
            binding.tvWeatherState.text = it.current?.weatherText
            binding.tvUvIndex.text =
                it.daily?.uvIndex?.get(0).toString() + " " + it.daily?.uvIndexText?.get(0)
            binding.tvHumidity.text = it.current?.relativeHumidity?.toString() + "%"
            binding.tvVisibility.text =
                String.format("%.1f", it.current?.visibility) + " " + it.current?.visibilityUnit
            // Temperature
            binding.tvCurrentTemp.text =
                String.format("%.1f", it.current?.temperature) + it.current?.tempUnit
            binding.tvApparentTemp.text =
                String.format("%.1f", it.current?.apparentTemperature) + it.current?.tempUnit
            binding.tvMaxTemp.text =
                String.format("%.1f", it.daily?.temperatureMax?.get(0)) + it.current?.tempUnit
            binding.tvMinTemp.text =
                String.format("%.1f", it.daily?.temperatureMin?.get(0)) + it.current?.tempUnit
            // Wind
            binding.tvWindSpeed.text =
                String.format("%.1f", it.current?.windSpeed) + it.current?.windSpeedUnit
            binding.tvWindDegrees.text = it.current?.windDegrees.toString() + "°"
            binding.tvWindDirection.text = it.current?.windDirection
            // Precipitation
            binding.tvProbability.text = it.current?.precipitationProbability.toString() + "%"
            binding.tvSum.text =
                it.current?.precipitation.toString() + it.current?.precipitationUnit
            // Sun
            binding.ivSun.setAnimation(R.raw.clear_day)
            binding.ivSun.playAnimation()
            binding.tvSunrise.text = it.daily?.sunrise?.get(0).toString()
            binding.tvSunset.text = it.daily?.sunset?.get(0).toString()
            binding.tvDuration.text = it.daily?.sunshineDuration?.get(0)
        } ?: run {

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear binding reference to avoid memory leaks
        activity?.window?.navigationBarColor =
            ContextCompat.getColor(requireContext(), R.color.gradient_end)
    }
}