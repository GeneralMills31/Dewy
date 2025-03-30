package com.example.dewy

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
Data classes to fit JSON response.
They all hold relevant conditions data.
Retrofit will automatically convert the response into these objects!
@Serializable enables Kotlin serialization so Retrofit can convert JSON into this object.
*/

/* Main API response object (for Forecast). */
/* ASSIGNMENT 4 */
@Serializable
data class ForecastData(
    @SerialName("city") val city: City,
    @SerialName("list") val list: List<DailyForecast>,
    @SerialName("cnt") val cnt: Int
)

@Serializable
data class City(
    @SerialName("name") val name: String,
    @SerialName("country") val country: String
)

@Serializable
data class DailyForecast(
    @SerialName("dt") val dt: Long,
    @SerialName("temp") val temp: Temperature,
    @SerialName("weather") val weather: List<WeatherCondition>
)

@Serializable
data class Temperature(
    @SerialName("day") val day: Double,
    @SerialName("min") val min: Double,
    @SerialName("max") val max: Double
)