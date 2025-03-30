package com.example.dewy

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController


@Composable
fun ForecastScreen(viewModel: WeatherViewModel = viewModel(), zip: String = "55101", navController: NavController) {
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

        // Put back button here!
        Text("Days Loaded: ${forecastData?.list?.size ?: 0}")

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { navController.popBackStack() }) {
            Text("Back to Current Weather")
        }

    }
}
