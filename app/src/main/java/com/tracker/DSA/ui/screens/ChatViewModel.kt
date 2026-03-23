package com.tracker.DSA.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tracker.DSA.data.remote.models.Chat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _messages = MutableStateFlow<List<Chat>>(emptyList())
    val messages: StateFlow<List<Chat>> = _messages.asStateFlow()

    fun listenToCommunityChat(communityId: String) {
        firestore.collection("communities")
            .document(communityId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    android.util.Log.e("ChatViewModel", "Chat listen failed", e)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val messageList = snapshot.documents.mapNotNull { it.toObject(Chat::class.java) }
                    _messages.value = messageList
                }
            }
    }

    fun sendMessage(communityId: String, messageText: String) {
        val user = auth.currentUser ?: return
        if (messageText.isBlank()) return

        val chatId = UUID.randomUUID().toString()
        val chatMessage = Chat(
            id = chatId,
            communityId = communityId,
            senderUid = user.uid,
            senderName = user.displayName ?: "Anonymous",
            message = messageText,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            try {
                firestore.collection("communities")
                    .document(communityId)
                    .collection("messages")
                    .document(chatId)
                    .set(chatMessage)
            } catch (e: Exception) {
                android.util.Log.e("ChatViewModel", "Send message failed", e)
            }
        }
    }
}
