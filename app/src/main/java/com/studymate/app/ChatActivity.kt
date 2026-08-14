package com.studymate.app

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class ChatActivity : Activity() {
    private var partnerId: Long = 0
    private lateinit var messagesContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        partnerId = intent.getLongExtra("partner_id", 0)
        val partner = StudyPartnerRepository.find(partnerId) ?: run { finish(); return }
        ChatStore.ensureConversation(this, partnerId)
        findViewById<TextView>(R.id.chatPartnerName).text = partner.name
        findViewById<TextView>(R.id.chatPartnerContext).text = "${partner.course} • ${partner.topic}"
        messagesContainer = findViewById(R.id.chatMessagesContainer)
        val input = findViewById<EditText>(R.id.messageInput)
        findViewById<Button>(R.id.sendMessageButton).setOnClickListener {
            val text = input.text.toString().trim()
            if (text.isNotEmpty()) {
                ChatStore.add(this, partnerId, ChatMessage(text, true, System.currentTimeMillis()))
                input.text.clear(); renderMessages()
            }
        }
        findViewById<Button>(R.id.shareLocationButton).setOnClickListener { shareLocation() }
        renderMessages()
    }

    private fun renderMessages() {
        messagesContainer.removeAllViews()
        ChatStore.messages(this, partnerId).forEach { message ->
            val bubble = layoutInflater.inflate(R.layout.item_chat_message, messagesContainer, false) as TextView
            bubble.text = message.text
            bubble.gravity = if (message.sentByMe) android.view.Gravity.END else android.view.Gravity.START
            bubble.setTextColor(getColor(if (message.sentByMe) R.color.white else R.color.navy))
            bubble.setBackgroundResource(if (message.sentByMe) R.drawable.bg_message_me else R.drawable.bg_message_them)
            messagesContainer.addView(bubble)
        }
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
        ChatStore.add(this, partnerId, ChatMessage(message, true, System.currentTimeMillis()))
        renderMessages()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 41 && grantResults.any { it == PackageManager.PERMISSION_GRANTED }) shareLocation()
        else if (requestCode == 41) Toast.makeText(this, "Location permission was not granted", Toast.LENGTH_SHORT).show()
    }
}
