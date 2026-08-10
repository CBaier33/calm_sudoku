# Sudoku for Mudita Kompakt — native Android

A native Kotlin/Jetpack Compose rebuild of the Flutter app in the repository
root, built on [Mudita Mindful Design (MMD)](https://github.com/mudita/MMD) —
Mudita's Apache-2.0 Compose component library for E Ink displays.

Behaviour is a strict port of the Flutter build: the same puzzle generator,
the same rules, the same four screens. The visual language is MMD's.

## Build

```bash
./gradlew :app:testDebugUnitTest    # generator + game rules
./gradlew :app:assembleDebug
```

Requires JDK 17+ and an Android SDK with platform 35. `local.properties` must
point `sdk.dir` at the SDK (it is gitignored and generated on first build by
Android Studio).

## Install

The `applicationId` is shared with the Flutter build, which is release-signed,
so a debug build cannot upgrade it:

```bash
adb uninstall com.cbaier33.sudoku    # once, if the Flutter build is installed
./gradlew :app:installDebug
```

## Dependency pinning

MMD 1.0.2 is compiled against **material3 1.3.1** and uses
`LocalRippleConfiguration`, which was experimental in that line. `material3` and
`compose-ui` are pinned to explicit versions in `gradle/libs.versions.toml`
rather than floating on a Compose BOM. After changing any Compose version, check
that MMD still gets what it expects:

```bash
./gradlew :app:dependencies --configuration debugRuntimeClasspath \
  | grep -E 'mudita|material3'
```

`com.mudita:MMD` is a Kotlin Multiplatform root module; Gradle redirects it to
`com.mudita:MMD-android` through module metadata.

## Notes on MMD

Two gaps are patched in `theme/AppTheme.kt`:

- `eInkColorScheme` leaves six surface slots `Color.Unspecified`
  (`surfaceBright`, `surfaceDim`, `surfaceContainer`, `surfaceContainerHigh`,
  `surfaceContainerHighest`, `surfaceContainerLowest`). `TopAppBarMMD` reads
  `surfaceContainer`, so they are all set to white.
- MMD asks for `FontWeight.Medium` everywhere, but its bundled Lato family
  registers no W500 face, so its text resolves to Lato Regular. The board picks
  `Black` and `Light` explicitly — both exact matches — to keep givens visually
  distinct from entered digits.

MMD provides no grid, `Scaffold`, `Icon`, `IconButton` or dialog. The board is
hand-drawn on a `Canvas`; the rest come from stock Material 3, which inherits
MMD's theme and its globally disabled ripple.

`ExperimentalMaterial3Api` is opted into once in `app/build.gradle.kts`, since
`TopAppBarMMD` and `ModalBottomSheetMMD` both expose it.
