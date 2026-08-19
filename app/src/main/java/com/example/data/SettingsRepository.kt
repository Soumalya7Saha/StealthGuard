package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.DispatchChannel
import com.example.model.StealthSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("stealth_guard_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<StealthSettings> = _settings.asStateFlow()

    private fun loadSettings(): StealthSettings {
        val channelName = prefs.getString(KEY_DISPATCH_CHANNEL, DispatchChannel.WHATSAPP.name)
        val channel = try {
            DispatchChannel.valueOf(channelName ?: DispatchChannel.WHATSAPP.name)
        } catch (_: Exception) {
            DispatchChannel.WHATSAPP
        }

        return StealthSettings(
            sosTriggerCode = prefs.getString(KEY_SOS_CODE, "911") ?: "911",
            settingsTriggerCode = prefs.getString(KEY_SETTINGS_CODE, "0000") ?: "0000",
            emergencyContactPhone = prefs.getString(KEY_PHONE, "") ?: "",
            dispatchChannel = channel,
            customMessagePrefix = prefs.getString(KEY_CUSTOM_MSG, "URGENT SOS: I need immediate assistance!")
                ?: "URGENT SOS: I need immediate assistance!",
            includeLocation = prefs.getBoolean(KEY_INC_LOCATION, true),
            includeBatteryInfo = prefs.getBoolean(KEY_INC_BATTERY, true),
            includeTimestamp = prefs.getBoolean(KEY_INC_TIMESTAMP, true),
            hapticFeedbackEnabled = prefs.getBoolean(KEY_HAPTIC, true),
            silentCovertMode = prefs.getBoolean(KEY_SILENT_MODE, false),
            fakeCalculationResult = prefs.getString(KEY_FAKE_RESULT, "0") ?: "0"
        )
    }

    fun updateSettings(newSettings: StealthSettings) {
        prefs.edit().apply {
            putString(KEY_SOS_CODE, newSettings.sosTriggerCode)
            putString(KEY_SETTINGS_CODE, newSettings.settingsTriggerCode)
            putString(KEY_PHONE, newSettings.emergencyContactPhone)
            putString(KEY_DISPATCH_CHANNEL, newSettings.dispatchChannel.name)
            putString(KEY_CUSTOM_MSG, newSettings.customMessagePrefix)
            putBoolean(KEY_INC_LOCATION, newSettings.includeLocation)
            putBoolean(KEY_INC_BATTERY, newSettings.includeBatteryInfo)
            putBoolean(KEY_INC_TIMESTAMP, newSettings.includeTimestamp)
            putBoolean(KEY_HAPTIC, newSettings.hapticFeedbackEnabled)
            putBoolean(KEY_SILENT_MODE, newSettings.silentCovertMode)
            putString(KEY_FAKE_RESULT, newSettings.fakeCalculationResult)
            apply()
        }
        _settings.value = newSettings
    }

    companion object {
        private const val KEY_SOS_CODE = "sos_trigger_code"
        private const val KEY_SETTINGS_CODE = "settings_trigger_code"
        private const val KEY_PHONE = "emergency_phone"
        private const val KEY_DISPATCH_CHANNEL = "dispatch_channel"
        private const val KEY_CUSTOM_MSG = "custom_message_prefix"
        private const val KEY_INC_LOCATION = "include_location"
        private const val KEY_INC_BATTERY = "include_battery"
        private const val KEY_INC_TIMESTAMP = "include_timestamp"
        private const val KEY_HAPTIC = "haptic_feedback"
        private const val KEY_SILENT_MODE = "silent_mode"
        private const val KEY_FAKE_RESULT = "fake_calculation_result"
    }
}
