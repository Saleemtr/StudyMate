package com.studymate.app

import android.app.NotificationChannel
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StudyMateMessagingService : FirebaseMessagingService() {
    override fun onRegistered(installationId: String) {
        getSharedPreferences("firebase", MODE_PRIVATE).edit().putString("fcm_installation_id", installationId).apply()
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            FirebaseFirestore.getInstance().collection("users").document(uid).update("fcmInstallationId", installationId)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val channelId = "studymate_messages"
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(NotificationChannel(channelId, "StudyMate messages", NotificationManager.IMPORTANCE_DEFAULT))
        val intent = Intent(this, ConversationsActivity::class.java)
        val pending = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val notification = Notification.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle(message.notification?.title ?: "New StudyMate message")
            .setContentText(message.notification?.body ?: message.data["message"] ?: "Open StudyMate to view it")
            .setContentIntent(pending).setAutoCancel(true).build()
        manager.notify(message.messageId?.hashCode() ?: System.currentTimeMillis().toInt(), notification)
    }
}
