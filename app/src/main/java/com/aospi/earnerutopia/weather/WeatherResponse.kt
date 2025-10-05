package com.aospi.earnerutopia.weather

data class WeatherResponse(
    val hourly: List<HourlyForecast>
)

data class HourlyForecast(
    val dt: Long,
    val temp: Float,
    val weather: List<WeatherDescription>
)

data class WeatherDescription(
    val main: String,
    val description: String
)
