package org.example.project.profile.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ProfileState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val isLoggingOut: Boolean = false,
) {
    val displayName: String get() {
        val full = "$firstName $lastName".trim()
        return if (full.isNotBlank()) full else if (email.isNotBlank()) email.substringBefore('@') else "Пользователь"
    }
    val avatarInitial: String get() = displayName.firstOrNull()?.uppercase() ?: "U"
}

sealed interface ProfileIntent {
    data object Back : ProfileIntent
    data object OpenCatalog : ProfileIntent
    data object Logout : ProfileIntent
}

class ProfileComponent(
    context: ComponentContext,
    private val fetchAccessToken: (suspend () -> String?)? = null,
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val onBack: () -> Unit = {},
    private val onCatalog: () -> Unit = {},
    private val onLogout: () -> Unit = {},
) : ComponentContext by context {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val mutableState = MutableStateFlow(ProfileState())
    val state = mutableState.asStateFlow()

    init {
        lifecycle.doOnDestroy { scope.cancel() }
        loadProfile()
    }

    private fun loadProfile() {
        val fetch = fetchAccessToken ?: return
        scope.launch {
            try {
                val token = fetch()
                if (!token.isNullOrBlank()) {
                    val claims = withContext(defaultDispatcher) { parseJwtPayload(token) }
                    val email = claims["email"] ?: claims["sub"] ?: ""
                    val firstName = claims["first_name"] ?: claims["given_name"] ?: if (email.contains('@')) email.substringBefore('@').replaceFirstChar { it.uppercase() } else ""
                    val lastName = claims["last_name"] ?: claims["family_name"] ?: ""
                    mutableState.value = ProfileState(
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                /* Retain default profile state */
            }
        }
    }

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.Back -> onBack()
            ProfileIntent.OpenCatalog -> onCatalog()
            ProfileIntent.Logout -> performLogout()
        }
    }

    private fun performLogout() {
        if (mutableState.value.isLoggingOut) return
        mutableState.value = mutableState.value.copy(isLoggingOut = true)
        onLogout()
    }

    private fun parseJwtPayload(jwt: String): Map<String, String> {
        return try {
            val parts = jwt.split(".")
            if (parts.size < 2) return emptyMap()
            val payloadBase64 = parts[1]
                .replace('-', '+')
                .replace('_', '/')
            val padded = payloadBase64 + "=".repeat((4 - payloadBase64.length % 4) % 4)
            val jsonString = decodeBase64ToString(padded)
            val map = mutableMapOf<String, String>()
            val regex = Regex(""""([^"]+)":\s*"([^"]+)"""")
            regex.findAll(jsonString).forEach { match ->
                map[match.groupValues[1]] = match.groupValues[2]
            }
            map
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun decodeBase64ToString(base64: String): String {
        val table = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
        var buffer = 0
        var bits = 0
        val out = StringBuilder()
        for (ch in base64) {
            if (ch == '=') break
            val idx = table.indexOf(ch)
            if (idx >= 0) {
                buffer = (buffer shl 6) or idx
                bits += 6
                if (bits >= 8) {
                    bits -= 8
                    out.append(((buffer shr bits) and 0xFF).toChar())
                }
            }
        }
        return out.toString()
    }
}
