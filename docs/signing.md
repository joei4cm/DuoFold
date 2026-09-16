# Release signing & CI tags

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

Optional version override:

```sh
export DUOFOLD_VERSION_NAME=0.7.0
export DUOFOLD_VERSION_CODE=7000
./scripts/release-signed.sh
```

| Variable | Meaning |
|---|---|
| `DUOFOLD_RELEASE_STORE_FILE` | Absolute path to `.jks` (must be **outside** this repository) |
| `DUOFOLD_RELEASE_STORE_PASSWORD` | Keystore password |
| `DUOFOLD_RELEASE_KEY_ALIAS` | Key alias (`duofold`) |
| `DUOFOLD_RELEASE_KEY_PASSWORD` | Key password |

## GitHub Actions secrets

Required for **dev** and **release** tags (not for branch checks):

| Secret | Value |
|---|---|
| `DUOFOLD_RELEASE_KEYSTORE_BASE64` | Base64 of the `.jks` (`./scripts/print-keystore-base64.sh`) |
| `DUOFOLD_RELEASE_STORE_PASSWORD` | Keystore password |
| `DUOFOLD_RELEASE_KEY_ALIAS` | Key alias (e.g. `duofold`) |
| `DUOFOLD_RELEASE_KEY_PASSWORD` | Key password |

Configure in the repo: **Settings → Secrets and variables → Actions**.

```sh
# From a machine that has the keystore + gh auth:
source ~/.local/share/duofold/keystore.env
gh secret set DUOFOLD_RELEASE_KEYSTORE_BASE64 -R joei4cm/DuoFold < <(base64 -w0 "$DUOFOLD_RELEASE_STORE_FILE")
gh secret set DUOFOLD_RELEASE_STORE_PASSWORD -R joei4cm/DuoFold -b "$DUOFOLD_RELEASE_STORE_PASSWORD"
gh secret set DUOFOLD_RELEASE_KEY_ALIAS -R joei4cm/DuoFold -b "$DUOFOLD_RELEASE_KEY_ALIAS"
gh secret set DUOFOLD_RELEASE_KEY_PASSWORD -R joei4cm/DuoFold -b "$DUOFOLD_RELEASE_KEY_PASSWORD"
```

## CI triggers & versioning

| Event | Job | Output |
|---|---|---|
| Branch push / PR | Code check | `compileDebugKotlin` + unit tests |
| Tag `dev-0.7.0` or `dev/0.7.0` | Dev build | Signed APK artifact only (`DuoFold-0.7.0-dev.apk`) |
| Tag `v0.7.0` | Release publish | Signed APK + GitHub Release |

Version mapping (`scripts/resolve-version-from-tag.sh`):

- `v1.2.3` → `versionName=1.2.3`, `versionCode=1002003`
- `dev-1.2.3` → `versionName=1.2.3-dev`, `versionCode=1002003`

Examples:

```sh
git tag v0.7.0 && git push origin v0.7.0          # publish
git tag dev-0.7.1 && git push origin dev-0.7.1    # build only
```

## Verify

```sh
apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
```

You should see your DuoFold release cert CN, not `CN=Android Debug`.
