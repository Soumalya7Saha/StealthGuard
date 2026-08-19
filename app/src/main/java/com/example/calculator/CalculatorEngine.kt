package com.example.calculator

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

enum class Operation(val symbol: String) {
    ADD("+"),
    SUBTRACT("−"),
    MULTIPLY("×"),
    DIVIDE("÷")
}

sealed class CalculatorEvent {
    data class SecretTriggered(val code: String) : CalculatorEvent()
    data class NormalResult(val result: String) : CalculatorEvent()
    object Cleared : CalculatorEvent()
}

class CalculatorEngine {
    var displayValue: String = "0"
        private set

    var expressionPreview: String = ""
        private set

    private var currentInput: String = "0"
    private var pendingOperand: BigDecimal? = null
    private var pendingOperation: Operation? = null
    private var isNewEntry: Boolean = true
    private var rawPasscodeBuffer: String = "0"

    fun onDigit(digit: Int): CalculatorEvent? {
        if (isNewEntry) {
            currentInput = digit.toString()
            isNewEntry = false
            rawPasscodeBuffer = digit.toString()
        } else {
            if (currentInput == "0" && digit != 0) {
                currentInput = digit.toString()
            } else if (currentInput != "0" && currentInput.replace(",", "").replace("-", "").length < 12) {
                currentInput += digit.toString()
            }
            rawPasscodeBuffer += digit.toString()
        }
        displayValue = formatNumberString(currentInput)
        return null
    }

    fun onDecimal(): CalculatorEvent? {
        if (isNewEntry) {
            currentInput = "0."
            isNewEntry = false
            rawPasscodeBuffer = "0."
        } else if (!currentInput.contains(".")) {
            currentInput += "."
            rawPasscodeBuffer += "."
        }
        displayValue = formatNumberString(currentInput)
        return null
    }

    fun onOperation(op: Operation): CalculatorEvent? {
        if (pendingOperand != null && pendingOperation != null && !isNewEntry) {
            calculateIntermediate()
        } else {
            pendingOperand = parseBigDecimal(currentInput)
        }
        pendingOperation = op
        isNewEntry = true
        rawPasscodeBuffer = ""
        expressionPreview = "${formatBigDecimal(pendingOperand ?: BigDecimal.ZERO)} ${op.symbol}"
        return null
    }

    fun onEquals(
        sosCode: String = "911",
        settingsCode: String = "0000"
    ): CalculatorEvent {
        val trimmedRaw = currentInput.trim().replace(",", "")
        val rawBuffer = rawPasscodeBuffer.trim()

        // Check if user entered a secret code directly (no pending operator)
        if (pendingOperation == null) {
            if (trimmedRaw == sosCode || rawBuffer == sosCode) {
                return CalculatorEvent.SecretTriggered(sosCode)
            }
            if (trimmedRaw == settingsCode || rawBuffer == settingsCode) {
                return CalculatorEvent.SecretTriggered(settingsCode)
            }
        }

        if (pendingOperand == null || pendingOperation == null) {
            return CalculatorEvent.NormalResult(displayValue)
        }

        val secondOperand = parseBigDecimal(currentInput)
        val firstOperand = pendingOperand ?: BigDecimal.ZERO
        val operation = pendingOperation ?: return CalculatorEvent.NormalResult(displayValue)

        expressionPreview = "${formatBigDecimal(firstOperand)} ${operation.symbol} ${formatBigDecimal(secondOperand)} ="

        val result = try {
            when (operation) {
                Operation.ADD -> firstOperand.add(secondOperand)
                Operation.SUBTRACT -> firstOperand.subtract(secondOperand)
                Operation.MULTIPLY -> firstOperand.multiply(secondOperand)
                Operation.DIVIDE -> {
                    if (secondOperand.compareTo(BigDecimal.ZERO) == 0) {
                        displayValue = "Error"
                        pendingOperand = null
                        pendingOperation = null
                        isNewEntry = true
                        return CalculatorEvent.NormalResult("Error")
                    } else {
                        firstOperand.divide(secondOperand, 10, RoundingMode.HALF_UP).stripTrailingZeros()
                    }
                }
            }
        } catch (_: Exception) {
            displayValue = "Error"
            pendingOperand = null
            pendingOperation = null
            isNewEntry = true
            return CalculatorEvent.NormalResult("Error")
        }

        currentInput = result.toPlainString()
        displayValue = formatBigDecimal(result)
        pendingOperand = null
        pendingOperation = null
        isNewEntry = true
        rawPasscodeBuffer = ""

        return CalculatorEvent.NormalResult(displayValue)
    }

    fun onClear(): CalculatorEvent {
        currentInput = "0"
        displayValue = "0"
        expressionPreview = ""
        pendingOperand = null
        pendingOperation = null
        isNewEntry = true
        rawPasscodeBuffer = "0"
        return CalculatorEvent.Cleared
    }

    fun onToggleSign(): CalculatorEvent? {
        if (currentInput == "0" || displayValue == "Error") return null
        currentInput = if (currentInput.startsWith("-")) {
            currentInput.substring(1)
        } else {
            "-$currentInput"
        }
        displayValue = formatNumberString(currentInput)
        return null
    }

    fun onPercent(): CalculatorEvent? {
        if (displayValue == "Error") return null
        val current = parseBigDecimal(currentInput)
        val percentValue = if (pendingOperand != null && (pendingOperation == Operation.ADD || pendingOperation == Operation.SUBTRACT)) {
            pendingOperand!!.multiply(current).divide(BigDecimal(100), 10, RoundingMode.HALF_UP)
        } else {
            current.divide(BigDecimal(100), 10, RoundingMode.HALF_UP)
        }
        currentInput = percentValue.stripTrailingZeros().toPlainString()
        displayValue = formatBigDecimal(percentValue)
        return null
    }

    fun setFakeResult(result: String) {
        currentInput = result
        displayValue = formatNumberString(result)
        pendingOperand = null
        pendingOperation = null
        expressionPreview = ""
        isNewEntry = true
    }

    private fun calculateIntermediate() {
        val second = parseBigDecimal(currentInput)
        val first = pendingOperand ?: return
        val op = pendingOperation ?: return

        val res = try {
            when (op) {
                Operation.ADD -> first.add(second)
                Operation.SUBTRACT -> first.subtract(second)
                Operation.MULTIPLY -> first.multiply(second)
                Operation.DIVIDE -> if (second.compareTo(BigDecimal.ZERO) == 0) BigDecimal.ZERO else first.divide(second, 10, RoundingMode.HALF_UP)
            }
        } catch (_: Exception) {
            first
        }
        pendingOperand = res
        currentInput = res.toPlainString()
        displayValue = formatBigDecimal(res)
    }

    private fun parseBigDecimal(str: String): BigDecimal {
        return try {
            BigDecimal(str.replace(",", "").trim())
        } catch (_: Exception) {
            BigDecimal.ZERO
        }
    }

    private fun formatNumberString(raw: String): String {
        if (raw.isEmpty()) return "0"
        if (raw == "-") return "-"

        val isNegative = raw.startsWith("-")
        val clean = if (isNegative) raw.substring(1) else raw

        val parts = clean.split(".")
        val integerPart = parts[0]
        val formattedInt = if (integerPart.isEmpty()) "0" else {
            try {
                val num = integerPart.toLong()
                val symbols = DecimalFormatSymbols(Locale.US)
                val formatter = DecimalFormat("#,###", symbols)
                formatter.format(num)
            } catch (_: Exception) {
                integerPart
            }
        }

        val result = if (parts.size > 1) {
            "$formattedInt.${parts[1]}"
        } else if (raw.endsWith(".")) {
            "$formattedInt."
        } else {
            formattedInt
        }

        return if (isNegative) "-$result" else result
    }

    private fun formatBigDecimal(bd: BigDecimal): String {
        val stripped = bd.stripTrailingZeros()
        val plain = stripped.toPlainString()
        return formatNumberString(plain)
    }
}
