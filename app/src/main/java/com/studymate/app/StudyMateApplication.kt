package com.studymate.app

import android.app.Application

class StudyMateApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseBackend.initialize(this)
    }
}
