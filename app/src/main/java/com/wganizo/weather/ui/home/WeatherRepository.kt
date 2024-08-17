package com.wganizo.weather.ui.home

import com.wganizo.weather.constants.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class WeatherRepository {
    private val constants = Constants()
    private val weatherApi: WeatherApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(constants.baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        weatherApi = retrofit.create(WeatherApi::class.java)
    }

    suspend fun getWeather(lat: Double, lon: Double): Weather? {
        return try {
            val response = weatherApi.getWeather(lat, lon, constants.apiKey)
            if (response.isSuccessful) {
                response.body()?.toWeather(lat, lon)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    interface WeatherApi {
        @GET("weather")
        suspend fun getWeather(
            @Query("lat") lat: Double,
            @Query("lon") lon: Double,
            @Query("appid") apiKey: String,
            @Query("units") units: String = "metric"
        ): retrofit2.Response<WeatherResponse>
    }
}