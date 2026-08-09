#!/bin/sh
set -e

echo "Getting packages..."
flutter pub get

echo "Generating launcher icons..."
dart run flutter_launcher_icons:generate -f pubspec.yaml

echo "Generating splash screen..."
dart run flutter_native_splash:create --path=pubspec.yaml

echo "Building release APK..."

VERSION=$(grep '^version:' pubspec.yaml | cut -d' ' -f2 | cut -d'+' -f1)

flutter build apk --release

mv build/app/outputs/flutter-apk/app-release.apk \
   "build/app/outputs/flutter-apk/sudoku-${VERSION}.apk"

mv build/app/outputs/flutter-apk/app-release.apk.sha1 \
   "build/app/outputs/flutter-apk/sudoku-${VERSION}.apk.sha1"

echo "Build completed for sudoku-${VERSION}.apk"

echo "Installing..."

flutter install --use-application-binary="build/app/outputs/flutter-apk/sudoku-${VERSION}.apk"
