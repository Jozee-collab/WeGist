"use client";

import { useMemo, useState } from "react";
import { ArrowLeft, Check, CheckCheck, Gamepad2, MoreHorizontal, Paperclip, Search, Send, Smile, Video, UserPlus } from "lucide-react";
import { conversations, friends, friendRequests } from "../lib/demo-data";

function Avatar({ person, size = "" }) {
  return <span className={`avatar avatar-${person.tint} ${size ? `avatar-${size}` : ""}`} aria-hidden="true">{person.avatar}</span>;
}

export default function ChatView({ selectedId, onSelect, messageMap, onSend, searchQuery }) {
  const [chatTab, setChatTab] = useState("chats");
  const [subQuery, setSubQuery] = useState("");
  const [friendState, setFriendState] = useState({});
  const [draft, setDraft] = useState("");
  const activeChat = conversations.find((chat) => chat.id === selectedId) || conversations[0];
  const visibleChats = useMemo(() => conversations.filter((chat) => `${chat.name} ${chat.preview} ${chat.game}`.toLowerCase().includes(`${searchQuery} ${subQuery}`.trim().toLowerCase())), [searchQuery, subQuery]);
  const visibleFriends = friends.filter((friend) => friend.name.toLowerCase().includes(subQuery.toLowerCase()));
  const messages = messageMap[selectedId] || [];

  function submitMessage(event) {
    event.preventDefault();
    if (!draft.trim()) return;
    onSend(selectedId, draft.trim());
    setDraft("");
  }

  function handleComposerKeyDown(event) {
    if (event.key === "Enter" && !event.shiftKey) {
      if (event.nativeEvent.isComposing || event.keyCode === 229) return;
      event.preventDefault();
      event.currentTarget.form?.requestSubmit();
    }
  }

  return (
    <section className="chat-layout" aria-label="Messages workspace">
      <div className={`conversation-list-panel ${selectedId ? "thread-open" : ""}`}>
        <div className="section-heading chat-list-heading">
          <div><p className="eyebrow">YOUR SPACE</p><h1>Messages</h1></div>
          <button className="icon-button outlined" type="button" aria-label="Start a new chat"><UserPlus size={18} /></button>
        </div>
        <div className="segment-tabs" role="tablist" aria-label="Messages sections">
          <button type="button" role="tab" aria-selected={chatTab === "chats"} className={chatTab === "chats" ? "selected" : ""} onClick={() => setChatTab("chats")}>Chats <span className="tab-count">3</span></button>
          <button type="button" role="tab" aria-selected={chatTab === "friends"} className={chatTab === "friends" ? "selected" : ""} onClick={() => setChatTab("friends")}>Friends <span className="tab-count">2</span></button>
        </div>
        <label className="search-field chat-search"><Search size={16} /><input aria-label="Search chats and friends" placeholder="Search chats" value={subQuery} onChange={(event) => setSubQuery(event.target.value)} /></label>
        {chatTab === "chats" ? (
          <div className="conversation-list">
            <p className="list-section-label">RECENT</p>
            {visibleChats.map((chat) => (
              <button key={chat.id} type="button" className={`conversation-row ${chat.id === selectedId ? "selected" : ""}`} onClick={() => onSelect(chat.id)} aria-pressed={chat.id === selectedId}>
                <span className="avatar-wrap"><Avatar person={chat} /><span className={`presence-dot ${chat.presence === "Away" ? "away" : ""}`} /></span>
                <span className="conversation-copy"><span className="conversation-title"><strong>{chat.name}</strong><time>{chat.time}</time></span><span className="conversation-preview">{chat.preview}</span><span className="conversation-game"><Gamepad2 size={12} /> {chat.game}</span></span>
                {chat.unread > 0 && <span className="unread-count">{chat.unread}</span>}
              </button>
            ))}
            <div className="friends-nudge"><span className="nudge-icon"><Gamepad2 size={16} /></span><span><strong>Find your next teammate</strong><small>Invite a friend to play</small></span><button type="button" aria-label="View friends" onClick={() => setChatTab("friends")}>→</button></div>
          </div>
        ) : (
          <div className="conversation-list friend-list-panel">
            <p className="list-section-label">FRIEND REQUESTS · {friendRequests.filter((request) => friendState[request.id] !== "accepted" && friendState[request.id] !== "declined").length}</p>
            {friendRequests.filter((request) => friendState[request.id] !== "accepted" && friendState[request.id] !== "declined").map((request) => (
              <div className="request-row" key={request.id}><Avatar person={request} /><span className="request-copy"><strong>{request.name}</strong><small>{request.note}</small></span><button className="small-primary" type="button" onClick={() => setFriendState((state) => ({ ...state, [request.id]: "accepted" }))}>Accept</button><button className="small-icon" type="button" aria-label={`Decline ${request.name}`} onClick={() => setFriendState((state) => ({ ...state, [request.id]: "declined" }))}>×</button></div>
            ))}
            <p className="list-section-label friends-label">YOUR FRIENDS</p>
            {visibleFriends.map((friend) => <button key={friend.id} className="friend-row" type="button" onClick={() => { onSelect(friend.id === "dave" ? "alice" : friend.id); setChatTab("chats"); }}><Avatar person={friend} /><span className="request-copy"><strong>{friend.name}</strong><small>{friend.presence} · #{friend.rank} global</small></span><span className={`presence-dot ${friend.presence === "Away" ? "away" : ""}`} /></button>)}
          </div>
        )}
        <div className="list-foot"><span className="online-dot" /> 3 friends online</div>
      </div>

      <div className={`thread-panel ${selectedId ? "thread-open" : ""}`}>
        <header className="thread-header">
          <button className="icon-button back-thread" type="button" aria-label="Back to conversations" onClick={() => onSelect(null)}><ArrowLeft size={18} /></button>
          <Avatar person={activeChat} />
          <div className="thread-person"><strong>{activeChat.name}</strong><span><i className="presence-dot" /> {activeChat.presence}</span></div>
          <div className="thread-actions"><button className="icon-button" aria-label="Start voice call" type="button"><Video size={18} /></button><button className="icon-button" aria-label="More conversation options" type="button"><MoreHorizontal size={20} /></button></div>
        </header>
        <div className="thread-context"><span className="context-game"><Gamepad2 size={14} /> {activeChat.game}</span><span>·</span><span>Friends since last week</span><button type="button" onClick={() => setDraft("Want to play " + activeChat.game + "? ")}>Invite to play <span>↗</span></button></div>
        <div className="message-timeline" aria-live="polite">
          <div className="day-divider"><span /> TODAY <span /></div>
          {messages.map((message) => (
            <div key={message.id} className={`message-line ${message.author === "You" ? "from-me" : "from-them"}`}>
              {message.author !== "You" && <Avatar person={activeChat} size="small" />}
              <div className="message-stack">{message.author !== "You" && <span className="message-author">{message.author}</span>}{message.game && <button className="challenge-card" type="button" onClick={() => setDraft("I’m in! Let’s play " + activeChat.game + ".")}><span className="challenge-art"><Gamepad2 size={21} /></span><span><strong>Game challenge</strong><small>{activeChat.game} · Your turn</small></span><span className="challenge-play">Play</span></button>}{!message.game && <div className="message-bubble">{message.text}</div>}<span className="message-meta">{message.time} {message.author === "You" && <CheckCheck size={13} />}</span></div>
            </div>
          ))}
          <div className="typing-indicator"><span className="typing-dots"><i /><i /><i /></span> Alice is typing</div>
        </div>
        <form className="composer" onSubmit={submitMessage}>
          <button className="icon-button composer-action" type="button" aria-label="Attach a file"><Paperclip size={18} /></button>
          <input aria-label="Write a message" placeholder={`Message ${activeChat.name.split(" ")[0]}...`} value={draft} onChange={(event) => setDraft(event.target.value)} onKeyDown={handleComposerKeyDown} />
          <button className="icon-button composer-action emoji-button" type="button" aria-label="Add a reaction" onClick={() => setDraft((value) => value + " ✨")}><Smile size={18} /></button>
          <button className="send-button" type="submit" aria-label="Send message" disabled={!draft.trim()}><Send size={17} /></button>
        </form>
        <div className="composer-hint"><Check size={12} /> End-to-end encrypted <span>·</span> Enter to send</div>
      </div>
    </section>
  );
}
