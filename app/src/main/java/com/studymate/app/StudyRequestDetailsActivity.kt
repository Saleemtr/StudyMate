package com.studymate.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class StudyRequestDetailsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_details)
        val id = intent.getLongExtra("request_id", 0)
        val request = StudyRequestStore.find(this, id) ?: run { finish(); return }
        findViewById<TextView>(R.id.detailsCourse).text = request.course
        findViewById<TextView>(R.id.detailsTopic).text = request.topic
        findViewById<TextView>(R.id.detailsSchedule).text = "${request.date} at ${request.time}"
        findViewById<TextView>(R.id.detailsLocation).text = request.location
        findViewById<TextView>(R.id.detailsNotes).text = request.notes.ifBlank { "No additional notes" }
        findViewById<TextView>(R.id.detailsStatus).text = request.status
        findViewById<Button>(R.id.editRequestButton).setOnClickListener {
            startActivity(Intent(this, CreateRequestActivity::class.java).putExtra("request_id", id)); finish()
        }
        findViewById<Button>(R.id.closeRequestButton).setOnClickListener {
            StudyRequestStore.save(this, request.copy(status = "Closed")); recreate()
        }
        findViewById<Button>(R.id.deleteRequestButton).setOnClickListener {
            AlertDialog.Builder(this).setTitle("Delete request?")
                .setMessage("This action cannot be undone.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete") { _, _ -> StudyRequestStore.delete(this, id); finish() }
                .show()
        }
    }
}
