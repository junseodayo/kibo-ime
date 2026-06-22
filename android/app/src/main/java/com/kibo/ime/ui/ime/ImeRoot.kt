package com.kibo.ime.ui.ime

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kibo.ime.ui.theme.KiboTheme

/**
 * The whole on-screen IME surface (spec §9, §10). Bottom-docked toolbar, with an
 * optional candidate strip (§5), slide-up panel (§8/§9), or fallback OSK (§10) above it.
 */
@Composable
fun ImeRoot(state: ImeUiState, actions: ImeActions, navStripHeight: Dp) {
    val c = KiboTheme.colors
    Column(Modifier.fillMaxWidth().background(c.surface)) {
        // Candidate strip only when converting and no panel is covering the area.
        val candidates = state.candidates
        if (candidates != null && state.panel == ImePanel.NONE) {
            CandidateStrip(candidates, actions::pickCandidate)
            Divider()
        }

        when (state.panel) {
            ImePanel.EMOJI -> EmojiPanel(onInsert = actions::insert, onClose = actions::closePanel)
            ImePanel.SYMBOL -> SymbolPanel(onInsert = actions::insert, onClose = actions::closePanel)
            ImePanel.CLIPBOARD -> ClipboardPanel(
                history = state.clipHistory,
                presets = state.clipPresets,
                onPaste = { actions.insert(it); actions.closePanel() },
                onClose = actions::closePanel,
            )
            ImePanel.NONE -> if (state.oskVisible) OnScreenKeyboard(state, actions)
        }

        // Toolbar — flat, no top border/shadow.
        KiboToolbar(state, actions)

        // Accent strip behind the system nav buttons — drawn here so it recolors live
        // when the accent (point) color changes (spec §13).
        Box(Modifier.fillMaxWidth().height(navStripHeight).background(c.accent))
    }
}

@Composable
private fun Divider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(KiboTheme.colors.border))
}
