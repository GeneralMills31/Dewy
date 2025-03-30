package com.example.dewy

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
Data classes to fit JSON response.
They all hold relevant conditions data.
Retrofit will automatically convert the response into these objects!
@Serializable enables Kotlin serialization so Retrofit can convert JSON into this object.
*/

/* Main API response object. */
@Serializable
data class WeatherData(
    /* Maps these attributes to JSON fields. */
    @SerialName("main") val main: Main,
    @SerialName("weather") val weather: List<WeatherCondition>,
    @SerialName("wind") val wind: Wind,
    @SerialName("sys") val sys: Sys
)

@Serializable
data class Main(
    val temp: Double,
    @SerialName("feels_like") val feelsLike: Double,
    @SerialName("temp_min") val tempMin: Double,
    @SerialName("temp_max") val tempMax: Double,
    val humidity: Int,
    val pressure: Int
)

@Serializable
data class WeatherCondition(
    val description: String,
    val icon: String
)

@Serializable
data class Wind(
    val speed: Double,
    val deg: Int,
    val gust: Double
)

@Serializable
data class Sys(
    val country: String,
    val sunrise: Long,
    val sunset: Long
)