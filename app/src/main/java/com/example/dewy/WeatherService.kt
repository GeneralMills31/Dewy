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

/* Assignment 5 */

class WeatherService : Service() {
    /* Binder given to clients. */
    private val binder = LocalBinder()
    /* Used to access device location. */
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    /* Coroutine scope for background tasks (like network requests). */
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Log.d("WeatherDebug", "WeatherService created")
        /* Initialize location client. */
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        /* Create notification channel (Required on Android 8+). */
        createNotificationChannel()
    }

    /* Needed to call foreground service and stop from hanging */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("WeatherDebug", "onStartCommand() called")

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

        //fetchLocationSpecificWeather()
        return START_STICKY
    }

    /* This runs/is returned when bindService is called in MainActivity! */
    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    /* Class used for the client Binder. */
    inner class LocalBinder : Binder() {
        /* Return this instance of WeatherService so clients can call public methods. */
        fun getService(): WeatherService = this@WeatherService
    }

    /* Function to get current location and show the weather notification. */
    fun fetchLocationSpecificWeather() {
        Log.d("WeatherDebug", "fetchLocationSpecificWeather() called")
        /* Check if location permission is granted. */
        val permissionApproved = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        Log.d("WeatherDebug", "Location permission approved? $permissionApproved")

        if (permissionApproved) {
            /* Use getCurrentLocation for accurate single fix. */
            val cancellationTokenSource = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                Log.d("WeatherDebug", "getCurrentLocation() success: $location")
                if (location != null) {
                    /* Run weather fetch and notification logic in background. */
                    serviceScope.launch {
                        try {
                            val data = RetrofitClient.instance.getWeatherCoord(
                                location.latitude,
                                location.longitude,
                                API_KEY
                            )
                            showNotification(data)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        } else {
            Log.d("WeatherDebug", "Permission not granted. Stopping service.")
            stopSelf()
        }
    }

    /* Create and display a persistent notification with current weather data. */
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

        Log.d("WeatherDebug", "Updating notification with weather: ${weatherData.name}, ${weatherData.main.temp}")
        Log.d("WeatherDebug", "Calling startForeground() with notification")
        startForeground(notificationID, builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Weather Alerts" // Or getString(R.string.channel_name) | Update externalized Strings!
            val descriptionText = "Live location specific updates."
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        /* Notification constants */
        const val CHANNEL_ID = "weatherChannel"
        const val notificationID = 1
    }

}

/* End Assignment 5 */