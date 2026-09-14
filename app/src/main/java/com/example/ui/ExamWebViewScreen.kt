package com.example.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.SecurityViolation
import com.example.model.SupervisorConfig
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentCyanBright
import com.example.ui.theme.BorderLight
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.SecurityAmber
import com.example.ui.theme.SecurityGreen
import com.example.ui.theme.SecurityRed
import com.example.ui.theme.SecurityRedBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ExamWebViewScreen(
    config: SupervisorConfig,
    violations: List<SecurityViolation>,
    isVpnAlertShowing: Boolean,
    isFocusAlertShowing: Boolean,
    focusAlertMessage: String,
    onDismissFocusAlert: () -> Unit,
    onEnterSupervisor: () -> Unit,
    onExitExamWithPassword: (String) -> Boolean,
    onManualViolationReport: (String, String) -> Unit
) {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var pageLoadingProgress by remember { mutableIntStateOf(0) }
    var isPageLoading by remember { mutableStateOf(true) }
    var webLoadError by remember { mutableStateOf<String?>(null) }

    // Exit password dialog state
    var showExitDialog by remember { mutableStateOf(false) }
    var exitPasswordInput by remember { mutableStateOf("") }
    var exitPasswordError by remember { mutableStateOf<String?>(null) }

    // Secret supervisor tap trigger counter
    var supervisorTapCount by remember { mutableIntStateOf(0) }
    var lastTapTime by remember { mutableStateOf(0L) }

    fun triggerSecretSupervisor() {
        val now = System.currentTimeMillis()
        if (now - lastTapTime > 2000) {
            supervisorTapCount = 1
        } else {
            supervisorTapCount++
        }
        lastTapTime = now

        if (supervisorTapCount >= 5) {
            supervisorTapCount = 0
            Toast.makeText(context, "Membuka Panel Pengawas...", Toast.LENGTH_SHORT).show()
            onEnterSupervisor()
        }
    }

    // Intercept hardware and software Back button
    BackHandler(enabled = true) {
        // Back press triggers exit password dialog! Cannot leave without password
        exitPasswordInput = ""
        exitPasswordError = null
        showExitDialog = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Exam Top Security Bar
            Surface(
                color = DarkSurface,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Secure Status Indicator with Secret 5-Tap Pengawas trigger
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    triggerSecretSupervisor()
                                }
                                .testTag("secure_status_indicator")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(SecurityGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "UJIAN TERKUNCI",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (violations.isEmpty()) "Anti-Nyontek Aktif" else "${violations.size} Pelanggaran",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (violations.isEmpty()) SecurityGreen else SecurityAmber
                                )
                            }
                        }

                        // Right actions: Refresh, Hidden Supervisor Button, and Exit Button
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Reload button
                            IconButton(
                                onClick = {
                                    webLoadError = null
                                    webViewRef?.reload()
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Muat Ulang Web",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Hidden Supervisor button: discreetly integrated, 1-click or multi-tap
                            // styled subtly so students don't tamper with it
                            IconButton(
                                onClick = {
                                    triggerSecretSupervisor()
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("hidden_supervisor_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = "Menu Pengawas",
                                    tint = TextMuted.copy(alpha = 0.4f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Exit Exam Button (Requires Password "13456")
                            Button(
                                onClick = {
                                    exitPasswordInput = ""
                                    exitPasswordError = null
                                    showExitDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SecurityRedBg,
                                    contentColor = SecurityRed
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("exit_exam_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Keluar",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    // Progress bar
                    if (isPageLoading) {
                        LinearProgressIndicator(
                            progress = { pageLoadingProgress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp),
                            color = AccentCyanBright,
                            trackColor = DarkSurfaceVariant
                        )
                    }
                }
            }

            // WebView Main Area or Error View
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (webLoadError != null) {
                    // Friendly offline/error message with retry
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = SecurityAmber,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Tidak Dapat Terhubung ke Server CBT",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Pastikan perangkat Anda sudah terhubung ke WiFi Ruang Ujian sekolah dan server di ${config.examUrl} aktif.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = {
                                    try {
                                        context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                                    } catch (_: Exception) {
                                    }
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Buka WiFi", color = TextPrimary)
                            }

                            Button(
                                onClick = {
                                    webLoadError = null
                                    isPageLoading = true
                                    webViewRef?.loadUrl(config.examUrl)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Coba Lagi", color = TextPrimary)
                            }
                        }
                    }
                }

                // Android WebView
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = android.view.ViewGroup.LayoutParams(
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT
                            )

                            // Anti-tamper & security settings
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.databaseEnabled = true
                            settings.allowFileAccess = false
                            settings.allowContentAccess = false
                            settings.useWideViewPort = true
                            settings.loadWithOverviewMode = true
                            settings.cacheMode = WebSettings.LOAD_DEFAULT

                            // Custom CBT User Agent
                            val defaultUa = settings.userAgentString
                            settings.userAgentString = "$defaultUa CBT-SecureExamBrowser/1.0"

                            // Disable text selection & long press to prevent dictionary / copy paste
                            setOnLongClickListener { true }
                            isLongClickable = false
                            isHapticFeedbackEnabled = false

                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    isPageLoading = true
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isPageLoading = false
                                }

                                override fun onReceivedError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    error: WebResourceError?
                                ) {
                                    super.onReceivedError(view, request, error)
                                    if (request?.isForMainFrame == true) {
                                        webLoadError = error?.description?.toString() ?: "Gagal terhubung ke server CBT"
                                        isPageLoading = false
                                    }
                                }
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    pageLoadingProgress = newProgress
                                    if (newProgress >= 100) {
                                        isPageLoading = false
                                    }
                                }
                            }

                            loadUrl(config.examUrl)
                            webViewRef = this
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // ==========================================
        // REAL-TIME VPN DETECTED BLOCKER OVERLAY
        // ==========================================
        AnimatedVisibility(visible = isVpnAlertShowing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xE60A0A0A))
                    .padding(24.dp)
                    .testTag("vpn_blocker_overlay"),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, SecurityRed)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(SecurityRedBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VpnLock,
                                contentDescription = null,
                                tint = SecurityRed,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "KONEKSI VPN TERDETEKSI!",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = SecurityRed,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Penggunaan VPN atau Proxy sangat dilarang selama ujian berlangsung. Layar ujian ditangguhkan secara otomatis sampai VPN dimatikan sepenuhnya.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                try {
                                    context.startActivity(Intent(Settings.ACTION_VPN_SETTINGS))
                                } catch (_: Exception) {
                                    try {
                                        context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "Silakan matikan VPN di bilah notifikasi", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SecurityRed),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Buka Pengaturan VPN", color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ==========================================
        // FOCUS LOST / FLOATING WINDOW ALERT DIALOG
        // ==========================================
        if (isFocusAlertShowing) {
            AlertDialog(
                onDismissRequest = { /* Cannot dismiss by clicking outside */ },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = SecurityAmber,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = {
                    Text(
                        text = "Peringatan Anti-Nyontek!",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                },
                text = {
                    Column {
                        Text(
                            text = focusAlertMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Dilarang meminimalkan aplikasi, membuka jendela mengambang, kalkulator pop-up, split screen, atau notifikasi. Jika pelanggaran berulang, lembar ujian akan dikunci permanen!",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecurityAmber
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onDismissFocusAlert,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                    ) {
                        Text("Saya Mengerti, Lanjutkan", color = TextPrimary)
                    }
                },
                containerColor = DarkSurface,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // ==========================================
        // EXIT EXAM PASSWORD DIALOG (Password "13456")
        // ==========================================
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.LockClock,
                        contentDescription = null,
                        tint = AccentCyanBright,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = "Konfirmasi Keluar Ujian",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Untuk keluar dari mode ujian, masukkan kata sandi resmi dari pengawas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = exitPasswordInput,
                            onValueChange = {
                                exitPasswordInput = it
                                exitPasswordError = null
                            },
                            label = { Text("Kata Sandi Keluar") },
                            placeholder = { Text("Contoh: 13456") },
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
                                .testTag("exit_password_input")
                        )

                        if (exitPasswordError != null) {
                            Text(
                                text = exitPasswordError.orEmpty(),
                                color = SecurityRed,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }

                        Text(
                            text = "Password Default: 13456",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val success = onExitExamWithPassword(exitPasswordInput)
                            if (success) {
                                showExitDialog = false
                            } else {
                                exitPasswordError = "Kata sandi salah! Hubungi pengawas (13456)."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SecurityRed),
                        modifier = Modifier.testTag("confirm_exit_button")
                    ) {
                        Text("Keluar Ujian", color = TextPrimary)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showExitDialog = false }) {
                        Text("Batal", color = TextSecondary)
                    }
                },
                containerColor = DarkSurface,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
