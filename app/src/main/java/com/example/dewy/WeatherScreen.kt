package com.example.dewy

import android.Manifest
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import com.example.dewy.util.TestHelper

@Composable
/* Make a simple descriptive card for the day that is passed through. This is used for the WeatherScreens
*  LazyRow. */
fun ForecastCardRow(daily: DailyForecast) {
    val icon = getLocalIcon(daily.weather?.firstOrNull()?.icon)
    val day = getDayOfWeek(daily.dt)
    Card(
        modifier = Modifier
            .size(width = 80.dp, height = 120.dp)
            .testTag("ForecastCard"),
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

/* A helper function to continue the location flow if both permissions are granted.
 * It will start WeatherService and call fetchLocationAndUpdateWeather() from MainActivity. */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun startFlowIfPermissionGranted(context: Context) {
    if (hasLocationPermission(context) && hasNotificationPermission(context)) {
        startWeatherService(context)
        /* Check if the current context is from MainActivity. fetchLocationAndUpdateWeather
        *  is from MainActivity so this needs to be the case. */
        if (context is MainActivity) {
            context.fetchLocationAndUpdateWeather()
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
    /* Used for ZIP input validation. Defaults to Faribault, MN. */
    var zipCode by remember { mutableStateOf("55021") }
    /* Holds an error message if there is one. */
    var errorMessage by remember { mutableStateOf<String?>(null) }
    /* Used for launching suspend functions. */
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    /* Used for debugging | Lots of them in this file. */
    val TAG = "WeatherDebug"


    /* Launch on startup and with default location (Faribault, MN). Updated for testing. */
    LaunchedEffect(zipCode) {
        if (!TestHelper.isRunningTest()) {
            if (weatherData == null) {
                viewModel.fetchWeather(zipCode)
            }
            if (forecastData == null) {
                viewModel.fetchForecast(zipCode)
            }
        }
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
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

            /* Where the icon is handled and displayed. Utilizes a function to map a locally stored
            * icon to the icon code from the API response. */
            val icon = getLocalIcon(weatherData?.weather?.firstOrNull()?.icon)
            Image(
                painter = painterResource(icon),
                contentDescription = "Weather Icon",
                modifier = Modifier.size(200.dp)
                    .testTag("WeatherIcon")
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
                        .padding(vertical = 42.dp, horizontal = 10.dp),
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
                *  wind speed, gust, etc.). */
                Column(
                    modifier = Modifier
                        .padding(vertical = 6.dp, horizontal = 6.dp)
                        .fillMaxWidth()
                        .heightIn(max = 100.dp)
                        .background((Color(0xFF98C0E5)))
                        .padding(4.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    // Humidity
                    Text(
                        text = "RH: ${weatherData?.main?.humidity ?: "N/A"}%",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                    // Pressure
                    Text(
                        text = "hPa: ${weatherData?.main?.pressure ?: "N/A"}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                    // Wind Speed
                    Text(
                        text = "Wind: ${weatherData?.wind?.speed ?: "N/A"}MPH",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                    // Gust
                    Text(
                        text = "Gust: ${weatherData?.wind?.gust ?: "N/A"}MPH",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                    // Country
                    Text(
                        text = "Country: ${weatherData?.sys?.country ?: "N/A"}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                    // Sunrise
                    Text(
                        text = "Sunrise: ${weatherData?.sys?.sunrise?.let { convertUnixToTime(it) } ?: "N/A"}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                    // Sunset
                    Text(
                        text = "Sunset: ${weatherData?.sys?.sunset?.let { convertUnixToTime(it) } ?: "N/A"}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            /* Code here takes seven of the days returned from the forecast API response
            * and creates a card for them (utilizing ForecastCardRow()) and then adds each of
            * them to a LazyRow. */
            forecastData?.list?.take(7)?.let { forecastList ->
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(forecastList) { daily ->
                        ForecastCardRow(daily)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            /* Error handling reminder:
            *  Initially LaunchedEffect will trigger and load data for the ZIP 55021 (Faribault).
            *  weatherData and forecastData will be setup and observed at this time as well.
            *  Essentially, the button does input validation (length = 5 & only digits). Then
            *  WeatherViewModels fetchWeather function checks for and handles null values
            *  (if ZIP entered is valid but doesn't exist/isn't applicable.) and will return
            *  a boolean value based on the functions success. The button will then respond based
            *  on this boolean (display an error message or not). */

            /* Permission launchers follow a sequential pattern:
             * 1) If location is not granted, request it.
             * 2) If granted, check notification permission.
             * 3) If not granted, request notification permission.
             * 4) When both are granted, proceed with the next step in the process (startFlow).
             * NOTE: Might have a problem where location is not granted and notification is granted.
             *       Might have to do some additional work later on to make this clearer.
             *       In cases like this, nothing will happen. Should try to make the situation
             *       clearer to the user. */

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                /* This TextField manages the user input. User can enter whatever they want, input
                *  will be checked and validated later. */
                TextField(
                    value = zipCode,
                    onValueChange = {zipCode = it},
                    label = { Text("Enter ZIP Code") },
                    isError = errorMessage != null,
                    /* For displaying the number pad. */
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).testTag("ZipInput")
                )

                Spacer(modifier = Modifier.width(8.dp))

                /* Requests notification permission. */
                val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                    // Log.d(TAG, "Notification permission result: $granted")
                    startFlowIfPermissionGranted(context)
                }

                /* Requests location permission. Checks notification permission as well. If both are
                *  granted, it will start the weather service and data fetching to cause a live data update. */
                val locationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                    // Log.d(TAG, "Location permission result: $granted")
                    if (!hasNotificationPermission(context)) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        startFlowIfPermissionGranted(context)
                    }
                }

                /* Button that checks permission details and helps launch the service.
                *  Works in tandem with the launchers to keep the flow reasonable. */
                Button(
                    onClick = {
                        when {
                            !hasLocationPermission(context) -> {
                                locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                            !hasNotificationPermission(context) -> {
                                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            else -> {
                                startFlowIfPermissionGranted(context)
                            }
                        }
                    },
                    modifier = Modifier.testTag("GetWeatherButton"),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "My Location Button",
                        tint = Color.White
                    )
                }
            }

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
                modifier = Modifier.fillMaxWidth().testTag("MyLocationButton")
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