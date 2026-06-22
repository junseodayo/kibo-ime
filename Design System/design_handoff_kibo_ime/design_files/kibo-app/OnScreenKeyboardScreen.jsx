/* Kibo — on-screen keyboard (spec §10: emergency fallback). Per-language layouts.
   lang: "ko" 두벌식 · "en" QWERTY · "ja" QWERTY(로마자) + 변환 후보. */
function OnScreenKeyboardScreen({ lang = "ko", showBanner = true }) {
  const { Banner } = window.DesignSystem_396ddf;
  const I = window.KiboIcons;

  const layouts = {
    ko: [["ㅂ","ㅈ","ㄷ","ㄱ","ㅅ","ㅛ","ㅕ","ㅑ","ㅐ","ㅔ"], ["ㅁ","ㄴ","ㅇ","ㄹ","ㅎ","ㅗ","ㅓ","ㅏ","ㅣ"], ["ㅋ","ㅌ","ㅊ","ㅍ","ㅠ","ㅜ","ㅡ"]],
    en: [["q","w","e","r","t","y","u","i","o","p"], ["a","s","d","f","g","h","j","k","l"], ["z","x","c","v","b","n","m"]],
    ja: [["q","w","e","r","t","y","u","i","o","p"], ["a","s","d","f","g","h","j","k","l"], ["z","x","c","v","b","n","m"]],
  };
  const rows = layouts[lang];
  const caret = <span style={{ display: "inline-block", width: 2, height: 20, background: "var(--accent)", marginLeft: 1, verticalAlign: "-3px" }} />;
  const docByLang = {
    ko: <>물리 키보드가 안 될 때{caret}</>,
    en: <>Typing without hardware keys{caret}</>,
    ja: <>物理キーボードなしで<span style={{ borderBottom: "2px solid var(--accent)", paddingBottom: 1, background: "var(--accent-subtle)" }}>にほんご</span>{caret}</>,
  };

  const Key = ({ children, fn = false, wide }) => (
    <button style={{
      flex: wide ? "3 1 0" : "1 1 0", height: 46, minWidth: 0,
      border: "1px solid var(--border)", borderRadius: "var(--radius-md)",
      background: fn ? "var(--surface-subtle)" : "var(--surface)",
      color: fn ? "var(--text-muted)" : "var(--text-strong)",
      fontFamily: "var(--font-sans)", fontSize: 17, fontWeight: 500, cursor: "pointer",
      display: "inline-flex", alignItems: "center", justifyContent: "center",
      boxShadow: "0 1px 0 var(--border-strong)",
    }}>{children}</button>
  );

  const spaceLabel = lang === "ja" ? "space — 変換" : lang === "en" ? "space" : "스페이스";

  return (
    <div style={{ flex: 1, display: "flex", flexDirection: "column", minHeight: 0 }}>
      <div style={{ display: "flex", alignItems: "center", gap: 12, padding: "6px 18px 14px", borderBottom: "1px solid var(--border)" }}>
        <span style={{ color: "var(--text-strong)" }}>{I.back({ size: 24 })}</span>
        <div style={{ flex: 1, fontSize: 16, fontWeight: 600, color: "var(--text-strong)" }}>메모</div>
      </div>
      <div style={{ flex: 1, overflowY: "auto", padding: "18px 20px" }}>
        <div style={{ fontSize: 18, lineHeight: 1.7, color: "var(--text-strong)" }}>{docByLang[lang]}</div>
        {showBanner ? <div style={{ marginTop: 16 }}><Banner variant="warning">물리 키보드를 사용할 수 없어 온스크린 키보드로 전환했어요.</Banner></div> : null}
      </div>

      {lang === "ja" ? <KiboCandidates candidates={["日本語", "にほんご", "ニホンゴ"]} active={0} /> : null}
      <KiboToolbar lang={lang} osk={true} />

      <div style={{ background: "var(--surface-sunken)", padding: "8px 6px 10px", display: "flex", flexDirection: "column", gap: 7 }}>
        {rows.map((row, i) => (
          <div key={i} style={{ display: "flex", gap: 6, padding: i === 1 ? "0 18px" : "0 2px" }}>
            {i === 2 ? <Key fn>⇧</Key> : null}
            {row.map((k) => <Key key={k}>{k}</Key>)}
            {i === 2 ? <Key fn>⌫</Key> : null}
          </div>
        ))}
        <div style={{ display: "flex", gap: 6, padding: "0 2px" }}>
          <Key fn>!#1</Key>
          <Key fn><span style={{ display: "inline-flex" }}>{I.languages({ size: 18 })}</span></Key>
          <Key wide>{spaceLabel}</Key>
          <Key fn>↵</Key>
        </div>
      </div>
    </div>
  );
}
window.OnScreenKeyboardScreen = OnScreenKeyboardScreen;
