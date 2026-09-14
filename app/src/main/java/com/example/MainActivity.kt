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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.model.ScreenMode
import com.example.ui.ExamWebViewScreen
import com.example.ui.ForceLockedScreen
import com.example.ui.SupervisorPanelScreen
import com.example.ui.TokenGateScreen
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentCyanBright
import com.example.ui.theme.BorderLight
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SecurityRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
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

                var showSupervisorAuthDialog by remember { mutableStateOf(false) }
                var supervisorPasswordInput by remember { mutableStateOf("") }
                var supervisorAuthError by remember { mutableStateOf<String?>(null) }

                // Dynamically apply or clear anti-cheat window flags (Screenshot blocker & immersive mode)
                LaunchedEffect(screenMode, supervisorConfig.screenshotBlockerEnabled) {
                    val inExam = screenMode == ScreenMode.EXAM_ACTIVE

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
                                    onEnterSupervisor = {
                                        supervisorPasswordInput = ""
                                        supervisorAuthError = null
                                        showSupervisorAuthDialog = true
                                    }
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
                                    onEnterSupervisor = {
                                        supervisorPasswordInput = ""
                                        supervisorAuthError = null
                                        showSupervisorAuthDialog = true
                                    },
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
                                    onResetToTokenScreen = { viewModel.exitSupervisorMode(resumeExam = false) },
                                    onResetTokenToDefault = { viewModel.resetTokenToDefault() },
                                    onResetPasswordToDefault = { viewModel.resetPasswordToDefault() },
                                    onGenerateRandomToken = { viewModel.generateRandomToken() },
                                    onResetAllCredentials = { viewModel.resetAllCredentialsToDefault() }
                                )
                            }
                        }

                        ScreenMode.FORCE_LOCKED_VIOLATION -> {
                            Box(modifier = Modifier.safeDrawingPadding()) {
                                ForceLockedScreen(
                                    violations = violations,
                                    onEnterSupervisor = {
                                        supervisorPasswordInput = ""
                                        supervisorAuthError = null
                                        showSupervisorAuthDialog = true
                                    }
                                )
                            }
                        }
                    }

                    // Dialog Verifikasi Password Pengawas (00132)
                    if (showSupervisorAuthDialog) {
                        AlertDialog(
                            onDismissRequest = { showSupervisorAuthDialog = false },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = AccentCyanBright,
                                    modifier = Modifier.size(36.dp)
                                )
                            },
                            title = {
                                Text(
                                    text = "Verifikasi Pengawas",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                            },
                            text = {
                                Column {
                                    Text(
                                        text = "Masukkan Password Pengawas untuk membuka menu konfigurasi.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))

                                    OutlinedTextField(
                                        value = supervisorPasswordInput,
                                        onValueChange = {
                                            supervisorPasswordInput = it
                                            supervisorAuthError = null
                                        },
                                        label = { Text("Password Pengawas") },
                                        placeholder = { Text("Contoh: ${supervisorConfig.exitPassword}") },
                                        singleLine = true,
                                        visualTransformation = PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AccentCyanBright,
                                            unfocusedBorderColor = BorderLight,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedLabelColor = AccentCyanBright,
                                            unfocusedLabelColor = TextSecondary
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("supervisor_auth_input")
                                    )

                                    if (supervisorAuthError != null) {
                                        Text(
                                            text = supervisorAuthError.orEmpty(),
                                            color = SecurityRed,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(top = 6.dp)
                                        )
                                    }

                                    Text(
                                        text = "Password Default: 00132",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        if (viewModel.validateSupervisorPassword(supervisorPasswordInput)) {
                                            showSupervisorAuthDialog = false
                                            viewModel.enterSupervisorMode()
                                        } else {
                                            supervisorAuthError = "Password pengawas salah! (${supervisorConfig.exitPassword})"
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                                    modifier = Modifier.testTag("submit_supervisor_auth")
                                ) {
                                    Text("Buka Panel", color = TextPrimary, fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                OutlinedButton(onClick = { showSupervisorAuthDialog = false }) {
                                    Text("Batal", color = TextSecondary)
                                }
                            },
                            containerColor = DarkSurface,
                            shape = RoundedCornerShape(16.dp)
                        )
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
