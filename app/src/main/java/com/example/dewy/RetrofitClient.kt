package com.example.dewy

import android.util.Log
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/* Creates an instance (singleton) of Retrofit. */
object RetrofitClient {
    /* Root URL for all API requests. */
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)
            val responseBody = response.peekBody(Long.MAX_VALUE)
            Log.d("API_RESPONSE", responseBody.string())
            response
        }
        .build()

    /* Initialize Retrofit only when accessed (by lazy). */
    val instance: WeatherApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            /* Converts JSON responses into Kotlin objects. */
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }
}