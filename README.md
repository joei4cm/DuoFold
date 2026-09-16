# DuoFold

Independent foldable Android launcher for **Xiaomi 18 Fold / HyperOS 4**.

**v0.5.0** — original clean-room implementation aiming at modern foldable continuity (inspired by public iPhone Duo experiences; unaffiliated).

Package: `com.duofold.launcher`. Free MIT. No activation server.

## Features

- 4×6 Home grid, pager, frosted vertical dock
- Leading canvas when unfolded (non-cover + wide)
- Hinge soft leaf wipe (optional)
- Cover landscape **StandBy suite** (clock / canvas / glances)
- Long-press **edit mode** with finger-follow drag between grid, leading pane, and dock
- Native AppWidget host + first-party glance cards
- Optional **Assist**: shade gestures + soft nav (Accessibility global actions only)
- Opt-in LockSurface after system unlock (does not replace Keyguard)
- Local layout persistence

Vision & roadmap: [docs/vision-iphone-duo.md](docs/vision-iphone-duo.md)

## Install (signed release)

```sh
./scripts/generate-release-keystore.sh   # once
./scripts/release-signed.sh
# app/build/outputs/apk/release/app-release.apk
```

## Docs

- [Architecture](docs/architecture.md)
- [Signing](docs/signing.md)
- [Privacy](PRIVACY.md)
- [Design notes](docs/design-notes.md)

## License

[MIT](LICENSE) — DuoFold contributors. See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
