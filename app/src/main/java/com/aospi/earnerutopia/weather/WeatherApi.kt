package com.aospi.earnerutopia.weather

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("onecall")
    suspend fun getHourlyForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("exclude") exclude: String = "current,minutely,daily,alerts",
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse
}
