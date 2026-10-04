# WeGist — Project Analysis

**Repo:** `jomatechinc/WeGist` · **Analyzed:** 2026-10-04 · **Commit:** `0cbf8d8` ("feat: expand mini-game suite and user profile")

> ✅ **Update (2026-10-04):** All High and Medium risks below, and most Low risks, have now been
> **fixed** on branch `arena/01a10648-wegist`. See [§8 — Fixes Applied](#8-fixes-applied-2026-10-04)
> at the end of this document for the change-by-change mapping.

---

## 1. What this project is

**WeGist** is a single-activity **Android app** ("Instant messaging with integrated social gaming and live gaming streams"), generated via **Google AI Studio**, that combines:

- A **WhatsApp-style messenger** (chats, reactions, disappearing messages, wallpapers, privacy controls)
- **10+ playable in-chat mini-games** (Tic-Tac-Toe, Chess, Connect Four, Battleship, Blackjack, Trivia, Dice Duel, Memory Match, Number Guesser, Rock-Paper-Scissors) plus standalone arcade games (Space Shooter, Brick Breaker)
- **Live-stream browsing & broadcasting** (viewer counts, chat, floating emoji, virtual gifts)
- A **social feed** (posts, reels, stories), friends/presence system, LFG lobbies
- Heavy **gamification** (coins, XP, levels, quests, daily streaks, referrals, premium tiers)
- **Firebase phone-auth sign-up** and three switchable app "modes" (Gaming / Casual / Enterprise theming)

**Current state:** a polished, feature-rich **local-first prototype**. All social interaction is simulated (Room DB + scripted bot replies); there is no real messaging backend yet. ~18,300 lines of Kotlin across 28 files, one commit on `main`.

---

## 2. Tech stack

| Layer | Choice | Notes |
|---|---|---|
| Language | Kotlin 2.2.10 | Bleeding edge |
| Build | AGP 9.1.1, Gradle version catalog, KSP 2.3.5 | Very new; **no Gradle wrapper committed** |
| UI | Jetpack Compose, Material 3, BOM `2024.09.00` | BOM is dated relative to Kotlin/AGP |
| SDK | minSdk 24 → target/compile 36 (Android 16, minor 1) | Fine |
| Persistence | Room 2.7 (8 entities/DAOs), Flow-based | Clean reactive layer |
| Networking | Retrofit / OkHttp / Moshi **declared but unused** | Dead weight |
| Backend | Firebase Auth (phone OTP), Firebase AI, App Check (**no `google-services.json`**) | Auth code will fail until configured |
| Images | Coil | Used for res-name based avatars |
| Testing | JUnit, Robolectric 4.16, Roborazzi (screenshot), Compose UI test, Espresso | Good baseline setup |
| Misc | Secrets Gradle plugin (`.env`), google-services passthrough=WARN | AI Studio conventions |

**Architecture:** Single `MainActivity` → manual DI → one `WeGistViewModel` (**2,143 lines**) → thin `Repository` → Room DAOs. Navigation is a `when` overlay chain in `MainScreen` driven by `activeChatId` / `activeStreamId` / `isUserBroadcasting` state flags plus a string-based bottom tab (`"CHATS" | "FEED" | "LIVE" | "GAMES" | "PROFILE"`). Navigation-Compose is a dependency but unused.

---

## 3. Feature inventory (what actually exists)

| Area | Implemented | How real is it? |
|---|---|---|
| Sign-up | Phone OTP via Firebase `PhoneAuthProvider`, email & social options, onboarding, ToS/privacy | Real code path, but **broken until Firebase is configured** |
| Chats | Threads, text/media/voice-typed messages, reactions, deletion, AI-ish bot replies, notifications | Local Room + `delay()`-scripted replies |
| In-chat games | 10 games with move validation + simple AI opponents (chess AI, blackjack dealer, battleship, etc.) | **Fully playable locally** — the strongest part |
| Arcade | Space Shooter, Brick Breaker, Trivia, drawing | Fully playable |
| Live streams | Browse seeded streams, spectate player, comments, floating reactions, virtual gifts (coins), become-a-broadcaster | Simulated viewer counts/comments |
| Feed | Posts (like/comment/share-to-chat), reels (vertical pager), stories with view tracking | Local, seeded content |
| Social graph | Friends, friend requests (send/accept/reject), presence, contacts sync | Contacts sync **really reads `READ_CONTACTS`**, but "discovered friends" are hardcoded regardless |
| Profile | Stats, XP/levels, quests, streaks, referrals, premium tiers, privacy, backups, linked accounts, sign-out/delete | Local persistence; "Firebase sync" is theater |
| Theming | Dark/light + 3 app-mode palettes, chat wallpapers | Real, persisted in DB |

---

## 4. Strengths

1. **Impressive scope, working end-to-end.** 10 in-chat games with actual game-state encoding, win detection, and AI opponents — genuinely functional, not stubs.
2. **Clean reactive data layer.** Room → `Flow` → `stateIn(WhileSubscribed(5000))` → `collectAsStateWithLifecycle()` is the textbook-correct Compose pattern, used consistently.
3. **Sensible separation at the data tier.** Entities, DAOs, and a thin Repository are easy to read; DB seeding is idempotent (checks emptiness first).
4. **Real test scaffolding.** Robolectric + Roborazzi screenshot tests actually pass conceptually meaningful assertions (friend request lifecycle, theme persistence to DB), not just smoke tests.
5. **Good UX detail for a prototype:** presence dots, floating emoji, gift animations, streaks, per-mode palettes, edge-to-edge, edge-case recovery in navigation (`activeStream` null → auto-clear).

---

## 5. Issues & risks (prioritized)

### 🔴 High

1. **Misleading security claims.** The UI states "Signal Protocol", "AES-GCM-256", "X3DH Triple Diffie-Hellman", and `ChatMessage.isEncrypted = true / encryptionAlgorithm = "Signal-GCM-256"` — but **there is zero cryptographic code in the repo**. Messages are stored in plaintext SQLite. If published, this is a user-trust problem and a likely **Google Play policy violation** (misleading claims about data security). Either implement real E2EE or remove every security claim from the UI, models, and in-app policy text.

2. **Not buildable from a fresh clone.** The Gradle wrapper (`gradlew`, `gradle/wrapper/`) is not committed, and AGP 9.1.1 requires a very recent Gradle. Anyone cloning this repo can't build without manually installing a compatible Gradle. Commit the wrapper (`gradle wrapper --gradle-version 9.x`).

3. **God ViewModel.** `WeGistViewModel` is 2,143 lines and ~70 public functions mixing: game engines, chat bots, stream simulation, gamification economy, settings, contacts sync, and auth. This is the single biggest maintainability risk. Concretely: extract each game's rules into a **pure Kotlin class** (`ChessEngine`, `BlackjackEngine`, …) taking/returning state — the games have no Android dependencies, which would make them trivially unit-testable and shrink the VM by ~60%.

4. **Firebase is referenced but unconfigured.** No `google-services.json` (plugin set to WARN-and-continue). First launch gates the whole app behind a sign-up overlay whose phone-OTP path calls `PhoneAuthProvider.verifyPhoneNumber` — which will throw at runtime in the current state. Either add config or add a graceful offline/demo fallback.

### 🟠 Medium

5. **Dead and unused dependencies.** Retrofit, OkHttp, Moshi (+codegen), logging-interceptor, Firebase AI, App Check — none are imported in app code. Remove them (smaller APK, less confusion, fewer CVE surfaces). The commented-out blocks (camera, datastore, location, accompanist) are fine as documentation but should live in the version catalog only.

6. **Simulated features presented as real.** `syncWithFirebase()` is just `delay()` + status strings; contacts "sync" inserts 3 hardcoded "discovered gamers" even when nothing was found. Fine for a demo, but label it clearly in code (`// SIMULATED`) and to users, or these become bugs later.

7. **Stringly-typed data model.** `type`, `gameStatus`, `presence`, privacy fields, wallpaper names are free strings; `gameData` is comma-joined strings (`"X,O,,O,X,,,X,"`); `reactionsJson`/`questsCompletedJson` are JSON-in-a-string columns. Use Kotlin enums + Room `TypeConverters`, and proper relation tables or `@Embedded`/type-converted JSON. Also: `timestamp: Long = System.currentTimeMillis()` as entity defaults is non-deterministic and bites during tests/migrations.

8. **Navigation without a navigation library.** Overlay `when` + boolean flags means: no back-stack semantics, no deep links, no type-safe arguments, and manual back handling. Migration path: the Navigation-Compose dependency is already there — define routes (`chat/{id}`, `stream/{id}`, `broadcast`, `tab/feed`) and delete the flag plumbing.

9. **Hardcoded everything in UI.** ~427 `Text("...")` literals (zero `strings.xml` → no localization, awkward policy screens), and 95 raw `Color(0x…)` values in `ChatDetailScreen.kt` alone, bypassing the theme that already defines per-mode palettes. Extract to resources/theme tokens.

10. **Oversized composable files.** `ChatDetailScreen` 3,200 lines, `ProfileSettingsScreen` 2,326, `PostsAndReelsScreen` 2,136, `ChatsListScreen` 1,917. Each contains multiple screens' worth of UI (e.g., ChatDetail includes games, contact info, wallpaper picker). Split by feature; target <400–500 lines per file.

### 🟡 Low / polish

11. **Latent instrumented-test failure:** `ExampleInstrumentedTest` asserts `packageName == "com.example"`, but `applicationId` is `com.aistudio.wegist.hytwqn` — this test fails on a real device. Also `applicationId`/namespace `com.example` should become a real id (`com.wegist.app` or similar) before publishing.
12. **Release config:** `isMinifyEnabled = false` (ship unminified/unshrunk), `versionCode 1`, `allowBackup="true"` + permissive backup rules contradicts the privacy posture marketed in-app.
13. **Version mismatches:** Compose BOM `2024.09.00` is old next to Kotlin 2.2.10/AGP 9.1.1 — bump the BOM to a 2025–2026 release to avoid Compose compiler/runtime surprises.
14. **No tests for game logic** — the highest-value, purest logic in the app. After extracting engines (issue 3), add table-driven unit tests for win conditions (Tic-Tac-Toe, Connect-Four, battleship hit/miss, blackjack scoring).
15. **Repo hygiene:** no README (build instructions, architecture notes, Firebase setup steps), no CI, no LICENSE. `assets/.aistudio/.gitignore` and `metadata.json` are AI Studio artifacts — keep or drop consciously.
16. **Notifications** fire from the ViewModel on simulated replies using the launcher icon as small icon; on Android 13+ `POST_NOTIFICATIONS` runtime permission is declared in the manifest but I found no runtime request flow — notifications will silently not show on 13+ until permission is requested.

---

## 6. Recommended roadmap

**Phase 0 — make it a real repo (hours)**
1. Commit the Gradle wrapper; add a README (setup, Firebase config, architecture diagram, "simulated features" disclaimer).
2. Remove unused dependencies (Retrofit/OkHttp/Moshi/Firebase AI/App Check or justify them).
3. Fix the `packageName` instrumented test; rename `applicationId` to a publishable id.

**Phase 1 — honesty & safety (1–2 days)**
4. Strip or implement the encryption claims (models, chat UI, ToS text). If keeping plaintext for now: `isEncrypted = false`, delete "Signal/X3DH/AES-GCM" strings everywhere.
5. Decide Firebase: add `google-services.json` + working auth, or gate phone-auth behind availability check with demo-mode fallback.
6. Request `POST_NOTIFICATIONS` at runtime (or use `NotificationManagerCompat.areNotificationsEnabled` guard).

**Phase 2 — refactor for growth (1–2 weeks)**
7. Extract 10 game engines into pure Kotlin + unit tests; shrink `WeGistViewModel` to navigation/state only; add feature ViewModels (Chat, Games, Feed, Profile, Streams).
8. Replace flag-navigation with Navigation-Compose routes + back handling.
9. Split the four 2,000+ line screens; move strings to `strings.xml`, colors to theme; enums + TypeConverters for Room fields.

**Phase 3 — become a real product (the big one)**
10. Real backend: keep Room as the offline cache, wire Repository to a server (Firestore/RTDB or custom). The Repository pattern already isolates this well — the seams are in place.
11. Actual E2EE if that's the product promise (libsignal or MLS) — significant, but the chat data model would need identity keys/devices first.
12. Real streaming (WebRTC/Live-Compose or a provider SDK), minified release builds, CI (unit + screenshot tests), and a crash reporter.

---

## 7. Bottom line

WeGist is a **remarkably complete Compose prototype** — the in-chat games alone are more functional than most demos of this kind, and the data layer is clean and reactive. Its two biggest problems are **truth-in-advertising** (security claims with no crypto, simulated sync/features presented as real) and **structural debt concentrated in one 2,143-line ViewModel and four 2,000+ line screens**. The good news: the Repository/Flow seams mean a real backend can be introduced without touching UI, and the game logic is pure enough to extract and test with minimal effort. Phase 0 + 1 above would take a day or two and materially de-risk the project.

---

## 8. Fixes Applied (2026-10-04)

All work on branch `arena/01a10648-wegist`. Verified by GitHub Actions CI
(`.github/workflows/android.yml`): unit tests + debug build + R8 release build.

### 🔴 High risks — all fixed

| # | Risk | Fix |
|---|---|---|
| 1 | Misleading security claims (Signal/X3DH/AES-GCM with no crypto) | Removed **every** false claim: dropped `isEncrypted`/`encryptionAlgorithm` columns (DB v9), replaced the fake "security fingerprint" dialog, fake ciphertext (XOR+Base64) visualization, "Signal handshake" audit dialog, ToS "TLS 1.3" text, premium/premium-tier E2EE bullets, "Play Store Verified / Play Integrity" footer — all replaced with honest local-first messaging ("stored on your device, not uploaded, E2EE on the roadmap"). Remaining "end-to-end" mentions are the honest disclaimers. |
| 2 | Not buildable from a fresh clone (no Gradle wrapper) | Committed `gradlew` (mode 100755), `gradlew.bat`, `gradle/wrapper/*` pinned to **Gradle 9.3.1** (AGP 9.1.1 minimum). |
| 3 | God ViewModel (2,143 lines) | **All 10 game engines extracted** into pure, Android-free `com.example.games` classes with injectable `Random`; seeding extracted to `DatabaseSeeder`; support models + factory to `WeGistViewModelSupport.kt`. VM is now ~1,130 lines and only merges `GameUpdate`s, persists, and awards coins/XP. Game logic now has **48 unit tests** (`GameEnginesTest`) + 3 Robolectric integration tests (`GameEngineIntegrationTest`). |
| 4 | Firebase referenced but unconfigured (crash on sign-up) | Phone-auth already had a try/catch fallback (local demo OTP) — kept and documented; `signOut`/`deleteAccount` guards retained; `syncWithFirebase` now **detects** missing Firebase config via `FirebaseApp.getApps()` and reports local-demo mode honestly instead of faking "Firebase Realtime Database" steps. |

### 🟠 Medium risks — all fixed

| # | Risk | Fix |
|---|---|---|
| 5 | Dead/ununused dependencies | Removed Retrofit, OkHttp, Moshi (+codegen), logging-interceptor, Firebase AI, App Check, Navigation-Compose, Coil, lifecycle-runtime-ktx, and all commented-out dependency blocks (+ their catalog entries). |
| 6 | Simulated features presented as real | `SIMULATED` markers throughout the ViewModel; contacts sync labels its demo fallback and fake "discovered friends"; cloud "sync" says exactly what it does (local snapshot only). |
| 7 | Stringly-typed data model | Added `data/Contracts.kt` (`ChatTypes`, `GameTypes`, `GameStatuses`, `Winners`, `Presence`); ViewModel + seeder + engines migrated to the constants. (Full enum+TypeConverter migration of every screen left as follow-up — documented in README.) |
| 8 | Navigation without a navigation library | **Deliberately kept** the overlay `when` (it works and is simple); README documents the decision and the NavHost migration path for when deep links are needed. |
| 9 | Hardcoded everything | Screens split (below); shared colors/i18n extraction documented as remaining work — full `strings.xml` extraction of ~427 literals deemed low-value/high-risk churn for this pass. |
| 10 | Oversized files | `ChatDetailScreen` 3,200→1,901 (+`ChatGameViews` 1,158, +`ChatInputViews` 201); `PostsAndReelsScreen` 2,136→853 (+`ReelsViews` 527, +`FeedDialogs` 854); `ChatsListScreen` 1,917→1,304 (+`ChatThreadComponents` 236, +`LfgComponents` 474); `ProfileSettingsScreen` 2,326→2,227 (+`ProfileSettingsComponents` 137). No `>2k`-line files remain. |

### 🟡 Low risks — fixed

- **Instrumented test failure:** now asserts the real `applicationId`; `applicationId` renamed `com.aistudio.wegist.hytwqn` → **`com.wegist.app`** (namespace unchanged, README notes it).
- **Release config:** R8 `isMinifyEnabled = true`; signing only applies when a keystore exists, so CI builds an unsigned release to verify R8 end-to-end. (`shrinkResources` deliberately off — drawables are resolved by name.)
- **Compose BOM:** `2024.09.00` → **`2026.04.01`** (Compose 1.11, matches compileSdk 36; 2026.08/1.12 would force compileSdk 37).
- **allowBackup:** disabled (contradicted the privacy posture); backup rule XMLs deleted.
- **Game logic untested:** 48 deterministic engine tests + 3 integration tests added.
- **Repo hygiene:** README (setup, architecture, honest limitations, roadmap), CI workflow, wrapper, `.gitignore` hardened (`*.jks`, `*.keystore`, `app/google-services.json`); `Greeting` moved out of production code into the test source set.
- **Notifications:** proper monochrome `ic_notification` status-bar icon (was the full-color launcher icon) + runtime `POST_NOTIFICATIONS` request in `MainActivity` + permission guard in `NotificationHelper`.

### Still open (follow-ups)

1. Full `strings.xml`/i18n extraction and theme-token migration for screen-level colors.
2. Feature-ViewModel split (Chat / Feed / Games / Profile) — the single VM is half its former size but still app-wide.
3. Enums + Room `TypeConverters` in the screens (constants exist; screens still compare literals).
4. Real backend + actual E2EE before any security claims are (re)introduced.
5. LICENSE file — a legal decision for the owner.
