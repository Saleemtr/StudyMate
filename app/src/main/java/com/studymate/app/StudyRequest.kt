package com.studymate.app

data class StudyRequest(
    val id: Long,
    val course: String,
    val topic: String,
    val date: String,
    val time: String,
    val location: String,
    val notes: String,
    val status: String = "Open"
)
