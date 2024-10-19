package com.skysphere.skysphere.API

import android.content.Context
import androidx.room.Room
import com.skysphere.skysphere.WeatherViewModel
import com.skysphere.skysphere.data.WeatherDatabase
import com.skysphere.skysphere.data.WeatherRepository
import com.skysphere.skysphere.data.dao.CurrentWeatherDao
import com.skysphere.skysphere.data.dao.DailyWeatherDao
import com.skysphere.skysphere.data.dao.HourlyWeatherDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

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
}
