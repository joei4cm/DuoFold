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
| Tag `dev-0.1.0_20260917_rev.1` | Dev build | Signed APK artifact only |
| Tag `v0.1.0_20260917_rev.1` | Release publish | Signed APK + GitHub Release |

**Tag format (canonical):** `v0.1.x_yyyymmdd_rev.n`

| Part | Meaning | Example |
|---|---|---|
| `0.1.x` | Product line + patch | `0.1.0` |
| `yyyymmdd` | Build calendar day | `20260917` |
| `rev.n` | Same-day revision (0–99) | `rev.1` |

Mapping (`scripts/resolve-version-from-tag.sh`):

- `v0.1.0_20260917_rev.1` → `versionName=0.1.0_20260917_rev.1`, `versionCode=2026091701` (`yyyymmdd*100 + rev`)
- `dev-0.1.0_20260917_rev.1` → `versionName=…-dev`, same `versionCode`

```sh
git tag v0.1.0_20260917_rev.1 && git push origin v0.1.0_20260917_rev.1
git tag dev-0.1.0_20260917_rev.2 && git push origin dev-0.1.0_20260917_rev.2
```

## Verify

```sh
apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
```

You should see your DuoFold release cert CN, not `CN=Android Debug`.
