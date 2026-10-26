# সহজ কোরআন — Offline download update

Android Java project. Uses Al Quran Cloud API for Arabic Quran and Bengali translation, and the ar.alafasy surah audio CDN for recitation.

## New behavior
- First launch shows a Bengali prompt explaining that downloaded data can be read offline.
- User can start download or skip and download later from Settings.
- Settings includes separate text/translation download and full text+114 audio download.
- Downloaded surah JSON and MP3 files are saved in app-private internal storage; audio playback prefers local MP3 when present.
- Download progress is shown. If the connection fails, already downloaded files remain and the user can retry.

## Build
Upload the project contents to the root of your GitHub repository and run `.github/workflows/android-build.yml`.

## Important limitations
- The full-audio option can use substantial mobile data and storage; use Wi-Fi and check free space first. Android internal app storage is used, so uninstalling the app removes downloaded data.
- API/CDN availability, Bengali edition availability, and audio URLs depend on third-party services and may change. The app needs internet to download, but previously downloaded files can be used offline.
- Build has not been verified in this environment because Android SDK/Gradle build tooling is not available here. Verify the GitHub Actions build and test on a real device before publishing.
- Check the API/CDN terms and attribution requirements before public release.
