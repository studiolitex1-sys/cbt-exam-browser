package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeviceSecurityInfo
import com.example.model.SchoolWifiPreset
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
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TokenGateScreen(
    deviceInfo: DeviceSecurityInfo,
    selectedRoom: String,
    wifiPresets: List<SchoolWifiPreset>,
    onSelectRoom: (String) -> Unit,
    onSubmitToken: (String) -> Boolean,
    onEnterSupervisor: () -> Unit
) {
    val context = LocalContext.current
    var tokenInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var secretTapCount by remember { mutableIntStateOf(0) }
    var lastTapTime by remember { mutableStateOf(0L) }

    val currentPreset = wifiPresets.find { it.roomNumber == selectedRoom } ?: wifiPresets.first()

    // Secret supervisor trigger logic (5 rapid taps on shield)
    fun handleSecretTap() {
        val now = System.currentTimeMillis()
        if (now - lastTapTime > 2000) {
            secretTapCount = 1
        } else {
            secretTapCount++
        }
        lastTapTime = now

        if (secretTapCount >= 5) {
            secretTapCount = 0
            Toast.makeText(context, "Mode Pengawas Diaktifkan!", Toast.LENGTH_SHORT).show()
            onEnterSupervisor()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // App Brand Header with 5-Tap Secret Pengawas Trigger
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant)
                    .border(2.dp, AccentCyan, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        handleSecretTap()
                    }
                    .testTag("secret_shield_trigger"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Logo Keamanan Ujian",
                    tint = AccentCyanBright,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "CBT EXAM BROWSER",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = TextPrimary
            )

            Text(
                text = "Sistem Ujian Terkunci & Aman",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Token Entrance Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = AccentCyanBright,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Akses Lembar Ujian",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = "Anti-nyontek akan otomatis aktif secara ketat setelah token berhasil dimasukkan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = tokenInput,
                        onValueChange = {
                            tokenInput = it
                            errorMessage = null
                        },
                        label = { Text("Masukkan Token Ujian") },
                        placeholder = { Text("Contoh: 132456") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val success = onSubmitToken(tokenInput)
                                if (!success) {
                                    errorMessage = "Token salah! Masukkan token yang valid dari pengawas (132456)."
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyanBright,
                            unfocusedBorderColor = BorderLight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = AccentCyanBright,
                            unfocusedLabelColor = TextSecondary,
                            cursorColor = AccentCyanBright
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("token_input_field")
                    )

                    AnimatedVisibility(visible = errorMessage != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = SecurityRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = errorMessage.orEmpty(),
                                color = SecurityRed,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val success = onSubmitToken(tokenInput)
                            if (!success) {
                                errorMessage = "Token salah! Masukkan token yang valid dari pengawas (132456)."
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("start_exam_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentCyan,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Mulai Ujian Sekarang",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    // Helper pill showing token reminder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Token Default: 132456",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // School WiFi Preset & Connect Helper Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            tint = AccentCyanBright,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Bantuan WiFi Sekolah",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = "Wajib terhubung ke WiFi Ruang Ujian untuk mengakses server CBT lokal.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    Text(
                        text = "Pilih Ruangan Anda:",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(wifiPresets) { preset ->
                            val isSelected = preset.roomNumber == selectedRoom
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectRoom(preset.roomNumber) },
                                label = { Text("Ruang ${preset.roomNumber}") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentCyan,
                                    selectedLabelColor = TextPrimary,
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Room Wi-Fi Credentials Info Box
                    Surface(
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Nama SSID:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                    Text(
                                        text = currentPreset.ssid,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Kata Sandi WiFi:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                    Text(
                                        text = currentPreset.password,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = AccentCyanBright
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("WiFi Password", currentPreset.password)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Password ${currentPreset.password} disalin!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Salin Sandi", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Buka pengaturan WiFi manual", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant, contentColor = TextPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Set WiFi", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Current Network Connection Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NetworkCheck,
                                contentDescription = null,
                                tint = if (deviceInfo.connectedSsid.contains("Ruang", ignoreCase = true)) SecurityGreen else SecurityAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Terhubung: ${deviceInfo.connectedSsid}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                        Text(
                            text = "IP: ${deviceInfo.ipAddress}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Security Status Checklist Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Status Kesiapan Keamanan",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    SecurityStatusItem(
                        title = "Deteksi VPN",
                        status = if (deviceInfo.isVpnConnected) "VPN Aktif (Wajib Matikan!)" else "Aman (Non-aktif)",
                        isSafe = !deviceInfo.isVpnConnected
                    )

                    SecurityStatusItem(
                        title = "Perangkat Fisik",
                        status = if (deviceInfo.isEmulator) "Emulator Terdeteksi" else "Perangkat Fisik Asli",
                        isSafe = !deviceInfo.isEmulator
                    )

                    SecurityStatusItem(
                        title = "Anti Screenshot & Rekam",
                        status = "Siap Diaktifkan Otomatis",
                        isSafe = true
                    )

                    SecurityStatusItem(
                        title = "Anti Jendela Mengambang",
                        status = "Siap Diaktifkan Otomatis",
                        isSafe = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Discreet Pengawas Trigger Button at bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        handleSecretTap()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Akses Pengawas",
                        tint = TextMuted.copy(alpha = 0.35f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "CBT v1.0.0 • Ujian Sekolah Terpusat",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
fun SecurityStatusItem(title: String, status: String, isSafe: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isSafe) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isSafe) SecurityGreen else SecurityRed,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = if (isSafe) SecurityGreen else SecurityRed
            )
        }
    }
}
