package com.aospi.earnerutopia.ui

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aospi.earnerutopia.ui.components.RecommendedPlan
import com.aospi.earnerutopia.ui.theme.Uber
import com.aospi.earnerutopia.viewmodel.ScheduleViewModel

data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    var fourth: D
)
@Composable
fun PlanScreen(modifier: Modifier = Modifier, onStart: () -> Unit = {}, viewModel: ScheduleViewModel) {
    val optimizedJson = viewModel.optimizedPlanJson

    if (optimizedJson != null) {
        Log.d("successful", optimizedJson.toString())
    }

    val steps = mutableListOf<Quadruple<Int, String, Double, Int>>()
    if (optimizedJson != null) {
        for (boy in optimizedJson) {
            steps.add(Quadruple(
                boy.hour ?: 0,                        // default 0
                boy.activity ?: "Unknown",            // default text
                boy.predicted_earnings ?: 0.0,         // default 0.0
                (boy.hour?.plus(1)) ?: 0
            ))
        }
    }
    var prev:Quadruple<Int,String, Double, Int>
    prev = Quadruple<Int,String,Double,Int>(0,"0",0.0,0)
    var flag = false;
    for(i in 1..<steps.size) {
        var x = steps[i]
        if (flag && prev.second == x.second && prev.fourth.equals(x.first)){
            prev.fourth = x.first + 1
            steps.remove(x)
    } else prev = x
        flag = true;
    }

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