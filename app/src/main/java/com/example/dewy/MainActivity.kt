package com.example.dewy

/* Imports | Ctrl + Alt + O to optimize imports. */
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dewy.ui.theme.DewyTheme
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

/*
Data classes to fit JSON response.
They all hold relevant conditions data.
Retrofit will automatically convert the response into these objects!
@Serializable enables Kotlin serialization so Retrofit can convert JSON into this object.
*/

/* Main API response object. */
@Serializable
data class WeatherData(
    /* Maps these attributes to JSON fields. */
    @SerialName("main") val main: Main,
    @SerialName("weather") val weather: List<WeatherCondition>,
    @SerialName("wind") val wind: Wind,
    @SerialName("sys") val sys: Sys
)

@Serializable
data class Main(
    val temp: Double,
    @SerialName("feels_like") val feelsLike: Double,
    @SerialName("temp_min") val tempMin: Double,
    @SerialName("temp_max") val tempMax: Double,
    val humidity: Int,
    val pressure: Int
)

@Serializable
data class WeatherCondition(
    val description: String,
    val icon: String
)

@Serializable
data class Wind(
    val speed: Double,
    val deg: Int,
    val gust: Double
)

@Serializable
data class Sys(
    val country: String,
    val sunrise: Long,
    val sunset: Long
)

/*
Get weather data from OpenWeatherMap using Retrofit.
Defines how Retrofit fetches the data.
*/
interface WeatherApi {
    /* GET request to OpenWeatherMaps /weather endpoint. */
    @GET("weather")
    /* Returns a WeatherData object. */
    suspend fun getWeather(
        /* The city name we are using. */
        @Query("q") city: String,
        /* API Key */
        @Query("appid") apiKey: String,
        /* Get temperature data in Fahrenheit. */
        @Query("units") units: String = "imperial"
    ): WeatherData
}

/* Creates an instance (singleton) of Retrofit. */
object RetrofitClient {
    /* Root URL for all API requests. */
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

    /* Initialize Retrofit only when accessed (by lazy). */
    val instance: WeatherApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            /* Converts JSON responses into Kotlin objects. */
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }
}

/* Fetch data (manages API requests) and store results in LiveData. */
class WeatherViewModel : ViewModel() {
    /* Hold API response. */
    private val _weatherData = MutableLiveData<WeatherData?>()
    /* UI will observe this to update data. */
    val weatherData: LiveData<WeatherData?> = _weatherData

    fun fetchWeather(city: String) {
        /* Means network call is run asynchronously. */
        viewModelScope.launch {
            try {
                /*
                Debugging
                Log.d("WeatherDebug", "API Key: $API_KEY")
                */
                val response = RetrofitClient.instance.getWeather(city, API_KEY)
                /*
                Debugging
                Log.d("WeatherDebug", "API Response: $response")
                */
                /* Updates weather data with data received from OpenWeatherMap API request. */
                _weatherData.postValue(response)
            } catch (e: retrofit2.HttpException) {
                /* Debugging */
                Log.d("WeatherDebug", "HTTP Exception: ${e.code()} - ${e.message()}")
            } catch (e: Exception) {
                /* Debugging */
                Log.d("WeatherDebug", "General Exception: ${e.message}")
                /* If error, set _weatherData to null. */
                _weatherData.postValue(null)
            }
        }
    }
}

/* MainActivity where the UI is set. */
class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /* Extend UI to edges of screen. */
        enableEdgeToEdge()
        /* Load WeatherScreen. */
        setContent {
            DewyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { WeatherScreen() }
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------

/* Composable function to display the weather data and start data fetching. */
@Composable
fun WeatherScreen(viewModel: WeatherViewModel = viewModel()) {
    /* Observes weather data | Updates UI when LiveData changes. */
    val weatherData by viewModel.weatherData.observeAsState()
    /*
    Placed here instead of inside LaunchedEffect as .current must be in a composable function.
    Gets city name from strings.xml.
    */
    val context = LocalContext.current
    val city = context.getString(R.string.city)

    /* Function to convert the returned sunrise and sunset data into a readable time. */
    fun convertUnixToTime(unixTime: Long): String {
        val date = Date(unixTime * 1000) /* Convert seconds to milliseconds. */
        val format = SimpleDateFormat("hh:mm a", Locale.getDefault()) /* Format as HH:MM AM/PM. */
        return format.format(date)
    }

    /* Starts the fetchWeather() function when the application is launched. */
    LaunchedEffect(Unit) {
        viewModel.fetchWeather(city)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp)
    ) {
        Row(
            modifier = Modifier
                .background(Color.LightGray)
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 14.dp)
        ) {
            Text(text = stringResource(id = R.string.app_name), style = MaterialTheme.typography.headlineSmall)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = stringResource(id = R.string.city), style = MaterialTheme.typography.headlineSmall)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${weatherData?.main?.temp?.toInt()}°",
                    style = MaterialTheme.typography.displayMedium.copy(fontSize = 80.sp)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(context.getString(R.string.feels_like_label) + " ${weatherData?.main?.temp?.toInt()}°")
            }
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.sunny),
                    contentDescription = stringResource(id = R.string.weather_icon_desc),
                    modifier = Modifier.size(60.dp)
                )
            }
        }
        /* Additional current conditions data. */
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp, horizontal = 32.dp)) {
            Text(context.getString(R.string.low_label) + " ${weatherData?.main?.tempMin?.toInt()}" + context.getString(R.string.degree_symbol_label))
            Text(context.getString(R.string.high_label) + " ${weatherData?.main?.tempMax?.toInt()}" + context.getString(R.string.degree_symbol_label))
            Text(context.getString(R.string.humidity_label) + " ${weatherData?.main?.humidity}" + context.getString(R.string.percent_label))
            Text(context.getString(R.string.pressure_label)+ " ${weatherData?.main?.pressure}" + context.getString(R.string.hpa_label))
            Text(context.getString(R.string.wind_label) + " ${weatherData?.wind?.speed?.toInt()}" + context.getString(R.string.mph_label))
            Text(context.getString(R.string.degree_label)+ " ${weatherData?.wind?.deg}")
            Text(context.getString(R.string.gust_label) + " ${weatherData?.wind?.gust?.toInt()}" + context.getString(R.string.mph_label))
            Text(context.getString(R.string.desc_label) + " ${weatherData?.weather?.get(0)?.description}")
            Text(context.getString(R.string.country_label)+ " ${weatherData?.sys?.country}")
            Text(context.getString(R.string.sunrise_label) + " ${weatherData?.sys?.sunrise?.let { convertUnixToTime(it) }}")
            Text(context.getString(R.string.sunset_label) + " ${weatherData?.sys?.sunset?.let { convertUnixToTime(it) }}")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherPreview() {
    DewyTheme {
        WeatherScreen()
    }
}