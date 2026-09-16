#!/usr/bin/env bash
# Print base64 of the release keystore for GitHub secret DUOFOLD_RELEASE_KEYSTORE_BASE64.
set -euo pipefail
ENV_FILE="${DUOFOLD_KEYSTORE_ENV:-$HOME/.local/share/duofold/keystore.env}"
if [[ -f "$ENV_FILE" ]]; then
  # shellcheck disable=SC1090
  source "$ENV_FILE"
fi
: "${DUOFOLD_RELEASE_STORE_FILE:?Set DUOFOLD_RELEASE_STORE_FILE or create keystore via ./scripts/generate-release-keystore.sh}"
test -f "$DUOFOLD_RELEASE_STORE_FILE"
base64 -w0 "$DUOFOLD_RELEASE_STORE_FILE"
echo
echo "# Paste the line above into GitHub secret DUOFOLD_RELEASE_KEYSTORE_BASE64" >&2
echo "# Also set DUOFOLD_RELEASE_STORE_PASSWORD / KEY_ALIAS / KEY_PASSWORD" >&2
