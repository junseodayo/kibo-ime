/* Kibo — clipboard IN USE: tapped from the toolbar while typing in a real app,
   the list slides up over the keyboard; tap an item to paste. window.ClipboardScreen */
function ClipboardScreen() {
  const { Segmented } = window.DesignSystem_396ddf;
  const I = window.KiboIcons;
  const [tab, setTab] = React.useState("history");

  const history = [
    { id: "h0", text: "서울특별시 강남구 테헤란로 123, 4층", meta: "주소", pin: true },
    { id: "h1", text: "https://kibo.app/download", meta: "방금" },
    { id: "h2", text: "010-1234-5678", meta: "12분 전" },
    { id: "h3", text: "ありがとうございます。", meta: "1시간 전" },
  ];
  const presets = [
    { id: "p0", text: "감사합니다, 김키보 드림", pin: true },
    { id: "p1", text: "회의 링크: meet.kibo.app/abc-defg" },
    { id: "p2", text: "확인했습니다. 곧 회신드리겠습니다." },
  ];
  const list = tab === "history" ? history : presets;

  const bubble = (text, me) => (
    <div style={{ display: "flex", justifyContent: me ? "flex-end" : "flex-start" }}>
      <div style={{ maxWidth: "76%", padding: "9px 13px", fontSize: 15, lineHeight: 1.4, borderRadius: 15,
        background: me ? "var(--accent)" : "var(--surface-subtle)", color: me ? "var(--text-on-accent)" : "var(--text)",
        borderBottomRightRadius: me ? 4 : 15, borderBottomLeftRadius: me ? 15 : 4 }}>{text}</div>
    </div>
  );

  return (
    <div style={{ flex: 1, display: "flex", flexDirection: "column", minHeight: 0 }}>
      {/* chat header */}
      <div style={{ display: "flex", alignItems: "center", gap: 12, padding: "6px 18px 12px", borderBottom: "1px solid var(--border)" }}>
        <span style={{ color: "var(--text-strong)" }}>{I.back({ size: 24 })}</span>
        <div style={{ width: 34, height: 34, borderRadius: "var(--radius-pill)", background: "var(--surface-inverse)", color: "var(--text-inverse)", display: "flex", alignItems: "center", justifyContent: "center", fontWeight: 600, fontSize: 13 }}>팀</div>
        <div style={{ flex: 1, fontSize: 16, fontWeight: 600, color: "var(--text-strong)" }}>출장 준비</div>
      </div>

      {/* messages */}
      <div style={{ flex: 1, overflowY: "auto", padding: "14px 18px", display: "flex", flexDirection: "column", gap: 9, justifyContent: "flex-end" }}>
        {bubble("호텔 주소 좀 보내줄 수 있어?", false)}
        {bubble("응 잠깐만", true)}
      </div>

      {/* text input — about to paste */}
      <div style={{ display: "flex", alignItems: "center", gap: 10, padding: "10px 16px", borderTop: "1px solid var(--border)" }}>
        <div style={{ flex: 1, height: 40, border: "1px solid var(--border-strong)", borderRadius: "var(--radius-pill)", display: "flex", alignItems: "center", padding: "0 16px", fontSize: 15, color: "var(--text-subtle)" }}>
          메시지 입력<span style={{ display: "inline-block", width: 2, height: 18, background: "var(--accent)", marginLeft: 1 }} />
        </div>
        <button style={{ width: 40, height: 40, border: "none", borderRadius: "var(--radius-pill)", background: "var(--surface-subtle)", color: "var(--text-subtle)", display: "inline-flex", alignItems: "center", justifyContent: "center" }}>{I.arrowRight({ size: 20 })}</button>
      </div>

      {/* clipboard list slid up over the keyboard area */}
      <div style={{ background: "var(--surface-sunken)", borderTop: "1px solid var(--border)", display: "flex", flexDirection: "column" }}>
        <div style={{ display: "flex", alignItems: "center", gap: 10, padding: "12px 14px 8px" }}>
          <span style={{ display: "inline-flex", color: "var(--text-strong)" }}>{I.clipboard({ size: 18 })}</span>
          <span style={{ fontSize: 15, fontWeight: 700, color: "var(--text-strong)", flexShrink: 0, whiteSpace: "nowrap" }}>클립보드</span>
          <span style={{ fontSize: 12, color: "var(--text-subtle)", whiteSpace: "nowrap" }}>· 탭하여 붙여넣기</span>
          <div style={{ flex: 1 }} />
          <button style={{ border: "none", background: "transparent", color: "var(--text-muted)", cursor: "pointer", display: "inline-flex" }}>{I.x({ size: 20 })}</button>
        </div>
        <div style={{ padding: "0 14px 8px" }}>
          <Segmented value={tab} onChange={setTab} options={[{ value: "history", label: "복사 히스토리" }, { value: "preset", label: "텍스트 프리셋" }]} />
        </div>
        <div style={{ padding: "0 14px 14px", display: "flex", flexDirection: "column", gap: 8, maxHeight: 196, overflowY: "auto" }}>
          {list.map((x) => (
            <div key={x.id} style={{ display: "flex", alignItems: "center", gap: 10, padding: "11px 13px", background: "var(--surface)", border: "1px solid var(--border)", borderRadius: "var(--radius-md)", cursor: "pointer" }}>
              {x.pin ? <span style={{ flexShrink: 0, color: "var(--accent-text)", display: "inline-flex" }}>{I.pin({ size: 15 })}</span> : null}
              <span style={{ flex: 1, minWidth: 0, fontSize: 14, color: "var(--text-strong)", whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis" }}>{x.text}</span>
              {x.meta ? <span style={{ flexShrink: 0, fontSize: 11, color: "var(--text-subtle)", whiteSpace: "nowrap" }}>{x.meta}</span> : null}
            </div>
          ))}
        </div>
      </div>

      <KiboToolbar lang="ko" />
    </div>
  );
}
window.ClipboardScreen = ClipboardScreen;
