/* Kibo app — Macros screen */
function MacrosScreen() {
  const { Card, Keycap, Button, Badge, ListRow } = window.DesignSystem_396ddf;
  const I = window.KiboIcons;
  const macros = [
    { trigger: ["⌘", "1"], label: "이메일 서명", out: "감사합니다,\n김키보 드림", on: true },
    { trigger: ["⌥", "D"], label: "오늘 날짜", out: "2026. 06. 19.", on: true },
    { trigger: ["⌘", "⇧", "M"], label: "회의 링크", out: "meet.kibo.app/…", on: false },
  ];
  return (
    <div style={{ flex: 1, overflowY: "auto", paddingBottom: 24 }}>
      <ScreenHeader title="매크로" subtitle="키 조합으로 자주 쓰는 문구를 입력하세요"
        trailing={<Button variant="primary" size="sm" leadingIcon={I.plus({ size: 16 })}>추가</Button>} />

      <div style={{ padding: "0 22px", display: "flex", flexDirection: "column", gap: 12 }}>
        {macros.map((m, i) => (
          <Card key={i} padding="md" elevation="flat">
            <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
              <div style={{ display: "flex", alignItems: "center", gap: 5 }}>
                {m.trigger.map((t, j) => <Keycap key={j}>{t}</Keycap>)}
              </div>
              <Badge variant={m.on ? "accent-subtle" : "outline"} dot={m.on}>{m.on ? "켜짐" : "꺼짐"}</Badge>
            </div>
            <div style={{ marginTop: 14 }}>
              <div style={{ fontSize: 15, fontWeight: 600, color: "var(--text-strong)" }}>{m.label}</div>
              <div style={{ marginTop: 4, fontSize: 13, color: "var(--text-muted)", whiteSpace: "pre-line", fontFamily: "var(--font-mono)" }}>{m.out}</div>
            </div>
          </Card>
        ))}
      </div>
    </div>
  );
}
window.MacrosScreen = MacrosScreen;
