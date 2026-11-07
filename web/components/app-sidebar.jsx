import { Gamepad2, MessageCircle, Radio, UserRound, UsersRound, Zap, CircleHelp } from "lucide-react";

const primaryNav = [
  { id: "chats", label: "Messages", icon: MessageCircle, badge: "2" },
  { id: "feed", label: "Community", icon: UsersRound },
  { id: "live", label: "Live now", icon: Radio, live: true },
  { id: "arcade", label: "Arcade", icon: Gamepad2 },
  { id: "profile", label: "My profile", icon: UserRound }
];

export default function AppSidebar({ activeSection, onNavigate, username }) {
  return (
    <aside className="sidebar">
      <a className="brand-lockup" href="#home" onClick={(event) => { event.preventDefault(); onNavigate("chats"); }} aria-label="WeGist home">
        <span className="brand-mark"><MessageCircle size={22} strokeWidth={2.5} /><Zap className="brand-zap" size={12} fill="currentColor" /></span>
        <span className="brand-name">wegist<span>.</span></span>
      </a>
      <div className="sidebar-label">YOUR SPACE</div>
      <nav className="primary-nav" aria-label="Main navigation">
        {primaryNav.map(({ id, label, icon: Icon, badge, live }) => (
          <button key={id} type="button" className={`nav-link ${activeSection === id ? "active" : ""}`} onClick={() => onNavigate(id)} aria-current={activeSection === id ? "page" : undefined}>
            <Icon size={19} strokeWidth={1.8} />
            <span>{label}</span>
            {badge && <span className="nav-badge">{badge}</span>}
            {live && <span className="nav-live-dot" aria-label="Live" />}
          </button>
        ))}
      </nav>
      <div className="sidebar-divider" />
      <button className="nav-link help-link" type="button" onClick={() => onNavigate("profile")}><CircleHelp size={19} strokeWidth={1.8} /><span>Help & feedback</span></button>
      <div className="sidebar-spacer" />
      <div className="sidebar-status"><span className="online-dot" /> All systems online</div>
      <button className="sidebar-user" type="button" onClick={() => onNavigate("profile")}>
        <span className="avatar avatar-user">W</span>
        <span className="sidebar-user-copy"><strong>{username}</strong><small>Level 8 · Player</small></span>
        <span className="user-menu-dots">···</span>
      </button>
      <div className="sidebar-footnote">WEGIST WEB · DEMO</div>
    </aside>
  );
}
