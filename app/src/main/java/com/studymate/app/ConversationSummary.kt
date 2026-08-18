package com.studymate.app

data class ConversationSummary(
    val partnerId: String,
    val lastMessage: String,
    val lastMessageAt: Long
)
