package com.aospi.earnerutopia.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScheduleViewModel : ViewModel() {

    // Holds intervals as list of (start, end)
    private val _steps = MutableStateFlow<List<Pair<Int, Int>>>(emptyList())
    val steps = _steps.asStateFlow()

    fun addStep(start: Int, end: Int) {
        _steps.value += (start to end)
    }

    fun clearSteps() {
        _steps.value = emptyList()
    }
}
