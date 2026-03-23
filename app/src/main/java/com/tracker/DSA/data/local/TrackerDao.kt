package com.tracker.DSA.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopics(topics: List<Topic>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProblems(problems: List<Problem>)

    @Transaction
    @Query("SELECT * FROM topics")
    fun getTopicsWithProblems(): Flow<List<TopicWithProblems>>

    @Query("UPDATE problems SET isCompleted = :isCompleted WHERE id = :problemId")
    suspend fun updateProblemStatus(problemId: String, isCompleted: Boolean)

    @Query("SELECT COUNT(*) FROM problems WHERE isCompleted = 1")
    fun getCompletedProblemsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM problems WHERE isCompleted = 1")
    suspend fun getCompletedProblemsCountSync(): Int


    @Query("SELECT COUNT(*) FROM problems")
    fun getTotalProblemsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM problems")
    suspend fun getTotalProblemsCountSync(): Int


    @Query("SELECT COUNT(*) FROM topics")
    suspend fun getTopicCount(): Int
}
