package com.kibo.ime.engine

/**
 * Real kanji converter backed by **Mozc** through JNI (spec §5).
 *
 * Because [Romaji] already does ローマ字→かな in Kotlin, Mozc only needs to do
 * かな→漢字: we hand it a hiragana reading and read back ranked candidates. That
 * keeps the native surface tiny — four functions.
 *
 * The native library is `libmozcjni.so`, built out-of-band on macOS/Linux with
 * Bazel + NDK (see `docs/mozc-integration.md`) and dropped into
 * `app/src/main/jniLibs/<abi>/`. If the library or its dictionary data are
 * missing, construction throws and the caller falls back to the stub
 * (see [JapaneseConverterFactory]).
 *
 * @param dataPath absolute path to the unpacked `mozc.data` dictionary blob.
 */
class MozcJapaneseConverter(dataPath: String) : JapaneseConverter {

    private val handle: Long

    init {
        val h = nativeInit(dataPath)
        require(h != 0L) { "Mozc nativeInit failed for dataPath=$dataPath" }
        handle = h
    }

    override fun convert(reading: String): List<String> {
        if (reading.isEmpty()) return emptyList()
        return nativeConvert(handle, reading)?.toList().orEmpty()
    }

    override fun addUserWord(reading: String, surface: String) {
        if (reading.isNotBlank() && surface.isNotBlank()) {
            nativeAddUserWord(handle, reading, surface)
        }
    }

    /** Release the native engine. Safe to call once; idempotent on the native side. */
    fun close() {
        if (handle != 0L) nativeDestroy(handle)
    }

    // --- JNI surface (implemented in src/main/cpp/mozc_jni.cc) ----------------
    private external fun nativeInit(dataPath: String): Long
    private external fun nativeConvert(handle: Long, reading: String): Array<String>?
    private external fun nativeAddUserWord(handle: Long, reading: String, surface: String)
    private external fun nativeDestroy(handle: Long)

    companion object {
        init {
            // Throws UnsatisfiedLinkError if libmozcjni.so isn't bundled yet;
            // JapaneseConverterFactory catches it and falls back to the stub.
            System.loadLibrary("mozcjni")
        }
    }
}
