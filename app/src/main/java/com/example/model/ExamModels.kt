package com.example.model

enum class ScreenMode {
    TOKEN_GATE,
    EXAM_ACTIVE,
    SUPERVISOR_PANEL,
    FORCE_LOCKED_VIOLATION
}

data class SecurityViolation(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val title: String,
    val detail: String,
    val isSevere: Boolean = false
)

data class SupervisorConfig(
    val examUrl: String = "http://192.168.10.99/cbt",
    val tokenRequired: String = "132456",
    val exitPassword: String = "00132",
    val screenshotBlockerEnabled: Boolean = true,
    val vpnDetectionEnabled: Boolean = true,
    val emulatorDetectionEnabled: Boolean = true,
    val focusLossDetectionEnabled: Boolean = true,
    val maxViolationsBeforeLock: Int = 3
)

data class DeviceSecurityInfo(
    val isVpnConnected: Boolean = false,
    val isEmulator: Boolean = false,
    val emulatorReason: String = "",
    val isDevOptionsEnabled: Boolean = false,
    val isUsbDebuggingEnabled: Boolean = false,
    val connectedSsid: String = "Tidak Terhubung",
    val ipAddress: String = "0.0.0.0",
    val batteryPercent: Int = 100,
    val isCharging: Boolean = false
)

data class SchoolWifiPreset(
    val roomNumber: String,
    val ssid: String,
    val password: String
)
