package com.kibo.ime.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kibo.ime.engine.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Snapshot of all IME settings, exposed as one [Flow] for the UI/service to observe. */
data class KiboSettings(
    val languageOrder: List<Language> = Language.DEFAULT_ORDER,
    val switchKey: KeyBinding = KeyBinding.DEFAULT,
    val appLangMasterOn: Boolean = false,
    val appLangMappings: List<AppLangMapping> = emptyList(),
    val clipboard: ClipboardSettings = ClipboardSettings(),
    /** Accent override for the §13 color picker; null = default acid lime. */
    val accentColor: Int? = null,
    val userDict: List<DictEntry> = emptyList(),
)

/**
 * Persists [KiboSettings] in the (backed-up) settings DataStore. Lists are stored
 * as JSON strings via the helpers in Models.kt.
 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val LANG_ORDER = stringPreferencesKey("lang_order")
        val SWITCH_KEY = stringPreferencesKey("switch_key")
        val APP_LANG_ON = booleanPreferencesKey("app_lang_on")
        val APP_LANG_MAP = stringPreferencesKey("app_lang_map")
        val CLIP_MAX = intPreferencesKey("clip_max")
        val CLIP_DEDUPE = booleanPreferencesKey("clip_dedupe")
        val CLIP_EXCL_PW = booleanPreferencesKey("clip_excl_pw")
        val ACCENT = longPreferencesKey("accent_color")
        val USER_DICT = stringPreferencesKey("user_dict")
    }

    val settings: Flow<KiboSettings> = context.settingsStore.data.map { p ->
        val order = p[Keys.LANG_ORDER]?.split(",")
            ?.mapNotNull { Language.fromTag(it) }
            ?.takeIf { it.size == Language.entries.size }
            ?: Language.DEFAULT_ORDER
        val switch = p[Keys.SWITCH_KEY]?.let {
            runCatching { KeyBinding.fromJson(org.json.JSONObject(it)) }.getOrNull()
        } ?: KeyBinding.DEFAULT
        KiboSettings(
            languageOrder = order,
            switchKey = switch,
            appLangMasterOn = p[Keys.APP_LANG_ON] ?: false,
            appLangMappings = p[Keys.APP_LANG_MAP].decode(AppLangMapping::fromJson),
            clipboard = ClipboardSettings(
                maxCount = p[Keys.CLIP_MAX] ?: 50,
                dedupe = p[Keys.CLIP_DEDUPE] ?: true,
                excludePasswords = p[Keys.CLIP_EXCL_PW] ?: true,
            ),
            accentColor = p[Keys.ACCENT]?.toInt(),
            userDict = p[Keys.USER_DICT].decode(DictEntry::fromJson),
        )
    }

    suspend fun setLanguageOrder(order: List<Language>) = edit {
        it[Keys.LANG_ORDER] = order.joinToString(",") { l -> l.tag }
    }

    suspend fun setSwitchKey(binding: KeyBinding) = edit {
        it[Keys.SWITCH_KEY] = binding.toJson().toString()
    }

    suspend fun setAppLangMasterOn(on: Boolean) = edit { it[Keys.APP_LANG_ON] = on }

    suspend fun setAppLangMappings(mappings: List<AppLangMapping>) = edit {
        it[Keys.APP_LANG_MAP] = mappings.encode(AppLangMapping::toJson)
    }

    suspend fun setClipboardSettings(s: ClipboardSettings) = edit {
        it[Keys.CLIP_MAX] = s.maxCount
        it[Keys.CLIP_DEDUPE] = s.dedupe
        it[Keys.CLIP_EXCL_PW] = s.excludePasswords
    }

    suspend fun setAccentColor(color: Int?) = edit {
        if (color == null) it.remove(Keys.ACCENT) else it[Keys.ACCENT] = color.toLong()
    }

    suspend fun setUserDict(entries: List<DictEntry>) = edit {
        it[Keys.USER_DICT] = entries.encode(DictEntry::toJson)
    }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.settingsStore.edit(block)
    }
}
