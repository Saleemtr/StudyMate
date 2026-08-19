package com.studymate.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast

class SearchPartnersActivity : Activity() {
    private lateinit var keyword: EditText
    private lateinit var availability: Spinner
    private lateinit var meetingMode: Spinner
    private lateinit var results: LinearLayout
    private lateinit var resultCount: TextView
    private lateinit var emptyResults: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_partners)
        setupBackButton()
        keyword = findViewById(R.id.searchKeyword)
        availability = findViewById(R.id.availabilityFilter)
        meetingMode = findViewById(R.id.modeFilter)
        results = findViewById(R.id.partnerResultsContainer)
        resultCount = findViewById(R.id.resultCount)
        emptyResults = findViewById(R.id.emptyPartnersText)

        findViewById<View>(R.id.searchPartnersAction).setOnClickListener { applyFilters() }
        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) = applyFilters()
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        availability.onItemSelectedListener = listener
        meetingMode.onItemSelectedListener = listener
        resultCount.text = "Loading students…"
        StudyPartnerRepository.load(this) { success, error ->
            if (!success) Toast.makeText(this, error ?: "Could not load students", Toast.LENGTH_LONG).show()
            applyFilters()
        }
    }

    private fun applyFilters() {
        val query = keyword.text.toString().trim()
        val availabilityValue = availability.selectedItem.toString()
        val modeValue = meetingMode.selectedItem.toString()
        val matches = StudyPartnerRepository.partners.filter { partner ->
            (query.isBlank() || listOf(partner.course, partner.topic, partner.name, partner.department)
                .any { it.contains(query, ignoreCase = true) }) &&
                (availabilityValue == "Any availability" || partner.availability == availabilityValue) &&
                (modeValue == "Any meeting mode" || partner.meetingMode == modeValue)
        }.sortedByDescending { it.matchScore }

        results.removeAllViews()
        resultCount.text = "${matches.size} matches"
        emptyResults.visibility = if (matches.isEmpty()) View.VISIBLE else View.GONE
        matches.forEach { partner ->
            val row = layoutInflater.inflate(R.layout.item_study_partner, results, false)
            row.findViewById<TextView>(R.id.partnerName).text = partner.name
            row.findViewById<TextView>(R.id.partnerCourse).text = "${partner.course} • ${partner.topic}"
            row.findViewById<TextView>(R.id.partnerAvailability).text = "${partner.availability} • ${partner.meetingMode}"
            row.findViewById<TextView>(R.id.partnerScore).text = "${partner.matchScore}% match"
            row.setOnClickListener {
                startActivity(Intent(this, PartnerDetailsActivity::class.java).putExtra("partner_id", partner.id))
            }
            results.addView(row)
        }
    }
}
