package com.kibo.ime.input

import com.kibo.ime.engine.EditorBridge
import com.kibo.ime.engine.EnglishEngine
import com.kibo.ime.engine.InputEngine
import com.kibo.ime.engine.JapaneseEngine
import com.kibo.ime.engine.KoreanEngine
import com.kibo.ime.engine.Language

/**
 * The language mode state machine (spec §2): holds the 3 engines, the active
 * language, and the user-defined order; switches between them and routes the
 * current composition out cleanly on every switch.
 */
class LanguageController(
    private val korean: InputEngine = KoreanEngine(),
    private val english: InputEngine = EnglishEngine(),
    private val japanese: InputEngine = JapaneseEngine(),
) {
    var order: List<Language> = Language.DEFAULT_ORDER
        private set

    var current: Language = order.first()
        private set

    val engine: InputEngine
        get() = engineFor(current)

    private fun engineFor(lang: Language): InputEngine = when (lang) {
        Language.KOREAN -> korean
        Language.ENGLISH -> english
        Language.JAPANESE -> japanese
    }

    fun setOrder(newOrder: List<Language>) {
        if (newOrder.isNotEmpty()) {
            order = newOrder
            if (current !in newOrder) current = newOrder.first()
        }
    }

    /**
     * Force the active language without touching the editor — used on
     * [onStartInput] when there is no live composition to commit (spec §11).
     */
    fun forceLanguage(lang: Language) {
        if (lang in order) {
            current = lang
            engine.reset()
        }
    }

    /** Switch directly to [lang], committing any in-progress composition first. */
    fun switchTo(lang: Language, bridge: EditorBridge) {
        if (lang == current) return
        engine.finishComposition(bridge)
        current = lang
    }

    /** Cycle to the next language in [order] (spec §2 switch key). */
    fun cycle(bridge: EditorBridge): Language {
        engine.finishComposition(bridge)
        val idx = order.indexOf(current).coerceAtLeast(0)
        current = order[(idx + 1) % order.size]
        return current
    }
}
