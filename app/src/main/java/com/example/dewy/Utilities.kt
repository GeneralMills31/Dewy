package com.example.dewy

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/* Function to convert the returned sunrise and sunset data into a readable time. */
fun convertUnixToTime(unixTime: Long): String {
    val date = Date(unixTime * 1000) /* Convert seconds to milliseconds. */
    val format = SimpleDateFormat("hh:mm a", Locale.getDefault()) /* Format as HH:MM AM/PM. */
    return format.format(date)
}