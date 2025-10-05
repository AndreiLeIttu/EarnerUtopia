package com.aospi.earnerutopia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aospi.earnerutopia.model.PlanStep
import com.aospi.earnerutopia.ui.Quadruple
import com.aospi.earnerutopia.ui.theme.Uber
import java.time.format.DateTimeFormatter

private val timeFmtShow = DateTimeFormatter.ofPattern("H:mm")

@Composable
fun TimelineRow(index: Int, step: Quadruple<Int, String, Double, Int>, isLast: Boolean) {
    Row(Modifier.height(IntrinsicSize.Min).fillMaxWidth()) {
        Box(Modifier.width(32.dp).fillMaxHeight()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(28.dp)
                                   .background(Color.White, CircleShape)
                                   .align(Alignment.TopCenter)
            ) {
                Text(
                    index.toString(),
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontFamily = Uber,
                    fontWeight = FontWeight.Normal
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = 28.dp)
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(Color.White)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "${step.first}:00 - ${step.fourth}:00",
                fontSize = 20.sp,
                fontFamily = Uber,
                fontWeight = FontWeight.Normal
            )
            Text(
                step.second,
                fontSize = 24.sp,
                fontFamily = Uber,
                fontWeight = FontWeight.Normal,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}