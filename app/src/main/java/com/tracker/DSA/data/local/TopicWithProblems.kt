package com.tracker.DSA.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class TopicWithProblems(
    @Embedded val topic: Topic,
    @Relation(
        parentColumn = "id",
        entityColumn = "topicId"
    )
    val problems: List<Problem>
)
