package com.aospi.earnerutopia.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aospi.earnerutopia.viewmodel.ScheduleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    modifier: Modifier,
    onNextButtonClicked: () -> Unit,
    viewModel: ScheduleViewModel
    ) {
    val steps by viewModel.steps.collectAsState()

    // Dialog visibility control
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    // Temp state for current interval being selected
    var currentStart by remember { mutableIntStateOf(0) }
    var currentEnd by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Display all intervals
        steps.forEachIndexed { index, (start, end) ->
            Text("Interval ${index + 1}: $start - $end")
        }

        Spacer(Modifier.height(24.dp))

        if (showStartPicker) {
            val state = rememberTimePickerState(is24Hour = true)
            Column {
                TimeInput(
                    state = state,
                )
                Button(onClick = { showStartPicker = false }) {
                    Text("Dismiss picker")
                }
                Button(onClick = {
                    currentStart = state.hour
                    showStartPicker = false
                    showEndPicker = true
                }) {
                    Text("Confirm selection")
                }
            }
        }

        if (showEndPicker) {
            val state = rememberTimePickerState(is24Hour = true)
            Column {
                TimeInput(
                    state = state,
                )
                Button(onClick = { showEndPicker = false }) {
                    Text("Cancel")
                }
                Button(onClick = {
                    currentEnd = state.hour
                    showEndPicker = false
                    viewModel.addStep(currentStart, currentEnd)
                }) {
                    Text("Confirm selection")
                }
            }
        }

        Button(onClick = { showStartPicker = true }) {
            Text("New interval")
        }

        Spacer(Modifier.height(12.dp))

        Button(onClick = onNextButtonClicked) {
            Text("Go to plan")
        }
    }
}