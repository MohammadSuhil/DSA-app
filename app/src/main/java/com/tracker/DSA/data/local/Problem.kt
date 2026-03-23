package com.tracker.DSA.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "problems",
    foreignKeys = [
        ForeignKey(
            entity = Topic::class,
            parentColumns = ["id"],
            childColumns = ["topicId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("topicId")]
)
data class Problem(
    @PrimaryKey
    val id: String,
    val topicId: String,
    val name: String,
    val difficulty: String, // "easy", "medium", "hard"
    val url: String,
    val isCompleted: Boolean = false
)
