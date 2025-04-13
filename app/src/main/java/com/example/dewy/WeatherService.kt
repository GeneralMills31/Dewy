package com.example.dewy

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log

class WeatherService : Service() {
    /* Binder given to clients | Is returned to clients (bindService()) */
    private val binder = LocalBinder()
    /* Used to access device location. */
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    /* Coroutine scope for background tasks | Doesn't stop main thread. */
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    /* Initializes service when it is first created. */
    override fun onCreate() {
        super.onCreate()
        // Log.d("WeatherDebug", "WeatherService created")
        /* Start location client. */
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        /* Create notification channel. */
        createNotificationChannel()
    }

    /* Called when startForegroundService is called (Utilities)
    *  Creates a temp notification to comply with Androids requirement that foreground
    *  services must show a notification within 5 seconds. If this isn't done, the program
    *  will have issues. */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Log.d("WeatherDebug", "onStartCommand() called")

        /* Start foreground with a temp notification to keep app from crashing.
        * startForegroundService() requires that startForeground() be called within 5 seconds.
        * getCurrentLocation() is slower and can cause the app to never reach it in time. Use this
        * to keep the app from failing and just simply update the notification later. */
        val placeholderNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Fetching weather data...")
            .setContentText("Getting your current location")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(notificationID, placeholderNotification)

        return START_STICKY
    }

    /* Called when a client binds to the service. This runs/is returned when
       bindService is called in MainActivity! */
    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    /* Class used for the client Binder. */
    inner class LocalBinder : Binder() {
        /* Returns this instance of WeatherService so clients can then call public methods. */
        fun getService(): WeatherService = this@WeatherService
    }

    /* How getCurrentLocation works and how the callback functions work:
    *  fun getCurrentLocation(callback: (Double, Double) -> Unit) Based on this definition,
    *  it doesn't know exactly what to do with the lat/lon values, it just knows to pass them
    *  to whatever callback the caller supplied. EX: In MainActivity, it uses mBound (a WeatherService)
    *  to call getCurrentLocation. It passes in a lambda function as the callback ({lat, lon -> ...})
    *  which the service will be responsible for passing those values into. In this case, it will
    *  take the resulting lat/lon values and send them to the viewmodel to update the data on the screen.
    *  It essentially says (In MainActivity), get my current location (mbound.getCurrentLocation call)
    *  and pass the values it generates into this lambda ({lat, lon -> ...}) which will call the viewmodel.*/

    /* Function to get current location and show the weather notification. */
    fun getCurrentLocation(callback: (Double, Double) -> Unit) {
        // Log.d("WeatherDebug", "getCurrentLocation() called")
        /* Check if location permission is granted. */
        val permissionApproved = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        // Log.d("WeatherDebug", "Location permission approved: $permissionApproved")

        /* If permission is granted, use getCurrentLocation to get some location data and pass that
           data into the callback function and use that data to update the notification as well. */
        if (permissionApproved) {
            /* Use getCurrentLocation for a single/accurate location fix. */
            val cancellationTokenSource = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                // Log.d("WeatherDebug", "getCurrentLocation() success: $location")
                if (location != null) {
                    /* Gets the lat/lon values and pass them into the callback. */
                    val lat = location.latitude
                    val lon = location.longitude
                    callback(lat, lon)

                    /* Fetch weather and update notification. */
                    serviceScope.launch {
                        try {
                            // Log.d("WeatherDebug", "API key being used: ${BuildConfig.OPENWEATHER_API_KEY}")
                            // Log.d("WeatherDebug", "Making coord weather call with lat=$lat, lon=$lon")
                            val weatherData = RetrofitClient.instance.getWeatherCoord(lat, lon, BuildConfig.OPENWEATHER_API_KEY)
                            showNotification(weatherData)
                        } catch (e: Exception) {
                            // Log.e("WeatherDebug", "Failed to fetch weather for notification: ${e.message}")
                        }
                    }
                } else {
                    // Log.d("WeatherDebug", "Location is null")
                }
            }.addOnFailureListener {
                // Log.d("WeatherDebug", "Failed to grab location ${it.message}")
            }
        } else {
            // Log.d("WeatherDebug", "Permission not granted. Stopping service.")
            return
        }
    }

    /* Create and display a persistent notification with CURRENT weather data.
    *  Will build and display the real notification based on the API response.  */
    private fun showNotification(weatherData: WeatherData) {
        /* Intent to open app when user taps the notification. */
        val intent = Intent(this, MainActivity::class.java).apply {flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK}
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        /* Notification builder with basic weather info. */
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(getLocalIcon(weatherData.weather.firstOrNull()?.icon)) // Put your icon here!
            .setContentTitle("${weatherData.name}: ${weatherData.main.temp.toInt()}°")
            .setContentText(weatherData.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() }
                ?: "N/A",)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Log.d("WeatherDebug", "Updating notification with weather: ${weatherData.name}, ${weatherData.main.temp}")
        // Log.d("WeatherDebug", "Calling startForeground() with notification")
        startForeground(notificationID, builder.build())
    }

    /* Channel used to show persistent notification. */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Weather Updates"
            val descriptionText = "Live Location Updates"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /* Used for the notification channel/notification. */
    companion object {
        /* Notification constants */
        const val CHANNEL_ID = "weatherChannel"
        const val notificationID = 1
    }
}