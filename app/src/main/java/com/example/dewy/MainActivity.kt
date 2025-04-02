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
 *  .....Continue here.....
 *
 */
/*
Application Behavior:
1) MainActivity launches and calls WeatherScreen().
2A) WeatherScreen initializes the ViewModel, LiveData, and gets the data. LiveData and UI update as a result.
2B) WeatherScreen launches. LiveData and ViewModel are created. LaunchEffect is then triggered which tells the ViewModel to
fetch the data. The Viewmodel calls its fetchWeather() function which utilizes the Retrofit client to make the OpenWeatherMap
API data request. The Retrofit client uses WeatherAPI to define how it gets the data.
JSON data is returned and converted to Kotlin data. Upon return to the ViewModel, if successful,
LiveData (_weatherData) is updated with the new data and the UI changes as a result.
*/

/* API Key */
const val API_KEY = BuildConfig.OPENWEATHER_API_KEY

/* MainActivity where the UI is set. */
class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /* Extend UI to edges of screen. */
        enableEdgeToEdge()
        /* Load WeatherScreen. */
        setContent {
            val navController = rememberNavController()
            DewyTheme {
                /* Setup Navigation */
                NavHost(navController = navController, startDestination = "current") {
                    composable("current") { WeatherScreen(navController = navController) }
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