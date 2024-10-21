package com.skysphere.skysphere.ui.details

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.skysphere.skysphere.R
import com.skysphere.skysphere.view_models.WeatherViewModel
import com.skysphere.skysphere.data.WeatherResults
import com.skysphere.skysphere.databinding.FragmentDailyDetailsBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DailyDetailsFragment : Fragment() {

    private var _binding: FragmentDailyDetailsBinding? = null
    private val binding get() = _binding!!
    private var position: Int = -1
    /*
        Inject data using Hilt
     */
    @Inject
    lateinit var viewModel: WeatherViewModel
    private var weatherResults: WeatherResults? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            position = bundle.getInt("clickedPosition", -1) // Default to -1 if not found
        }

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
        _binding = FragmentDailyDetailsBinding.inflate(inflater, container, false)
        activity?.window?.navigationBarColor =
            ContextCompat.getColor(requireContext(), R.color.background_white)



        return binding.root
    }

    // Initializes UI components using binding
    private fun setData() {

        weatherResults?.let {
            // Set title to current day
            (activity as AppCompatActivity?)!!.supportActionBar!!.title =
                it.daily?.day?.get(position).toString()
            // Weather
            it.daily?.weatherType?.get(position).let { weatherType ->
                weatherType?.lottieAnimRes?.let { lottieFileName ->
                    binding.ivWeatherState.setAnimation(lottieFileName)
                    binding.ivWeatherState.playAnimation()
                }
            }
            binding.tvWeatherState.text = it.daily?.weatherText?.get(position)
            binding.tvUvIndex.text =
                it.daily?.uvIndex?.get(position).toString() + " " + it.daily?.uvIndexText?.get(
                    position
                )
            binding.tvVisibility.text = String.format(
                "%.1f",
                it.daily?.visibility?.get(position)
            ) + " " + it.current?.visibilityUnit
            // Temperature
            binding.tvMaxTemp.text = String.format(
                "%.1f",
                it.daily?.temperatureMax?.get(position)
            ) + it.current?.tempUnit
            binding.tvMinTemp.text = String.format(
                "%.1f",
                it.daily?.temperatureMin?.get(position)
            ) + it.current?.tempUnit
            // Wind
            binding.tvWindSpeed.text = String.format(
                "%.1f",
                it.daily?.windSpeed?.get(position)
            ) + it.current?.windSpeedUnit
            binding.tvWindDegrees.text = it.daily?.windDegrees?.get(position).toString() + "°"
            binding.tvWindDirection.text = it.daily?.windDirection?.get(position)
            // Precipitation
            binding.tvProbability.text =
                it.daily?.precipitationProbability?.get(position).toString() + "%"
            binding.tvSum.text =
                it.daily?.precipitationSum?.get(position).toString() + it.current?.precipitationUnit
            // Sun
            binding.ivSun.setAnimation(R.raw.clear_day)
            binding.ivSun.playAnimation()
            binding.tvSunrise.text = it.daily?.sunrise?.get(position).toString()
            binding.tvSunset.text = it.daily?.sunset?.get(position).toString()
            binding.tvDuration.text = it.daily?.sunshineDuration?.get(position)
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