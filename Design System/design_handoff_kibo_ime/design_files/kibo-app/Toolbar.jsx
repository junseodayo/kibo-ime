/* Kibo — IME toolbar per spec §9.
   Functional buttons: mode indicator · on-screen KB toggle · 기호 · 이모지 · 클립보드.
   Plus a Japanese conversion candidate strip (Space=next, Enter=confirm). */

/* Mode indicator — always shows current language + caps + layer (spec §2). */
function ModeIndicator({ lang = "ja", caps = false, layer = false }) {
  const segs = [{ k: "ko", t: "한" }, { k: "en", t: "EN" }, { k: "ja", t: "日" }];
  return (
    <div style={{ display: "inline-flex", alignItems: "center", gap: 8 }}>
      <div style={{ display: "inline-flex", padding: 3, background: "var(--surface-sunken)", borderRadius: "var(--radius-md)", gap: 2 }}>
        {segs.map((s) => (
          <span key={s.k} style={{
            minWidth: 30, height: 28, padding: "0 8px", borderRadius: "calc(var(--radius-md) - 1px)",
            display: "inline-flex", alignItems: "center", justifyContent: "center",
            fontSize: 14, fontWeight: 700,
            background: lang === s.k ? "var(--accent)" : "transparent",
            color: lang === s.k ? "var(--text-on-accent)" : "var(--text-subtle)",
          }}>{s.t}</span>
        ))}
      </div>
      {(caps || layer) ? (
        <span style={{ fontSize: 11, fontWeight: 700, color: "var(--text-muted)", fontFamily: "var(--font-mono)" }}>
          {caps ? "CAPS" : ""}{caps && layer ? " · " : ""}{layer ? "SYM" : ""}
        </span>
      ) : null}
    </div>
  );
}

/* Japanese conversion candidate strip. */
function KiboCandidates({ candidates = [], active = 0 }) {
  return (
    <div style={{ display: "flex", alignItems: "center", height: 48, borderBottom: "1px solid var(--border)", background: "var(--surface)" }}>
      <div className="kb-cands" style={{ flex: 1, display: "flex", alignItems: "center", gap: 6, padding: "0 10px", overflowX: "auto", scrollbarWidth: "none", msOverflowStyle: "none" }}>
        {candidates.map((c, i) => (
          <button key={i} style={{
            flex: "none", border: "none", cursor: "pointer",
            height: 34, padding: "0 14px", borderRadius: "var(--radius-md)",
            fontFamily: "var(--font-sans)", fontSize: 17, fontWeight: i === active ? 700 : 500,
            background: i === active ? "var(--accent)" : "transparent",
            color: i === active ? "var(--text-on-accent)" : "var(--text)",
          }}>{c}</button>
        ))}
      </div>
      <div style={{ flex: "none", display: "flex", alignItems: "center", gap: 10, padding: "0 14px 0 8px", borderLeft: "1px solid var(--border)", height: 24 }}>
        <span style={{ fontSize: 11, color: "var(--text-subtle)", whiteSpace: "nowrap" }}><b style={{ color: "var(--text-muted)" }}>Space</b> 다음</span>
        <span style={{ fontSize: 11, color: "var(--text-subtle)", whiteSpace: "nowrap" }}><b style={{ color: "var(--text-muted)" }}>Enter</b> 확정</span>
      </div>
    </div>
  );
}

/* Functional toolbar row. */
function KiboToolbar({ lang = "ja", caps = false, layer = false, osk = false }) {
  const I = window.KiboIcons;
  const btn = (icon, label, on) => (
    <button aria-label={label} title={label} style={{
      width: 42, height: 42, border: "none", borderRadius: "var(--radius-md)",
      background: on ? "var(--accent)" : "transparent",
      color: on ? "var(--text-on-accent)" : "var(--text-muted)",
      display: "inline-flex", alignItems: "center", justifyContent: "center", cursor: "pointer",
    }}>{icon}</button>
  );
  return (
    <div style={{ background: "var(--surface)", borderTop: "1px solid var(--border)", boxShadow: "0 -8px 24px -12px rgba(28,26,22,0.12)" }}>
      <div style={{ display: "flex", alignItems: "center", gap: 4, padding: "8px 10px" }}>
        <ModeIndicator lang={lang} caps={caps} layer={layer} />
        <div style={{ flex: 1 }} />
        {btn(I.keyboard({ size: 21 }), "온스크린 키보드", osk)}
        {btn(I.hash({ size: 21 }), "기호", false)}
        {btn(I.smile({ size: 21 }), "이모지", false)}
        {btn(I.clipboard({ size: 20 }), "클립보드", false)}
      </div>
    </div>
  );
}

window.ModeIndicator = ModeIndicator;
window.KiboCandidates = KiboCandidates;
window.KiboToolbar = KiboToolbar;
