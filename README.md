# compose3d
Software 3D pipeline drawn through Compose, with Skia doing the triangle fill.

`engine` is plain Kotlin with no Compose dependency: `Mesh` (flat vertex and
index buffers), `Camera`, `Light`, `Renderer` and an OBJ parser. `Renderer`
transforms a mesh into screen space, culls back faces, lights it (flat or
Gouraud) and sorts the triangles far to near. `common` holds the `Model3D`
composable, which hands the sorted triangles to the platform canvas through
`drawVertices`. `desktop` and `android` are thin launchers.

Known limits of the current renderer: painter's sort instead of a depth
buffer, and triangles crossing the near plane are dropped rather than
clipped. The Android `drawVertices` actual
(`common/src/androidMain/kotlin/DrawTriangles.android.kt`) is written but not
run on a device or emulator yet.

## Build requirements

- JDK 17 (set `JAVA_HOME` to it; the Gradle wrapper itself runs fine on newer
  JDKs too).
- Gradle 8.14.5 (via `./gradlew`, no local Gradle install needed).
- Kotlin 2.4.20 and Compose Multiplatform 1.12.0, pinned in
  `gradle/libs.versions.toml` (the newest non-preview releases of each on
  Maven Central at the time of writing). AGP 8.9.1, within the 8.5.2-9.3.1
  range Kotlin 2.4.20's compatibility guide documents.
- Network access to `google()` (Google's Maven repository) is required to
  build `common` (its `androidTarget()`) or `android`, and therefore also
  `desktop` (which depends on `common`). `engine` has no such dependency and
  builds with only Maven Central.

## Targets

- **`jvm`** (desktop): the app and the snapshot test run here.
- **`iosArm64` / `iosSimulatorArm64`**: declared on `engine` and `common`.
  `commonMain` has no JVM-only APIs, so it type-checks for iOS unchanged.
  Cannot be linked from a non-macOS host regardless of network access; see
  the `ios` job in `.github/workflows/ci.yml`.
- **`wasmJs`**: declared on `engine` and `common`, both expected to compile
  under Compose Multiplatform 1.8+.
- **`android`** (on `common`, plus the `:android` app module): the standard
  `com.android.library` + `androidTarget()` approach.

## Compose Multiplatform Application

**Desktop**
- `./gradlew run` - run application
- `./gradlew package` - package native distribution into `build/compose/binaries`
- `./gradlew :engine:jvmTest :desktop:jvmTest` - unit tests plus an offscreen
  render of the teapot and cube, written to `desktop/build/snapshots/model3d.png`

**Android**
- `./gradlew installDebug` - install Android application on an Android device (on a real device or on an emulator)
- `./gradlew :android:assembleDebug` - compile without installing

## Continuous integration

`.github/workflows/ci.yml`, on every push and pull request:
- `test` (ubuntu-latest, JDK 17): `:engine:jvmTest`, `:desktop:jvmTest`,
  `:common:compileKotlinWasmJs`, then `:android:assembleDebug` (the
  GitHub-hosted Ubuntu runner ships the Android SDK), then uploads
  `desktop/build/snapshots/model3d.png` as a build artifact.
- `ios` (macos-latest, optional/best-effort): `:common:compileKotlinIosSimulatorArm64`.

### A note on developing without access to Google's Maven repository

This project (`common` in particular) was developed and its `common`/
`desktop` compilation was verified in a sandboxed environment that could
reach Maven Central but not `google()`. In that kind of environment:

- `:engine:jvmTest`, `:engine:compileKotlinWasmJs` and
  `:engine:compileKotlinIosArm64` build and pass normally (`engine` has no
  Android or androidx dependency).
- Any task touching `:common` (and so `:desktop`, which depends on it) fails
  at project configuration, because `com.android.library` itself cannot be
  resolved: `common/build.gradle.kts` applies it unconditionally for
  `androidTarget()`. The exact failure seen there:
  ```
  Plugin [id: 'com.android.library', version: '8.9.1'] was not found in any of the following sources:
  ...
     Searched in the following repositories:
       Gradle Central Plugin Repository
       MavenRepo
       Google
  ```
- For `:common`/`:desktop`/`:android`, the GitHub Actions workflow above is
  the verifier, not a local build in such an environment.
- Declaring `iosArm64`/`iosSimulatorArm64` on a target makes the Kotlin
  Gradle plugin eagerly download the Kotlin/Native compiler from
  `download.jetbrains.com` during ordinary project configuration. If that
  host is also unreachable, point
  `kotlin.native.distribution.baseDownloadUrl` (in `~/.gradle/gradle.properties`,
  not this project's `gradle.properties`) at the matching GitHub release,
  e.g. `https://github.com/JetBrains/kotlin/releases/download/v2.4.20`,
  which Kotlin's Gradle plugin also accepts as a compiler distribution
  source.
