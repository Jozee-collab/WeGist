# WeGist

**Instant messaging with integrated social gaming and live gaming streams** — an Android app built with Jetpack Compose, Kotlin and Room.

> ⚠️ **This is a local-first demo.** Every social feature (opponent replies, stream viewers,
> friend discovery, cloud backup) is **simulated locally**. Nothing leaves your device, there is
> no backend, and messages are **not** end-to-end encrypted yet. The UI states this honestly.

---

## Features

- 💬 **Chats** — threads, reactions, disappearing-message settings, wallpapers, voice-note UI
- 🎮 **10 playable in-chat games** — Tic-Tac-Toe, Rock-Paper-Scissors, Trivia, Chess (4×4),
  Connect Four, Battleship, Dice Duel, Memory Match, Number Guesser, Blackjack — each with
  move validation and an AI opponent
- 🕹️ **Arcade hub** — Space Shooter, Brick Breaker, party games (Would You Rather, Trivia Crack, …)
- 📺 **Live streams** — browse seeded streams, spectate with simulated chat/floating reactions,
  go live yourself with virtual gifts
- 📱 **Feed** — posts, reels, stories, share-to-chat
- 👥 **Friends** — requests, presence, contacts sync (demo), LFG lobbies
- 🏆 **Gamification** — coins, XP/levels, quests, daily streaks, referrals, premium tiers
- 🎨 **Three app modes** (Gaming / Casual / Enterprise) with distinct palettes, dark/light theme

## Tech stack

| Layer | Choice |
|---|---|
| UI | Jetpack Compose (BOM 2026.04.01), Material 3 |
| Language | Kotlin 2.2.10 |
| Build | AGP 9.1.1 · Gradle 9.3.1 (wrapper committed) · version catalog · KSP |
| Persistence | Room 2.7 (8 entities, Flow-based DAOs, destructive migrations) |
| Auth | Firebase Phone Auth — **optional**; falls back to local demo OTP when unconfigured |
| SDK | minSdk 24 → targetSdk 36 |
| Testing | JUnit, Robolectric 4.16, Roborazzi screenshots, pure-JVM game-engine tests |

## Build & run

```bash
./gradlew :app:assembleDebug          # debug APK
./gradlew :app:testDebugUnitTest      # unit + Robolectric + game-engine tests
./gradlew :app:assembleRelease        # R8-minified unsigned release (signs if KEYSTORE_PATH set)
```

Requirements: JDK 17+ and an Android SDK with platform 36 (AGP auto-installs missing
components when licenses are accepted). CI (`.github/workflows/android.yml`) runs the full
build + test suite on every push.

### Firebase (optional)

The app runs fine without Firebase (`googleServices.missing.passthrough = WARN`): sign-up
falls back to a **local demo OTP** flow. To enable real phone auth, drop a
`google-services.json` into `app/` and add SHA-1/SHA-256 fingerprints of your signing keys in
the Firebase console. `google-services.json` is git-ignored.

### Release signing

Release builds are minified with R8 and only signed when a keystore is available —
set `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD` env vars (or place
`my-upload-key.jks` in the repo root).

## Architecture

```
MainActivity (single activity, manual DI)
└── WeGistViewModel (~1.1k lines)          app-wide state, coin/XP rewards, persistence
    ├── com.example.games.*                PURE Kotlin game engines (no Android deps)
    │   └── TicTacToeEngine, ChessEngine, ConnectFourEngine, BattleshipEngine,
    │       BlackjackEngine, MemoryMatchEngine, DiceDuelEngine, NumberGuessEngine,
    │       RockPaperScissorsEngine, TriviaEngine   ← all unit-tested
    ├── DatabaseSeeder                      idempotent first-launch demo seeding
    ├── Repository (thin)                   pass-through over 8 Room DAOs
    └── Room (AppDatabase v9)
```

- **Game rules** live in `com.example.games` as pure objects operating on the serialized
  `gameData` strings; the ViewModel only merges results (`GameUpdate`) into the persisted
  message and awards coins/wins. Engines take an injectable `kotlin.random.Random`, so the
  tests are deterministic.
- **Navigation** is a `when` overlay chain in `MainScreen` (sign-up → broadcast → stream →
  chat) plus a bottom tab bar. It is deliberately simple; migrating to Navigation-Compose
  routes is recommended before adding deep links.
- **UI** is split per feature (`screens/`, `screens/ChatGameViews.kt`, `screens/FeedDialogs.kt`, …).
- `app/src/main/java/com/example/data/Contracts.kt` holds the typed string constants
  (`ChatTypes`, `GameTypes`, `GameStatuses`, `Presence`, `Winners`) for the stringly-typed
  Room columns.

### Known deliberate limitations (demo)

- All "other users" are simulated — replies, presence, stream viewers, friend discovery.
- `syncWithFirebase()` is a labeled simulation; only a local backup timestamp is written.
- No E2E encryption (the UI says so; don't re-add security claims without implementing them).
- Most UI strings are hardcoded English (no `strings.xml` extraction / i18n yet).
- Screens still contain hardcoded colors instead of theme tokens in places.

## Testing

```bash
./gradlew :app:testDebugUnitTest
```

- `GameEnginesTest` — pure JVM tests for all 10 in-chat game engines (win detection,
  invalid moves, scoring, determinism via seeded `Random`)
- `GameEngineIntegrationTest` — move flows through ViewModel into Room
- `ExampleRobolectricTest` — friends/requests/theme persistence
- `GreetingScreenshotTest` — Roborazzi screenshot regression

## Project history

This project was generated with Google AI Studio and then hardened
(see `PROJECT_ANALYSIS.md` for the full audit): false security claims removed, unused
dependencies pruned, Gradle wrapper + CI added, game engines extracted and unit-tested,
oversized files split, notification permission handling fixed, and `applicationId` set to
`com.wegist.app`.

## License

No license yet — pick one (e.g. Apache-2.0, MIT) and add a `LICENSE` file before
publishing. Until then, default copyright applies.
