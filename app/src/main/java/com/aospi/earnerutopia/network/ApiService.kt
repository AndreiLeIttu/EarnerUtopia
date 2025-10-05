package com.aospi.earnerutopia.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// The request model for /optimize
data class OptimizeRequest(
    val available_hours: List<Int>
)

// The response model (same shape as your Python schedule output)
data class ScheduleItem(
    val hour: Int?,
    val activity: String?,
    val predicted_earnings: Double?
)

interface ApiService {
    @POST("/optimize")
    suspend fun optimizeSchedule(
        @Body request: OptimizeRequest
    ): Response<List<ScheduleItem>>
}
