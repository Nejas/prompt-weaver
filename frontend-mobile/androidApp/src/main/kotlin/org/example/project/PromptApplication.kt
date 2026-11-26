package org.example.project

import android.app.Application
import org.example.project.di.initializeDependencies

class PromptApplication : Application() {
    lateinit var dependencies: org.koin.core.Koin
        private set
    override fun onCreate() {
        super.onCreate()
        dependencies = initializeDependencies(BuildConfig.GATEWAY_BASE_URL, AndroidTokenStore(this), AndroidLocalChatTemplateStore(this))
    }
}
