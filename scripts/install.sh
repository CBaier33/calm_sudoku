#!/bin/sh
# Build the release APK and install it on the attached device.
#
# A debug-signed build cannot upgrade a release-signed one (and the reverse),
# so if adb rejects the install with INSTALL_FAILED_UPDATE_INCOMPATIBLE, run
# `adb uninstall com.cbaier33.sudoku` once and try again.
set -e

cd "$(dirname "$0")/.."

VERSION=$(grep -m1 'versionName' app/build.gradle.kts | sed 's/.*"\(.*\)".*/\1/')

sh scripts/build-release.sh

echo "Installing..."
adb install -r "release/sudoku-$VERSION.apk"
