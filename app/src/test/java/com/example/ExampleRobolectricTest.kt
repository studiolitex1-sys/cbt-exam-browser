package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.ScreenMode
import com.example.viewmodel.ExamViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("CBT Exam Browser", appName)
  }

  @Test
  fun `verify token and exit password validation`() {
    val application = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = ExamViewModel(application)

    // Token check
    assertTrue(viewModel.validateToken("132456"))
    assertFalse(viewModel.validateToken("wrong_token"))

    // Start exam with token
    assertTrue(viewModel.startExam("132456"))
    assertEquals(ScreenMode.EXAM_ACTIVE, viewModel.screenMode.value)

    // Exit password check
    assertTrue(viewModel.validateExitPassword("13456"))
    assertFalse(viewModel.validateExitPassword("12345"))

    // Exit with password
    assertTrue(viewModel.exitExamWithPassword("13456"))
    assertEquals(ScreenMode.TOKEN_GATE, viewModel.screenMode.value)
  }

  @Test
  fun `verify supervisor mode turns off anti cheat`() {
    val application = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = ExamViewModel(application)

    viewModel.enterSupervisorMode()
    assertEquals(ScreenMode.SUPERVISOR_PANEL, viewModel.screenMode.value)
    assertFalse(viewModel.isVpnAlertShowing.value)
    assertFalse(viewModel.isFocusAlertShowing.value)
  }

  @Test
  fun `verify wifi presets formatted correctly`() {
    val application = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = ExamViewModel(application)

    val presets = viewModel.wifiPresets
    assertTrue(presets.isNotEmpty())
    val room1 = presets.first()
    assertEquals("Ruang01", room1.ssid)
    assertEquals("<@Ruang01>", room1.password)
  }
}

