package com.skysphere.skysphere.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.skysphere.skysphere.R
import com.skysphere.skysphere.data.entities.locations.LocationEntity

/*
    Adapter class to intialize locations recycler view
 */
class LocationsAdapter(
    private var locationsList: List<LocationEntity> = emptyList(),
    private val onItemClicked: (LocationEntity) -> Unit
) : RecyclerView.Adapter<LocationsAdapter.LocationsViewHolder>() {


    inner class LocationsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val areaTextView: TextView = itemView.findViewById(R.id.tvArea)
        private val countryTextView: TextView = itemView.findViewById(R.id.tvCountry)
        private val currentTextView: TextView = itemView.findViewById(R.id.tvCurrentLocation)

        // Bind location data to the view
        fun bind(location: LocationEntity) {
            if(position == 0)
            {
                currentTextView.visibility = View.VISIBLE
            }
            areaTextView.text = location.area

            countryTextView.text = location.country

            itemView.setOnClickListener {
                onItemClicked(location)
            }
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

    fun updateLocations(locations: List<LocationEntity>) {
        this.locationsList = locations
        notifyDataSetChanged() // Notify the adapter to refresh the view
    }

    fun getLocationAt(position: Int): LocationEntity {
        return locationsList[position]
    }
}
