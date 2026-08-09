#!/bin/sh
set -e

VERSION=$(grep '^version:' pubspec.yaml | awk '{print $2}' | cut -d'+' -f1)

flutter build apk --release --split-per-abi

OUT="build/app/outputs/flutter-apk"

for apk in "$OUT"/app-*-release.apk; do
    ABI=$(basename "$apk" | sed 's/app-\(.*\)-release.apk/\1/')
    mv "$apk" "$OUT/sudoku-$ABI-v$VERSION.apk"
    mv "$apk.sha1" "$OUT/sudoku-$ABI-v$VERSION.apk.sha1"
done
