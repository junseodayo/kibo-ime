package com.kibo.ime.clipboard

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kibo.ime.prefs.ClipItem
import com.kibo.ime.prefs.ClipboardSettings
import com.kibo.ime.prefs.PresetItem
import com.kibo.ime.prefs.clipboardStore
import com.kibo.ime.prefs.decode
import com.kibo.ime.prefs.encode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Clipboard history + text presets (spec §8). Stored in the **clipboard** DataStore,
 * which is excluded from backup/sync. Pinned items sort to the top.
 */
class ClipboardRepository(private val context: Context) {

    private val historyKey = stringPreferencesKey("history")
    private val presetKey = stringPreferencesKey("presets")

    val history: Flow<List<ClipItem>> = context.clipboardStore.data
        .map { it[historyKey].decode(ClipItem::fromJson).sortedWith(pinnedFirst) }

    val presets: Flow<List<PresetItem>> = context.clipboardStore.data
        .map { it[presetKey].decode(PresetItem::fromJson).sortedWith(presetPinnedFirst) }

    private val pinnedFirst = compareByDescending<ClipItem> { it.pinned }.thenByDescending { it.timestamp }
    private val presetPinnedFirst = compareByDescending<PresetItem> { it.pinned }.thenBy { it.id }

    /**
     * Record copied [text] into history, honoring dedupe + max count (spec §8).
     * The caller is responsible for skipping password fields (spec §8).
     */
    suspend fun record(text: String, settings: ClipboardSettings) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        context.clipboardStore.edit { p ->
            var list = p[historyKey].decode(ClipItem::fromJson)
            if (settings.dedupe) list = list.filterNot { it.text == trimmed }
            val item = ClipItem(
                id = System.nanoTime(),
                text = trimmed,
                pinned = false,
                timestamp = System.currentTimeMillis(),
            )
            list = listOf(item) + list
            // Enforce max count, but never drop pinned items.
            val pinned = list.filter { it.pinned }
            val unpinned = list.filterNot { it.pinned }.take((settings.maxCount - pinned.size).coerceAtLeast(0))
            p[historyKey] = (pinned + unpinned).encode(ClipItem::toJson)
        }
    }

    suspend fun toggleHistoryPin(id: Long) = mutateHistory { list ->
        list.map { if (it.id == id) it.copy(pinned = !it.pinned) else it }
    }

    suspend fun deleteHistory(id: Long) = mutateHistory { list -> list.filterNot { it.id == id } }

    suspend fun clearHistory() = mutateHistory { it.filter { item -> item.pinned } }

    suspend fun addPreset(text: String) = mutatePresets { list ->
        list + PresetItem(id = System.nanoTime(), text = text.trim())
    }

    suspend fun updatePreset(id: Long, text: String) = mutatePresets { list ->
        list.map { if (it.id == id) it.copy(text = text.trim()) else it }
    }

    suspend fun togglePresetPin(id: Long) = mutatePresets { list ->
        list.map { if (it.id == id) it.copy(pinned = !it.pinned) else it }
    }

    suspend fun deletePreset(id: Long) = mutatePresets { list -> list.filterNot { it.id == id } }

    suspend fun reorderPresets(ordered: List<PresetItem>) = mutatePresets { ordered }

    private suspend fun mutateHistory(block: (List<ClipItem>) -> List<ClipItem>) {
        context.clipboardStore.edit { p ->
            p[historyKey] = block(p[historyKey].decode(ClipItem::fromJson)).encode(ClipItem::toJson)
        }
    }

    private suspend fun mutatePresets(block: (List<PresetItem>) -> List<PresetItem>) {
        context.clipboardStore.edit { p ->
            p[presetKey] = block(p[presetKey].decode(PresetItem::fromJson)).encode(PresetItem::toJson)
        }
    }
}
