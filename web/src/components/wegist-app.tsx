"use client"

import Image from "next/image"
import { useMemo, useState } from "react"
import {
  ArrowUpRight,
  Bell,
  ChevronRight,
  Gamepad2,
  Headphones,
  Radio,
  Search,
  Sparkles,
  Trophy,
  Users,
  X,
} from "lucide-react"
import { AppSidebar, MobileNavigation } from "@/components/app-navigation"
import { AvatarPhoto } from "@/components/avatar-photo"
import { ChatView } from "@/components/chat-view"
import { ArcadeView, LiveView, ProfileView } from "@/components/discovery-views"
import { FeedView } from "@/components/feed-view"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import {
  avatarImage,
  initialConversations,
  initialPosts,
  initialStreams,
  onlineFriends,
  type FeedPost,
  type LiveStream,
  type Section,
} from "@/lib/demo-data"

const pageCopy: Record<Section, { kicker: string; title: string; description: string }> = {
  chats: { kicker: "GAMER REALM · WEDNESDAY, OCT 16", title: "Your squad, on tap.", description: "Good games start with good people." },
  feed: { kicker: "THE COMMUNITY FEED", title: "The good stuff, lately.", description: "Highlights, hot takes, and small wins from your circle." },
  live: { kicker: "LIVE FROM THE LOUNGE", title: "Pull up a stream.", description: "Watch the plays while they are happening." },
  arcade: { kicker: "GAMES FOR THE SQUAD", title: "One more round?", description: "Quick games. Big rivalries. Very questionable rematches." },
  profile: { kicker: "YOUR WEGIST PROFILE", title: "Your corner of the lounge.", description: "Your stats, your setup, your next milestone." },
}

export function WeGistApp() {
  const [activeSection, setActiveSection] = useState<Section>("chats")
  const [selectedConversationId, setSelectedConversationId] = useState("alice")
  const [conversations, setConversations] = useState(initialConversations)
  const [posts, setPosts] = useState(initialPosts)
  const [searchQuery, setSearchQuery] = useState("")
  const [coins, setCoins] = useState(1240)
  const [isDark, setIsDark] = useState(true)
  const [toast, setToast] = useState("")
  const [selectedStream, setSelectedStream] = useState<LiveStream | null>(null)
  const [selectedStory, setSelectedStory] = useState("")

  const page = pageCopy[activeSection]
  const filteredPosts = useMemo(() => {
    const query = searchQuery.trim().toLowerCase()
    if (!query) return posts
    return posts.filter((post) => `${post.author} ${post.handle} ${post.game} ${post.content}`.toLowerCase().includes(query))
  }, [posts, searchQuery])
  const filteredStreams = useMemo(() => {
    const query = searchQuery.trim().toLowerCase()
    if (!query) return initialStreams
    return initialStreams.filter((stream) => `${stream.streamer} ${stream.title} ${stream.game}`.toLowerCase().includes(query))
  }, [searchQuery])

  function notify(message: string) {
    setToast(message)
    window.setTimeout(() => setToast(""), 2600)
  }

  function createPost(content: string) {
    const newPost: FeedPost = {
      id: `post-${Date.now()}`,
      author: "Alex Rivers",
      handle: "@alexrivers",
      avatar: avatarImage,
      game: "Player update",
      time: "Just now",
      content,
      likes: 0,
      comments: 0,
      liked: false,
      following: true,
    }
    setPosts((current) => [newPost, ...current])
    setActiveSection("feed")
    notify("Your gist is live in the feed.")
  }

  function toggleLike(postId: string) {
    setPosts((current) => current.map((post) => post.id === postId ? { ...post, liked: !post.liked, likes: post.likes + (post.liked ? -1 : 1) } : post))
  }

  function addReply(postId: string) {
    setPosts((current) => current.map((post) => post.id === postId ? { ...post, comments: post.comments + 1 } : post))
    notify("Reply added to the conversation.")
  }

  function sendMessage(conversationId: string, text: string) {
    const time = new Intl.DateTimeFormat("en", { hour: "numeric", minute: "2-digit" }).format(new Date())
    setConversations((current) => current.map((conversation) => conversation.id === conversationId ? {
      ...conversation,
      time,
      lastMessage: text,
      messages: [...conversation.messages, { id: `message-${Date.now()}`, from: "me", text, time }],
    } : conversation))
  }

  function openFriendChat(friendId: string) {
    if (conversations.some((conversation) => conversation.id === friendId)) {
      setSelectedConversationId(friendId)
      setActiveSection("chats")
      return
    }
    notify("That friend is one invite away from your chats.")
  }

  function handleCheckIn() {
    setCoins((value) => value + 40)
    notify("Daily check-in claimed: +40 Gist coins.")
  }

  return (
    <div className={`app-shell${isDark ? "" : " theme-light"}`}>
      <AppSidebar activeSection={activeSection} onNavigate={setActiveSection} onCompose={() => setActiveSection("feed")} coins={coins} />

      <div className="main-shell">
        <header className="topbar">
          <div className="mobile-brand">
            <div className="brand-mark"><Image src="/images/wegist-mark.jpg" alt="" width={34} height={34} /></div>
            <span>WeGist</span>
          </div>
          <div className="topbar-heading">
            <p className="eyebrow">{page.kicker}</p>
            <h1>{page.title}</h1>
            <p className="topbar-description">{page.description}</p>
          </div>
          <div className="topbar-tools">
            <label className="global-search">
              <Search size={17} aria-hidden="true" />
              <input aria-label="Search WeGist" onChange={(event) => setSearchQuery(event.target.value)} placeholder="Search players, games..." value={searchQuery} />
              <kbd>⌘ K</kbd>
            </label>
            <Button variant="ghost" size="icon" className="topbar-bell" aria-label="Notifications" onClick={() => notify("You are all caught up.")}>
              <Bell aria-hidden="true" />
              <span className="notification-dot" />
            </Button>
            <span className="topbar-divider" aria-hidden="true" />
            <button className="topbar-profile" type="button" onClick={() => setActiveSection("profile")} aria-label="Open Alex Rivers profile">
              <AvatarPhoto name="Alex Rivers" src={avatarImage} size="default" online />
            </button>
          </div>
        </header>

        <main className="content-grid" id="main-content">
          <section className="main-column" aria-label={page.title}>
            {activeSection === "chats" ? (
              <ChatView
                conversations={conversations}
                selectedConversationId={selectedConversationId}
                searchQuery={searchQuery}
                onSelectConversation={setSelectedConversationId}
                onSendMessage={sendMessage}
                onNotify={notify}
              />
            ) : null}
            {activeSection === "feed" ? (
              <FeedView
                posts={filteredPosts}
                searchQuery={searchQuery}
                onCreatePost={createPost}
                onLikePost={toggleLike}
                onReply={addReply}
                onNotify={notify}
                onOpenStory={setSelectedStory}
              />
            ) : null}
            {activeSection === "live" ? (
              <LiveView
                streams={filteredStreams}
                searchQuery={searchQuery}
                onWatch={setSelectedStream}
                onStartBroadcast={() => notify("Broadcast setup is ready for your next stream.")}
              />
            ) : null}
            {activeSection === "arcade" ? (
              <ArcadeView
                coins={coins}
                onPlay={(gameName) => notify(`Finding a ${gameName} match for your squad.`)}
                onCheckIn={handleCheckIn}
              />
            ) : null}
            {activeSection === "profile" ? (
              <ProfileView
                coins={coins}
                isDark={isDark}
                onToggleTheme={() => setIsDark((value) => !value)}
                onNotify={notify}
              />
            ) : null}
          </section>

          <aside className="context-rail" aria-label="Squad activity">
            <Card className="rail-card live-rail-card">
              <CardHeader className="rail-card-heading">
                <div><p className="eyebrow">HAPPENING NOW</p><CardTitle>Live in the lounge</CardTitle></div>
                <Button variant="ghost" size="icon-sm" aria-label="View all live streams" onClick={() => setActiveSection("live")}><ArrowUpRight aria-hidden="true" /></Button>
              </CardHeader>
              <CardContent className="rail-stream-list">
                {initialStreams.slice(0, 2).map((stream) => (
                  <button className="rail-stream-row" key={stream.id} onClick={() => setSelectedStream(stream)} type="button">
                    <span className="rail-stream-image"><Image src={stream.image} alt="" fill sizes="72px" /></span>
                    <span className="rail-stream-copy"><strong>{stream.streamer}</strong><small>{stream.game}</small><span className="rail-live-status"><span className="live-dot" /> {stream.viewers} watching</span></span>
                  </button>
                ))}
              </CardContent>
            </Card>

            <Card className="rail-card squad-rail-card">
              <CardHeader className="rail-card-heading">
                <div><p className="eyebrow">YOUR PEOPLE</p><CardTitle>Online friends</CardTitle></div>
                <Badge variant="secondary">{onlineFriends.length} online</Badge>
              </CardHeader>
              <CardContent className="online-friend-list">
                {onlineFriends.map((friend) => (
                  <button className="online-friend-row" key={friend.id} onClick={() => openFriendChat(friend.id)} type="button">
                    <AvatarPhoto name={friend.name} src={friend.avatar} online size="default" />
                    <span className="online-friend-copy"><strong>{friend.name}</strong><small><Gamepad2 size={12} aria-hidden="true" /> {friend.game}</small></span>
                    <ChevronRight className="friend-chevron" size={15} aria-hidden="true" />
                  </button>
                ))}
                <Button variant="outline" size="sm" className="all-friends-button" onClick={() => notify("Your full friends list is ready.")}>View squad <ChevronRight data-icon="inline-end" aria-hidden="true" /></Button>
              </CardContent>
            </Card>

            <div className="quest-card">
              <span className="quest-icon"><Trophy size={17} aria-hidden="true" /></span>
              <div><p className="eyebrow">DAILY QUEST</p><strong>Play with a friend</strong><span>1 of 2 matches complete</span><div className="quest-progress"><span /></div></div>
            </div>

            <div className="rail-footer"><span><Headphones size={13} aria-hidden="true" /> Community-first gaming</span><span>Help · Rules · About</span></div>
          </aside>
        </main>
      </div>

      <MobileNavigation activeSection={activeSection} onNavigate={setActiveSection} />

      {toast ? <div className="app-toast" role="status" aria-live="polite"><Sparkles size={16} aria-hidden="true" />{toast}</div> : null}
      {selectedStory ? <StoryPreview name={selectedStory} onClose={() => setSelectedStory("")} /> : null}
      {selectedStream ? <StreamPreview stream={selectedStream} onClose={() => setSelectedStream(null)} onNotify={notify} /> : null}
    </div>
  )
}

function StoryPreview({ name, onClose }: { name: string; onClose: () => void }) {
  return (
    <div className="modal-backdrop" role="presentation">
      <section aria-labelledby="story-title" aria-modal="true" className="story-preview" role="dialog" onKeyDown={(event) => { if (event.key === "Escape") onClose() }}>
        <Image src="/images/stream-space.jpg" alt="Space Shooter story backdrop" fill sizes="420px" />
        <div className="story-preview-shade" />
        <div className="story-preview-top"><AvatarPhoto name={name} src={avatarImage} online size="sm" /><strong id="story-title">{name}</strong><span>just now</span><Button variant="ghost" size="icon-sm" aria-label="Close story" onClick={onClose}><X aria-hidden="true" /></Button></div>
        <div className="story-preview-content"><Badge variant="outline">SQUAD STORY</Badge><h2>One more run before the night ends.</h2><p>Tap into the feed to see what the rest of the squad is sharing.</p><Button onClick={onClose}>Back to the lounge</Button></div>
      </section>
    </div>
  )
}

function StreamPreview({ stream, onClose, onNotify }: { stream: LiveStream; onClose: () => void; onNotify: (message: string) => void }) {
  return (
    <div className="modal-backdrop" role="presentation">
      <section aria-labelledby="stream-title" aria-modal="true" className="stream-preview" role="dialog" onKeyDown={(event) => { if (event.key === "Escape") onClose() }}>
        <div className="stream-preview-header"><div><p className="eyebrow">LIVE STREAM PREVIEW</p><h2 id="stream-title">{stream.streamer}</h2></div><Button variant="ghost" size="icon" aria-label="Close stream" onClick={onClose}><X aria-hidden="true" /></Button></div>
        <div className="stream-preview-image"><Image src={stream.image} alt={`${stream.game} live gameplay`} fill sizes="(max-width: 760px) 100vw, 760px" /><span className="stream-preview-wash" /><span className="preview-play"><Radio size={25} aria-hidden="true" /></span><span className="live-label"><span className="live-dot" /> LIVE</span><span className="stream-viewers"><Users size={12} aria-hidden="true" /> {stream.viewers}</span></div>
        <div className="stream-preview-footer"><div><Badge variant="outline">{stream.game}</Badge><p>{stream.title}</p></div><Button onClick={() => onNotify(`You joined ${stream.streamer}'s stream.`)}>Join stream <ArrowUpRight data-icon="inline-end" aria-hidden="true" /></Button></div>
      </section>
    </div>
  )
}
