package com.example.dewy

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/* Fetch data (manages API requests) and store results in LiveData. */
class WeatherViewModel : ViewModel() {
    /* Hold API response. */
    private val _weatherData = MutableLiveData<WeatherData?>()
    /* ASSIGNMENT 4 */
    private val _forecastData = MutableLiveData<ForecastData?>()
    /* UI will observe this to update data. */
    val weatherData: LiveData<WeatherData?> = _weatherData
    /* ASSIGNMENT 4 */
    val forecastData: LiveData<ForecastData?> = _forecastData

    suspend fun fetchWeather(zip: String): Boolean {
        val formattedZip = "$zip,us"
        return try {
            val response = RetrofitClient.instance.getWeather(formattedZip, API_KEY)
            // Log.d("WeatherDebug", "API Response: $response")
            if (response != null) {
                _weatherData.postValue(response)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            /*
            Debugging
            Log.d("WeatherDebug", "General Exception: ${e.message}")
            */
            /* If error, set _weatherData to null. */
            false
        }
    }

    /* ASSIGNMENT 4 */
    suspend fun fetchForecast(zip: String): Boolean {
        val formattedZip = "$zip,us"
        return try {
            Log.d("WeatherViewModel", "Fetching forecast for $zip with API key $API_KEY")
            val response = RetrofitClient.instance.getForecast(formattedZip, API_KEY)
            Log.d("WeatherViewModel", "Forecast API Response: $response")
            _forecastData.postValue(response)
            true
        } catch (e : Exception) {
            Log.e("WeatherViewModel", "General Exception: ${e.message}")
            _forecastData.postValue(null)
            false
        }
    }
}