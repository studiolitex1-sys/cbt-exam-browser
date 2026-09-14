package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.model.DeviceSecurityInfo
import com.example.model.SchoolWifiPreset
import com.example.ui.TokenGateScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        TokenGateScreen(
          deviceInfo = DeviceSecurityInfo(),
          selectedRoom = "01",
          wifiPresets = listOf(
            SchoolWifiPreset("01", "Ruang01", "<@Ruang01>"),
            SchoolWifiPreset("02", "Ruang02", "<@Ruang02>")
          ),
          onSelectRoom = {},
          onSubmitToken = { true },
          onEnterSupervisor = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

