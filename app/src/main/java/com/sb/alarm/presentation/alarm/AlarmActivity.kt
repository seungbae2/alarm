package com.sb.alarm.presentation.alarm

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.sb.alarm.MainActivity
import com.sb.alarm.shared.theme.AlarmTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlarmActivity : ComponentActivity() {

    private val viewModel: AlarmViewModel by viewModels()

    // 화면 모니터링을 위한 변수들
    private var isAlarmActive = true
    private var screenMonitoringHandler: Handler? = null
    private var currentAlarmId: Int = -1
    private var isUserActionCompleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("AlarmActivity", "onCreate() called")

        // 잠금화면 위에 표시하고 화면 켜기
        setupWindowFlags()

        val alarmId = intent.getIntExtra("ALARM_ID", -1)
        if (alarmId == -1) {
            Log.e("AlarmActivity", "Invalid alarm ID")
            finish()
            return
        }

        currentAlarmId = alarmId
        Log.d("AlarmActivity", "AlarmActivity started for alarm ID: $alarmId")

        // 화면 모니터링 시작
        startScreenMonitoring()

        setContent {
            AlarmTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val uiState by viewModel.uiState.collectAsState()

                    // 알람 정보 로드
                    LaunchedEffect(alarmId) {
                        viewModel.loadAlarm(alarmId)
                    }

                    // 알람 결과 처리
                    LaunchedEffect(Unit) {
                        viewModel.alarmEffect.collect {
                            when (it) {
                                AlarmEffect.NavigateToSchedule -> {
                                    Log.d(
                                        "AlarmActivity",
                                        "AlarmEffect.NavigateToSchedule received - USER ACTION: stopping alarm and navigating to schedule"
                                    )
                                    isUserActionCompleted = true // 사용자 액션 완료 표시
                                    isAlarmActive = false // 알람 비활성화
                                    stopScreenMonitoring("AlarmEffect.NavigateToSchedule") // 화면 모니터링 중지
                                    stopAlarmSound() // 사용자 액션이므로 알람 중지
                                    navigateToScheduleScreen()
                                }

                                AlarmEffect.NavigateToScheduleAfterDismiss -> {
                                    Log.d(
                                        "AlarmActivity",
                                        "AlarmEffect.NavigateToScheduleAfterDismiss received - USER ACTION: stopping alarm and navigating to schedule"
                                    )
                                    isUserActionCompleted = true // 사용자 액션 완료 표시
                                    isAlarmActive = false // 알람 비활성화
                                    stopScreenMonitoring("AlarmEffect.NavigateToScheduleAfterDismiss") // 화면 모니터링 중지
                                    stopAlarmSound() // 사용자 액션이므로 알람 중지
                                    navigateToScheduleScreen()
                                }
                            }
                        }
                    }

                    AlarmScreen(
                        uiState = uiState,
                        onEvent = { event ->
                            viewModel.onEvent(event, alarmId)
                        }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("AlarmActivity", "onStart() called")
    }

    override fun onResume() {
        super.onResume()
        Log.d("AlarmActivity", "onResume() called")
    }

    override fun onPause() {
        super.onPause()
        Log.d("AlarmActivity", "onPause() called - Activity going to background")
    }

    override fun onStop() {
        super.onStop()
        Log.d("AlarmActivity", "onStop() called - Activity no longer visible")

        // 사용자가 명시적으로 알람을 처리하지 않았다면 새로운 Activity 실행
        if (!isUserActionCompleted && isAlarmActive) {
            Log.d("AlarmActivity", "Activity stopped but alarm still active - restarting activity")
            restartAlarmActivityDelayed()
        }
    }

    override fun onRestart() {
        super.onRestart()
        Log.d("AlarmActivity", "onRestart() called")
    }

    private fun stopAlarmSound() {
        try {
            viewModel.stopAlarmFromActivity(this)
            Log.d("AlarmActivity", "Stop alarm signal sent successfully")
        } catch (e: Exception) {
            Log.e("AlarmActivity", "Failed to stop alarm sound", e)
        }
    }

    private fun navigateToScheduleScreen() {
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                // 기존 MainActivity 스택을 모두 지우고 새로 시작
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                // ScheduleScreen으로 이동하라는 정보 추가 (필요시)
                putExtra("navigate_to", "schedule")
            }
            startActivity(intent)
            finish() // 현재 AlarmActivity 종료
            Log.d("AlarmActivity", "Successfully navigated to ScheduleScreen")
        } catch (e: Exception) {
            Log.e("AlarmActivity", "Failed to navigate to ScheduleScreen", e)
            // 실패 시 그냥 종료
            finish()
        }
    }

    private fun setupWindowFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            // Android 8.1 이상
            setTurnScreenOn(true)
            setShowWhenLocked(true)
            Log.d("AlarmActivity", "Set turn screen on and show when locked (API 27+)")
        } else {
            // Android 8.0 이하
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
            Log.d("AlarmActivity", "Set window flags for older Android versions")
        }

        // 추가 플래그들 - 강제성을 위한 강화된 플래그들
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        )
    }

    private fun startScreenMonitoring() {
        screenMonitoringHandler = Handler(Looper.getMainLooper())
        val screenCheckRunnable = object : Runnable {
            override fun run() {
                if (isAlarmActive && !isUserActionCompleted) {
                    // 화면이 꺼져있으면 다시 켜기
                    if (!isScreenOn()) {
                        Log.d("AlarmActivity", "Screen is off, restarting alarm activity")
                        restartAlarmActivity()
                    }
                    screenMonitoringHandler?.postDelayed(this, 2000) // 2초마다 체크
                }
            }
        }
        screenMonitoringHandler?.post(screenCheckRunnable)
        Log.d("AlarmActivity", "Started screen monitoring")
    }

    private fun stopScreenMonitoring(reason: String = "Unknown") {
        Log.d("AlarmActivity", "stopScreenMonitoring() called - Reason: $reason")

        screenMonitoringHandler?.removeCallbacksAndMessages(null)
        screenMonitoringHandler = null
        Log.d("AlarmActivity", "Stopped screen monitoring")
    }

    private fun isScreenOn(): Boolean {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        return powerManager.isInteractive
    }

    private fun restartAlarmActivity() {
        try {
            val intent = Intent(this, AlarmActivity::class.java).apply {
                putExtra("ALARM_ID", currentAlarmId)
                putExtra("MEDICATION_NAME", intent.getStringExtra("MEDICATION_NAME"))
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_NO_USER_ACTION
            }
            startActivity(intent)
            Log.d("AlarmActivity", "Restarted alarm activity due to screen off")
        } catch (e: Exception) {
            Log.e("AlarmActivity", "Failed to restart alarm activity", e)
        }
    }

    private fun restartAlarmActivityDelayed() {
        // 약간의 지연 후 새로운 Activity 실행 (시스템이 안정화될 시간 제공)
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isUserActionCompleted && isAlarmActive) {
                try {
                    val intent = Intent(this, AlarmActivity::class.java).apply {
                        putExtra("ALARM_ID", currentAlarmId)
                        putExtra("MEDICATION_NAME", intent.getStringExtra("MEDICATION_NAME"))
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                                Intent.FLAG_ACTIVITY_NO_USER_ACTION
                    }
                    startActivity(intent)
                    Log.d("AlarmActivity", "Delayed restart of alarm activity executed")
                } catch (e: Exception) {
                    Log.e("AlarmActivity", "Failed to restart alarm activity with delay", e)
                }
            }
        }, 1000) // 1초 후 실행
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("AlarmActivity", "onDestroy() called")

        // 화면 모니터링만 중지 (알람 서비스는 사용자 액션이 있을 때만 중지)
        stopScreenMonitoring("onDestroy")

        // 사용자 액션이 완료된 경우에만 알람 중지
        if (isUserActionCompleted) {
            stopAlarmSound()
            Log.d("AlarmActivity", "User action completed - alarm sound stopped")
        } else {
            Log.d("AlarmActivity", "Activity destroyed but no user action - alarm continues")
        }

        Log.d("AlarmActivity", "AlarmActivity destroyed")
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // 뒤로가기 버튼 무시 (알람은 반드시 처리되어야 함)
        if (isAlarmActive && !isUserActionCompleted) {
            Log.d("AlarmActivity", "Back button pressed - ignored for alarm handling")
            // super.onBackPressed() 호출하지 않음으로써 뒤로가기 방지
        } else {
            super.onBackPressed()
        }
    }
} 