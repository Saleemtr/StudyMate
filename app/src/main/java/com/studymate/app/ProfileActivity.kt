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
        val preferences = getSharedPreferences("profile", MODE_PRIVATE)
        val name = findViewById<EditText>(R.id.profileName)
        val department = findViewById<EditText>(R.id.profileDepartment)
        val courses = findViewById<EditText>(R.id.profileCourses)
        val availability = findViewById<EditText>(R.id.profileAvailability)
        val bio = findViewById<EditText>(R.id.profileBio)
        name.setText(preferences.getString("name", ""))
        department.setText(preferences.getString("department", ""))
        courses.setText(preferences.getString("courses", ""))
        availability.setText(preferences.getString("availability", ""))
        bio.setText(preferences.getString("bio", ""))
        findViewById<Button>(R.id.saveProfileButton).setOnClickListener {
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
            Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.continueHomeButton).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
