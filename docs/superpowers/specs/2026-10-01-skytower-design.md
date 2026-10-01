# SkyTower Android Game Design

**Date:** 2026-10-01

## 1. Goal

Build a lightweight portrait Android game named **SkyTower** for Google Play. The player taps once to drop a horizontally moving block onto the tower. Any overhanging portion is cut away, the next block inherits the remaining width, and the run continues until a block misses completely.

The first release must be simple, responsive, visually polished, playable offline, and easy to build in GitHub Actions as both APK and Android App Bundle (AAB).

## 2. Product Constraints

- Platform: Android only.
- Store target: Google Play.
- Orientation: portrait only.
- Package name: `com.elxvro.skytower`.
- Application name: `SkyTower`.
- Minimum Android version: API 26 (Android 8.0).
- Compile SDK: API 36.
- Target SDK: API 36.
- Rendering approach: native Kotlin with a custom `SurfaceView` game loop; no external game engine.
- Offline-first: gameplay, score storage, settings, audio and visuals must work without network access.
- First release should not require account creation, analytics, ads, billing, remote backend or cloud save.

## 3. Core Gameplay

### 3.1 Start State

The player starts from a simple main menu with:

- Play button.
- Best score.
- Sound toggle.
- Vibration toggle.
- Theme/skin button prepared for local cosmetic variants.

Tapping Play immediately starts a run.

### 3.2 Block Movement

- A rectangular block moves horizontally from one side of the screen to the other above the current tower.
- Movement direction alternates naturally as the block bounces at screen bounds.
- The initial movement speed is intentionally forgiving.
- Speed increases gradually as the score rises.
- The game loop uses delta time so movement remains consistent across device refresh rates.

### 3.3 Drop and Cut Logic

On player tap:

1. Freeze the moving block.
2. Compare its horizontal overlap with the top block.
3. If overlap is greater than zero, keep only the overlapping portion as the new placed block.
4. Animate the overhanging fragment falling away.
5. Spawn the next block above the tower using the surviving width.
6. Increment score.

If overlap is zero, the run ends.

### 3.4 Perfect Placement and Combo

A placement is considered **perfect** when the horizontal offset is within a small tolerance of the block below.

- Perfect placement snaps to exact alignment.
- Width is preserved.
- Consecutive perfect placements increase a combo counter.
- Combo produces a short visual pulse, text feedback and light haptic feedback.
- Combo is cosmetic/score-feedback only in v1 and does not create complicated power systems.

### 3.5 Camera and Tower Growth

- The tower remains visually centered near the lower-middle portion of the screen.
- As tower height grows, the world scrolls downward to keep the newest blocks visible.
- Older blocks may remain in a lightweight list but drawing can skip blocks outside the visible viewport.

## 4. Scoring and Progression

- Every successful placement: +1 score.
- Best score is stored locally using `SharedPreferences`.
- Current score is shown during gameplay.
- Best score is shown on the menu and game-over screen.
- Game speed rises using a capped progression curve so later play becomes harder without becoming physically impossible.

No currencies, missions, login streaks or online leaderboards are required for v1.

## 5. Screens

### 5.1 Main Menu

Visual direction follows the approved concept art: bright sky, floating-island atmosphere, colorful glossy blocks and large readable controls.

Main menu elements:

- SkyTower logo/title.
- Play button.
- Best score.
- Sound toggle.
- Vibration toggle.
- Theme button.

### 5.2 Game Screen

- Current score at top center.
- Pause button at top-left.
- Best score or combo indicator in a secondary position.
- Tower and moving block occupy most of the screen.
- Minimal HUD so the tower remains visually dominant.

### 5.3 Pause Overlay

- Resume.
- Restart.
- Return to menu.
- Sound toggle.
- Vibration toggle.

### 5.4 Game Over Overlay

- Final score.
- Best score.
- New-record indication when applicable.
- Retry.
- Main menu.

## 6. Visual System

The game should use a clean, colorful, family-friendly visual style based on the previously approved SkyTower mockup.

### Background

- Gradient blue sky.
- Soft cloud layers.
- Lightweight floating-island silhouettes/shapes created as app assets.
- Subtle parallax may be added only if it does not complicate performance.

### Blocks

- Rounded rectangles with a glossy highlight.
- Bright rotating palette: blue, pink, yellow, purple, green, orange.
- Small landing squash/pulse animation on successful placement.
- Falling cut fragments rotate slightly as they leave the screen.

### Effects

- Tiny particles for perfect placement.
- Short combo flash.
- Screen shake must be subtle and only used for failure or high combo moments.

The game must avoid relying on copyrighted third-party characters, logos or externally licensed art.

## 7. Audio and Haptics

Audio is local and lightweight.

Required events:

- Tap/drop.
- Normal placement.
- Perfect placement.
- Combo milestone.
- Game over.
- Menu button.

Haptic events:

- Light vibration on placement.
- Slightly stronger vibration on perfect placement.
- Distinct failure vibration on game over.

Both audio and vibration must be independently disableable and persist locally.

## 8. Architecture

### Android shell

- `MainActivity` hosts the app and locks portrait orientation.
- A simple screen/state coordinator switches between menu, game, pause and game-over states.

### Game loop

- `SkyTowerView : SurfaceView` owns rendering and receives touch input.
- A dedicated game-loop thread updates simulation and renders frames while active.
- `GameEngine` owns deterministic gameplay state independent from Android drawing APIs as much as practical.

### Core model

- `Block`: world-space rectangle, color/theme id and state.
- `GameState`: score, combo, speed, block list, moving block, camera offset and mode.
- `PlacementResult`: miss, normal overlap or perfect placement.

### Supporting components

- `PlacementCalculator`: pure overlap/perfect-placement math.
- `DifficultyCurve`: score-to-speed mapping with explicit min/max values.
- `GamePreferences`: best score, sound enabled, vibration enabled and selected theme.
- `SoundController`: local audio playback.
- `HapticController`: guarded vibration calls.
- `ThemePalette`: local block/background color definitions.

The placement math and difficulty progression must be unit-testable without Android UI instrumentation.

## 9. Input and State Rules

- Only one gameplay action is required: tap to drop.
- Ignore duplicate taps while a placement animation/transition is being resolved.
- Pause the simulation when the app loses foreground focus.
- Resume only after explicit player action if the game was interrupted.
- Restart must reset score, combo, speed progression, camera state and block list but keep persisted settings/best score.

## 10. Performance

- Target smooth animation on ordinary Android devices.
- Avoid per-frame object allocation in the hot rendering/update path where practical.
- Clamp large delta-time spikes after app interruptions.
- Keep assets modest in resolution and memory footprint.
- No networking is required during gameplay.

## 11. Persistence

Use `SharedPreferences` for v1.

Persist:

- Best score.
- Sound enabled.
- Vibration enabled.
- Selected local theme id.

Do not persist an unfinished run in v1.

## 12. Testing

### Unit tests

Must cover at minimum:

- Exact placement preserves full width.
- Partial overlap returns correct surviving width and cut fragment.
- Zero overlap returns game-over/miss.
- Perfect tolerance snaps correctly.
- Difficulty speed increases and respects maximum cap.
- Restart resets transient state while keeping persisted best score separately.

### Build verification

CI must run:

- Gradle unit tests.
- Android lint.
- Debug APK build.
- Release APK build.
- Release AAB build.

## 13. CI/CD and Deliverables

GitHub Actions should build from the repository on pushes to `main` and manual dispatch.

Artifacts:

- `SkyTower-debug.apk` for quick device testing.
- `SkyTower-release.apk` for signed/release-style testing when signing credentials are available.
- `SkyTower-release.aab` for Google Play upload when signing credentials are available.

Signing credentials must be supplied through GitHub Actions secrets and must never be committed to the repository.

If release signing secrets are not yet configured, CI may always produce the debug APK while keeping the signed release jobs ready to activate once secrets are added.

## 14. Google Play Compatibility

As of 2026-10-01, new Google Play phone/tablet apps and updates are required to target Android 16 / API 36 or higher from 2026-08-31 onward. SkyTower therefore targets API 36 from the first commit.

The first production upload should use the AAB artifact. APK remains useful for direct testing and internal distribution outside the production Play upload flow.

## 15. Explicit v1 Non-Goals

The following are intentionally excluded from the first playable release:

- Online accounts.
- Google Sign-In.
- Cloud save.
- Online leaderboard.
- Ads.
- In-app purchases.
- Daily rewards.
- Backend API.
- Multiplayer.
- Complex achievement system.

These can be added later without changing the core stacking engine.

## 16. Success Criteria

The first release is successful when:

1. The app installs and launches on Android.
2. The player can start a run with one tap from the menu.
3. Blocks move, drop, cut correctly and game-over triggers on a complete miss.
4. Perfect placements and combo feedback work.
5. Score and best score behave correctly across restarts.
6. Pause/resume/retry/menu flows are reliable.
7. Sound and vibration settings persist.
8. GitHub Actions passes tests/lint and produces an installable APK.
9. The project is configured to produce an API-36-compatible AAB for Google Play.
