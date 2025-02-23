package com.example.dewy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.dewy.ui.theme.DewyTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DewyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WeatherScreen()
                }
            }
        }
    }
}

@Composable
fun WeatherScreen() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Dewy :)", style = MaterialTheme.typography.headlineMedium)
        Text(text = "St. Paul, MN", style = MaterialTheme.typography.titleMedium)
        Text(text = "72°F", style = MaterialTheme.typography.displayMedium)
        Text(text = "High: 85°F", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Humidity: 100%", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Pressure: 1023 hPa", style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherPreview() {
    DewyTheme {
        WeatherScreen()
    }
}