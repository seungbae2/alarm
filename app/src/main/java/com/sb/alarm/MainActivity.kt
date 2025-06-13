package com.sb.alarm

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.navigation.compose.rememberNavController
import com.sb.alarm.shared.theme.AlarmTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // 권한 요청 런처
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted")
        } else {
            Log.w("MainActivity", "Notification permission denied")
        }
    }

    // 배터리 최적화 예외 요청 런처
    private val batteryOptimizationLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        checkBatteryOptimization()
    }

    // 오버레이 권한 요청 런처
    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                Log.d("MainActivity", "Overlay permission granted")
            } else {
                Log.w("MainActivity", "Overlay permission denied")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 필요한 권한들 요청
        requestNecessaryPermissions()

        setContent {
            AlarmTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    AppNavGraph(
                        navController = navController
                    )
                }
            }
        }
    }

    private fun requestNecessaryPermissions() {
        // 1. 알림 권한 요청 (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // 2. 배터리 최적화 예외 요청
        requestBatteryOptimizationExemption()

        // 3. 오버레이 권한 요청
        requestOverlayPermission()

        // 4. 정확한 알람 권한 확인 (Android 12+)
        checkExactAlarmPermission()
    }

    @SuppressLint("BatteryLife")
    private fun requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                try {
                    val intent =
                        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = "package:$packageName".toUri()
                        }
                    batteryOptimizationLauncher.launch(intent)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Failed to request battery optimization exemption", e)
                    // Fallback: 배터리 최적화 설정 화면으로 이동
                    try {
                        val fallbackIntent =
                            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        batteryOptimizationLauncher.launch(fallbackIntent)
                    } catch (e2: Exception) {
                        Log.e("MainActivity", "Failed to open battery optimization settings", e2)
                    }
                }
            } else {
                Log.d("MainActivity", "Battery optimization already disabled")
            }
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                        data = "package:$packageName".toUri()
                    }
                    overlayPermissionLauncher.launch(intent)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Failed to request overlay permission", e)
                }
            } else {
                Log.d("MainActivity", "Overlay permission already granted")
            }
        }
    }

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as android.app.AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = "package:$packageName".toUri()
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Failed to request exact alarm permission", e)
                }
            } else {
                Log.d("MainActivity", "Exact alarm permission already granted")
            }
        }
    }

    private fun checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            if (powerManager.isIgnoringBatteryOptimizations(packageName)) {
                Log.d("MainActivity", "Battery optimization exemption granted")
            } else {
                Log.w("MainActivity", "Battery optimization exemption not granted")
            }
        }
    }
}