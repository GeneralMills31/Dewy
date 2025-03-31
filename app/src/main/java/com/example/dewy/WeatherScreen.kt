package com.example.dewy

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

@Composable
// GET ICON FOR EACH DAY (NOT JUST FOR ONE AND APPLY TO ALL)
fun ForecastCardLocal(daily: DailyForecast) {
    val icon = getLocalIcon(daily.weather?.firstOrNull()?.icon)
    Card(
        modifier = Modifier
            .size(width = 80.dp, height = 120.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xAAFFFFFF))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
    var zipCode by remember { mutableStateOf("55101") }

    // Fetch default weather (Saint Paul MN) at startup!
    LaunchedEffect(Unit) {
        viewModel.fetchWeather(zipCode)
        viewModel.fetchForecast(zipCode)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF9ECFFF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = weatherData?.name.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            val icon = getLocalIcon(weatherData?.weather?.firstOrNull()?.icon)
            Image(
                painter = painterResource(icon),
                contentDescription = "Weather Icon",
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "${weatherData?.main?.temp?.toInt() ?: "--"}°",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White
            )

            Text(
                text = weatherData?.weather?.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "Loading...",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "${weatherData?.main?.tempMax?.toInt() ?: "--"}° | L: ${weatherData?.main?.tempMin?.toInt() ?: "--"}°",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            forecastData?.list?.take(5)?.let { forecastList ->
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(forecastList) { daily ->
                        ForecastCardLocal(daily)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    viewModel.fetchWeather(zipCode)
                    viewModel.fetchForecast(zipCode)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Get Weather")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    navController.navigate("forecast/$zipCode")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Forecast")
            }
        }
    }
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 48.dp)
//    ) {
//        // HEADER
//        Row(
//            modifier = Modifier
//                .background(Color.LightGray)
//                .fillMaxWidth()
//                .padding(vertical = 10.dp, horizontal = 14.dp)
//        ) {
//            Text(
//                text = stringResource(id = R.string.app_name),
//                style = MaterialTheme.typography.headlineSmall
//            )
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        weatherData?.let { data ->
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.Center,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Column(
//                    modifier = Modifier
//                        .weight(1f)
//                        .fillMaxWidth()
//                        .padding(horizontal = 24.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Text(
//                        text = "${data.main.temp}°F",
//                        style = MaterialTheme.typography.displayMedium.copy(fontSize = 60.sp)
//                    )
//                    Spacer(modifier = Modifier.size(4.dp))
//                    Text("Feels like: ${data.main.feelsLike}°F")
//                }
//
//                Column(
//                    modifier = Modifier
//                        .weight(1f)
//                        .fillMaxWidth(),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    val icon = getLocalIcon(weatherData?.weather?.firstOrNull()?.icon)
//                    Image(
//                        painter = painterResource(icon),
//                        contentDescription = "Weather Icon",
//                        modifier = Modifier.size(60.dp)
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(32.dp))
//
//            // Extra Details
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 32.dp)
//            ) {
//                Text("Humidity: ${data.main.humidity}%")
//                Text("High: ${data.main.tempMax}°F")
//                Text("Low: ${data.main.tempMin}°F")
//                Text("Description: ${data.weather.firstOrNull()?.description ?: "N/A"}")
//                Text("Sunrise: ${convertUnixToTime(data.sys.sunrise)}")
//                Text("Sunset: ${convertUnixToTime(data.sys.sunset)}")
//            } ?: Text(
//                text = "No Weather Data Loaded.",
//                modifier = Modifier.padding(16.dp)
//            )
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
//            TextField(
//                value = zipCode,
//                onValueChange = { zipCode = it },
//                label = { Text("Enter ZIP Code") },
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Button(
//                onClick = { viewModel.fetchWeather(zipCode) },
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text("Get Weather")
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Button(
//                onClick = { navController.navigate("forecast/$zipCode") },
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text("View Forecast")
//            }
//
//        }
//    }
}