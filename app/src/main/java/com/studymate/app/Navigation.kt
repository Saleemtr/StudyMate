package com.studymate.app

import android.app.Activity
import android.widget.ImageButton

fun Activity.setupBackButton() {
    findViewById<ImageButton?>(R.id.backButton)?.setOnClickListener { finish() }
}
