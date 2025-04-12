package com.example.dewy

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import android.util.Log

@Composable
/* Make a simple descriptive card for the day that is passed through. This is used for the WeatherScreens
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

/* Composable function to display the weather data and start data fetching. */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun WeatherScreen(viewModel: WeatherViewModel = viewModel(), navController: NavHostController) {
    /* Observe weather data | Update UI when LiveData changes. */
    val weatherData by viewModel.weatherData.observeAsState()
    val forecastData by viewModel.forecastData.observeAsState()
    var zipCode by remember { mutableStateOf("55021") }
    /* Used for ZIP input validation. */
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    /* Assignment 5 */
    val context = LocalContext.current
    /* Debugging */
    val TAG = "WeatherDebug"


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
                    text = "${weatherData?.main?.temp?.toInt() ?: "N/A"}°",
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
                            ?: "N/A",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    /* High and Low */
                    Text(
                        text = "H: ${weatherData?.main?.tempMax?.toInt() ?: "N/A"}° L: ${weatherData?.main?.tempMin?.toInt() ?: "N/A"}°",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                /* Additional sub-column for secondary information (humidity, pressure,
                * wind speed, gust, etc.). */
//                Column(
//                    modifier = Modifier
//                        .padding(vertical = 6.dp, horizontal = 6.dp)
//                        .fillMaxWidth(),
//                    horizontalAlignment = Alignment.Start,
//                    verticalArrangement = Arrangement.Bottom
//                ) {
//                    // Humidity
//                    Text(
//                        text = "RH: ${weatherData?.main?.humidity ?: "N/A"}%",
//                        color = Color.White,
//                        style = MaterialTheme.typography.bodySmall
//                    )
//                    // Pressure
//                    Text(
//                        text = "hPa: ${weatherData?.main?.pressure ?: "N/A"}",
//                        color = Color.White,
//                        style = MaterialTheme.typography.bodySmall
//                    )
//                    // Wind Speed
//                    Text(
//                        text = "Wind: ${weatherData?.wind?.speed ?: "N/A"}MPH",
//                        color = Color.White,
//                        style = MaterialTheme.typography.bodySmall
//                    )
//                    // Gust
//                    Text(
//                        text = "Gust: ${weatherData?.wind?.gust ?: "N/A"}MPH",
//                        color = Color.White,
//                        style = MaterialTheme.typography.bodySmall
//                    )
//                    // Country
//                    Text(
//                        text = "Country: ${weatherData?.sys?.country ?: "N/A"}",
//                        color = Color.White,
//                        style = MaterialTheme.typography.bodySmall
//                    )
//                    // Sunrise
//                    Text(
//                        text = "Sunrise: ${weatherData?.sys?.sunrise?.let { convertUnixToTime(it) } ?: "N/A"}",
//                        color = Color.White,
//                        style = MaterialTheme.typography.bodySmall
//                    )
//                    // Sunset
//                    Text(
//                        text = "Sunset: ${weatherData?.sys?.sunset?.let { convertUnixToTime(it) } ?: "N/A"}",
//                        color = Color.White,
//                        style = MaterialTheme.typography.bodySmall
//                    )
//                }
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
            * weatherData and forecastData will be setup and observed at this time as well.
            * Essentially, the button does input validation (length = 5 & only digits). Then
            * WeatherViewModels fetchWeather function checks for and handles null values
            * (if ZIP entered is valid but doesn't exist/isn't applicable.) and will return
            * a boolean value based on the functions success. The button will then respond based
            * on this boolean (display an error message or not). */

            /* This TextField manages the user input. User can enter whatever they want, input
            * will be checked and validated later. */
            TextField(
                value = zipCode,
                onValueChange = {
                    zipCode = it
                },
                label = { Text("Enter ZIP Code") },
                isError = errorMessage != null,
                /* For displaying the number pad. */
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )

            /* Assignment 5 */

            // Might need to do something for if it is NOT granted?
            val locationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()
            ) { granted ->
                Log.d(TAG, "Inside location launcher | Location permission result: $granted")
                if (granted) {
                    if (hasNotificationPermission(context)) {
                        Log.d(TAG, "Inside location launcher | notifications also granted | Starting service")
                        startWeatherService(context)
                    }
                } else {
                    Log.d(TAG, "Location permission denied")
                }
            }

            // Might need to do something for if it is NOT granted?
            val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()
            ) { granted ->
                Log.d(TAG, "Inside notification launcher | Notification permission result: $granted")
                if (granted) {
                    if (hasLocationPermission(context)) {
                        Log.d(TAG, "Inside notification launcher | location also granted | Starting service")
                        startWeatherService(context)
                    }
                } else {
                    Log.d(TAG, "Notification permission denied")
                }
            }

            /* Button that checks permission details and launches the service. */
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(onClick = {
                    val locationPermission = hasLocationPermission(context)
                    Log.d("WeatherDebug", "Inside button | Value of locationPermission: $locationPermission")
                    val notificationPermission = hasNotificationPermission(context)
                    Log.d("WeatherDebug", "Inside button | Value of nocationPermission: $notificationPermission")
                    when {
                        !locationPermission -> {
                            Log.d(TAG, "Location permission missing. Launching request.")
                            locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                        !notificationPermission -> {
                            Log.d(TAG, "Notification permission missing. Launching request.")
                            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        else -> {
                            startWeatherService(context)
                            if (context is MainActivity) {
                                context.fetchLocationAndUpdateWeather()
                            }
                        }
                    }
                }) {
                    Text("My Location")
                }
            }

            /* End Assignment 5 */

            Spacer(modifier = Modifier.height(8.dp))

            /* Displays the error message if there is one. */
            errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            /* Get Weather button */
            Button(
                onClick = {
                    if (zipCode.length == 5 && zipCode.all {it.isDigit() }) {
                        /* Calling a suspend function (fetchWeather). Must run in a coroutine.
                        * Functions are utilizing suspend to protect the main thread. */
                        coroutineScope.launch {
                            val success = viewModel.fetchWeather(zipCode)
                            if (!success) {
                                errorMessage = "Problem getting weather data. Please check your ZIP code."
                            } else {
                                errorMessage = null
                                viewModel.fetchForecast(zipCode)
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

            /* View Forecast button */
            Button(
                onClick = { navController.navigate("forecast/$zipCode") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Forecast")
            }
        }
    }
}