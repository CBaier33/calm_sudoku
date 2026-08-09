#!/bin/sh
set -e

echo "Getting packages..."
flutter pub get

echo "Generating launcher icons..."
dart run flutter_launcher_icons:generate -f pubspec.yaml

echo "Generating splash screen..."
dart run flutter_native_splash:create --path=pubspec.yaml
