package com.aospi.earnerutopia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.aospi.earnerutopia.ui.theme.EarnerUtopiaTheme
import com.aospi.earnerutopia.ui.theme.Uber

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EarnerUtopiaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Rider",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().fillMaxHeight(),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        ) {
        Text(
            text = "Welcome, $name!",
            modifier = modifier,
            fontFamily = Uber,
            fontWeight = FontWeight.Bold,
            fontSize = 40.sp,
        )
        Text(
            fontFamily = Uber,
            fontWeight = FontWeight.Normal,
            text = "Are you ready for a new day?",
            fontSize = 30.sp,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EarnerUtopiaTheme {
        Greeting("Android")
    }
}