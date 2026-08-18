package com.studymate.app

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object StudyPartnerRepository {
    var partners: List<StudyPartner> = emptyList()
        private set

    fun load(context: Context, result: (Boolean, String?) -> Unit) {
        if (!FirebaseBackend.isConfigured(context)) {
            partners = emptyList()
            result(true, null)
            return
        }
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        val localProfile = context.getSharedPreferences("profile", Context.MODE_PRIVATE)
        val ownDepartment = localProfile.getString("department", "").orEmpty()
        val ownCourses = localProfile.getString("courses", "").orEmpty()
        FirebaseFirestore.getInstance().collection("publicProfiles").get()
            .addOnSuccessListener { snapshot ->
                partners = snapshot.documents.filter { it.id != currentUid }.map { document ->
                    val department = document.getString("department").orEmpty()
                    val courses = document.getString("courses").orEmpty().ifBlank { "General study" }
                    val availability = document.getString("availability").orEmpty().ifBlank { "Flexible" }
                    val score = 70 + (if (department.equals(ownDepartment, true) && department.isNotBlank()) 15 else 0) +
                        (if (ownCourses.isNotBlank() && courses.contains(ownCourses, true)) 10 else 0)
                    StudyPartner(
                        id = document.id,
                        name = document.getString("name").orEmpty().ifBlank { "StudyMate student" },
                        department = department.ifBlank { "Student" },
                        course = courses,
                        topic = "Open to study requests",
                        availability = availability,
                        meetingMode = document.getString("meetingMode").orEmpty().ifBlank { "Hybrid" },
                        location = document.getString("location").orEmpty().ifBlank { "To be arranged" },
                        bio = document.getString("bio").orEmpty().ifBlank { "Looking for a study partner." },
                        matchScore = score.coerceAtMost(99)
                    )
                }
                result(true, null)
            }
            .addOnFailureListener { error -> result(false, error.localizedMessage) }
    }

    fun find(id: String) = partners.firstOrNull { it.id == id }
}
