# SkyTower Replaceable Home Assets Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the SkyTower Home screen visually replaceable by swapping image files, without changing Kotlin/Canvas code, while preserving all current gameplay data and touch behavior.

**Architecture:** Add one cached asset-loading path (`UiAssetCatalog` + `UiAssetCache` + `UiAssetLoader`) and one bitmap drawing helper (`UiBitmapRenderer`). Migrate only `HomeScreen` in this phase: layout/touch rectangles and live text remain code-owned; background, logo, tower/platform, card shells, icons and play-button art become asset-owned with safe Canvas fallbacks.

**Tech Stack:** Android/Kotlin, SurfaceView/Canvas, `AssetManager`, `BitmapFactory`, JUnit 4, existing GitHub Actions Android build.

**Spec:** `docs/superpowers/specs/2026-10-03-skytower-user-replaceable-visual-assets-design.md`

## Global Constraints

- Asset root is exactly `app/src/main/assets/ui/`.
- Stable filenames are the public art-swap contract; normal visual replacement must not require Kotlin edits.
- Dynamic values stay code-rendered: coin count, best score, level, XP, prices, progress and state labels.
- Background uses center-crop; foreground art uses aspect-fit unless explicitly documented otherwise.
- Reference layout remains 1080x1920 and current Home touch targets must not move.
- Missing/invalid optional assets must fall back safely; no visual asset may crash the app.
- No network, backend, remote theme packs or in-app image editor.
- Decode bitmaps once and cache them; never decode inside the frame loop.
- First migration target is Home only. Other screens remain unchanged until Home is visually approved.

## Review Focus

1. **Corrupt or unreadable asset:** Android decode returns `null` and Home uses the Canvas fallback; build/lint plus runtime seam verify this in Tasks 2 and 5.
2. **Very tall / very wide device:** center-crop fully covers background and aspect-fit never distorts foreground assets; tested in Task 1.
3. **Asset with extreme aspect ratio:** destination math stays inside the target rectangle for aspect-fit; tested in Task 1.
4. **Repeated frame rendering:** same asset path is decoded once and served from cache; pure cache behavior tested in Task 2.
5. **Visual replacement changing taps:** Home menu/play/daily hit rectangles remain numerically identical to current 1080x1920 geometry; tested in Task 3.

---

## File Structure

**Create**
- `app/src/main/java/com/elxvro/skytower/ui/assets/UiAssetCatalog.kt` — canonical stable asset keys/paths.
- `app/src/main/java/com/elxvro/skytower/ui/assets/UiAssetCache.kt` — pure generic positive/negative cache.
- `app/src/main/java/com/elxvro/skytower/ui/assets/UiAssetLoader.kt` — Android bitmap decode adapter backed by the cache.
- `app/src/main/java/com/elxvro/skytower/ui/assets/UiBitmapRenderer.kt` — center-crop/aspect-fit math and Canvas bitmap drawing.
- `app/src/main/java/com/elxvro/skytower/ui/assets/HomeAssetLayout.kt` — current Home reference rectangles/touch geometry in one pure layout contract.
- `app/src/main/java/com/elxvro/skytower/ui/assets/HomeAssetSlots.kt` — pure slot-to-path mapping.
- `app/src/test/java/com/elxvro/skytower/ui/assets/UiAssetCatalogTest.kt`
- `app/src/test/java/com/elxvro/skytower/ui/assets/UiBitmapRendererMathTest.kt`
- `app/src/test/java/com/elxvro/skytower/ui/assets/UiAssetCacheTest.kt`
- `app/src/test/java/com/elxvro/skytower/ui/assets/HomeAssetLayoutTest.kt`
- `app/src/test/java/com/elxvro/skytower/ui/assets/HomeAssetSlotsTest.kt`
- `app/src/main/assets/ui/home/*` — starter replaceable Home images.
- `app/src/main/assets/ui/icons/*` — starter replaceable common Home icons.
- `docs/visual-assets/SkyTower-Home-Asset-Guide.md` — filename, size and replacement guide for the user.

**Modify**
- `app/src/main/java/com/elxvro/skytower/ui/screens/HomeScreen.kt` — draw available asset art, keep live text/state and fallbacks.
- `app/src/main/java/com/elxvro/skytower/ui/SkyTowerV05View.kt` — instantiate one loader/cache, inject it into Home, clear it on release.
- `README.md` — point to the visual-asset guide and art replacement workflow.

---

### Task 1: Canonical asset catalog and scaling math

**Files:**
- Create: `app/src/main/java/com/elxvro/skytower/ui/assets/UiAssetCatalog.kt`
- Create: `app/src/main/java/com/elxvro/skytower/ui/assets/UiBitmapRenderer.kt`
- Test: `app/src/test/java/com/elxvro/skytower/ui/assets/UiAssetCatalogTest.kt`
- Test: `app/src/test/java/com/elxvro/skytower/ui/assets/UiBitmapRendererMathTest.kt`

**Interfaces:**
- Produces: `object UiAssetCatalog` with exact string paths for all Home assets.
- Produces: `data class SourceSize(val width: Float, val height: Float)`.
- Produces: `data class FloatRect(val left: Float, val top: Float, val right: Float, val bottom: Float)`.
- Produces: `UiBitmapRenderer.aspectFit(source: SourceSize, target: FloatRect): FloatRect?`.
- Produces: `UiBitmapRenderer.centerCropSource(source: SourceSize, target: FloatRect): FloatRect?`.

- [ ] **Step 1: Write failing catalog tests**

Assert exact paths including `ui/home/background.webp`, `logo.webp`, `tower_platform.webp`, `button_play.webp`, all eight Home card paths and all required icon paths.

- [ ] **Step 2: Write failing geometry tests**

Assert:
- 1000x500 source aspect-fit into 300x300 yields 300x150 centered vertically.
- 500x1000 source aspect-fit into 300x300 yields 150x300 centered horizontally.
- 1000x500 source center-crop into 300x600 selects a source rectangle with target aspect ratio and within source bounds.
- zero/non-positive source dimensions return `null` rather than divide by zero.

- [ ] **Step 3: Run tests and verify RED**

Run: `gradle --no-daemon :app:testDebugUnitTest --tests 'com.elxvro.skytower.ui.assets.*'`

Expected: FAIL because catalog/math types do not exist.

- [ ] **Step 4: Implement minimal catalog and pure math**

Add Canvas helpers `drawAspectFit(canvas, bitmap, target, paint)` and `drawCenterCrop(canvas, bitmap, target, paint)`. No file decoding in this class.

- [ ] **Step 5: Run tests and verify GREEN**

Same command; expected Task 1 tests PASS.

- [ ] **Step 6: Commit**

Commit: `feat: add replaceable UI asset catalog and scaling math`

---

### Task 2: Cached, failure-safe asset loading

**Files:**
- Create: `app/src/main/java/com/elxvro/skytower/ui/assets/UiAssetCache.kt`
- Create: `app/src/main/java/com/elxvro/skytower/ui/assets/UiAssetLoader.kt`
- Test: `app/src/test/java/com/elxvro/skytower/ui/assets/UiAssetCacheTest.kt`

**Interfaces:**
- Produces: `class UiAssetCache<T : Any>(private val decoder: (String) -> T?)`.
- Produces: `fun get(path: String): T?` and `fun clear()`.
- Produces: `class UiAssetLoader(assetManager: AssetManager)`.
- Produces: `fun bitmap(path: String): Bitmap?` and `fun clear()`.
- `UiAssetLoader` composes `UiAssetCache<Bitmap>` and owns Android decoding only.

- [ ] **Step 1: Write failing pure cache tests**

Use `UiAssetCache<String>` and assert:
- first `get(path)` decodes once;
- repeated request returns cached value without another decode;
- missing path (`decoder -> null`) is negative-cached;
- decoder exception is caught and exposed as `null`;
- `clear()` removes positive and negative cache so a later request decodes again.

- [ ] **Step 2: Run cache test and verify RED**

Run: `gradle --no-daemon :app:testDebugUnitTest --tests 'com.elxvro.skytower.ui.assets.UiAssetCacheTest'`

Expected: FAIL because cache class does not exist.

- [ ] **Step 3: Implement minimal pure cache**

Use one map for successful values and one set for known misses. Decoder exceptions count as misses.

- [ ] **Step 4: Implement Android adapter**

`UiAssetLoader` opens via `AssetManager.open(path)`, decodes with `BitmapFactory.decodeStream`, closes the stream, and returns `null` for missing/corrupt assets. Do not add Robolectric or mocking libraries.

- [ ] **Step 5: Run cache tests plus Android compile/lint**

Run: `gradle --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug`

Expected: pure cache tests PASS and Android adapter compiles/lints.

- [ ] **Step 6: Commit**

Commit: `feat: add cached UI asset loader`

---

### Task 3: Freeze Home geometry before visual migration

**Files:**
- Create: `app/src/main/java/com/elxvro/skytower/ui/assets/HomeAssetLayout.kt`
- Test: `app/src/test/java/com/elxvro/skytower/ui/assets/HomeAssetLayoutTest.kt`
- Modify: `app/src/main/java/com/elxvro/skytower/ui/screens/HomeScreen.kt`

**Interfaces:**
- Produces pure reference rectangles for daily, best, tower, play, and six menu tiles.
- Produces `fun menuTile(index: Int): FloatRect?`.

- [ ] **Step 1: Write failing reference-geometry test**

Pin current Home values exactly:
- daily `(45,330,235,170)`
- best `(800,330,235,170)`
- tower `(220,520,640,445)`
- play `(205,1035,670,155)`
- tile 0 `(58,1230,300,170)`
- horizontal stride `338`
- vertical stride `205`
- exactly six tiles; invalid indices return `null`.

- [ ] **Step 2: Run and verify RED**

Run: `gradle --no-daemon :app:testDebugUnitTest --tests 'com.elxvro.skytower.ui.assets.HomeAssetLayoutTest'`

- [ ] **Step 3: Implement layout and make HomeScreen consume it**

Move only rectangle definitions; do not change destinations or `ScreenAction` mappings.

- [ ] **Step 4: Run all unit tests**

Run: `gradle --no-daemon :app:testDebugUnitTest`

Expected: all tests PASS.

- [ ] **Step 5: Commit**

Commit: `refactor: freeze Home visual and touch geometry`

---

### Task 4: Add user-replaceable Home asset pack and guide

**Files:**
- Create binary starter files under `app/src/main/assets/ui/home/` and `app/src/main/assets/ui/icons/` using exact catalog names.
- Create: `docs/visual-assets/SkyTower-Home-Asset-Guide.md`

**Interfaces:**
- Produces files the user can replace without Kotlin changes.

- [ ] **Step 1: Add valid starter assets**

Required Home files:
- `background.webp` — 1080x1920 recommended.
- `logo.webp` — 760x320 transparent recommended.
- `tower_platform.webp` — 700x260 transparent recommended.
- `button_play.webp` — 680x180 recommended.
- `card_daily.webp`, `card_best_score.webp` — 360x240 recommended.
- `card_themes.webp`, `card_tasks.webp`, `card_level.webp`, `card_skins.webp`, `card_powerups.webp`, `card_settings.webp` — 320x210 recommended.

Required icons use exact Task 1 paths; 256x256 transparent recommended.

Starter files must be valid decodable assets and visually safe. They are not the final art direction; the user will replace them.

- [ ] **Step 2: Write user guide**

For every asset document exact filename, recommended size, transparency, whether text should be baked in (default: no for dynamic content), background safe area (central 900x1680), and replacement workflow.

- [ ] **Step 3: Verify packaged paths, not only compilation**

Run:
`gradle --no-daemon :app:assembleDebug`

Then:
`unzip -l app/build/outputs/apk/debug/app-debug.apk | grep 'assets/ui/home/background.webp'`
`unzip -l app/build/outputs/apk/debug/app-debug.apk | grep 'assets/ui/icons/'`

Expected: required asset paths are present inside APK.

- [ ] **Step 4: Commit**

Commit: `assets: add replaceable Home visual pack and guide`

---

### Task 5: Migrate Home rendering to assets with Canvas fallbacks

**Files:**
- Create: `app/src/main/java/com/elxvro/skytower/ui/assets/HomeAssetSlots.kt`
- Test: `app/src/test/java/com/elxvro/skytower/ui/assets/HomeAssetSlotsTest.kt`
- Modify: `app/src/main/java/com/elxvro/skytower/ui/screens/HomeScreen.kt`
- Modify: `app/src/main/java/com/elxvro/skytower/ui/SkyTowerV05View.kt`
- Modify: `README.md`

**Interfaces:**
- Consumes `UiAssetLoader.bitmap(path)`, `UiBitmapRenderer`, `HomeAssetLayout` and catalog keys.
- `HomeAssetSlots` maps each replaceable Home visual slot to exactly one asset path; dynamic values have no image slot.
- HomeScreen constructor becomes `HomeScreen(kit: SkyVisualKit, assets: UiAssetLoader, bitmapRenderer: UiBitmapRenderer = UiBitmapRenderer(), stackRenderer: ReferenceStackRenderer = ...)`.
- `SkyTowerV05View` creates exactly one `UiAssetLoader(context.assets)` and injects it into Home.
- `release()` calls `uiAssets.clear()`.

- [ ] **Step 1: Write failing slot mapping test**

Assert every Home replaceable visual maps to exactly one stable catalog path, and coin count / score / level / XP are absent from slots.

- [ ] **Step 2: Run and verify RED**

Run: `gradle --no-daemon :app:testDebugUnitTest --tests 'com.elxvro.skytower.ui.assets.HomeAssetSlotsTest'`

- [ ] **Step 3: Implement slot mapping**

No rendering logic in this pure file.

- [ ] **Step 4: Migrate background/logo/tower/play drawing**

If asset exists, draw with correct aspect rule; otherwise run existing Canvas fallback (`drawSky`, `drawLogo`, `ReferenceStackRenderer`, `drawButton`). Do not move rectangles.

- [ ] **Step 5: Migrate cards and icons**

Asset owns shell/icon appearance; labels and all live values remain code-rendered on top. Missing asset uses existing Canvas fallback.

- [ ] **Step 6: Wire one loader in SkyTowerV05View**

No per-frame/per-draw loader creation. Clear cache only on `release()`.

- [ ] **Step 7: Run complete verification**

Run: `gradle --no-daemon testDebugUnitTest lintDebug assembleDebug assembleRelease bundleRelease`

Expected: exit 0 and all artifacts produced.

- [ ] **Step 8: Prove art swap requires no Kotlin change**

Replace only `ui/home/background.webp` with a clearly different test bitmap, rebuild, confirm `git diff -- '*.kt'` is empty and packaged `background.webp` hash changes; restore starter asset afterward.

- [ ] **Step 9: Update README**

Add a short “Changing visuals” section linking `docs/visual-assets/SkyTower-Home-Asset-Guide.md`.

- [ ] **Step 10: Commit**

Commit: `feat: make SkyTower Home visuals user replaceable`

---

### Task 6: Final CI and deliverable verification

**Files:**
- No product-code changes unless verification exposes a defect.

**Interfaces:**
- Consumes final feature-branch HEAD.
- Produces verified development APK/artifact; does not merge PR.

- [ ] **Step 1: Read GitHub Actions run for exact final SHA**

Workflow command remains:
`gradle --no-daemon --stacktrace testDebugUnitTest lintDebug assembleDebug assembleRelease bundleRelease`

- [ ] **Step 2: Require every relevant step green**

Unit tests, lint, debug APK, release APK, release AAB, artifact collection and upload must conclude `success`.

- [ ] **Step 3: Download artifact and verify archive integrity**

ZIP must open without errors and include the debug APK plus Home asset paths.

- [ ] **Step 4: Do not merge PR**

Keep `feat/skytower-v0.1` open against `main`; integration remains a separate user decision.
