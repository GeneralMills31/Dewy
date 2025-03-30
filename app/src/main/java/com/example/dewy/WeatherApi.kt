package com.example.dewy

import retrofit2.http.GET
import retrofit2.http.Query

/*
Get weather data from OpenWeatherMap using Retrofit.
Defines how Retrofit fetches the data.
*/
interface WeatherApi {
    /* GET request to OpenWeatherMaps /weather endpoint. */
    @GET("weather")
    /* Returns a WeatherData object. */
    suspend fun getWeather(
        /* The city name we are using. */
        @Query("q") city: String,
        /* API Key */
        @Query("appid") apiKey: String,
        /* Get temperature data in Fahrenheit. */
        @Query("units") units: String = "imperial"
    ): WeatherData

    /* ASSIGNMENT 4 */
    @GET("forecast/daily")
    suspend fun getForecast(
        @Query("zip") zip: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "imperial",
        @Query("cnt") days: Int = 16
    ): ForecastData
}