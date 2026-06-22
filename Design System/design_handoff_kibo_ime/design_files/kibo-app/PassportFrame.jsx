/* Kibo — BlackBerry Passport-style device frame: a 1:1 square display with a
   physical hardware keyboard below it. The on-screen content shows only the app
   + Kibo's software toolbar; letter keys are hardware. window.PassportFrame */

function HardwareKeyboard() {
  const rows = [
    ["Q","W","E","R","T","Y","U","I","O","P"],
    ["A","S","D","F","G","H","J","K","L"],
    ["⇧","Z","X","C","V","B","N","M","⌫"],
  ];
  const fret = <div style={{ height: 1, background: "linear-gradient(to right, transparent, #5a5a5e, transparent)", margin: "7px 0", opacity: 0.7 }} />;
  return (
    <div style={{ padding: "10px 0 4px" }}>
      {rows.map((r, i) => (
        <React.Fragment key={i}>
          {i > 0 ? fret : null}
          <div style={{ display: "flex", gap: 5, padding: i === 1 ? "0 24px" : "0 2px" }}>
            {r.map((k) => (
              <div key={k} style={{
                flex: "1 1 0", height: 32, borderRadius: 5, minWidth: 0,
                background: "linear-gradient(#3c3c41, #2a2a2e)",
                color: "#e8e8ea", display: "inline-flex", alignItems: "center", justifyContent: "center",
                fontSize: 13, fontWeight: 600,
                boxShadow: "0 1px 1px rgba(0,0,0,0.6), inset 0 1px 0 rgba(255,255,255,0.07)",
              }}>{k}</div>
            ))}
          </div>
        </React.Fragment>
      ))}
    </div>
  );
}

function PassportFrame({ theme = "light", children }) {
  return (
    <div data-theme={theme} style={{
      width: 500, flex: "none",
      background: "linear-gradient(#141416, #0c0c0e)",
      borderRadius: 24, padding: "13px 16px 14px",
      boxShadow: "0 40px 100px -30px rgba(16,15,12,0.55), 0 0 0 1px rgba(0,0,0,0.5)",
    }}>
      {/* top bezel — earpiece + camera */}
      <div style={{ height: 28, display: "flex", alignItems: "center", justifyContent: "center", gap: 12 }}>
        <span style={{ width: 7, height: 7, borderRadius: "50%", background: "#26262a" }} />
        <span style={{ width: 46, height: 5, borderRadius: 3, background: "#26262a" }} />
      </div>

      {/* 1:1 square display */}
      <div style={{
        width: 468, height: 468, background: "var(--bg)", borderRadius: 5, overflow: "hidden",
        display: "flex", flexDirection: "column", position: "relative",
        boxShadow: "inset 0 0 0 1px rgba(0,0,0,0.25)",
      }}>
        {children}
      </div>

      {/* physical keyboard */}
      <HardwareKeyboard />

      {/* chin / logo */}
      <div style={{ height: 14, display: "flex", alignItems: "center", justifyContent: "center" }}>
        <span style={{ fontSize: 10, fontWeight: 800, letterSpacing: "0.18em", color: "#3a3a3e" }}>KIBO</span>
      </div>
    </div>
  );
}

window.HardwareKeyboard = HardwareKeyboard;
window.PassportFrame = PassportFrame;
