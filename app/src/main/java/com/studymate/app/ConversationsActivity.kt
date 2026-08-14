package com.studymate.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import java.text.DateFormat
import java.util.Date

class ConversationsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversations)
    }

    override fun onResume() {
        super.onResume()
        val container = findViewById<LinearLayout>(R.id.conversationsContainer)
        val empty = findViewById<TextView>(R.id.emptyConversationsText)
        container.removeAllViews()
        val partners = ChatStore.activePartnerIds(this).mapNotNull(StudyPartnerRepository::find)
        empty.visibility = if (partners.isEmpty()) View.VISIBLE else View.GONE
        partners.forEach { partner ->
            val messages = ChatStore.messages(this, partner.id)
            val last = messages.lastOrNull()
            val row = layoutInflater.inflate(R.layout.item_conversation, container, false)
            row.findViewById<TextView>(R.id.conversationName).text = partner.name
            row.findViewById<TextView>(R.id.conversationPreview).text = last?.text ?: "Start a conversation"
            row.findViewById<TextView>(R.id.conversationTime).text = last?.let {
                DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(it.timestamp))
            } ?: ""
            row.setOnClickListener {
                startActivity(Intent(this, ChatActivity::class.java).putExtra("partner_id", partner.id))
            }
            container.addView(row)
        }
    }
}
