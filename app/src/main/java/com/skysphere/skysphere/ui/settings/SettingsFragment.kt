package com.skysphere.skysphere.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.skysphere.skysphere.MainActivity
import com.skysphere.skysphere.R
import com.skysphere.skysphere.WeatherViewModel
import com.skysphere.skysphere.data.SettingsManager
import com.skysphere.skysphere.notifications.WeatherService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.O)
class SettingsFragment : Fragment() {
    /*
        Inject Hilt components
     */
    @Inject
    lateinit var viewModel: WeatherViewModel

    @Inject
    lateinit var settingsManager: SettingsManager


    // Object for severe notification preference key, needed for the notification functionality
    companion object {
        const val SEVERE_NOTIFICATION_PREFERENCE_KEY = "severe_notification_preference"
    }

    // Declared the views that have been created in the XML files
    private lateinit var temperatureUnitTextView: TextView
    private lateinit var windspeedUnitTextView: TextView
    private lateinit var rainfallUnitTextView: TextView
    private lateinit var visibilityTextView: TextView
    private lateinit var ttsTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        activity?.window?.navigationBarColor =
            ContextCompat.getColor(requireContext(), R.color.background_white)
        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
        actionBar?.setBackgroundDrawable(
            ColorDrawable(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.background_white
                )
            )
        ) // Action Bar Color

        // Change default app colours
        activity?.window?.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.background_white) // Status Bar Color
        actionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setTitle("")

        // Assigning the cards
        val tempCard: CardView = view.findViewById(R.id.tempCard)
        val windCard: CardView = view.findViewById(R.id.windCard)
        val rainCard: CardView = view.findViewById(R.id.rainfallCard)
        val ttsCard: CardView = view.findViewById(R.id.ttsCard)
        val visibilityCard: CardView = view.findViewById(R.id.visibilityCard)
        val severeWeatherWarningCheckBox: CheckBox = view.findViewById(R.id.severe_weather_warnings)

        // Assigning the views
        temperatureUnitTextView = view.findViewById(R.id.temp_details)
        windspeedUnitTextView = view.findViewById(R.id.wind_details)
        rainfallUnitTextView = view.findViewById(R.id.rain_details)
        visibilityTextView = view.findViewById(R.id.visibility_details)
        ttsTextView = view.findViewById(R.id.tts_details)

        /*
            Set on click listeners in the cards and update their texts, building alert dialogues to set user options
         */
        tempCard.setOnClickListener {
            val temperatureUnits = arrayOf("Celsius", "Fahrenheit")

            val currentUnit = settingsManager.getTemperatureUnit()
            val selectedIndex =
                if (currentUnit == "Celsius") 0 else 1

            val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme)

            builder.setTitle("Temperature Unit")
                .setSingleChoiceItems(temperatureUnits, selectedIndex) { dialog, which ->
                    when (which) {
                        0 -> {
                            settingsManager.setPreferredUnit("temperature_unit", "Celsius")
                        }

                        1 -> {
                            settingsManager.setPreferredUnit("temperature_unit", "Fahrenheit")
                        }
                    }
                    updateTemperatureUnitTextView()
                    viewModel.fetchWeatherData()

                    // Notify MainActivity to update calendar events
                    (activity as? MainActivity)?.updateCalendarEvents()

                    dialog.dismiss()
                }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        windCard.setOnClickListener {
            val windSpeedUnits = arrayOf("km/h", "mph", "m/s", "knots")
            val currentUnit = settingsManager.getWindSpeedUnit()

            val selectedIndex = when (currentUnit) {
                "km/h" -> 0
                "mph" -> 1
                "m/s" -> 2
                "knots" -> 3
                else -> 0
            }

            val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme)

            builder.setTitle("Wind Speed Unit")
                .setSingleChoiceItems(windSpeedUnits, selectedIndex) { dialog, which ->
                    // Handle selection
                    when (which) {
                        0 -> {
                            settingsManager.setPreferredUnit("wind_speed_unit", "km/h")
                        }

                        1 -> {
                            settingsManager.setPreferredUnit("wind_speed_unit", "mph")
                        }

                        2 -> {
                            settingsManager.setPreferredUnit("wind_speed_unit", "m/s")
                        }

                        3 -> {
                            settingsManager.setPreferredUnit("wind_speed_unit", "knots")
                        }
                    }
                    updateWindSpeedUnitTextView()
                    viewModel.fetchWeatherData()
                    dialog.dismiss()
                }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        rainCard.setOnClickListener {
            val rainfallUnits = arrayOf("mm", "in.")

            val currentUnit = settingsManager.getPrecipitationUnit()
            val selectedIndex =
                if (currentUnit == "mm") 0 else 1

            val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme)

            builder.setTitle("Precipitation Unit")
                .setSingleChoiceItems(rainfallUnits, selectedIndex) { dialog, which ->
                    when (which) {
                        0 -> {
                            settingsManager.setPreferredUnit("rainfall_unit", "mm")
                        }

                        1 -> {
                            settingsManager.setPreferredUnit("rainfall_unit", "in.")
                        }
                    }
                    updateRainfallUnitTextView()
                    viewModel.fetchWeatherData()
                    dialog.dismiss()
                }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        ttsCard.setOnClickListener {
            val ttsStatus = arrayOf("Enable", "Disable")

            val currentStatus = settingsManager.getTtsStatus()
            val selectedIndex =
                if (currentStatus == "enabled") 0 else 1

            val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme)

            builder.setTitle("Enable Text To Speech")
                .setSingleChoiceItems(ttsStatus, selectedIndex) { dialog, which ->
                    when (which) {
                        0 -> {
                            settingsManager.setPreferredUnit("tts", "enabled")
                        }

                        1 -> {
                            settingsManager.setPreferredUnit("tts", "disabled")
                        }
                    }
                    updateTtsUnitTextView()
                    dialog.dismiss()
                }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        visibilityCard.setOnClickListener {
            val visibilityUnits = arrayOf("km", "mi.")

            val currentUnit = settingsManager.getVisibilityUnit()
            val selectedIndex =
                if (currentUnit == "km") 0 else 1

            val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme)

            builder.setTitle("Visibility Unit")
                .setSingleChoiceItems(visibilityUnits, selectedIndex) { dialog, which ->
                    when (which) {
                        0 -> {
                            // User selected Celsius
                            settingsManager.setPreferredUnit("visibility_unit", "km")
                        }

                        1 -> {
                            // User selected Fahrenheit
                            settingsManager.setPreferredUnit("visibility_unit", "mi.")
                        }
                    }
                    updateVisibilityUnitTextView()
                    viewModel.fetchWeatherData()
                    dialog.dismiss()
                }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        // Determining the checked state of the weather warnings notification preference (off by default)
        severeWeatherWarningCheckBox.isChecked =
            settingsManager.checkNotification(SEVERE_NOTIFICATION_PREFERENCE_KEY, false)

        // Setting up the listener for the severe weather warnings checkbox
        severeWeatherWarningCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (checkNotificationPermission()) {
                    settingsManager.saveNotificationPreference(true)
                    WeatherService.startWeatherMonitoring(requireContext())
                } else {
                    // Uncheck the box if permission is not granted
                    severeWeatherWarningCheckBox.isChecked = false
                    Toast.makeText(
                        context,
                        "Notification permission is required for severe weather alerts",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                settingsManager.saveNotificationPreference(false)
                WeatherService.stopWeatherMonitoring(requireContext())
            }
        }

        // Initializing the TextView of Temperature Details with the current unit
        updateTemperatureUnitTextView()
        // Initializing the TextView of Wind Speed Details with the current unit
        updateWindSpeedUnitTextView()
        // Initializing the TextView of Rainfall Details with the current unit
        updateRainfallUnitTextView()
        // Initializing the TextView of TTS Details with the current status
        updateTtsUnitTextView()
        // Initializing the TextView of Visibility Details with the current unit
        updateVisibilityUnitTextView()

        return view
    }

    // Retrieving the stored preference for temperature metric unit of the user
    private fun updateTemperatureUnitTextView() {
        // This sets the TextView for the Temperature Details to what it equates and displays it onto the settings page
        val unit = settingsManager.getTemperatureSymbol()
        temperatureUnitTextView.text = unit
    }

    // Retrieving the stored preference for wind speed metric unit of the user
    private fun updateWindSpeedUnitTextView() {
        val unit = settingsManager.getWindSpeedUnit()
        windspeedUnitTextView.text = unit
    }

    // Retrieving the stored preference for rainfall metric unit of the user
    private fun updateRainfallUnitTextView() {
        val unit = settingsManager.getPrecipitationUnit()
        rainfallUnitTextView.text = unit
    }

    // Retrieving the stored preference for rainfall metric unit of the user
    private fun updateTtsUnitTextView() {
        val status = settingsManager.getTtsString()
        ttsTextView.text = status
    }

    private fun updateVisibilityUnitTextView() {
        // This sets the TextView for the Temperature Details to what it equates and displays it onto the settings page
        val unit = settingsManager.getVisibilityUnit()
        visibilityTextView.text = unit
    }

    // Checking if the notification permission has been granted
    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    // Change colors back when destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        activity?.window?.navigationBarColor =
            ContextCompat.getColor(requireContext(), R.color.gradient_end)
        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
        actionBar?.setBackgroundDrawable(
            ColorDrawable(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.gradient_start
                )
            )
        )
        activity?.window?.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.gradient_start) // Status Bar Color
    }
}