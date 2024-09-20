package com.skysphere.skysphere.ui.recommendations

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.skysphere.skysphere.R
import com.skysphere.skysphere.API.WeatherType
import java.text.SimpleDateFormat
import java.util.*

class WeatherRecommendationsFragment : Fragment() {

    private lateinit var recommendationsTextView: TextView
    private lateinit var lastUpdatedTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_weather_recommendations, container, false)

        //Initialize TextViews for displaying recommendations and last updated.
        recommendationsTextView = view.findViewById(R.id.tvRecommendations)
        lastUpdatedTextView = view.findViewById(R.id.tvLastUpdated)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateRecommendations()
    }

    // Retrieve weather data from SharedPreferences and update UI with recommendations
    private fun updateRecommendations() {
        val sharedPrefs = requireContext().getSharedPreferences("weather_data", Context.MODE_PRIVATE)
        val temperatureCelsius = sharedPrefs.getFloat("temperature_celsius", 0f)
        val weatherCode = sharedPrefs.getInt("weather_code", 0)
        val lastUpdated = sharedPrefs.getLong("last_updated", 0)

        val isDay = isDaytime()
        val recommendations = getRecommendations(temperatureCelsius.toDouble(), weatherCode, isDay)
        recommendationsTextView.text = recommendations

        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        val lastUpdatedStr = if (lastUpdated != 0L) {
            "Last updated: ${dateFormat.format(Date(lastUpdated))}"
        } else {
            "Last updated: Unknown"
        }
        lastUpdatedTextView.text = lastUpdatedStr
    }

    // Check if it's currently daytime (between 6 AM and 6 PM)
    private fun isDaytime(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour in 6..17
    }

    // Gathering weather recommendations by giving the temp, weather code and isday method using the
    // getActivities,and getClothingRecommendation methods.
    private fun getRecommendations(temperature: Double, weatherCode: Int, isDay: Boolean): String {
        val weatherType = WeatherType.fromWMO(weatherCode)
        val activityRecommendation = getActivities(weatherCode, isDay)
        val clothingRecommendation = getClothingRecommendations(temperature, weatherCode)

        return "Current Weather: ${weatherType.weatherDesc}\n\n" +
                "Activities to do:\n$activityRecommendation\n\n" +
                "Clothing Recommendations:\n$clothingRecommendation"
    }

    // Suggest activities based on weather code and time of day
    private fun getActivities(weatherCode: Int, isDay: Boolean): String {
        return when {

            // Activities for clear weather.
            weatherCode in 0..3 -> if (isDay) {
                "1. Go for a walk or jog\n" +
                        "2. Have a picnic in the park\n" +
                        "3. Play outdoor sports\n" +
                        "4. Visit an outdoor café"
            } else {
                "1. Stargaze\n" +
                        "2. Take a moonlit walk\n" +
                        "3. Visit a rooftop bar\n" +
                        "4. Have a late-night picnic"
            }
            // Activities for foggy conditions
            weatherCode in 45..48 -> {
                "1. Visit a museum\n" +
                        "2. Enjoy a hot drink at a cafe\n3" +
                        ". Take atmospheric photographs\n" +
                        "4. Explore a local art gallery"
            }
            // Activities for different intensities of rain
            weatherCode in 51..67 -> {
                "1. Watch a movie\n" +
                        "2. Read a book\n" +
                        "3. Visit a shopping mall\n" +
                        "4. Take a cooking class"
            }
            // Activities for different intensities of snow
            weatherCode in 71..77 -> if (isDay) {
                "1. Build a snowman\n" +
                        "2. Go sledding\n" +
                        "3. Have a snowball fight\n" +
                        "4. Try cross-country skiing"
            } else {
                "1. Have a cozy night in\n" +
                        "2. Try night skiing\n" +
                        "3. Visit a winter night festival\n" +
                        "4. Go ice skating"
            }
            // Activities for thunderstorms or extreme conditions
            else -> {
                "1. Indoor workout\n" +
                        "2. Start a home project\n" +
                        "3. Have a game night\n" +
                        "4. Practice meditation or yoga"
            }
        }
    }

    // Provide clothing recommendations based on temperature and weather code
    private fun getClothingRecommendations(temperature: Double, weatherCode: Int): String {
        val initialRecommendation = when {

            // Clothing for hot temps
            temperature > 25 -> {
                "1. Light, breathable clothing\n" +
                    "2. Sun hat or cap\n" +
                    "3. Sunglasses\n" +
                    "4. Sunscreen"
            }

            // Clothing for normal temps
            temperature in 15.0..25.0 -> {
                "1. Light jacket or long-sleeved shirt\n" +
                        "2. Comfortable pants or jeans\n" +
                        "3. Closed-toe shoes\n" +
                        "4. Light scarf or shawl"
            }

            // Clothing for cold temps
            else -> {
                "1. Warm coat or heavy jacket\n" +
                        "2. Layered clothing (thermals, sweater)\n" +
                        "3. Warm hat and gloves\n" +
                        "4. Thick socks and boots"
            }
        }

        val additionalRecommendation = when {

            // Clothing for rainy conditions
            weatherCode in 51..67 -> {
                "\n5. Waterproof jacket or raincoat\n" +
                        "6. Water-resistant shoes\n" +
                        "7. Umbrella"
            }
            // Clothing for snow conditions
            weatherCode in 71..77 -> {
                "\n5. Insulated, waterproof outerwear\n" +
                        "6. Waterproof snow boots\n" +
                        "7. Warm, waterproof gloves"
            }
            else -> ""
        }

        return initialRecommendation + additionalRecommendation
    }
}