#!/bin/sh
# Build a signed release APK and collect it in release/ as sudoku-<version>.apk,
# alongside a .sha1 of the same name. Signing keys come from key.properties in
# the repository root; without it Gradle falls back to the debug keystore and
# this script says so rather than shipping a debug-signed APK by surprise.
set -e

cd "$(dirname "$0")/.."

VERSION=$(grep -m1 'versionName' app/build.gradle.kts | sed 's/.*"\(.*\)".*/\1/')

if [ -z "$VERSION" ]; then
    echo "Could not read versionName from app/build.gradle.kts" >&2
    exit 1
fi

if [ ! -f key.properties ]; then
    echo "WARNING: no key.properties - this APK will be debug-signed and cannot" >&2
    echo "         upgrade an installed release build." >&2
fi

echo "Running unit tests..."
./gradlew :app:testReleaseUnitTest

echo "Building release APK for v$VERSION..."
./gradlew :app:assembleRelease

OUT="release"
APK="app/build/outputs/apk/release/app-release.apk"

mkdir -p "$OUT"
cp "$APK" "$OUT/sudoku-$VERSION.apk"
sha1sum "$OUT/sudoku-$VERSION.apk" | awk '{print $1}' > "$OUT/sudoku-$VERSION.apk.sha1"

# apksigner ships in the SDK build-tools and is rarely on PATH, so fall back to
# the newest copy under sdk.dir. The check is a courtesy, not a gate.
APKSIGNER=$(command -v apksigner || true)
if [ -z "$APKSIGNER" ] && [ -f local.properties ]; then
    SDK_DIR=$(grep -m1 '^sdk.dir=' local.properties | cut -d'=' -f2-)
    APKSIGNER=$(ls -d "$SDK_DIR"/build-tools/*/apksigner 2>/dev/null | tail -1)
fi
if [ -n "$APKSIGNER" ]; then
    "$APKSIGNER" verify --print-certs "$OUT/sudoku-$VERSION.apk" \
        | grep -i 'certificate DN' || true
fi

echo "Build completed for $OUT/sudoku-$VERSION.apk"
