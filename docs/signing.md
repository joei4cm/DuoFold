# Release signing

DuoFold must **not** be distributed with the Android Studio debug keystore.

## One-time keystore (local)

```sh
./scripts/generate-release-keystore.sh
```

Creates (outside the git tree by default):

- `$HOME/.local/share/duofold/duofold-release.jks`
- `$HOME/.local/share/duofold/keystore.env` (passwords + paths; mode `600`)

Never commit the `.jks` or `keystore.env`.

## Build a signed release APK (local)

```sh
./scripts/release-signed.sh
```

Output: `app/build/outputs/apk/release/app-release.apk`

| Variable | Meaning |
|---|---|
| `DUOFOLD_RELEASE_STORE_FILE` | Absolute path to `.jks` (must be **outside** this repository) |
| `DUOFOLD_RELEASE_STORE_PASSWORD` | Keystore password |
| `DUOFOLD_RELEASE_KEY_ALIAS` | Key alias (`duofold`) |
| `DUOFOLD_RELEASE_KEY_PASSWORD` | Key password |

## GitHub Actions secrets (CI release)

The `release` job on `main` / `workflow_dispatch` builds a **signed** APK. Add these repository secrets:

| Secret | Value |
|---|---|
| `DUOFOLD_RELEASE_KEYSTORE_BASE64` | Base64 of the `.jks` file (`base64 -w0 duofold-release.jks`) |
| `DUOFOLD_RELEASE_STORE_PASSWORD` | Keystore password |
| `DUOFOLD_RELEASE_KEY_ALIAS` | Key alias (e.g. `duofold`) |
| `DUOFOLD_RELEASE_KEY_PASSWORD` | Key password |

Encode example:

```sh
base64 -w0 "$HOME/.local/share/duofold/duofold-release.jks" | pbcopy   # macOS
# or
base64 -w0 "$HOME/.local/share/duofold/duofold-release.jks"            # Linux: paste into the secret
```

Download the artifact **DuoFold-release** from the Actions run.

## Verify

```sh
apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
```

You should see your DuoFold release cert CN, not `CN=Android Debug`.
