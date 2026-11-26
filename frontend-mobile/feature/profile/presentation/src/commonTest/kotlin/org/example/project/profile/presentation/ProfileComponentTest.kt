package org.example.project.profile.presentation

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProfileComponentTest {
    @Test fun backIntentReturnsToPreviousScreen() {
        var returned = false
        val component = ProfileComponent(context = DefaultComponentContext(LifecycleRegistry()), onBack = { returned = true })
        component.onIntent(ProfileIntent.Back)
        assertTrue(returned)
    }

    @Test fun openChatIntentOpensChat() {
        var chatOpened = false
        val component = ProfileComponent(context = DefaultComponentContext(LifecycleRegistry()), onChat = { chatOpened = true })
        component.onIntent(ProfileIntent.OpenChat)
        assertTrue(chatOpened)
    }

    @Test fun logoutIntentTriggersLogout() {
        var loggedOut = false
        val component = ProfileComponent(context = DefaultComponentContext(LifecycleRegistry()), onLogout = { loggedOut = true })
        component.onIntent(ProfileIntent.Logout)
        assertTrue(loggedOut)
    }

    @Test fun profilePayloadIsParsedOnInjectedDefaultDispatcher() = runTest {
        val defaultScheduler = TestCoroutineScheduler()
        val component = ProfileComponent(
            context = DefaultComponentContext(LifecycleRegistry()),
            fetchAccessToken = { "header.eyJlbWFpbCI6InVzZXJAZXhhbXBsZS5jb20ifQ.signature" },
            dispatcher = StandardTestDispatcher(testScheduler),
            defaultDispatcher = StandardTestDispatcher(defaultScheduler),
        )

        testScheduler.runCurrent()
        assertEquals("", component.state.value.email)

        defaultScheduler.runCurrent()
        testScheduler.runCurrent()
        assertEquals("user@example.com", component.state.value.email)
    }
}
