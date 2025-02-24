package com.example.dewy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.dewy.ui.theme.DewyTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

/**
 * GENERAL NOTES:
 *  Icon Set = Kawaii Flat
 *  https://www.flaticon.com/search?style_id=134&author_id=1&word=weather&type=standard
 */

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
        Row(modifier = Modifier.fillMaxWidth()){
            Text(text = "Dewy :)", style = MaterialTheme.typography.headlineMedium)
        }
        Row(modifier = Modifier.fillMaxWidth()){
            Text(text = "St. Paul, MN", style = MaterialTheme.typography.titleMedium)
        }
        // This is the row that needs to have a temp (72), "Feels like...", and an icon!
        // NOTE: We want to use the tool the Instructor referenced that would place both objects
        // (text lines and the icon) equally apart and at the ends.
        Row(modifier = Modifier.fillMaxWidth()) {
            // Column for the first two lines of text.
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "72°", style = MaterialTheme.typography.displayMedium)
                // Put some space between the two text lines.
                Spacer(modifier = Modifier.size(4.dp))
                Text(text = "Feels like 78°")
            }
            // Split up the text and icon.
            Spacer(modifier = Modifier.size(48.dp))
            // Image for the icon we wish to display.
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.sunny),
                    contentDescription = "Current Weather Icon",
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()){
            Text(text = "Low: 65°F", style = MaterialTheme.typography.bodyLarge)
        }
        Row(modifier = Modifier.fillMaxWidth()){
            Text(text = "High: 80°F", style = MaterialTheme.typography.bodyLarge)
        }
        Row(modifier = Modifier.fillMaxWidth()){
            Text(text = "Humidity: 100%", style = MaterialTheme.typography.bodyLarge)
        }
        Row(modifier = Modifier.fillMaxWidth()){
            Text(text = "Pressure: 1023 hPa", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherPreview() {
    DewyTheme {
        WeatherScreen()
    }
}