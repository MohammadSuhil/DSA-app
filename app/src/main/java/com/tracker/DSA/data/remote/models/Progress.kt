package com.tracker.DSA.data.remote.models

data class Progress(
    val problemId: String = "",
    val isCompleted: Boolean = false,
    val completedAt: Long = 0L
)
