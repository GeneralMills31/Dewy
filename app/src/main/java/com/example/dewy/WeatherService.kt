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
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

/* Assignment 5 */

class WeatherService : Service() {
    /* Binder given to clients. */
    private val binder = LocalBinder()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    /* Methods for clients here.  */
    /* ...Methods needed for location access/display and notifications... */

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    /* This runs/is returned when bindService is called in MainActivity! */
    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    /* Class used for the client Binder. */
    inner class LocalBinder : Binder() {
        /* Return this instance of LocalService so clients can call public methods. */
        fun getService(): WeatherService = this@WeatherService
        // Location function call here?
    }

    // ...


    private fun showNotification() {

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        var builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.notificationIcon) // Put your icon here!
            .setContentTitle(testTitle) // Put your title here!
            .setContentText(textContent) // Put your desc here!
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(notificationId, builder.build())
        }

    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

// From the Android Studio documentation. Maybe it can be a good reference for later?
//    /** Called when a button is clicked (the button in the layout file attaches to
//     * this method with the android:onClick attribute).  */
//    fun onButtonClick(v: View) {
//        if (mBound) {
//            // Call a method from the LocalService.
//            // However, if this call is something that might hang, then put this request
//            // in a separate thread to avoid slowing down the activity performance.
//            val num: Int = mService.randomNumber
//            Toast.makeText(this, "number: $num", Toast.LENGTH_SHORT).show()
//        }
//    }

/* End Assignment 5 */