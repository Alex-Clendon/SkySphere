package com.skysphere.skysphere.API

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import com.skysphere.skysphere.R

sealed class WeatherType(
    val weatherDesc: String,
    @RawRes val lottieAnimRes: Int,
    @DrawableRes val iconRes: Int
) {

    object ClearSky : WeatherType(
        weatherDesc = "Clear Sky",
        iconRes = R.drawable.ic_sunny,
        lottieAnimRes = R.raw.clear_day,
    )

    object MainlyClear : WeatherType(
        weatherDesc = "Mainly Clear",
        iconRes = R.drawable.ic_sunny,
        lottieAnimRes = R.raw.clear_day
    )

    object PartlyCloudy : WeatherType(
        weatherDesc = "Partly Cloudy",
        iconRes = R.drawable.ic_sunnycloudy,
        lottieAnimRes = R.raw.partly_cloudy_day
    )

    object Overcast : WeatherType(
        weatherDesc = "Overcast",
        iconRes = R.drawable.ic_very_cloudy,
        lottieAnimRes = R.raw.overcast_day
    )

    object Foggy : WeatherType(
        weatherDesc = "Foggy",
        iconRes = R.drawable.ic_very_cloudy,
        lottieAnimRes = R.raw.fog
    )

    object DepositingRimeFog : WeatherType(
        weatherDesc = "Depositing Rime Fog",
        iconRes = R.drawable.ic_very_cloudy,
        lottieAnimRes = R.raw.fog
    )

    object LightDrizzle : WeatherType(
        weatherDesc = "Lightly Drizzling",
        iconRes = R.drawable.ic_rainshower,
        lottieAnimRes = R.raw.light_drizzle
    )

    object ModerateDrizzle : WeatherType(
        weatherDesc = "Drizzling",
        iconRes = R.drawable.ic_rainshower,
        lottieAnimRes = R.raw.drizzle
    )

    object DenseDrizzle : WeatherType(
        weatherDesc = "Densely Drizzling",
        iconRes = R.drawable.ic_rainy,
        lottieAnimRes = R.raw.drizzle
    )

    object LightFreezingDrizzle : WeatherType(
        weatherDesc = "Lightly Sleeting",
        iconRes = R.drawable.ic_snowyrainy,
        lottieAnimRes = R.raw.sleet
    )

    object DenseFreezingDrizzle : WeatherType(
        weatherDesc = "Sleeting",
        iconRes = R.drawable.ic_snowyrainy,
        lottieAnimRes = R.raw.sleet
    )

    object SlightRain : WeatherType(
        weatherDesc = "Slight Rain",
        iconRes = R.drawable.ic_rainy,
        lottieAnimRes = R.raw.partly_cloudy_day_rain
    )

    object ModerateRain : WeatherType(
        weatherDesc = "Rainy",
        iconRes = R.drawable.ic_rainy,
        lottieAnimRes = R.raw.rain
    )

    object HeavyRain : WeatherType(
        weatherDesc = "Heavy Rain",
        iconRes = R.drawable.ic_rainy,
        lottieAnimRes = R.raw.overcast_rain
    )

    object HeavyFreezingRain : WeatherType(
        weatherDesc = "Heavy Freezing Rain",
        iconRes = R.drawable.ic_snowyrainy,
        lottieAnimRes = R.raw.overcast_sleet
    )

    object SlightSnowFall : WeatherType(
        weatherDesc = "Slight Snowfall",
        iconRes = R.drawable.ic_snowy,
        lottieAnimRes = R.raw.snow
    )

    object ModerateSnowFall : WeatherType(
        weatherDesc = "Snow",
        iconRes = R.drawable.ic_heavysnow,
        lottieAnimRes = R.raw.snow
    )

    object HeavySnowFall : WeatherType(
        weatherDesc = "Heavy Snow",
        iconRes = R.drawable.ic_heavysnow,
        lottieAnimRes = R.raw.overcast_snow
    )

    object SnowGrains : WeatherType(
        weatherDesc = "Snow Grains",
        iconRes = R.drawable.ic_heavysnow,
        lottieAnimRes = R.raw.snow
    )

    object SlightRainShowers : WeatherType(
        weatherDesc = "Slight Rain Showers",
        iconRes = R.drawable.ic_rainshower,
        lottieAnimRes = R.raw.partly_cloudy_day_rain
    )

    object ModerateRainShowers : WeatherType(
        weatherDesc = "Moderate Rain Showers",
        iconRes = R.drawable.ic_rainshower,
        lottieAnimRes = R.raw.rain
    )

    object ViolentRainShowers : WeatherType(
        weatherDesc = "Violent Rain Showers",
        iconRes = R.drawable.ic_rainshower,
        lottieAnimRes = R.raw.overcast_rain
    )

    object SlightSnowShowers : WeatherType(
        weatherDesc = "Light Snow Showers",
        iconRes = R.drawable.ic_snowy,
        lottieAnimRes = R.raw.snow
    )

    object HeavySnowShowers : WeatherType(
        weatherDesc = "Heavy Snow Showers",
        iconRes = R.drawable.ic_snowy,
        lottieAnimRes = R.raw.overcast_snow
    )

    object ModerateThunderstorm : WeatherType(
        weatherDesc = "Moderate Thunderstorms",
        iconRes = R.drawable.ic_thunder,
        lottieAnimRes = R.raw.thunderstorms_rain
    )

    object SlightHailThunderstorm : WeatherType(
        weatherDesc = "Thunderstorms With Slight Hail",
        iconRes = R.drawable.ic_rainythunder,
        lottieAnimRes = R.raw.thunderstorms_overcast_rain
    )

    object HeavyHailThunderstorm : WeatherType(
        weatherDesc = "Thunderstorms With Heavy Hail",
        iconRes = R.drawable.ic_rainythunder,
        lottieAnimRes = R.raw.thunderstorms_day_extreme_rain
    )

    companion object {
        fun fromWMO(code: Int?): WeatherType {
            return when (code) {
                0 -> ClearSky
                1 -> MainlyClear
                2 -> PartlyCloudy
                3 -> Overcast
                45 -> Foggy
                48 -> DepositingRimeFog
                51 -> LightDrizzle
                53 -> ModerateDrizzle
                55 -> DenseDrizzle
                56 -> LightFreezingDrizzle
                57 -> DenseFreezingDrizzle
                61 -> SlightRain
                63 -> ModerateRain
                65 -> HeavyRain
                66 -> LightFreezingDrizzle
                67 -> HeavyFreezingRain
                71 -> SlightSnowFall
                73 -> ModerateSnowFall
                75 -> HeavySnowFall
                77 -> SnowGrains
                80 -> SlightRainShowers
                81 -> ModerateRainShowers
                82 -> ViolentRainShowers
                85 -> SlightSnowShowers
                86 -> HeavySnowShowers
                95 -> ModerateThunderstorm
                96 -> SlightHailThunderstorm
                99 -> HeavyHailThunderstorm
                else -> ClearSky
            }
        }
    }
}
