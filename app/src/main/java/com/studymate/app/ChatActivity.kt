package com.studymate.app

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.ListenerRegistration

class ChatActivity : Activity() {
    private var partnerId: String = ""
    private lateinit var messagesContainer: LinearLayout
    private lateinit var messagesScroll: ScrollView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var locationButton: Button
    private var messageListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        partnerId = intent.getStringExtra("partner_id").orEmpty()
        val partner = StudyPartnerRepository.find(partnerId) ?: run { finish(); return }
        ChatStore.ensureConversation(this, partnerId)
        findViewById<TextView>(R.id.chatPartnerName).text = partner.name
        findViewById<TextView>(R.id.chatPartnerContext).text = "${partner.course} • ${partner.topic}"
        messagesScroll = findViewById(R.id.chatScroll)
        messagesContainer = findViewById(R.id.chatMessagesContainer)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendMessageButton)
        locationButton = findViewById(R.id.shareLocationButton)
        sendButton.setOnClickListener {
            val text = messageInput.text.toString().trim()
            if (text.isNotEmpty()) {
                sendOutgoingMessage(text, clearInput = true)
            }
        }
        locationButton.setOnClickListener { shareLocation() }
        renderMessages(ChatStore.messages(this, partnerId))
        messageListener = FirebaseBackend.observeMessages(this, partnerId) { messages, error ->
            if (error == null) renderMessages(messages)
            else Toast.makeText(this, "Could not load messages: $error", Toast.LENGTH_LONG).show()
        }
    }

    private fun sendOutgoingMessage(text: String, clearInput: Boolean) {
        sendButton.isEnabled = false
        locationButton.isEnabled = false
        sendButton.text = "Sending…"
        ChatStore.add(this, partnerId, ChatMessage(text, true, System.currentTimeMillis())) { success, error ->
            sendButton.isEnabled = true
            locationButton.isEnabled = true
            sendButton.text = "Send"
            if (success) {
                if (clearInput) messageInput.text.clear()
            } else {
                Toast.makeText(this, error ?: "Could not send message", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun renderMessages(messages: List<ChatMessage>) {
        messagesContainer.removeAllViews()
        messages.forEach { message ->
            val bubble = layoutInflater.inflate(R.layout.item_chat_message, messagesContainer, false) as TextView
            bubble.text = message.text
            bubble.gravity = if (message.sentByMe) android.view.Gravity.END else android.view.Gravity.START
            bubble.setTextColor(getColor(if (message.sentByMe) R.color.white else R.color.navy))
            bubble.setBackgroundResource(if (message.sentByMe) R.drawable.bg_message_me else R.drawable.bg_message_them)
            messagesContainer.addView(bubble)
        }
        messagesScroll.post { messagesScroll.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    override fun onDestroy() {
        messageListener?.remove()
        super.onDestroy()
    }

    private fun shareLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 41)
            return
        }
        val manager = getSystemService(LOCATION_SERVICE) as LocationManager
        val location = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .mapNotNull { provider -> runCatching { manager.getLastKnownLocation(provider) }.getOrNull() }
            .maxByOrNull { it.time }
        val message = location?.let { "📍 My location: https://maps.google.com/?q=${it.latitude},${it.longitude}" }
            ?: "📍 I’m ready to share my location, but no recent location is available yet."
        sendOutgoingMessage(message, clearInput = false)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 41 && grantResults.any { it == PackageManager.PERMISSION_GRANTED }) shareLocation()
        else if (requestCode == 41) Toast.makeText(this, "Location permission was not granted", Toast.LENGTH_SHORT).show()
    }
}
