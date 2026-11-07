export const initialMessages = {
  alice: [
    { id: "a1", author: "Alice", text: "Hey! Down for some casual gaming?", time: "10:42 AM" },
    { id: "a2", author: "Alice", text: "I challenge you to Tic-Tac-Toe. Your move!", time: "10:44 AM", game: true },
    { id: "a3", author: "You", text: "Absolutely. Winner picks the next game.", time: "10:47 AM" }
  ],
  bob: [
    { id: "b1", author: "Bob", text: "Just went live with a new Brick Breaker run!", time: "9:28 AM" },
    { id: "b2", author: "You", text: "I’m jumping in now. That last score was wild.", time: "9:35 AM" }
  ],
  spacequeen: [
    { id: "s1", author: "SpaceQueen", text: "Squad up for the next round? We need one more.", time: "Yesterday" }
  ]
};

export const conversations = [
  { id: "alice", name: "Alice (Retro Gamer)", handle: "@aliceplays", avatar: "A", tint: "violet", presence: "Online", preview: "Winner picks the next game.", time: "10:47 AM", unread: 2, game: "Tic-Tac-Toe" },
  { id: "bob", name: "Bob_Destroyer", handle: "@bob_destroyer", avatar: "B", tint: "mint", presence: "In Brick Breaker", preview: "I’m jumping in now. That last score was wild.", time: "9:35 AM", unread: 0, game: "Brick Breaker" },
  { id: "spacequeen", name: "SpaceQueen", handle: "@spacequeen", avatar: "S", tint: "pink", presence: "Online", preview: "Squad up for the next round?", time: "Yesterday", unread: 0, game: "Space Shooter" }
];

export const friends = [
  { id: "alice", name: "Alice (Retro Gamer)", handle: "@aliceplays", avatar: "A", tint: "violet", presence: "Online", rank: 42, wins: 32 },
  { id: "bob", name: "Bob_Destroyer", handle: "@bob_destroyer", avatar: "B", tint: "mint", presence: "In Brick Breaker", rank: 110, wins: 25 },
  { id: "dave", name: "Dave_The_Noob", handle: "@dave", avatar: "D", tint: "orange", presence: "Away", rank: 230, wins: 2 }
];

export const friendRequests = [
  { id: "spacequeen", name: "SpaceQueen", handle: "@spacequeen", avatar: "S", tint: "pink", note: "Plays Space Shooter" },
  { id: "charlie", name: "GamerCharlie", handle: "@charlie", avatar: "C", tint: "blue", note: "Found you in Arcade" }
];

export const initialPosts = [
  { id: "p1", author: "Bob_Destroyer", handle: "@bob_destroyer", avatar: "B", tint: "mint", time: "12 min ago", text: "Finally cracked my Brick Breaker high score. 18,420 and still climbing. Who’s taking the leaderboard next?", image: "/images/brick-breaker.jpg", likes: 24, comments: 3, liked: false, tag: "Brick Breaker" },
  { id: "p2", author: "SpaceQueen", handle: "@spacequeen", avatar: "S", tint: "pink", time: "48 min ago", text: "Late-night runs hit different. Anyone up for a co-op session? 🌌", image: "/images/space-shooter.jpg", likes: 18, comments: 7, liked: false, tag: "Space Shooter" },
  { id: "p3", author: "Alice (Retro Gamer)", handle: "@aliceplays", avatar: "A", tint: "violet", time: "2 hr ago", text: "Just joined the friendliest corner of the arcade. Mini-games, good people, zero pressure. Let’s play.", likes: 31, comments: 12, liked: false, tag: "Community" }
];

export const streams = [
  { id: "stream_bob", streamer: "Bob_Destroyer", handle: "@bob_destroyer", title: "Breaking my all-time high score", game: "Brick Breaker", viewers: "1.4K", avatar: "B", tint: "mint", image: "/images/brick-breaker.jpg", category: "Arcade" },
  { id: "stream_queen", streamer: "SpaceQueen", handle: "@spacequeen", title: "Ranked space matches — come fly with us", game: "Space Shooter", viewers: "890", avatar: "S", tint: "pink", image: "/images/space-shooter.jpg", category: "Action" }
];

export const games = [
  { id: "space-shooter", name: "Space Shooter", genre: "ACTION · ARCADE", players: "2.4k playing", image: "/images/space-shooter.jpg", tint: "pink", description: "Take the controls, clear the asteroid field, and chase your personal best." },
  { id: "brick-breaker", name: "Brick Breaker", genre: "CLASSIC · ARCADE", players: "1.8k playing", image: "/images/brick-breaker.jpg", tint: "violet", description: "A neon take on the arcade classic. Keep the ball alive and clear every row." },
  { id: "tic-tac-toe", name: "Tic-Tac-Toe", genre: "QUICK MATCH · 2 PLAYERS", players: "Play with a friend", image: "/images/wegist-icon.jpg", tint: "mint", description: "A fast, friendly duel. Invite someone from your chats and make the first move." },
  { id: "trivia", name: "Trivia Rush", genre: "PUZZLE · PARTY", players: "640 playing", image: "/images/space-shooter.jpg", tint: "orange", description: "Quick-fire questions across games, science, movies, and pop culture." }
];
