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
    private val wind_speedUnitKey = "wind_speed_unit"
    private val rainfallUnitKey = "rainfall_unit"

    // Declared the views that have been created in the XML files
    private lateinit var temperatureUnitTextView: TextView

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
        val kilometerPerHourButton: Button = view.findViewById(R.id.Km_h)
        val milesPerHourButton: Button = view.findViewById(R.id.Mph)
        val knotsButton: Button = view.findViewById(R.id.Knots)
        val millimetersButton: Button = view.findViewById(R.id.Millimeter)
        val inchesButton: Button = view.findViewById(R.id.Inches)

        // Assigning the views to their corresponding variables declared above
        temperatureUnitTextView = view.findViewById(R.id.temp_details)

        // Setting up the buttons in the settings fragment xml file with their corresponding metric unit
        celsiusButton.setOnClickListener {
            saveTemperatureUnit("Celsius")
        }
        fahrenheitButton.setOnClickListener{
            saveTemperatureUnit("Fahrenheit")
        }

        // Initializing the TextView Temperature Details with the current temperature unit
        updateTemperatureUnitTextView()

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

    // Retrieving the stored preference for temperature metric unit of the user
    private fun updateTemperatureUnitTextView() {
        val sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val unit = sharedPreferences.getString("temperature_unit", "Celsius") ?: "Celsius"

        // This sets the TextView for the Temperature Details to what it equates and displays it onto the settings page
        temperatureUnitTextView.text = "The temperature unit is currently set to $unit"
    }
}