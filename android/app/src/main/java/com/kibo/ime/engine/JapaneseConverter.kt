package com.kibo.ime.engine

/**
 * Kanji conversion contract (spec §5). This is the seam where **Mozc** plugs in
 * via the NDK — the single biggest piece of remaining work (spec §15).
 *
 * Keep this interface stable: the rest of the IME (candidate strip, space-to-cycle,
 * enter-to-confirm) only depends on this, so swapping the stub for a real Mozc
 * JNI bridge requires no UI/engine changes.
 */
interface JapaneseConverter {
    /** Given a ひらがな reading, return ranked conversion candidates (kanji/kana mix). */
    fun convert(reading: String): List<String>

    /** Add a user-dictionary entry to bias conversion (spec §7). No-op for the stub. */
    fun addUserWord(reading: String, surface: String) {}
}

/**
 * STUB converter — NO real kanji conversion. Returns the reading itself plus a
 * カタカナ form so the candidate flow is exercised end-to-end in the UI.
 *
 * TODO(Mozc): replace with a JNI-backed converter:
 *   1. NDK-build Mozc (`mozc/src`), expose a small C API (start/append/convert/commit).
 *   2. Bundle the system dictionary + NEologd-derived entries (spec §5) as an asset,
 *      with an updatable layout (spec §5 "사전 갱신 가능 구조").
 *   3. Bridge through JNI and implement [convert]/[addUserWord] here.
 */
class StubJapaneseConverter : JapaneseConverter {
    override fun convert(reading: String): List<String> {
        if (reading.isEmpty()) return emptyList()
        val katakana = Romaji.toKatakana(reading)
        // De-dupe while preserving order.
        return listOf(reading, katakana).distinct()
    }
}
