#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUTPUT="${SCRIPT_DIR}/digdir-workshop.zip"

zip -r "$OUTPUT" "$SCRIPT_DIR" \
  --exclude "$SCRIPT_DIR/.git/*" \
  --exclude "$SCRIPT_DIR/slides/*"

echo "Created: $OUTPUT"
