package com.kibo.ime.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * Two separate stores:
 *  - [settingsStore] — everything backed up (spec §12 stage 0);
 *  - [clipboardStore] — clipboard history, **excluded from backup/sync** (spec §8).
 *    The file name `clipboard.preferences_pb` is the one excluded in the manifest backup rules.
 */
val Context.settingsStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
val Context.clipboardStore: DataStore<Preferences> by preferencesDataStore(name = "clipboard")
