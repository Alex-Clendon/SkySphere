package com.skysphere.skysphere.ui.recommendations

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.android.material.textfield.TextInputLayout
import com.skysphere.skysphere.API.WeatherType
import com.skysphere.skysphere.R
import com.skysphere.skysphere.view_models.WeatherViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class WeatherRecommendationsFragment : Fragment(R.layout.fragment_weather_recommendations) {

    // UI components
    private lateinit var currentWeatherTextView: TextView
    private lateinit var activityRecommendationsTextView: TextView
    private lateinit var clothingRecommendationsTextView: TextView
    private lateinit var lastUpdatedTextView: TextView
    private lateinit var updateButton: Button
    private lateinit var toggleIndoor: ToggleButton
    private lateinit var toggleOutdoor: ToggleButton
    private lateinit var questionInput: TextInputLayout
    private lateinit var askQuestionButton: Button
    private lateinit var answerTextView: TextView

    // User preference for activities (indoor, outdoor, or both)
    private var preference: String = "both"

    // Initialize AI model for generating recommendations
    private lateinit var generativeModel: GenerativeModel

    // Inject ViewModel
    @Inject
    lateinit var weatherViewModel: WeatherViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateColours()

        // Initialize views
        currentWeatherTextView = view.findViewById(R.id.tvCurrentWeather)
        activityRecommendationsTextView = view.findViewById(R.id.tvActivityRecommendations)
        clothingRecommendationsTextView = view.findViewById(R.id.tvClothingRecommendations)
        lastUpdatedTextView = view.findViewById(R.id.tvLastUpdated)
        updateButton = view.findViewById(R.id.btnUpdateRecommendations)
        toggleIndoor = view.findViewById(R.id.toggleIndoor)
        toggleOutdoor = view.findViewById(R.id.toggleOutdoor)
        questionInput = view.findViewById(R.id.questionInput)
        askQuestionButton = view.findViewById(R.id.askQuestionButton)
        answerTextView = view.findViewById(R.id.answerTextView)

        // Initialize model with API key from resources
        val apiKey = getString(R.string.gemini_api_key)
        generativeModel = GenerativeModel(modelName = "gemini-pro", apiKey = apiKey)

        // Set up listeners
        updateButton.setOnClickListener { updateRecommendations() }
        askQuestionButton.setOnClickListener { askWeatherQuestion() }

        // Set up toggle buttons for preference
        setupToggleButtons()

        // Observe ViewModel weather data
        observeWeatherData()

        // Load saved recommendations
        loadSavedRecommendations()
    }

    // Observe live data from view model
    private fun observeWeatherData() {
        weatherViewModel.weatherResults.observe(viewLifecycleOwner) { weatherResults ->
            weatherResults?.let {
                // Update UI with the latest weather data from the ViewModel
                currentWeatherTextView.text = it.current?.weatherText
                // You can use the weatherResults object to update other UI elements as needed
            }
        }
    }

    // Function to update the UI colours
    private fun updateColours() {
        activity?.window?.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.gradient_start)
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
    }

    private fun setupToggleButtons() {
        toggleIndoor.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                toggleOutdoor.isChecked = false
                preference = "indoor"
            } else if (!toggleOutdoor.isChecked) {
                preference = "both"
            }
        }

        toggleOutdoor.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                toggleIndoor.isChecked = false
                preference = "outdoor"
            } else if (!toggleIndoor.isChecked) {
                preference = "both"
            }
        }
    }

    private fun loadSavedRecommendations() {
        val sharedPrefs = requireContext().getSharedPreferences("recommendations", Context.MODE_PRIVATE)
        currentWeatherTextView.text = sharedPrefs.getString("current_weather", "")
        activityRecommendationsTextView.text = sharedPrefs.getString("activities", "")
        clothingRecommendationsTextView.text = sharedPrefs.getString("clothing", "")
        lastUpdatedTextView.text = sharedPrefs.getString("last_updated", "")
    }

    private fun updateRecommendations() {
        // Observe weather data from the ViewModel
        weatherViewModel.weatherResults.observe(viewLifecycleOwner) { weatherResults ->
            if (weatherResults != null) {
                val temperature = weatherResults.current?.roundedTemperature
                val weatherText = weatherResults.current?.weatherText
                val currentTime = weatherResults.current?.time

                updateButton.isEnabled = false

                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val recommendations = withContext(Dispatchers.IO) {
                            getAIRecommendations(temperature, weatherText, currentTime, preference)
                        }
                        updateUI(recommendations)
                    } catch (e: Exception) {
                        updateUIWithError(e.message ?: "An error occurred")
                    } finally {
                        updateButton.isEnabled = true
                    }
                }
            }
        }
    }


    private suspend fun getAIRecommendations(temperature: Int?, weatherText: String?, currentTime: String?, preference: String): Recommendations {
        val prompt = """
        Given the following weather conditions:
        - Temperature: $temperature°C
        - Weather: ${weatherText}
        - Current time: $currentTime
        - User preference: $preference (indoor, outdoor, or both)

       Please provide:
        1. A brief description of the current weather, incorporating the provided weather description.
        2. Three unique activity recommendations suitable for these conditions(temperature, weather, current time) and the user's preference.
        3. Three clothing recommendations appropriate for this weather and the recommended activities.
        Format your response as follows:
        
        Current Weather: [Brief description incorporating ${weatherText}]
        
        Activity Recommendations:
        1. [Activity 1]
        2. [Activity 2]
        3. [Activity 3]
        
        Clothing Recommendations:
        1. [Clothing item 1]
        2. [Clothing item 2]
        3. [Clothing item 3]
        
    """.trimIndent()

        val response: GenerateContentResponse = generativeModel.generateContent(prompt)
        val content = response.text ?: throw Exception("Unable to generate recommendations")

        val currentWeather = content.substringAfter("Current Weather:").substringBefore("Activity Recommendations:").trim()
        val activities = content.substringAfter("Activity Recommendations:").substringBefore("Clothing Recommendations:").trim()
        val clothing = content.substringAfter("Clothing Recommendations:").trim()

        return Recommendations(currentWeather, activities, clothing)
    }

    private fun updateUI(recommendations: Recommendations) {
        currentWeatherTextView.text = recommendations.currentWeather
        activityRecommendationsTextView.text = recommendations.activities
        clothingRecommendationsTextView.text = recommendations.clothing

        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        val lastUpdatedStr = "Last updated: ${dateFormat.format(Date())}"
        lastUpdatedTextView.text = lastUpdatedStr

        saveRecommendations(recommendations, lastUpdatedStr)
    }

    private fun updateUIWithError(errorMessage: String) {
        currentWeatherTextView.text = "Error: $errorMessage"
        activityRecommendationsTextView.text = ""
        clothingRecommendationsTextView.text = ""
    }

    private fun saveRecommendations(recommendations: Recommendations, lastUpdated: String) {
        val sharedPrefs = requireContext().getSharedPreferences("recommendations", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putString("current_weather", recommendations.currentWeather)
            putString("activities", recommendations.activities)
            putString("clothing", recommendations.clothing)
            putString("last_updated", lastUpdated)
            apply()
        }
    }

    private fun askWeatherQuestion() {
        val question = questionInput.editText?.text.toString()
        if (question.isNotEmpty()) {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    askQuestionButton.isEnabled = false
                    val answer = withContext(Dispatchers.IO) {
                        getAIAnswer(question)
                    }
                    updateAnswerUI(answer)
                } catch (e: Exception) {
                    updateAnswerUI("Sorry, I couldn't generate an answer.")
                } finally {
                    askQuestionButton.isEnabled = true
                }
            }
        }
    }

    private suspend fun getAIAnswer(question: String): String {
        val weatherResults = weatherViewModel.getData()
        val temperature = weatherResults?.current?.roundedTemperature
        val weatherText = weatherResults?.current?.weatherText
        val currentTime = weatherResults?.current?.time

        val prompt = """
        Given the following weather conditions and current time:
        - Temperature: $temperature°C
        - Weather: ${weatherText}
        - Current time: $currentTime

        Please answer the following weather-related question:
        $question
        
        Provide a concise and helpful answer, including time as a factor, Do not answer questions that do not relate to weather
        
        """.trimIndent()

        val response: GenerateContentResponse = generativeModel.generateContent(prompt)
        return response.text ?: "I'm sorry, I couldn't generate an answer."
    }

    private fun updateAnswerUI(answer: String) {
        answerTextView.text = answer
    }
}
