/* Kibo app — Keyboards (home) screen */
function KeyboardsScreen({ onOpenLayout }) {
  const { Card, Badge, ListRow, Button, Keycap } = window.DesignSystem_396ddf;
  const I = window.KiboIcons;
  const layouts = [
    { id: "ko-std", name: "두벌식 표준", meta: "한국어 · 기본", keys: ["ㅂ","ㅈ","ㄷ","ㄱ","ㅅ"] },
    { id: "ko-old", name: "두벌식 옛글", meta: "한국어 · 확장", keys: ["ㅸ","ㆆ","ㅿ","ㆁ","ㆍ"] },
    { id: "en-colemak", name: "Colemak", meta: "English", keys: ["Q","W","F","P","G"] },
  ];
  return (
    <div style={{ flex: 1, overflowY: "auto", paddingBottom: 24 }}>
      <ScreenHeader title="키보드" subtitle="3개 레이아웃 · 1개 활성"
        trailing={<Button variant="primary" size="sm" leadingIcon={I.plus({ size: 16 })}>추가</Button>} />

      <div style={{ padding: "0 22px" }}>
        <SectionLabel>활성 레이아웃</SectionLabel>
        <Card elevation="raised" padding="none" interactive onClick={() => onOpenLayout("ko-std")} style={{ overflow: "hidden" }}>
          <div style={{ padding: "18px 18px 16px" }}>
            <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 14 }}>
              <div>
                <div style={{ fontSize: 19, fontWeight: 700, color: "var(--text-strong)", letterSpacing: "-0.01em" }}>두벌식 표준</div>
                <div style={{ fontSize: 13, color: "var(--text-muted)", marginTop: 2 }}>한국어 · 기본 매핑</div>
              </div>
              <Badge variant="accent" dot>활성</Badge>
            </div>
            <div style={{ display: "flex", gap: 5 }}>
              {["ㅂ","ㅈ","ㄷ","ㄱ","ㅅ","ㅛ","ㅕ","ㅑ"].map((k) => <Keycap key={k}>{k}</Keycap>)}
            </div>
          </div>
        </Card>
      </div>

      <div style={{ padding: "22px 22px 0" }}>
        <SectionLabel>설치된 레이아웃</SectionLabel>
        <Card padding="none" style={{ overflow: "hidden" }}>
          {layouts.slice(1).map((l) => (
            <ListRow key={l.id} as="button" onClick={() => onOpenLayout(l.id)}
              icon={I.layers({ size: 18 })}
              title={l.name} description={l.meta}
              trailing={I.chevron({ size: 18 })} />
          ))}
          <ListRow as="button" onClick={() => onOpenLayout("ko-std")}
            icon={<span style={{ color: "var(--accent-text)" }}>{I.plus({ size: 18 })}</span>}
            title={<span style={{ color: "var(--accent-text)", fontWeight: 600 }}>스토어에서 레이아웃 찾기</span>}
            trailing={I.chevron({ size: 18 })} />
        </Card>
      </div>
    </div>
  );
}
window.KeyboardsScreen = KeyboardsScreen;
