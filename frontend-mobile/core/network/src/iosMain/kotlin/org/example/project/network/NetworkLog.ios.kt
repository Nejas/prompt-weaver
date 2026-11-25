package org.example.project.network

import platform.Foundation.NSLog

internal actual object NetworkLog {
    actual fun debug(message: String) {
        NSLog("PromptWeaverNetwork: $message")
    }

    actual fun error(message: String) {
        NSLog("PromptWeaverNetwork ERROR: $message")
    }
}
