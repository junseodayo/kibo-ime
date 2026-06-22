/* Kibo app — Settings screen. Theme control lifts to App for live switching. */
function SettingsScreen({ themePref, onThemePref }) {
  const { Card, ListRow, Switch, Segmented, Badge } = window.DesignSystem_396ddf;
  const I = window.KiboIcons;
  const [autocorrect, setAutocorrect] = React.useState(true);
  const [sound, setSound] = React.useState(false);
  const [haptic, setHaptic] = React.useState(true);
  const [predict, setPredict] = React.useState(true);

  return (
    <div style={{ flex: 1, overflowY: "auto", paddingBottom: 24 }}>
      <ScreenHeader title="설정" />

      <div style={{ padding: "0 22px", display: "flex", flexDirection: "column", gap: 22 }}>
        <div>
          <SectionLabel>입력</SectionLabel>
          <Card padding="none" style={{ overflow: "hidden" }}>
            <ListRow icon={I.check({ size: 18 })} title="자동 수정" description="오타를 즉시 교정" trailing={<Switch checked={autocorrect} onChange={setAutocorrect} />} />
            <ListRow icon={I.zap({ size: 18 })} title="예측 입력" trailing={<Switch checked={predict} onChange={setPredict} />} />
            <ListRow icon={I.volume({ size: 18 })} title="입력 소리" trailing={<Switch checked={sound} onChange={setSound} />} />
            <ListRow icon={I.vibrate({ size: 18 })} title="햅틱 피드백" trailing={<Switch checked={haptic} onChange={setHaptic} />} />
          </Card>
        </div>

        <div>
          <SectionLabel>화면</SectionLabel>
          <Card padding="md">
            <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: 12 }}>
              <div>
                <div style={{ fontSize: 15, fontWeight: 600, color: "var(--text-strong)" }}>테마</div>
                <div style={{ fontSize: 13, color: "var(--text-muted)", marginTop: 2 }}>시스템 설정을 따릅니다</div>
              </div>
            </div>
            <div style={{ marginTop: 14 }}>
              <Segmented value={themePref} onChange={onThemePref} options={[
                { value: "system", label: "시스템" },
                { value: "light", label: "라이트" },
                { value: "dark", label: "다크" },
              ]} />
            </div>
          </Card>
        </div>

        <div>
          <SectionLabel>정보</SectionLabel>
          <Card padding="none" style={{ overflow: "hidden" }}>
            <ListRow icon={I.bluetooth({ size: 18 })} title="연결된 기기" value="Titan Pocket" trailing={I.chevron({ size: 18 })} as="button" />
            <ListRow icon={I.info({ size: 18 })} title="버전" trailing={<Badge variant="outline">2.4.0</Badge>} />
          </Card>
        </div>
      </div>
    </div>
  );
}
window.SettingsScreen = SettingsScreen;
