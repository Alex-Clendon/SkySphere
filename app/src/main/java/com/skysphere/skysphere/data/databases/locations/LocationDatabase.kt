package com.skysphere.skysphere.data.databases.locations

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.skysphere.skysphere.data.dao.locations.LocationDao
import com.skysphere.skysphere.data.entities.locations.LocationEntity

@Database(entities = [LocationEntity::class], version = 1)
abstract class LocationDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
}
