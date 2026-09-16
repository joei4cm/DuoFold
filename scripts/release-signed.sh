#!/usr/bin/env bash
# Assemble a release-signed DuoFold APK (not the debug key).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

ENV_FILE="${DUOFOLD_KEYSTORE_ENV:-$HOME/.local/share/duofold/keystore.env}"
if [[ -f "$ENV_FILE" ]]; then
  # shellcheck disable=SC1090
  source "$ENV_FILE"
fi

: "${DUOFOLD_RELEASE_STORE_FILE:?Set DUOFOLD_RELEASE_* or run ./scripts/generate-release-keystore.sh}"
: "${DUOFOLD_RELEASE_STORE_PASSWORD:?missing}"
: "${DUOFOLD_RELEASE_KEY_ALIAS:?missing}"
: "${DUOFOLD_RELEASE_KEY_PASSWORD:?missing}"

export DUOFOLD_RELEASE_STORE_FILE DUOFOLD_RELEASE_STORE_PASSWORD
export DUOFOLD_RELEASE_KEY_ALIAS DUOFOLD_RELEASE_KEY_PASSWORD

if [[ -z "${JAVA_HOME:-}" && -x "$ROOT/.jdk/jdk-17.0.13+11/bin/java" ]]; then
  export JAVA_HOME="$ROOT/.jdk/jdk-17.0.13+11"
  export PATH="$JAVA_HOME/bin:$PATH"
fi
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
export ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-$ANDROID_HOME}"

./scripts/gradle.sh :app:assembleRelease

APK="$ROOT/app/build/outputs/apk/release/app-release.apk"
echo "Signed release APK: $APK"
if command -v apksigner >/dev/null 2>&1; then
  apksigner verify --print-certs "$APK" || true
elif [[ -x "$ANDROID_HOME/build-tools/36.0.0/apksigner" ]]; then
  "$ANDROID_HOME/build-tools/36.0.0/apksigner" verify --print-certs "$APK" || true
else
  # Fallback: any build-tools apksigner
  AS="$(ls -d "$ANDROID_HOME"/build-tools/*/apksigner 2>/dev/null | sort -V | tail -1 || true)"
  if [[ -n "$AS" ]]; then "$AS" verify --print-certs "$APK" || true; fi
fi
