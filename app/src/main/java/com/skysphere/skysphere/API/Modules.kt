package com.skysphere.skysphere.API

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.skysphere.skysphere.updaters.WeatherCache

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideWeatherAPI(): WeatherAPI {
        return RetrofitInstance.getInstance(true) // This uses your existing Retrofit instance
    }

    @Provides
    @Singleton
    fun provideWeatherCache(): WeatherCache {
        return WeatherCache() // Initialize your WeatherCache appropriately
    }
}
