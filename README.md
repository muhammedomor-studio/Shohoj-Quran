# Shohoj Quran (সহজ কোরআন) — Android starter v1

A Java + Android SDK project intended to build with GitHub Actions. The current starter includes:
- Dark/light theme toggle saved locally
- A 114-surah metadata list (names and verse counts)
- Basic home, surah list, settings screens
- GitHub Actions workflow to build a debug APK artifact

## Important status note
This ZIP is a **starter scaffold, not a complete Quran app**. It does not yet contain all Arabic ayah text, Bengali translation, Mishary Al-Afasy audio, Arabic Quran font assets, or a working data downloader. The surah list is metadata only. Do not publish this build as a complete Quran app.

## Build on GitHub Actions
1. Create a GitHub repository, for example `shohoj-quran`.
2. Upload the contents of this ZIP to the repository root (not the enclosing folder).
3. Push to `main` or `master`, or go to **Actions → Build Shohoj Quran APK → Run workflow**.
4. Download the `shohoj-quran-debug-apk` artifact from the completed workflow run.

The workflow uses JDK 17 and Gradle 8.9. It builds a debug APK for testing, not a signed release APK.

## Final architecture requirement (data-only updates)
To support adding future data without rebuilding the app, the next code milestone must add a versioned manifest/data downloader that reads files from a fixed raw URL in your repository, validates SHA-256, downloads to app-specific persistent storage, and reads the downloaded data locally. Android app-specific files persist until app data is cleared or the app is uninstalled; see Android docs: https://developer.android.com/training/data-storage/app-specific

Once that downloader is implemented and tested, future data packages can be added to the repository without changing the APK, as long as the JSON schema and manifest remain compatible. Audio hosting bandwidth and GitHub file-size limits must be considered.

## Suggested data schema
`data/manifest.json` should include a schema version and package metadata. Every downloadable file should have a stable path, size, SHA-256 checksum, and source/licence note. Arabic text must be verified against a trusted Quran source; Bengali translation and recitation redistribution permissions must be checked before inclusion.

## Data sources / rights
- Arabic text source candidate: Tanzil (follow its text licence and attribution requirements): https://tanzil.net/download/ and https://tanzil.net/docs/Text_License
- Recitation/translation sources must be checked individually for redistribution rights. A public audio URL does not automatically grant permission to re-host it.

## App identity
Display name: সহজ কোরআন
English name: Shohoj Quran
Package ID currently: `com.shohojquran.app`
Version: `1.0.0` (starter scaffold)
