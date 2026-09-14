package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeviceSecurityInfo
import com.example.model.SchoolWifiPreset
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
import com.example.ui.theme.SecurityGreenBg
import com.example.ui.theme.SecurityRed
import com.example.ui.theme.SecurityRedBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SupervisorPanelScreen(
    config: SupervisorConfig,
    deviceInfo: DeviceSecurityInfo,
    violations: List<SecurityViolation>,
    wifiPresets: List<SchoolWifiPreset>,
    selectedRoom: String,
    onSelectRoom: (String) -> Unit,
    onUpdateConfig: (SupervisorConfig) -> Unit,
    onClearViolations: () -> Unit,
    onRefreshDeviceStatus: () -> Unit,
    onResumeExam: () -> Unit,
    onResetToTokenScreen: () -> Unit,
    onResetTokenToDefault: () -> String = { "132456" },
    onResetPasswordToDefault: () -> String = { "00132" },
    onGenerateRandomToken: () -> String = { "132456" },
    onResetAllCredentials: () -> Unit = {}
) {
    val context = LocalContext.current
    var currentTab by remember { mutableIntStateOf(0) }

    // Editable configurations
    var urlInput by remember { mutableStateOf(config.examUrl) }
    var tokenInput by remember { mutableStateOf(config.tokenRequired) }
    var exitPwInput by remember { mutableStateOf(config.exitPassword) }
    var vpnCheckEnabled by remember { mutableStateOf(config.vpnDetectionEnabled) }
    var screenshotBlockEnabled by remember { mutableStateOf(config.screenshotBlockerEnabled) }
    var emuCheckEnabled by remember { mutableStateOf(config.emulatorDetectionEnabled) }
    var focusCheckEnabled by remember { mutableStateOf(config.focusLossDetectionEnabled) }

    val currentPreset = wifiPresets.find { it.roomNumber == selectedRoom } ?: wifiPresets.first()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
    ) {
        // Top Proctor Header
        Surface(
            color = DarkSurface,
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AccentCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = TextPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "PANEL PENGAWAS CBT",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Mode Pengawas: Anti-Nyontek Dinonaktifkan",
                                style = MaterialTheme.typography.labelSmall,
                                color = SecurityGreen
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = onRefreshDeviceStatus) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Perbarui Status",
                                tint = AccentCyanBright
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Proctor Banner Notice
                Surface(
                    color = SecurityGreenBg.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SecurityGreen.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = SecurityGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Anti-nyontek OFF otomatis tanpa memasukkan password saat berada di panel pengawas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = currentTab,
            containerColor = DarkSurfaceVariant,
            contentColor = AccentCyanBright,
            edgePadding = 12.dp
        ) {
            Tab(
                selected = currentTab == 0,
                onClick = { currentTab = 0 },
                text = { Text("Diagnostik") },
                icon = { Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = currentTab == 1,
                onClick = { currentTab = 1 },
                text = { Text("Log Pelanggaran (${violations.size})") },
                icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = currentTab == 2,
                onClick = { currentTab = 2 },
                text = { Text("Aturan Ujian") },
                icon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = currentTab == 3,
                onClick = { currentTab = 3 },
                text = { Text("WiFi Sekolah") },
                icon = { Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        // Tab Contents
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            when (currentTab) {
                0 -> DiagnosticsTab(deviceInfo = deviceInfo)
                1 -> ViolationsTab(
                    violations = violations,
                    onClearViolations = onClearViolations
                )
                2 -> ExamRulesConfigTab(
                    url = urlInput,
                    token = tokenInput,
                    exitPw = exitPwInput,
                    vpnCheck = vpnCheckEnabled,
                    screenBlock = screenshotBlockEnabled,
                    emuCheck = emuCheckEnabled,
                    focusCheck = focusCheckEnabled,
                    onUrlChange = { urlInput = it },
                    onTokenChange = { tokenInput = it },
                    onExitPwChange = { exitPwInput = it },
                    onVpnCheckChange = { vpnCheckEnabled = it },
                    onScreenBlockChange = { screenshotBlockEnabled = it },
                    onEmuCheckChange = { emuCheckEnabled = it },
                    onFocusCheckChange = { focusCheckEnabled = it },
                    onResetTokenClick = {
                        val def = onResetTokenToDefault()
                        tokenInput = def
                        Toast.makeText(context, "Token direset ke default ($def)", Toast.LENGTH_SHORT).show()
                    },
                    onRandomTokenClick = {
                        val rnd = onGenerateRandomToken()
                        tokenInput = rnd
                        Toast.makeText(context, "Token baru dibuat: $rnd", Toast.LENGTH_SHORT).show()
                    },
                    onResetPasswordClick = {
                        val defPw = onResetPasswordToDefault()
                        exitPwInput = defPw
                        Toast.makeText(context, "Password Pengawas direset ke $defPw", Toast.LENGTH_SHORT).show()
                    },
                    onResetAllClick = {
                        onResetAllCredentials()
                        tokenInput = "132456"
                        exitPwInput = "00132"
                        Toast.makeText(context, "Kredensial direset ke default: Token 132456 | PW 00132", Toast.LENGTH_SHORT).show()
                    },
                    onSave = {
                        val newConfig = config.copy(
                            examUrl = urlInput,
                            tokenRequired = tokenInput,
                            exitPassword = exitPwInput,
                            vpnDetectionEnabled = vpnCheckEnabled,
                            screenshotBlockerEnabled = screenshotBlockEnabled,
                            emulatorDetectionEnabled = emuCheckEnabled,
                            focusLossDetectionEnabled = focusCheckEnabled
                        )
                        onUpdateConfig(newConfig)
                        Toast.makeText(context, "Konfigurasi pengawas tersimpan!", Toast.LENGTH_SHORT).show()
                    }
                )
                3 -> SchoolWifiTab(
                    presets = wifiPresets,
                    selectedRoom = selectedRoom,
                    currentPreset = currentPreset,
                    onSelectRoom = onSelectRoom
                )
            }
        }

        // Bottom Action Bar: Resume Exam or Exit to Token Gate
        Surface(
            color = DarkSurface,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onResetToTokenScreen,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SecurityRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Reset Siswa", fontSize = 13.sp)
                }

                Button(
                    onClick = onResumeExam,
                    modifier = Modifier.weight(1.5f),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Lanjutkan Ujian", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DiagnosticsTab(deviceInfo: DeviceSecurityInfo) {
    val context = LocalContext.current
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Status Jaringan & VPN",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = AccentCyanBright
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    DiagnosticRow("WiFi SSID", deviceInfo.connectedSsid)
                    DiagnosticRow("Alamat IP Lokal", deviceInfo.ipAddress)
                    DiagnosticRow(
                        "Koneksi VPN / Proxy",
                        if (deviceInfo.isVpnConnected) "VPN AKTIF (Dilarang)" else "Non-Aktif (Aman)",
                        isWarning = deviceInfo.isVpnConnected
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Integritas Perangkat Siswa",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = AccentCyanBright
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    DiagnosticRow(
                        "Tipe Lingkungan",
                        if (deviceInfo.isEmulator) "EMULATOR (${deviceInfo.emulatorReason})" else "Perangkat Fisik Asli",
                        isWarning = deviceInfo.isEmulator
                    )
                    DiagnosticRow(
                        "Opsi Pengembang",
                        if (deviceInfo.isDevOptionsEnabled) "Aktif" else "Non-aktif",
                        isWarning = deviceInfo.isDevOptionsEnabled
                    )
                    DiagnosticRow(
                        "USB Debugging (ADB)",
                        if (deviceInfo.isUsbDebuggingEnabled) "Aktif" else "Non-aktif",
                        isWarning = deviceInfo.isUsbDebuggingEnabled
                    )
                    DiagnosticRow(
                        "Baterai Siswa",
                        "${deviceInfo.batteryPercent}% ${if (deviceInfo.isCharging) "(Mengisi daya)" else ""}"
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    try {
                        context.startActivity(Intent(Settings.ACTION_SETTINGS))
                    } catch (_: Exception) {
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Buka Pengaturan Sistem Android", color = TextPrimary, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun DiagnosticRow(label: String, value: String, isWarning: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = if (isWarning) SecurityRed else TextPrimary
        )
    }
}

@Composable
fun ViolationsTab(
    violations: List<SecurityViolation>,
    onClearViolations: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total Pelanggaran: ${violations.size}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = if (violations.isEmpty()) SecurityGreen else SecurityRed
            )
            if (violations.isNotEmpty()) {
                OutlinedButton(
                    onClick = onClearViolations,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SecurityAmber)
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset Log", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (violations.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = SecurityGreen,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tidak Ada Pelanggaran",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "Siswa belum melakukan tindakan mencurigakan atau kecurangan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        } else {
            val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(violations) { violation ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (violation.isSevere) SecurityRedBg.copy(alpha = 0.3f) else DarkSurface
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (violation.isSevere) SecurityRed.copy(alpha = 0.6f) else BorderLight
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (violation.isSevere) SecurityRed else SecurityAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = violation.title,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                }
                                Text(
                                    text = dateFormat.format(Date(violation.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = violation.detail,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExamRulesConfigTab(
    url: String,
    token: String,
    exitPw: String,
    vpnCheck: Boolean,
    screenBlock: Boolean,
    emuCheck: Boolean,
    focusCheck: Boolean,
    onUrlChange: (String) -> Unit,
    onTokenChange: (String) -> Unit,
    onExitPwChange: (String) -> Unit,
    onVpnCheckChange: (Boolean) -> Unit,
    onScreenBlockChange: (Boolean) -> Unit,
    onEmuCheckChange: (Boolean) -> Unit,
    onFocusCheckChange: (Boolean) -> Unit,
    onResetTokenClick: () -> Unit = {},
    onRandomTokenClick: () -> Unit = {},
    onResetPasswordClick: () -> Unit = {},
    onResetAllClick: () -> Unit = {},
    onSave: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Konfigurasi Server & Kredensial Ujian",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = AccentCyanBright
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = url,
                        onValueChange = onUrlChange,
                        label = { Text("URL Server CBT") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyanBright,
                            unfocusedBorderColor = BorderLight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Token Management Section
                    Text(
                        text = "Token Masuk Ujian Siswa",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = token,
                        onValueChange = onTokenChange,
                        label = { Text("Token Masuk") },
                        placeholder = { Text("Contoh: 132456") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyanBright,
                            unfocusedBorderColor = BorderLight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onResetTokenClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyanBright)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Token (132456)", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = onRandomTokenClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Acak Token Baru", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Pengawas Management Section
                    Text(
                        text = "Password Pengawas / Keluar Ujian",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = exitPw,
                        onValueChange = onExitPwChange,
                        label = { Text("Password Pengawas") },
                        placeholder = { Text("Contoh: 00132") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyanBright,
                            unfocusedBorderColor = BorderLight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = onResetPasswordClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SecurityAmber)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset Password Pengawas ke Default (00132)", fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onResetAllClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SecurityGreen)
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset Semua: Token 132456 & Password 00132", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Saklar Fitur Anti-Nyontek",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = AccentCyanBright
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    ProctorSwitchRow(
                        title = "Deteksi VPN Real-Time",
                        desc = "Blokir layar saat siswa mengaktifkan VPN/Proxy",
                        checked = vpnCheck,
                        onCheckedChange = onVpnCheckChange
                    )

                    ProctorSwitchRow(
                        title = "Blokir Screenshot (FLAG_SECURE)",
                        desc = "Cegah tangkapan layar, perekaman & recent preview",
                        checked = screenBlock,
                        onCheckedChange = onScreenBlockChange
                    )

                    ProctorSwitchRow(
                        title = "Deteksi Emulator Android",
                        desc = "Catat pelanggaran jika berjalan di emulator PC",
                        checked = emuCheck,
                        onCheckedChange = onEmuCheckChange
                    )

                    ProctorSwitchRow(
                        title = "Anti Jendela Mengambang & Fokus",
                        desc = "Kunci ujian saat siswa meminimalkan aplikasi",
                        checked = focusCheck,
                        onCheckedChange = onFocusCheckChange
                    )
                }
            }
        }

        item {
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Simpan Perubahan Aturan", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProctorSwitchRow(
    title: String,
    desc: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
            Text(text = desc, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextPrimary,
                checkedTrackColor = AccentCyan,
                uncheckedTrackColor = DarkSurfaceVariant
            )
        )
    }
}

@Composable
fun SchoolWifiTab(
    presets: List<SchoolWifiPreset>,
    selectedRoom: String,
    currentPreset: SchoolWifiPreset,
    onSelectRoom: (String) -> Unit
) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Daftar Akses WiFi Ruangan Ujian",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
        Text(
            text = "Format nama: Ruang0<X> | Format sandi: <@Ruang0<X>>",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Ruangan Aktif: Ruang ${currentPreset.roomNumber}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = AccentCyanBright
                )
                Spacer(modifier = Modifier.height(8.dp))
                DiagnosticRow("SSID", currentPreset.ssid)
                DiagnosticRow("Password", currentPreset.password)

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("WiFi Password", currentPreset.password)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Password disalin!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Salin Sandi", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            try {
                                context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                            } catch (_: Exception) {
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                    ) {
                        Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WiFi Settings", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Ganti Ruangan Ujian:",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(presets) { preset ->
                val isSelected = preset.roomNumber == selectedRoom
                Card(
                    onClick = { onSelectRoom(preset.roomNumber) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) DarkSurfaceVariant else DarkSurface
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) AccentCyanBright else BorderLight
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Ruang ${preset.roomNumber}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "SSID: ${preset.ssid}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                        Text(
                            text = preset.password,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = AccentCyanBright
                        )
                    }
                }
            }
        }
    }
}
