package com.example.dewy
/* Imports | Ctrl + Alt + O to optimize imports. */
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dewy.ui.theme.DewyTheme

/*  GENERAL NOTES AND REFERENCES:
 *  Icon Set = Kawaii Flat
 *  https://www.flaticon.com/search?style_id=134&author_id=1&word=weather&type=standard
 *
 *  val = immutable | var = mutable
 *  Kotlin has automatic type detection (type inference | Will detect String, Int, Double, etc.).
 *
 *  How to read functions
 *  EX: add(a: Int, b: Int): Int --> Takes two Int parameters (a,b) and returns an Int.
 *
 *  How to read classes
 *  EX: class Person(val name: String, var age: Int) {
 *          fun birthday() {
 *              age += 1
 *          }
 *      }
 *      Explanation: This is a class called person which has the attributes name and age.
 *                   Class functions defined inside the classes {} (like birthday() here).
 *
 *  Data Classes: Special classes designed to hold data. They utilize @Serializable (serialization)
 *                which allows for converting JSON --> Kotlin objects.
 *  EX: @Serializable
 *      data class WeatherData(
 *          @SerialName("main") val main: Main,
 *          @SerialName("weather") val weather: List<WeatherCondition>
 *      )
 *
 *      Explanation: @SerialName("X") maps a JSON key to one of the classes properties.
 *
 *  Lists: val num = listOf(1,2,3)
 *         val mutableNum = MutableListOf(1,2,3)
 *
 *  Loops: for (i in num) { println(i) }
 *
 *  Null types and Safe calls:
 *         var name: String? // Nullable variable
 *         println(name?.length) // Prints null instead of crashing application.
 *
 *  Lambdas: Defined with {} and -> which separates the parameters and the body. If return type isn't
 *           Unit, last expression in the body is returned.
 *
 *           EX: val sum: (Int, Int) -> Int = { x, y -> x + y }
 *           Explanation: (Int, Int) -> Int declares a function type that takes two Int arguments and returns an Int.
 *                        { x, y -> x + y } is the lambda expression, defining the function's behavior.
 *                        x and y are the parameters and x + y is the body, and its value is returned.
 *
 *  Code Line Explanations:
 *         val temp = weatherData?.main?.temp ?: "N/A"
 *         Explanation: '?: "N/A"' = If null, display N/A
 *
 *  .....Continue here.....
 *
 */
/*
Application Behavior:
1) MainActivity launches and calls WeatherScreen().
2A) WeatherScreen initializes the ViewModel, LiveData, and gets the data. LiveData and UI update as a result.
2B) WeatherScreen launches. LiveData and ViewModel are created. LaunchEffect is then triggered which tells the ViewModel to
fetch the data. The Viewmodel calls its fetchWeather() function which utilizes the Retrofit client to make the OpenWeatherMap
API data request. The Retrofit client uses WeatherAPI to define how it gets the data.
JSON data is returned and converted to Kotlin data. Upon return to the ViewModel, if successful,
LiveData (_weatherData) is updated with the new data and the UI changes as a result.
*/

/* API Key */
const val API_KEY = BuildConfig.OPENWEATHER_API_KEY

/* MainActivity where the UI is set. */
class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /* Extend UI to edges of screen. */
        enableEdgeToEdge()
        /* Load WeatherScreen. */
        /* Do I still need a scaffold here? */
        setContent {
            val navController = rememberNavController()
            DewyTheme {
                NavHost(navController = navController, startDestination = "current") {
                    composable("current") {
                        WeatherScreen(navController = navController)
                    }
                    composable("forecast/{zip}") { backStackEntry ->
                        val zip = backStackEntry.arguments?.getString("zip") ?: "55021"
                        ForecastScreen(zip = zip, navController = navController)
                    }
                }
            }
        }
    }
}

/* Composable function to display the weather data and start data fetching. */
@Composable
fun WeatherScreen(viewModel: WeatherViewModel = viewModel(), navController: NavHostController) {
    /* Observes weather data | Updates UI when LiveData changes. */
    val weatherData by viewModel.weatherData.observeAsState()
    var zipCode by remember { mutableStateOf("55101") }

    // Fetch default weather (Saint Paul MN) at startup!
    LaunchedEffect(Unit) {
        viewModel.fetchWeather(zipCode)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp)
    ) {
        // HEADER
        Row(
            modifier = Modifier
                .background(Color.LightGray)
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 14.dp)
        ) {
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineSmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        weatherData?.let { data ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${data.main.temp}°F",
                        style = MaterialTheme.typography.displayMedium.copy(fontSize = 60.sp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Feels like: ${data.main.feelsLike}°F")
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val icon = getLocalIcon(weatherData?.weather?.firstOrNull()?.icon)
                    Image(
                        painter = painterResource(icon),
                        contentDescription = "Weather Icon",
                        modifier = Modifier.size(60.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Extra Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                Text("Humidity: ${data.main.humidity}%")
                Text("High: ${data.main.tempMax}°F")
                Text("Low: ${data.main.tempMin}°F")
                Text("Description: ${data.weather.firstOrNull()?.description ?: "N/A"}")
                Text("Sunrise: ${convertUnixToTime(data.sys.sunrise)}")
                Text("Sunset: ${convertUnixToTime(data.sys.sunset)}")
            } ?: Text(
                text = "No Weather Data Loaded.",
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
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

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate("forecast/$zipCode") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Forecast")
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherPreview() {
    val navController = rememberNavController()
    DewyTheme {
        WeatherScreen(navController = navController)
    }
}