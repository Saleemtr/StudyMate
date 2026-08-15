package com.studymate.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object ChatStore {
    private const val PREFS = "chat_messages"

    fun ensureConversation(context: Context, partnerId: Long) {
        if (!context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).contains(partnerId.toString())) {
            save(context, partnerId, listOf(ChatMessage("Hi! I’m interested in studying together.", true, System.currentTimeMillis())))
        }
    }

    fun activePartnerIds(context: Context): Set<Long> =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).all.keys.mapNotNull { it.toLongOrNull() }.toSet()

    fun messages(context: Context, partnerId: Long): List<ChatMessage> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(partnerId.toString(), "[]") ?: "[]"
        val array = JSONArray(raw)
        return (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            ChatMessage(item.getString("text"), item.getBoolean("sentByMe"), item.getLong("timestamp"))
        }
    }

    fun add(context: Context, partnerId: Long, message: ChatMessage) {
        save(context, partnerId, messages(context, partnerId) + message)
        FirebaseBackend.syncMessage(context, partnerId, message)
    }

    private fun save(context: Context, partnerId: Long, messages: List<ChatMessage>) {
        val array = JSONArray()
        messages.forEach { message ->
            array.put(JSONObject().apply {
                put("text", message.text); put("sentByMe", message.sentByMe); put("timestamp", message.timestamp)
            })
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(partnerId.toString(), array.toString()).apply()
    }
}
