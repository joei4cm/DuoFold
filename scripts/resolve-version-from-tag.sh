#!/usr/bin/env bash
# Map DuoFold git tags to Android versionName / versionCode.
#   v1.2.3       → 1.2.3 / 1002003
#   v1.2.3-rc.1  → 1.2.3-rc.1 / 1002003
#   dev-1.2.3    → 1.2.3-dev / 1002003
#   dev/1.2.3    → 1.2.3-dev / 1002003
set -euo pipefail

TAG="${1:-${GITHUB_REF_NAME:-}}"
if [[ -z "$TAG" ]]; then
  echo "Usage: $0 <tag>" >&2
  exit 1
fi

raw="$TAG"
channel="release"
case "$raw" in
  v*) raw="${raw#v}" ;;
  dev-*) raw="${raw#dev-}"; channel="dev" ;;
  dev/*) raw="${raw#dev/}"; channel="dev" ;;
  *)
    echo "Unsupported tag '$TAG' (expected v* or dev-* / dev/*)" >&2
    exit 1
    ;;
esac

# Strip optional pre-release / build metadata for numeric code
core="${raw%%[-+]*}"
IFS=. read -r major minor patch _ <<<"${core}.0.0.0"
major=${major:-0}
minor=${minor:-0}
patch=${patch:-0}
# Keep only digits
major=${major//[^0-9]/}
minor=${minor//[^0-9]/}
patch=${patch//[^0-9]/}
major=${major:-0}
minor=${minor:-0}
patch=${patch:-0}

version_code=$(( major * 1000000 + minor * 1000 + patch ))
if [[ "$channel" == "dev" ]]; then
  version_name="${core}-dev"
else
  version_name="$raw"
fi

if [[ -n "${GITHUB_ENV:-}" ]]; then
  {
    echo "DUOFOLD_VERSION_NAME=${version_name}"
    echo "DUOFOLD_VERSION_CODE=${version_code}"
    echo "DUOFOLD_CHANNEL=${channel}"
    echo "DUOFOLD_TAG=${TAG}"
  } >> "$GITHUB_ENV"
fi

echo "tag=${TAG} channel=${channel} versionName=${version_name} versionCode=${version_code}"
