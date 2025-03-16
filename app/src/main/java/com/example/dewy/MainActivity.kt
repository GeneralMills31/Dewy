package com.example.dewy

// TEST
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

/** GENERAL NOTES:
 *  Icon Set = Kawaii Flat
 *  https://www.flaticon.com/search?style_id=134&author_id=1&word=weather&type=standard
 *
 *  Hard code location using either zip cope, city name, or long/lat pair.
 *
 *  Create a data class to match the JSON data and then fetch and deserialize the data (using
 *  Retrofit and Kotlin).
 *
 *  Create a ViewModel and use LiveData to interact with the data layer.
 */

// Data needed to make an API call.
enum class API_KEY(value: String) {
    OPEN_WEATHER("5bc7c198df5901b12f19005827b097b7")
}

// Data class to represent the JSON response
@Serializable
data class WeatherData(
    @SerialName("main") val main: Main,
    @SerialName("weather") val weather: List<WeatherCondition>
)

@Serializable
data class Main(
    val temp: Double,
    val humidity: Int
)

@Serializable
data class WeatherCondition(
    val description: String,
    val icon: String
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

    fun fetchWeather(city: String, apiKey: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getWeather(city, apiKey)
                _weatherData.postValue(response)
            } catch (e: Exception) {
                _weatherData.postValue(null)
            }
        }
    }
}

// MainActivity where the UI is set
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DewyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WeatherScreen()
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------

// Composable function to display the weather data
@Composable
fun WeatherScreen(viewModel: WeatherViewModel = viewModel()) {
    val weatherData by viewModel.weatherData.observeAsState()

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
                Text(text = stringResource(id = R.string.temp), style = MaterialTheme.typography.displayMedium.copy(fontSize = 80.sp))
                Spacer(modifier = Modifier.size(4.dp))
                Text(text = stringResource(id = R.string.feels_like))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.sunny),
                    contentDescription = stringResource(id = R.string.weather_icon_desc),
                    modifier = Modifier.size(60.dp)
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp, horizontal = 32.dp)
        ) {
            weatherData?.let {
                Text(text = "Temperature: ${it.main.temp}°C")
                Text(text = "Humidity: ${it.main.humidity}%")
                Text(text = "Description: ${it.weather.firstOrNull()?.description ?: "Unknown"}")
            } ?: Text("No data available")
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