package com.stopwatch.app.model

data class LapTime(
    val lapNumber: Int,
    val totalTime: Long,  // Total elapsed time at this lap in milliseconds
    val lapDuration: Long // Duration of this lap in milliseconds
)
