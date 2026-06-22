# Kibo 키보 — 물리 키보드 한·영·일 IME (Android)

물리 QWERTY 키보드 기기(Unihertz Titan 2 / BlackBerry Passport 계열, Android 14+)용 입력기(IME).
한국어·영어·일본어를 입력하며, **일본어(로마자→가나→한자, Mozc 기반)**가 핵심 차별점입니다.
물리 키보드가 1차 입력 수단이고 온스크린 키보드는 비상용 폴백입니다.

> 디자인/기능 출처: `../Design System/design_handoff_kibo_ime/`의 `feature_spec.md`(동작 정의서)와
> `README.md`(디자인 핸드오프), 디자인 토큰(`design_files/tokens`).

## 환경

- **언어/UI:** Kotlin + Jetpack Compose
- **compileSdk / targetSdk:** 35 · **minSdk:** 34 (Android 14)
- **AGP 8.7 / Kotlin 2.0 / Compose BOM 2024.12**

## 빌드 / 설치

이 PC는 백그라운드 빌드가 차단되어 있어 **Android Studio에서 빌드**하세요.

1. Android Studio에서 `android/` 폴더를 엽니다.
2. 최초 동기화 시 Gradle Wrapper(`gradlew`, `gradle-wrapper.jar`)가 없으면 Studio가 생성하거나
   번들 Gradle로 동기화합니다. (CLI로 만들려면 Gradle 설치 후 `gradle wrapper` 1회 실행)
3. `app` 실행 → 기기/에뮬레이터에 설치.
4. 앱을 열고 **"시스템 키보드 설정 열기"** → Kibo 활성화 → **"입력기 선택"**에서 Kibo로 전환.
   - 또는 설정 → 시스템 → 언어 및 입력 → 화면 키보드 → 입력기 관리.

## 아키텍처

```
com.kibo.ime
├─ KiboInputMethodService        # IME 본체. 하드웨어 키 가로채기 → 엔진 라우팅, Compose 툴바 호스팅
├─ engine/                       # 언어 엔진 (기기 비종속, 안드로이드 의존 없는 순수 로직)
│  ├─ InputEngine / EditorBridge # 엔진 ↔ 에디터 추상화 (단위 테스트 용이)
│  ├─ Hangul / KoreanEngine      # 두벌식 한글 오토마타 (자모조합·겹받침·도깨비불·백스페이스 분기) §3
│  ├─ EnglishEngine              # 패스스루 + Shift/Caps, 자동완성·교정 없음 §4
│  ├─ Romaji / JapaneseEngine    # 로마자→가나 (실제) + 후보 흐름. 한자변환은 ↓ stub §5
│  └─ JapaneseConverter          # ★ Mozc 연결 지점 (현재 StubJapaneseConverter)
├─ input/
│  ├─ LanguageController         # 언어 상태기계 + 순서 (§2)
│  └─ Fullwidth                  # 전각/반각 (§6)
├─ symbol 처리                    # 서비스 내 getUnicodeChar(metaState) 기반 (§6)
├─ prefs/ · clipboard/ · dict/   # DataStore 영속화 (설정/클립보드/사용자사전) §7,§8,§11
└─ ui/
   ├─ theme/                     # 디자인 토큰 → Compose (greige + acid lime, Pyeojin Gothic)
   ├─ components/                # Keycap, Segmented, Switch 등 공용
   ├─ ime/                       # 툴바·후보스트립·이모지/심볼 패널·온스크린 키보드 (§9,§10)
   └─ settings/                  # 설정 액티비티 전 화면 (§2,§7,§8,§11,§13)
```

### 키 디스패치 흐름 (서비스)
`onKeyDown` → ① 언어 전환키 매칭(§2) → ② 백스페이스(짧게=자모, 길게=단어 §3) /
Space / Enter → ③ alt·sym 레이어면 조합 확정 후 `getUnicodeChar(metaState)` 문자 삽입(§6) →
④ 그 외 글자키는 현재 언어 엔진으로. 소비하지 않으면 OS 기본 처리(영어 패스스루).

## 구현된 항목 (요청: 전체 + 컬러피커)

- §1 IME 골격, 하드웨어 키 인터셉트, 입력 디스패처
- §2 언어 상태기계, 드래그(↑↓) 순서 변경, **press-to-assign 전환키 + 충돌 안내**, 모드 인디케이터
- §3 두벌식 한글 오토마타 (도깨비불/겹받침/백스페이스 자모·단어 분기)
- §4 영어 패스스루
- §5 일본어 로마자→가나 + 후보 스트립(Space=다음/Enter=확정, 터치 선택) — **한자 변환은 인터페이스 stub**
- §6 숫자/심볼 레이어 (Key Character Map 활용, 조합 선확정, 일본어 전각/반각)
- §7 사용자 사전 (3개 언어 탭)
- §8 클립보드 (히스토리+프리셋, 중복제거·최대개수·pin·비번칸 제외·백업 제외)
- §9 툴바 (모드 인디케이터·온스크린 토글·기호·이모지·클립보드)
- §10 온스크린 키보드 폴백 (한 두벌식 / 영·일 QWERTY)
- §11 앱별 기본 언어 (마스터 토글 + 패키지→언어 매핑, EditorInfo.packageName)
- §13 테마 컬러피커 (빠른 선택 칩 + HSV 피커 + HEX)

## ★ 남은 최대 작업: Mozc 일본어 한자 변환 (§5, §15)

현재 `StubJapaneseConverter`는 한자 변환 없이 읽기/가타카나만 후보로 냅니다.
**Mozc 연결 배선은 이미 들어가 있습니다** — 산출물 2개만 떨궈 넣으면 자동 전환됩니다:

| 산출물 | 만드는 법 | 놓을 위치 |
|---|---|---|
| `libmozcjni.so` | macOS/Linux에서 Bazel+NDK 빌드 | `app/src/main/jniLibs/arm64-v8a/` |
| `mozc.data` (사전) | Mozc 빌드가 생성 | `app/src/main/assets/mozc/` |

`JapaneseConverterFactory`가 런타임에 둘의 존재를 확인해 **있으면 Mozc, 없으면 stub**으로
폴백합니다(코드 수정 불필요). 코틀린 바인딩(`MozcJapaneseConverter`)과 JNI 래퍼 스켈레톤
(`app/src/main/cpp/mozc_jni.cc`)도 준비돼 있습니다.

→ 단계별 빌드 방법: **[docs/mozc-integration.md](docs/mozc-integration.md)**
(공식 문서도 엔진 `.so`만 빌드 — APK 클라이언트는 deprecated.)

라이선스(Mozc BSD, NEologd 조건)는 출시 시 점검 (§16).

## 폰트

Pyeojin Gothic(펴진고딕) 7종을 `app/src/main/res/font/`에 self-host. 디자인 핸드오프의
`fonts/`에서 복사됨.

## 알려진 단순화 / TODO

- 온스크린 키보드는 비상용 폴백 수준(레이아웃/기본 키). 롱프레스 액센트 등 미구현.
- 전환키 충돌 감지는 "IME에 이벤트가 도달하지 않으면 시스템 선점"이라는 자연 신호에 의존
  (설정 화면에서 캡처가 안 되면 다른 조합 안내). 시스템 선점 조합 강제 탈취는 불가(§2 한계).
- Gradle Wrapper 바이너리(`gradle-wrapper.jar`)는 미포함 — Android Studio가 생성.
- 백업/복원(§12), 입력방식 변경 옵션(§3 추후), auto-learn 앱언어(§11 추후)는 미구현.
