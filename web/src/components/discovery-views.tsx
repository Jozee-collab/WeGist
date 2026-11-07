"use client"

import Image from "next/image"
import { useState } from "react"
import {
  ArrowRight,
  Check,
  ChevronRight,
  Coins,
  Gamepad2,
  Globe2,
  Headphones,
  Play,
  Radio,
  Settings2,
  ShieldCheck,
  Sparkles,
  Trophy,
  Users,
  Video,
  WandSparkles,
  type LucideIcon,
} from "lucide-react"
import { AvatarPhoto } from "@/components/avatar-photo"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import {
  Card,
  CardAction,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { games, type LiveStream } from "@/lib/demo-data"

type LiveViewProps = {
  streams: LiveStream[]
  searchQuery: string
  onWatch: (stream: LiveStream) => void
  onStartBroadcast: () => void
}

export function LiveView({ streams, searchQuery, onWatch, onStartBroadcast }: LiveViewProps) {
  const visibleStreams = streams.filter((stream) => `${stream.streamer} ${stream.title} ${stream.game}`.toLowerCase().includes(searchQuery.toLowerCase()))
  const featured = visibleStreams[0] ?? streams[0]

  return (
    <section className="live-view" aria-label="Live streams">
      <div className="live-hero-wrap">
        {featured ? (
          <button className="live-hero" onClick={() => onWatch(featured)} type="button" aria-label={`Watch ${featured.streamer} playing ${featured.game}`}>
            <Image src={featured.image} alt={`${featured.game} stream thumbnail`} fill priority sizes="(max-width: 900px) 100vw, 820px" />
            <span className="live-hero-shade" />
            <span className="live-hero-content">
              <span className="live-label"><span className="live-dot" /> LIVE NOW <span className="viewer-total">{featured.viewers} watching</span></span>
              <strong>{featured.title}</strong>
              <span className="live-hero-meta"><AvatarPhoto name={featured.streamer} src={featured.avatar} size="sm" /> {featured.streamer} <span className="meta-dot" /> {featured.game}</span>
              <span className="watch-cta"><Play size={14} fill="currentColor" aria-hidden="true" /> Watch stream</span>
            </span>
            <span className="live-hero-corner"><Radio size={16} aria-hidden="true" /> TRENDING</span>
          </button>
        ) : null}
        <Card className="broadcast-card">
          <CardHeader>
            <span className="broadcast-icon"><Video size={19} aria-hidden="true" /></span>
            <CardTitle>Go live with your squad</CardTitle>
          </CardHeader>
          <CardContent>
            <p>Turn your next run into a shared moment. Your friends will get a heads-up when you start.</p>
            <Button className="broadcast-button" onClick={onStartBroadcast}><Video data-icon="inline-start" aria-hidden="true" /> Start a broadcast</Button>
            <div className="broadcast-footnote"><ShieldCheck size={14} aria-hidden="true" /> Only share what you are comfortable streaming</div>
          </CardContent>
        </Card>
      </div>

      <div className="section-heading stream-section-heading">
        <div><p className="eyebrow">FROM YOUR COMMUNITY</p><h2>Streams to jump into</h2></div>
        <Button variant="ghost" size="sm">Browse all <ArrowRight data-icon="inline-end" aria-hidden="true" /></Button>
      </div>
      <div className="stream-grid">
        {visibleStreams.map((stream) => (
          <Card className="stream-card" key={stream.id}>
            <button className="stream-thumbnail" onClick={() => onWatch(stream)} type="button" aria-label={`Watch ${stream.streamer}'s ${stream.game} stream`}>
              <Image src={stream.image} alt={`${stream.game} gameplay`} fill sizes="(max-width: 700px) 100vw, 340px" />
              <span className="live-label"><span className="live-dot" /> LIVE</span>
              <span className="stream-viewers"><Users size={12} aria-hidden="true" /> {stream.viewers}</span>
            </button>
            <CardContent className="stream-card-content">
              <AvatarPhoto name={stream.streamer} src={stream.avatar} size="default" online />
              <div className="stream-card-copy"><strong>{stream.title}</strong><span>{stream.streamer} · {stream.game}</span></div>
              <Button variant="ghost" size="icon-sm" aria-label={`Open ${stream.streamer}'s stream`} onClick={() => onWatch(stream)}><ChevronRight aria-hidden="true" /></Button>
            </CardContent>
          </Card>
        ))}
      </div>
    </section>
  )
}

type ArcadeViewProps = {
  coins: number
  onPlay: (gameName: string) => void
  onCheckIn: () => void
}

const gameIcons: LucideIcon[] = [Gamepad2, Globe2, Trophy, WandSparkles]

export function ArcadeView({ coins, onPlay, onCheckIn }: ArcadeViewProps) {
  const [checkedIn, setCheckedIn] = useState(false)

  function checkIn() {
    if (checkedIn) return
    setCheckedIn(true)
    onCheckIn()
  }

  return (
    <section className="arcade-view" aria-label="Arcade">
      <Card className="arcade-hero">
        <CardContent>
          <div className="arcade-hero-copy">
            <Badge variant="outline" className="arcade-kicker"><Sparkles data-icon="inline-start" aria-hidden="true" /> THE ARCADE</Badge>
            <h2>One more round?</h2>
            <p>Find a quick match, challenge your squad, and collect coins along the way.</p>
            <div className="arcade-hero-actions">
              <Button onClick={() => onPlay("Space Shooter")}>Find a quick game <ArrowRight data-icon="inline-end" aria-hidden="true" /></Button>
              <span className="arcade-coins"><span className="coin-symbol">W</span> {coins.toLocaleString()} coins</span>
            </div>
          </div>
          <div className="arcade-orbit" aria-hidden="true"><div className="orbit-ring orbit-one" /><div className="orbit-ring orbit-two" /><div className="orbit-core"><Gamepad2 size={38} /></div><span className="orbit-spark spark-one" /><span className="orbit-spark spark-two" /></div>
        </CardContent>
      </Card>

      <div className="section-heading arcade-section-heading">
        <div><p className="eyebrow">PICK YOUR NEXT RUN</p><h2>Games for every mood</h2></div>
        <Button variant="outline" size="sm" onClick={checkIn} disabled={checkedIn}>
          {checkedIn ? <Check data-icon="inline-start" aria-hidden="true" /> : <Coins data-icon="inline-start" aria-hidden="true" />}
          {checkedIn ? "Claimed today" : "Daily check-in · +40"}
        </Button>
      </div>
      <div className="game-grid">
        {games.map((game, index) => {
          const Icon = gameIcons[index]
          return (
            <Card className={`game-card game-${game.accent}`} key={game.id}>
              <CardHeader className="game-card-header">
                <span className="game-art"><Icon size={25} aria-hidden="true" /></span>
                <CardAction><Button variant="ghost" size="icon-sm" aria-label={`${game.name} options`}><Settings2 aria-hidden="true" /></Button></CardAction>
              </CardHeader>
              <CardContent className="game-card-content">
                <p className="eyebrow">{game.genre}</p>
                <h3>{game.name}</h3>
                <div className="game-card-meta"><span><Users size={13} aria-hidden="true" /> {game.players}</span><span><Headphones size={13} aria-hidden="true" /> Voice ready</span></div>
                <Button variant="secondary" className="game-play-button" onClick={() => onPlay(game.name)}>Play now <Play data-icon="inline-end" aria-hidden="true" /></Button>
              </CardContent>
            </Card>
          )
        })}
      </div>
    </section>
  )
}

type ProfileViewProps = {
  coins: number
  isDark: boolean
  onToggleTheme: () => void
  onNotify: (message: string) => void
}

export function ProfileView({ coins, isDark, onToggleTheme, onNotify }: ProfileViewProps) {
  return (
    <section className="profile-view" aria-label="Your profile">
      <Card className="profile-hero">
        <div className="profile-banner" />
        <CardContent className="profile-hero-content">
          <AvatarPhoto name="Alex Rivers" src="/images/avatar-gamer.jpg" size="lg" online className="profile-large-avatar" />
          <div className="profile-identity"><div className="profile-name-row"><h2>Alex Rivers</h2><Badge variant="outline">LEVEL 08</Badge></div><p>@alexrivers · Member since 2024</p><span className="profile-bio">Here for the close games, good people, and one more round.</span></div>
          <Button variant="outline" className="profile-edit-button" onClick={() => onNotify("Profile editor opened.")}><Settings2 data-icon="inline-start" aria-hidden="true" /> Edit profile</Button>
        </CardContent>
      </Card>

      <div className="profile-stats-grid">
        <ProfileStat icon={Trophy} value="18" label="Wins" accent="mint" />
        <ProfileStat icon={Radio} value="04" label="Streams" accent="purple" />
        <ProfileStat icon={Coins} value={coins.toLocaleString()} label="Gist coins" accent="orange" />
        <ProfileStat icon={Users} value="32" label="Friends" accent="pink" />
      </div>

      <Card className="profile-settings-card">
        <CardHeader><div><p className="eyebrow">PREFERENCES</p><CardTitle>Your setup</CardTitle></div></CardHeader>
        <CardContent className="settings-list">
          <div className="settings-row"><span className="settings-row-icon"><Sparkles size={16} aria-hidden="true" /></span><span className="settings-row-copy"><strong>Dark mode</strong><small>Keep your lounge easy on the eyes</small></span><button aria-checked={isDark} className={`toggle-switch${isDark ? " is-on" : ""}`} onClick={onToggleTheme} role="switch" type="button"><span /></button></div>
          <button className="settings-row settings-action" onClick={() => onNotify("Privacy settings opened.")} type="button"><span className="settings-row-icon"><ShieldCheck size={16} aria-hidden="true" /></span><span className="settings-row-copy"><strong>Privacy and safety</strong><small>Control who can reach you</small></span><ChevronRight size={18} aria-hidden="true" /></button>
          <button className="settings-row settings-action" onClick={() => onNotify("Notification settings opened.")} type="button"><span className="settings-row-icon"><Radio size={16} aria-hidden="true" /></span><span className="settings-row-copy"><strong>Notifications</strong><small>Choose what you hear about</small></span><ChevronRight size={18} aria-hidden="true" /></button>
        </CardContent>
      </Card>
    </section>
  )
}

function ProfileStat({ icon: Icon, value, label, accent }: { icon: LucideIcon; value: string; label: string; accent: string }) {
  return <Card className="profile-stat"><CardContent><span className={`profile-stat-icon stat-${accent}`}><Icon size={17} aria-hidden="true" /></span><strong>{value}</strong><span>{label}</span></CardContent></Card>
}
