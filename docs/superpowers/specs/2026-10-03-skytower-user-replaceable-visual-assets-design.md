# SkyTower User-Replaceable Visual Assets Design

Date: 2026-10-03
Status: Approved design, awaiting written-spec review
Branch: feat/skytower-v0.1

## 1. Goal

Make SkyTower's visual presentation user-replaceable without requiring Kotlin/Canvas changes for normal art swaps.

The user controls the visual identity by replacing image files. The application code remains responsible for layout, touch targets, text, live values, responsive scaling, and gameplay state.

Core rule:

> Code owns behavior and geometry. Asset files own appearance.

## 2. Scope

This system covers the visual parts that benefit from manual art replacement:

- Home background
- Logo
- Tower/platform art
- Menu card backgrounds
- Menu icons
- Play button background
- Coin/crown/gift/trophy and other reusable icons
- Classic block faces
- Theme-preview art
- Skin-preview art
- Optional decorative clouds/islands if supplied as assets later

This change does not move dynamic values into images.

Dynamic content remains code-rendered:

- Coin count
- Score / best score
- Level number
- XP progress
- Mission progress
- Prices
- Inventory counts
- Button labels that change by state
- Locked / owned / selected state

## 3. Asset Architecture

Assets live under:

```text
app/src/main/assets/ui/
```

Primary structure:

```text
ui/
  home/
    background.webp
    logo.webp
    tower_platform.webp
    card_daily.webp
    card_best_score.webp
    card_themes.webp
    card_tasks.webp
    card_level.webp
    card_skins.webp
    card_powerups.webp
    card_settings.webp
    button_play.webp

  icons/
    coin.webp
    crown.webp
    gift.webp
    trophy.webp
    themes.webp
    tasks.webp
    level.webp
    skins.webp
    powerups.webp
    settings.webp
    back.webp
    lock.webp
    check.webp

  blocks/
    classic_blue.webp
    classic_brown.webp
    classic_green.webp
    classic_yellow.webp
    classic_pink.webp
    classic_cream.webp

  common/
    panel.webp
    header_ribbon.webp
    button_primary.webp
    button_secondary.webp
    progress_track.webp
    progress_fill.webp
```

Additional screen-specific folders may be added later using the same rule rather than embedding new visuals back into Canvas code.

## 4. Naming Contract

Asset names are stable API-like identifiers.

The user may replace the file contents but should keep the filename unchanged.

Examples:

- `home/background.webp`
- `home/card_themes.webp`
- `icons/themes.webp`
- `blocks/classic_blue.webp`

Changing the filename requires code/config changes and is not part of the normal art-swap workflow.

## 5. Recommended Source Dimensions

The application scales images responsively from a 1080 x 1920 reference canvas.

Recommended master dimensions:

| Asset | Master size | Notes |
| --- | ---: | --- |
| `home/background.webp` | 1080x1920 | Full-screen portrait artwork |
| `home/logo.webp` | 760x320 | Transparent background |
| `home/tower_platform.webp` | 700x260 | Transparent background |
| `home/button_play.webp` | 680x180 | Text can remain code-rendered |
| Home menu cards | 320x210 | Background only, transparent or opaque |
| Large utility cards | 360x240 | Daily / best score |
| Reusable icons | 256x256 | Transparent background |
| Block faces | 512x160 | Horizontal block texture/face |
| Common header ribbon | 760x170 | Transparent background |
| Generic panel | 1000x1200 | 9-slice-like stretch is not assumed; normal scaled bitmap |

These are source recommendations, not hard pixel requirements. Runtime rendering uses destination rectangles and aspect-fit/crop rules.

## 6. Rendering Rules

### 6.1 Background

`home/background.webp` uses center-crop to fill the visible app area.

- No distortion.
- Extra top/bottom or left/right can crop depending on aspect ratio.
- Safe content should stay within the central 900x1680 area of the 1080x1920 master.

### 6.2 Logo and icons

Use aspect-fit inside a fixed destination rectangle.

- Never stretch non-uniformly.
- Transparent WebP is preferred.

### 6.3 Card backgrounds

Cards use fixed responsive destination rectangles defined by the layout system.

The art file provides appearance only.

Code overlays:

- icon when the card art does not already include one
- label
- numeric state
- badge/lock/selection state

The default system will support either:

1. background-only card art + separate icon, or
2. card art containing decorative/icon elements while dynamic text remains code-driven.

### 6.4 Blocks

Each classic block asset is drawn into the existing gameplay block rectangle using aspect-fill.

Gameplay collision geometry does not change with the image.

## 7. Asset Loader

Introduce one shared loader/cache rather than decoding the same image every frame.

Responsibilities:

- Load by stable asset key.
- Decode once and cache `Bitmap` instances.
- Reuse bitmaps during Canvas rendering.
- Gracefully fall back to code-drawn placeholders when an asset is missing or invalid.
- Release cache when the view/application is destroyed if needed.

Proposed class boundary:

```text
UiAssetCatalog
UiAssetLoader
UiBitmapRenderer
```

`UiAssetCatalog` owns canonical paths.

`UiAssetLoader` owns loading/caching.

`UiBitmapRenderer` owns aspect-fit / center-crop drawing helpers.

## 8. Fallback Behavior

The application must not crash because an optional image is missing.

Fallback priority:

1. Requested user asset.
2. Bundled fallback/default asset if one exists.
3. Existing flat Canvas placeholder.

For required structural assets such as the home background, CI tests verify that the expected path is present in the project package.

## 9. Configuration

A lightweight Kotlin asset catalog is preferred over user-editable JSON for v1 of this system.

Reason:

- Filenames are stable and predictable.
- Fewer runtime parsing failures.
- User only needs to replace image files.

No complex visual editor or in-app asset picker is added.

## 10. Responsive Layout

The existing 1080x1920 reference layout remains the coordinate source.

Rules:

- Scale uniformly to fit device width.
- Respect Android system bars / safe area.
- Preserve existing touch target geometry.
- Background uses center-crop.
- Foreground artwork uses aspect-fit unless explicitly configured otherwise.
- No asset controls its own click area.

This separation prevents a replaced image from breaking interactions.

## 11. Visual Ownership by Screen

### Home

User replaceable:

- background
- logo
- center tower/platform
- daily card art
- best-score card art
- six menu-card art files
- play-button art
- associated icons

Code owned:

- coin amount
- level
- XP bar
- score number
- all touch targets
- card labels if not baked into the image

### Other screens

The same pattern will be extended incrementally:

- screen background/header art may be replaceable
- item-card shells may be replaceable
- dynamic values remain code-rendered
- gameplay mechanics never depend on the bitmap dimensions

The first implementation target is Home because the user asked to design screens one-by-one.

## 12. User Workflow

Normal art update flow:

1. Create or export a new PNG/WebP using the recommended template size.
2. Rename it to the exact existing asset name.
3. Replace the matching file under `app/src/main/assets/ui/...`.
4. Commit/push.
5. GitHub Actions builds the APK.

No Kotlin edits are required for normal replacements.

## 13. Image Format

Preferred:

- WebP for backgrounds/cards.
- Transparent WebP or PNG for icons/logo.

Avoid:

- SVG unless explicitly introduced later.
- Huge 4K assets for simple cards.
- Text-heavy flattened images for dynamic content.

## 14. Performance

- No image decoding inside the frame loop.
- Bitmaps are cached.
- Assets are scaled at draw time using cached decoded bitmap data.
- Large background source size should remain reasonable; 1080x1920 is the default target.
- WebP is preferred to keep APK size under control.

## 15. Tests

Required tests/verification:

1. Catalog returns stable expected paths.
2. Missing optional asset does not crash the renderer.
3. Aspect-fit math preserves source ratio.
4. Center-crop math fully covers destination without distortion.
5. Home layout touch rectangles remain unchanged after visual asset migration.
6. Existing game/mission/economy tests remain green.
7. Android lint passes.
8. Debug APK and release APK/AAB build successfully.

## 16. Migration Strategy

Do not convert every screen in one risky rewrite.

Order:

1. Create shared asset infrastructure.
2. Migrate Home screen visuals.
3. Build/test on device.
4. Once Home is visually approved, migrate the next screen using the same infrastructure.

This matches the user's request to proceed screen-by-screen and lets the user choose each visual asset set.

## 17. Non-Goals

Not included in this phase:

- In-app image upload/editor.
- Downloading UI assets from a server.
- Remote theme packs.
- Runtime modding from shared storage.
- User account/cloud sync.
- Full redesign of gameplay logic.

## 18. Acceptance Criteria

This system is accepted when:

- Replacing `home/background.webp` changes the Home background without Kotlin changes.
- Replacing any home card image changes that card appearance without Kotlin changes.
- Replacing logo/icon/block files updates their visuals without changing gameplay logic.
- Dynamic score/coin/level/mission state remains functional.
- Images scale without distortion on common portrait Android sizes.
- Missing optional images fall back safely.
- Home touch targets remain correct.
- CI remains green and produces an installable debug APK.
