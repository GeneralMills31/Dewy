package com.example.dewy
/* Imports | Ctrl + Alt + O to optimize imports. */
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dewy.ui.theme.DewyTheme
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

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
* - Move location button next to the ZIP field!
* - Consider refactoring code a bit. Move some work into Utilities if able.
*/

/*
* Steps (Bind from client):
* 1) Implement ServiceConnection (Override onServiceConnected/onServiceDisconnected).
* 2) Call bindService (passing ServiceConnection implementation).
* 3) When the onServiceConnected callback method is called, you can begin making calls to the service
*    using the methods defined by the interface!
*
*
* Bind to the Service (WeatherService) by calling bindService()! The Android system then calls the
* services onBind() method, which will return an IBinder for INTERACTING WITH THE SERVICE.
* To receive the IBinder, the client (activities, services, content providers, etc.) must create
* an instance of ServiceConnection and pass it to bindService().
*
* From the SERVER:
* 1)
*
* When the system calls your onServiceConnected() callback method, you can begin making calls to the
* service, using the methods defined by the interface.
*
* QUESTIONS:
* 1) Do we have to pass anything from MainActivity to other files? Is the order correct here?
* 2) What should I put in the interface (functions)? How to call them? (Notification? Location?)
* 3) getLastLocation or getCurrentLocation OR requestLocationUpdates?
*
* NOTES:
* 1) Declare this service in the manifest!
* 2) Best to have the connection done here since the service is app wide!
* 3) Do we use mService or connection for service function calls?
* 4) Check all dependencies after implementing code!
* 5) Service doesn't really ask for permission. It checks if it has permission, but it won't
* request it itself. The composable should be the one to get permission!
*/

/* OpenWeather API Key */
const val API_KEY = BuildConfig.OPENWEATHER_API_KEY

/* MainActivity where the UI is set/Entry point of the app.
* ComponentActivity in the base class of JC apps. */
class MainActivity : ComponentActivity() {

    /* ServiceConnection Implementation! */

    /* Reference to bound service. */
    private lateinit var mService: WeatherService
    /* Indicates whether activity is bound or not. */
    private var mBound: Boolean = false
    /* Used in the situation where fetchLocationAndUpdateWeather is called before the binding
    *  is complete. Essentially, allows data fetching to continue after binding is complete and
    *  avoid issues the app can run into if the service is called/referenced before it is bound. */
    private var pendingLocationFetch = false
    /* Reference to the WeatherViewModel */
    private lateinit var viewModel: WeatherViewModel

    /* Function used to bind MainActivity to WeatherService. Must override onServiceConnected
       and onServiceDisconnected. */
    private val connection = object : ServiceConnection {
        /* When the system calls your onServiceConnected() callback method, you can begin making
           calls to the service, using the methods defined by the interface! Triggered when the
           service is connected. */
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as WeatherService.LocalBinder
            mService = binder.getService()
            mBound = true

            if (pendingLocationFetch) {
                // Log.d("WeatherDebug", "Pending fetch detected. Fetching now.")
                fetchLocationAndUpdateWeather()
                pendingLocationFetch = false
            }
        }
        /* Triggered when the service is disconnected. */
        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    /* this is the entry point of the app. Sets up the ViewModel, theme, and Compose navigation. */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[WeatherViewModel::class.java]
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
                    composable("weather") { WeatherScreen(viewModel = viewModel, navController = navController) }
                    /* Setup forecast route and pass navController */
                    composable("forecast/{zip}") { backStackEntry ->
                        val zip = backStackEntry.arguments?.getString("zip") ?: "55021"
                        ForecastScreen(zip = zip, navController = navController)
                    }
                }
            }
        }
    }

    /* This is the binding! Call bindService and pass the ServiceConnection implementation */
    override fun onStart() {
        super.onStart()
        /* Bind to WeatherService. Connection is the instance
         of ServiceConnection that receives the IBinder. */
        Intent(this, WeatherService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    /* Function to unbind from the service. */
    override fun onStop() {
        super.onStop()
        unbindService(connection)
        mBound = false
    }

    /* Called from WeatherScreen when the user clicks location button. If everything is bound and
    *  good it will request location information and then pass that into the viewmodel to get location
    *  specific weather data. Sets the pendingLocationFetch to true if not yet bound so it may attempt
    *  to do so again after binding. */
    fun fetchLocationAndUpdateWeather() {
        if (mBound) {
            mService.getCurrentLocation { lat, lon ->
                // Log.d("WeatherDebug", "Calling ViewModel to fetch weather from: $lat, $lon")
                lifecycleScope.launch {
                    viewModel.fetchWeatherByCoordinates(lat, lon)
                }
            }
        } else {
            // Log.d("WeatherDebug", "Service not bound yet.")
            pendingLocationFetch = true
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Preview(showBackground = true)
@Composable
fun WeatherPreview() {
    val navController = rememberNavController()
    DewyTheme {
        WeatherScreen(navController = navController)
    }
}