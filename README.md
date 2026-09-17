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
- Versions are pinned in `gradle/libs.versions.toml`: Kotlin 1.9.22 and
  Compose Multiplatform 1.5.12, lower than the Kotlin 2.x / Compose
  Multiplatform 1.8+ this project is aiming for. Every Compose Multiplatform
  release from 1.6.0 onward adds a runtime dependency that resolves to
  genuine `androidx.annotation` / `androidx.collection` artifacts (and, from
  1.6.10, `androidx.lifecycle` too), which are published only to Google's
  Maven repository (`google()`). Raise both versions once building against
  `google()` is not a constraint; see `TODO.md` section 3 for the details of
  what was tried.

## Targets

- **`jvm`** (desktop): fully supported, this is where the app and the
  snapshot test run.
- **`iosArm64` / `iosSimulatorArm64`**: declared on `engine` and `common` so
  `commonMain` is checked against them, but cannot be linked from a
  non-macOS host regardless of network access. They are opt-out with
  `-PskipNative=true` (enabled by default) because merely declaring a native
  target makes the Kotlin Gradle plugin eagerly download the Kotlin/Native
  compiler during ordinary project configuration.
- **`wasmJs`**: declared on `engine` and `common`. `engine` (pure Kotlin, no
  Compose dependency) compiles for wasm. `common`'s wasm target does not: at
  the pinned Compose Multiplatform 1.5.12, `compose.runtime`/`compose.ui`
  simply have no `wasm-js` variant to resolve (wasm support for Compose UI
  only shipped starting around 1.6.0, out of reach here for the same
  `google()` reason above).
- **`android`** (on `common`, plus the `:android` app module): declared with
  the standard `com.android.library` + `androidTarget()` approach, opt-out
  with `-PskipAndroid=true` (enabled by default) since AGP and androidx need
  `google()`. Unverified: this could not be built or tested wherever
  `google()` is unreachable.

## Compose Multiplatform Application

**Desktop**
- `./gradlew run` - run application
- `./gradlew package` - package native distribution into `build/compose/binaries`
- `./gradlew :engine:jvmTest :desktop:jvmTest` - unit tests plus an offscreen
  render of the teapot and cube, written to `desktop/build/snapshots/model3d.png`.
  Where `google()` or `download.jetbrains.com` are unreachable, add
  `-PskipAndroid=true -PskipNative=true`.

**Android**
- `./gradlew installDebug` - install Android application on an Android device (on a real device or on an emulator)

## Continuous integration

`.github/workflows/ci.yml` runs `:engine:jvmTest :desktop:jvmTest` on every
push and pull request with JDK 17, with Android and the native targets
skipped (see above), and uploads `desktop/build/snapshots/model3d.png` as a
build artifact.
