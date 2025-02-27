package com.example.dewy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** GENERAL NOTES:
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp)
    ) {
        // DEWY ROW
        Row(
            modifier = Modifier
                .background(Color.LightGray)
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 14.dp)
        ) {
            Text(text = "Dewy :)", style = MaterialTheme.typography.headlineSmall)
        }
        // CITY ROW
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "St. Paul, MN", style = MaterialTheme.typography.headlineSmall)
        }
        // ICON ROW
        Row(
            // Align data in center of row vertically and horizontally.
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
            ) {
            Column(
                // In the first column, center the data horizontally.
                // Also, provide each column an equal weight.
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "72°", style = MaterialTheme.typography.displayMedium.copy(fontSize = 80.sp))
                Spacer(modifier = Modifier.size(4.dp))
                Text(text = "Feels like 78°")
            }
            Column(
                // In the first column, center the data horizontally.
                // Also, provide each column an equal weight.
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Our icon
                Image(
                    painter = painterResource(id = R.drawable.sunny),
                    contentDescription = "Current Weather Icon",
                    modifier = Modifier.size(60.dp)
                )
            }
        }
        // ROWS FOR HIGH, LOW, HUMIDITY, ETC.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp, horizontal = 32.dp)
        ) {
            Text(
                text = "Low 65°F",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(vertical = 2.dp)
            )
            Text(
                text = "High 80°F",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(vertical = 2.dp)
            )
            Text(
                text = "Humidity 100%",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(vertical = 2.dp)
                )
            Text(
                text = "Pressure 1023 hPa",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(vertical = 2.dp)
            )
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