/* Kibo app — root: navigation, theme, device frame. window.KiboApp */
function KiboApp() {
  const { TabBar } = window.DesignSystem_396ddf;
  const I = window.KiboIcons;
  const [tab, setTab] = React.useState("keys");
  const [remapOpen, setRemapOpen] = React.useState(false);
  const [themePref, setThemePref] = React.useState("system");

  const [systemDark, setSystemDark] = React.useState(() =>
    window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches);
  React.useEffect(() => {
    if (!window.matchMedia) return;
    const mq = window.matchMedia("(prefers-color-scheme: dark)");
    const fn = (e) => setSystemDark(e.matches);
    mq.addEventListener ? mq.addEventListener("change", fn) : mq.addListener(fn);
    return () => { mq.removeEventListener ? mq.removeEventListener("change", fn) : mq.removeListener(fn); };
  }, []);
  const theme = themePref === "system" ? (systemDark ? "dark" : "light") : themePref;

  let screen;
  if (tab === "keys") {
    screen = remapOpen
      ? <RemapScreen onBack={() => setRemapOpen(false)} />
      : <KeyboardsScreen onOpenLayout={() => setRemapOpen(true)} />;
  } else if (tab === "macros") {
    screen = <MacrosScreen />;
  } else {
    screen = <SettingsScreen themePref={themePref} onThemePref={setThemePref} />;
  }

  const navItems = [
    { value: "keys", label: "키보드", icon: I.keyboard({ size: 22 }) },
    { value: "macros", label: "매크로", icon: I.command({ size: 22 }) },
    { value: "settings", label: "설정", icon: I.settings({ size: 22 }) },
  ];

  return (
    <PhoneFrame theme={theme}>
      <StatusBar dark={theme === "dark"} />
      {screen}
      <div style={{ flex: "none", paddingBottom: 6 }}>
        <TabBar items={navItems} value={remapOpen && tab === "keys" ? "keys" : tab}
          onChange={(v) => { setRemapOpen(false); setTab(v); }} />
        <div style={{ height: 5, width: 134, background: "var(--text-strong)", opacity: 0.85, borderRadius: 3, margin: "6px auto 0" }} />
      </div>
    </PhoneFrame>
  );
}
window.KiboApp = KiboApp;
