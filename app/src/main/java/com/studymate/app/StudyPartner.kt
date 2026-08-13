package com.studymate.app

data class StudyPartner(
    val id: Long,
    val name: String,
    val department: String,
    val course: String,
    val topic: String,
    val availability: String,
    val meetingMode: String,
    val location: String,
    val bio: String,
    val matchScore: Int
)
