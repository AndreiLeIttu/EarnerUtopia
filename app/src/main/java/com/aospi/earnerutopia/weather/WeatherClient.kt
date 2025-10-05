package com.aospi.earnerutopia.weather

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WeatherClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/data/3.0/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: WeatherApi = retrofit.create(WeatherApi::class.java)
}
