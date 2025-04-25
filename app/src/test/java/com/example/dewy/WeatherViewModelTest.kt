package com.example.dewy

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/* Class that tests view model functionality. */
class WeatherViewModelTest {

    /* Need this for LiveData (Used for synchronicity). */
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    /* Use StandardTestDispatcher for better control. */
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: WeatherViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = WeatherViewModel()
    }

    /* Check that the view model fetches data properly. */
    @Test
    fun FetchWeatherViewModelTest() = runTest(testDispatcher) {
        val zip = "55021"
        val result = viewModel.fetchWeather(zip)
        testDispatcher.scheduler.advanceUntilIdle()
        /* Check if the function returned true. */
        assertTrue(result)
        /* Verify the function has data. */
        val data = viewModel.weatherData.value
        assertEquals("Faribault",data?.name)
    }

    /* Check that the view model fetches forecast data properly. */
    @Test
    fun FetchForecastViewModelTest() = runTest(testDispatcher) {
        val zip = "55021"
        val result = viewModel.fetchForecast(zip)
        testDispatcher.scheduler.advanceUntilIdle()
        /* Check if the function returned true. */
        assertTrue(result)
        /* Verify the function has data. */
        assertEquals(16, viewModel.forecastData.value?.list?.size)
    }

    /* Check that the view model fetches weather data by coordinates properly. */
    @Test
    fun FetchWeatherByCoordViewModelTest() = runTest(testDispatcher) {
        val lat = 44.2950
        val lon = -93.2688
        val result = viewModel.fetchWeatherByCoordinates(lat,lon)
        testDispatcher.scheduler.advanceUntilIdle()
        /* Check if the function returned true. */
        assertTrue(result)
        /* Verify the function has data. */
        val data = viewModel.weatherData.value
        assertEquals("Faribault", data?.name)
    }

}