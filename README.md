# DuoFold

Independent foldable Android launcher for **Xiaomi 18 Fold / HyperOS 4**.

**v0.6.0** · package `com.duofold.launcher` · [MIT](LICENSE)

[中文说明](README.zh-CN.md)

DuoFold is an original Home app focused on fold continuity: cover ↔ inner layout, calm materials, StandBy-style cover mode, fluid editing, widgets, and optional HyperOS Assist gestures. It is **not affiliated with** Apple, Xiaomi, or other launcher products.

## Features

| Area | What you get |
|---|---|
| Home | 4×6 grid, horizontal pager, frosted vertical dock |
| Fold | Leading canvas when unfolded; optional hinge leaf wipe |
| StandBy | Cover landscape multi-page suite (clock / canvas / glances) |
| Editing | Long-press edit; finger-follow drag between grid, leading pane, and dock |
| Widgets | Native AppWidget host + first-party glance cards |
| Assist | Optional shade swipe + soft Back/Home/Recents (Accessibility global actions only) |
| LockSurface | Opt-in post-unlock glass clock (does **not** replace system Keyguard) |

Layout and preferences stay on device. No accounts, analytics, or activation server.

## Requirements

- **compile / target:** Android 17 (API 37)
- **minSdk:** 31 (Android 12+) — runs on older foldables while targeting Android 17
- JDK 17 for local builds
- Android SDK Platform 37 (+ Build-Tools 37)

## Build

Debug:

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

Signed release (keystore **outside** the repo):

```sh
./scripts/generate-release-keystore.sh   # once
./scripts/release-signed.sh
# → app/build/outputs/apk/release/app-release.apk
```

See [docs/signing.md](docs/signing.md).

## Install

1. Install the release APK.
2. Open DuoFold → **Set as Home** (or system Default apps).
3. Optional: Settings → shade gestures / soft nav → enable **DuoFold Assist** in Accessibility.

## Docs

- [Architecture](docs/architecture.md)
- [Vision / roadmap](docs/vision-iphone-duo.md)
- [User guide](docs/user-guide.md)
- [QA matrix](docs/qa-matrix.md)
- [Design notes](docs/design-notes.md)
- [Privacy](PRIVACY.md)
- [Third-party notices](THIRD_PARTY_NOTICES.md)

## CI

GitHub Actions on `main`:

1. Runs unit tests
2. Builds a **signed release APK** using repository secrets (see [docs/signing.md](docs/signing.md))
3. Uploads artifact **DuoFold-release**

Pull requests run unit tests only (no signing secrets required).

## License

[MIT](LICENSE) — DuoFold contributors.
