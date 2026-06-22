package com.kibo.ime.prefs

import com.kibo.ime.engine.Language
import org.json.JSONArray
import org.json.JSONObject

/** A captured key combination for the language-switch key (spec §2). */
data class KeyBinding(
    val keyCode: Int,
    /** Normalized modifier meta-state (META_*_ON bits), at least one required. */
    val metaState: Int,
) {
    fun toJson(): JSONObject = JSONObject().put("k", keyCode).put("m", metaState)
    companion object {
        fun fromJson(o: JSONObject) = KeyBinding(o.getInt("k"), o.getInt("m"))
        /**
         * Default: Shift + Enter. (Titan2's fn key is captured by the OS as Home,
         * so an fn combo never reaches the IME — spec §2 "system-preempted combos".)
         */
        val DEFAULT = KeyBinding(
            keyCode = android.view.KeyEvent.KEYCODE_ENTER,
            metaState = android.view.KeyEvent.META_SHIFT_ON,
        )
    }
}

/** Package → language mapping for app-specific default language (spec §11). */
data class AppLangMapping(
    val packageName: String,
    val appLabel: String,
    val language: Language,
) {
    fun toJson(): JSONObject = JSONObject()
        .put("pkg", packageName).put("label", appLabel).put("lang", language.tag)
    companion object {
        fun fromJson(o: JSONObject) = AppLangMapping(
            o.getString("pkg"), o.optString("label", o.getString("pkg")),
            Language.fromTag(o.getString("lang")) ?: Language.KOREAN,
        )
    }
}

/** Clipboard history item (spec §8). */
data class ClipItem(
    val id: Long,
    val text: String,
    val pinned: Boolean = false,
    val timestamp: Long = 0L,
) {
    fun toJson(): JSONObject = JSONObject()
        .put("id", id).put("t", text).put("p", pinned).put("ts", timestamp)
    companion object {
        fun fromJson(o: JSONObject) =
            ClipItem(o.getLong("id"), o.getString("t"), o.optBoolean("p"), o.optLong("ts"))
    }
}

/** Text preset — a frequently pasted phrase (spec §8). */
data class PresetItem(
    val id: Long,
    val text: String,
    val pinned: Boolean = false,
) {
    fun toJson(): JSONObject = JSONObject().put("id", id).put("t", text).put("p", pinned)
    companion object {
        fun fromJson(o: JSONObject) = PresetItem(o.getLong("id"), o.getString("t"), o.optBoolean("p"))
    }
}

/** Clipboard behavior settings (spec §8). */
data class ClipboardSettings(
    val maxCount: Int = 50,
    val dedupe: Boolean = true,
    val excludePasswords: Boolean = true,
)

/** A user-dictionary entry for one language (spec §7). */
data class DictEntry(
    val id: Long,
    val language: Language,
    val word: String,
    /** Reading/category. For 日本語: 漢字 surface + よみ; for 한국어: 고유명사 etc. */
    val reading: String,
) {
    fun toJson(): JSONObject = JSONObject()
        .put("id", id).put("lang", language.tag).put("w", word).put("r", reading)
    companion object {
        fun fromJson(o: JSONObject) = DictEntry(
            o.getLong("id"), Language.fromTag(o.getString("lang")) ?: Language.KOREAN,
            o.getString("w"), o.optString("r"),
        )
    }
}

// ---- small JSON list helpers -------------------------------------------------

internal fun <T> List<T>.encode(toJson: (T) -> JSONObject): String {
    val arr = JSONArray()
    forEach { arr.put(toJson(it)) }
    return arr.toString()
}

internal fun <T> String?.decode(fromJson: (JSONObject) -> T): List<T> {
    if (this.isNullOrBlank()) return emptyList()
    return runCatching {
        val arr = JSONArray(this)
        (0 until arr.length()).map { fromJson(arr.getJSONObject(it)) }
    }.getOrDefault(emptyList())
}
