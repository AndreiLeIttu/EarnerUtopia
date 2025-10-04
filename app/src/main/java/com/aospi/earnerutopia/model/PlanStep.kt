package com.aospi.earnerutopia.model

import java.time.LocalTime
data class PlanStep(
    val start: LocalTime,
    val end: LocalTime,
    val title: String
)