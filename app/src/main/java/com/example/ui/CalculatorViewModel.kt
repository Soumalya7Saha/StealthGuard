package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculator.CalculatorEngine
import com.example.calculator.CalculatorEvent
import com.example.calculator.Operation
import com.example.data.SettingsRepository
import com.example.location.EmergencyDispatcher
import com.example.location.LocationInfo
import com.example.model.StealthSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SosDispatchState(
    val isDispatching: Boolean = false,
    val statusMessage: String? = null,
    val location: LocationInfo? = null,
    val generatedMessage: String? = null,
    val isTest: Boolean = false
)

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsRepo = SettingsRepository(application.applicationContext)
    private val emergencyDispatcher = EmergencyDispatcher(application.applicationContext)
    private val engine = CalculatorEngine()

    val settings: StateFlow<StealthSettings> = settingsRepo.settings

    private val _displayValue = MutableStateFlow("0")
    val displayValue: StateFlow<String> = _displayValue.asStateFlow()

    private val _expressionPreview = MutableStateFlow("")
    val expressionPreview: StateFlow<String> = _expressionPreview.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val _sosState = MutableStateFlow(SosDispatchState())
    val sosState: StateFlow<SosDispatchState> = _sosState.asStateFlow()

    private val _activeOperation = MutableStateFlow<Operation?>(null)
    val activeOperation: StateFlow<Operation?> = _activeOperation.asStateFlow()

    fun onDigit(digit: Int) {
        vibrateIfNeeded()
        _activeOperation.value = null
        engine.onDigit(digit)
        syncDisplay()
    }

    fun onDecimal() {
        vibrateIfNeeded()
        _activeOperation.value = null
        engine.onDecimal()
        syncDisplay()
    }

    fun onOperation(op: Operation) {
        vibrateIfNeeded()
        _activeOperation.value = op
        engine.onOperation(op)
        syncDisplay()
    }

    fun onEquals() {
        vibrateIfNeeded()
        _activeOperation.value = null
        val currentSettings = settings.value
        val event = engine.onEquals(
            sosCode = currentSettings.sosTriggerCode,
            settingsCode = currentSettings.settingsTriggerCode
        )

        when (event) {
            is CalculatorEvent.SecretTriggered -> {
                if (event.code == currentSettings.sosTriggerCode) {
                    // Trigger Covert Emergency SOS!
                    // Disguise display with fake result (default "0" or configured result)
                    engine.setFakeResult(currentSettings.fakeCalculationResult)
                    syncDisplay()
                    triggerSos(isTest = false)
                } else if (event.code == currentSettings.settingsTriggerCode) {
                    // Open Secret Settings Modal
                    engine.onClear()
                    syncDisplay()
                    _isSettingsOpen.value = true
                }
            }
            is CalculatorEvent.NormalResult -> {
                syncDisplay()
            }
            is CalculatorEvent.Cleared -> {
                syncDisplay()
            }
        }
    }

    fun onClear() {
        vibrateIfNeeded()
        _activeOperation.value = null
        engine.onClear()
        syncDisplay()
    }

    fun onToggleSign() {
        vibrateIfNeeded()
        engine.onToggleSign()
        syncDisplay()
    }

    fun onPercent() {
        vibrateIfNeeded()
        engine.onPercent()
        syncDisplay()
    }

    fun openSettings() {
        _isSettingsOpen.value = true
    }

    fun closeSettings() {
        _isSettingsOpen.value = false
    }

    fun updateSettings(newSettings: StealthSettings) {
        settingsRepo.updateSettings(newSettings)
    }

    fun triggerSos(isTest: Boolean) {
        viewModelScope.launch {
            val currentSettings = settings.value

            _sosState.value = SosDispatchState(
                isDispatching = true,
                statusMessage = if (isTest) "Running Safe SOS Test..." else "Triggering Covert Emergency SOS...",
                isTest = isTest
            )

            // Covert double buzz if not in silent mode
            if (!currentSettings.silentCovertMode && currentSettings.hapticFeedbackEnabled) {
                vibrateCustom(pattern = longArrayOf(0, 100, 80, 100))
            }

            // Silently obtain GPS fix
            val location = if (currentSettings.includeLocation) {
                emergencyDispatcher.obtainLocation(timeoutMs = 4500L)
            } else {
                null
            }

            val sosMessage = emergencyDispatcher.buildEmergencyMessage(
                settings = currentSettings,
                location = location,
                isTest = isTest
            )

            // Dispatch immediately to WhatsApp / SMS
            val dispatched = emergencyDispatcher.executeDispatch(currentSettings, sosMessage)

            _sosState.value = SosDispatchState(
                isDispatching = false,
                statusMessage = if (dispatched) {
                    if (isTest) "Test dispatch launched successfully!" else "Emergency SOS dispatched."
                } else {
                    "Failed to launch dispatch channel."
                },
                location = location,
                generatedMessage = sosMessage,
                isTest = isTest
            )
        }
    }

    fun dismissSosState() {
        _sosState.value = SosDispatchState()
    }

    fun hasLocationPermission(): Boolean {
        return emergencyDispatcher.hasLocationPermission()
    }

    private fun syncDisplay() {
        _displayValue.value = engine.displayValue
        _expressionPreview.value = engine.expressionPreview
    }

    private fun vibrateIfNeeded() {
        if (settings.value.hapticFeedbackEnabled) {
            vibrateCustom(pattern = longArrayOf(0, 15))
        }
    }

    private fun vibrateCustom(pattern: LongArray) {
        try {
            val context = getApplication<Application>().applicationContext
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (pattern.size <= 2) {
                        vibrator.vibrate(VibrationEffect.createOneShot(pattern.last(), VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                    }
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(pattern, -1)
                }
            }
        } catch (_: Exception) {
            // Ignore vibration failure
        }
    }
}
