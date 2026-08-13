package com.studymate.app

object StudyPartnerRepository {
    val partners = listOf(
        StudyPartner(1, "Maya Cohen", "Computer Science", "Kotlin", "Android fundamentals", "Evenings", "In person", "Campus library", "Second-year student who enjoys learning through practical exercises.", 96),
        StudyPartner(2, "Omar Khalil", "Mathematics", "Calculus", "Integrals", "Afternoons", "Hybrid", "Study room", "Looking for a consistent weekly study group.", 92),
        StudyPartner(3, "Noa Levi", "Engineering", "Physics", "Mechanics", "Mornings", "Online", "Online", "Prefers focused sessions with a clear list of goals.", 88),
        StudyPartner(4, "Daniel Mansour", "Business", "Statistics", "Probability", "Evenings", "In person", "Campus café", "Happy to review exercises and prepare together for exams.", 84),
        StudyPartner(5, "Lina Haddad", "Computer Science", "Data Structures", "Trees and graphs", "Weekends", "Hybrid", "Campus library", "Visual learner interested in pair programming and problem solving.", 81),
        StudyPartner(6, "Adam Saad", "Natural Sciences", "Chemistry", "Organic chemistry", "Afternoons", "Online", "Online", "Likes short, structured sessions and shared summaries.", 78)
    )

    fun find(id: Long) = partners.firstOrNull { it.id == id }
}
