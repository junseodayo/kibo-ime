# Kibo — Mobile App UI Kit

Interactive recreation of the Kibo configuration app for smartphones with a physical keyboard. Built entirely from the design-system component primitives (`window.DesignSystem_396ddf`).

## Run
Open `index.html`. It loads `styles.css` + the compiled `_ds_bundle.js`, then mounts the app inside a device frame.

## Screens
| File | Screen | What it shows |
|------|--------|---------------|
| `KeyboardsScreen.jsx` | 키보드 (home) | Active layout card with live keycaps, installed-layout list, add CTA |
| `RemapScreen.jsx` | 키 매핑 | The signature screen — a physical keyboard of `Keycap`s; tap a key to remap it |
| `MacrosScreen.jsx` | 매크로 | Macro list: trigger keycaps → output text |
| `SettingsScreen.jsx` | 설정 | Grouped setting rows; the **테마** segmented control switches the frame light/dark live |

## Shell
- `PhoneFrame.jsx` — device bezel, status bar, `ScreenHeader`, `SectionLabel`
- `icons.jsx` — Lucide-style inline icon set (`window.KiboIcons`)
- `App.jsx` — nav + theme state (`window.KiboApp`)

## Interactions
- Bottom `TabBar` switches between 키보드 / 매크로 / 설정
- Tapping the active layout opens the key-remap screen; tapping any key sets it "live" (lime ring)
- Settings → 테마 → 라이트/다크 re-themes the entire frame instantly
- Switches, segmented controls are all live

Theme defaults to **system** (`prefers-color-scheme`), with explicit light/dark override — matching the brand requirement.
