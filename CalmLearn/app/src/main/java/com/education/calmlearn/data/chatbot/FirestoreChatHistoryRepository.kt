package com.education.calmlearn.data.chatbot

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class FirestoreChatHistoryRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ChatHistoryRepository {

    override suspend fun loadRecentMessages(): ChatHistoryResult {
        val user = auth.currentUser ?: return ChatHistoryResult.NotSignedIn
        return try {
            val snapshot = messagesOf(user.uid)
                .orderBy(FIELD_CREATED_AT, Query.Direction.DESCENDING)
                .limit(LOAD_LIMIT)
                .get()
                .await()
            ChatHistoryResult.Loaded(snapshot.documents.mapNotNull { it.toChatMessage() }.reversed())
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (readError: Exception) {
            ChatHistoryResult.Error(ChatbotErrorReason.HISTORY_READ_FAILED)
        }
    }

    override suspend fun saveMessage(message: ChatMessage): ChatHistoryResult {
        val user = auth.currentUser ?: return ChatHistoryResult.NotSignedIn
        return try {
            val data = mapOf(
                FIELD_ROLE to message.role,
                FIELD_TEXT to message.text.take(MAX_TEXT_LENGTH),
                FIELD_CREATED_AT to FieldValue.serverTimestamp()
            )
            messagesOf(user.uid).add(data).await()
            ChatHistoryResult.Saved
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (writeError: Exception) {
            ChatHistoryResult.Error(ChatbotErrorReason.HISTORY_SAVE_FAILED)
        }
    }

    private fun messagesOf(uid: String): CollectionReference =
        firestore.collection(CONVERSATIONS_COLLECTION).document(uid).collection(MESSAGES_COLLECTION)

    private fun DocumentSnapshot.toChatMessage(): ChatMessage? {
        val role = getString(FIELD_ROLE) ?: return null
        val text = getString(FIELD_TEXT) ?: return null
        val createdAt = getTimestamp(FIELD_CREATED_AT)?.toDate()?.time ?: System.currentTimeMillis()
        return ChatMessage(role = role, text = text, createdAtMillis = createdAt)
    }

    private companion object {
        const val CONVERSATIONS_COLLECTION = "chatConversations"
        const val MESSAGES_COLLECTION = "messages"
        const val FIELD_ROLE = "role"
        const val FIELD_TEXT = "text"
        const val FIELD_CREATED_AT = "createdAt"
        const val LOAD_LIMIT = 50L
        const val MAX_TEXT_LENGTH = 3999
    }
}
