package com.studymate.app

import android.app.Activity
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 42)
        }
        findViewById<android.widget.Button>(R.id.createRequestButton).setOnClickListener {
            startActivity(Intent(this, CreateRequestActivity::class.java))
        }
        findViewById<android.widget.Button>(R.id.myRequestsButton).setOnClickListener {
            startActivity(Intent(this, MyRequestsActivity::class.java))
        }
        findViewById<android.widget.Button>(R.id.searchPartnersButton).setOnClickListener {
            startActivity(Intent(this, SearchPartnersActivity::class.java))
        }
        findViewById<android.widget.Button>(R.id.messagesButton).setOnClickListener {
            startActivity(Intent(this, ConversationsActivity::class.java))
        }
        findViewById<android.widget.Button>(R.id.profileButton).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
}
