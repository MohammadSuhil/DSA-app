package com.tracker.DSA.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tracker.DSA.data.remote.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LeaderboardViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _leaderboardUsers = MutableStateFlow<List<User>>(emptyList())
    val leaderboardUsers: StateFlow<List<User>> = _leaderboardUsers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var lastDocument: com.google.firebase.firestore.DocumentSnapshot? = null
    private var isLastPage = false

    init {
        fetchLeaderboard()
    }

    fun fetchLeaderboard(isRefresh: Boolean = false) {
        if (isRefresh) {
            lastDocument = null
            isLastPage = false
        }

        if (isLastPage || _isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                var query = firestore.collection("users")
                    .orderBy("progressPercentage", Query.Direction.DESCENDING)
                    .orderBy("lastCompletionTimestamp", Query.Direction.DESCENDING)
                    .limit(20)

                lastDocument?.let {
                    query = query.startAfter(it)
                }

                val snapshot = query.get().await()
                
                if (snapshot.isEmpty) {
                    isLastPage = true
                } else {
                    lastDocument = snapshot.documents.last()
                    val newUsers = snapshot.documents.mapNotNull { it.toObject(User::class.java) }
                    if (isRefresh) {
                        _leaderboardUsers.value = newUsers
                    } else {
                        _leaderboardUsers.value += newUsers
                    }
                }
            } catch (e: com.google.firebase.firestore.FirebaseFirestoreException) {
                if (e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.FAILED_PRECONDITION) {
                    _errorMessage.value = "Indexing... Please wait 2-5 minutes for Firestore to build the leaderboard index."
                } else {
                    _errorMessage.value = "Permissions Denied: Check your Firestore Security Rules."
                }
                android.util.Log.e("LeaderboardViewModel", "Firestore error", e)
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
                android.util.Log.e("LeaderboardViewModel", "Error fetching leaderboard", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMore() {
        if (!_isLoading.value && !isLastPage) {
            fetchLeaderboard()
        }
    }
}
