package com.skysphere.skysphere.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.skysphere.skysphere.R

class SettingsFragment : Fragment()
{
    // Initializing variables to store user preferences
    private lateinit var sharedPreferences: SharedPreferences
    private val temperatureUnitKey = "temperature_unit"
    private val windspeedUnitKey = "wind_speed_unit"
    private val rainfallUnitKey = "rainfall_unit"

    // Declared the views that have been created in the XML files
    private lateinit var temperatureUnitTextView: TextView
    private lateinit var windspeedUnitTextView: TextView
    private lateinit var rainfallUnitTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Initializing a SharedPreferences object called sharedPreferences so that the user can access the
        // shared preferences, allowing you to read and write preferences (such as user settings or application state)
        // in a persistent storage
        sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        // Assigning the buttons to their corresponding variables declared above
        val celsiusButton: Button = view.findViewById(R.id.Celsius)
        val fahrenheitButton: Button = view.findViewById(R.id.Fahrenheit)
        val kilometersPerHourButton: Button = view.findViewById(R.id.Km_h)
        val milesPerHourButton: Button = view.findViewById(R.id.Mph)
        val knotsButton: Button = view.findViewById(R.id.Knots)
        val millimetersButton: Button = view.findViewById(R.id.Millimeter)
        val inchesButton: Button = view.findViewById(R.id.Inches)

        // Assigning the views to their corresponding variables declared above
        temperatureUnitTextView = view.findViewById(R.id.temp_details)
        windspeedUnitTextView = view.findViewById(R.id.wind_speed_details)
        rainfallUnitTextView = view.findViewById(R.id.rainfall_details)

        // Setting up the buttons in the settings fragment xml file with their corresponding metric unit
        celsiusButton.setOnClickListener {
            saveTemperatureUnit("Celsius")
        }
        fahrenheitButton.setOnClickListener{
            saveTemperatureUnit("Fahrenheit")
        }
        kilometersPerHourButton.setOnClickListener{
            saveWindSpeedUnit("Kilometers/Hour")
        }
        milesPerHourButton.setOnClickListener{
            saveWindSpeedUnit("Miles/Hour")
        }
        knotsButton.setOnClickListener{
            saveWindSpeedUnit("Knots")
        }
        millimetersButton.setOnClickListener {
            saveRainfallUnit("Millimeters")
        }
        inchesButton.setOnClickListener {
            saveRainfallUnit("Inches")
        }

        // Initializing the TextView of Temperature Details with the current metric unit
        updateTemperatureUnitTextView()
        // Initializing the TextView of Wind Speed Details with the current metric unit
        updateWindSpeedUnitTextView()
        // Initializing the TextView of Rainfall Details with the current metric unit
        updateRainfallUnitTextView()

        return view
    }

    // Saving the preference for the temperature metric unit of the user
    private fun saveTemperatureUnit(unit: String){
        val editor = sharedPreferences.edit()
        editor.putString(temperatureUnitKey, unit)
        editor.apply()

        // Updating the TextView Temperature Details with the chosen temperature unit
        updateTemperatureUnitTextView()
    }

    // Saving the preference for the wind speed metric unit of the user
    private fun saveWindSpeedUnit(unit: String) {
        val editor = sharedPreferences.edit()
        editor.putString(windspeedUnitKey, unit)
        editor.apply()

        // Updating the TextView Wind Speed Details with the chosen temperature unit
        updateWindSpeedUnitTextView()
    }

    // Saving the preference for the rainfall metric unit of the user
    private fun saveRainfallUnit(unit: String) {
        val editor = sharedPreferences.edit()
        editor.putString(rainfallUnitKey, unit)
        editor.apply()

        // Updating the TextView Rainfall Details with the chosen temperature unit
        updateRainfallUnitTextView()
    }

    // Retrieving the stored preference for temperature metric unit of the user
    private fun updateTemperatureUnitTextView() {
        val sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val unit = sharedPreferences.getString("temperature_unit", "Celsius") ?: "Celsius"

        // This sets the TextView for the Temperature Details to what it equates and displays it onto the settings page
        temperatureUnitTextView.text = "The temperature unit is currently set to $unit"
    }

    // Retrieving the stored preference for wind speed metric unit of the user
    private fun updateWindSpeedUnitTextView() {
        val sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val unit = sharedPreferences.getString("wind_speed_unit", "Kilometers/Hour") ?: "Kilometers/Hour"

        // This sets the TextView for the Wind Speed Details to what it equates and displays it onto the settings page
        windspeedUnitTextView.text = "The wind speed unit is currently set to $unit"
    }

    // Retrieving the stored preference for rainfall metric unit of the user
    private fun updateRainfallUnitTextView() {
        val sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val unit = sharedPreferences.getString("rainfall_unit", "Millimeters") ?: "Millimeters"

        // This sets the TextView for the rainfall Details to what it equates and displays it onto the settings page
        rainfallUnitTextView.text = "The rainfall unit is currently set to $unit"
    }
}