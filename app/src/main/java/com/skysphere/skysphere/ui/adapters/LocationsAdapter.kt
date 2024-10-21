package com.skysphere.skysphere.ui.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.skysphere.skysphere.API.Article
import com.skysphere.skysphere.API.WeatherType
import com.skysphere.skysphere.R
import com.skysphere.skysphere.data.weather.WeatherDaily

/*
    Adapter class to intialize locations recycler view
 */
class LocationsAdapter: RecyclerView.Adapter<LocationsAdapter.LocationsViewHolder>() {

    private var locationsList: List<String> = emptyList()

    inner class LocationsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val areaTextView: TextView = itemView.findViewById(R.id.tvArea)
        private val countryTextView: TextView = itemView.findViewById(R.id.tvCountry)

        // Bind article data to the view
        fun bind(location: String) {
            areaTextView.text = location
            countryTextView.text = location
        }

    }

    // ViewHolder to hold the views for each row
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationsViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.location_item, parent, false)
        return LocationsViewHolder(itemView)
    }

    override fun getItemCount() = locationsList.size

    override fun onBindViewHolder(holder: LocationsViewHolder, position: Int) {
        holder.bind(locationsList[position])
    }
}
