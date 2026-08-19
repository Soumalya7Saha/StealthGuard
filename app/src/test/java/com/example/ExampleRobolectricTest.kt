package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.calculator.CalculatorEngine
import com.example.calculator.CalculatorEvent
import com.example.calculator.Operation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("StealthGuard", appName)
  }

  @Test
  fun `calculator basic arithmetic addition`() {
    val engine = CalculatorEngine()
    engine.onDigit(5)
    engine.onOperation(Operation.ADD)
    engine.onDigit(7)
    val event = engine.onEquals()
    assertTrue(event is CalculatorEvent.NormalResult)
    assertEquals("12", (event as CalculatorEvent.NormalResult).result)
  }

  @Test
  fun `calculator intercepts secret SOS code 911`() {
    val engine = CalculatorEngine()
    engine.onDigit(9)
    engine.onDigit(1)
    engine.onDigit(1)
    val event = engine.onEquals(sosCode = "911", settingsCode = "0000")
    assertTrue(event is CalculatorEvent.SecretTriggered)
    assertEquals("911", (event as CalculatorEvent.SecretTriggered).code)
  }

  @Test
  fun `calculator intercepts secret settings code 0000`() {
    val engine = CalculatorEngine()
    engine.onDigit(0)
    engine.onDigit(0)
    engine.onDigit(0)
    engine.onDigit(0)
    val event = engine.onEquals(sosCode = "911", settingsCode = "0000")
    assertTrue(event is CalculatorEvent.SecretTriggered)
    assertEquals("0000", (event as CalculatorEvent.SecretTriggered).code)
  }
}
