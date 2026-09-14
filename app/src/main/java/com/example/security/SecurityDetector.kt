package com.example.security

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import com.example.model.DeviceSecurityInfo
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Collections
import java.util.Locale

object SecurityDetector {

    /**
     * Real-time check for active VPN connections.
     * Uses both NetworkCapabilities and active network interface scanning.
     */
    fun isVpnActive(context: Context): Boolean {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            if (cm != null) {
                val activeNetwork = cm.activeNetwork
                if (activeNetwork != null) {
                    val caps = cm.getNetworkCapabilities(activeNetwork)
                    if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                        return true
                    }
                }
            }

            // Fallback: Check network interfaces for tun, ppp, wg, tap
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                if (intf.isUp) {
                    val name = intf.name.lowercase(Locale.ROOT)
                    if (name.startsWith("tun") || name.startsWith("ppp") ||
                        name.startsWith("tap") || name.startsWith("wg") ||
                        name.startsWith("p2p")
                    ) {
                        return true
                    }
                }
            }
        } catch (_: Exception) {
            // Ignore error and proceed safely
        }
        return false
    }

    /**
     * Real-time check if app is running inside an Android Emulator.
     */
    fun checkEmulator(): Pair<Boolean, String> {
        val fingerprint = Build.FINGERPRINT.lowercase(Locale.ROOT)
        val model = Build.MODEL.lowercase(Locale.ROOT)
        val manufacturer = Build.MANUFACTURER.lowercase(Locale.ROOT)
        val brand = Build.BRAND.lowercase(Locale.ROOT)
        val device = Build.DEVICE.lowercase(Locale.ROOT)
        val product = Build.PRODUCT.lowercase(Locale.ROOT)
        val hardware = Build.HARDWARE.lowercase(Locale.ROOT)
        val board = Build.BOARD.lowercase(Locale.ROOT)

        if (fingerprint.startsWith("generic") || fingerprint.startsWith("unknown") ||
            fingerprint.contains("vbox") || fingerprint.contains("test-keys")
        ) {
            return Pair(true, "Fingerprint mengindikasikan emulator: $fingerprint")
        }

        if (model.contains("google_sdk") || model.contains("emulator") ||
            model.contains("android sdk built for x86")
        ) {
            return Pair(true, "Model perangkat emulator: $model")
        }

        if (hardware.contains("goldfish") || hardware.contains("ranchu") ||
            hardware.contains("vbox86") || hardware.contains("nox")
        ) {
            return Pair(true, "Hardware emulator terdeteksi: $hardware")
        }

        if (manufacturer.contains("genymotion") || manufacturer.contains("nox") ||
            manufacturer.contains("netease") || manufacturer.contains("bluestacks")
        ) {
            return Pair(true, "Manufaktur emulator terdeteksi: $manufacturer")
        }

        if (brand.startsWith("generic") && device.startsWith("generic")) {
            return Pair(true, "Generic Brand & Device emulator terdeteksi")
        }

        if (product.contains("sdk_google") || product.contains("google_sdk") ||
            product.contains("vbox86p") || product.contains("nox")
        ) {
            return Pair(true, "Product image emulator: $product")
        }

        return Pair(false, "Perangkat Fisik Asli")
    }

    fun isDeveloperOptionsEnabled(context: Context): Boolean {
        return try {
            Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                0
            ) != 0
        } catch (_: Exception) {
            false
        }
    }

    fun isUsbDebuggingEnabled(context: Context): Boolean {
        return try {
            Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.ADB_ENABLED,
                0
            ) != 0
        } catch (_: Exception) {
            false
        }
    }

    fun canDrawOverlays(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            false
        }
    }

    fun getLocalIpAddress(): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                if (!intf.isUp || intf.isLoopback) continue
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: "0.0.0.0"
                    }
                }
            }
        } catch (_: Exception) {
            // Ignore
        }
        return "0.0.0.0"
    }

    @Suppress("DEPRECATION")
    fun getConnectedWifiSsid(context: Context): String {
        try {
            val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val info = wm?.connectionInfo
            if (info != null) {
                val ssid = info.ssid
                if (ssid != null && ssid != "<unknown ssid>" && ssid != "") {
                    return ssid.replace("\"", "")
                }
            }
        } catch (_: Exception) {
            // Ignore
        }
        return "WiFi Tidak Diketahui"
    }

    fun getBatteryStatus(context: Context): Pair<Int, Boolean> {
        return try {
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = context.registerReceiver(null, intentFilter)
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
            val pct = if (level >= 0 && scale > 0) (level * 100) / scale else 100
            Pair(pct, isCharging)
        } catch (_: Exception) {
            Pair(100, false)
        }
    }

    fun getFullDeviceSecurityInfo(context: Context): DeviceSecurityInfo {
        val vpn = isVpnActive(context)
        val (isEmu, emuReason) = checkEmulator()
        val devOptions = isDeveloperOptionsEnabled(context)
        val adb = isUsbDebuggingEnabled(context)
        val ip = getLocalIpAddress()
        val ssid = getConnectedWifiSsid(context)
        val (battery, charging) = getBatteryStatus(context)

        return DeviceSecurityInfo(
            isVpnConnected = vpn,
            isEmulator = isEmu,
            emulatorReason = emuReason,
            isDevOptionsEnabled = devOptions,
            isUsbDebuggingEnabled = adb,
            connectedSsid = ssid,
            ipAddress = ip,
            batteryPercent = battery,
            isCharging = charging
        )
    }
}
