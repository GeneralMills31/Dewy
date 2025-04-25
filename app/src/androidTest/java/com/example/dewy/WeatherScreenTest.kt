package com.example.dewy

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertAny
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/* Class that handles Weather Screen functionality minus navigation. */
class WeatherScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()
    private lateinit var viewModel: WeatherViewModel

    val testWeatherData = WeatherData(
        main = Main(temp = 65.0, feelsLike = 68.0, tempMin = 60.0, tempMax = 70.0, humidity = 25, pressure = 1024),
        weather = listOf(WeatherCondition(description = "clear sky", icon = "01d")),
        wind = Wind(speed = 3.0, deg = 150, gust = 4.5),
        sys = Sys(country = "US", sunrise = 1591701000, sunset = 1591752000),
        name = "Faribault"
    )
    val testForecastData = ForecastData(
        city = City(name = "Faribault", country = "US"),
        cnt = 7,
        list = List(7) {
            DailyForecast(
                dt = 1591800000,
                temp = Temperature(day = 70.0, min = 60.0, max = 75.0),
                weather = listOf(WeatherCondition(description = "Partly cloudy", icon = "02d"))
            )
        }
    )

    /* Before function to limit repeated work. */
    @Before
    fun setup() {
        viewModel = WeatherViewModel()
        viewModel._weatherData.postValue(testWeatherData)
        viewModel._forecastData.postValue(testForecastData)

        composeTestRule.setContent {
            WeatherScreen(
                viewModel = viewModel,
                navController = rememberNavController()
            )
        }
    }

    /* Check that zip input exists and is editable. */
    @Test
    fun ZipInputExistsAndIsEditable() {
        composeTestRule.onNodeWithTag("ZipInput")
            .assertExists()
            .performTextInput("55021")
    }

    /* Check that the get weather button works and is clickable. */
    @Test
    fun GetWeatherButtonExistsAndIsClickable() {
        composeTestRule.onNodeWithTag("GetWeatherButton")
            .assertExists()
            .assertHasClickAction()
    }

    /* Check that the my location button exists and is clickable. */
    @Test
    fun LocationButtonExistsAndIsClickable() {
        composeTestRule.onNodeWithTag("MyLocationButton")
            .assertExists()
            .assertHasClickAction()
    }

    /* Check that the weather screen displays city and temp data properly. */
    @Test
    fun WeatherScreenDisplaysCityAndTemperature() {
        composeTestRule.onNodeWithText("Faribault").assertIsDisplayed()
        composeTestRule.onNodeWithText("65°").assertExists()
    }

    /* Check that the weather screen displays description data properly. */
    @Test
    fun WeatherScreenDisplaysDescriptionAndIcon() {
        composeTestRule.onNodeWithText("clear sky", ignoreCase = true).assertExists()
        composeTestRule.onNodeWithTag("WeatherIcon").assertExists()
    }

    /* Check that the weather screen displays forecast cards properly. */
    @Test
    fun ForecastCardsAreDisplayed() {
        composeTestRule.onAllNodesWithTag("ForecastCard").assertAny(hasTestTag("ForecastCard"))
    }
}