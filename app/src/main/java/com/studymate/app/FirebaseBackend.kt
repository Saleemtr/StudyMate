package com.studymate.app

import android.content.Context
import android.net.Uri
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage

object FirebaseBackend {
    fun initialize(context: Context): Boolean {
        val app = FirebaseApp.initializeApp(context) ?: return false
        FirebaseMessaging.getInstance().register()
        return true
    }

    fun isConfigured(context: Context) = FirebaseApp.getApps(context).isNotEmpty()

    fun signIn(context: Context, email: String, password: String, result: (Boolean, String?) -> Unit): Boolean {
        if (!isConfigured(context)) return false
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task -> result(task.isSuccessful, task.exception?.localizedMessage) }
        return true
    }

    fun register(context: Context, email: String, password: String, profile: Map<String, Any>, result: (Boolean, String?) -> Unit): Boolean {
        if (!isConfigured(context)) return false
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (!task.isSuccessful) result(false, task.exception?.localizedMessage)
            else {
                val uid = task.result.user?.uid ?: return@addOnCompleteListener result(false, "Missing user ID")
                FirebaseFirestore.getInstance().collection("users").document(uid).set(profile)
                    .addOnCompleteListener { save -> result(save.isSuccessful, save.exception?.localizedMessage) }
            }
        }
        return true
    }

    fun syncProfile(context: Context, profile: Map<String, Any>) {
        if (!isConfigured(context)) return
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            FirebaseFirestore.getInstance().collection("users").document(uid).set(profile)
        }
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

    fun syncMessage(context: Context, partnerId: Long, message: ChatMessage) {
        if (!isConfigured(context)) return
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("chats").document("${uid}_$partnerId").collection("messages")
            .add(mapOf("text" to message.text, "senderId" to uid, "timestamp" to message.timestamp))
    }

    fun uploadProfilePhoto(context: Context, image: Uri, result: (Boolean, String?) -> Unit) {
        if (!isConfigured(context)) return result(false, "Firebase is not configured")
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return result(false, "Sign in first")
        FirebaseStorage.getInstance().reference.child("profilePhotos/$uid.jpg").putFile(image)
            .addOnCompleteListener { task -> result(task.isSuccessful, task.exception?.localizedMessage) }
    }
}
