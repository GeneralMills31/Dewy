package com.example.dewy

// Imports
// Ctrl + Alt + O to optimize imports.
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


/** GENERAL NOTES:
 *  Icon Set = Kawaii Flat
 *  https://www.flaticon.com/search?style_id=134&author_id=1&word=weather&type=standard
 */

// API Key
const val API_KEY = BuildConfig.OPENWEATHER_API_KEY

// Data classes to represent the JSON response.
// Holds relevant conditions data.
@Serializable
data class WeatherData(
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

// Retrofit interface for API calls
interface WeatherApi {
    @GET("weather")
    suspend fun getWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherData
}

// Retrofit client instance
object RetrofitClient {
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

    val instance: WeatherApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }
}

// ViewModel to manage data fetching and hold LiveData
class WeatherViewModel : ViewModel() {
    private val _weatherData = MutableLiveData<WeatherData?>()
    val weatherData: LiveData<WeatherData?> = _weatherData

    // Update with API key
    fun fetchWeather(city: String) {
        viewModelScope.launch {
            // Prints not working
            // println("API Key: $API_KEY")
            try {
                // Debugging
                Log.d("WeatherDebug", "API Key: $API_KEY")
                // Updated with API key
                val response = RetrofitClient.instance.getWeather(city, API_KEY)
                // Troubleshooting
                // println("API Response: $response")
                Log.d("WeatherDebug", "API Response: $response")
                _weatherData.postValue(response)
            } catch (e: retrofit2.HttpException) {
                //println("HTTP Exception: ${e.code()} - ${e.message()}")
                Log.d("WeatherDebug", "HTTP Exception: ${e.code()} - ${e.message()}")
            } catch (e: Exception) {
                // println("General Exception: ${e.message}")
                Log.d("WeatherDebug", "General Exception: ${e.message}")
            }
        }
    }
}

// MainActivity where the UI is set
class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DewyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { WeatherScreen() }
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------

// Composable function to display the weather data
@Composable
fun WeatherScreen(viewModel: WeatherViewModel = viewModel()) {
    val weatherData by viewModel.weatherData.observeAsState()
    // Placed here instead of inside LaunchedEffect as .current must be in a composable function.
    val context = LocalContext.current
    val city = context.getString(R.string.city)

    // Function to convert the returned sunrise and sunset data into a readable time.
    fun convertUnixToTime(unixTime: Long): String {
        val date = Date(unixTime * 1000) // Convert seconds to milliseconds
        val format = SimpleDateFormat("hh:mm a", Locale.getDefault()) // Format as HH:MM AM/PM
        return format.format(date)
    }

    // Function to convert celsius to fahrenheit.
    fun convertCelsiusToFahrenheit(c: Double): Double {
        val f = (c * 9/5 + 32)
        return f
    }


    // Triggers the fetchWeather() function when the application is launched.

    // London for testing
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
                    // Convert API response data to Fahrenheit.
                    text = "${weatherData?.main?.temp?.let { convertCelsiusToFahrenheit(it) }?.toInt() ?: "N/A"}°",
                    style = MaterialTheme.typography.displayMedium.copy(fontSize = 80.sp)
                )
                Spacer(modifier = Modifier.size(4.dp))
                // Convert API response data to Fahrenheit.
                Text("Feels like: ${weatherData?.main?.temp?.let { convertCelsiusToFahrenheit(it) }?.toInt() ?: "N/A"}°")
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
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp, horizontal = 32.dp)) {
            Text("Low: ${weatherData?.main?.tempMin?.toInt()} (Missing on free version? | When using /weather)")
            Text("High: ${weatherData?.main?.tempMax?.toInt()} (Missing on free version? | When using /weather)\"")
            Text("Humidity: ${weatherData?.main?.humidity}%")
            Text("Pressure: ${weatherData?.main?.pressure} hPa")
            Text("Wind Speed: ${weatherData?.wind?.speed?.toInt()} MPH")
            Text("Degree: ${weatherData?.wind?.deg}")
            Text("Gusts: ${weatherData?.wind?.gust?.toInt()} MPH")
            Text("Description: ${weatherData?.weather?.get(0)?.description}")
            Text("Country: ${weatherData?.sys?.country}")
            Text("Sunrise: ${weatherData?.sys?.sunrise?.let { convertUnixToTime(it) } ?: "N/A"}")
            Text("Sunset: ${weatherData?.sys?.sunset?.let { convertUnixToTime(it) } ?: "N/A"}")
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