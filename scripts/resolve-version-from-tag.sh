#!/usr/bin/env bash
# Map DuoFold git tags to Android versionName / versionCode.
#
# Canonical release: v0.1.x_yyyymmdd_rev.n
#   v0.1.0_20260917_rev.1 → versionName=0.1.0_20260917_rev.1
#                           versionCode=2026091701  (yyyymmdd*100 + rev)
#
# Dev:  dev-0.1.0_20260917_rev.1  or  dev/0.1.0_20260917_rev.1
#   → versionName=0.1.0_20260917_rev.1-dev , same versionCode
#
# Legacy fallback (still accepted):
#   v1.2.3 / dev-1.2.3 → versionCode = major*1000000 + minor*1000 + patch
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
    echo "Unsupported tag '$TAG' (expected v0.1.x_yyyymmdd_rev.n or dev-…)" >&2
    exit 1
    ;;
esac

version_name=""
version_code=0

# Preferred: 0.1.x_yyyymmdd_rev.n
if [[ "$raw" =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)_([0-9]{8})_rev\.([0-9]+)$ ]]; then
  major="${BASH_REMATCH[1]}"
  minor="${BASH_REMATCH[2]}"
  patch="${BASH_REMATCH[3]}"
  ymd="${BASH_REMATCH[4]}"
  rev="${BASH_REMATCH[5]}"
  if (( rev > 99 )); then
    echo "rev must be 0–99 for versionCode packing (got $rev)" >&2
    exit 1
  fi
  version_name="${major}.${minor}.${patch}_${ymd}_rev.${rev}"
  version_code=$((10#$ymd * 100 + 10#$rev))
else
  # Legacy semver core
  core="${raw%%[-+]*}"
  IFS=. read -r major minor patch _ <<<"${core}.0.0.0"
  major=${major//[^0-9]/}; major=${major:-0}
  minor=${minor//[^0-9]/}; minor=${minor:-0}
  patch=${patch//[^0-9]/}; patch=${patch:-0}
  version_code=$((10#$major * 1000000 + 10#$minor * 1000 + 10#$patch))
  version_name="$raw"
fi

if [[ "$channel" == "dev" ]]; then
  version_name="${version_name}-dev"
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
