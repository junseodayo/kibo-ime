/* Kibo — toolbar in context: Japanese input (roman→kana→kanji conversion),
   the app's core differentiator. window.ToolbarMockupScreen */
function ToolbarMockupScreen() {
  const I = window.KiboIcons;
  return (
    <div style={{ flex: 1, display: "flex", flexDirection: "column", minHeight: 0 }}>
      {/* notes app header */}
      <div style={{ display: "flex", alignItems: "center", gap: 12, padding: "6px 18px 14px", borderBottom: "1px solid var(--border)" }}>
        <span style={{ color: "var(--text-strong)" }}>{I.back({ size: 24 })}</span>
        <div style={{ flex: 1, fontSize: 16, fontWeight: 600, color: "var(--text-strong)" }}>メモ</div>
        <span style={{ color: "var(--text-muted)" }}>{I.check({ size: 22 })}</span>
      </div>

      {/* document body with an in-progress Japanese conversion */}
      <div style={{ flex: 1, overflowY: "auto", padding: "20px 20px" }}>
        <div style={{ fontSize: 18, lineHeight: 1.9, color: "var(--text-strong)" }}>
          物理キーボードで
          {/* composing reading — hiragana, underlined per IME convention */}
          <span style={{
            borderBottom: "2px solid var(--accent)", paddingBottom: 1,
            background: "var(--accent-subtle)",
          }}>にほんご</span>
          <span style={{ display: "inline-block", width: 2, height: 20, background: "var(--accent)", marginLeft: 1, verticalAlign: "-3px" }} />
          <span style={{ color: "var(--text-subtle)" }}> を入力。</span>
        </div>
        <div style={{ marginTop: 16, fontSize: 13, color: "var(--text-subtle)" }}>ローマ字 → かな → 漢字 変換中…</div>
      </div>

      {/* Japanese candidate strip (Space=next, Enter=confirm) */}
      <KiboCandidates candidates={["日本語", "にほんご", "ニホンゴ", "日本誤"]} active={0} />

      {/* Kibo functional toolbar in 日本語 mode */}
      <KiboToolbar lang="ja" />
    </div>
  );
}
window.ToolbarMockupScreen = ToolbarMockupScreen;
