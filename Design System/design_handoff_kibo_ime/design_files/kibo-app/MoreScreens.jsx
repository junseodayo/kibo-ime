/* Kibo — §2 언어 전환키 지정(+충돌), §7 사용자 사전, §13 테마(추후) */

/* §2 — press-to-assign language switch key. mode: "captured" | "conflict" */
function AssignKeyScreen({ mode = "captured" }) {
  const { Keycap, Button, Banner, Badge } = window.DesignSystem_396ddf;
  const conflict = mode === "conflict";
  return (
    <div style={{ flex: 1, overflowY: "auto", display: "flex", flexDirection: "column" }}>
      <ScreenHeader title="언어 전환키" subtitle="전환에 사용할 키 조합을 직접 누르세요" onBack={() => {}} />
      <div style={{ padding: "0 22px", display: "flex", flexDirection: "column", gap: 16 }}>
        <div style={{
          border: "1.5px dashed " + (conflict ? "var(--danger)" : "var(--accent)"),
          background: conflict ? "var(--danger-subtle)" : "var(--accent-subtle)",
          borderRadius: "var(--radius-lg)", padding: "32px 20px", textAlign: "center",
        }}>
          <div style={{ fontSize: 13, fontWeight: 600, color: "var(--text-muted)", marginBottom: 16 }}>
            {conflict ? "감지된 조합" : "지정된 조합"}
          </div>
          <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: 8 }}>
            <Keycap state={conflict ? "default" : "live"} size="lg">fn</Keycap>
            <span style={{ color: "var(--text-subtle)", fontSize: 18 }}>+</span>
            <Keycap state={conflict ? "default" : "live"} size="lg" wide>Space</Keycap>
          </div>
        </div>

        {conflict
          ? <Banner variant="danger" title="이 조합은 시스템이 사용 중입니다">IME에 이벤트가 도달하지 않았어요. 다른 조합을 눌러 주세요.</Banner>
          : <Banner variant="success">사용 가능한 조합입니다. 기존 바인딩과 충돌 없음.</Banner>}

        <div style={{ display: "flex", alignItems: "center", gap: 8, fontSize: 12, color: "var(--text-muted)" }}>
          <Badge variant="outline">규칙</Badge> 최소 1개 수정자(fn·shift·alt·sym) 포함 — 단일 글자키 금지
        </div>

        <div style={{ display: "flex", gap: 10, marginTop: 4 }}>
          <Button variant="ghost" block>취소</Button>
          <Button variant="primary" block {...(conflict ? { "aria-disabled": "true" } : {})}>이 조합으로 지정</Button>
        </div>
      </div>
    </div>
  );
}

/* §7 — user dictionary (3 languages) */
function UserDictScreen() {
  const { Card, Button, Badge } = window.DesignSystem_396ddf;
  const I = window.KiboIcons;
  const [tab, setTab] = React.useState("ja");
  const data = {
    ko: [{ w: "키보", r: "고유명사" }, { w: "펴진고딕", r: "서체명" }, { w: "테헤란로", r: "지명" }],
    en: [{ w: "Kibo", r: "brand" }, { w: "Unihertz", r: "device" }],
    ja: [{ w: "如月", r: "きさらぎ" }, { w: "兎月", r: "うづき" }, { w: "推し活", r: "おしかつ · 신조어" }],
  };
  const tabs = [{ v: "ko", t: "한국어" }, { v: "en", t: "English" }, { v: "ja", t: "日本語" }];
  return (
    <div style={{ flex: 1, overflowY: "auto", paddingBottom: 24 }}>
      <ScreenHeader title="사용자 사전" subtitle="3개 언어 커스텀 단어 · 변환 보정" onBack={() => {}}
        trailing={<Button variant="primary" size="sm" leadingIcon={I.plus({ size: 16 })}>추가</Button>} />
      <div style={{ padding: "0 22px 14px" }}>
        <div style={{ display: "inline-flex", padding: 3, background: "var(--surface-sunken)", borderRadius: "var(--radius-md)", gap: 2, width: "100%" }}>
          {tabs.map((o) => (
            <button key={o.v} onClick={() => setTab(o.v)} style={{ flex: 1, border: "none", cursor: "pointer", padding: "8px 0", borderRadius: "calc(var(--radius-md) - 1px)", fontFamily: "var(--font-sans)", fontSize: 14, fontWeight: tab === o.v ? 600 : 500, background: tab === o.v ? "var(--surface)" : "transparent", color: tab === o.v ? "var(--text-strong)" : "var(--text-muted)", boxShadow: tab === o.v ? "var(--shadow-xs)" : "none" }}>{o.t}</button>
          ))}
        </div>
      </div>
      <div style={{ padding: "0 22px" }}>
        <Card padding="none" style={{ overflow: "hidden" }}>
          {data[tab].map((e, i) => (
            <div key={i} style={{ display: "flex", alignItems: "center", gap: 12, padding: "13px 16px", borderBottom: i < data[tab].length - 1 ? "1px solid var(--border)" : "none" }}>
              <span style={{ flex: 1, minWidth: 0 }}>
                <span style={{ display: "block", fontSize: 16, fontWeight: 600, color: "var(--text-strong)" }}>{e.w}</span>
                <span style={{ display: "block", fontSize: 12, color: "var(--text-muted)", marginTop: 1 }}>{e.r}</span>
              </span>
              <button style={{ flexShrink: 0, border: "none", background: "transparent", cursor: "pointer", color: "var(--text-subtle)" }}>{I.trash({ size: 16 })}</button>
            </div>
          ))}
        </Card>
        {tab === "ja" ? <p style={{ fontSize: 12, color: "var(--text-muted)", marginTop: 12, lineHeight: 1.5 }}>인명·지명·신조어 보강. NEologd 계열 사전과 통합되어 변환 후보에 반영됩니다.</p> : null}
      </div>
    </div>
  );
}

/* §13 — theme / color (추후). Curated chips + a free HSV color picker. */
function _hsv2rgb(h, s, v) {
  const c = v * s, x = c * (1 - Math.abs((h / 60) % 2 - 1)), m = v - c;
  let r = 0, g = 0, b = 0;
  if (h < 60) { r = c; g = x; } else if (h < 120) { r = x; g = c; }
  else if (h < 180) { g = c; b = x; } else if (h < 240) { g = x; b = c; }
  else if (h < 300) { r = x; b = c; } else { r = c; b = x; }
  return [Math.round((r + m) * 255), Math.round((g + m) * 255), Math.round((b + m) * 255)];
}
function _hsv2hex(h, s, v) { return "#" + _hsv2rgb(h, s, v).map((n) => n.toString(16).padStart(2, "0")).join("").toUpperCase(); }
function _hex2hsv(hex) {
  const r = parseInt(hex.slice(1, 3), 16) / 255, g = parseInt(hex.slice(3, 5), 16) / 255, b = parseInt(hex.slice(5, 7), 16) / 255;
  const mx = Math.max(r, g, b), mn = Math.min(r, g, b), d = mx - mn;
  let h = 0;
  if (d) { if (mx === r) h = 60 * (((g - b) / d) % 6); else if (mx === g) h = 60 * ((b - r) / d + 2); else h = 60 * ((r - g) / d + 4); }
  if (h < 0) h += 360;
  return [h, mx ? d / mx : 0, mx];
}
function _lum(hex) { const p = [1, 3, 5].map((i) => parseInt(hex.slice(i, i + 2), 16) / 255); return 0.2126 * p[0] + 0.7152 * p[1] + 0.0722 * p[2]; }

function ThemeScreen() {
  const { Card, ListRow, Badge, Keycap } = window.DesignSystem_396ddf;
  const I = window.KiboIcons;
  const presets = ["#C8F03C", "#2F6BFF", "#16A06A", "#FF5A36", "#A78BFA", "#1C1A16"];
  const [hsv, setHsv] = React.useState(() => _hex2hsv("#C8F03C"));
  const [showPicker, setShowPicker] = React.useState(false);
  const [h, s, v] = hsv;
  const color = _hsv2hex(h, s, v);
  const onColor = _lum(color) > 0.6 ? "#1C1A16" : "#fff";
  const svRef = React.useRef(null);
  const pickSV = (e) => {
    const r = svRef.current.getBoundingClientRect();
    const x = Math.min(Math.max((e.clientX - r.left) / r.width, 0), 1);
    const y = Math.min(Math.max((e.clientY - r.top) / r.height, 0), 1);
    setHsv([h, x, 1 - y]);
  };
  return (
    <div style={{ flex: 1, overflowY: "auto", paddingBottom: 24 }}>
      <ScreenHeader title="테마 · 색상" subtitle="툴바·온스크린 키보드 색상" onBack={() => {}}
        trailing={<Badge variant="outline">추후</Badge>} />
      <div style={{ padding: "0 22px", display: "flex", flexDirection: "column", gap: 18 }}>
        {/* live preview */}
        <Card padding="md">
          <div style={{ fontSize: 12, color: "var(--text-subtle)", marginBottom: 12 }}>미리보기</div>
          <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
            <Keycap>한</Keycap>
            <Keycap style={{ background: color, borderColor: color, color: onColor }}>A</Keycap>
            <span style={{ flex: 1 }} />
            <span style={{ width: 36, height: 36, borderRadius: "var(--radius-md)", background: color, display: "inline-flex", alignItems: "center", justifyContent: "center", color: onColor }}>{I.zap({ size: 18 })}</span>
          </div>
        </Card>

        {/* curated chips */}
        <div>
          <SectionLabel>포인트 색상 · 빠른 선택</SectionLabel>
          <Card padding="md">
            <div style={{ display: "flex", gap: 12, flexWrap: "wrap" }}>
              {presets.map((c) => (
                <button key={c} onClick={() => { setHsv(_hex2hsv(c)); setShowPicker(false); }} aria-label={c} style={{
                  width: 40, height: 40, borderRadius: "var(--radius-pill)", background: c, cursor: "pointer",
                  border: !showPicker && color === c ? "2px solid var(--text-strong)" : "2px solid transparent",
                  outline: !showPicker && color === c ? "2px solid var(--surface)" : "none", outlineOffset: -4,
                }} />
              ))}
              <button onClick={() => setShowPicker((p) => !p)} aria-label="직접 선택" style={{
                display: "inline-flex", alignItems: "center", gap: 7, height: 40, padding: "0 16px",
                borderRadius: "var(--radius-md)", cursor: "pointer",
                fontFamily: "var(--font-sans)", fontSize: 13, fontWeight: 600,
                border: "1px solid " + (showPicker ? "var(--accent)" : "var(--border-strong)"),
                background: showPicker ? "var(--accent-subtle)" : "var(--surface)",
                color: showPicker ? "var(--accent-text)" : "var(--text-strong)",
              }}>
                <span style={{ display: "inline-flex" }}>{I.palette({ size: 16 })}</span>
                직접 선택
              </button>
            </div>
          </Card>
        </div>

        {/* free color picker — revealed by the 직접 선택 chip */}
        {showPicker ? (
        <div>
          <SectionLabel>직접 선택</SectionLabel>
          <Card padding="md">
            <div ref={svRef}
              onPointerDown={(e) => { e.currentTarget.setPointerCapture(e.pointerId); pickSV(e); }}
              onPointerMove={(e) => { if (e.buttons) pickSV(e); }}
              style={{ position: "relative", height: 150, borderRadius: "var(--radius-md)", cursor: "crosshair", touchAction: "none",
                background: "linear-gradient(to top, #000, transparent), linear-gradient(to right, #fff, hsl(" + h + ",100%,50%))" }}>
              <span style={{ position: "absolute", left: (s * 100) + "%", top: ((1 - v) * 100) + "%", transform: "translate(-50%,-50%)", width: 18, height: 18, borderRadius: "50%", border: "2px solid #fff", boxShadow: "0 0 0 1px rgba(0,0,0,0.35)", background: color }} />
            </div>
            <input type="range" min="0" max="360" value={Math.round(h)} onChange={(e) => setHsv([Number(e.target.value), s, v])}
              style={{ width: "100%", height: 14, marginTop: 16, borderRadius: 999, WebkitAppearance: "none", appearance: "none", cursor: "pointer",
                background: "linear-gradient(to right,#f00,#ff0,#0f0,#0ff,#00f,#f0f,#f00)" }} />
            <div style={{ display: "flex", alignItems: "center", gap: 10, marginTop: 16 }}>
              <span style={{ width: 32, height: 32, borderRadius: "var(--radius-sm)", background: color, border: "1px solid var(--border)", flexShrink: 0 }} />
              <span style={{ fontFamily: "var(--font-mono)", fontSize: 14, fontWeight: 600, color: "var(--text-strong)", whiteSpace: "nowrap" }}>{color}</span>
              <div style={{ flex: 1 }} />
              <label style={{ display: "inline-flex", alignItems: "center", gap: 8, fontSize: 13, fontWeight: 600, color: "var(--text-muted)", cursor: "pointer", whiteSpace: "nowrap" }}>
                HEX 입력
                <input type="color" value={color} onChange={(e) => setHsv(_hex2hsv(e.target.value))}
                  style={{ width: 32, height: 32, padding: 0, border: "1px solid var(--border-strong)", borderRadius: "var(--radius-sm)", background: "none", cursor: "pointer" }} />
              </label>
            </div>
          </Card>
        </div>
        ) : null}

        <Card padding="none" style={{ overflow: "hidden" }}>
          <ListRow icon={I.sliders({ size: 18 })} title="툴바 색상" value="포인트 색상 사용" trailing={I.chevron({ size: 18 })} as="button" />
          <ListRow icon={I.keyboard({ size: 18 })} title="온스크린 키보드 색상" value="시스템" trailing={I.chevron({ size: 18 })} as="button" />
        </Card>
        <p style={{ fontSize: 12, color: "var(--text-muted)", lineHeight: 1.5, textAlign: "center", margin: 0 }}>테마 변수만 분리하면 구현이 가벼움 · MVP 이후 마감 단계에서 제공 예정</p>
      </div>
    </div>
  );
}

window.AssignKeyScreen = AssignKeyScreen;
window.UserDictScreen = UserDictScreen;
window.ThemeScreen = ThemeScreen;
