package com.example.dewy

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController


@Composable
// Make a card for the day that is passed through.
fun ForecastCardColumn(daily: DailyForecast) {
    val icon = getLocalIcon(daily.weather?.firstOrNull()?.icon)
    val day = getDayOfWeek(daily.dt)
    Card(
        modifier = Modifier
            .size(width = 400.dp, height = 80.dp)
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xAAFFFFFF))
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = day, style = MaterialTheme.typography.labelMedium)
            Image(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Text("${daily.temp.min.toInt()}° / ${daily.temp.max.toInt()}°", fontSize = 12.sp)
        }
    }
}

@Composable
fun ForecastScreen(viewModel: WeatherViewModel = viewModel(), zip: String = "55101", navController: NavController) {
    LaunchedEffect(zip) {
        viewModel.fetchForecast(zip)
    }

    val forecastData by viewModel.forecastData.observeAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .background(Color(0xFF9ECFFF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
        ) {
            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = { navController.popBackStack() },
            ) {
                Text("Back to Current Weather")
            }
            // Horizontal LazyRow where each element is a card returned by ForecastCardLocal
            forecastData?.list?.let { forecastList ->
                LazyColumn(modifier = Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.CenterHorizontally) {
                    items(forecastList) { daily ->
                        ForecastCardColumn(daily)
                    }
                }
            }
        }
    }
}
