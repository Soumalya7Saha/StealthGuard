package com.example.model

enum class DispatchChannel(val displayName: String, val description: String) {
    WHATSAPP("WhatsApp", "Direct WhatsApp message via wa.me / WhatsApp API"),
    SMS("Standard SMS", "Direct carrier SMS via native Messaging app"),
    SYSTEM_CHOOSER("System Chooser", "Prompt Android app chooser for WhatsApp, SMS, or Telegram")
}

data class StealthSettings(
    val sosTriggerCode: String = "911",
    val settingsTriggerCode: String = "0000",
    val emergencyContactPhone: String = "",
    val dispatchChannel: DispatchChannel = DispatchChannel.WHATSAPP,
    val customMessagePrefix: String = "URGENT SOS: I need immediate assistance!",
    val includeLocation: Boolean = true,
    val includeBatteryInfo: Boolean = true,
    val includeTimestamp: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val silentCovertMode: Boolean = false,
    val fakeCalculationResult: String = "0"
)
