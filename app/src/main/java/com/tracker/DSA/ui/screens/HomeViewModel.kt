package com.tracker.DSA.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tracker.DSA.data.local.AppDatabase
import com.tracker.DSA.data.local.TopicWithProblems
import com.tracker.DSA.data.repository.SyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val trackerDao = db.trackerDao()

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val syncRepository = SyncRepository(trackerDao, firestore, auth)

    private val _topicsWithProblems = MutableStateFlow<List<TopicWithProblems>>(emptyList())
    val topicsWithProblems: StateFlow<List<TopicWithProblems>> = _topicsWithProblems.asStateFlow()

    private val _completedCount = MutableStateFlow(0)
    val completedCount: StateFlow<Int> = _completedCount.asStateFlow()

    private val _totalCount = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCount.asStateFlow()

    init {
        viewModelScope.launch {
            trackerDao.getTopicsWithProblems().collect {
                _topicsWithProblems.value = it
            }
        }
        viewModelScope.launch {
            trackerDao.getCompletedProblemsCount().collect {
                _completedCount.value = it
            }
        }
        viewModelScope.launch {
            trackerDao.getTotalProblemsCount().collect {
                _totalCount.value = it
            }
        }
    }

    fun toggleProblemStatus(problemId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            trackerDao.updateProblemStatus(problemId, isCompleted)
            // Trigger remote sync if user is logged in
            if (auth.currentUser != null) {
                syncRepository.syncLocalProgressToRemote()
            }
        }
    }

    fun syncProgress() {
        viewModelScope.launch {
            if (auth.currentUser != null) {
                syncRepository.syncLocalProgressToRemote()
            }
        }
    }
}
