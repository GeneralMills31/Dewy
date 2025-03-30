package com.example.dewy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ForecastScreen(viewModel: WeatherViewModel = viewModel(), zip: String = "55101") {
    LaunchedEffect(zip) {
        viewModel.fetchForecast(zip)
    }

    val forecastData by viewModel.forecastData.observeAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp)
    ) {
        Text("Forecast for $zip")
        Spacer(Modifier.height(16.dp))

        forecastData?.list?.forEach { daily ->
            Text("Date: ${convertUnixToTime(daily.dt)}")
            Text("High: ${daily.temp.max.toInt()}°F, Low: ${daily.temp.min.toInt()}°F")
            Text("Description: ${daily.weather.firstOrNull()?.description ?: "N/A"}")
            Spacer(modifier = Modifier.height(12.dp))
        } ?: Text("Loading forecast...")
    }
}
