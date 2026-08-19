package com.studymate.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object ChatStore {
    private const val PREFS = "chat_messages"

    fun ensureConversation(context: Context, partnerId: String) {
        if (!context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).contains(partnerId)) {
            save(context, partnerId, listOf(ChatMessage("Hi! I’m interested in studying together.", true, System.currentTimeMillis())))
        }
        FirebaseBackend.ensureConversation(context, partnerId)
    }

    fun activePartnerIds(context: Context): Set<String> =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).all.keys

    fun messages(context: Context, partnerId: String): List<ChatMessage> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(partnerId, "[]") ?: "[]"
        val array = JSONArray(raw)
        return (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            ChatMessage(item.getString("text"), item.getBoolean("sentByMe"), item.getLong("timestamp"))
        }
    }

    fun add(
        context: Context,
        partnerId: String,
        message: ChatMessage,
        result: (Boolean, String?) -> Unit
    ) {
        FirebaseBackend.syncMessage(context, partnerId, message) { success, error ->
            if (success) save(context, partnerId, messages(context, partnerId) + message)
            result(success, error)
        }
    }

    private fun save(context: Context, partnerId: String, messages: List<ChatMessage>) {
        val array = JSONArray()
        messages.forEach { message ->
            array.put(JSONObject().apply {
                put("text", message.text); put("sentByMe", message.sentByMe); put("timestamp", message.timestamp)
            })
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(partnerId, array.toString()).apply()
    }
}
