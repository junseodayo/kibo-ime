/* Kibo — clipboard MANAGEMENT (full screen in the app): organize history & presets,
   edit/delete/pin/reorder, history settings. window.ClipboardManageScreen */
function ClipboardManageScreen({ onBack }) {
  const { Card, ListRow, Switch, Button, Badge } = window.DesignSystem_396ddf;
  const I = window.KiboIcons;
  const [tab, setTab] = React.useState("history");
  const [dedupe, setDedupe] = React.useState(true);
  const [pwExclude, setPwExclude] = React.useState(true);

  const Tabs = () => (
    <div style={{ display: "inline-flex", padding: 3, background: "var(--surface-sunken)", borderRadius: "var(--radius-md)", gap: 2, width: "100%" }}>
      {[{ v: "history", t: "복사 히스토리" }, { v: "preset", t: "텍스트 프리셋" }].map((o) => (
        <button key={o.v} onClick={() => setTab(o.v)} style={{
          flex: 1, border: "none", cursor: "pointer", padding: "8px 0", borderRadius: "calc(var(--radius-md) - 1px)",
          fontFamily: "var(--font-sans)", fontSize: 14, fontWeight: tab === o.v ? 600 : 500,
          background: tab === o.v ? "var(--surface)" : "transparent",
          color: tab === o.v ? "var(--text-strong)" : "var(--text-muted)",
          boxShadow: tab === o.v ? "var(--shadow-xs)" : "none",
        }}>{o.t}</button>
      ))}
    </div>
  );

  const history = [
    { id: "h0", text: "서울특별시 강남구 테헤란로 123, 4층", meta: "주소 · 고정됨", pin: true },
    { id: "h1", text: "https://kibo.app/download", meta: "방금" },
    { id: "h2", text: "010-1234-5678", meta: "12분 전" },
    { id: "h3", text: "ありがとうございます。", meta: "1시간 전" },
    { id: "h4", text: "오후 3시 회의 가능하세요?", meta: "어제" },
  ];
  const presets = [
    { id: "p0", text: "감사합니다, 김키보 드림", pin: true },
    { id: "p1", text: "회의 링크: meet.kibo.app/abc-defg" },
    { id: "p2", text: "확인했습니다. 곧 회신드리겠습니다." },
  ];

  const RowItem = ({ x, preset }) => (
    <div style={{ display: "flex", alignItems: "center", gap: 12, padding: "12px 14px", borderBottom: "1px solid var(--border)" }}>
      <span style={{ color: "var(--text-subtle)", cursor: "grab", flexShrink: 0 }}>{I.drag({ size: 18 })}</span>
      <span style={{ flex: 1, minWidth: 0 }}>
        <span style={{ display: "block", fontSize: 14, color: "var(--text-strong)", whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis" }}>{x.text}</span>
        {x.meta ? <span style={{ display: "block", fontSize: 11, color: "var(--text-subtle)", marginTop: 2 }}>{x.meta}</span> : null}
      </span>
      <button style={{ flexShrink: 0, border: "none", background: "transparent", cursor: "pointer", padding: 0, color: x.pin ? "var(--accent-text)" : "var(--text-subtle)" }}>{I.pin({ size: 17 })}</button>
      {preset ? <button style={{ flexShrink: 0, border: "none", background: "transparent", cursor: "pointer", padding: 0, color: "var(--text-muted)" }}>{I.pencil({ size: 16 })}</button> : null}
      <button style={{ flexShrink: 0, border: "none", background: "transparent", cursor: "pointer", padding: 0, color: "var(--text-subtle)" }}>{I.trash({ size: 16 })}</button>
    </div>
  );

  return (
    <div style={{ flex: 1, overflowY: "auto", paddingBottom: 28 }}>
      <ScreenHeader title="클립보드 관리" subtitle="복사 히스토리와 자주 쓰는 문구를 정리하세요" onBack={onBack} />

      <div style={{ padding: "0 22px 14px" }}><Tabs /></div>

      {tab === "history" ? (
        <div style={{ padding: "0 22px", display: "flex", flexDirection: "column", gap: 16 }}>
          <Card padding="none" style={{ overflow: "hidden" }}>
            <ListRow title="히스토리 보관" value="최대 50개" trailing={I.chevron({ size: 18 })} as="button" />
            <ListRow title="중복 제거" trailing={<Switch checked={dedupe} onChange={setDedupe} />} />
            <ListRow title="비밀번호 칸 제외" description="민감 입력은 저장하지 않음" trailing={<Switch checked={pwExclude} onChange={setPwExclude} />} />
          </Card>

          <div>
            <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", padding: "0 2px 10px" }}>
              <span style={{ fontSize: 12, fontWeight: 600, letterSpacing: "0.1em", textTransform: "uppercase", color: "var(--text-subtle)", whiteSpace: "nowrap" }}>항목 {history.length}</span>
              <span style={{ fontSize: 13, fontWeight: 600, color: "var(--danger-text)", cursor: "pointer", whiteSpace: "nowrap" }}>전체 지우기</span>
            </div>
            <Card padding="none" style={{ overflow: "hidden" }}>
              {history.map((x) => <RowItem key={x.id} x={x} />)}
            </Card>
          </div>
        </div>
      ) : (
        <div style={{ padding: "0 22px", display: "flex", flexDirection: "column", gap: 16 }}>
          <Card padding="none" style={{ overflow: "hidden" }}>
            {presets.map((x) => <RowItem key={x.id} x={x} preset />)}
          </Card>
          <Button variant="primary" block leadingIcon={I.plus({ size: 18 })}>프리셋 추가</Button>
          <p style={{ margin: 0, fontSize: 12, color: "var(--text-muted)", lineHeight: 1.5, textAlign: "center" }}>자주 붙여넣는 문구를 미리 등록해 두세요. 드래그로 순서를 바꿀 수 있어요.</p>
        </div>
      )}
    </div>
  );
}
window.ClipboardManageScreen = ClipboardManageScreen;
