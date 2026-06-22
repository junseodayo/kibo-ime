/* Kibo app — Layout detail / key remapping. The signature keycap screen. */
function RemapScreen({ onBack }) {
  const { Card, Keycap, Button, Badge, Segmented } = window.DesignSystem_396ddf;
  const I = window.KiboIcons;
  const [sel, setSel] = React.useState("A");
  const [mode, setMode] = React.useState("char");

  const rows = [
    ["Q","W","E","R","T","Y","U","I","O","P"],
    ["A","S","D","F","G","H","J","K","L"],
    ["Z","X","C","V","B","N","M"],
  ];

  return (
    <div style={{ flex: 1, overflowY: "auto", paddingBottom: 24 }}>
      <ScreenHeader title="두벌식 표준" subtitle="키를 눌러 다시 매핑하세요" onBack={onBack}
        trailing={<Badge variant="accent-subtle">편집 중</Badge>} />

      {/* Physical keyboard */}
      <div style={{ padding: "0 16px" }}>
        <Card elevation="flat" padding="md" style={{ background: "var(--surface-subtle)" }}>
          <div style={{ display: "flex", flexDirection: "column", gap: 7, alignItems: "center" }}>
            {rows.map((row, i) => (
              <div key={i} style={{ display: "flex", gap: 6, paddingLeft: i === 1 ? 14 : i === 2 ? 34 : 0 }}>
                {row.map((k) => (
                  <button key={k} onClick={() => setSel(k)} style={{ border: "none", background: "transparent", padding: 0, cursor: "pointer" }}>
                    <Keycap state={sel === k ? "live" : "default"}>{k}</Keycap>
                  </button>
                ))}
              </div>
            ))}
            <div style={{ display: "flex", gap: 6, marginTop: 1 }}>
              <Keycap wide>⇧</Keycap>
              <Keycap state="accent" wide style={{ minWidth: 150 }}>Space</Keycap>
              <Keycap wide>⌫</Keycap>
            </div>
          </div>
        </Card>
      </div>

      {/* Remap panel */}
      <div style={{ padding: "20px 22px 0" }}>
        <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 16 }}>
          <Keycap state="live" size="lg">{sel}</Keycap>
          <span style={{ color: "var(--text-subtle)" }}>{I.arrowRight({ size: 20 })}</span>
          <Keycap state="accent" size="lg">{sel === "A" ? "ㅁ" : "?"}</Keycap>
          <span style={{ marginLeft: "auto", fontSize: 13, color: "var(--text-muted)" }}>{sel} 키 매핑</span>
        </div>

        <Segmented value={mode} onChange={setMode} options={[
          { value: "char", label: "문자" },
          { value: "macro", label: "매크로" },
          { value: "shortcut", label: "단축키" },
        ]} />

        <div style={{ display: "flex", flexWrap: "wrap", gap: 8, marginTop: 16 }}>
          {["ㅁ","ㄴ","ㅇ","ㄹ","ㅎ","ㅗ","ㅓ","ㅏ","ㅣ"].map((c, idx) => (
            <button key={c} style={{ border: "none", background: "transparent", padding: 0, cursor: "pointer" }}>
              <Keycap state={idx === 0 ? "accent" : "default"} size="lg">{c}</Keycap>
            </button>
          ))}
        </div>

        <div style={{ marginTop: 24, display: "flex", gap: 10 }}>
          <Button variant="ghost" block onClick={onBack}>취소</Button>
          <Button variant="primary" block onClick={onBack}>저장</Button>
        </div>
      </div>
    </div>
  );
}
window.RemapScreen = RemapScreen;
