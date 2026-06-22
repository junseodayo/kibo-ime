/* Kibo app — inline Lucide-style icons (2px stroke, round caps). Shared via window.KiboIcons. */
const KIcon = ({ d, size = 22, sw = 2, fill = "none" }) => (
  <svg width={size} height={size} viewBox="0 0 24 24" fill={fill} stroke="currentColor"
    strokeWidth={sw} strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
    {d}
  </svg>
);

const KiboIcons = {
  keyboard: (p) => <KIcon {...p} d={<><rect x="2" y="5" width="20" height="14" rx="2"/><path d="M6 9h0M10 9h0M14 9h0M18 9h0M6 13h0M18 13h0M9 13h6"/></>} />,
  command: (p) => <KIcon {...p} d={<path d="M15 6a3 3 0 1 0 3 3h-3V6zm-6 0a3 3 0 1 1-3 3h3V6zm0 12a3 3 0 1 0-3-3h3v3zm6 0a3 3 0 1 1 3-3h-3v3zM9 9h6v6H9z"/>} />,
  layers: (p) => <KIcon {...p} d={<><path d="m12 2 9 5-9 5-9-5 9-5z"/><path d="m3 12 9 5 9-5M3 17l9 5 9-5"/></>} />,
  languages: (p) => <KIcon {...p} d={<><path d="m5 8 6 6M4 14l6-6 2-3M2 5h12M7 2h1"/><path d="m22 22-5-10-5 10M14 18h6"/></>} />,
  settings: (p) => <KIcon {...p} d={<><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/></>} />,
  sliders: (p) => <KIcon {...p} d={<path d="M4 21v-7M4 10V3M12 21v-9M12 8V3M20 21v-5M20 12V3M1 14h6M9 8h6M17 16h6"/>} />,
  bluetooth: (p) => <KIcon {...p} d={<path d="m7 7 10 10-5 5V2l5 5L7 17"/>} />,
  zap: (p) => <KIcon {...p} d={<path d="M13 2 3 14h9l-1 8 10-12h-9l1-8z"/>} />,
  volume: (p) => <KIcon {...p} d={<><path d="M11 5 6 9H2v6h4l5 4V5z"/><path d="M15.5 8.5a5 5 0 0 1 0 7"/></>} />,
  moon: (p) => <KIcon {...p} d={<path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9z"/>} />,
  check: (p) => <KIcon {...p} d={<path d="M20 6 9 17l-5-5"/>} />,
  plus: (p) => <KIcon {...p} d={<path d="M12 5v14M5 12h14"/>} />,
  search: (p) => <KIcon {...p} d={<><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></>} />,
  trash: (p) => <KIcon {...p} d={<path d="M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>} />,
  chevron: (p) => <KIcon {...p} d={<path d="m9 18 6-6-6-6"/>} />,
  back: (p) => <KIcon {...p} d={<path d="m12 19-7-7 7-7M19 12H5"/>} />,
  info: (p) => <KIcon {...p} d={<><circle cx="12" cy="12" r="10"/><path d="M12 16v-4M12 8h.01"/></>} />,
  bell: (p) => <KIcon {...p} d={<path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9M10.3 21a1.94 1.94 0 0 0 3.4 0"/>} />,
  grid: (p) => <KIcon {...p} d={<><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/></>} />,
  pencil: (p) => <KIcon {...p} d={<path d="M12 20h9M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4 12.5-12.5z"/>} />,
  arrowRight: (p) => <KIcon {...p} d={<path d="M5 12h14M12 5l7 7-7 7"/>} />,
  globe: (p) => <KIcon {...p} d={<><circle cx="12" cy="12" r="10"/><path d="M2 12h20M12 2a15 15 0 0 1 0 20 15 15 0 0 1 0-20"/></>} />,
  x: (p) => <KIcon {...p} d={<path d="M18 6 6 18M6 6l12 12"/>} />,
  vibrate: (p) => <KIcon {...p} d={<path d="m2 8 2 2-2 2 2 2M22 8l-2 2 2 2-2 2M8 5h8a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2z"/>} />,
  clipboard: (p) => <KIcon {...p} d={<><rect x="8" y="2" width="8" height="4" rx="1"/><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/></>} />,
  smile: (p) => <KIcon {...p} d={<><circle cx="12" cy="12" r="10"/><path d="M8 14s1.5 2 4 2 4-2 4-2M9 9h.01M15 9h.01"/></>} />,
  hash: (p) => <KIcon {...p} d={<path d="M4 9h16M4 15h16M10 3 8 21M16 3l-2 18"/>} />,
  keyboardOff: (p) => <KIcon {...p} d={<><path d="M 20 4 A2 2 0 0 1 22 6 v10 M 2 6 a2 2 0 0 1 2-2 h12 M6 9h0M10 9h0M2 2l20 20M11 13h1M14 9h0"/></>} />,
  pin: (p) => <KIcon {...p} d={<path d="M12 17v5M9 10.76V8a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v2.76a2 2 0 0 0 .5 1.32l1.94 2.2A1 1 0 0 1 16.69 16H7.31a1 1 0 0 1-.75-1.72l1.94-2.2a2 2 0 0 0 .5-1.32z"/>} />,
  drag: (p) => <KIcon {...p} d={<path d="M9 5h.01M9 12h.01M9 19h.01M15 5h.01M15 12h.01M15 19h.01"/>} />,
  book: (p) => <KIcon {...p} d={<path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>} />,
  palette: (p) => <KIcon {...p} d={<><circle cx="13.5" cy="6.5" r=".5"/><circle cx="17.5" cy="10.5" r=".5"/><circle cx="8.5" cy="7.5" r=".5"/><circle cx="6.5" cy="12.5" r=".5"/><path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10c.926 0 1.648-.746 1.648-1.688 0-.437-.18-.835-.437-1.125-.29-.289-.438-.652-.438-1.125a1.64 1.64 0 0 1 1.668-1.668h1.996c3.051 0 5.555-2.503 5.555-5.554C21.965 6.012 17.461 2 12 2z"/></>} />,
};

window.KiboIcons = KiboIcons;
