package com.studymate.app

import android.content.Context
import android.net.Uri
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage

object FirebaseBackend {
    fun initialize(context: Context): Boolean {
        val app = FirebaseApp.initializeApp(context) ?: return false
        FirebaseMessaging.getInstance().register()
        migrateOwnProfile(context)
        return true
    }

    fun isConfigured(context: Context) = FirebaseApp.getApps(context).isNotEmpty()

    fun signIn(context: Context, email: String, password: String, result: (Boolean, String?) -> Unit): Boolean {
        if (!isConfigured(context)) return false
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    syncInstallationId(context)
                    migrateOwnProfile(context)
                }
                result(task.isSuccessful, task.exception?.localizedMessage)
            }
        return true
    }

    fun register(context: Context, email: String, password: String, profile: Map<String, Any>, result: (Boolean, String?) -> Unit): Boolean {
        if (!isConfigured(context)) return false
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (!task.isSuccessful) result(false, task.exception?.localizedMessage)
            else {
                val uid = task.result.user?.uid ?: return@addOnCompleteListener result(false, "Missing user ID")
                val database = FirebaseFirestore.getInstance()
                val privateProfile = mapOf("email" to email)
                val publicProfile = profile.filterKeys { it != "email" }
                database.runBatch { batch ->
                    batch.set(database.collection("users").document(uid), privateProfile, SetOptions.merge())
                    batch.set(database.collection("publicProfiles").document(uid), publicProfile, SetOptions.merge())
                }.addOnCompleteListener { save ->
                        if (save.isSuccessful) syncInstallationId(context)
                        result(save.isSuccessful, save.exception?.localizedMessage)
                }
            }
        }
        return true
    }

    fun syncProfile(context: Context, profile: Map<String, Any>) {
        if (!isConfigured(context)) return
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            FirebaseFirestore.getInstance().collection("publicProfiles").document(uid).set(profile, SetOptions.merge())
        }
    }

    private fun migrateOwnProfile(context: Context) {
        if (!isConfigured(context)) return
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val database = FirebaseFirestore.getInstance()
        database.collection("users").document(uid).get().addOnSuccessListener { document ->
            val publicFields = listOf("name", "department", "courses", "availability", "bio", "meetingMode", "location")
                .mapNotNull { key -> document.get(key)?.let { key to it } }.toMap()
            if (publicFields.isNotEmpty()) {
                database.collection("publicProfiles").document(uid).set(publicFields, SetOptions.merge())
            }
        }
    }

    fun syncInstallationId(context: Context) {
        if (!isConfigured(context)) return
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val installationId = context.getSharedPreferences("firebase", Context.MODE_PRIVATE)
            .getString("fcm_installation_id", null) ?: return
        val email = FirebaseAuth.getInstance().currentUser?.email.orEmpty()
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .set(mapOf("fcmInstallationId" to installationId, "email" to email), SetOptions.merge())
    }

    fun syncRequest(context: Context, request: StudyRequest) {
        if (!isConfigured(context)) return
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("studyRequests").document(request.id.toString()).set(
            mapOf("ownerId" to uid, "course" to request.course, "topic" to request.topic, "date" to request.date,
                "time" to request.time, "location" to request.location, "notes" to request.notes, "status" to request.status)
        )
    }

    fun deleteRequest(context: Context, id: Long) {
        if (isConfigured(context)) FirebaseFirestore.getInstance().collection("studyRequests").document(id.toString()).delete()
    }

    fun syncMessage(context: Context, partnerId: String, message: ChatMessage) {
        if (!isConfigured(context)) return
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val chat = FirebaseFirestore.getInstance().collection("chats").document(chatId(uid, partnerId))
        chat.set(mapOf(
            "lastMessage" to message.text,
            "lastMessageAt" to message.timestamp
        ), SetOptions.merge()).addOnSuccessListener {
            chat.collection("messages")
                .add(mapOf("text" to message.text, "senderId" to uid, "timestamp" to message.timestamp))
        }
    }

    fun ensureConversation(context: Context, partnerId: String) {
        if (!isConfigured(context)) return
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("chats").document(chatId(uid, partnerId))
            .set(mapOf("participantIds" to participantIds(uid, partnerId)), SetOptions.merge())
    }

    fun observeMessages(context: Context, partnerId: String, result: (List<ChatMessage>) -> Unit): ListenerRegistration? {
        if (!isConfigured(context)) return null
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return null
        return FirebaseFirestore.getInstance().collection("chats").document(chatId(uid, partnerId))
            .collection("messages").orderBy("timestamp").addSnapshotListener { snapshot, _ ->
                if (snapshot != null) result(snapshot.documents.map { document ->
                    ChatMessage(
                        text = document.getString("text").orEmpty(),
                        sentByMe = document.getString("senderId") == uid,
                        timestamp = document.getLong("timestamp") ?: 0L
                    )
                })
            }
    }

    fun observeConversations(context: Context, result: (List<ConversationSummary>) -> Unit): ListenerRegistration? {
        if (!isConfigured(context)) return null
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return null
        return FirebaseFirestore.getInstance().collection("chats")
            .whereArrayContains("participantIds", uid).addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    result(snapshot.documents.mapNotNull { document ->
                        val participants = document.get("participantIds") as? List<*> ?: return@mapNotNull null
                        val partnerId = participants.filterIsInstance<String>().firstOrNull { it != uid }
                            ?: return@mapNotNull null
                        ConversationSummary(
                            partnerId = partnerId,
                            lastMessage = document.getString("lastMessage").orEmpty(),
                            lastMessageAt = document.getLong("lastMessageAt") ?: 0L
                        )
                    }.sortedByDescending { it.lastMessageAt })
                }
            }
    }

    private fun chatId(firstUid: String, secondUid: String) = listOf(firstUid, secondUid).sorted().joinToString("_")

    private fun participantIds(firstUid: String, secondUid: String) = listOf(firstUid, secondUid).sorted()

    fun uploadProfilePhoto(context: Context, image: Uri, result: (Boolean, String?) -> Unit) {
        if (!isConfigured(context)) return result(false, "Firebase is not configured")
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return result(false, "Sign in first")
        FirebaseStorage.getInstance().reference.child("profilePhotos/$uid.jpg").putFile(image)
            .addOnCompleteListener { task -> result(task.isSuccessful, task.exception?.localizedMessage) }
    }
}
