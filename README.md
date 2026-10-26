# Shohoj Quran — connected Android project

Package: `com.shohojquran.app`

## Separate app and data repositories
- App source repository: https://github.com/muhammedomor-studio/Shohoj-Quran
- Data repository (create/upload separately): https://github.com/muhammedomor-studio/Shohoj-Quran-Data
- Fixed manifest URL: https://raw.githubusercontent.com/muhammedomor-studio/Shohoj-Quran-Data/main/manifest.json

The app includes a downloader that reads `manifest.json`, fetches `quran-ar.json`, checks its schema (114 surahs, 6,236 non-empty ayahs), and stores it in app-private storage for offline reading. Settings exposes the download/update action. This is a code integration; the separate data repo must contain a complete, licensed and verified Quran text before download succeeds.

## Current limitations
- The data ZIP scaffold does not yet include the complete Arabic ayah dataset.
- Bengali translation and recitation audio are not included; add only sources whose redistribution license/permission is confirmed, and implement their download/read paths separately.
- The Android build has not been executed in this environment. Run GitHub Actions to verify compilation before release.

## Build
Push this project to the app repository and run the `Android APK Build` workflow under Actions.
