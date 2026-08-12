package com.studymate.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MyRequestsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_requests)
        findViewById<Button>(R.id.addRequestButton).setOnClickListener {
            startActivity(Intent(this, CreateRequestActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        val container = findViewById<LinearLayout>(R.id.requestsContainer)
        val empty = findViewById<TextView>(R.id.emptyRequestsText)
        container.removeAllViews()
        val requests = StudyRequestStore.getAll(this)
        empty.visibility = if (requests.isEmpty()) View.VISIBLE else View.GONE
        requests.forEach { request ->
            val row = layoutInflater.inflate(R.layout.item_study_request, container, false)
            row.findViewById<TextView>(R.id.itemCourse).text = request.course
            row.findViewById<TextView>(R.id.itemTopic).text = request.topic
            row.findViewById<TextView>(R.id.itemSchedule).text = "${request.date} • ${request.time} • ${request.location}"
            row.findViewById<TextView>(R.id.itemStatus).text = request.status
            row.setOnClickListener {
                startActivity(Intent(this, StudyRequestDetailsActivity::class.java).putExtra("request_id", request.id))
            }
            container.addView(row)
        }
    }
}
