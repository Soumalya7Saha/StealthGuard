package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DispatchChannel
import com.example.model.StealthSettings
import com.example.ui.theme.CalcBlack
import com.example.ui.theme.CalcCardDark
import com.example.ui.theme.CalcOpKeyBg
import com.example.ui.theme.CalcSurfaceDark
import com.example.ui.theme.DividerDark
import com.example.ui.theme.StealthAlertRed
import com.example.ui.theme.StealthShieldBlue
import com.example.ui.theme.StealthSuccessGreen
import com.example.ui.theme.StealthWarningAmber
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscreetSettingsModal(
    settings: StealthSettings,
    hasLocationPermission: Boolean,
    onRequestLocationPermission: () -> Unit,
    onSave: (StealthSettings) -> Unit,
    onTestTrigger: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var emergencyPhone by remember { mutableStateOf(settings.emergencyContactPhone) }
    var sosCode by remember { mutableStateOf(settings.sosTriggerCode) }
    var settingsCode by remember { mutableStateOf(settings.settingsTriggerCode) }
    var selectedChannel by remember { mutableStateOf(settings.dispatchChannel) }
    var messagePrefix by remember { mutableStateOf(settings.customMessagePrefix) }
    var includeLocation by remember { mutableStateOf(settings.includeLocation) }
    var includeBattery by remember { mutableStateOf(settings.includeBatteryInfo) }
    var includeTimestamp by remember { mutableStateOf(settings.includeTimestamp) }
    var hapticFeedback by remember { mutableStateOf(settings.hapticFeedbackEnabled) }
    var silentMode by remember { mutableStateOf(settings.silentCovertMode) }
    var fakeResult by remember { mutableStateOf(settings.fakeCalculationResult) }
    var showGuide by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CalcSurfaceDark,
        contentColor = Color.White,
        dragHandle = null,
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .testTag("settings_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CalcSurfaceDark)
        ) {
            // Header Bar
            Surface(
                color = CalcCardDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(StealthShieldBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Security Center",
                                tint = StealthShieldBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "StealthGuard Settings",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Covert SOS & Triggers Hub",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Location Permission Banner if not granted
                if (!hasLocationPermission) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StealthWarningAmber.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, StealthWarningAmber.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location Warning",
                                    tint = StealthWarningAmber,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Location Permission Needed",
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Grant permission so SOS messages include your live GPS Google Maps pin.",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = onRequestLocationPermission,
                                colors = ButtonDefaults.buttonColors(containerColor = StealthWarningAmber),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("btn_grant_location_permission")
                            ) {
                                Text("Allow", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Section 1: Emergency Contact Info
                SectionCard(title = "Emergency Contact", icon = Icons.Default.Phone) {
                    OutlinedTextField(
                        value = emergencyPhone,
                        onValueChange = { emergencyPhone = it },
                        label = { Text("Emergency Phone Number (with Country Code)") },
                        placeholder = { Text("+1 (555) 019-2834") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        colors = outlinedTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_emergency_phone"),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = StealthShieldBlue
                            )
                        }
                    )
                    Text(
                        text = "Include country code (e.g. +1 for US, +44 for UK, +91 for India) for seamless WhatsApp and SMS delivery.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                // Section 2: Covert Trigger Codes
                SectionCard(title = "Covert Passcodes", icon = Icons.Default.Lock) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = sosCode,
                            onValueChange = { if (it.length <= 8) sosCode = it },
                            label = { Text("SOS Code") },
                            placeholder = { Text("911") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = outlinedTextFieldColors(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_sos_code"),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Emergency,
                                    contentDescription = null,
                                    tint = StealthAlertRed
                                )
                            }
                        )

                        OutlinedTextField(
                            value = settingsCode,
                            onValueChange = { if (it.length <= 8) settingsCode = it },
                            label = { Text("Settings Code") },
                            placeholder = { Text("0000") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = outlinedTextFieldColors(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_settings_code"),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = StealthShieldBlue
                                )
                            }
                        )
                    }

                    Text(
                        text = "Type your code on the calculator and tap '=' to trigger the secret action.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                // Section 3: Dispatch Channel
                SectionCard(title = "Preferred Dispatch Channel", icon = Icons.AutoMirrored.Filled.Message) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DispatchChannel.values().forEach { channel ->
                            val isSelected = selectedChannel == channel
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) StealthShieldBlue.copy(alpha = 0.12f) else CalcBlack.copy(alpha = 0.4f))
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) StealthShieldBlue else DividerDark,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedChannel = channel }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .testTag("radio_channel_${channel.name}"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedChannel = channel },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = StealthShieldBlue,
                                        unselectedColor = TextSecondary
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = channel.displayName,
                                        color = if (isSelected) Color.White else TextSecondary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = channel.description,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 4: Custom Message & Data Toggles
                SectionCard(title = "SOS Message Template", icon = Icons.Default.Emergency) {
                    OutlinedTextField(
                        value = messagePrefix,
                        onValueChange = { messagePrefix = it },
                        label = { Text("SOS Alert Message Header") },
                        placeholder = { Text("URGENT SOS: I need immediate assistance!") },
                        maxLines = 3,
                        colors = outlinedTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_message_prefix")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SettingToggleRow(
                        title = "Attach Live GPS Google Maps Link",
                        subtitle = "Includes real-time high-accuracy coordinates (e.g. maps.google.com/?q=...)",
                        icon = Icons.Default.LocationOn,
                        iconTint = StealthShieldBlue,
                        checked = includeLocation,
                        onCheckedChange = { includeLocation = it },
                        testTag = "toggle_include_location"
                    )

                    SettingToggleRow(
                        title = "Include Battery Status & Timestamp",
                        subtitle = "Attaches remaining device battery percentage and exact time",
                        icon = Icons.Default.BatteryChargingFull,
                        iconTint = StealthSuccessGreen,
                        checked = includeBattery && includeTimestamp,
                        onCheckedChange = {
                            includeBattery = it
                            includeTimestamp = it
                        },
                        testTag = "toggle_include_battery"
                    )
                }

                // Section 5: Stealth & Covert Options
                SectionCard(title = "Stealth & Disguise Options", icon = Icons.Default.VisibilityOff) {
                    SettingToggleRow(
                        title = "Haptic Vibration Feedback",
                        subtitle = "Vibrate softly on calculator key presses",
                        icon = Icons.Default.Vibration,
                        iconTint = CalcOpKeyBg,
                        checked = hapticFeedback,
                        onCheckedChange = { hapticFeedback = it },
                        testTag = "toggle_haptic"
                    )

                    SettingToggleRow(
                        title = "Silent Covert SOS Trigger",
                        subtitle = "Mute vibrations when emergency SOS triggers so onlookers notice nothing",
                        icon = Icons.Default.VisibilityOff,
                        iconTint = StealthAlertRed,
                        checked = silentMode,
                        onCheckedChange = { silentMode = it },
                        testTag = "toggle_silent_mode"
                    )

                    OutlinedTextField(
                        value = fakeResult,
                        onValueChange = { if (it.length <= 6) fakeResult = it },
                        label = { Text("Fake Result on Screen when SOS Triggers") },
                        placeholder = { Text("0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        singleLine = true,
                        colors = outlinedTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .testTag("input_fake_result")
                    )
                }

                // Section 6: Test SOS Trigger
                Card(
                    colors = CardDefaults.cardColors(containerColor = StealthAlertRed.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, StealthAlertRed.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Emergency,
                                contentDescription = null,
                                tint = StealthAlertRed,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Verify Emergency Setup",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Test your GPS and messaging dispatch safely. The test message will be clearly tagged with '[TEST SOS - PLEASE IGNORE]'.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onTestTrigger,
                            colors = ButtonDefaults.buttonColors(containerColor = StealthAlertRed),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_test_sos_trigger")
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Launch Safe SOS Test", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }

                // Section 7: How StealthGuard Works (Collapsible)
                Card(
                    colors = CardDefaults.cardColors(containerColor = CalcCardDark),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showGuide = !showGuide },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = StealthShieldBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "How StealthGuard Operates",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }
                            Text(
                                text = if (showGuide) "Hide" else "Show",
                                color = StealthShieldBlue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (showGuide) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "1. The app looks and functions exactly like a native calculator.\n\n" +
                                        "2. When in danger, type your SOS code (${sosCode.ifEmpty { "911" }}) and tap '='.\n\n" +
                                        "3. The app secretly acquires high-accuracy GPS coordinates in the background.\n\n" +
                                        "4. It instantly opens WhatsApp or SMS to your emergency contact with your coordinates, battery level, and help message.\n\n" +
                                        "5. If GPS is unavailable, it gracefully sends an urgent fallback message.\n\n" +
                                        "6. Type ${settingsCode.ifEmpty { "0000" }} and tap '=' anytime to return here.",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Bottom Action Bar
            Surface(
                color = CalcCardDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("btn_cancel_settings")
                    ) {
                        Text("Cancel", fontSize = 15.sp)
                    }

                    Button(
                        onClick = {
                            val updated = settings.copy(
                                emergencyContactPhone = emergencyPhone.trim(),
                                sosTriggerCode = sosCode.trim().ifEmpty { "911" },
                                settingsTriggerCode = settingsCode.trim().ifEmpty { "0000" },
                                dispatchChannel = selectedChannel,
                                customMessagePrefix = messagePrefix.trim().ifEmpty { "URGENT SOS: I need immediate assistance!" },
                                includeLocation = includeLocation,
                                includeBatteryInfo = includeBattery,
                                includeTimestamp = includeTimestamp,
                                hapticFeedbackEnabled = hapticFeedback,
                                silentCovertMode = silentMode,
                                fakeCalculationResult = fakeResult.trim().ifEmpty { "0" }
                            )
                            onSave(updated)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StealthShieldBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(50.dp)
                            .testTag("btn_save_settings")
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save & Disguise", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CalcCardDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = StealthShieldBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            content()
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = StealthShieldBlue,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = CalcBlack
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = StealthShieldBlue,
    unfocusedBorderColor = DividerDark,
    focusedLabelColor = StealthShieldBlue,
    unfocusedLabelColor = TextSecondary,
    cursorColor = StealthShieldBlue,
    focusedPlaceholderColor = TextSecondary,
    unfocusedPlaceholderColor = TextSecondary
)
