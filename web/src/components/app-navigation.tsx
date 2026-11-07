"use client"

import Image from "next/image"
import {
  AudioLines,
  Bell,
  Gamepad2,
  MessagesSquare,
  Newspaper,
  Plus,
  Radio,
  Settings2,
  Sparkles,
  Trophy,
  UserRound,
  type LucideIcon,
} from "lucide-react"
import { Button } from "@/components/ui/button"
import { AvatarPhoto } from "@/components/avatar-photo"
import type { Section } from "@/lib/demo-data"

type NavigationItem = {
  id: Section
  label: string
  icon: LucideIcon
  count?: string
}

const navigationItems: NavigationItem[] = [
  { id: "chats", label: "Chats", icon: MessagesSquare, count: "2" },
  { id: "feed", label: "Gaming feed", icon: Newspaper },
  { id: "live", label: "Live streams", icon: Radio, count: "LIVE" },
  { id: "arcade", label: "Arcade", icon: Gamepad2 },
  { id: "profile", label: "Profile", icon: UserRound },
]

type AppNavigationProps = {
  activeSection: Section
  onNavigate: (section: Section) => void
  onCompose: () => void
  coins: number
}

export function AppSidebar({ activeSection, onNavigate, onCompose, coins }: AppNavigationProps) {
  return (
    <aside className="app-sidebar" aria-label="WeGist workspace">
      <div className="sidebar-brand-row">
        <div className="brand-lockup">
          <div className="brand-mark">
            <Image src="/images/wegist-mark.jpg" alt="" width={38} height={38} priority />
          </div>
          <div>
            <p className="brand-name">WeGist</p>
            <p className="brand-caption">PLAY THE MOMENT</p>
          </div>
        </div>
        <Button variant="ghost" size="icon-sm" aria-label="Notifications" className="sidebar-notice">
          <Bell aria-hidden="true" />
        </Button>
      </div>

      <Button className="compose-button" onClick={onCompose}>
        <Plus data-icon="inline-start" aria-hidden="true" />
        Share a gist
      </Button>

      <div className="realm-switcher" aria-label="Current community">
        <span className="realm-icon"><AudioLines size={16} aria-hidden="true" /></span>
        <span className="realm-copy">
          <span className="realm-kicker">YOUR REALM</span>
          <span className="realm-title">Gamer lounge</span>
        </span>
        <Settings2 size={15} className="realm-settings" aria-hidden="true" />
      </div>

      <p className="sidebar-label">YOUR SPACE</p>
      <nav className="primary-navigation" aria-label="Main navigation">
        {navigationItems.map(({ id, label, icon: Icon, count }) => (
          <button
            aria-current={activeSection === id ? "page" : undefined}
            className={`navigation-link${activeSection === id ? " is-active" : ""}`}
            key={id}
            onClick={() => onNavigate(id)}
            type="button"
          >
            <Icon size={18} strokeWidth={1.8} aria-hidden="true" />
            <span>{label}</span>
            {count ? <span className={`navigation-count${count === "LIVE" ? " is-live" : ""}`}>{count}</span> : null}
          </button>
        ))}
      </nav>

      <div className="sidebar-challenge">
        <div className="challenge-icon"><Trophy size={17} aria-hidden="true" /></div>
        <div className="challenge-copy">
          <strong>Daily streak</strong>
          <span>3 days in a row</span>
        </div>
        <Sparkles size={15} className="challenge-spark" aria-hidden="true" />
      </div>

      <div className="sidebar-bottom">
        <div className="coin-balance">
          <span className="coin-symbol">W</span>
          <span className="coin-copy"><strong>{coins.toLocaleString()}</strong><small>Gist coins</small></span>
          <Plus size={16} aria-hidden="true" />
        </div>
        <button className="account-button" onClick={() => onNavigate("profile")} type="button">
          <AvatarPhoto name="Alex Rivers" src="/images/avatar-gamer.jpg" size="default" />
          <span className="account-copy"><strong>Alex Rivers</strong><small>Level 08 · Gold</small></span>
          <span className="account-menu" aria-hidden="true">•••</span>
        </button>
      </div>
    </aside>
  )
}

export function MobileNavigation({ activeSection, onNavigate }: Pick<AppNavigationProps, "activeSection" | "onNavigate">) {
  return (
    <nav className="mobile-navigation" aria-label="Main navigation">
      {navigationItems.map(({ id, label, icon: Icon }) => (
        <button
          aria-current={activeSection === id ? "page" : undefined}
          className={`mobile-navigation-link${activeSection === id ? " is-active" : ""}`}
          key={id}
          onClick={() => onNavigate(id)}
          type="button"
        >
          <Icon size={19} strokeWidth={1.9} aria-hidden="true" />
          <span>{label === "Gaming feed" ? "Feed" : label === "Live streams" ? "Live" : label}</span>
        </button>
      ))}
    </nav>
  )
}
