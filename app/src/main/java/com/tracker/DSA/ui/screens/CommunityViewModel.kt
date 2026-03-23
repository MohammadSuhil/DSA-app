package com.tracker.DSA.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tracker.DSA.data.remote.models.Community
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class CommunityViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _communities = MutableStateFlow<List<Community>>(emptyList())
    val communities: StateFlow<List<Community>> = _communities.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchCommunities()
    }

    fun fetchCommunities(searchQuery: String = "") {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = firestore.collection("communities")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(100)
                    .get()
                    .await()

                val list = snapshot.documents.mapNotNull { it.toObject(Community::class.java) }
                
                _communities.value = if (searchQuery.isNotBlank()) {
                    list.filter { it.name.contains(searchQuery, ignoreCase = true) }
                } else {
                    list
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createCommunity(name: String, description: String, onComplete: (Boolean) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            android.util.Log.e("CommunityViewModel", "Cannot create community: User is not logged in")
            onComplete(false)
            return
        }

        val adminUid = user.uid
        val communityId = java.util.UUID.randomUUID().toString()
        val newCommunity = Community(
            id = communityId,
            name = name,
            description = description,
            adminUid = adminUid,
            members = listOf(adminUid),
            createdAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            try {
                firestore.collection("communities").document(communityId).set(newCommunity).await()
                fetchCommunities()
                onComplete(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            }
        }
    }

    fun joinCommunity(communityId: String, onComplete: (Boolean) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onComplete(false)
            return
        }

        viewModelScope.launch {
            try {
                val docRef = firestore.collection("communities").document(communityId)
                firestore.runTransaction { transaction ->
                    val snapshot = transaction.get(docRef)
                    val community = snapshot.toObject(Community::class.java)
                    if (community != null && !community.members.contains(user.uid)) {
                        val updatedMembers = community.members + user.uid
                        transaction.update(docRef, "members", updatedMembers)
                    }
                }.await()
                fetchCommunities()
                onComplete(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            }
        }
    }
}
