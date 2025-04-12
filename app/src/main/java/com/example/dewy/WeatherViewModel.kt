package com.example.dewy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

/* Fetch data (manages API requests) and store results in LiveData. */
class WeatherViewModel : ViewModel() {
    /* Define LiveData variables to hold API response and that ViewModel can change. */
    private val _weatherData = MutableLiveData<WeatherData?>()
    private val _forecastData = MutableLiveData<ForecastData?>()
    /* UI will observe this to update data. Read-only reference to _weatherData; updates with _weatherData. */
    val weatherData: LiveData<WeatherData?> = _weatherData
    val forecastData: LiveData<ForecastData?> = _forecastData

    /* Fetch weather from OpenWeather. API/network requests done through Retrofit.
    Functions utilize suspend to safeguard main/UI thread. */
    suspend fun fetchWeather(zip: String): Boolean {
        /* Make all ZIP codes US-based. Allows user to just enter a 5 digit code and for it to work. */
        val formattedZip = "$zip,us"
        /* Utilizing boolean values to handle null/non-null API responses. Allows the weatherScreen
        * to behave appropriately (EX: If valid, but non-applicable, ZIP is entered, the UI will not update. */
        return try {
            val response = RetrofitClient.instance.getWeather(formattedZip, API_KEY)
            if (response != null) {
                _weatherData.postValue(response)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /* Fetch forecast data from OpenWeather. */
    suspend fun fetchForecast(zip: String): Boolean {
        val formattedZip = "$zip,us"
        return try {
            val response = RetrofitClient.instance.getForecast(formattedZip, API_KEY)
            _forecastData.postValue(response)
            true
        } catch (e : Exception) {
            _forecastData.postValue(null)
            false
        }
    }

    /* Get weather data by coordinates | Used for specific location */
    suspend fun fetchWeatherByCoordinates(lat: Double, lon: Double) {
        return try {
            val response = RetrofitClient.instance.getWeatherCoord(lat, lon, API_KEY)
            _weatherData.postValue(response)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}