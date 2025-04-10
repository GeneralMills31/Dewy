package com.example.dewy

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

/* Assignment 5 */

class WeatherService : Service() {
    /* Binder given to clients. */
    private val binder = LocalBinder()

    /* Methods for clients here.  */
    /* ...Methods needed for location access/display and notifications... */

    /* Class used for the client Binder. */
    inner class LocalBinder : Binder() {
        /* Return this instance of LocalService so clients can call public methods. */
        fun getService(): WeatherService = this@WeatherService
    }

    /* This runs/is returned when bindService is called in MainActivity! */
    override fun onBind(intent: Intent): IBinder {
        return binder
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