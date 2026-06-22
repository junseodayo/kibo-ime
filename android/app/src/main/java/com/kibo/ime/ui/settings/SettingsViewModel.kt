package com.kibo.ime.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kibo.ime.clipboard.ClipboardRepository
import com.kibo.ime.dict.UserDictRepository
import com.kibo.ime.engine.Language
import com.kibo.ime.prefs.AppLangMapping
import com.kibo.ime.prefs.ClipboardSettings
import com.kibo.ime.prefs.DictEntry
import com.kibo.ime.prefs.KeyBinding
import com.kibo.ime.prefs.KiboSettings
import com.kibo.ime.prefs.PresetItem
import com.kibo.ime.prefs.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Backs all settings screens. One repo trio, exposed as state flows + mutators. */
class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val settingsRepo = SettingsRepository(app)
    private val clipRepo = ClipboardRepository(app)
    private val dictRepo = UserDictRepository(settingsRepo)

    val settings = settingsRepo.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), KiboSettings())

    val history = clipRepo.history
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val presets = clipRepo.presets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // --- §2 language order & switch key ------------------------------------
    fun setLanguageOrder(order: List<Language>) = launch { settingsRepo.setLanguageOrder(order) }
    fun setSwitchKey(binding: KeyBinding) = launch { settingsRepo.setSwitchKey(binding) }

    // --- §11 app-specific language -----------------------------------------
    fun setAppLangMasterOn(on: Boolean) = launch { settingsRepo.setAppLangMasterOn(on) }
    fun setAppLangMappings(m: List<AppLangMapping>) = launch { settingsRepo.setAppLangMappings(m) }
    fun addAppMapping(mapping: AppLangMapping) =
        launch { settingsRepo.setAppLangMappings(settings.value.appLangMappings + mapping) }
    fun removeAppMapping(pkg: String) =
        launch { settingsRepo.setAppLangMappings(settings.value.appLangMappings.filterNot { it.packageName == pkg }) }

    // --- §8 clipboard -------------------------------------------------------
    fun setClipboardSettings(s: ClipboardSettings) = launch { settingsRepo.setClipboardSettings(s) }
    fun toggleHistoryPin(id: Long) = launch { clipRepo.toggleHistoryPin(id) }
    fun deleteHistory(id: Long) = launch { clipRepo.deleteHistory(id) }
    fun clearHistory() = launch { clipRepo.clearHistory() }
    fun addPreset(text: String) = launch { clipRepo.addPreset(text) }
    fun updatePreset(id: Long, text: String) = launch { clipRepo.updatePreset(id, text) }
    fun togglePresetPin(id: Long) = launch { clipRepo.togglePresetPin(id) }
    fun deletePreset(id: Long) = launch { clipRepo.deletePreset(id) }
    fun reorderPresets(ordered: List<PresetItem>) = launch { clipRepo.reorderPresets(ordered) }

    // --- §7 user dictionary -------------------------------------------------
    fun addDictEntry(lang: Language, word: String, reading: String) =
        launch { dictRepo.add(settings.value.userDict, lang, word, reading) }
    fun deleteDictEntry(id: Long) =
        launch { dictRepo.delete(settings.value.userDict, id) }

    // --- §13 theme ----------------------------------------------------------
    fun setAccentColor(color: Int?) = launch { settingsRepo.setAccentColor(color) }

    private fun launch(block: suspend () -> Unit) = viewModelScope.launch { block() }
}
