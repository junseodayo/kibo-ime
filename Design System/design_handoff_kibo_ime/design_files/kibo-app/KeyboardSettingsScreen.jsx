/* Kibo — keyboard settings, mapped to the feature spec. window.KeyboardSettingsScreen */
function KeyboardSettingsScreen({ onBack }) {
  const { Card, ListRow, Switch, Keycap, Badge, Button, Divider } = window.DesignSystem_396ddf;
  const I = window.KiboIcons;
  const [appLang, setAppLang] = React.useState(true);
  const [pwExclude, setPwExclude] = React.useState(true);
  const [dedupe, setDedupe] = React.useState(true);

  const langOrder = [
    { t: "한국어", s: "두벌식 표준" },
    { t: "English", s: "Passthrough · Caps" },
    { t: "日本語", s: "Mozc · ローマ字→漢字" },
  ];
  const appMaps = [
    { app: "카", name: "카카오톡", lang: "한국어" },
    { app: "X", name: "X", lang: "English" },
    { app: "L", name: "LINE", lang: "日本語" },
  ];

  const label = (t) => <span style={{ fontSize: 15, fontWeight: 600, color: "var(--text-strong)", flexShrink: 0, whiteSpace: "nowrap" }}>{t}</span>;

  return (
    <div style={{ flex: 1, overflowY: "auto", paddingBottom: 28 }}>
      <ScreenHeader title="키보드 설정" subtitle="입력기 동작을 구성하세요" onBack={onBack}
        trailing={<Badge variant="accent-subtle">IME</Badge>} />

      <div style={{ padding: "0 22px", display: "flex", flexDirection: "column", gap: 22 }}>

        {/* 언어 — 순서(드래그) + 전환키 */}
        <div>
          <SectionLabel>언어 · 순서 (드래그로 변경)</SectionLabel>
          <Card padding="none" style={{ overflow: "hidden" }}>
            {langOrder.map((l, i) => (
              <div key={l.t} style={{ display: "flex", alignItems: "center", gap: 14, padding: "14px 16px", borderBottom: i < langOrder.length - 1 ? "1px solid var(--border)" : "none" }}>
                <span style={{ color: "var(--text-subtle)", cursor: "grab", flexShrink: 0 }}>{I.drag({ size: 18 })}</span>
                <span style={{ width: 22, fontSize: 13, fontWeight: 700, color: "var(--accent-text)", fontFamily: "var(--font-mono)", flexShrink: 0 }}>{i + 1}</span>
                <span style={{ flex: 1, minWidth: 0 }}>
                  <span style={{ display: "block", fontSize: 15, fontWeight: 600, color: "var(--text-strong)" }}>{l.t}</span>
                  <span style={{ display: "block", fontSize: 12, color: "var(--text-muted)", marginTop: 1 }}>{l.s}</span>
                </span>
              </div>
            ))}
          </Card>
          <Card padding="md" style={{ marginTop: 12 }}>
            <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: 12 }}>
              {label("언어 전환키")}
              <div style={{ display: "flex", alignItems: "center", gap: 6, flexShrink: 0 }}>
                <Keycap>fn</Keycap>
                <span style={{ color: "var(--text-subtle)" }}>+</span>
                <Keycap wide>Space</Keycap>
              </div>
            </div>
            <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: 12, marginTop: 12 }}>
              <span style={{ fontSize: 12, color: "var(--text-muted)", lineHeight: 1.5 }}>최소 1개 수정자 포함 · 충돌 감지</span>
              <Button variant="secondary" size="sm">재지정</Button>
            </div>
          </Card>
        </div>

        {/* 앱별 기본 언어 */}
        <div>
          <SectionLabel>앱별 기본 언어</SectionLabel>
          <Card padding="none" style={{ overflow: "hidden" }}>
            <ListRow icon={I.languages({ size: 18 })} title="앱별 언어 사용"
              description={appLang ? "앱마다 지정 언어로 시작" : "직전 사용 언어 유지"}
              trailing={<Switch checked={appLang} onChange={setAppLang} />} />
            {appLang ? appMaps.map((m) => (
              <div key={m.name} style={{ display: "flex", alignItems: "center", gap: 14, padding: "12px 16px", borderTop: "1px solid var(--border)" }}>
                <span style={{ width: 30, height: 30, borderRadius: "var(--radius-md)", background: "var(--surface-sunken)", color: "var(--text-strong)", display: "inline-flex", alignItems: "center", justifyContent: "center", fontSize: 13, fontWeight: 700, flexShrink: 0 }}>{m.app}</span>
                <span style={{ flex: 1, fontSize: 14, color: "var(--text-strong)", minWidth: 0, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{m.name}</span>
                <span style={{ fontSize: 13, color: "var(--text-muted)", flexShrink: 0, whiteSpace: "nowrap" }}>{m.lang}</span>
                <span style={{ color: "var(--text-subtle)", flexShrink: 0 }}>{I.chevron({ size: 16 })}</span>
              </div>
            )) : null}
            {appLang ? (
              <button style={{ display: "flex", alignItems: "center", gap: 10, padding: "13px 16px", width: "100%", border: "none", borderTop: "1px solid var(--border)", background: "transparent", cursor: "pointer", color: "var(--accent-text)", fontWeight: 600, fontSize: 14, fontFamily: "var(--font-sans)" }}>
                {I.plus({ size: 18 })} 앱 매핑 추가
              </button>
            ) : null}
          </Card>
        </div>

        {/* 클립보드 */}
        <div>
          <SectionLabel>클립보드</SectionLabel>
          <Card padding="none" style={{ overflow: "hidden" }}>
            <ListRow icon={I.clipboard({ size: 18 })} title="복사 히스토리" value="최대 50개" trailing={I.chevron({ size: 18 })} as="button" />
            <ListRow title="중복 제거" trailing={<Switch checked={dedupe} onChange={setDedupe} />} />
            <ListRow title="비밀번호 칸 제외" description="민감 입력은 저장하지 않음" trailing={<Switch checked={pwExclude} onChange={setPwExclude} />} />
            <ListRow icon={I.pin({ size: 18 })} title="텍스트 프리셋" value="8개" trailing={I.chevron({ size: 18 })} as="button" />
          </Card>
        </div>

        {/* 사용자 사전 · 온스크린 · 테마 */}
        <div>
          <SectionLabel>그 외</SectionLabel>
          <Card padding="none" style={{ overflow: "hidden" }}>
            <ListRow icon={I.book({ size: 18 })} title="사용자 사전" description="한 · 영 · 일 커스텀 단어" trailing={I.chevron({ size: 18 })} as="button" />
            <ListRow icon={I.keyboard({ size: 18 })} title="온스크린 키보드" description="물리 키 고장 시 비상 폴백" value="툴바에서 토글" />
            <ListRow icon={I.palette({ size: 18 })} title="테마 · 색상" trailing={<Badge variant="outline">추후</Badge>} />
          </Card>
        </div>

      </div>
    </div>
  );
}
window.KeyboardSettingsScreen = KeyboardSettingsScreen;
