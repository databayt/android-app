#!/bin/bash
# pick-icon.sh — browse the App Store and install any app's icon as a tile.
#
# Usage:
#   tools/pick-icon.sh <block> [search term]
#
# Examples:
#   tools/pick-icon.sh students "Duolingo"
#   tools/pick-icon.sh grades "Khan Academy"
#   tools/pick-icon.sh exams "Quizlet"
#   tools/pick-icon.sh library "Kindle"
#
# Shows top 10 matches with names + preview URLs, asks you to pick one,
# downloads the 1024x1024 PNG to feature/dashboard/src/main/res/drawable-nodpi/ic_tile_<block>.png.
# No code change needed — the existing iconRes wiring reads that filename.
#
# Valid blocks:
#   students attendance grades schedule atom notifications exams
#   assignments library events profile home alerts

set -e

BLOCK="${1:-}"
shift || true
QUERY="$*"

if [ -z "$BLOCK" ]; then
  echo "usage: $0 <block> [search term]"
  echo "blocks: students attendance grades schedule atom notifications exams assignments library events profile home alerts"
  exit 1
fi

case "$BLOCK" in
  students|attendance|grades|schedule|atom|notifications|exams|assignments|library|events|profile|home|alerts) ;;
  *) echo "unknown block: $BLOCK"; exit 1 ;;
esac

if [ -z "$QUERY" ]; then
  read -p "search term: " QUERY
fi

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
DEST="$REPO_ROOT/feature/dashboard/src/main/res/drawable-nodpi/ic_tile_${BLOCK}.png"
PREVIEW_DIR="/tmp/icon-previews-$$"
mkdir -p "$PREVIEW_DIR"

ESC=$(echo "$QUERY" | sed 's/ /+/g')
JSON=$(curl -s "https://itunes.apple.com/search?term=${ESC}&entity=software&limit=10&country=us")

# Parse and display results
echo ""
echo "Results for '$QUERY':"
echo "$JSON" | python3 -c "
import sys, json
d = json.loads(sys.stdin.read(), strict=False)
results = d.get('results', [])
if not results:
    print('  (no matches)')
    sys.exit(0)
for i, r in enumerate(results, 1):
    name = (r.get('trackName','?') or '?')[:45]
    artist = (r.get('artistName','?') or '?')[:22]
    tid = r.get('trackId','?')
    print(f'  {i:2}. {name:<45}  by {artist:<22}  id={tid}')
"

# Save image URLs for download
echo "$JSON" | python3 -c "
import sys, json, re
d = json.loads(sys.stdin.read(), strict=False)
for i, r in enumerate(d.get('results', []), 1):
    u = r.get('artworkUrl512','')
    if u:
        u1k = re.sub(r'/512x512bb\.(jpg|png)\$','/1024x1024bb.png', u)
        print(f'{i}|{u1k}')
" > "$PREVIEW_DIR/urls.txt"

# Download all thumbnails in parallel for quick preview
echo ""
echo "Downloading thumbnails for preview..."
while IFS='|' read -r i url; do
  curl -s -o "$PREVIEW_DIR/${i}.png" "$url" &
done < "$PREVIEW_DIR/urls.txt"
wait

# Open previews in Finder so user can see all at once
echo "Opening thumbnails — check the Finder window."
open "$PREVIEW_DIR"

echo ""
read -p "pick number (1-10) or Enter to cancel: " PICK

if [ -z "$PICK" ]; then
  echo "cancelled"
  rm -rf "$PREVIEW_DIR"
  exit 0
fi

SRC="$PREVIEW_DIR/${PICK}.png"
if [ ! -f "$SRC" ]; then
  echo "invalid pick"
  rm -rf "$PREVIEW_DIR"
  exit 1
fi

cp "$SRC" "$DEST"
SIZE=$(stat -f%z "$DEST")
DIM=$(file "$DEST" | grep -oE '[0-9]+ x [0-9]+' | head -1)
echo ""
echo "installed: $DEST"
echo "  dim=$DIM  size=${SIZE}B"

# Clean up
rm -rf "$PREVIEW_DIR"
