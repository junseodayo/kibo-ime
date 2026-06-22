package com.kibo.ime.engine

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileNotFoundException

/**
 * Chooses the Japanese converter at runtime (spec §5):
 *  - if `libmozcjni.so` is bundled **and** the `mozc.data` dictionary asset is
 *    present → real [MozcJapaneseConverter];
 *  - otherwise → [StubJapaneseConverter] (kana/katakana only).
 *
 * This lets the app run today (stub) and silently upgrade to Mozc the moment the
 * native artifacts are dropped in — no code change needed.
 *
 * Dictionary update structure (spec §5 "사전 갱신 가능 구조"): `mozc.data` lives as
 * an asset and is copied to filesDir on first run. Swapping the asset (e.g. a
 * NEologd-enriched build) updates the dictionary without touching code.
 */
object JapaneseConverterFactory {

    private const val TAG = "KiboMozc"
    private const val DATA_ASSET = "mozc/mozc.data"
    private const val DATA_FILE = "mozc.data"

    fun create(context: Context): JapaneseConverter {
        return try {
            val dataPath = ensureDictionary(context)
            if (dataPath == null) {
                Log.i(TAG, "mozc.data asset not bundled — using stub converter.")
                StubJapaneseConverter()
            } else {
                MozcJapaneseConverter(dataPath).also { Log.i(TAG, "Mozc converter active.") }
            }
        } catch (t: UnsatisfiedLinkError) {
            Log.i(TAG, "libmozcjni.so not bundled — using stub converter.")
            StubJapaneseConverter()
        } catch (t: Throwable) {
            Log.w(TAG, "Mozc init failed — using stub converter.", t)
            StubJapaneseConverter()
        }
    }

    /** Copy the bundled dictionary to filesDir once; return its path, or null if no asset. */
    private fun ensureDictionary(context: Context): String? {
        val out = File(context.filesDir, DATA_FILE)
        if (out.exists() && out.length() > 0) return out.absolutePath
        return try {
            context.assets.open(DATA_ASSET).use { input ->
                out.outputStream().use { input.copyTo(it) }
            }
            out.absolutePath
        } catch (e: FileNotFoundException) {
            null  // dictionary not shipped yet
        }
    }
}
