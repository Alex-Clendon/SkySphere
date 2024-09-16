package com.skysphere.skysphere.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.skysphere.skysphere.R
import java.text.DecimalFormat

// Adapter class for the recyclerview to display hourly temperatures
class HourlyTemperatureAdapter(
    private val temperatures: List<Double>
) : RecyclerView.Adapter<HourlyTemperatureAdapter.HourlyViewHolder>() {

    // Format for temperature metric unit
    private val decimalFormat = DecimalFormat("#.#")

    // Generating times from 00:00 to 23:00
    private val times: List<String> = List(24) { index ->
        String.format("%02d:00", index)
    }

    // Called when recyclerview needs a new viewholder to display an item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyViewHolder {
        // Inflating the item layout for each entry of hourly temperature.
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hourly_temperature, parent, false)
        return HourlyViewHolder(view) // Returning a new viewholder instance.
    }

    // Called by recyclerview to display the data at the specified position
    override fun onBindViewHolder(holder: HourlyViewHolder, position: Int) {
        // Binding the time and temperature data to the corresponding textviews.
        holder.tvHour.text = times[position]
        val temperature = temperatures[position]
        holder.tvHourlyTemperature.text = decimalFormat.format(temperature) + "°"
    }

    // Returns the total number of items to be displayed in the recyclerview.
    override fun getItemCount(): Int {
        return temperatures.size
    }

    // viewholder class to hold references to each item's views.
    class HourlyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvHour: TextView = view.findViewById(R.id.tvHour)
        val tvHourlyTemperature: TextView = view.findViewById(R.id.tvHourlyTemperature)
    }
}