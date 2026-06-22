// =============================================================================
// Kibo ↔ Mozc JNI bridge (spec §5).
//
// Builds into `libmozcjni.so`. Because Kibo does ローマ字→かな in Kotlin (Romaji.kt),
// this bridge only needs かな→漢字: take a hiragana reading, return ranked candidates.
//
// Built inside the Mozc Bazel workspace (NOT with Gradle) — see
// docs/mozc-integration.md. Verified against the Mozc API as of 2026-06
// (Engine::CreateEngine / StatusOr, shared_ptr<const ConverterInterface>,
// ConverterInterface::StartConversion, mozc::converter::Candidate::value).
//
// Maps to com.kibo.ime.engine.MozcJapaneseConverter:
//   nativeInit(String dataPath) : long
//   nativeConvert(long, String) : String[]
//   nativeAddUserWord(long, String, String) : void
//   nativeDestroy(long) : void
// =============================================================================

#include <jni.h>

#include <cstddef>
#include <memory>
#include <string>
#include <utility>
#include <vector>

#include "composer/composer.h"             // mozc::composer::Composer (seeds the reading)
#include "converter/candidate.h"           // mozc::converter::Candidate (.value)
#include "converter/converter_interface.h" // mozc::ConverterInterface
#include "converter/segments.h"            // mozc::Segments / Segment
#include "data_manager/data_manager.h"     // mozc::DataManager (CreateFromFile -> StatusOr)
#include "engine/engine.h"                 // mozc::Engine (CreateEngine -> StatusOr)
#include "request/conversion_request.h"    // mozc::ConversionRequest

namespace {

// Owns the engine; the converter is a shared_ptr borrowed from the engine.
struct MozcHolder {
  std::unique_ptr<mozc::Engine> engine;
  std::shared_ptr<const mozc::ConverterInterface> converter;
};

std::string ToUtf8(JNIEnv* env, jstring s) {
  if (s == nullptr) return {};
  const char* chars = env->GetStringUTFChars(s, nullptr);
  std::string out(chars ? chars : "");
  if (chars) env->ReleaseStringUTFChars(s, chars);
  return out;
}

MozcHolder* AsHolder(jlong handle) {
  return reinterpret_cast<MozcHolder*>(handle);
}

}  // namespace

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_kibo_ime_engine_MozcJapaneseConverter_nativeInit(
    JNIEnv* env, jobject /*thiz*/, jstring data_path) {
  const std::string path = ToUtf8(env, data_path);

  // DataManager::CreateFromFile -> absl::StatusOr<unique_ptr<const DataManager>>.
  auto data_manager_or = mozc::DataManager::CreateFromFile(path);
  if (!data_manager_or.ok()) {
    return 0;
  }

  // Engine::CreateEngine(unique_ptr<const DataManager>) -> StatusOr<unique_ptr<Engine>>.
  auto engine_or = mozc::Engine::CreateEngine(*std::move(data_manager_or));
  if (!engine_or.ok()) {
    return 0;
  }

  auto* holder = new MozcHolder();
  holder->engine = *std::move(engine_or);
  holder->converter = holder->engine->GetConverter();  // shared_ptr<const ConverterInterface>
  if (holder->converter == nullptr) {
    delete holder;
    return 0;
  }
  return reinterpret_cast<jlong>(holder);
}

JNIEXPORT jobjectArray JNICALL
Java_com_kibo_ime_engine_MozcJapaneseConverter_nativeConvert(
    JNIEnv* env, jobject /*thiz*/, jlong handle, jstring reading) {
  jclass string_class = env->FindClass("java/lang/String");
  MozcHolder* holder = AsHolder(handle);
  if (holder == nullptr || holder->converter == nullptr) {
    return env->NewObjectArray(0, string_class, nullptr);
  }

  const std::string yomi = ToUtf8(env, reading);

  // Mozc takes the reading from the request's composer (NOT a pre-set segment key),
  // so seed a composer with our hiragana and build the conversion request from it.
  mozc::composer::Composer composer;
  composer.SetPreeditTextForTestOnly(yomi);
  const mozc::ConversionRequest request =
      mozc::ConversionRequestBuilder().SetComposer(composer).Build();

  mozc::Segments segments;
  if (!holder->converter->StartConversion(request, &segments) ||
      segments.conversion_segments_size() == 0) {
    return env->NewObjectArray(0, string_class, nullptr);
  }

  const mozc::Segment& result = segments.conversion_segment(0);
  std::vector<std::string> values;
  values.reserve(result.candidates_size());
  for (size_t i = 0; i < result.candidates_size(); ++i) {
    values.push_back(result.candidate(static_cast<int>(i)).value);
  }

  jobjectArray array =
      env->NewObjectArray(static_cast<jsize>(values.size()), string_class, nullptr);
  for (jsize i = 0; i < static_cast<jsize>(values.size()); ++i) {
    jstring jv = env->NewStringUTF(values[static_cast<size_t>(i)].c_str());
    env->SetObjectArrayElement(array, i, jv);
    env->DeleteLocalRef(jv);
  }
  return array;
}

JNIEXPORT void JNICALL
Java_com_kibo_ime_engine_MozcJapaneseConverter_nativeAddUserWord(
    JNIEnv* env, jobject /*thiz*/, jlong handle, jstring reading, jstring surface) {
  // TODO(user-dict): wire into Mozc's user dictionary (spec §7). No-op for now.
  (void)env;
  (void)handle;
  (void)reading;
  (void)surface;
}

JNIEXPORT void JNICALL
Java_com_kibo_ime_engine_MozcJapaneseConverter_nativeDestroy(
    JNIEnv* /*env*/, jobject /*thiz*/, jlong handle) {
  delete AsHolder(handle);
}

}  // extern "C"
