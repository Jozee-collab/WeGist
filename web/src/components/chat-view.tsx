"use client"

import { useMemo, useState } from "react"
import { ArrowLeft, Gamepad2, MessageSquare, MoreHorizontal, Phone, Search, Send, Video } from "lucide-react"
import { AvatarPhoto } from "@/components/avatar-photo"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import type { Conversation } from "@/lib/demo-data"

type ChatViewProps = {
  conversations: Conversation[]
  selectedConversationId: string
  searchQuery: string
  onSelectConversation: (id: string) => void
  onSendMessage: (conversationId: string, text: string) => void
  onNotify: (message: string) => void
}

export function ChatView({ conversations, selectedConversationId, searchQuery, onSelectConversation, onSendMessage, onNotify }: ChatViewProps) {
  const [chatSearch, setChatSearch] = useState("")
  const [draft, setDraft] = useState("")
  const [mobileListOpen, setMobileListOpen] = useState(false)
  const activeConversation = conversations.find((conversation) => conversation.id === selectedConversationId)
  const filteredConversations = useMemo(() => {
    const query = `${chatSearch} ${searchQuery}`.trim().toLowerCase()
    return conversations.filter((conversation) => `${conversation.name} ${conversation.handle} ${conversation.game}`.toLowerCase().includes(query))
  }, [chatSearch, conversations, searchQuery])

  function submitMessage(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const text = draft.trim()
    if (!text || !activeConversation) return
    onSendMessage(activeConversation.id, text)
    setDraft("")
  }

  return (
    <section className={`chat-layout${mobileListOpen ? " mobile-list-open" : ""}`} aria-label="Your chats">
      <Card className="chat-list-panel">
        <CardHeader className="chat-list-heading">
          <div>
            <p className="eyebrow">YOUR SQUAD</p>
            <CardTitle>Messages <span className="chat-total">{conversations.length}</span></CardTitle>
          </div>
          <Button variant="ghost" size="icon-sm" aria-label="Start a new chat" onClick={() => onNotify("Choose a friend from the online list to start a chat.")}><MessageSquare aria-hidden="true" /></Button>
        </CardHeader>
        <CardContent className="chat-list-content">
          <div className="chat-search-field">
            <Search size={16} aria-hidden="true" />
            <Input aria-label="Search conversations" onChange={(event) => setChatSearch(event.target.value)} placeholder="Find a teammate" value={chatSearch} />
          </div>
          <div className="conversation-filters" aria-label="Conversation filters">
            <span className="filter-chip is-selected">All chats</span>
            <span className="filter-chip">Unread</span>
            <span className="filter-chip">Groups</span>
          </div>
          <div className="conversation-list" role="list">
            {filteredConversations.map((conversation) => (
              <button
                aria-current={conversation.id === selectedConversationId ? "true" : undefined}
                className={`conversation-row${conversation.id === selectedConversationId ? " is-selected" : ""}`}
                key={conversation.id}
                onClick={() => {
                  onSelectConversation(conversation.id)
                  setMobileListOpen(false)
                }}
                role="listitem"
                type="button"
              >
                <AvatarPhoto name={conversation.name} src={conversation.avatar} online={conversation.online} size="lg" />
                <span className="conversation-copy">
                  <span className="conversation-topline"><strong>{conversation.name}</strong><time>{conversation.time}</time></span>
                  <span className="conversation-lastline"><span>{conversation.lastMessage}</span>{conversation.unread ? <span className="unread-count">{conversation.unread}</span> : null}</span>
                  <span className="conversation-game"><Gamepad2 size={12} aria-hidden="true" /> {conversation.game}</span>
                </span>
              </button>
            ))}
            {!filteredConversations.length ? <p className="no-conversations">No chats found.</p> : null}
          </div>
          <Button variant="outline" className="invite-friend-button" onClick={() => onNotify("Your invite link is ready to share.")}>
            <span className="invite-plus">+</span> Invite a friend
          </Button>
        </CardContent>
      </Card>

      {activeConversation ? (
        <Card className="chat-thread-panel">
          <CardHeader className="thread-header">
            <Button className="mobile-back-button" variant="ghost" size="icon-sm" aria-label="Back to chats" onClick={() => setMobileListOpen(true)}><ArrowLeft aria-hidden="true" /></Button>
            <AvatarPhoto name={activeConversation.name} src={activeConversation.avatar} online={activeConversation.online} size="lg" />
            <div className="thread-contact">
              <CardTitle>{activeConversation.name}</CardTitle>
              <span>{activeConversation.online ? "Online now" : "Last seen recently"} <span className="meta-dot" aria-hidden="true" /> {activeConversation.handle}</span>
            </div>
            <div className="thread-actions">
              <Button variant="ghost" size="icon-sm" aria-label="Start voice call" onClick={() => onNotify("Voice calls are coming soon.")}><Phone aria-hidden="true" /></Button>
              <Button variant="ghost" size="icon-sm" aria-label="Start video call" onClick={() => onNotify("Video calls are coming soon.")}><Video aria-hidden="true" /></Button>
              <Button variant="ghost" size="icon-sm" aria-label="Conversation options" onClick={() => onNotify("Conversation settings opened.")}><MoreHorizontal aria-hidden="true" /></Button>
            </div>
          </CardHeader>
          <div className="thread-date">TODAY · 8:38 PM</div>
          <ol className="message-list" aria-label={`Messages with ${activeConversation.name}`}>
            {activeConversation.messages.map((message) => (
              <li className={`message-row${message.from === "me" ? " from-me" : " from-them"}`} key={message.id}>
                {message.from === "them" ? <AvatarPhoto name={activeConversation.name} src={activeConversation.avatar} size="sm" /> : null}
                <div className="message-stack">
                  {message.gameInvite ? (
                    <Card className="game-invite-card">
                      <CardContent>
                        <div className="game-invite-top"><span className="game-invite-icon"><Gamepad2 size={17} aria-hidden="true" /></span><Badge variant="outline">GAME INVITE</Badge></div>
                        <strong>Tic-Tac-Toe challenge</strong>
                        <p>Quick match with {activeConversation.name.split(" ")[0]}. Your turn to make the first move.</p>
                        <Button size="sm" onClick={() => onNotify("Tic-Tac-Toe challenge accepted.")}>Accept challenge</Button>
                      </CardContent>
                    </Card>
                  ) : <p className="message-bubble">{message.text}</p>}
                  <time className="message-time">{message.time}</time>
                </div>
              </li>
            ))}
          </ol>
          <div className="thread-footer">
            <div className="quick-replies" aria-label="Quick actions">
              <Button variant="outline" size="xs" onClick={() => onNotify("Game invite ready to send.")}><Gamepad2 data-icon="inline-start" aria-hidden="true" /> Invite to play</Button>
              <Button variant="outline" size="xs" onClick={() => onNotify("Stream invite ready to send.")}><Video data-icon="inline-start" aria-hidden="true" /> Share stream</Button>
            </div>
            <form className="message-composer" onSubmit={submitMessage}>
              <Input aria-label="Write a message" onChange={(event) => setDraft(event.target.value)} placeholder={`Message ${activeConversation.name.split(" ")[0]}...`} value={draft} />
              <Button aria-label="Send message" disabled={!draft.trim()} size="icon" type="submit"><Send aria-hidden="true" /></Button>
            </form>
            <p className="composer-note">Your chats are private and end-to-end encrypted.</p>
          </div>
        </Card>
      ) : (
        <Card className="chat-empty"><CardContent><MessageSquare size={24} aria-hidden="true" /><h2>Choose a conversation</h2><p>Pick someone from your squad to jump back in.</p></CardContent></Card>
      )}
    </section>
  )
}
