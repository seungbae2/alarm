# 알람 앱 (Alarm App)

신뢰할 수 있는 Android 알람 애플리케이션입니다. 잠금화면에서도 정확하게 작동하며, 배터리 최적화와 시스템 제약을 극복하여 안정적인 알람 기능을 제공합니다.

## ✨ 주요 기능

- **정확한 알람**: Android 12+ 정확한 알람 스케줄링 지원
- **잠금화면 대응**: 잠금화면 위에서 알람 표시 및 해제
- **배터리 최적화 대응**: 배터리 절약 모드에서도 안정적인 작동
- **재부팅 복원**: 기기 재시작 후 자동으로 알람 복원
- **지속성 모니터링**: 백그라운드에서 알람 상태 지속적 감시
- **모던 UI**: Jetpack Compose 기반의 현대적인 사용자 인터페이스

## 🛠 기술 스택

### 핵심 기술

- **Kotlin**: 100% Kotlin으로 개발
- **Jetpack Compose**: 선언적 UI 프레임워크
- **Coroutines**: 비동기 처리 및 동시성 관리

### 아키텍처 & 패턴

- **Google App Architecture**: 도메인-데이터-프레젠테이션 레이어 분리
- **MVVM Pattern**: Model-View-ViewModel 아키텍처
- **Repository Pattern**: 데이터 접근 추상화

### 주요 라이브러리

- **Hilt**: 의존성 주입 프레임워크
- **Room**: 로컬 데이터베이스 (SQLite)
- **Navigation Compose**: 화면 네비게이션
- **Kotlinx DateTime**: 날짜/시간 처리

## 📱 시스템 요구사항

- **최소 SDK**: Android 5.0 (API 21)
- **대상 SDK**: Android 14 (API 34)
- **컴파일 SDK**: Android 14 (API 34)

## 🏗 프로젝트 구조

```
app/src/main/java/com/sb/alarm/
├── MainActivity.kt                    # 메인 액티비티
├── AlarmApplication.kt               # 애플리케이션 클래스
├── AppNavGraph.kt                    # 네비게이션 그래프
├── data/                             # 데이터 레이어
├── domain/                           # 도메인 레이어
│   ├── model/                        # 도메인 모델
│   ├── repository/                   # 리포지토리 인터페이스
│   └── usecase/                      # 비즈니스 로직
├── presentation/                     # 프레젠테이션 레이어
│   ├── alarm/                        # 알람 화면
│   ├── schedule/                     # 스케줄 화면
│   ├── service/                      # 서비스 컴포넌트
│   └── receiver/                     # 브로드캐스트 리시버
├── shared/                           # 공통 컴포넌트
└── di/                              # 의존성 주입 모듈
```

## 🚀 설치 및 실행

### 필수 권한 설정

앱 실행 시 다음 권한들이 자동으로 요청됩니다:

- **알림 권한** (Android 13+)
- **배터리 최적화 예외**
- **다른 앱 위에 표시**
- **정확한 알람 스케줄링**

## 🔧 주요 컴포넌트

### 서비스

- **AlarmService**: 알람 실행 및 관리
- **PersistentAlarmService**: 지속적 알람 모니터링

### 리시버

- **AlarmReceiver**: 알람 이벤트 처리
- **BootReceiver**: 시스템 재시작 감지 및 알람 복원

### 액티비티

- **MainActivity**: 메인 화면 및 권한 관리
- **AlarmActivity**: 전용 알람 화면 (잠금화면 대응)

## 📝 개발 노트

### 배터리 최적화 대응

- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` 권한 사용
- 포그라운드 서비스로 중요한 알람 작업 수행
- 지속적 모니터링 서비스로 안정성 확보

### 잠금화면 호환성

- `USE_FULL_SCREEN_INTENT` 권한 활용
- 전용 AlarmActivity로 잠금화면 위 표시
- `showOnLockScreen`, `turnScreenOn` 플래그 설정

### 시스템 변경 대응

- 재부팅, 시간 변경, 앱 업데이트 감지
- BootReceiver로 알람 자동 복원
- 시간대 변경 시 알람 시간 자동 조정
