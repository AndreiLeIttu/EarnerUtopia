package com.aospi.earnerutopia.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.aospi.earnerutopia.ui.theme.Uber

@Composable
fun StartScreen(
    modifier: Modifier = Modifier,
    name: String,
) {
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