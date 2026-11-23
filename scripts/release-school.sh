#!/usr/bin/env bash
# Build, sign, verify and (optionally) publish a school's own APK.
#
#   scripts/release-school.sh <school> [--publish] [--min-code N] [--apk <verified.apk>]
#
# --apk skips the build and ships an APK you already built and tested (it is still
# verified: signature, package, label, cert).
#
# The brand lives OUTSIDE this public repo: ~/.config/hogwarts-android/brands/<school>/
# (brand.properties + icon.png; tools/brand-res.py turns them into res/).
# Signing: ~/.config/hogwarts-android/release.jks, password in the Keychain
# (hogwarts-android-keystore-password). Every update MUST use the same key.
#
# --publish uploads to the CDN (databayt-cdn → cdn.databayt.org):
#   balqalam/app/<school>/<school>-<versionName>.apk   (kept)
#   balqalam/app/<school>/<school>.apk                 (stable alias the school shares)
#   balqalam/app/<school>/version.json                 (the in-app update channel; its apkUrl is the kept, versioned file)
# plus any extra alias keys listed in brand.properties `aliases=` (comma-separated).
# minCode defaults to the new versionCode: everyone below it is made to update.
set -euo pipefail

SCHOOL="${1:?usage: scripts/release-school.sh <school> [--publish] [--min-code N]}"
shift
PUBLISH=0
MIN_CODE=""
GIVEN_APK=""
while [ $# -gt 0 ]; do
  case "$1" in
    --publish) PUBLISH=1 ;;
    --min-code) MIN_CODE="$2"; shift ;;
    --apk) GIVEN_APK="$2"; shift ;;
    *) echo "unknown flag: $1" >&2; exit 2 ;;
  esac
  shift
done

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BRAND="$HOME/.config/hogwarts-android/brands/$SCHOOL"
KEYSTORE="$HOME/.config/hogwarts-android/release.jks"
BUCKET="databayt-cdn"
DISTRIBUTION="E3PHDXTDSBCQSJ"
CDN="https://cdn.databayt.org"

[ -f "$BRAND/brand.properties" ] || { echo "no brand at $BRAND" >&2; exit 1; }
[ -f "$KEYSTORE" ] || { echo "no keystore at $KEYSTORE" >&2; exit 1; }
prop() { grep -E "^$1=" "$BRAND/brand.properties" | head -1 | cut -d= -f2-; }

STUDIO_JBR="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
if [ -z "${JAVA_HOME:-}" ] && [ -d "$STUDIO_JBR" ]; then export JAVA_HOME="$STUDIO_JBR"; fi
SDK="$(grep -E '^sdk.dir=' "$ROOT/local.properties" | cut -d= -f2-)"
BT="$(ls -d "$SDK"/build-tools/* | sort -V | tail -1)"

if [ -n "$GIVEN_APK" ]; then
  APK="$GIVEN_APK"
  [ -f "$APK" ] || { echo "no APK at $APK" >&2; exit 1; }
else
  echo "→ brand resources"
  python3 "$ROOT/tools/brand-res.py" "$BRAND"

  # Production Firebase (project hogwarts-4bb67, owner osmanabdout.jr): one Android app
  # org.hogwarts.android serves every school brand. Kept outside git (public repo).
  FIREBASE_CONFIG="$HOME/.config/hogwarts-android/firebase/google-services.json"
  [ -f "$FIREBASE_CONFIG" ] || { echo "no production Firebase config at $FIREBASE_CONFIG" >&2; exit 1; }
  cp "$FIREBASE_CONFIG" "$ROOT/app/google-services.json"

  echo "→ assemble schoolRelease"
  PASS="$(security find-generic-password -a "$USER" -s hogwarts-android-keystore-password -w)"
  ( cd "$ROOT" && HOGWARTS_KEYSTORE="$KEYSTORE" HOGWARTS_KEYSTORE_PASSWORD="$PASS" \
      ./gradlew --quiet :app:assembleSchoolRelease -Phogwarts.brand="$BRAND" )
  unset PASS
  APK="$(ls "$ROOT"/app/build/outputs/apk/school/release/*.apk | head -1)"
fi
"$BT/apksigner" verify "$APK"
BADGING="$("$BT/aapt2" dump badging "$APK")"
PKG="$(sed -nE "s/^package: name='([^']+)'.*/\1/p" <<<"$BADGING")"
CODE="$(sed -nE "s/.*versionCode='([0-9]+)'.*/\1/p" <<<"$BADGING" | head -1)"
NAME="$(sed -nE "s/.*versionName='([^']+)'.*/\1/p" <<<"$BADGING" | head -1)"
LABEL="$(sed -nE "s/^application-label:'(.*)'/\1/p" <<<"$BADGING")"
CERT="$("$BT/apksigner" verify --print-certs "$APK" | sed -nE 's/.*certificate SHA-256 digest: (.*)/\1/p' | head -1)"
SHA="$(shasum -a 256 "$APK" | cut -d' ' -f1)"
SIZE="$(stat -f%z "$APK")"
[ "$PKG" = "$(prop package)" ] || { echo "package $PKG ≠ brand $(prop package)" >&2; exit 1; }

OUT="$HOME/.cache/balqalam-apk/$SCHOOL"
mkdir -p "$OUT"
[ "$APK" -ef "$OUT/$SCHOOL-$NAME.apk" ] || cp "$APK" "$OUT/$SCHOOL-$NAME.apk"
cat <<EOF
  package   $PKG
  label     $LABEL
  version   $NAME ($CODE)
  size      $SIZE bytes
  sha256    $SHA
  cert      $CERT
  copy      $OUT/$SCHOOL-$NAME.apk
EOF

[ "$PUBLISH" = 1 ] || { echo "(dry run — pass --publish to upload)"; exit 0; }

MIN_CODE="${MIN_CODE:-$CODE}"
ALIAS_URL="$CDN/balqalam/app/$SCHOOL/$SCHOOL.apk"
# The update channel names the VERSIONED file, never the alias. The app (1.3.3+) downloads this
# URL itself and checks its SHA-256 against the manifest: an alias still cached at the edge for a
# minute after a publish would be the previous build, fail the check and tell a teacher her
# "download did not complete". The versioned file is immutable, so it can never disagree with the
# manifest. Older apps open the URL in the browser, where a file named after its version also ends
# the "which alabidae (4).apk do I open" problem. Never publish two builds under one version name.
APK_URL="$CDN/balqalam/app/$SCHOOL/$SCHOOL-$NAME.apk"
put_apk() {
  aws s3 cp "$OUT/$SCHOOL-$NAME.apk" "s3://$BUCKET/$1" --only-show-errors \
    --content-type application/vnd.android.package-archive \
    --cache-control "$2" --content-disposition "attachment; filename=\"$(basename "$1")\""
}
echo "→ upload"
put_apk "balqalam/app/$SCHOOL/$SCHOOL-$NAME.apk" "public, max-age=31536000, immutable"
put_apk "balqalam/app/$SCHOOL/$SCHOOL.apk" "public, max-age=60"
PATHS=("/balqalam/app/$SCHOOL/$SCHOOL.apk" "/balqalam/app/$SCHOOL/version.json")
IFS=',' read -r -a EXTRA <<<"$(prop aliases)"
for key in "${EXTRA[@]}"; do
  [ -n "$key" ] || continue
  put_apk "$key" "public, max-age=60"
  PATHS+=("/$key")
done

VJ="$(mktemp)"
python3 - "$CODE" "$MIN_CODE" "$APK_URL" "$NAME" "$SHA" >"$VJ" <<'EOF'
import json, sys
code, min_code, url, name, sha = sys.argv[1:]
print(json.dumps({
    "latestCode": int(code), "minCode": int(min_code), "versionName": name,
    "apkUrl": url, "sha256": sha,
    "notesAr": "نزّل النسخة الجديدة من التطبيق عشان تواصل.",
    "notesEn": "Install the new version to continue.",
}, ensure_ascii=False, indent=2))
EOF
aws s3 cp "$VJ" "s3://$BUCKET/balqalam/app/$SCHOOL/version.json" --only-show-errors \
  --content-type "application/json; charset=utf-8" --cache-control "public, max-age=60"
rm -f "$VJ"

aws cloudfront create-invalidation --distribution-id "$DISTRIBUTION" --paths "${PATHS[@]}" \
  --query 'Invalidation.Id' --output text
echo "published: $ALIAS_URL  (update channel → $APK_URL; minCode $MIN_CODE, latestCode $CODE)"
