package com.example.dewy

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

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

    fun fetchWeather(zip: String) {
        /* Means network call is run asynchronously. */
        val formattedZip = "$zip,us"
        viewModelScope.launch {
            try {
                /*
                Debugging
                Log.d("WeatherDebug", "API Key: $API_KEY")
                */
                val response = RetrofitClient.instance.getWeather(formattedZip, API_KEY)
                /*
                Debugging
                Log.d("WeatherDebug", "API Response: $response")
                */
                /* Updates weather data with data received from OpenWeatherMap API request. */
                _weatherData.postValue(response)
            } catch (e: retrofit2.HttpException) {
                /*
                Debugging
                Log.d("WeatherDebug", "HTTP Exception: ${e.code()} - ${e.message()}")
                */
            } catch (e: Exception) {
                /*
                Debugging
                Log.d("WeatherDebug", "General Exception: ${e.message}")
                */
                /* If error, set _weatherData to null. */
                _weatherData.postValue(null)
            }
        }
    }

    /* ASSIGNMENT 4 */
    fun fetchForecast(zip: String) {
        val formattedZip = "$zip,us"
        viewModelScope.launch {
            try {
                Log.d("WeatherViewModel", "Fetching forecast for $zip with API key $API_KEY")
                val response = RetrofitClient.instance.getForecast(formattedZip, API_KEY)
                Log.d("WeatherViewModel", "Forecast API Response: $response")
                _forecastData.postValue(response)
            } catch (e : retrofit2.HttpException) {
                Log.e("WeatherViewModel", "HTTP Exception: ${e.code()} - ${e.message()}")
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("WeatherViewModel", "Error body: $errorBody")
            } catch (e : Exception) {
                Log.e("WeatherViewModel", "General Exception: ${e.message}")
                _forecastData.postValue(null)
            }
        }
    }
}