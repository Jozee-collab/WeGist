"use client";

import { useEffect, useState } from "react";
import { Eye, Gamepad2, MessageCircle, Radio, Send, Sparkles, UsersRound, X } from "lucide-react";

function Avatar({ stream }) {
  return <span className={`avatar avatar-${stream.tint}`} aria-hidden="true">{stream.avatar}</span>;
}

export default function LiveView({ streams, onGoLive, requestedStream }) {
  const [selectedStream, setSelectedStream] = useState(null);
  const [showGoLive, setShowGoLive] = useState(false);
  const [title, setTitle] = useState("Arcade high-score run");
  const [game, setGame] = useState("Space Shooter");
  const [chat, setChat] = useState("");
  const [chatLines, setChatLines] = useState(["SpaceQueen: that dodge was clean!", "retro_amy: let’s goooo", "Bob_Destroyer: nice run"]);

  useEffect(() => {
    if (!requestedStream) return;
    const stream = streams.find((item) => item.id === requestedStream.streamId);
    if (stream) setSelectedStream(stream);
  }, [requestedStream, streams]);

  function startStream(event) {
    event.preventDefault();
    onGoLive({ title: title.trim() || "Going live now", game });
    setShowGoLive(false);
  }

  function sendChat(event) {
    event.preventDefault();
    if (!chat.trim()) return;
    setChatLines((lines) => [...lines, `You: ${chat.trim()}`]);
    setChat("");
  }

  return (
    <section className="live-page">
      <div className="section-heading live-heading"><div><p className="eyebrow">WATCH TOGETHER</p><h1>Live streams</h1><p className="section-subtitle">Pull up a seat. Your crew is already here.</p></div><button className="live-button" type="button" onClick={() => setShowGoLive(true)}><Radio size={16} /> Go live</button></div>
      <button className="live-hero" type="button" onClick={() => setSelectedStream(streams[0])}>
        <span className="live-hero-art" style={{ backgroundImage: `linear-gradient(90deg, rgba(10,13,19,.96) 2%, rgba(10,13,19,.66) 51%, rgba(10,13,19,.12)), url(${streams[1]?.image || "/images/space-shooter.jpg"})` }} />
        <span className="live-hero-content"><span className="live-kicker"><i /> FEATURED ARENA</span><strong>There’s always<br />one more round.</strong><small>Join the watch party and make it a night.</small><span className="hero-watch">Browse live <span>↗</span></span></span><span className="hero-viewer-count"><UsersRound size={14} /> 2,310 watching</span>
      </button>
      <div className="live-list-heading"><div><h2>Live right now <span className="live-count">{streams.length}</span></h2><p>Creators you follow are in the arena</p></div><button type="button">All streams <span>→</span></button></div>
      <div className="stream-grid">
        {streams.map((stream) => <article className="stream-card" key={stream.id}>
          <button className="stream-thumbnail" type="button" onClick={() => setSelectedStream(stream)} style={{ backgroundImage: `linear-gradient(180deg, rgba(0,0,0,.06), rgba(0,0,0,.66)), url(${stream.image})` }} aria-label={`Watch ${stream.streamer}: ${stream.title}`}><span className="live-chip"><i /> LIVE</span><span className="viewer-chip"><Eye size={13} /> {stream.viewers}</span><span className="stream-game-tag"><Gamepad2 size={13} /> {stream.game}</span><span className="stream-play">▶</span></button>
          <div className="stream-details"><Avatar stream={stream} /><div className="stream-copy"><strong>{stream.title}</strong><span>{stream.streamer} <i>·</i> {stream.category}</span></div><button className="icon-button stream-more" type="button" aria-label={`More from ${stream.streamer}`}><span>···</span></button></div>
        </article>)}
      </div>
      <div className="stream-squad-card"><div className="squad-icon"><Sparkles size={18} /></div><div><strong>Your next watch party starts here</strong><p>Invite a friend from Messages and catch a live run together.</p></div><button type="button" onClick={() => window.alert("Open Messages to invite your crew.")}>Invite a friend <span>↗</span></button></div>
      {showGoLive && <div className="modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) setShowGoLive(false); }}><form className="modal-card go-live-modal" role="dialog" aria-modal="true" aria-labelledby="go-live-title" onSubmit={startStream}><button className="modal-close" type="button" aria-label="Close" onClick={() => setShowGoLive(false)}><X size={18} /></button><span className="modal-icon live-modal-icon"><Radio size={22} /></span><p className="eyebrow">START A WATCH PARTY</p><h2 id="go-live-title">Your stream, your rules.</h2><p className="modal-intro">Set the scene and let your friends know you’re live.</p><label className="form-label">Stream title<input value={title} onChange={(event) => setTitle(event.target.value)} maxLength={80} required /></label><label className="form-label">Choose a game<select value={game} onChange={(event) => setGame(event.target.value)}><option>Space Shooter</option><option>Brick Breaker</option></select></label><button className="live-button modal-submit" type="submit"><Radio size={16} /> Start stream</button></form></div>}
      {selectedStream && <div className="modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) setSelectedStream(null); }}><div className="watch-modal" role="dialog" aria-modal="true" aria-label={`Watching ${selectedStream.streamer}`}><div className="watch-screen" style={{ backgroundImage: `linear-gradient(0deg, rgba(0,0,0,.5), transparent), url(${selectedStream.image})` }}><button className="modal-close watch-close" type="button" aria-label="Close stream" onClick={() => setSelectedStream(null)}><X size={18} /></button><span className="live-chip"><i /> LIVE</span><span className="watch-overlay-title">{selectedStream.title}</span></div><div className="watch-info"><Avatar stream={selectedStream} /><div><strong>{selectedStream.streamer}</strong><small><Eye size={13} /> {selectedStream.viewers} watching · {selectedStream.game}</small></div><button className="watch-follow" type="button">Follow</button></div><div className="watch-chat"><div className="watch-chat-title"><MessageCircle size={15} /> Stream chat <span>Live</span></div><div className="watch-chat-lines" aria-live="polite">{chatLines.map((line, index) => <p key={index}>{line}</p>)}</div><form className="watch-chat-form" onSubmit={sendChat}><input aria-label="Send a stream message" value={chat} onChange={(event) => setChat(event.target.value)} placeholder="Say something nice..." /><button type="submit" aria-label="Send chat message"><Send size={15} /></button></form></div></div></div>}
    </section>
  );
}
