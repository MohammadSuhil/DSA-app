package com.tracker.DSA.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.tracker.DSA.data.local.TrackerDao
import kotlinx.coroutines.tasks.await

class SyncRepository(
    private val trackerDao: TrackerDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    suspend fun syncLocalProgressToRemote() {
        val user = auth.currentUser ?: return
        try {
            val completedCount = trackerDao.getCompletedProblemsCountSync()
            val totalCount = trackerDao.getTotalProblemsCountSync()
            val progressPercentage = if (totalCount > 0) (completedCount * 100) / totalCount else 0

            val updateData = mapOf(
                "uid" to user.uid,
                "completedCount" to completedCount,
                "progressPercentage" to progressPercentage,
                "lastCompletionTimestamp" to System.currentTimeMillis(),
                "name" to (user.displayName ?: "Anonymous"),
                "photoUrl" to (user.photoUrl?.toString() ?: ""),
                "email" to (user.email ?: "")
            )

            firestore.collection("users")
                .document(user.uid)
                .set(updateData, SetOptions.merge())
                .await()
            
            Log.d("SyncRepository", "Sync successful: $completedCount/$totalCount")
        } catch (e: Exception) {
            Log.e("SyncRepository", "Error syncing progress", e)
        }
    }
}
