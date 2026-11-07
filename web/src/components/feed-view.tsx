"use client"

import Image from "next/image"
import { useMemo, useState } from "react"
import {
  Bookmark,
  Flame,
  Heart,
  ImagePlus,
  MessageCircle,
  MoreHorizontal,
  Plus,
  Send,
  Sparkles,
} from "lucide-react"
import { AvatarPhoto } from "@/components/avatar-photo"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import {
  Card,
  CardAction,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs"
import type { FeedPost } from "@/lib/demo-data"

type FeedFilter = "for-you" | "following" | "trending"

type FeedViewProps = {
  posts: FeedPost[]
  searchQuery: string
  onCreatePost: (content: string) => void
  onLikePost: (postId: string) => void
  onReply: (postId: string, text: string) => void
  onNotify: (message: string) => void
  onOpenStory: (name: string) => void
}

const stories = [
  { name: "Your story", handle: "Add to your story", image: "/images/avatar-gamer.jpg", own: true },
  { name: "Nova Chen", handle: "nova", image: "/images/stream-space.jpg" },
  { name: "Pixel Dave", handle: "pixel_dave", image: "/images/stream-puzzle.jpg" },
  { name: "Mira Chen", handle: "mira.exe", image: "/images/avatar-gamer.jpg" },
  { name: "Kai Rivers", handle: "kai_rivers", image: "/images/stream-space.jpg" },
]

export function FeedView({ posts, searchQuery, onCreatePost, onLikePost, onReply, onNotify, onOpenStory }: FeedViewProps) {
  const [filter, setFilter] = useState<FeedFilter>("for-you")
  const [draft, setDraft] = useState("")

  const visiblePosts = useMemo(() => {
    const query = searchQuery.trim().toLowerCase()
    const filtered = posts.filter((post) => {
      const matchesQuery = !query || `${post.author} ${post.handle} ${post.game} ${post.content}`.toLowerCase().includes(query)
      return matchesQuery && (filter !== "following" || post.following)
    })

    return filter === "trending" ? [...filtered].sort((a, b) => b.likes - a.likes) : filtered
  }, [filter, posts, searchQuery])

  function submitPost(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const content = draft.trim()
    if (!content) return
    onCreatePost(content)
    setDraft("")
  }

  return (
    <section className="feed-view" aria-label="Gaming feed">
      <div className="stories-panel">
        <div className="section-heading compact-heading">
          <div>
            <p className="eyebrow">SQUAD STATUS</p>
            <h2>Stories from your circle</h2>
          </div>
          <Button variant="ghost" size="sm" onClick={() => onNotify("All squad stories are up to date.")}>View all</Button>
        </div>
        <div className="stories-row">
          {stories.map((story) => (
            <button className="story-button" key={story.name} onClick={() => onOpenStory(story.name)} type="button">
              <span className={`story-ring${story.own ? " story-ring-own" : ""}`}>
                <span className="story-photo">
                  <Image src={story.image} alt={`${story.name} story`} fill sizes="64px" />
                </span>
                {story.own ? <span className="story-add"><Plus size={13} aria-hidden="true" /></span> : null}
              </span>
              <span className="story-name">{story.own ? "Your story" : story.handle}</span>
            </button>
          ))}
        </div>
      </div>

      <Card className="compose-card">
        <form onSubmit={submitPost}>
          <CardHeader className="compose-card-header">
            <AvatarPhoto name="Alex Rivers" src="/images/avatar-gamer.jpg" />
            <Input
              aria-label="Share a gaming moment"
              className="compose-input"
              onChange={(event) => setDraft(event.target.value)}
              placeholder="What was your best gaming moment today?"
              value={draft}
            />
          </CardHeader>
          <CardFooter className="compose-card-footer">
            <div className="compose-shortcuts">
              <Button type="button" variant="ghost" size="sm" onClick={() => onNotify("Media attachments are coming soon.")}>
                <ImagePlus data-icon="inline-start" aria-hidden="true" />
                Clip or image
              </Button>
              <Button type="button" variant="ghost" size="sm" onClick={() => onNotify("Choose a game tag after you share your gist.")}>
                <GameTagIcon />
                Tag a game
              </Button>
            </div>
            <Button size="sm" type="submit" disabled={!draft.trim()}>
              <Send data-icon="inline-start" aria-hidden="true" />
              Post gist
            </Button>
          </CardFooter>
        </form>
      </Card>

      <div className="feed-controls">
        <Tabs value={filter} onValueChange={(value) => setFilter(value as FeedFilter)}>
          <TabsList className="feed-tabs" variant="line">
            <TabsTrigger value="for-you">For you</TabsTrigger>
            <TabsTrigger value="following">Following</TabsTrigger>
            <TabsTrigger value="trending"><Flame data-icon="inline-start" aria-hidden="true" />Trending</TabsTrigger>
          </TabsList>
        </Tabs>
        <Button variant="ghost" size="sm" className="feed-filter" onClick={() => onNotify("Your feed is tuned for gaming and friends.")}>
          <Sparkles data-icon="inline-start" aria-hidden="true" />
          Tuning
        </Button>
      </div>

      <div className="post-list">
        {visiblePosts.length ? visiblePosts.map((post) => (
          <PostCard key={post.id} post={post} onLike={onLikePost} onReply={onReply} onNotify={onNotify} />
        )) : (
          <Card className="empty-posts">
            <CardContent>
              <div className="empty-post-icon"><Sparkles size={20} aria-hidden="true" /></div>
              <h3>No gists match that search</h3>
              <p>Try another player, game, or phrase.</p>
            </CardContent>
          </Card>
        )}
      </div>
    </section>
  )
}

function PostCard({ post, onLike, onReply, onNotify }: {
  post: FeedPost
  onLike: (postId: string) => void
  onReply: (postId: string, text: string) => void
  onNotify: (message: string) => void
}) {
  const [commentsOpen, setCommentsOpen] = useState(false)
  const [reply, setReply] = useState("")

  function submitReply(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const text = reply.trim()
    if (!text) return
    onReply(post.id, text)
    setReply("")
  }

  return (
    <Card className="post-card">
      <CardHeader className="post-header">
        <AvatarPhoto name={post.author} src={post.avatar} size="lg" />
        <div className="post-author">
          <div className="post-name-row"><CardTitle>{post.author}</CardTitle><span className="verified-mark" aria-label="Verified player">✓</span><span className="post-time">· {post.time}</span></div>
          <div className="post-meta"><span>{post.handle}</span><span className="meta-dot" aria-hidden="true" /><Badge variant="outline">{post.game}</Badge></div>
        </div>
        <CardAction>
          <Button variant="ghost" size="icon-sm" aria-label={`More options for ${post.author}'s post`} onClick={() => onNotify("Post options are ready.")}>
            <MoreHorizontal aria-hidden="true" />
          </Button>
        </CardAction>
      </CardHeader>
      <CardContent className="post-body">
        <p className="post-copy">{post.content}</p>
        {post.image ? (
          <div className="post-image-frame">
            <Image src={post.image} alt={`${post.game} gameplay highlight shared by ${post.author}`} fill sizes="(max-width: 850px) 100vw, 760px" />
            <div className="image-caption"><span className="caption-pulse" aria-hidden="true" /> GAMEPLAY MOMENT</div>
          </div>
        ) : null}
      </CardContent>
      <CardFooter className="post-actions">
        <Button variant="ghost" size="sm" aria-pressed={post.liked} className={post.liked ? "post-action is-liked" : "post-action"} onClick={() => onLike(post.id)}>
          <Heart data-icon="inline-start" fill={post.liked ? "currentColor" : "none"} aria-hidden="true" />
          {post.likes}
        </Button>
        <Button variant="ghost" size="sm" aria-expanded={commentsOpen} className="post-action" onClick={() => setCommentsOpen((open) => !open)}>
          <MessageCircle data-icon="inline-start" aria-hidden="true" />
          {post.comments} comments
        </Button>
        <Button variant="ghost" size="sm" className="post-action save-action" onClick={() => onNotify("Saved to your highlights.")}>
          <Bookmark data-icon="inline-start" aria-hidden="true" />
          Save
        </Button>
        <Button variant="ghost" size="icon-sm" className="share-action" aria-label="Share post" onClick={() => onNotify("Share link copied to your clipboard.")}>
          <Send aria-hidden="true" />
        </Button>
      </CardFooter>
      {commentsOpen ? (
        <div className="comments-area">
          <p className="sample-comment"><strong>Pixel Dave</strong> That timing was unreal. Run it back?</p>
          <form className="comment-form" onSubmit={submitReply}>
            <Input aria-label="Write a comment" onChange={(event) => setReply(event.target.value)} placeholder="Add a thoughtful reply…" value={reply} />
            <Button aria-label="Send comment" disabled={!reply.trim()} size="icon" type="submit"><Send aria-hidden="true" /></Button>
          </form>
        </div>
      ) : null}
    </Card>
  )
}

function GameTagIcon() {
  return <span className="game-tag-icon" aria-hidden="true">#</span>
}
