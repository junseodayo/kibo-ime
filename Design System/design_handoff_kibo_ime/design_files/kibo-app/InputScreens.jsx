/* Kibo — input-state screens (spec §3 한국어, §4 영어, §6 숫자/심볼 레이어).
   Shared InputShell keeps the app-context consistent. */

function InputShell({ doc, hint, candidates, candActive = 0, toolbarProps }) {
  const I = window.KiboIcons;
  return (
    <div style={{ flex: 1, display: "flex", flexDirection: "column", minHeight: 0 }}>
      <div style={{ display: "flex", alignItems: "center", gap: 12, padding: "6px 18px 14px", borderBottom: "1px solid var(--border)" }}>
        <span style={{ color: "var(--text-strong)" }}>{I.back({ size: 24 })}</span>
        <div style={{ flex: 1, fontSize: 16, fontWeight: 600, color: "var(--text-strong)" }}>메모</div>
        <span style={{ color: "var(--text-muted)" }}>{I.check({ size: 22 })}</span>
      </div>
      <div style={{ flex: 1, overflowY: "auto", padding: "20px 20px" }}>
        <div style={{ fontSize: 18, lineHeight: 1.85, color: "var(--text-strong)" }}>{doc}</div>
        {hint ? <div style={{ marginTop: 18 }}>{hint}</div> : null}
      </div>
      {candidates ? <KiboCandidates candidates={candidates} active={candActive} /> : null}
      <KiboToolbar {...toolbarProps} />
    </div>
  );
}

/* composing chunk (underlined accent) */
function Composing({ children }) {
  return <span style={{ borderBottom: "2px solid var(--accent)", paddingBottom: 1, background: "var(--accent-subtle)" }}>{children}</span>;
}
function Caret() {
  return <span style={{ display: "inline-block", width: 2, height: 20, background: "var(--accent)", marginLeft: 1, verticalAlign: "-3px" }} />;
}

function HintCard({ title, children }) {
  return (
    <div style={{ background: "var(--surface-subtle)", border: "1px solid var(--border)", borderRadius: "var(--radius-md)", padding: "12px 14px" }}>
      {title ? <div style={{ fontSize: 12, fontWeight: 700, letterSpacing: "0.04em", color: "var(--text-strong)", marginBottom: 6 }}>{title}</div> : null}
      <div style={{ fontSize: 13, color: "var(--text-muted)", lineHeight: 1.55 }}>{children}</div>
    </div>
  );
}

/* §3 — Korean 두벌식, 자모 조합 + 백스페이스 규칙 */
function KoreanInputScreen() {
  const { Keycap } = window.DesignSystem_396ddf;
  return (
    <InputShell
      toolbarProps={{ lang: "ko" }}
      doc={<>오늘은 날씨가 <Composing>맑</Composing><Caret /></>}
      hint={
        <HintCard title="백스페이스 — 조합/확정 분기">
          <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 6 }}>
            <Keycap>⌫</Keycap><span>짧게 — 자모 단위 삭제 (조합 중 글자)</span>
          </div>
          <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
            <Keycap>⌫</Keycap><span style={{ color: "var(--text-subtle)" }}>꾹</span><span>길게 — 단어 단위 삭제 (확정 텍스트)</span>
          </div>
        </HintCard>
      }
    />
  );
}

/* §4 — English passthrough, Caps, no autocorrect */
function EnglishInputScreen() {
  return (
    <InputShell
      toolbarProps={{ lang: "en", caps: true }}
      doc={<>The quick BROWN<Caret /></>}
      hint={<HintCard title="영어 — 패스스루">자동완성·자동교정 없음. Shift / Caps Lock만 처리합니다. 상단 인디케이터에 <b style={{ color: "var(--text-strong)" }}>CAPS</b> 표시.</HintCard>}
    />
  );
}

/* §6 — 숫자/심볼 레이어 (alt/sym), 조합 확정 후 삽입 */
function SymbolLayerInputScreen() {
  const { Keycap } = window.DesignSystem_396ddf;
  return (
    <InputShell
      toolbarProps={{ lang: "ko", layer: true }}
      doc={<>결제 금액 <Composing>₩</Composing><Caret /></>}
      hint={
        <HintCard title="숫자 · 심볼 레이어">
          <div style={{ display: "flex", alignItems: "center", gap: 6, marginBottom: 8 }}>
            <Keycap>alt</Keycap><span style={{ color: "var(--text-subtle)" }}>+</span><Keycap>W</Keycap><span style={{ color: "var(--text-subtle)" }}>→</span><Keycap state="accent">2</Keycap>
          </div>
          OS의 Key Character Map으로 해석 · 진행 중 조합을 <b style={{ color: "var(--text-strong)" }}>먼저 확정한 뒤</b> 삽입. 한·영·일 모두 동일.
        </HintCard>
      }
    />
  );
}

window.InputShell = InputShell;
window.KoreanInputScreen = KoreanInputScreen;
window.EnglishInputScreen = EnglishInputScreen;
window.SymbolLayerInputScreen = SymbolLayerInputScreen;
