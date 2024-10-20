package com.skysphere.skysphere.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.skysphere.skysphere.API.WeatherType
import com.skysphere.skysphere.R
import com.skysphere.skysphere.data.weather.WeatherDaily

/*
    Adapter class to intialize daily recycler view
 */
class DailyWeatherAdapter
    (
    private var dailyWeather: WeatherDaily?,
    private val onItemClicked: (Int) -> Unit
) :
    RecyclerView.Adapter<DailyWeatherAdapter.DailyWeatherViewHolder>() {

    // ViewHolder to hold the views for each row
    inner class DailyWeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayTextView: TextView = itemView.findViewById(R.id.textViewDay)
        val weatherImageView: LottieAnimationView = itemView.findViewById(R.id.imageViewWeather)
        val nightImageView: LottieAnimationView = itemView.findViewById(R.id.imageView2)
        val tempMaxTextView: TextView = itemView.findViewById(R.id.textViewTemp)
        val precipitationTextView: TextView = itemView.findViewById(R.id.textViewPrecipitation)

        init {
            itemView.setOnClickListener {
                onItemClicked(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyWeatherViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.daily_weather_item, parent, false)
        return DailyWeatherViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DailyWeatherViewHolder, position: Int) {

        val day = dailyWeather?.day?.get(position)
        val weatherType = dailyWeather?.weatherType?.get(position)
        val tempMax = dailyWeather?.roundedTemperatureMax?.get(position)
        val tempMin = dailyWeather?.roundedTemperatureMin?.get(position)
        val precipitation = dailyWeather?.precipitationProbability?.get(position)

        if (position == 0) {
            holder.dayTextView.text = "Today"
        } else {
            holder.dayTextView.text = day ?: ""
        }
        holder.precipitationTextView.text = precipitation.toString() + "%"
        if (weatherType != null) {
            holder.weatherImageView.setAnimation(weatherType.lottieAnimRes)
            holder.weatherImageView.playAnimation()
        }
        holder.nightImageView.setAnimation((R.raw.partly_cloudy_night))
        holder.nightImageView.playAnimation()
        holder.tempMaxTextView.text = tempMax?.toString() + "° " + tempMin.toString() + "°"
    }

    override fun getItemCount(): Int {
        return dailyWeather!!.time.size
    }
}
