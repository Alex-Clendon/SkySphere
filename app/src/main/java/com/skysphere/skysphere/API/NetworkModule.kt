package com.skysphere.skysphere.API

import android.content.Context
import androidx.room.Room
import com.skysphere.skysphere.data.dao.locations.LocationDao
import com.skysphere.skysphere.data.dao.weather.CurrentWeatherDao
import com.skysphere.skysphere.data.dao.weather.DailyWeatherDao
import com.skysphere.skysphere.data.dao.weather.HourlyWeatherDao
import com.skysphere.skysphere.data.databases.locations.LocationDatabase
import com.skysphere.skysphere.data.databases.weather.WeatherDatabase
import com.skysphere.skysphere.data.repositories.LocationRepository
import com.skysphere.skysphere.data.repositories.WeatherRepository
import com.skysphere.skysphere.view_models.LocationViewModel
import com.skysphere.skysphere.view_models.WeatherViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/*
      Module class for Hilt Injection, providing instances of the necessary components
 */

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideWeatherAPI(): WeatherAPI {
        return RetrofitInstance.getInstance(true)
    }

    @Provides
    @Singleton
    fun provideWeatherViewModel(
        weatherRepository: WeatherRepository
    ): WeatherViewModel {
        return WeatherViewModel(weatherRepository)
    }

    @Provides
    @Singleton
    fun provideLocationViewModel(
        locationRepository: LocationRepository
    ): LocationViewModel {
        return LocationViewModel(locationRepository)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): WeatherDatabase {
        return Room.databaseBuilder(
            appContext,
            WeatherDatabase::class.java,
            "weather_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideCurrentWeatherDao(db: WeatherDatabase): CurrentWeatherDao {
        return db.currentWeatherDao()
    }

    @Provides
    @Singleton
    fun provideHourlyWeatherDao(db: WeatherDatabase): HourlyWeatherDao {
        return db.hourlyWeatherDao()
    }

    @Provides
    @Singleton
    fun provideDailyWeatherDao(db: WeatherDatabase): DailyWeatherDao {
        return db.dailyWeatherDao()
    }

    @Provides
    @Singleton
    fun provideLocationDatabase(@ApplicationContext appContext: Context): LocationDatabase {
        return Room.databaseBuilder(
            appContext,
            LocationDatabase::class.java,
            "location_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideLocationDao(db: LocationDatabase): LocationDao {
        return db.locationDao()
    }
}
