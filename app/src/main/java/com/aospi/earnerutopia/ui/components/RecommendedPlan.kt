package com.aospi.earnerutopia.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aospi.earnerutopia.model.PlanStep
import com.aospi.earnerutopia.ui.theme.Uber

@Composable
fun RecommendedPlan(title: String, steps: List<PlanStep>, modifier: Modifier = Modifier) {
    Column(modifier.padding(horizontal = 16.dp)) {
        Text(
            title,
            fontSize = 35.sp,
            fontFamily = Uber,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 40.dp)
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.fillMaxWidth()) {
            itemsIndexed(steps) { index, step ->
                TimelineRow(index = index + 1, step = step, isLast = index == steps.lastIndex)
            }
        }
    }
}