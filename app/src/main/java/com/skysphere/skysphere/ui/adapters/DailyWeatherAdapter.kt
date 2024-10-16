package com.skysphere.skysphere.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.skysphere.skysphere.API.WeatherType
import com.skysphere.skysphere.R
import com.skysphere.skysphere.data.weather.WeatherDaily

class DailyWeatherAdapter(private val dailyWeather: WeatherDaily) :
    RecyclerView.Adapter<DailyWeatherAdapter.DailyWeatherViewHolder>() {

    // ViewHolder to hold the views for each row
    inner class DailyWeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayTextView: TextView = itemView.findViewById(R.id.textViewDay)
        val weatherImageView: ImageView = itemView.findViewById(R.id.imageViewWeather)
        val tempMaxTextView: TextView = itemView.findViewById(R.id.textViewTemp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyWeatherViewHolder {
        // Inflate the layout for each row
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.daily_weather_item, parent, false)
        return DailyWeatherViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DailyWeatherViewHolder, position: Int) {
        // Bind the data to the views
        val day = dailyWeather.day[position]
        val weatherCode = dailyWeather.weatherCode[position]
        val tempMax = dailyWeather.temperatureMax[position]?.toInt()
        val tempMin = dailyWeather.temperatureMin[position]?.toInt()

        val weatherImageRes = WeatherType.fromWMO(weatherCode ?: 0).iconRes

        holder.dayTextView.text = day ?: ""
        holder.weatherImageView.setImageResource(weatherImageRes)
        holder.tempMaxTextView.text = tempMax?.toString() + "° " + tempMin.toString() + "°"
    }

    override fun getItemCount(): Int {
        return dailyWeather.time.size // Number of items in the daily weather data
    }
}
