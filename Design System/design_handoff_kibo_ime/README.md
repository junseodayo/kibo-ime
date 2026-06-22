# Handoff: Kibo (키보) — 물리 키보드 3개국어 IME

## Overview
Kibo is a **physical-keyboard input method (IME)** for Android, targeting devices with a hardware QWERTY keyboard and a near-square display (BlackBerry Passport / Unihertz Titan class). It inputs **Korean · English · Japanese**, with **Japanese (romaji→kana→kanji, Mozc-based)** as the core differentiator. The physical keyboard is the primary input; an on-screen keyboard exists only as an emergency fallback.

This bundle is the **UI design reference** for that product. The full functional spec is in `feature_spec.md` (read it first — it is the source of truth for behavior).

## About the Design Files
The files under `design_files/` are **design references authored in HTML + React (JSX), styled with CSS custom properties** — prototypes that show the intended look and behavior. **They are not production code to ship.** The task is to **recreate these designs in the target environment** — for this product that means a native **Android `InputMethodService`** (Kotlin/Java) with Jetpack Compose or Android Views for the on-screen surfaces (toolbar, candidate strip, panels, settings activity), and an **NDK build of Mozc** for the Japanese engine. Use the codebase's established patterns; treat the HTML as the visual/interaction spec, not a literal source.

The prototypes run in a browser. To view them live, open `design_files/kibo-app/gallery.html` (needs the compiled design-system bundle from the original project; if it is missing, the screens still document layout/markup). `gallery.html` is the comprehensive screen inventory; `mockups.html` and `clipboard.html` are focused subsets.

## Fidelity
**High-fidelity (hifi).** Final colors, typography (Pyeojin Gothic), spacing, radii, and interaction states are all defined via design tokens (see Design Tokens). Recreate the UI faithfully using these values, mapped onto the codebase's styling system. The **device frame** (square screen + hardware keyboard) is a realistic mock of the target hardware — in the real product the letter keys are physical hardware; only the app content and Kibo's software toolbar/candidate strip render on screen.

---

## Device & Layout Model
- **Display:** 1:1 **square** (≈1440×1440 on the Passport). All on-screen layouts assume a square content area, not a tall phone screen. Design files mock it at 468×468 inside `PassportFrame.jsx`.
- **Hardware keyboard:** 3 physical rows (QWERTYUIOP / ASDFGHJKL / ⇧ ZXCVBNM ⌫), below the screen. Rendered in `PassportFrame.jsx` as device chrome — **not** part of the app UI.
- **On-screen, the IME draws only:** (1) the host app's text field, (2) Kibo's **software toolbar** docked at the bottom of the screen, and (3) when in Japanese conversion, a **candidate strip** above the toolbar. Letter input comes from hardware.

---

## Screens / Views
Files are in `design_files/kibo-app/`. Each screen is a React component exported to `window`.

### 1. Language input — Korean 두벌식 (`InputScreens.jsx` → `KoreanInputScreen`)
- **Purpose:** Show Korean composing state + backspace behavior (spec §3).
- **Layout:** App header (back chevron, title "메모", check) → scrollable doc body → Kibo toolbar at bottom.
- **Components:** Composing jamo cluster rendered with a 2px lime underline + `--accent-subtle` background; blinking-style caret (2px × 20px, `--accent`). A hint card documents: **백스페이스 짧게 = 자모 단위 삭제 (조합 중)**, **길게 = 단어 단위 삭제 (확정 텍스트)**.
- **Toolbar mode indicator:** 한 active.

### 2. Language input — English (`InputScreens.jsx` → `EnglishInputScreen`)
- **Purpose:** Passthrough + Caps; **no autocomplete/autocorrect** (spec §4).
- **Components:** Doc body with caret; hint card noting "자동완성·자동교정 없음, Shift/Caps만". Toolbar mode indicator: EN active, **CAPS** state shown.

### 3. Language input — Japanese conversion (`ToolbarMockupScreen.jsx` → `ToolbarMockupScreen`)
- **Purpose:** The core flow — romaji→kana→kanji (spec §5).
- **Components:** Doc shows confirmed text + composing reading `にほんご` (lime underline). **Candidate strip** (`Toolbar.jsx` → `KiboCandidates`): horizontal candidates, first selected (lime fill), with hints **Space=다음 / Enter=확정** (no number-key selection). Toolbar mode indicator: 日 active.

### 4. Number/Symbol layer (`InputScreens.jsx` → `SymbolLayerInputScreen`)
- **Purpose:** alt/sym layer (spec §6).
- **Components:** Hint card shows `alt + W → 2` via keycaps; notes OS Key Character Map is used, and an in-progress composition is **committed first** before inserting the symbol. Toolbar mode indicator shows **SYM** layer state.

### 5. Language switch key assignment (`MoreScreens.jsx` → `AssignKeyScreen`, prop `mode`)
- **Purpose:** Press-to-assign the switch key (spec §2). Two states:
  - `mode="captured"`: dashed **lime** box showing captured `fn + Space` (live keycaps) + success banner "사용 가능".
  - `mode="conflict"`: dashed **danger** box + danger banner "이 조합은 시스템이 사용 중입니다" (event never reached the IME).
- **Rule line:** "최소 1개 수정자 포함 — 단일 글자키 금지". Buttons: 취소 / 이 조합으로 지정 (disabled on conflict).

### 6. Emoji panel (`Panels.jsx` → `EmojiPanelScreen`) & Symbol panel (`SymbolPanelScreen`)
- **Purpose:** Toolbar 이모지 / 기호 buttons (spec §9). Panel slides up over the keyboard area.
- **Components:** Header (icon + title + ✕), category chip row (first chip = active/lime, horizontal scroll), 7-column grid of glyphs. Emoji panel grid font 24px; symbol panel 19px (includes ₩ € ¥ → ← 【 】「」 etc., with a 일본어 기호 category).

### 7. Clipboard — in use (`ClipboardScreen.jsx` → `ClipboardScreen`)
- **Purpose:** Tapped from the toolbar while typing; list slides up over the keyboard; tap to paste (spec §8).
- **Components:** Chat context (header, messages, input field with caret) → clipboard panel: header (clipboard icon, "클립보드", "· 탭하여 붙여넣기", ✕), segmented tabs **복사 히스토리 / 텍스트 프리셋**, item rows (pinned items show a lime pin first; each row: text ellipsis + meta time + optional pin). Toolbar below.

### 8. Clipboard — management (`ClipboardManageScreen.jsx` → `ClipboardManageScreen`)
- **Purpose:** Full app screen to organize history & presets (spec §8).
- **Components:** ScreenHeader → segmented tabs. **History tab:** settings card (히스토리 보관 "최대 50개", 중복 제거 switch, 비밀번호 칸 제외 switch) + item list (drag handle, text, pin toggle, trash) + "항목 N" / "전체 지우기". **Preset tab:** list with drag/pin/edit(pencil)/trash + "프리셋 추가" primary button.

### 9. On-screen keyboard fallback (`OnScreenKeyboardScreen.jsx`, prop `lang`)
- **Purpose:** Emergency soft keyboard when hardware fails (spec §10). Variants `lang="ko"|"en"|"ja"`.
- **Components:** Warning banner (ko, default) → toolbar (osk toggle active) → soft keys. **ko** = 두벌식 jamo; **en/ja** = QWERTY; **ja** also shows the candidate strip and "space — 変換". Keys: flat, `--surface` fill, 1px border, `0 1px 0 --border-strong` bottom shadow; function keys (⇧ ⌫ !#1 space ↵) on `--surface-subtle`.

### 10. Keyboard settings (`KeyboardSettingsScreen.jsx`)
- **Purpose:** IME configuration (spec §2, §11).
- **Components:** **언어 순서** — draggable list (drag handle, index, language + engine subtitle: 두벌식 표준 / Passthrough·Caps / Mozc·ローマ字→漢字). **언어 전환키** card (fn + Space keycaps, 재지정 button, "최소 1수정자·충돌 감지"). **앱별 기본 언어** — master switch + package→language mappings (카카오톡→한국어, X→English, LINE→日本語) + "앱 매핑 추가". **클립보드** rows. **그 외** — 사용자 사전, 온스크린 키보드, 테마(추후) rows.

### 11. User dictionary (`MoreScreens.jsx` → `UserDictScreen`)
- **Purpose:** Custom words for all 3 languages (spec §7).
- **Components:** ScreenHeader + 추가 button; tabbed 한국어/English/日本語; entry rows (word + reading/category; Japanese shows 漢字 + よみ); delete. Note on 日本語 tab about NEologd dictionary integration.

### 12. Theme / color (`MoreScreens.jsx` → `ThemeScreen`) — spec §13 (later)
- **Purpose:** Recolor toolbar / on-screen keyboard. Marked **추후 (later / post-MVP)**.
- **Components:** Live preview (keycap + toolbar accent). **빠른 선택** color chips + a **"직접 선택"** outline pill (palette icon) that toggles a full **HSV color picker** (saturation/value box with draggable thumb, hue range slider, HEX `<input type=color>`). Selected color updates the preview live.

---

## Interactions & Behavior
- **Language switching:** physical key combo (default `fn + Space`), set via press-to-assign with conflict detection. IME only sees events the OS forwards; system-claimed combos cannot be overridden (show the conflict message). Mode indicator (한/EN/日 + CAPS/SYM) is always visible in the toolbar.
- **Japanese conversion:** Space cycles to next candidate, Enter confirms. No numeric candidate selection. Katakana + full/half-width (全角/半角) handling.
- **Korean backspace:** short press deletes one jamo (within a composing syllable); long press deletes a whole word (committed text). The boundary between composing vs committed must be explicit.
- **Symbol/number layer:** commit any in-progress composition *before* inserting the alt/sym character; identical accuracy across all 3 languages; use `getUnicodeChar(metaState)` rather than hardcoded maps.
- **Clipboard:** dedupe, max 50, pin to top, **exclude password fields**, exclude history from backup/sync.
- **App-specific language:** master ON/OFF; ON looks up by `EditorInfo.packageName`; unmapped apps fall back to the last-used language.
- **Toolbar buttons (spec §9):** mode indicator · on-screen keyboard toggle · 기호 · 이모지 · 클립보드.
- **Transitions:** quiet, no bounce. `--ease-out: cubic-bezier(0.22,1,0.36,1)`; durations 120/180/280ms. Toggles/segments transition background+color; panels fade/slide up.
- **States:** hover = subtle `--surface-subtle` fill or one-step-darker accent; press = `--accent-pressed` + 0.5px nudge (no scale-pop); focus = 2px lime outline + 3px ring; disabled = 0.4 opacity.

## State Management
- **Current language** (한/영/일) + **caps** + **active layer** (base/sym) — drive the mode indicator.
- **Switch-key binding** (modifier+key) + assignment/conflict status.
- **Composition buffer** (Korean automata state; Japanese reading + candidate list + selected index).
- **Per-app language map** (master toggle + package→language) + global last-used language.
- **Clipboard** (history array w/ pins + timestamps, preset array; settings: max count, dedupe, exclude-passwords).
- **User dictionary** per language (word + reading/category).
- **Theme** (accent color; toolbar/keyboard color overrides) — later.
- Settings switches/segments in the mocks are local `useState`; back them with persisted IME preferences (DataStore/SharedPreferences) in the app.

## Design Tokens
Authoritative source: `design_files/tokens/*.css` and `design_files/component-styles/*.css`. Light + dark themes; default follows system (`prefers-color-scheme`), with explicit `[data-theme="light"|"dark"]` overrides.

**Neutrals (greige ramp):** 50 `#FAF9F6` · 100 `#F2F0EA` · 150 `#ECE9E1` · 200 `#E7E4DD` · 300 `#D8D4C9` · 400 `#BBB6A9` · 500 `#9A958A` · 600 `#7A766C` · 700 `#5B584F` · 800 `#3A382F` · 900 `#1C1A16` · 950 `#100F0C`

**Accent — Acid Lime:** 100 `#ECF7C6` · 300 `#DAF27E` · **400 `#C8F03C` (core accent)** · 500 `#B4DC2A` · 600 `#97BA15` · 700 `#6E8A0C`

**Semantic:** danger `#D9442B` · warning `#E0A33A` · success `#4F9D6B` · info `#4A6CF0` (each with a subtle tint, see colors.css).

**Light aliases:** bg `#FAF9F6` · surface `#FFFFFF` · surface-subtle `#F2F0EA` · surface-sunken `#ECE9E1` · text-strong `#1C1A16` · text `#3A382F` · text-muted `#7A766C` · border `#E7E4DD` · border-strong `#D8D4C9` · accent-text `#6E8A0C` · text-on-accent `#1C1A16`.

**Dark aliases:** bg `#100F0C` · surface `#1A1813` · surface-subtle `#211F18` · surface-sunken `#141310` · text-strong `#FAF9F6` · text `#ECE9E1` · text-muted `#BBB6A9` · border `#2C2A22` · accent-text `#C8F03C` (lime unchanged).

**Typography — Pyeojin Gothic** (self-hosted, `fonts/`, weights 300–900). Scale (size / line-height / tracking): display-lg 44 / 1.08 / −0.024em · h1 34 / 1.18 / −0.018em · h2 28 / 1.22 / −0.014em · h3 22 / 1.30 / −0.010em · h4 18 / 1.40 · body-lg 18 / 1.62 · body 16 / 1.60 · body-sm 14 / 1.55 · caption 13 / 1.45 · overline 12 / 1.30 / 0.14em uppercase. Mono stack for shortcuts. Display/headings use weight 700–800 with tight negative tracking; body 400.

**Spacing (4px grid):** 2,4,8,12,16,20,24,32,40,48,64,80,96,128 (`--space-1…14`).
**Radius (sharp):** xs 2 · sm 3 · md 4 · lg 6 · xl 10 · pill 999. Default UI uses 4–6px; pills only for switches/sliders/dots.
**Shadows (soft, low):** sm `0 1px 3px rgba(28,26,22,.06)` · md `0 6px 18px -6px rgba(28,26,22,.10)` · lg `0 18px 48px -12px rgba(28,26,22,.16)`. Dark mode uses black-based shadows.

## Assets
- **Font:** Pyeojin Gothic (펴진고딕), 7 weights, in `fonts/` — self-host in the app.
- **Icons:** Lucide (https://lucide.dev) — 2px stroke, round caps, `currentColor`. The prototypes use inline Lucide-style paths (`kibo-app/icons.jsx`); use Lucide (or the platform's equivalent at matching weight) in the app. Emoji glyphs appear only as *content* in the emoji panel, never as UI chrome.
- **Logo/mark:** keycap mark in the original project's `assets/logo/kibo-mark.svg` (a keycap outline with a lime "live key" square).
- **No raster imagery** — the brand is type + token + keycap-motif driven.

## Files (in this bundle)
- `feature_spec.md` — the functional spec (source of truth).
- `design_files/kibo-app/` — all screen components + device frame + galleries:
  - `PassportFrame.jsx` (device chrome), `PhoneFrame.jsx` (StatusBar/ScreenHeader/SectionLabel helpers), `Toolbar.jsx` (ModeIndicator, KiboCandidates, KiboToolbar), `icons.jsx`
  - `InputScreens.jsx`, `ToolbarMockupScreen.jsx`, `Panels.jsx`, `OnScreenKeyboardScreen.jsx`, `ClipboardScreen.jsx`, `ClipboardManageScreen.jsx`, `KeyboardSettingsScreen.jsx`, `MoreScreens.jsx`
  - `gallery.html` (full inventory), `mockups.html`, `clipboard.html`
- `design_files/styles.css` + `design_files/tokens/` — design tokens & base.
- `design_files/component-styles/` — the `.kb-*` component CSS (Button, Card, ListRow, Switch, Segmented, Keycap, Badge, etc.) — the component contract to recreate.
- `fonts/` — Pyeojin Gothic TTFs.

> Note: the `.jsx` here are **design references**, not the production component library. Recreate them as native Android views/composables following the spec and tokens above.
