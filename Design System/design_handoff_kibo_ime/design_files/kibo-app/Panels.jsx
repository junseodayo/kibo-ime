/* Kibo — toolbar panels: 이모지 / 기호 (spec §9). Pop over the keyboard area. */

function PanelShell({ icon, title, chips, activeChip, grid, fontSize, lang = "ko" }) {
  const I = window.KiboIcons;
  return (
    <div style={{ flex: 1, display: "flex", flexDirection: "column", minHeight: 0 }}>
      <div style={{ display: "flex", alignItems: "center", gap: 12, padding: "6px 18px 12px", borderBottom: "1px solid var(--border)" }}>
        <span style={{ color: "var(--text-strong)" }}>{I.back({ size: 24 })}</span>
        <div style={{ flex: 1, fontSize: 16, fontWeight: 600, color: "var(--text-strong)" }}>메시지</div>
      </div>
      <div style={{ flex: 1, minHeight: 30, background: "var(--bg)" }} />

      <div style={{ background: "var(--surface-sunken)", borderTop: "1px solid var(--border)", display: "flex", flexDirection: "column" }}>
        <div style={{ display: "flex", alignItems: "center", gap: 10, padding: "12px 14px 8px" }}>
          <span style={{ display: "inline-flex", color: "var(--text-strong)" }}>{icon}</span>
          <span style={{ fontSize: 15, fontWeight: 700, color: "var(--text-strong)", flexShrink: 0, whiteSpace: "nowrap" }}>{title}</span>
          <div style={{ flex: 1 }} />
          <button style={{ border: "none", background: "transparent", color: "var(--text-muted)", cursor: "pointer", display: "inline-flex" }}>{I.x({ size: 20 })}</button>
        </div>
        <div style={{ display: "flex", gap: 6, padding: "0 14px 8px", overflowX: "auto" }} className="kb-cands">
          {chips.map((c, i) => (
            <span key={c} style={{ flex: "none", padding: "5px 12px", borderRadius: "var(--radius-pill)", fontSize: 13, fontWeight: 600, whiteSpace: "nowrap",
              background: i === activeChip ? "var(--accent)" : "var(--surface)", color: i === activeChip ? "var(--text-on-accent)" : "var(--text-muted)",
              border: i === activeChip ? "none" : "1px solid var(--border)" }}>{c}</span>
          ))}
        </div>
        <div style={{ display: "grid", gridTemplateColumns: "repeat(7, 1fr)", gap: 4, padding: "4px 12px 14px", maxHeight: 200, overflowY: "auto" }}>
          {grid.map((g, i) => (
            <button key={i} style={{ height: 44, border: "none", background: "transparent", borderRadius: "var(--radius-md)", cursor: "pointer", fontSize: fontSize, color: "var(--text-strong)", display: "inline-flex", alignItems: "center", justifyContent: "center" }}>{g}</button>
          ))}
        </div>
      </div>
      <KiboToolbar lang={lang} />
    </div>
  );
}

function EmojiPanelScreen() {
  const I = window.KiboIcons;
  const emoji = "😀 😂 🥹 😊 😍 😎 🤔 👍 🙏 👏 🔥 ✨ 🎉 ❤️ 💯 ✅ ⭐ 🚀 ☕ 🍙 🌧️ 🗾 📌 💬 📷 🎧 🌙 ⌨️".split(" ");
  return <PanelShell icon={I.smile({ size: 18 })} title="이모지" chips={["최근", "표정", "사람", "자연", "사물", "기호"]} activeChip={0} grid={emoji} fontSize={24} />;
}

function SymbolPanelScreen() {
  const I = window.KiboIcons;
  const syms = "! ? @ # & * ~ ^ % ₩ $ € ¥ £ → ← ↑ ↓ … · — • ° × ÷ ± ≈ ™ © ® 【 】 「 」 〜 ・".split(" ");
  return <PanelShell icon={I.hash({ size: 18 })} title="기호" chips={["일반", "통화", "화살표", "수학", "일본어"]} activeChip={0} grid={syms} fontSize={19} />;
}

window.EmojiPanelScreen = EmojiPanelScreen;
window.SymbolPanelScreen = SymbolPanelScreen;
