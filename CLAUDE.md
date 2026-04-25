# Noti-Keep

## 아키텍처
- Clean Architecture (data / domain / presentation 레이어 분리)
- MVVM 패턴 (ViewModel + StateFlow)
- Repository 패턴
- 네트워크 호출 없음 (로컬 전용 앱)

## 기술 스택
- **언어**: Kotlin
- **UI**: Jetpack Compose
- **DI**: Hilt
- **DB**: Room
- **비동기**: Coroutines, Flow

## 패키지 구조
```
com.android.notikeep
├── data
│   ├── local
│   │   ├── dao/
│   │   ├── entity/
│   │   └── database/
│   └── repository/
├── domain
│   ├── model/
│   ├── repository/
│   └── usecase/
└── presentation
    ├── theme/
    └── {피처}/          ← Screen, ViewModel, UiState 피처별로 묶음
        └── note/
```

## 코딩 규칙
- 비동기 처리는 무조건 Flow 사용 (suspend function 지양)
- UI 상태는 피처별로 UiState data class로 묶어서 StateFlow로 관리
    - 예: `data class NoteListUiState(val notes: List<Note> = emptyList(), val isLoading: Boolean = false)`
    - ViewModel: `val uiState: StateFlow<{기능}UiState>`
- ViewModel에서 UseCase 호출
- UseCase는 Repository 인터페이스에만 의존 (domain 레이어)
- Repository 구현체는 data 레이어에 위치
- DB 접근은 Flow 사용
- 주석은 한국어로 작성

## Compose State Hoisting 규칙
- 상태는 항상 사용하는 컴포저블 중 가장 상위로 끌어올릴 것
- 컴포저블은 상태를 직접 소유하지 않고 state + event lambda를 파라미터로 받을 것
    - 예: `fun NoteItem(note: Note, onDelete: () -> Unit)` (내부에서 직접 ViewModel 접근 금지)
- Stateful 컴포저블과 Stateless 컴포저블 분리
    - Stateful: ViewModel 참조, UiState 수집 (Screen 단위)
    - Stateless: state/event만 파라미터로 받음 (재사용 가능한 UI 단위)
- `remember`, `mutableStateOf`는 Stateless 컴포저블 내 UI 전용 로컬 상태에만 사용 (예: 텍스트필드 포커스, 애니메이션)
- 비즈니스 상태는 무조건 ViewModel의 UiState로 관리

## 네이밍 규칙
- ViewModel: `{기능}ViewModel`
- UseCase: `{동사}{명사}UseCase` (예: GetNotesUseCase)
- Repository 인터페이스: `{명사}Repository`
- Repository 구현체: `{명사}RepositoryImpl`
- Entity: `{명사}Entity`
- Domain Model: `{명사}` (Entity 없이 순수 모델)

## 주의사항
- domain 레이어는 Android 프레임워크 의존성 금지
- presentation 레이어에서 Entity 직접 사용 금지, domain model로 변환 후 사용
- Hilt 모듈은 di/ 패키지에 모아서 관리