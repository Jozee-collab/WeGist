"use client";

import { useMemo, useState } from "react";
import { Bell, ChevronDown, Gamepad2, MessageCircle, Radio, Search, UserRound, UsersRound } from "lucide-react";
import AppSidebar from "./app-sidebar";
import ChatView from "./chat-view";
import FeedView from "./feed-view";
import LiveView from "./live-view";
import ArcadeView from "./arcade-view";
import ProfileView from "./profile-view";
import RightRail from "./right-rail";
import { conversations, initialMessages, initialPosts, streams } from "../lib/demo-data";

const sectionNames = { chats: "Messages", feed: "Community", live: "Live now", arcade: "Arcade", profile: "Profile" };

export default function WeGistApp() {
  const [activeSection, setActiveSection] = useState("chats");
  const [activeChat, setActiveChat] = useState("alice");
  const [messages, setMessages] = useState(initialMessages);
  const [posts, setPosts] = useState(initialPosts);
  const [activeStreams, setActiveStreams] = useState(streams);
  const [globalSearch, setGlobalSearch] = useState("");
  const [username, setUsername] = useState("WeGamer_Me");
  const [coins, setCoins] = useState(1200);
  const [isDark, setIsDark] = useState(true);
  const [watchedStream, setWatchedStream] = useState(null);

  const unreadMessages = useMemo(() => conversations.reduce((count, chat) => count + chat.unread, 0), []);

  function sendMessage(chatId, text) {
    setMessages((current) => ({
      ...current,
      [chatId]: [...(current[chatId] || []), { id: crypto.randomUUID(), author: "You", text, time: new Intl.DateTimeFormat("en", { hour: "numeric", minute: "2-digit" }).format(new Date()) }]
    }));
  }

  function createPost(text) {
    setPosts((current) => [{ id: crypto.randomUUID(), author: username, handle: "@wegamer", avatar: "W", tint: "mint", time: "Just now", text, likes: 0, comments: 0, liked: false, tag: "Community" }, ...current]);
  }

  function toggleLike(postId) {
    setPosts((current) => current.map((post) => post.id === postId ? { ...post, liked: !post.liked } : post));
  }

  function createStream({ title, game }) {
    const stream = { id: crypto.randomUUID(), streamer: username, handle: "@wegamer", title, game, viewers: "1", avatar: "W", tint: "mint", image: game === "Brick Breaker" ? "/images/brick-breaker.jpg" : "/images/space-shooter.jpg", category: "Your stream" };
    setActiveStreams((current) => [stream, ...current]);
  }

  function searchSubmit(event) {
    event.preventDefault();
    if (globalSearch.trim()) {
      setActiveSection("chats");
      setActiveChat(conversations.find((chat) => chat.name.toLowerCase().includes(globalSearch.toLowerCase()))?.id || "alice");
    }
  }

  return (
    <div className={`app-frame ${isDark ? "theme-dark" : "theme-light"}`}>
      <AppSidebar activeSection={activeSection} onNavigate={setActiveSection} username={username} />
      <div className="app-main">
        <header className="topbar">
          <div className="breadcrumbs"><span>YOUR SPACE</span><i>/</i><strong>{sectionNames[activeSection]}</strong><ChevronDown size={14} /></div>
          <div className="topbar-actions">
            <form className="global-search" onSubmit={searchSubmit}><Search size={16} /><input aria-label="Search across WeGist" placeholder="Search players, games..." value={globalSearch} onChange={(event) => setGlobalSearch(event.target.value)} /><kbd>⌘ K</kbd></form>
            <button className="topbar-icon notification-button" type="button" aria-label={`${unreadMessages} unread messages`} onClick={() => setActiveSection("chats")}><Bell size={18} /><i /></button>
            <button className="topbar-profile" type="button" onClick={() => setActiveSection("profile")}><span className="avatar avatar-user">W</span><ChevronDown size={14} /></button>
          </div>
        </header>
        <div className={`content-grid ${activeSection === "chats" ? "messages-content-grid" : ""}`}>
          <main className={`workspace-view view-${activeSection}`}>
            {activeSection === "chats" && <ChatView selectedId={activeChat} onSelect={setActiveChat} messageMap={messages} onSend={sendMessage} searchQuery={globalSearch} />}
            {activeSection === "feed" && <FeedView posts={posts} onCreatePost={createPost} onToggleLike={toggleLike} />}
            {activeSection === "live" && <LiveView streams={activeStreams} onGoLive={createStream} requestedStream={watchedStream} />}
            {activeSection === "arcade" && <ArcadeView coins={coins} onReward={(amount) => setCoins((current) => current + amount)} />}
            {activeSection === "profile" && <ProfileView username={username} onUsernameChange={setUsername} coins={coins} onToggleTheme={setIsDark} isDark={isDark} />}
          </main>
          <RightRail onNavigate={setActiveSection} onWatch={(streamId) => setWatchedStream({ streamId })} coins={coins} />
        </div>
      </div>
      <nav className="mobile-nav" aria-label="Mobile navigation">
        {[{ id: "chats", label: "Messages", icon: MessageCircle }, { id: "feed", label: "Community", icon: UsersRound }, { id: "live", label: "Live", icon: Radio }, { id: "arcade", label: "Arcade", icon: Gamepad2 }, { id: "profile", label: "Profile", icon: UserRound }].map(({ id, label, icon: Icon }) => <button key={id} type="button" className={activeSection === id ? "active" : ""} onClick={() => setActiveSection(id)} aria-current={activeSection === id ? "page" : undefined}><Icon size={19} strokeWidth={1.8} aria-hidden="true" /><small>{label}</small></button>)}
      </nav>
    </div>
  );
}
