package com.example

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.model.ScreenMode
import com.example.ui.ExamWebViewScreen
import com.example.ui.ForceLockedScreen
import com.example.ui.SupervisorPanelScreen
import com.example.ui.TokenGateScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ExamViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ExamViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val screenMode by viewModel.screenMode.collectAsState()
                val supervisorConfig by viewModel.supervisorConfig.collectAsState()
                val violations by viewModel.violations.collectAsState()
                val deviceInfo by viewModel.deviceInfo.collectAsState()
                val isVpnAlertShowing by viewModel.isVpnAlertShowing.collectAsState()
                val isFocusAlertShowing by viewModel.isFocusAlertShowing.collectAsState()
                val focusAlertMessage by viewModel.focusAlertMessage.collectAsState()
                val selectedRoom by viewModel.selectedRoom.collectAsState()

                // Dynamically apply or clear anti-cheat window flags (Screenshot blocker & immersive mode)
                LaunchedEffect(screenMode, supervisorConfig.screenshotBlockerEnabled) {
                    val inExam = screenMode == ScreenMode.EXAM_ACTIVE
                    val isProctor = screenMode == ScreenMode.SUPERVISOR_PANEL

                    if (inExam && supervisorConfig.screenshotBlockerEnabled) {
                        // Anti-screenshot & recording
                        window.setFlags(
                            WindowManager.LayoutParams.FLAG_SECURE,
                            WindowManager.LayoutParams.FLAG_SECURE
                        )
                        // Immersive fullscreen
                        hideSystemBars()
                    } else {
                        // In Supervisor panel or Token Gate, anti-cheat is OFF
                        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                        showSystemBars()
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when (screenMode) {
                        ScreenMode.TOKEN_GATE -> {
                            Box(modifier = Modifier.safeDrawingPadding()) {
                                TokenGateScreen(
                                    deviceInfo = deviceInfo,
                                    selectedRoom = selectedRoom,
                                    wifiPresets = viewModel.wifiPresets,
                                    onSelectRoom = { viewModel.selectRoom(it) },
                                    onSubmitToken = { token -> viewModel.startExam(token) },
                                    onEnterSupervisor = { viewModel.enterSupervisorMode() }
                                )
                            }
                        }

                        ScreenMode.EXAM_ACTIVE -> {
                            Box(modifier = Modifier.safeDrawingPadding()) {
                                ExamWebViewScreen(
                                    config = supervisorConfig,
                                    violations = violations,
                                    isVpnAlertShowing = isVpnAlertShowing,
                                    isFocusAlertShowing = isFocusAlertShowing,
                                    focusAlertMessage = focusAlertMessage,
                                    onDismissFocusAlert = { viewModel.dismissFocusAlert() },
                                    onEnterSupervisor = { viewModel.enterSupervisorMode() },
                                    onExitExamWithPassword = { pw -> viewModel.exitExamWithPassword(pw) },
                                    onManualViolationReport = { title, desc ->
                                        viewModel.recordViolation(title, desc)
                                    }
                                )
                            }
                        }

                        ScreenMode.SUPERVISOR_PANEL -> {
                            Box(modifier = Modifier.safeDrawingPadding()) {
                                SupervisorPanelScreen(
                                    config = supervisorConfig,
                                    deviceInfo = deviceInfo,
                                    violations = violations,
                                    wifiPresets = viewModel.wifiPresets,
                                    selectedRoom = selectedRoom,
                                    onSelectRoom = { viewModel.selectRoom(it) },
                                    onUpdateConfig = { newConfig -> viewModel.updateSupervisorConfig(newConfig) },
                                    onClearViolations = { viewModel.clearViolations() },
                                    onRefreshDeviceStatus = { viewModel.refreshDeviceInfo() },
                                    onResumeExam = { viewModel.exitSupervisorMode(resumeExam = true) },
                                    onResetToTokenScreen = { viewModel.exitSupervisorMode(resumeExam = false) }
                                )
                            }
                        }

                        ScreenMode.FORCE_LOCKED_VIOLATION -> {
                            Box(modifier = Modifier.safeDrawingPadding()) {
                                ForceLockedScreen(
                                    violations = violations,
                                    onEnterSupervisor = { viewModel.enterSupervisorMode() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // Anti-Floating Window & Screen Switch Detector:
        // If app loses focus while student is in EXAM_ACTIVE mode, trigger violation
        if (!hasFocus && viewModel.screenMode.value == ScreenMode.EXAM_ACTIVE) {
            viewModel.handleFocusLost()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshDeviceInfo()
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun showSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.show(WindowInsetsCompat.Type.systemBars())
    }
}
