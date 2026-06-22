# Mozc 일본어 변환 통합 가이드 (spec §5, §15)

Kibo는 **로마자→가나**를 Kotlin(`engine/Romaji.kt`)에서 처리하므로, Mozc에는
**가나 읽기 → 한자 후보** 한 가지만 시킵니다. 그래서 네이티브 표면이 작습니다(함수 4개).

```
Kotlin: "nihongo" --Romaji--> "にほんご" --JNI--> Mozc --> ["日本語","ニホンゴ",...]
                                           │
                          MozcJapaneseConverter (engine/)
                                           │
                                    libmozcjni.so  (Bazel로 빌드)
                                           │
                                   mozc.data  (사전, assets/mozc/)
```

## 동작 원리 (이미 코드에 반영됨)

- `JapaneseConverterFactory.create(context)`가 런타임에 판단:
  - `libmozcjni.so` 있고 `mozc.data` 에셋 있으면 → **MozcJapaneseConverter**
  - 하나라도 없으면 → **StubJapaneseConverter** (가나/가타카나만)
- 즉 **아무것도 안 넣으면 지금처럼 stub**, 두 파일을 떨궈 넣으면 **자동으로 Mozc로 전환**. 코드 수정 불필요.

산출물 두 개를 만들어 아래 위치에 넣으면 끝:

| 산출물 | 만드는 법 | 놓을 위치 |
|---|---|---|
| `libmozcjni.so` | Mozc 워크스페이스에서 Bazel 빌드 | `app/src/main/jniLibs/arm64-v8a/` |
| `mozc.data` | Mozc 빌드가 생성 (OSS dataset) | `app/src/main/assets/mozc/` |

---

## 1. 빌드 환경 (macOS / 맥미니)

공식 문서: https://github.com/google/mozc/blob/master/docs/build_mozc_for_android.md
→ 이 문서는 **엔진 `.so`만** 빌드합니다(“Client code for Android apk is deprecated”). 우리가 원하는 게 정확히 그것.

필요: **Bazel(bazelisk), Android NDK r29, Python 3.12+, Xcode CLT**. (Windows 불가 — 맥/리눅스 전용)

```bash
# Mozc 소스
git clone https://github.com/google/mozc.git
cd mozc/src

# NDK 경로 지정 (예시 — 실제 설치 경로로)
export ANDROID_NDK_HOME="$HOME/Library/Android/sdk/ndk/29.x.x"

# 우선 공식 타겟이 빌드되는지 확인 (엔진 + OSS dataset 생성됨)
bazelisk build package --config oss_android --config release_build
```

위 빌드가 성공하면 OSS 사전 `mozc.data`가 생성됩니다. 경로는 리비전마다 다르니
`bazel-bin` 아래에서 찾으세요 (대개 `bazel-bin/data_manager/oss/mozc.data` 부근):

```bash
find bazel-bin -name 'mozc.data'
```

## 2. JNI 래퍼를 Mozc 워크스페이스에 추가

Kibo의 `app/src/main/cpp/mozc_jni.cc`를 Mozc 소스 트리로 복사하고, BUILD 타겟을 추가합니다.
(이 `.cc`는 Gradle이 아니라 **Bazel이** 컴파일합니다.)

```bash
mkdir -p mozc/src/android/jni/kibo
cp <Kibo>/app/src/main/cpp/mozc_jni.cc mozc/src/android/jni/kibo/
```

`mozc/src/android/jni/kibo/BUILD.bazel` (예시 — `deps` 라벨은 리비전에 맞게 확인):

```python
cc_binary(
    name = "libmozcjni.so",
    srcs = ["mozc_jni.cc"],
    linkshared = True,
    linkstatic = True,
    deps = [
        "//engine",                       # mozc::Engine / EngineInterface
        "//converter:converter_interface",
        "//converter:segments",
        "//data_manager",                 # mozc::DataManager
        "//request:conversion_request",
        # JNI 헤더: NDK가 제공 (jni.h). Bazel android 설정에서 자동 포함되는 경우가 많음.
    ],
)
```

빌드 & 산출물 회수:

```bash
bazelisk build //android/jni/kibo:libmozcjni.so \
    --config oss_android --config release_build

# arm64 .so 와 사전을 Kibo로 복사
cp bazel-bin/android/jni/kibo/libmozcjni.so \
   <Kibo>/app/src/main/jniLibs/arm64-v8a/
cp $(find bazel-bin -name mozc.data | head -1) \
   <Kibo>/app/src/main/assets/mozc/mozc.data
```

> `mozc_jni.cc`의 `// VERIFY:` 주석 라인(헤더 경로, `CreateFromFile`,
> `CreateDesktopEngine`, `StartConversionForRequest`, `candidate(i).value`)은
> 사용하는 Mozc 리비전의 실제 심볼과 맞는지 확인·수정하세요. API가 종종 바뀝니다.

## 3. 확인

`PLACEHOLDER.md`들을 지우고 두 파일을 넣은 뒤 Kibo를 빌드·실행 →
`adb logcat -s KiboMozc` 에 `Mozc converter active.` 가 뜨면 성공.
가나 입력 후 **Space**로 한자 후보가 나오면 정상.

## 4. NEologd 보강 (spec §5, 출시 시 §16)

인명·지명·역명·유행어는 NEologd 항목을 **Mozc 사전 포맷으로 변환해 빌드 시 머지**한
새 `mozc.data`를 만들어 교체합니다. 코드는 그대로, **에셋만 바꾸면 사전 갱신** 완료.
NEologd 재배포 조건은 출시 전 라이선스 점검 대상.

## 5. 사용자 사전 (spec §7)

`nativeAddUserWord`는 현재 no-op(TODO). Mozc `UserDictionaryStorage`에 항목을 추가하고
엔진에 user dict 리로드를 요청하도록 채우면, 설정의 사용자 사전이 변환에 반영됩니다.
