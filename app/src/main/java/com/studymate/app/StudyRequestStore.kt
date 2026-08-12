package com.studymate.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object StudyRequestStore {
    private const val PREFS = "study_requests"
    private const val KEY = "requests"

    fun getAll(context: Context): List<StudyRequest> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "[]") ?: "[]"
        val array = JSONArray(raw)
        return (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            StudyRequest(
                item.getLong("id"), item.getString("course"), item.getString("topic"),
                item.getString("date"), item.getString("time"), item.getString("location"),
                item.optString("notes"), item.optString("status", "Open")
            )
        }.sortedByDescending { it.id }
    }

    fun find(context: Context, id: Long) = getAll(context).firstOrNull { it.id == id }

    fun save(context: Context, request: StudyRequest) {
        val requests = getAll(context).filterNot { it.id == request.id }.toMutableList()
        requests.add(request)
        persist(context, requests)
    }

    fun delete(context: Context, id: Long) = persist(context, getAll(context).filterNot { it.id == id })

    private fun persist(context: Context, requests: List<StudyRequest>) {
        val array = JSONArray()
        requests.forEach { request ->
            array.put(JSONObject().apply {
                put("id", request.id); put("course", request.course); put("topic", request.topic)
                put("date", request.date); put("time", request.time); put("location", request.location)
                put("notes", request.notes); put("status", request.status)
            })
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, array.toString()).apply()
    }
}
