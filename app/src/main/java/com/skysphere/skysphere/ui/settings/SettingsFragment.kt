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
    private lateinit var sharedPreferences: SharedPreferences
    private val temperatureUnitKey = "temperature_unit"
    private lateinit var temperatureUnitTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        val cButton: Button = view.findViewById(R.id.Celsius)
        val fButton: Button = view.findViewById(R.id.Fahrenheit)
        val kButton: Button = view.findViewById(R.id.Kelvin)
        temperatureUnitTextView = view.findViewById(R.id.temp_details)

        updateTemperatureUnitTextView()

        cButton.setOnClickListener {
            saveTemperatureUnit("Celsius")
        }

        fButton.setOnClickListener{
            saveTemperatureUnit("Fahrenheit")
        }

        kButton.setOnClickListener {
            saveTemperatureUnit("Kelvin")
        }

        return view
    }

    private fun saveTemperatureUnit(unit: String){
        val editor = sharedPreferences.edit()
        editor.putString(temperatureUnitKey, unit)
        editor.apply()
    }

    private fun updateTemperatureUnitTextView() {
        val sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val unit = sharedPreferences.getString("temperature_unit", "Celsius") ?: "Celsius"
        temperatureUnitTextView.text = "The temperature unit is currently set to $unit"
    }
}