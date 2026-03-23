package com.tracker.DSA.data.remote.models

data class Chat(
    val id: String = "",
    val communityId: String = "",
    val senderUid: String = "",
    val senderName: String = "",
    val message: String = "",
    val timestamp: Long = 0L
)
