package com.sb.alarm.presentation.alarm

import android.content.Intent
import android.os.Build
import android.os.Bundle
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

    // Hilt를 통한 ViewModel 의존성 주입
    private val viewModel: AlarmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 잠금화면 위에 표시하고 화면 켜기
        setupWindowFlags()

        val alarmId = intent.getIntExtra("ALARM_ID", -1)
        if (alarmId == -1) {
            Log.e("AlarmActivity", "Invalid alarm ID")
            finish()
            return
        }

        Log.d("AlarmActivity", "AlarmActivity started for alarm ID: $alarmId")

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
                                        "Alarm completed, stopping alarm and navigating to schedule"
                                    )
                                    stopAlarmSound()
                                    navigateToScheduleScreen()
                                }

                                AlarmEffect.NavigateToScheduleAfterDismiss -> {
                                    Log.d(
                                        "AlarmActivity",
                                        "Alarm dismissed, stopping alarm and navigating to schedule"
                                    )
                                    stopAlarmSound()
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

        // 추가 플래그들
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        // Activity가 종료될 때 알람 소리도 함께 종료
        stopAlarmSound()
        Log.d("AlarmActivity", "AlarmActivity destroyed, alarm sound stopped")
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // 뒤로가기 버튼 무시 (알람은 반드시 처리되어야 함)
        Log.d("AlarmActivity", "Back button pressed - ignored for alarm handling")
        // super.onBackPressed() 호출하지 않음으로써 뒤로가기 방지
    }
} 