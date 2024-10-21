package com.skysphere.skysphere.data.entities.locations

import androidx.room.Entity
import androidx.room.PrimaryKey

/*
   Entity class that holds data to be stored in the local database according to a key
 */
@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // ID will auto-generate
    val area: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
)
