package com.example.dewy;

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/* Class that handles testing with respect to navigation. */
public class NavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    val testWeatherData = WeatherData(
        main = Main(temp = 65.0, feelsLike = 68.0, tempMin = 60.0, tempMax = 70.0, humidity = 25, pressure = 1024),
        weather = listOf(WeatherCondition(description = "clear sky", icon = "01d")),
        wind = Wind(speed = 3.0, deg = 150, gust = 4.5),
        sys = Sys(country = "US", sunrise = 1591701000, sunset = 1591752000),
        name = "Faribault"
    )

    /* Check if the error message displays properly. */
    @Test
    fun CheckWeatherScreenDisplaysErrorMessage() {
        composeTestRule.setContent {
            val viewModel = WeatherViewModel()
            WeatherScreen(viewModel = viewModel, navController = rememberNavController())
        }
        composeTestRule.onNodeWithTag("ZipInput").performTextClearance()
        composeTestRule.onNodeWithTag("ZipInput").performTextInput("123")
        composeTestRule.onNodeWithTag("MyLocationButton").performClick()
        composeTestRule.onNodeWithText("Zip code must be 5 digits.").assertIsDisplayed()
    }

    /* Test that the view forecast button goes to the forecast screen. */
    @Test
    fun CheckViewForecastButtonGoesToForecastScreen() {
        val navController = TestNavHostController(composeTestRule.activity)
        navController.navigatorProvider.addNavigator(ComposeNavigator())
        val tempViewModel = WeatherViewModel()

        composeTestRule.setContent {
            NavHost(navController = navController, startDestination = "weather") {
                composable("weather") {
                    WeatherScreen(viewModel = tempViewModel, navController = navController)
                }
                composable("forecast/{zip}") {}
            }
        }
        composeTestRule.onNodeWithText("View Forecast").performClick()
        assertTrue(navController.currentDestination?.route?.startsWith("forecast") == true)
    }

    /* Check that the back button works properly. */
    @Test
    fun CheckBackButtonGoesBackToWeatherScreen() {
        val navController = TestNavHostController(composeTestRule.activity)
        navController.navigatorProvider.addNavigator(ComposeNavigator())
        val tempViewModel = WeatherViewModel()
        tempViewModel._weatherData.postValue(testWeatherData)

        composeTestRule.setContent {
            NavHost(navController = navController, startDestination = "weather") {
                composable("weather") {
                    WeatherScreen(viewModel = tempViewModel, navController = navController)
                }
                composable("forecast/{zip}") {
                    ForecastScreen(viewModel = tempViewModel, zip = "55021", navController = navController)
                }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("View Forecast").assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Back to Current Weather").assertIsDisplayed().performClick()
        assertTrue(navController.currentDestination?.route == "weather")
    }
}
