package com.tracker.DSA.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "topics")
data class Topic(
    @PrimaryKey
    val id: String,
    val name: String
)
