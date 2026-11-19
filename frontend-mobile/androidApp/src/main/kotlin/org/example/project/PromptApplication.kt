package org.example.project

import android.app.Application
import org.example.project.di.initializeDependencies

class PromptApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeDependencies("http://10.0.2.2:8000/")
    }
}
