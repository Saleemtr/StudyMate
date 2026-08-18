package com.studymate.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.firestore.ListenerRegistration
import java.text.DateFormat
import java.util.Date

class ConversationsActivity : Activity() {
    private var conversationListener: ListenerRegistration? = null
    private var summaries: List<ConversationSummary> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversations)
        StudyPartnerRepository.load(this) { _, _ -> renderConversations() }
        conversationListener = FirebaseBackend.observeConversations(this) { conversations ->
            summaries = conversations
            renderConversations()
        }
    }

    private fun renderConversations() {
        val container = findViewById<LinearLayout>(R.id.conversationsContainer)
        val empty = findViewById<TextView>(R.id.emptyConversationsText)
        container.removeAllViews()
        val rows = summaries.mapNotNull { summary ->
            StudyPartnerRepository.find(summary.partnerId)?.let { partner -> partner to summary }
        }
        empty.visibility = if (rows.isEmpty()) View.VISIBLE else View.GONE
        rows.forEach { (partner, summary) ->
            val row = layoutInflater.inflate(R.layout.item_conversation, container, false)
            row.findViewById<TextView>(R.id.conversationName).text = partner.name
            row.findViewById<TextView>(R.id.conversationPreview).text = summary.lastMessage.ifBlank { "Start a conversation" }
            row.findViewById<TextView>(R.id.conversationTime).text = if (summary.lastMessageAt > 0) {
                DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(summary.lastMessageAt))
            } else ""
            row.setOnClickListener {
                startActivity(Intent(this, ChatActivity::class.java).putExtra("partner_id", partner.id))
            }
            container.addView(row)
        }
    }

    override fun onDestroy() {
        conversationListener?.remove()
        super.onDestroy()
    }
}
