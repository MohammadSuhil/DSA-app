package com.tracker.DSA.data.remote.models

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val progressPercentage: Int = 0,
    val completedCount: Int = 0,
    val lastCompletionTimestamp: Long = 0L
)
