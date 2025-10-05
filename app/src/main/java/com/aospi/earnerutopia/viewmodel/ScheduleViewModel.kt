package com.aospi.earnerutopia.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.aospi.earnerutopia.network.ScheduleItem

class ScheduleViewModel : ViewModel() {

    // Holds intervals as list of (start, end)
    var optimizedPlanJson by mutableStateOf<List<ScheduleItem>?>(null)
}
