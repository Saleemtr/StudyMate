package com.studymate.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast

class RegisterActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        setupBackButton()
        val name = findViewById<EditText>(R.id.registerName)
        val email = findViewById<EditText>(R.id.registerEmail)
        val password = findViewById<EditText>(R.id.registerPassword)
        val department = findViewById<Spinner>(R.id.registerDepartment)
        val registerButton = findViewById<Button>(R.id.registerButton)
        registerButton.setOnClickListener {
            val validName = name.text.toString().trim().length >= 2
            val validEmail = Patterns.EMAIL_ADDRESS.matcher(email.text.toString().trim()).matches()
            val validPassword = password.text.length >= 6
            name.error = if (validName) null else "Enter your full name"
            email.error = if (validEmail) null else "Enter a valid email"
            password.error = if (validPassword) null else "Use at least 6 characters"
            if (validName && validEmail && validPassword) {
                registerButton.isEnabled = false
                registerButton.text = "Creating account…"
                val profile = mapOf<String, Any>(
                    "name" to name.text.toString().trim(), "email" to email.text.toString().trim(),
                    "department" to department.selectedItem.toString()
                )
                getSharedPreferences("profile", MODE_PRIVATE).edit()
                    .putString("name", name.text.toString().trim())
                    .putString("email", email.text.toString().trim())
                    .putString("department", department.selectedItem.toString())
                    .apply()
                val handledByFirebase = FirebaseBackend.register(this, email.text.toString().trim(), password.text.toString(), profile) { success, error ->
                    if (success) openProfile() else {
                        registerButton.isEnabled = true
                        registerButton.text = "Create account"
                        Toast.makeText(this, error ?: "Registration failed", Toast.LENGTH_LONG).show()
                    }
                }
                if (!handledByFirebase) {
                    registerButton.isEnabled = true
                    registerButton.text = "Create account"
                    Toast.makeText(this, "Firebase is not configured", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun openProfile() {
        startActivity(Intent(this, ProfileActivity::class.java))
        finish()
    }
}
