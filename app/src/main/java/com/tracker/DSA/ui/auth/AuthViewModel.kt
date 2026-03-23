package com.tracker.DSA.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.tracker.DSA.data.remote.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile

    init {
        auth.addAuthStateListener {
            _currentUser.value = it.currentUser
            if (it.currentUser != null) {
                fetchUserProfile(it.currentUser!!.uid)
            } else {
                _userProfile.value = null
            }
        }
    }

    private fun fetchUserProfile(uid: String) {
        viewModelScope.launch {
            try {
                val doc = firestore.collection("users").document(uid).get().await()
                _userProfile.value = doc.toObject(User::class.java)
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Error fetching user profile", e)
            }
        }
    }

    fun signOut() {
        auth.signOut()
    }
    
    fun syncUserToFirestore(firebaseUser: FirebaseUser) {
        val user = User(
            uid = firebaseUser.uid,
            name = firebaseUser.displayName ?: "User",
            email = firebaseUser.email ?: "",
            photoUrl = firebaseUser.photoUrl?.toString() ?: ""
        )
        viewModelScope.launch {
            try {
                firestore.collection("users").document(user.uid).set(user).await()
                android.util.Log.d("AuthViewModel", "User sync success: ${user.uid}")
                _userProfile.value = user
                seedTestData() // Seed data after Google Sign-In success
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "User sync failed", e)
            }
        }
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            try {
                android.util.Log.d("AuthViewModel", "Attempting anonymous sign-in")
                auth.signInAnonymously().await()
                android.util.Log.d("AuthViewModel", "Anonymous sign-in success: ${auth.currentUser?.uid}")
                seedTestData() // Seed data after Anonymous success
            } catch (e: com.google.firebase.auth.FirebaseAuthException) {
                if (e.errorCode == "ERROR_OPERATION_NOT_ALLOWED") {
                     android.util.Log.e("AuthViewModel", "Anonymous Auth is DISABLED in Firebase Console.")
                }
                android.util.Log.e("AuthViewModel", "Anonymous sign-in failed", e)
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Anonymous sign-in failed", e)
            }
        }
    }

    fun seedTestData() {
        viewModelScope.launch {
            try {
                // Seed leaderboard test data
                val testUsers = listOf(
                    User(uid = "test1", name = "Alice Python", progressPercentage = 85, completedCount = 65, lastCompletionTimestamp = System.currentTimeMillis()),
                    User(uid = "test2", name = "Bob Kotlin", progressPercentage = 70, completedCount = 54, lastCompletionTimestamp = System.currentTimeMillis() - 100000),
                    User(uid = "test3", name = "Charlie Java", progressPercentage = 95, completedCount = 73, lastCompletionTimestamp = System.currentTimeMillis() - 50000)
                )
                testUsers.forEach { 
                    firestore.collection("users").document(it.uid).set(it).await()
                }

                // Seed "Autem" community
                val autem = User( // Reuse User or create map with correct adminUid
                    uid = "autem_community",
                    name = "Autem"
                )
                val autemData = mapOf(
                    "id" to "autem_community",
                    "name" to "Autem",
                    "description" to "A community for Autem enthusiasts to track DSA together.",
                    "adminUid" to "system",
                    "members" to listOf("test1", "test2", "test3"),
                    "createdAt" to System.currentTimeMillis()
                )
                firestore.collection("communities").document("autem_community").set(autemData).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
