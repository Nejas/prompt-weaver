package org.example.project.settings.presentation

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class AppTheme { Light, Dark, System }
data class SettingsState(val theme: AppTheme = AppTheme.Dark, val textSize: Int = 16, val saveHistory: Boolean = true)
sealed interface SettingsIntent { data object Back : SettingsIntent; data class SelectTheme(val value: AppTheme) : SettingsIntent; data class ChangeTextSize(val value: Int) : SettingsIntent; data class SetSaveHistory(val value: Boolean) : SettingsIntent }
class SettingsComponent(context: ComponentContext, private val mutableState: MutableStateFlow<SettingsState>, private val onBack: () -> Unit) : ComponentContext by context {
    val state: StateFlow<SettingsState> = mutableState
    fun onIntent(intent: SettingsIntent) { when (intent) { SettingsIntent.Back -> onBack(); is SettingsIntent.SelectTheme -> mutableState.value = state.value.copy(theme = intent.value); is SettingsIntent.ChangeTextSize -> mutableState.value = state.value.copy(textSize = intent.value.coerceIn(12, 22)); is SettingsIntent.SetSaveHistory -> mutableState.value = state.value.copy(saveHistory = intent.value) } }
}
