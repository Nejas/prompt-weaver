package org.example.project.network

import android.util.Log

internal actual object NetworkLog {
    private const val Tag = "PromptWeaverNetwork"

    actual fun debug(message: String) {
        Log.d(Tag, message)
    }

    actual fun error(message: String) {
        Log.e(Tag, message)
    }
}
