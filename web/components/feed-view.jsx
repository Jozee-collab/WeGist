"use client";

import { useState } from "react";
import { Bookmark, Heart, Image as ImageIcon, MessageCircle, MoreHorizontal, Plus, Send, Sparkles } from "lucide-react";

function Avatar({ post, size = "" }) {
  return <span className={`avatar avatar-${post.tint} ${size ? `avatar-${size}` : ""}`} aria-hidden="true">{post.avatar}</span>;
}

export default function FeedView({ posts, onCreatePost, onToggleLike }) {
  const [draft, setDraft] = useState("");
  const [activeComment, setActiveComment] = useState(null);
  const [comments, setComments] = useState({});
  const [saved, setSaved] = useState({});

  function submitPost(event) {
    event.preventDefault();
    if (!draft.trim()) return;
    onCreatePost(draft.trim());
    setDraft("");
  }

  function submitComment(event, postId) {
    event.preventDefault();
    const value = new FormData(event.currentTarget).get("comment");
    if (!value?.trim()) return;
    setComments((current) => ({ ...current, [postId]: [...(current[postId] || []), value.trim()] }));
    event.currentTarget.reset();
  }

  return (
    <section className="feed-page">
      <div className="section-heading feed-heading"><div><p className="eyebrow">THE COMMUNITY</p><h1>Community</h1><p className="section-subtitle">Good games. Great people. Your kind of feed.</p></div><button className="primary-button" type="button" onClick={() => document.querySelector(".post-composer textarea")?.focus()}><Plus size={17} /> Create post</button></div>
      <div className="story-strip" aria-label="Stories">
        <button className="story-item add-story" type="button" onClick={() => { const text = window.prompt("Share a quick story with your friends"); if (text?.trim()) onCreatePost(text.trim()); }}><span className="story-ring own-story"><span className="avatar avatar-user">W</span><i><Plus size={14} /></i></span><span>Your story</span></button>
        {[{ name: "Alice", initials: "A", tint: "violet", seen: false }, { name: "SpaceQueen", initials: "S", tint: "pink", seen: false }, { name: "Bob", initials: "B", tint: "mint", seen: true }, { name: "Charlie", initials: "C", tint: "blue", seen: true }].map((story) => <button key={story.name} className="story-item" type="button" onClick={() => window.alert(`${story.name}’s story is ready to view.`)}><span className={`story-ring ${story.seen ? "seen" : ""}`}><span className={`avatar avatar-${story.tint}`}>{story.initials}</span></span><span>{story.name}</span></button>)}
      </div>
      <form className="post-composer" onSubmit={submitPost}>
        <div className="composer-top"><span className="avatar avatar-user">W</span><textarea aria-label="Write a community post" placeholder="Share a win, a clip, or what you’re playing..." rows={2} value={draft} onChange={(event) => setDraft(event.target.value)} /></div>
        <div className="composer-bottom"><span className="composer-prompt"><Sparkles size={14} /> Keep it kind. Keep it fun.</span><div><button className="composer-utility" type="button" onClick={() => setDraft((current) => current ? `${current} 🎮` : "Just hit a new high score! 🎮")}><ImageIcon size={16} /> Add media</button><button className="send-post" type="submit" disabled={!draft.trim()}><Send size={15} /> Post</button></div></div>
      </form>
      <div className="feed-sort"><span>For you</span><button type="button">Most recent <span>⌄</span></button></div>
      <div className="post-list">
        {posts.map((post) => <article className="post-card" key={post.id}>
          <header className="post-header"><Avatar post={post} /><div className="post-author"><strong>{post.author}</strong><span>{post.handle} <i>·</i> {post.time}</span></div><button className="icon-button post-menu" type="button" aria-label={`More options for ${post.author}’s post`}><MoreHorizontal size={19} /></button></header>
          <p className="post-copy">{post.text}</p>
          {post.image && <div className="post-image-wrap"><img src={post.image} alt={`${post.tag} gameplay`} className="post-image" /><span className="image-tag">{post.tag}</span></div>}
          <div className="post-statline"><span>{post.likes + (post.liked ? 1 : 0)} likes</span><span>{post.comments + (comments[post.id]?.length || 0)} comments</span></div>
          <div className="post-actions"><button className={post.liked ? "liked" : ""} type="button" onClick={() => onToggleLike(post.id)} aria-pressed={post.liked}><Heart size={18} fill={post.liked ? "currentColor" : "none"} /> Like</button><button type="button" onClick={() => setActiveComment(activeComment === post.id ? null : post.id)}><MessageCircle size={18} /> Comment</button><button type="button" onClick={() => setSaved((current) => ({ ...current, [post.id]: !current[post.id] }))} aria-pressed={Boolean(saved[post.id])}><Bookmark size={17} fill={saved[post.id] ? "currentColor" : "none"} /> {saved[post.id] ? "Saved" : "Save"}</button></div>
          {activeComment === post.id && <div className="comment-area"><div className="comment-list">{(comments[post.id] || []).map((comment, index) => <p key={index}><strong>You</strong> {comment}</p>)}</div><form className="comment-form" onSubmit={(event) => submitComment(event, post.id)}><input name="comment" aria-label={`Comment on ${post.author}’s post`} placeholder="Add a thoughtful comment..." /><button type="submit" aria-label="Send comment"><Send size={15} /></button></form></div>}
        </article>)}
      </div>
    </section>
  );
}
