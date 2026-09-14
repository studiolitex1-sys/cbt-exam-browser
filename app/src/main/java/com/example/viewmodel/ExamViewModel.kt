package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.DeviceSecurityInfo
import com.example.model.SchoolWifiPreset
import com.example.model.ScreenMode
import com.example.model.SecurityViolation
import com.example.model.SupervisorConfig
import com.example.security.SecurityDetector
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ExamViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("cbt_exam_config", Context.MODE_PRIVATE)

    private val _screenMode = MutableStateFlow(ScreenMode.TOKEN_GATE)
    val screenMode: StateFlow<ScreenMode> = _screenMode.asStateFlow()

    private val _supervisorConfig = MutableStateFlow(loadConfigFromPrefs())
    val supervisorConfig: StateFlow<SupervisorConfig> = _supervisorConfig.asStateFlow()

    private val _violations = MutableStateFlow<List<SecurityViolation>>(emptyList())
    val violations: StateFlow<List<SecurityViolation>> = _violations.asStateFlow()

    private val _deviceInfo = MutableStateFlow(DeviceSecurityInfo())
    val deviceInfo: StateFlow<DeviceSecurityInfo> = _deviceInfo.asStateFlow()

    private val _isVpnAlertShowing = MutableStateFlow(false)
    val isVpnAlertShowing: StateFlow<Boolean> = _isVpnAlertShowing.asStateFlow()

    private val _isFocusAlertShowing = MutableStateFlow(false)
    val isFocusAlertShowing: StateFlow<Boolean> = _isFocusAlertShowing.asStateFlow()

    private val _focusAlertMessage = MutableStateFlow("")
    val focusAlertMessage: StateFlow<String> = _focusAlertMessage.asStateFlow()

    private val _selectedRoom = MutableStateFlow("01")
    val selectedRoom: StateFlow<String> = _selectedRoom.asStateFlow()

    val wifiPresets: List<SchoolWifiPreset> = (1..15).map { num ->
        val roomCode = if (num < 10) "0$num" else "$num"
        SchoolWifiPreset(
            roomNumber = roomCode,
            ssid = "Ruang$roomCode",
            password = "<@Ruang$roomCode>"
        )
    }

    init {
        refreshDeviceInfo()
        startPeriodicSecurityCheck()
    }

    fun refreshDeviceInfo() {
        val info = SecurityDetector.getFullDeviceSecurityInfo(getApplication())
        _deviceInfo.value = info
    }

    private fun startPeriodicSecurityCheck() {
        viewModelScope.launch {
            while (isActive) {
                delay(1500)
                val isExamActive = _screenMode.value == ScreenMode.EXAM_ACTIVE
                val isVpn = SecurityDetector.isVpnActive(getApplication())
                val config = _supervisorConfig.value

                // Update device info
                _deviceInfo.update { it.copy(isVpnConnected = isVpn) }

                // If in exam and VPN detection is enabled
                if (isExamActive && config.vpnDetectionEnabled) {
                    if (isVpn) {
                        if (!_isVpnAlertShowing.value) {
                            _isVpnAlertShowing.value = true
                            recordViolation(
                                title = "VPN Terdeteksi Aktif",
                                detail = "Koneksi VPN diaktifkan saat ujian berlangsung. Layar ujian diblokir.",
                                isSevere = true
                            )
                        }
                    } else {
                        if (_isVpnAlertShowing.value) {
                            _isVpnAlertShowing.value = false
                        }
                    }
                }
            }
        }
    }

    fun selectRoom(roomNumber: String) {
        _selectedRoom.value = roomNumber
    }

    fun getCurrentWifiPreset(): SchoolWifiPreset {
        val current = _selectedRoom.value
        return wifiPresets.find { it.roomNumber == current } ?: wifiPresets.first()
    }

    fun validateToken(token: String): Boolean {
        return token.trim() == _supervisorConfig.value.tokenRequired.trim()
    }

    fun startExam(token: String): Boolean {
        if (validateToken(token)) {
            _screenMode.value = ScreenMode.EXAM_ACTIVE
            _isVpnAlertShowing.value = false
            _isFocusAlertShowing.value = false
            refreshDeviceInfo()

            // Check emulator on start if enabled
            if (_supervisorConfig.value.emulatorDetectionEnabled) {
                val (isEmu, reason) = SecurityDetector.checkEmulator()
                if (isEmu) {
                    recordViolation(
                        title = "Perangkat Emulator Terdeteksi",
                        detail = reason,
                        isSevere = false
                    )
                }
            }

            // Check VPN on start if enabled
            if (_supervisorConfig.value.vpnDetectionEnabled && _deviceInfo.value.isVpnConnected) {
                _isVpnAlertShowing.value = true
                recordViolation(
                    title = "VPN Terdeteksi pada Awal Masuk",
                    detail = "Aplikasi mendeteksi koneksi VPN saat memulai ujian.",
                    isSevere = true
                )
            }
            return true
        }
        return false
    }

    fun validateExitPassword(pw: String): Boolean {
        return pw.trim() == _supervisorConfig.value.exitPassword.trim()
    }

    fun exitExamWithPassword(pw: String): Boolean {
        if (validateExitPassword(pw)) {
            _screenMode.value = ScreenMode.TOKEN_GATE
            _isVpnAlertShowing.value = false
            _isFocusAlertShowing.value = false
            return true
        }
        return false
    }

    fun enterSupervisorMode() {
        // Supervisor mode automatically turns off anti-cheat without password
        _screenMode.value = ScreenMode.SUPERVISOR_PANEL
        _isVpnAlertShowing.value = false
        _isFocusAlertShowing.value = false
        refreshDeviceInfo()
    }

    fun exitSupervisorMode(resumeExam: Boolean) {
        if (resumeExam) {
            _screenMode.value = ScreenMode.EXAM_ACTIVE
            refreshDeviceInfo()
        } else {
            _screenMode.value = ScreenMode.TOKEN_GATE
        }
    }

    fun recordViolation(title: String, detail: String, isSevere: Boolean = false) {
        val newViolation = SecurityViolation(
            title = title,
            detail = detail,
            isSevere = isSevere
        )
        val updated = listOf(newViolation) + _violations.value
        _violations.value = updated

        // Check if student exceeded max violations
        val config = _supervisorConfig.value
        if (config.focusLossDetectionEnabled && updated.size >= config.maxViolationsBeforeLock) {
            // Force lock exam due to repeated violations!
            _screenMode.value = ScreenMode.FORCE_LOCKED_VIOLATION
        }
    }

    fun handleFocusLost() {
        if (_screenMode.value != ScreenMode.EXAM_ACTIVE) return
        if (!_supervisorConfig.value.focusLossDetectionEnabled) return

        val count = _violations.value.size + 1
        val max = _supervisorConfig.value.maxViolationsBeforeLock
        val msg = "Peringatan! Aplikasi kehilangan fokus atau mencoba membuka jendela lain ($count/$max). Dilarang split screen, pop-up, atau beralih aplikasi!"

        _focusAlertMessage.value = msg
        _isFocusAlertShowing.value = true

        recordViolation(
            title = "Aplikasi Kehilangan Fokus / Jendela Mengambang",
            detail = "Siswa terdeteksi meminimalkan aplikasi, membuka pop-up, split-screen, atau notifikasi.",
            isSevere = true
        )
    }

    fun dismissFocusAlert() {
        _isFocusAlertShowing.value = false
    }

    fun clearViolations() {
        _violations.value = emptyList()
    }

    fun updateSupervisorConfig(config: SupervisorConfig) {
        _supervisorConfig.value = config
        saveConfigToPrefs(config)
    }

    fun resetTokenToDefault(): String {
        val defToken = "132456"
        val updated = _supervisorConfig.value.copy(tokenRequired = defToken)
        _supervisorConfig.value = updated
        saveConfigToPrefs(updated)
        return defToken
    }

    fun resetPasswordToDefault(): String {
        val defPw = "00132"
        val updated = _supervisorConfig.value.copy(exitPassword = defPw)
        _supervisorConfig.value = updated
        saveConfigToPrefs(updated)
        return defPw
    }

    fun resetAllCredentialsToDefault() {
        val updated = _supervisorConfig.value.copy(
            tokenRequired = "132456",
            exitPassword = "00132"
        )
        _supervisorConfig.value = updated
        saveConfigToPrefs(updated)
    }

    fun generateRandomToken(): String {
        val randomToken = (100000..999999).random().toString()
        val updated = _supervisorConfig.value.copy(tokenRequired = randomToken)
        _supervisorConfig.value = updated
        saveConfigToPrefs(updated)
        return randomToken
    }

    fun validateSupervisorPassword(pw: String): Boolean {
        return pw.trim() == _supervisorConfig.value.exitPassword.trim()
    }

    fun unlockAndResetFromSupervisor() {
        clearViolations()
        _screenMode.value = ScreenMode.TOKEN_GATE
    }

    private fun loadConfigFromPrefs(): SupervisorConfig {
        return SupervisorConfig(
            examUrl = prefs.getString("exam_url", "http://192.168.10.99/cbt") ?: "http://192.168.10.99/cbt",
            tokenRequired = prefs.getString("token_required", "132456") ?: "132456",
            exitPassword = prefs.getString("exit_password", "00132") ?: "00132",
            screenshotBlockerEnabled = prefs.getBoolean("screenshot_blocker", true),
            vpnDetectionEnabled = prefs.getBoolean("vpn_detection", true),
            emulatorDetectionEnabled = prefs.getBoolean("emu_detection", true),
            focusLossDetectionEnabled = prefs.getBoolean("focus_detection", true),
            maxViolationsBeforeLock = prefs.getInt("max_violations", 3)
        )
    }

    private fun saveConfigToPrefs(config: SupervisorConfig) {
        prefs.edit()
            .putString("exam_url", config.examUrl)
            .putString("token_required", config.tokenRequired)
            .putString("exit_password", config.exitPassword)
            .putBoolean("screenshot_blocker", config.screenshotBlockerEnabled)
            .putBoolean("vpn_detection", config.vpnDetectionEnabled)
            .putBoolean("emu_detection", config.emulatorDetectionEnabled)
            .putBoolean("focus_detection", config.focusLossDetectionEnabled)
            .putInt("max_violations", config.maxViolationsBeforeLock)
            .apply()
    }
}
