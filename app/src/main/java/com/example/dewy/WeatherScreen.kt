package com.example.dewy

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

@Composable
// Make a card for the day that is passed through.
fun ForecastCardRow(daily: DailyForecast) {
    val icon = getLocalIcon(daily.weather?.firstOrNull()?.icon)
    val day = getDayOfWeek(daily.dt)
    Card(
        modifier = Modifier
            .size(width = 80.dp, height = 120.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xAAFFFFFF))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = day, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Image(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("${daily.temp.min.toInt()}° / ${daily.temp.max.toInt()}°", fontSize = 12.sp)
        }
    }
}

/* Composable function to display the weather data and start data fetching. */
@Composable
fun WeatherScreen(viewModel: WeatherViewModel = viewModel(), navController: NavHostController) {
    /* Observes weather data | Updates UI when LiveData changes. */
    val weatherData by viewModel.weatherData.observeAsState()
    /* UI Work */
    val forecastData by viewModel.forecastData.observeAsState()
    // Don't remove the 'us' or else it will default to the Ukraine.
    var zipCode by remember { mutableStateOf("55101,us") }
    val context = LocalContext.current

    // Fetch default weather (Saint Paul MN) at startup!
    LaunchedEffect(Unit) {
        viewModel.fetchWeather(zipCode)
        viewModel.fetchForecast(zipCode)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .background(Color(0xFF9ECFFF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // City Name
            Text(
                text = weatherData?.name.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Icon
            val icon = getLocalIcon(weatherData?.weather?.firstOrNull()?.icon)
            Image(
                painter = painterResource(icon),
                contentDescription = "Weather Icon",
                modifier = Modifier.size(200.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Row for Temp, Desc, and H/L.
            Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 14.dp),
                Arrangement.Start
                ) {
                // Main Temp
                Text(
                    text = "${weatherData?.main?.temp?.toInt() ?: "--"}°",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp),
                    color = Color.White
                )

                Column(
                    modifier = Modifier
                        .padding(vertical = 42.dp, horizontal = 18.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    // Description
                    Text(
                        text = weatherData?.weather?.firstOrNull()?.description?.replaceFirstChar { it.uppercase() }
                            ?: "Loading...",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    // High and Low
                    Text(
                        text = "H: ${weatherData?.main?.tempMax?.toInt() ?: "--"}° L: ${weatherData?.main?.tempMin?.toInt() ?: "--"}°",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(vertical = 6.dp, horizontal = 6.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    // Humidity
                    Text(
                        text = "RH: ${weatherData?.main?.humidity ?: "--"}%",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                    // Pressure
                    Text(
                        text = "hPa: ${weatherData?.main?.pressure ?: "--"}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                    // Wind Speed
                    Text(
                        text = "Wind: ${weatherData?.wind?.speed ?: "--"}MPH",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                    // Gust
                    Text(
                        text = "Gust: ${weatherData?.wind?.gust ?: "--"}MPH",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                    // Country
                    Text(
                        text = "Country: ${weatherData?.sys?.country ?: "--"}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                    // Sunrise
                    Text(
                        text = "Sunrise: ${weatherData?.sys?.sunrise?.let { convertUnixToTime(it) } ?: "--"}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                    // Sunset
                    Text(
                        text = "Sunset: ${weatherData?.sys?.sunset?.let { convertUnixToTime(it) } ?: "--"}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontal LazyRow where each element is a card returned by ForecastCardLocal
            forecastData?.list?.take(5)?.let { forecastList ->
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(forecastList) { daily ->
                        ForecastCardRow(daily)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Get weather button
            TextField(
                value = zipCode,
                onValueChange = { zipCode = it },
                label = { Text("Enter ZIP Code") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.fetchWeather(zipCode) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Get Weather")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // View Forecast button
            Button(
                onClick = { navController.navigate("forecast/$zipCode") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Forecast")
            }
        }
    }
}