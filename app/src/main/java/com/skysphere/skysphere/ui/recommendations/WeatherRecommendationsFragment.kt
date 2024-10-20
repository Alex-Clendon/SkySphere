package com.skysphere.skysphere.ui.recommendations

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.ToggleButton
import androidx.fragment.app.Fragment
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.skysphere.skysphere.API.WeatherType
import com.skysphere.skysphere.R
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout

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
    private val generativeModel = GenerativeModel(modelName = "gemini-pro", apiKey = "AIzaSyAJLOJbXHdCNS72X78uPi_If92bCAsAhgQ")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        // Set up listeners
        updateButton.setOnClickListener { updateRecommendations() }
        askQuestionButton.setOnClickListener { askWeatherQuestion() }

        // Set up toggle buttons for preference
        setupToggleButtons()

        // Load saved recommendations
        loadSavedRecommendations()
    }

    // Setting up the indoor and outdoor preference buttons
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

    // Function to load the saved recommendations from earlier session using sharedprefs, to save current recommendations and prevent automatically updating recommendations each time
    private fun loadSavedRecommendations() {
        val sharedPrefs = requireContext().getSharedPreferences("recommendations", Context.MODE_PRIVATE)
        currentWeatherTextView.text = sharedPrefs.getString("current_weather", "")
        activityRecommendationsTextView.text = sharedPrefs.getString("activities", "")
        clothingRecommendationsTextView.text = sharedPrefs.getString("clothing", "")
        lastUpdatedTextView.text = sharedPrefs.getString("last_updated", "")
    }

    // Function to update recommendations
    private fun updateRecommendations() {
        // Getting weather values from the main fragment via SharedPreferences
        val sharedPrefs = requireContext().getSharedPreferences("weather_data", Context.MODE_PRIVATE)
        val temperature = sharedPrefs.getFloat("temperature_celsius", 0f)
        val weatherCode = sharedPrefs.getInt("weather_code", 0)
        val currentTime = sharedPrefs.getString("current_time", null)

        // Disable update button after clicking to prevent multiple requests
        updateButton.isEnabled = false

        // Launch coroutine to perform AI request in the background
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val recommendations = withContext(Dispatchers.IO) {
                    // Get AI recommendations on IO dispatcher
                    getAIRecommendations(temperature, weatherCode, currentTime, preference)
                }
                // Update UI with new recommendations
                updateUI(recommendations)
            } catch (e: Exception) {
                updateUIWithError(e.message ?: "An error occurred")
            } finally {
                // Re-enable update button after process
                updateButton.isEnabled = true
            }
        }
    }

    // Function to get AI-generated recommendations based on weather conditions and user preferences
    private suspend fun getAIRecommendations(temperature: Float, weatherCode: Int, currentTime: String?, preference: String): Recommendations {
        // Get the weather type description from the weather code - thanks stephen
        val weatherType = WeatherType.fromWMO(weatherCode)

        // Making prompt for AI model
        val prompt = """
        Given the following weather conditions:
        - Temperature: $temperature°C
        - Weather: ${weatherType.weatherDesc}
        - Current time: $currentTime
        - User preference: $preference (indoor, outdoor, or both)

        Please provide:
        1. A brief description of the current weather, incorporating the provided weather description.
        2. Three unique activity recommendations suitable for these conditions(temperature, weather, current time) and the user's preference.
        3. Three clothing recommendations appropriate for this weather and the recommended activities.

        Format your response as follows:

        Current Weather: [Brief description incorporating ${weatherType.weatherDesc}]

        Activity Recommendations:
        1. [Activity 1]
        2. [Activity 2]
        3. [Activity 3]

        Clothing Recommendations:
        1. [Clothing item 1]
        2. [Clothing item 2]
        3. [Clothing item 3]
    """.trimIndent()

        // Generate content using the GeminiAI model
        val response: GenerateContentResponse = generativeModel.generateContent(prompt)
        val content = response.text ?: throw Exception("Unable to generate recommendations")

        // Parse AI response into separate sections
        val currentWeather = content.substringAfter("Current Weather:").substringBefore("Activity Recommendations:").trim()
        val activities = content.substringAfter("Activity Recommendations:").substringBefore("Clothing Recommendations:").trim()
        val clothing = content.substringAfter("Clothing Recommendations:").trim()

        // Return Recommendations object with parsed data
        return Recommendations(currentWeather, activities, clothing)
    }

    // Function to update the UI with new recommendations
    private fun updateUI(recommendations: Recommendations) {
        currentWeatherTextView.text = recommendations.currentWeather
        activityRecommendationsTextView.text = recommendations.activities
        clothingRecommendationsTextView.text = recommendations.clothing

        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        val lastUpdatedStr = "Last updated: ${dateFormat.format(Date())}"
        lastUpdatedTextView.text = lastUpdatedStr

        // Save new recommendations
        saveRecommendations(recommendations, lastUpdatedStr)
    }

    // Function to update the UI in case of error
    private fun updateUIWithError(errorMessage: String) {
        currentWeatherTextView.text = "Error: $errorMessage"
        activityRecommendationsTextView.text = ""
        clothingRecommendationsTextView.text = ""
    }

    // Function to save recommendations to SharedPreferences
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

    // Function to handle weather questions
    private fun askWeatherQuestion() {
        val question = questionInput.editText?.text.toString()
        if (question.isNotEmpty()) {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    // Disable button to prevent multiple requests
                    askQuestionButton.isEnabled = false
                    // Get AI answer in background thread
                    val answer = withContext(Dispatchers.IO) {
                        getAIAnswer(question)
                    }
                    // Update UI with the answer
                    updateAnswerUI(answer)
                } catch (e: Exception) {
                    updateAnswerUI("Sorry, I couldn't generate an answer. Please try again.")
                } finally {
                    // Re-enable button
                    askQuestionButton.isEnabled = true
                }
            }
        }
    }

    // Function to get AI-generated answer for user's question
    private suspend fun getAIAnswer(question: String): String {
        // Getting weather values from the main fragment via SharedPreferences
        val sharedPrefs = requireContext().getSharedPreferences("weather_data", Context.MODE_PRIVATE)
        val temperature = sharedPrefs.getFloat("temperature_celsius", 0f)
        val weatherCode = sharedPrefs.getInt("weather_code", 0)
        val currentTime = sharedPrefs.getString("current_time", null)
        val weatherType = WeatherType.fromWMO(weatherCode)

        // Making prompt for AI model
        val prompt = """
        Given the following weather conditions and time of day:
        - Temperature: $temperature°C
        - Weather: ${weatherType.weatherDesc}
        - Current time: $currentTime


        Please answer the following question about the weather:
        $question
        
        Provide a concise and helpful answer, including time as a factor, Do not answer questions that do not relate to weather
        """.trimIndent()

        // Generate content using the GeminiAI model
        val response: GenerateContentResponse = generativeModel.generateContent(prompt)
        return response.text ?: "I'm sorry, I couldn't generate an answer."
    }

    // Function to update UI with AI's answer
    private fun updateAnswerUI(answer: String) {
        answerTextView.text = answer
    }


}