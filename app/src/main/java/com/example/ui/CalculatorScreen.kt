package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.Operation
import com.example.ui.theme.CalcBlack
import com.example.ui.theme.CalcEqualsBg
import com.example.ui.theme.CalcEqualsText
import com.example.ui.theme.CalcFuncKeyBg
import com.example.ui.theme.CalcFuncKeyText
import com.example.ui.theme.CalcNumKeyBg
import com.example.ui.theme.CalcNumKeyText
import com.example.ui.theme.CalcOpKeyActiveBg
import com.example.ui.theme.CalcOpKeyActiveText
import com.example.ui.theme.CalcOpKeyBg
import com.example.ui.theme.CalcOpKeyText
import com.example.ui.theme.StealthShieldBlue
import com.example.ui.theme.SubtlePillIndicator
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    onRequestLocationPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayValue by viewModel.displayValue.collectAsState()
    val expressionPreview by viewModel.expressionPreview.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsState()
    val sosState by viewModel.sosState.collectAsState()
    val activeOp by viewModel.activeOperation.collectAsState()

    val isClearActive = displayValue != "0"

    Scaffold(
        containerColor = CalcBlack,
        contentWindowInsets = WindowInsets.statusBars,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CalcBlack)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                // Top Geometric Balance subtle pill indicator & covert trigger area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(SubtlePillIndicator)
                    )
                }

                // Covert Top Bar with secret subtle tap area
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Discreet secret indicator
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { viewModel.openSettings() }
                            )
                            .testTag("covert_settings_shortcut"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(if (sosState.isDispatching) StealthShieldBlue else Color.Transparent)
                        )
                    }

                    // Expression preview
                    Text(
                        text = expressionPreview,
                        color = TextSecondary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Light,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End,
                        modifier = Modifier.testTag("calc_expression_preview")
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Covert dispatch banner (slides in on top of calculator)
                CovertDispatchBanner(
                    state = sosState,
                    onDismiss = { viewModel.dismissSosState() }
                )

                // Large Main Display with dynamic auto-scaling font size
                val displayFontSize = when {
                    displayValue.length > 9 -> 44.sp
                    displayValue.length > 6 -> 56.sp
                    else -> 72.sp
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 16.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Text(
                        text = displayValue,
                        color = TextPrimary,
                        fontSize = displayFontSize,
                        fontWeight = FontWeight.Light,
                        letterSpacing = (-1.5).sp,
                        maxLines = 1,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calc_main_display")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Keypad Layout (5 rows with Geometric Balance 12dp spacing)
                // Row 1: AC/C, +/-, %, ÷
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CalculatorButton(
                        symbol = if (isClearActive) "C" else "AC",
                        backgroundColor = CalcFuncKeyBg,
                        textColor = CalcFuncKeyText,
                        modifier = Modifier.weight(1f),
                        testTag = "calc_btn_clear",
                        onClick = { viewModel.onClear() }
                    )
                    CalculatorButton(
                        symbol = "±",
                        backgroundColor = CalcFuncKeyBg,
                        textColor = CalcFuncKeyText,
                        modifier = Modifier.weight(1f),
                        testTag = "calc_btn_toggle_sign",
                        onClick = { viewModel.onToggleSign() }
                    )
                    CalculatorButton(
                        symbol = "%",
                        backgroundColor = CalcFuncKeyBg,
                        textColor = CalcFuncKeyText,
                        modifier = Modifier.weight(1f),
                        testTag = "calc_btn_percent",
                        onClick = { viewModel.onPercent() }
                    )
                    OperationButton(
                        operation = Operation.DIVIDE,
                        isActive = activeOp == Operation.DIVIDE,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.onOperation(Operation.DIVIDE) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 2: 7, 8, 9, ×
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DigitButton(7, Modifier.weight(1f)) { viewModel.onDigit(7) }
                    DigitButton(8, Modifier.weight(1f)) { viewModel.onDigit(8) }
                    DigitButton(9, Modifier.weight(1f)) { viewModel.onDigit(9) }
                    OperationButton(
                        operation = Operation.MULTIPLY,
                        isActive = activeOp == Operation.MULTIPLY,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.onOperation(Operation.MULTIPLY) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 3: 4, 5, 6, −
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DigitButton(4, Modifier.weight(1f)) { viewModel.onDigit(4) }
                    DigitButton(5, Modifier.weight(1f)) { viewModel.onDigit(5) }
                    DigitButton(6, Modifier.weight(1f)) { viewModel.onDigit(6) }
                    OperationButton(
                        operation = Operation.SUBTRACT,
                        isActive = activeOp == Operation.SUBTRACT,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.onOperation(Operation.SUBTRACT) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 4: 1, 2, 3, +
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DigitButton(1, Modifier.weight(1f)) { viewModel.onDigit(1) }
                    DigitButton(2, Modifier.weight(1f)) { viewModel.onDigit(2) }
                    DigitButton(3, Modifier.weight(1f)) { viewModel.onDigit(3) }
                    OperationButton(
                        operation = Operation.ADD,
                        isActive = activeOp == Operation.ADD,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.onOperation(Operation.ADD) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 5: 0 (wide), ., =
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CalculatorButton(
                        symbol = "0",
                        backgroundColor = CalcNumKeyBg,
                        textColor = CalcNumKeyText,
                        isWide = true,
                        modifier = Modifier.weight(2f),
                        testTag = "calc_btn_0",
                        onClick = { viewModel.onDigit(0) }
                    )
                    CalculatorButton(
                        symbol = ".",
                        backgroundColor = CalcNumKeyBg,
                        textColor = CalcNumKeyText,
                        modifier = Modifier.weight(1f),
                        testTag = "calc_btn_decimal",
                        onClick = { viewModel.onDecimal() }
                    )
                    CalculatorButton(
                        symbol = "=",
                        backgroundColor = CalcEqualsBg,
                        textColor = CalcEqualsText,
                        modifier = Modifier.weight(1f),
                        testTag = "calc_btn_equals",
                        onClick = { viewModel.onEquals() }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Secret Settings Modal Sheet
        if (isSettingsOpen) {
            DiscreetSettingsModal(
                settings = settings,
                hasLocationPermission = viewModel.hasLocationPermission(),
                onRequestLocationPermission = onRequestLocationPermission,
                onSave = { updated -> viewModel.updateSettings(updated) },
                onTestTrigger = { viewModel.triggerSos(isTest = true) },
                onDismiss = { viewModel.closeSettings() }
            )
        }

        // Test Result Dialog
        if (sosState.isTest && !sosState.isDispatching && sosState.generatedMessage != null) {
            SosTestResultDialog(
                state = sosState,
                onDismiss = { viewModel.dismissSosState() }
            )
        }
    }
}

@Composable
private fun DigitButton(
    digit: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    CalculatorButton(
        symbol = digit.toString(),
        backgroundColor = CalcNumKeyBg,
        textColor = CalcNumKeyText,
        modifier = modifier,
        testTag = "calc_btn_$digit",
        onClick = onClick
    )
}

@Composable
private fun OperationButton(
    operation: Operation,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isActive) CalcOpKeyActiveBg else CalcOpKeyBg,
        label = "op_bg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isActive) CalcOpKeyActiveText else CalcOpKeyText,
        label = "op_text"
    )

    CalculatorButton(
        symbol = operation.symbol,
        backgroundColor = bgColor,
        textColor = textColor,
        modifier = modifier,
        testTag = "calc_btn_${operation.name.lowercase()}",
        onClick = onClick
    )
}
