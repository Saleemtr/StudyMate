package com.studymate.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class PartnerDetailsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_partner_details)
        val partner = StudyPartnerRepository.find(intent.getStringExtra("partner_id").orEmpty()) ?: run { finish(); return }
        findViewById<TextView>(R.id.partnerDetailsName).text = partner.name
        findViewById<TextView>(R.id.partnerDetailsDepartment).text = partner.department
        findViewById<TextView>(R.id.partnerDetailsScore).text = "${partner.matchScore}% match"
        findViewById<TextView>(R.id.partnerDetailsCourse).text = partner.course
        findViewById<TextView>(R.id.partnerDetailsTopic).text = partner.topic
        findViewById<TextView>(R.id.partnerDetailsAvailability).text = partner.availability
        findViewById<TextView>(R.id.partnerDetailsMode).text = "${partner.meetingMode} • ${partner.location}"
        findViewById<TextView>(R.id.partnerDetailsBio).text = partner.bio
        findViewById<Button>(R.id.contactPartnerButton).setOnClickListener {
            ChatStore.ensureConversation(this, partner.id)
            startActivity(Intent(this, ChatActivity::class.java).putExtra("partner_id", partner.id))
        }
    }
}
