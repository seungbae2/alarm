package com.sb.alarm.presentation.alarm

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.sb.alarm.domain.model.Alarm
import com.sb.alarm.shared.RepeatType
import com.sb.alarm.ui.theme.AlarmTheme

@Composable
fun AlarmScreen(
    uiState: AlarmUiState,
    onEvent: (AlarmUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = true) {
        // 뒤로가기 차단 - 알람은 반드시 처리해야 함
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                when (uiState) {
                    is AlarmUiState.Success -> MaterialTheme.colorScheme.primaryContainer
                    is AlarmUiState.Loading, is AlarmUiState.Error -> MaterialTheme.colorScheme.surface
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is AlarmUiState.Loading -> {
                LoadingContent()
            }
            is AlarmUiState.Success -> {
                AlarmContent(
                    alarm = uiState.alarm,
                    currentTime = uiState.currentTime,
                    currentDate = uiState.currentDate,
                    onEvent = onEvent
                )
            }
            is AlarmUiState.Error -> {
                ErrorContent()
            }
        }
    }
}

@Composable
private fun AlarmContent(
    alarm: Alarm,
    currentTime: String,
    currentDate: String,
    onEvent: (AlarmUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 상단 시간 표시
        CurrentTimeCard(
            currentTime = currentTime,
            currentDate = currentDate
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 알람 아이콘 (펄스 애니메이션)
        AlarmIcon()
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 알람 정보 카드
        AlarmInfoCard(alarm = alarm)
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // 액션 버튼들
        AlarmActionButtons(onEvent = onEvent)
    }
}

@Composable
private fun LoadingContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "알람 정보를 불러오는 중...",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun ErrorContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⚠️",
            fontSize = 60.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "알람 정보를 불러올 수 없습니다",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun CurrentTimeCard(
    currentTime: String,
    currentDate: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentTime,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = currentDate,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AlarmIcon(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .size(120.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "⏰",
            fontSize = 60.sp
        )
    }
}


@Composable
private fun AlarmIconStatic(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "⏰",
            fontSize = 60.sp
        )
    }
}

@Composable
private fun AlarmInfoCard(
    alarm: Alarm,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "💊 복용 시간입니다",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = alarm.medicationName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${alarm.hour.toString().padStart(2, '0')}:${alarm.minute.toString().padStart(2, '0')}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AlarmActionButtons(
    onEvent: (AlarmUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 복용 완료 버튼 (가장 중요)
        Button(
            onClick = { onEvent(AlarmUiEvent.TakeCompleted) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "복용 완료",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        // 무시 버튼
        OutlinedButton(
            onClick = { onEvent(AlarmUiEvent.Dismiss) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "무시",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// Preview를 위한 샘플 데이터
@Preview(showBackground = true)
@Composable
private fun AlarmScreenPreview() {
    AlarmTheme {
        val sampleAlarm = Alarm(
            id = 1,
            hour = 9,
            minute = 30,
            medicationName = "비타민 D",
            isActive = true,
            repeatType = RepeatType.DAILY,
            startDate = System.currentTimeMillis(),
            endDate = null
        )
        
        val sampleState = AlarmUiState.Success(
            alarm = sampleAlarm,
            currentTime = "09:30",
            currentDate = "2024년 01월 15일"
        )
        
        AlarmScreen(
            uiState = sampleState,
            onEvent = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AlarmScreenLoadingPreview() {
    AlarmTheme {
        AlarmScreen(
            uiState = AlarmUiState.Loading,
            onEvent = { }
        )
    }
} 