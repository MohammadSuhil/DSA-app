package com.tracker.DSA.data.remote.models

data class Community(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val adminUid: String = "",
    val members: List<String> = emptyList(),
    val createdAt: Long = 0L
)
