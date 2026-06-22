package com.kibo.ime.dict

import com.kibo.ime.engine.Language
import com.kibo.ime.prefs.DictEntry
import com.kibo.ime.prefs.KiboSettings
import com.kibo.ime.prefs.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * User dictionary for all 3 languages (spec §7). Stored inside the settings store
 * (backed up). Convenience CRUD over [SettingsRepository.userDict].
 */
class UserDictRepository(private val settings: SettingsRepository) {

    val entries: Flow<List<DictEntry>> = settings.settings.map { it.userDict }

    suspend fun add(current: List<DictEntry>, language: Language, word: String, reading: String) {
        val entry = DictEntry(System.nanoTime(), language, word.trim(), reading.trim())
        settings.setUserDict(current + entry)
    }

    suspend fun delete(current: List<DictEntry>, id: Long) {
        settings.setUserDict(current.filterNot { it.id == id })
    }

    fun forLanguage(all: List<DictEntry>, language: Language) =
        all.filter { it.language == language }
}
