package com.studymate.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class LoginActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (FirebaseBackend.hasSignedInUser(this)) {
            FirebaseBackend.refreshOwnProfile(this) { _, _ -> openHome() }
            return
        }
        setContentView(R.layout.activity_login)
        val email = findViewById<EditText>(R.id.loginEmail)
        val password = findViewById<EditText>(R.id.loginPassword)
        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            val validEmail = Patterns.EMAIL_ADDRESS.matcher(email.text.toString().trim()).matches()
            val validPassword = password.text.length >= 6
            email.error = if (validEmail) null else "Enter a valid email"
            password.error = if (validPassword) null else "Use at least 6 characters"
            if (validEmail && validPassword) {
                loginButton.isEnabled = false
                loginButton.text = "Signing in…"
                val handledByFirebase = FirebaseBackend.signIn(this, email.text.toString().trim(), password.text.toString()) { success, error ->
                    if (success) openHome() else {
                        loginButton.isEnabled = true
                        loginButton.text = "Sign in"
                        Toast.makeText(this, error ?: "Sign in failed", Toast.LENGTH_LONG).show()
                    }
                }
                if (!handledByFirebase) {
                    loginButton.isEnabled = true
                    loginButton.text = "Sign in"
                    Toast.makeText(this, "Firebase is not configured", Toast.LENGTH_LONG).show()
                }
            }
        }
        findViewById<TextView>(R.id.openRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun openHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
