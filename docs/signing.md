# Release signing

DuoFold must **not** be distributed with the Android Studio debug keystore. Many HyperOS devices refuse or warn on debug-signed sideloads.

## One-time keystore

```sh
./scripts/generate-release-keystore.sh
```

Creates (outside the git tree by default):

- `$HOME/.local/share/duofold/duofold-release.jks`
- `$HOME/.local/share/duofold/keystore.env` (passwords + paths; mode `600`)

Never commit the `.jks` or `keystore.env`.

## Build a signed release APK

```sh
./scripts/release-signed.sh
```

Output: `app/build/outputs/apk/release/app-release.apk`

Environment variables (also loaded from `keystore.env`):

| Variable | Meaning |
|---|---|
| `DUOFOLD_RELEASE_STORE_FILE` | Absolute path to `.jks` (must be **outside** this repository) |
| `DUOFOLD_RELEASE_STORE_PASSWORD` | Keystore password |
| `DUOFOLD_RELEASE_KEY_ALIAS` | Key alias (`duofold`) |
| `DUOFOLD_RELEASE_KEY_PASSWORD` | Key password |

## Verify

```sh
apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
```

You should see your DuoFold release cert CN, not `CN=Android Debug`.
