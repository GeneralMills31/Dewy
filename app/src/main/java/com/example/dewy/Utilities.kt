package com.example.dewy

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/* Function to convert the returned sunrise and sunset data into a readable time. */
fun convertUnixToTime(unixTime: Long): String {
    /* Convert seconds to milliseconds. */
    val date = Date(unixTime * 1000)
    /* Format as HH:MM AM/PM. */
    val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return format.format(date)
}

/* Function to map the API response icon code to a locally saved icon. */
fun getLocalIcon(iconCode: String?): Int {
    return when (iconCode) {
        "01d" -> R.drawable.sunny
        "01n" -> R.drawable.moon
        "02d" -> R.drawable.few_clouds_day
        "02n" -> R.drawable.few_clouds_night
        "03d", "03n" -> R.drawable.scattered_clouds
        "04d", "04n" -> R.drawable.broken_clouds
        "09d", "09n", "10d", "10n" -> R.drawable.rain
        "11d", "11n" -> R.drawable.thunderstorm
        "13d", "13n" -> R.drawable.snow
        "50d", "50n" -> R.drawable.mist
        else -> R.drawable.unknown
    }
}

/*
Function to get the day of the week for a specific day. Used for the LazyRow cards
in WeatherScreen and LazyColumn cards in ForecastScreen.
 */
fun getDayOfWeek(unixTime: Long): String {
    /* Seconds to milliseconds */
    val date = Date(unixTime * 1000)
    /* 'EEE' for Mon, Tue, etc. 'EEEE" for Monday, Tuesday, etc. */
    val format = SimpleDateFormat("EEE", Locale.getDefault())
    return format.format(date)
}