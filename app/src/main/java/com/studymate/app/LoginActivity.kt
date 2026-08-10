package com.studymate.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class LoginActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val email = findViewById<EditText>(R.id.loginEmail)
        val password = findViewById<EditText>(R.id.loginPassword)
        findViewById<Button>(R.id.loginButton).setOnClickListener {
            val validEmail = Patterns.EMAIL_ADDRESS.matcher(email.text.toString().trim()).matches()
            val validPassword = password.text.length >= 6
            email.error = if (validEmail) null else "Enter a valid email"
            password.error = if (validPassword) null else "Use at least 6 characters"
            if (validEmail && validPassword) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
        findViewById<TextView>(R.id.openRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
