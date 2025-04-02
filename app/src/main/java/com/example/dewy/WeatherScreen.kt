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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch

@Composable
/* Make a card for the day that is passed through. This is used for the WeatherScreens
* LazyRow. */
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

/*
General to Finish!
- Check for Strings (must be externalized).
- Finish ERROR handling for incorrect ZIP data!!! It currently does not respond for some valid zip entries
  EX: 11111
- Finish commenting.

Just get it to handle bad inputs first. That is it. Only 5 and only digits. Anything else, displays
a warning/red line. That should be simple enough. For handling a bad request... We'll have to see.
Tackle that after!

*/

/* Composable function to display the weather data and start data fetching. */
@Composable
fun WeatherScreen(viewModel: WeatherViewModel = viewModel(), navController: NavHostController) {
    /* Observes weather data | Updates UI when LiveData changes. */
    val weatherData by viewModel.weatherData.observeAsState()
    val forecastData by viewModel.forecastData.observeAsState()
    var zipCode by remember { mutableStateOf("55021") }
    /* For error handling */
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()


    /* Launch on startup and with default location (Faribault, MN) */
    LaunchedEffect(Unit) {
        viewModel.fetchWeather(zipCode)
        viewModel.fetchForecast(zipCode)
    }

    /* Box to hold and organize WeatherScreen data */
    Box(
        modifier = Modifier
            .fillMaxSize()
            /* Helps automate the padding for certain areas (such as the notification and navigation bars). */
            .padding(WindowInsets.systemBars.asPaddingValues())
            .background(Color(0xFF9ECFFF))
    ) {
        /* Main Column for the WeatherScreens data */
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(24.dp))

            /* Where the city name is handled and displayed. */
            Text(
                text = weatherData?.name.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            /* UPDATE this area to utilize -> weatherData?.let { ... }
            * Will only run this area if WeatherData is NOT null. */

            /* Where the icon is handled and displayed. Utilizes a function to map a locally stored
            * icon to the icon code from the API response. */
            val icon = getLocalIcon(weatherData?.weather?.firstOrNull()?.icon)
            Image(
                painter = painterResource(icon),
                contentDescription = "Weather Icon",
                modifier = Modifier.size(200.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            /* Row for handling and displaying the main temperature, description,
            * min, max, humidity, pressure, wind speed, gust, country, sunrise, and sunset. */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 14.dp),
                Arrangement.Start
            ) {

                /* Main Temp */
                Text(
                    text = "${weatherData?.main?.temp?.toInt() ?: "--"}°",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp),
                    color = Color.White
                )

                /* Sub-column for managing key data (description and high/low values) */
                Column(
                    modifier = Modifier
                        .padding(vertical = 42.dp, horizontal = 18.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    /* Description */
                    Text(
                        text = weatherData?.weather?.firstOrNull()?.description?.replaceFirstChar { it.uppercase() }
                            ?: "Loading...",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    /* High and Low */
                    Text(
                        text = "H: ${weatherData?.main?.tempMax?.toInt() ?: "--"}° L: ${weatherData?.main?.tempMin?.toInt() ?: "--"}°",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                /* Additional sub-column for secondary information (humidity, pressure,
                * wind speed, gust, etc.). */
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

            /* Code here takes five of the days returned from the forecast API response
            * and creates a card for them (utilizing ForecastCardRow()) and then adds each of
            * them to a LazyRow. */
            forecastData?.list?.take(5)?.let { forecastList ->
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(forecastList) { daily ->
                        ForecastCardRow(daily)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            /* Error handling reminder:
            * Initially LaunchedEffect will trigger and load data for the ZIP 55021 (Faribault).
            * weatherData and forecastData will be setup and observed at this time as well. */

            /* This TextField manages the user input. It only allows for five digit inputs. */
            TextField(
                value = zipCode,
                onValueChange = {
                    zipCode = it
                },
                label = { Text("Enter ZIP Code") },
                isError = errorMessage != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (zipCode.length == 5 && zipCode.all {it.isDigit() }) {
                        coroutineScope.launch {
                            val success = viewModel.fetchWeather(zipCode)
                            viewModel.fetchForecast(zipCode)
                            if (!success) {
                                errorMessage = "Problem loading weather data. Check your ZIP code."
                            } else {
                                errorMessage = null
                            }
                        }
                    } else {
                        errorMessage = "Zip code must be 5 digits."
                    }
                },
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