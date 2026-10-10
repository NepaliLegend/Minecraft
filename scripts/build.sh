#!/usr/bin/env bash
# Builds a CurseForge-importable modpack zip: manifest.json + overrides/.
set -euo pipefail

cd "$(dirname "$0")/.."

name=$(jq -r '.name | gsub("[^A-Za-z0-9._-]"; "-")' manifest.json)
version=${1:-$(jq -r '.version' manifest.json)}

mkdir -p dist
out="dist/${name}-${version}.zip"
rm -f "$out"

tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT
jq --arg v "$version" '.version = $v' manifest.json > "$tmp/manifest.json"
cp -r overrides "$tmp/overrides"
find "$tmp/overrides" -name .gitkeep -delete

(cd "$tmp" && zip -qr "$OLDPWD/$out" manifest.json overrides)
echo "$out"
