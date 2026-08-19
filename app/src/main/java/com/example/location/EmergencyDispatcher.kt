package com.example.location

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import com.example.model.DispatchChannel
import com.example.model.StealthSettings
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LocationInfo(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timeMillis: Long
)

sealed class DispatchStatus {
    object Idle : DispatchStatus()
    object AcquiringLocation : DispatchStatus()
    data class ReadyToDispatch(
        val isTest: Boolean,
        val location: LocationInfo?,
        val messageText: String,
        val channel: DispatchChannel,
        val targetPhone: String
    ) : DispatchStatus()
    data class Completed(val message: String) : DispatchStatus()
    data class Failed(val error: String) : DispatchStatus()
}

class EmergencyDispatcher(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocation || coarseLocation
    }

    suspend fun obtainLocation(timeoutMs: Long = 5000L): LocationInfo? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) {
            return@withContext null
        }

        try {
            // First try high accuracy current location with timeout
            val cts = CancellationTokenSource()
            val location = withTimeoutOrNull(timeoutMs) {
                try {
                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cts.token
                    ).await()
                } catch (_: Exception) {
                    null
                }
            }

            if (location != null) {
                return@withContext LocationInfo(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy,
                    timeMillis = location.time
                )
            }

            // Fallback: Check last known location
            val lastLocation = try {
                fusedLocationClient.lastLocation.await()
            } catch (_: Exception) {
                null
            }

            if (lastLocation != null) {
                return@withContext LocationInfo(
                    latitude = lastLocation.latitude,
                    longitude = lastLocation.longitude,
                    accuracy = lastLocation.accuracy,
                    timeMillis = lastLocation.time
                )
            }

            // Fallback 2: Direct LocationManager
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (locationManager != null) {
                val gpsLoc: Location? = try {
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    } else null
                } catch (_: SecurityException) { null }

                val netLoc: Location? = try {
                    if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    } else null
                } catch (_: SecurityException) { null }

                val best = gpsLoc ?: netLoc
                if (best != null) {
                    return@withContext LocationInfo(
                        latitude = best.latitude,
                        longitude = best.longitude,
                        accuracy = best.accuracy,
                        timeMillis = best.time
                    )
                }
            }
        } catch (_: Exception) {
            // Silently fallback if error
        }

        return@withContext null
    }

    fun getBatteryLevel(): Int {
        return try {
            val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
                context.registerReceiver(null, filter)
            }
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level >= 0 && scale > 0) {
                (level * 100 / scale.toFloat()).toInt()
            } else {
                -1
            }
        } catch (_: Exception) {
            -1
        }
    }

    fun buildEmergencyMessage(
        settings: StealthSettings,
        location: LocationInfo?,
        isTest: Boolean = false
    ): String {
        val sb = StringBuilder()
        if (isTest) {
            sb.append("⚠️ [TEST SOS - NO EMERGENCY - PLEASE IGNORE] ⚠️\n\n")
        }

        if (location != null && settings.includeLocation) {
            sb.append(settings.customMessagePrefix)
            sb.append("\n\n📍 My Live GPS Location:\n")
            sb.append("https://maps.google.com/?q=${location.latitude},${location.longitude}\n")
            sb.append("(Accuracy: ~${location.accuracy.toInt()}m)")
        } else {
            sb.append("URGENT: I need help. Location unavailable. Please call me immediately.")
        }

        if (settings.includeTimestamp) {
            val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss (z)", Locale.getDefault()).format(Date())
            sb.append("\n\n⏰ Timestamp: ").append(timeStr)
        }

        if (settings.includeBatteryInfo) {
            val battery = getBatteryLevel()
            if (battery >= 0) {
                sb.append("\n🔋 Device Battery: ").append(battery).append("%")
            }
        }

        return sb.toString()
    }

    fun executeDispatch(
        settings: StealthSettings,
        messageText: String
    ): Boolean {
        val phone = cleanPhoneNumber(settings.emergencyContactPhone)
        val channel = settings.dispatchChannel

        return try {
            when (channel) {
                DispatchChannel.WHATSAPP -> dispatchWhatsApp(phone, messageText)
                DispatchChannel.SMS -> dispatchSms(phone, messageText)
                DispatchChannel.SYSTEM_CHOOSER -> dispatchSystemChooser(phone, messageText)
            }
        } catch (_: Exception) {
            // Fallback to generic SMS / Share Intent
            try {
                dispatchSms(phone, messageText)
            } catch (_: Exception) {
                dispatchSystemChooser(phone, messageText)
            }
        }
    }

    private fun cleanPhoneNumber(raw: String): String {
        return raw.replace(Regex("[^0-9+]"), "").trim()
    }

    private fun dispatchWhatsApp(phone: String, text: String): Boolean {
        val encoded = URLEncoder.encode(text, "UTF-8")
        val url = if (phone.isNotEmpty()) {
            val numericPhone = phone.replace("+", "")
            "https://api.whatsapp.com/send?phone=$numericPhone&text=$encoded"
        } else {
            "https://api.whatsapp.com/send?text=$encoded"
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        return try {
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            // If WhatsApp is not installed or web link fails, fallback to SMS
            dispatchSms(phone, text)
        }
    }

    private fun dispatchSms(phone: String, text: String): Boolean {
        val uri = if (phone.isNotEmpty()) {
            Uri.parse("smsto:$phone")
        } else {
            Uri.parse("smsto:")
        }

        val smsIntent = Intent(Intent.ACTION_SENDTO, uri).apply {
            putExtra("sms_body", text)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        return try {
            context.startActivity(smsIntent)
            true
        } catch (_: Exception) {
            dispatchSystemChooser(phone, text)
        }
    }

    private fun dispatchSystemChooser(phone: String, text: String): Boolean {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            if (phone.isNotEmpty()) {
                putExtra("address", phone)
            }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val chooser = Intent.createChooser(sendIntent, "🚨 Dispatch Emergency SOS").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(chooser)
        return true
    }
}
