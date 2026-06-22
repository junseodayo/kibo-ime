/* Kibo app — phone frame, status bar, screen header. Shared via window. */

function StatusBar({ dark }) {
  return (
    <div style={{
      height: 44, display: "flex", alignItems: "center", justifyContent: "space-between",
      padding: "0 26px 0 30px", fontSize: 14, fontWeight: 600,
      color: "var(--text-strong)", flex: "none",
    }}>
      <span style={{ letterSpacing: "-0.01em" }}>9:41</span>
      <div style={{ display: "flex", alignItems: "center", gap: 6 }}>
        <svg width="17" height="11" viewBox="0 0 17 11" fill="currentColor"><rect x="0" y="6" width="3" height="5" rx="1"/><rect x="4.5" y="4" width="3" height="7" rx="1"/><rect x="9" y="2" width="3" height="9" rx="1"/><rect x="13.5" y="0" width="3" height="11" rx="1"/></svg>
        <svg width="16" height="11" viewBox="0 0 16 11" fill="currentColor"><path d="M8 2.2C10 2.2 11.8 3 13.1 4.3l1.2-1.3C12.7 1.4 10.4.5 8 .5S3.3 1.4 1.7 3l1.2 1.3C4.2 3 6 2.2 8 2.2z" opacity="0.9"/><path d="M8 5.4c1.1 0 2.1.4 2.8 1.2l1.2-1.3C10.9 4.2 9.5 3.6 8 3.6s-2.9.6-4 1.7l1.2 1.3C5.9 5.8 6.9 5.4 8 5.4z"/><circle cx="8" cy="9" r="1.6"/></svg>
        <svg width="26" height="12" viewBox="0 0 26 12" fill="none"><rect x="0.5" y="0.5" width="22" height="11" rx="3" stroke="currentColor" opacity="0.4"/><rect x="2" y="2" width="18" height="8" rx="1.5" fill="currentColor"/><rect x="24" y="4" width="2" height="4" rx="1" fill="currentColor" opacity="0.5"/></svg>
      </div>
    </div>
  );
}

/* Large editorial screen header with optional back + trailing */
function ScreenHeader({ title, subtitle, onBack, trailing }) {
  return (
    <div style={{ padding: "8px 22px 14px" }}>
      {onBack ? (
        <button onClick={onBack} aria-label="뒤로" style={{
          border: "none", background: "transparent", color: "var(--text-strong)",
          cursor: "pointer", padding: "6px 0", marginBottom: 4, display: "inline-flex",
        }}>
          {window.KiboIcons.back({ size: 24 })}
        </button>
      ) : null}
      <div style={{ display: "flex", alignItems: "flex-start", justifyContent: "space-between", gap: 12 }}>
        <h1 style={{ fontSize: 30, fontWeight: 800, letterSpacing: "-0.03em", color: "var(--text-strong)", lineHeight: 1.1, margin: 0, flexShrink: 0, whiteSpace: "nowrap" }}>{title}</h1>
        {trailing ? <div style={{ flexShrink: 0, marginTop: 4 }}>{trailing}</div> : null}
      </div>
      {subtitle ? <p style={{ margin: "6px 0 0", fontSize: 14, color: "var(--text-muted)" }}>{subtitle}</p> : null}
    </div>
  );
}

/* Section label (overline) */
function SectionLabel({ children, style }) {
  return <div style={{ fontSize: 12, fontWeight: 600, letterSpacing: "0.1em", textTransform: "uppercase", color: "var(--text-subtle)", padding: "0 4px 10px", ...style }}>{children}</div>;
}

/* The device shell. theme: 'light' | 'dark'. children = screen content; tabbar rendered separately. */
function PhoneFrame({ theme = "light", children }) {
  return (
    <div data-theme={theme} style={{
      width: 402, height: 854, flex: "none",
      borderRadius: 52, padding: 12,
      background: theme === "dark" ? "#000" : "#0c0b08",
      boxShadow: "0 40px 100px -30px rgba(16,15,12,0.55), 0 0 0 1px rgba(0,0,0,0.4)",
      position: "relative",
    }}>
      <div style={{
        width: "100%", height: "100%", borderRadius: 40, overflow: "hidden",
        background: "var(--bg)", display: "flex", flexDirection: "column", position: "relative",
      }}>
        {/* Dynamic island */}
        <div style={{ position: "absolute", top: 12, left: "50%", transform: "translateX(-50%)", width: 110, height: 32, background: "#000", borderRadius: 20, zIndex: 30 }} />
        {children}
      </div>
    </div>
  );
}

Object.assign(window, { StatusBar, ScreenHeader, SectionLabel, PhoneFrame });
