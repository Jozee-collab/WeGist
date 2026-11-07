import { ArrowUpRight, Flame, Gamepad2, Radio, Sparkles, Trophy } from "lucide-react";
import { friends, streams } from "../lib/demo-data";

export default function RightRail({ onNavigate, onWatch, coins }) {
  return (
    <aside className="right-rail" aria-label="Community highlights">
      <div className="rail-section"><div className="rail-heading"><span>HAPPENING NOW</span><button type="button" aria-label="See all live streams" onClick={() => onNavigate("live")}><ArrowUpRight size={15} /></button></div>{streams.slice(0, 2).map((stream) => <button className="rail-live-item" key={stream.id} type="button" onClick={() => { onNavigate("live"); onWatch(stream.id); }}><span className={`rail-live-art rail-art-${stream.tint}`} style={{ backgroundImage: `url(${stream.image})` }}><i /></span><span className="rail-live-copy"><strong>{stream.streamer}</strong><small>{stream.game}</small><em><Radio size={11} /> {stream.viewers} watching</em></span></button>)}<button className="rail-text-link" type="button" onClick={() => onNavigate("live")}>Browse all streams <span>→</span></button></div>
      <div className="rail-section friends-rail-section"><div className="rail-heading"><span>YOUR CREW</span><button type="button" aria-label="See your friends" onClick={() => onNavigate("chats")}><ArrowUpRight size={15} /></button></div><div className="rail-friends">{friends.map((friend) => <button className="rail-friend" type="button" key={friend.id} onClick={() => onNavigate("chats")}><span className={`avatar avatar-${friend.tint}`}>{friend.avatar}</span><span><strong>{friend.name.split(" ")[0]}</strong><small>{friend.presence}</small></span><i className={`presence-dot ${friend.presence === "Away" ? "away" : ""}`} /></button>)}</div><button className="rail-text-link" type="button" onClick={() => onNavigate("chats")}>Open messages <span>→</span></button></div>
      <div className="rail-quest-card"><span className="quest-flare"><Flame size={17} /></span><span className="quest-label">WEEKLY QUEST</span><strong>Play 3 different games</strong><div className="quest-progress"><span><i /></span><small>2 / 3</small></div><button type="button" onClick={() => onNavigate("arcade")}>Keep playing <span>→</span></button></div>
      <div className="rail-balance"><span className="balance-symbol"><Trophy size={16} /></span><span><small>YOUR ARCADE WALLET</small><strong>{coins.toLocaleString()} <i>coins</i></strong></span><button type="button" onClick={() => onNavigate("arcade")} aria-label="Open arcade"><Gamepad2 size={16} /></button></div>
      <div className="rail-footer"><Sparkles size={13} /> PLAY NICE. PLAY TOGETHER.</div>
    </aside>
  );
}
