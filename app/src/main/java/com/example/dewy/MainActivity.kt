package com.example.dewy
/* Imports | Ctrl + Alt + O to optimize imports. */
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dewy.ui.theme.DewyTheme

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
 *  _weatherData: Mutable! utilized by ViewModel. Internal.
 *  weatherData: Read-only. Used in the UI. External.
 * weatherData will follow/change with _weatherData but it cannot be changed or altered (read-only).
 *
 *  .....Continue here.....
 *
 */

/*
* TO DO:
* - Externalize all strings and/or define constants for specific values.
* - Clean up additional weather data column (make it a card with a LazyColumn)?
* - Fix ForecastScreen (will refresh/re-run when an inapplicable ZIP is entered).
*/

/* API Key */
const val API_KEY = BuildConfig.OPENWEATHER_API_KEY

/* MainActivity where the UI is set/Entry point of the app.
* ComponentActivity in the base class of JC apps. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /* Extend UI to edges of screen. */
        enableEdgeToEdge()
        /* Set/Load UI/WeatherScreen. */
        setContent {
            /* Setup NavController */
            val navController = rememberNavController()
            /* Wrap UI in app theme */
            DewyTheme {
                /* Define NavHost, controller to use, and starting screen */
                NavHost(navController = navController, startDestination = "weather") {
                    /* Setup main route and pass navController for navigation */
                    composable("weather") { WeatherScreen(navController = navController) }
                    /* Setup forecast route and pass navController */
                    composable("forecast/{zip}") { backStackEntry ->
                        val zip = backStackEntry.arguments?.getString("zip") ?: "55021"
                        ForecastScreen(zip = zip, navController = navController)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherPreview() {
    val navController = rememberNavController()
    DewyTheme {
        WeatherScreen(navController = navController)
    }
}