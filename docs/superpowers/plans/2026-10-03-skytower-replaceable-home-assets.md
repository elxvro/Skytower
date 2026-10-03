# SkyTower Replaceable Home Assets Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the SkyTower Home screen visually replaceable by swapping image files, without changing Kotlin/Canvas code, while preserving all current gameplay data and touch behavior.

**Architecture:** Add one cached asset-loading path (`UiAssetCatalog` + `UiAssetLoader`) and one bitmap drawing helper (`UiBitmapRenderer`). Migrate only `HomeScreen` in this phase: layout/touch rectangles and live text remain code-owned; background, logo, tower/platform, card shells, icons and play-button art become asset-owned with safe Canvas fallbacks.

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

1. **Corrupt or unreadable asset:** loader returns `null` and Home falls back to current Canvas rendering without a crash; tested in Task 2.
2. **Very tall / very wide device:** center-crop fully covers background and aspect-fit never distorts foreground assets; tested in Task 1.
3. **Asset with extreme aspect ratio:** destination math stays inside the target rectangle for aspect-fit; tested in Task 1.
4. **Repeated frame rendering:** same asset path is decoded once and served from cache; tested in Task 2.
5. **Visual replacement changing taps:** Home menu/play/daily hit rectangles remain numerically identical to the current 1080x1920 geometry; tested in Task 3.

---

## File Structure

**Create**
- `app/src/main/java/com/elxvro/skytower/ui/assets/UiAssetCatalog.kt` — canonical stable asset keys/paths.
- `app/src/main/java/com/elxvro/skytower/ui/assets/UiAssetLoader.kt` — cached asset decode and safe missing/invalid handling.
- `app/src/main/java/com/elxvro/skytower/ui/assets/UiBitmapRenderer.kt` — center-crop/aspect-fit math and Canvas bitmap drawing.
- `app/src/main/java/com/elxvro/skytower/ui/assets/HomeAssetLayout.kt` — current Home reference rectangles/touch geometry in one pure layout contract.
- `app/src/test/java/com/elxvro/skytower/ui/assets/UiAssetCatalogTest.kt`
- `app/src/test/java/com/elxvro/skytower/ui/assets/UiBitmapRendererMathTest.kt`
- `app/src/test/java/com/elxvro/skytower/ui/assets/UiAssetLoaderTest.kt`
- `app/src/test/java/com/elxvro/skytower/ui/assets/HomeAssetLayoutTest.kt`
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
- Produces: `UiBitmapRenderer.aspectFit(source: SourceSize, target: FloatRect): FloatRect`.
- Produces: `UiBitmapRenderer.centerCropSource(source: SourceSize, target: FloatRect): FloatRect`.

- [ ] **Step 1: Write failing catalog tests**

Assert exact paths including:
- `HOME_BACKGROUND == "ui/home/background.webp"`
- `HOME_LOGO == "ui/home/logo.webp"`
- `HOME_TOWER_PLATFORM == "ui/home/tower_platform.webp"`
- `HOME_BUTTON_PLAY == "ui/home/button_play.webp"`
- card paths for daily, best-score, themes, tasks, level, skins, powerups, settings.
- icon paths for coin, crown, gift, trophy, themes, tasks, level, skins, powerups, settings.

- [ ] **Step 2: Write failing geometry tests**

Tests must assert:
- 1000x500 source aspect-fit into 300x300 target yields 300x150 centered vertically.
- 500x1000 source aspect-fit into 300x300 yields 150x300 centered horizontally.
- 1000x500 source center-crop into 300x600 selects a source rectangle that has target aspect ratio and stays within source bounds.
- zero/non-positive source dimensions return no drawable rectangle rather than dividing by zero.

- [ ] **Step 3: Run tests and verify RED**

Run:
`gradle --no-daemon :app:testDebugUnitTest --tests 'com.elxvro.skytower.ui.assets.*'`

Expected: FAIL because the new catalog/math types do not exist.

- [ ] **Step 4: Implement minimal catalog and pure math**

Implement only the signatures above plus Canvas draw helpers:
- `drawAspectFit(canvas: Canvas, bitmap: Bitmap, target: RectF, paint: Paint? = null)`
- `drawCenterCrop(canvas: Canvas, bitmap: Bitmap, target: RectF, paint: Paint? = null)`

No file decoding in this class.

- [ ] **Step 5: Run tests and verify GREEN**

Same command; expected all new Task 1 tests PASS.

- [ ] **Step 6: Commit**

Commit message: `feat: add replaceable UI asset catalog and scaling math`

---

### Task 2: Cached, failure-safe asset loader

**Files:**
- Create: `app/src/main/java/com/elxvro/skytower/ui/assets/UiAssetLoader.kt`
- Test: `app/src/test/java/com/elxvro/skytower/ui/assets/UiAssetLoaderTest.kt`

**Interfaces:**
- Consumes: stable string paths from `UiAssetCatalog`.
- Produces: `interface UiAssetSource { fun decode(path: String): Bitmap? }`.
- Produces: `class AndroidUiAssetSource(assetManager: AssetManager) : UiAssetSource`.
- Produces: `class UiAssetLoader(source: UiAssetSource)`.
- Produces: `fun bitmap(path: String): Bitmap?`.
- Produces: `fun clear()`.

- [ ] **Step 1: Write failing cache/failure tests**

Using a fake `UiAssetSource`, assert:
- first `bitmap(path)` decodes once;
- repeated `bitmap(path)` returns cached instance without another decode;
- missing path returns `null` and is negative-cached so it is not decoded every frame;
- source exception is caught and exposed as `null`;
- `clear()` empties positive and negative cache so a later request decodes again.

- [ ] **Step 2: Run loader test and verify RED**

Run:
`gradle --no-daemon :app:testDebugUnitTest --tests 'com.elxvro.skytower.ui.assets.UiAssetLoaderTest'`

Expected: FAIL because loader/source classes do not exist.

- [ ] **Step 3: Implement minimal loader**

`AndroidUiAssetSource.decode(path)` opens via `AssetManager.open(path)`, decodes with `BitmapFactory.decodeStream`, closes the stream, and returns `null` for missing/corrupt assets. `UiAssetLoader` owns cache policy only.

- [ ] **Step 4: Run loader test and verify GREEN**

Same command; expected PASS.

- [ ] **Step 5: Commit**

Commit message: `feat: add cached UI asset loader`

---

### Task 3: Freeze Home geometry before visual migration

**Files:**
- Create: `app/src/main/java/com/elxvro/skytower/ui/assets/HomeAssetLayout.kt`
- Test: `app/src/test/java/com/elxvro/skytower/ui/assets/HomeAssetLayoutTest.kt`
- Modify: `app/src/main/java/com/elxvro/skytower/ui/screens/HomeScreen.kt`

**Interfaces:**
- Consumes: existing 1080x1920 `ScreenLayout` scaling convention.
- Produces: pure reference rectangles for `daily`, `best`, `tower`, `play`, and six menu tiles.
- Produces: `fun menuTile(index: Int): FloatRect` with exactly 6 valid indices.

- [ ] **Step 1: Write failing reference-geometry test**

Pin current Home reference values exactly:
- daily `(45,330,235,170)`
- best `(800,330,235,170)`
- tower `(220,520,640,445)`
- play `(205,1035,670,155)`
- menu tile 0 `(58,1230,300,170)`
- horizontal tile stride `338`
- vertical tile stride `205`
- six tiles total.

Also assert invalid menu indices return `null` rather than creating an off-screen hit target.

- [ ] **Step 2: Run geometry test and verify RED**

Run:
`gradle --no-daemon :app:testDebugUnitTest --tests 'com.elxvro.skytower.ui.assets.HomeAssetLayoutTest'`

Expected: FAIL because `HomeAssetLayout` does not exist.

- [ ] **Step 3: Implement `HomeAssetLayout` and make HomeScreen consume it**

Move only rectangle definitions; do not change current destinations or `ScreenAction` mappings.

- [ ] **Step 4: Run all unit tests and verify GREEN**

Run:
`gradle --no-daemon :app:testDebugUnitTest`

Expected: all existing and new tests PASS.

- [ ] **Step 5: Commit**

Commit message: `refactor: freeze Home visual and touch geometry`

---

### Task 4: Add the user-replaceable Home asset pack and guide

**Files:**
- Create binary starter files under `app/src/main/assets/ui/home/` and `app/src/main/assets/ui/icons/` using the exact catalog names.
- Create: `docs/visual-assets/SkyTower-Home-Asset-Guide.md`

**Interfaces:**
- Consumes: exact paths from `UiAssetCatalog`.
- Produces: files the user can replace without Kotlin changes.

- [ ] **Step 1: Add starter asset files with exact stable names**

Required Home filenames:
- `background.webp` — recommended 1080x1920.
- `logo.webp` — recommended 760x320 transparent.
- `tower_platform.webp` — recommended 700x260 transparent.
- `button_play.webp` — recommended 680x180.
- `card_daily.webp`, `card_best_score.webp` — recommended 360x240.
- six menu cards — recommended 320x210 each.

Required icon filenames are the exact paths in Task 1; recommended 256x256 transparent.

Starter files may be neutral placeholders because the user will choose final art, but they must be valid decodable WebP/PNG content and visually safe if not replaced.

- [ ] **Step 2: Write the user guide**

Document for every asset:
- exact filename;
- recommended source size;
- transparency expectation;
- whether text should be baked in (default: no for dynamic content);
- safe area for `background.webp` (central 900x1680 of 1080x1920);
- replacement workflow: replace file, keep name, commit/push, rebuild APK.

Include a compact table the user can follow without reading Kotlin.

- [ ] **Step 3: Verify package visibility**

Run:
`gradle --no-daemon :app:assembleDebug`

Expected: build succeeds and generated APK contains `assets/ui/home/` and `assets/ui/icons/` entries.

- [ ] **Step 4: Commit**

Commit message: `assets: add replaceable Home visual pack and guide`

---

### Task 5: Migrate Home rendering to assets with Canvas fallbacks

**Files:**
- Modify: `app/src/main/java/com/elxvro/skytower/ui/screens/HomeScreen.kt`
- Modify: `app/src/main/java/com/elxvro/skytower/ui/SkyTowerV05View.kt`
- Modify: `README.md`

**Interfaces:**
- Consumes: `UiAssetLoader.bitmap(path)`, `UiBitmapRenderer`, `HomeAssetLayout` and catalog keys.
- HomeScreen constructor becomes `HomeScreen(kit: SkyVisualKit, assets: UiAssetLoader, bitmapRenderer: UiBitmapRenderer = UiBitmapRenderer(), stackRenderer: ReferenceStackRenderer = ...)`.
- `SkyTowerV05View` creates exactly one `UiAssetLoader(AndroidUiAssetSource(context.assets))` and injects it into HomeScreen.
- `release()` calls `uiAssets.clear()`.

- [ ] **Step 1: Add a Home asset-selection unit seam**

Extract a pure helper/state decision that maps each Home visual slot to its catalog path and fallback identity. Test that all required slots map to exactly one stable asset path and no dynamic value is represented as an image slot.

- [ ] **Step 2: Run the new mapping test and verify RED**

Expected: FAIL before the helper exists.

- [ ] **Step 3: Migrate background/logo/tower/play drawing**

For each slot:
- if bitmap exists, draw via the correct aspect rule;
- otherwise execute the existing Canvas fallback (`drawSky`, `drawLogo`, `ReferenceStackRenderer`, `drawButton`).

Do not move any rectangles.

- [ ] **Step 4: Migrate cards and icons**

Card shell and icon use assets when available. Keep labels, coin/score/level values and state text code-rendered on top. If a card/icon bitmap is missing, retain current Canvas drawing.

- [ ] **Step 5: Wire one shared loader in `SkyTowerV05View`**

Ensure no loader is created per frame or per draw call; clear cache only on `release()`.

- [ ] **Step 6: Run complete local verification**

Run:
`gradle --no-daemon testDebugUnitTest lintDebug assembleDebug assembleRelease bundleRelease`

Expected: exit 0; unit tests pass, lint has no blocking errors, debug APK/release APK/release AAB are produced.

- [ ] **Step 7: Verify swap behavior**

Temporarily replace `ui/home/background.webp` with a clearly different test bitmap, rebuild debug APK, confirm no Kotlin source changes are needed and the APK packages the changed asset; restore the starter background afterward.

- [ ] **Step 8: Update README**

Add one short “Changing visuals” section linking `docs/visual-assets/SkyTower-Home-Asset-Guide.md`.

- [ ] **Step 9: Commit**

Commit message: `feat: make SkyTower Home visuals user replaceable`

---

### Task 6: Final CI and deliverable verification

**Files:**
- No product-code changes unless verification exposes a defect.

**Interfaces:**
- Consumes final feature-branch HEAD.
- Produces verified development APK/artifact; does not merge PR.

- [ ] **Step 1: Push/commit final HEAD and read the GitHub Actions run for that exact SHA**

Required workflow command remains:
`gradle --no-daemon --stacktrace testDebugUnitTest lintDebug assembleDebug assembleRelease bundleRelease`

- [ ] **Step 2: Require all build steps green**

Verify unit tests, lint, debug APK, release APK, release AAB, artifact collection and artifact upload all conclude `success`.

- [ ] **Step 3: Download artifact and verify archive integrity**

Verify ZIP opens without errors and debug APK is present/installable-format.

- [ ] **Step 4: Do not merge PR**

Keep `feat/skytower-v0.1` open against `main`; integration remains a separate user decision.
