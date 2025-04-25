package com.example.dewy

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/* Test class that holds all tests related to Forecast Screen data minus navigation. */
class ForecastScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()
    private lateinit var viewModel: WeatherViewModel

    val testForecastData = ForecastData(
        city = City(name = "Faribault", country = "US"),
        cnt = 2,
        list = listOf(
            DailyForecast(
                dt = 1371901100,
                temp = Temperature(day = 65.0, min = 60.0, max = 70.0),
                weather = listOf(WeatherCondition(description = "Partly cloudy", icon = "02d"))
            ),
            DailyForecast(
                dt = 1391682400,
                temp = Temperature(day = 70.0, min = 65.0, max = 75.0),
                weather = listOf(WeatherCondition(description = "Light rain", icon = "10d"))
            )
        )
    )

    /* Setup function that handles viewmodel creation and content setup so it doesn't need to be
    *  repeated. */
    @Before
    fun setup() {
        val tempViewModel = WeatherViewModel()
        tempViewModel._forecastData.postValue(testForecastData)
        composeTestRule.setContent { ForecastScreen(viewModel = tempViewModel, zip = "55021", navController = rememberNavController()) }
    }

    /* Test if the Forecast Screen displays data. */
    @Test
    fun CheckForecastScreenDisplaysData() {
        composeTestRule.onNodeWithText("60° / 70°").assertExists()
    }

    /* Check that icons are displayed. */
    @Test
    fun CheckForecastIconsAreDisplayed() {
        composeTestRule.onAllNodesWithTag("ForecastIcon").assertCountEquals(2)
    }
}