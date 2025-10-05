package com.aospi.earnerutopia.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aospi.earnerutopia.network.ApiClient
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.aospi.earnerutopia.ui.theme.Uber
import androidx.compose.ui.window.Dialog

import com.aospi.earnerutopia.viewmodel.ScheduleViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HourOnlyTimeInput(
    state: TimePickerState,
    modifier: Modifier = Modifier
) {
    // Keep minutes locked at 00
    LaunchedEffect(state.minute) {
        if (state.minute != 0) state.minute = 0
    }

    Box(
        modifier = modifier
            .width(180.dp)   // 👈 adjust this to control width
            .height(100.dp), // 👈 adjust this to control height
        contentAlignment = Alignment.Center
    ) {
        TimeInput(
            state = state,
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    modifier: Modifier,
    onNextButtonClicked: () -> Unit,
    viewModel: ScheduleViewModel
    ) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val steps = remember { mutableStateListOf<Pair<Int, Int>>() }

    // Dialog visibility control
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    // Temp state for current interval being selected
    var currentStart by remember { mutableIntStateOf(0) }
    var currentEnd by remember { mutableIntStateOf(0) }
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            "Let's plan your day!",
            fontSize = 35.sp,
            fontFamily = Uber,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 40.dp).padding(horizontal = 32.dp)
        )
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Display all intervals
//        steps.forEachIndexed { index, (start, end) ->
//            Text("Interval ${index + 1}: $start - $end")
//        }

            // Display all intervals (cleaner layout)
            if (steps.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    steps.forEachIndexed { index, (start, end) ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Interval ${index + 1}",
                                    fontFamily = Uber,
                                    fontWeight = FontWeight.Normal
                                )
                                Text(
                                    text = "%02d:00 - %02d:00".format(start, end),
                                    fontFamily = Uber,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }


            Spacer(Modifier.height(24.dp))

            if (showStartPicker) {
                Dialog(onDismissRequest = { showStartPicker = false }) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        tonalElevation = 6.dp,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        val state = rememberTimePickerState(is24Hour = true)
                        Column(
                            modifier = Modifier.padding(20.dp).widthIn(min = 250.dp, max = 320.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Choose a starting time!",
                                color = Color.Black,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = Uber
                                )
                            )

                            Spacer(Modifier.height(10.dp))

                            TimeInput(
                                state = state,
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 20.dp)
                            ) {
                                IconButton(
                                    onClick = { showStartPicker = false }, modifier = Modifier
                                        .size(40.dp)
                                        .background(Color.White, RoundedCornerShape(16))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cancel",
                                        tint = Color.Black
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        currentStart = state.hour
                                        showStartPicker = false
                                        showEndPicker = true
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            Color.White,
                                            RoundedCornerShape(16)
                                        ) // light green
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Confirm",
                                        tint = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showEndPicker) {
                Dialog(onDismissRequest = { showStartPicker = false }) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        tonalElevation = 6.dp,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        val state = rememberTimePickerState(is24Hour = true)
                        Column(
                            modifier = Modifier.padding(20.dp).widthIn(min = 250.dp, max = 320.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Choose an end time!",
                                color = Color.Black,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = Uber
                                )
                            )

                            Spacer(Modifier.height(10.dp))

                            TimeInput(
                                state = state,
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 20.dp)
                            ) {
                                IconButton(
                                    onClick = { showEndPicker = false }, modifier = Modifier
                                        .size(40.dp)
                                        .background(Color.White, RoundedCornerShape(16))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cancel",
                                        tint = Color.Black
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        currentEnd = state.hour
                                        showEndPicker = false
                                        steps.add(Pair(currentStart, currentEnd))
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            Color.White,
                                            RoundedCornerShape(16)
                                        ) // light green
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Confirm",
                                        tint = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }

            IconButton(
                onClick = { showStartPicker = true },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add new interval",
                    tint = Color.Black
                )
            }

            Spacer(Modifier.height(12.dp))
        }
        Button(
            onClick = {
                val actualSteps = mutableListOf<Int>()
                for ((start, end) in steps) {
                    if (end > start + 1) {
                        for (i in start..<end) actualSteps.add(i)
                    } else {
                        actualSteps.add(start)
                    }
                }

                loading = true
                errorMessage = null

                scope.launch {
                    try {
                        val response = ApiClient.apiService.optimizeSchedule(
                            com.aospi.earnerutopia.network.OptimizeRequest(actualSteps)
                        )
                        if (response.isSuccessful) {
                            val result = response.body()
                            viewModel.optimizedPlanJson = result
                            onNextButtonClicked()
                        } else {
                            errorMessage = "Error: ${response.code()}"
                            Log.d("error-message", errorMessage!!)
                        }
                    } catch (e: Exception) {
                        errorMessage = "Failed: ${e.message}"
                        Log.d("exception", errorMessage!!)
                    } finally {
                        loading = false
                    }
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
                .padding(horizontal = 30.dp)
                .padding(vertical = 10.dp)
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
        ) { Text("Generate my plan!", fontWeight = FontWeight.Normal, fontFamily = Uber, fontSize = 20.sp) }
    }
}