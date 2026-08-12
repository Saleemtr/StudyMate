package com.studymate.app

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast

class CreateRequestActivity : Activity() {
    private var requestId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_request)
        requestId = intent.getLongExtra("request_id", 0)
        val course = findViewById<EditText>(R.id.requestCourse)
        val topic = findViewById<EditText>(R.id.requestTopic)
        val date = findViewById<EditText>(R.id.requestDate)
        val time = findViewById<EditText>(R.id.requestTime)
        val location = findViewById<Spinner>(R.id.requestLocation)
        val notes = findViewById<EditText>(R.id.requestNotes)

        StudyRequestStore.find(this, requestId)?.let { request ->
            course.setText(request.course); topic.setText(request.topic); date.setText(request.date)
            time.setText(request.time); notes.setText(request.notes)
            val position = resources.getStringArray(R.array.study_locations).indexOf(request.location)
            if (position >= 0) location.setSelection(position)
            findViewById<Button>(R.id.publishRequestButton).text = "Save changes"
        }

        findViewById<Button>(R.id.publishRequestButton).setOnClickListener {
            val validCourse = course.text.toString().trim().isNotEmpty()
            val validTopic = topic.text.toString().trim().isNotEmpty()
            val validDate = date.text.toString().trim().isNotEmpty()
            val validTime = time.text.toString().trim().isNotEmpty()
            course.error = if (validCourse) null else "Course is required"
            topic.error = if (validTopic) null else "Topic is required"
            date.error = if (validDate) null else "Date is required"
            time.error = if (validTime) null else "Time is required"
            if (validCourse && validTopic && validDate && validTime) {
                val existing = StudyRequestStore.find(this, requestId)
                StudyRequestStore.save(this, StudyRequest(
                    if (requestId == 0L) System.currentTimeMillis() else requestId,
                    course.text.toString().trim(), topic.text.toString().trim(), date.text.toString().trim(),
                    time.text.toString().trim(), location.selectedItem.toString(), notes.text.toString().trim(),
                    existing?.status ?: "Open"
                ))
                Toast.makeText(this, if (requestId == 0L) "Request published" else "Request updated", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
