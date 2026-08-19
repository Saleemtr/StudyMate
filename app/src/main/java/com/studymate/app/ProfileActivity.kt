package com.studymate.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class ProfileActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setupBackButton()
        val preferences = getSharedPreferences("profile", MODE_PRIVATE)
        val name = findViewById<EditText>(R.id.profileName)
        val department = findViewById<EditText>(R.id.profileDepartment)
        val courses = findViewById<EditText>(R.id.profileCourses)
        val availability = findViewById<EditText>(R.id.profileAvailability)
        val bio = findViewById<EditText>(R.id.profileBio)
        fun showSavedProfile() {
            name.setText(preferences.getString("name", ""))
            department.setText(preferences.getString("department", ""))
            courses.setText(preferences.getString("courses", ""))
            availability.setText(preferences.getString("availability", ""))
            bio.setText(preferences.getString("bio", ""))
        }
        showSavedProfile()
        FirebaseBackend.refreshOwnProfile(this) { success, error ->
            if (success) showSavedProfile()
            else if (error != "Firebase is not configured") {
                Toast.makeText(this, error ?: "Could not load profile", Toast.LENGTH_LONG).show()
            }
        }
        val saveButton = findViewById<Button>(R.id.saveProfileButton)
        saveButton.setOnClickListener {
            if (name.text.toString().trim().length < 2) {
                name.error = "Enter your full name"
                return@setOnClickListener
            }
            preferences.edit()
                .putString("name", name.text.toString().trim())
                .putString("department", department.text.toString().trim())
                .putString("courses", courses.text.toString().trim())
                .putString("availability", availability.text.toString().trim())
                .putString("bio", bio.text.toString().trim())
                .apply()
            saveButton.isEnabled = false
            saveButton.text = "Saving…"
            FirebaseBackend.syncProfile(this, mapOf(
                "name" to name.text.toString().trim(), "department" to department.text.toString().trim(),
                "courses" to courses.text.toString().trim(), "availability" to availability.text.toString().trim(),
                "bio" to bio.text.toString().trim()
            )) { success, error ->
                saveButton.isEnabled = true
                saveButton.text = "Save profile"
                Toast.makeText(
                    this,
                    if (success) "Profile saved" else error ?: "Could not save profile",
                    if (success) Toast.LENGTH_SHORT else Toast.LENGTH_LONG
                ).show()
            }
        }
        findViewById<Button>(R.id.continueHomeButton).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
