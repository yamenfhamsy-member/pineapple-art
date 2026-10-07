<div align="center">

# 🍍 Pineapple Art

### باينابل آرت — محرر صور مستقل للأندرويد

*Edit photos with a fresh tropical touch — حرّر الصور بلمسة استوائية منعشة*

</div>

Pineapple Art is an independent **fork of [Image Toolbox](https://github.com/T8RIN/ImageToolbox)
(`3.9.0`) by T8RIN (Malik Mukhametzyanov)**, rebranded with its own identity
(`com.pineapple.art`), an Arabic-first experience, curated OFL fonts and a new
green/gold visual design. All upstream copyright and license notices are preserved.

> Upstream license: **Apache License 2.0** (see [LICENSE](./LICENSE)).
> This fork does not claim the upstream code as its own — see
> [Attribution](#attribution) below.

## Features

Everything from the upstream editor is preserved and works fully offline:

- Crop, resize, rotate, flip
- Filters and adjustments
- Drawing and erase tools
- Text markup layers (Arabic + Latin + Emoji, RTL/LTR/mixed bidi)
- Stickers, shapes, collage maker
- Background tools (erase / replace)
- EXIF/metadata view and edit
- Compression and format conversion
- Batch processing
- Export and share
- Undo / redo, markup projects (`.itp`)

Pineapple Art additions:

- 🍍 New brand: name, launcher icon, splash, green/gold theme
- 🌍 Arabic + English UI with real RTL layouts (`values-ar/strings.xml`)
- 🔤 Font picker with search, Arabic/Latin/imported/favorites categories,
  recently-used list and bilingual preview (`باينابل آرت • Pineapple Art`)
- 🔤 Bundled OFL fonts: Noto Sans Arabic, Noto Kufi Arabic, Noto Naskh Arabic,
  Noto Sans, Noto Serif (licenses in
  `core/resources/src/main/assets/fonts-licenses/`)
- 📦 Release builds target **arm64-v8a only** (small APK)

## Arabic, RTL and text editor

- Full Arabic translation of editor, settings, menus, dialogs, export/share,
  permissions, errors and empty states.
- RTL uses `start/end` alignment everywhere; the text engine maps alignment to
  `TextAlign.Start/Center/End`, so Arabic shaping, bidi, punctuation, numbers,
  Emoji and Arabic+Latin mixing render through the standard Android text stack —
  strings are never reversed manually.
- Text layers support: font family, size, bold/italic/underline, letter and line
  spacing, color + opacity, stroke (outline), shadow + blur, background +
  opacity + padding, move / scale / rotate, multiline with wrapping.
- Custom fonts: import `.ttf` / `.otf` from the font picker (validated on import).

## Fonts and licenses

| Font              | License | Redistribution |
|-------------------|---------|----------------|
| Noto Sans Arabic  | OFL 1.1 | Allowed — see `assets/fonts-licenses/OFL-notosansarabic.txt` |
| Noto Kufi Arabic  | OFL 1.1 | Allowed — see `assets/fonts-licenses/OFL-notokufiarabic.txt` |
| Noto Naskh Arabic | OFL 1.1 | Allowed — see `assets/fonts-licenses/OFL-notonaskharabic.txt` |
| Noto Sans         | OFL 1.1 | Allowed — see `assets/fonts-licenses/OFL-notosans.txt` |
| Noto Serif        | OFL 1.1 | Allowed — see `assets/fonts-licenses/OFL-notoserif.txt` |

Only add fonts whose license explicitly allows embedding and redistribution,
and always ship the license file with attribution.

## Architecture (upstream `3.9.0` base)

- Language: Kotlin `2.3.21`, JVM target `21`, AGP `9.2.1`
- `compileSdk 37`, `targetSdk 37`, `minSdk 24`
- Application ID: `com.pineapple.art` (code namespace kept as
  `com.t8rin.imagetoolbox` for source compatibility)
- Modules: `:app` + `feature/*` (~50 features) + `core/*` + `lib/*`
  (see `settings.gradle.kts`)
- Variants: flavors `foss` / `market`, build types `debug` / `release` /
  `benchmark`, ABI splits limited to `arm64-v8a` (+ universal APK)
- Text editing lives in `feature/markup-layers` (extended, not duplicated)
- No backend, no analytics, no ads, no telemetry — editing is local-first

## Build

Requirements: JDK 21, Android SDK (platform 37, build-tools), ~8 GB RAM.

```bash
# Debug (no secrets needed)
./gradlew assembleFossDebug

# Release APK (arm64-v8a) — needs signing, see below
./gradlew assembleFossRelease

# App Bundle
./gradlew bundleFossRelease
```

Outputs:

- `app/build/outputs/apk/foss/debug/*.apk`
- `app/build/outputs/apk/foss/release/*.apk` (arm64-v8a)
- `app/build/outputs/bundle/fossRelease/*.aab`

### Signing a release locally

Never commit keystores or passwords. Provide them via environment:

```bash
export PINEAPPLE_KEYSTORE=/path/to/pineapple-art.jks
export PINEAPPLE_STORE_PASSWORD=...
export PINEAPPLE_KEY_ALIAS=pineapple-art
export PINEAPPLE_KEY_PASSWORD=...
./gradlew assembleFossRelease
```

Generate a key once with:

```bash
keytool -genkeypair -v -keystore pineapple-art.jks -alias pineapple-art \
  -keyalg RSA -keysize 4096 -validity 9125
```

## GitHub Actions

| Workflow | Trigger | Does |
|----------|---------|------|
| `.github/workflows/android.yml` | push / PR / manual | JDK 21, cache, lint, unit tests, `assembleFossDebug`, upload `pineapple-art-debug-apk` |
| `.github/workflows/release.yml` | tag `v*` | signed `assembleFossRelease` + `bundleFossRelease`, attach APK/AAB to the GitHub Release |

Signed CI builds use repository secrets (never committed):

- `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`,
  `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`

## Releases

- Tags like `v1.0.0` trigger the release workflow, which builds the signed
  arm64-v8a APK and the AAB and attaches them to the GitHub Release.

## License

- App code: **Apache License 2.0** — see [LICENSE](./LICENSE).
- Noto fonts: **SIL Open Font License 1.1** — see
  `core/resources/src/main/assets/fonts-licenses/`.

## Attribution

- Upstream project: **Image Toolbox** by **T8RIN (Malik Mukhametzyanov)**,
  Apache-2.0, https://github.com/T8RIN/ImageToolbox
- Noto fonts: **The Noto Project Authors**, OFL-1.1,
  https://github.com/notofonts / https://fonts.google.com
- Pineapple Art launcher/splash artwork is original to this fork
  (green + gold + white) and is not a copy of the upstream logo.
