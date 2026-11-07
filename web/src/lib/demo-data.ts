export type Section = "chats" | "feed" | "live" | "arcade" | "profile"

export type FeedPost = {
  id: string
  author: string
  handle: string
  avatar: string
  game: string
  time: string
  content: string
  image?: string
  likes: number
  comments: number
  liked: boolean
  following: boolean
}

export type ChatMessage = {
  id: string
  from: "me" | "them"
  text: string
  time: string
  gameInvite?: boolean
}

export type Conversation = {
  id: string
  name: string
  handle: string
  avatar: string
  online: boolean
  game: string
  time: string
  unread: number
  lastMessage: string
  messages: ChatMessage[]
}

export type LiveStream = {
  id: string
  streamer: string
  handle: string
  game: string
  title: string
  viewers: string
  avatar: string
  image: string
  category: "trending" | "friends"
}

export const avatarImage = "/images/avatar-gamer.jpg"

export const initialConversations: Conversation[] = [
  {
    id: "alice",
    name: "Alice Morgan",
    handle: "@alice_retro",
    avatar: avatarImage,
    online: true,
    game: "Tic-Tac-Toe",
    time: "8:47 PM",
    unread: 2,
    lastMessage: "Your move. No pressure.",
    messages: [
      { id: "a1", from: "them", text: "Hey, one quick round before the next stream?", time: "8:38 PM" },
      { id: "a2", from: "me", text: "Only if you promise not to use the corner trick again.", time: "8:41 PM" },
      { id: "a3", from: "them", text: "Your move. No pressure.", time: "8:47 PM", gameInvite: true },
    ],
  },
  {
    id: "bob",
    name: "Bob Stone",
    handle: "@bob_destroyer",
    avatar: avatarImage,
    online: true,
    game: "Brick Breaker",
    time: "8:21 PM",
    unread: 0,
    lastMessage: "That last run was wild.",
    messages: [
      { id: "b1", from: "them", text: "That last run was wild. I clipped the whole thing.", time: "8:21 PM" },
      { id: "b2", from: "me", text: "Send it over. We need a rematch soon.", time: "8:24 PM" },
    ],
  },
  {
    id: "mira",
    name: "Mira Chen",
    handle: "@mira.exe",
    avatar: avatarImage,
    online: false,
    game: "Last seen 12m ago",
    time: "7:56 PM",
    unread: 0,
    lastMessage: "I will be around after dinner.",
    messages: [
      { id: "m1", from: "them", text: "I will be around after dinner. Save a spot?", time: "7:56 PM" },
      { id: "m2", from: "me", text: "Always. I will ping you when we queue.", time: "8:02 PM" },
    ],
  },
  {
    id: "dave",
    name: "Pixel Dave",
    handle: "@pixel_dave",
    avatar: avatarImage,
    online: true,
    game: "In Space Shooter",
    time: "7:30 PM",
    unread: 0,
    lastMessage: "Need one more for ranked.",
    messages: [
      { id: "d1", from: "them", text: "Need one more for ranked. You in?", time: "7:30 PM" },
    ],
  },
]

export const initialPosts: FeedPost[] = [
  {
    id: "post-1",
    author: "Nova Chen",
    handle: "@nova_arcade",
    avatar: avatarImage,
    game: "Space Shooter",
    time: "12 min ago",
    content: "One heart left, final wave, and the cleanest run of my life. The squad saw it happen.",
    image: "/images/stream-space.jpg",
    likes: 128,
    comments: 18,
    liked: false,
    following: true,
  },
  {
    id: "post-2",
    author: "Pixel Dave",
    handle: "@pixel_dave",
    avatar: avatarImage,
    game: "Brick Breaker",
    time: "38 min ago",
    content: "Finally beat my personal best. The double bounce at the end was pure luck, and I am counting it.",
    image: "/images/stream-puzzle.jpg",
    likes: 84,
    comments: 12,
    liked: true,
    following: true,
  },
  {
    id: "post-3",
    author: "Mira Chen",
    handle: "@mira.exe",
    avatar: avatarImage,
    game: "Squad notes",
    time: "1 hr ago",
    content: "Looking for a chill co-op crew tonight. Good comms, no pressure, just good games.",
    likes: 42,
    comments: 7,
    liked: false,
    following: false,
  },
]

export const initialStreams: LiveStream[] = [
  {
    id: "stream-1",
    streamer: "Bob Stone",
    handle: "@bob_destroyer",
    game: "Brick Breaker",
    title: "Chasing the 10K score. Come hang out.",
    viewers: "1.4K",
    avatar: avatarImage,
    image: "/images/stream-puzzle.jpg",
    category: "trending",
  },
  {
    id: "stream-2",
    streamer: "Nova Chen",
    handle: "@nova_arcade",
    game: "Space Shooter",
    title: "Ranked runs with the squad",
    viewers: "890",
    avatar: avatarImage,
    image: "/images/stream-space.jpg",
    category: "friends",
  },
  {
    id: "stream-3",
    streamer: "Mira Chen",
    handle: "@mira.exe",
    game: "Pixel Rally",
    title: "No HUD challenge, first try only",
    viewers: "426",
    avatar: avatarImage,
    image: "/images/stream-puzzle.jpg",
    category: "trending",
  },
]

export const onlineFriends = [
  { id: "alice", name: "Alice Morgan", game: "Tic-Tac-Toe", avatar: avatarImage },
  { id: "bob", name: "Bob Stone", game: "Brick Breaker", avatar: avatarImage },
  { id: "dave", name: "Pixel Dave", game: "Space Shooter", avatar: avatarImage },
  { id: "kai", name: "Kai Rivers", game: "In the lobby", avatar: avatarImage },
]

export const games = [
  { id: "space", name: "Space Shooter", genre: "Arcade · Co-op", accent: "mint", players: "2.4K playing" },
  { id: "breakout", name: "Brick Breaker", genre: "Classic · Solo", accent: "purple", players: "1.8K playing" },
  { id: "rally", name: "Pixel Rally", genre: "Racing · Versus", accent: "orange", players: "960 playing" },
  { id: "tactics", name: "Tactics Lab", genre: "Strategy · Squad", accent: "pink", players: "740 playing" },
]
