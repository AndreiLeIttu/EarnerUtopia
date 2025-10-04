package com.aospi.earnerutopia.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aospi.earnerutopia.model.PlanStep
import com.aospi.earnerutopia.ui.components.RecommendedPlan
import com.aospi.earnerutopia.ui.theme.Uber
import java.time.LocalTime

@Composable
fun PlanScreen(modifier: Modifier = Modifier, onStart: () -> Unit = {}) {
    val steps = listOf(
        PlanStep(LocalTime.of(9, 0), LocalTime.of(10, 0), "Drive"),
        PlanStep(LocalTime.of(10, 0), LocalTime.of(11, 0), "Break"),
        PlanStep(LocalTime.of(11, 0), LocalTime.of(12, 0), "Drive"),
        PlanStep(LocalTime.of(11, 0), LocalTime.of(12, 0), "Break")
    )

    Box(modifier.fillMaxSize()) {
        RecommendedPlan(
            title = "Recommended plan",
            steps = steps,
            modifier = Modifier.align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        // Button
        Button(
            onClick = onStart,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
                .padding(horizontal = 30.dp)
                .padding(vertical = 10.dp)
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Let's go!",
                fontSize = 20.sp,
                fontFamily = Uber,
                fontWeight = FontWeight.Normal
            )
        }
    }
}