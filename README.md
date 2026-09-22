# compose3d
Software 3D pipeline drawn through Compose, with Skia doing the triangle fill.

`engine` is plain Kotlin with no Compose dependency: `Mesh` (flat vertex and
index buffers), `Camera`, `Light`, `Renderer` and an OBJ parser. `Renderer`
transforms a mesh into screen space, culls back faces, lights it (flat or
Gouraud) and sorts the triangles far to near. `common` holds the `Model3D`
composable, which hands the sorted triangles to the platform canvas through
`drawVertices`. `desktop` and `android` are thin launchers.

Drag the model to turn it, pinch or scroll to zoom. `rememberOrbitCamera`
holds the angles and `Modifier.orbit` feeds gestures into them; `Model3D`
reads the camera at draw time, so turning the view redraws without
recomposing.

Known limits of the current renderer: painter's sort instead of a depth
buffer, and triangles crossing the near plane are dropped rather than
clipped. The Android `drawVertices` actual
(`common/src/androidMain/kotlin/DrawTriangles.android.kt`) is written but not
run on a device or emulator yet.

## Build requirements

- JDK 17. The build declares `jvmToolchain(17)` and provisions one through
  the Foojay resolver if none is installed.
- Gradle 9.5.0 via `./gradlew`, Kotlin 2.4.20, Compose Multiplatform 1.12.0
  and AGP 9.1.1, all pinned in `gradle/libs.versions.toml`. Android builds
  need compileSdk 37; the app's minSdk is 23, which Compose 1.12 requires.
- The Android SDK, for `common` (which has an Android target) and `android`.
  `engine` needs neither the SDK nor Google's Maven repository.
- Android API 29 or newer. `Canvas.drawVertices` is only honoured on a
  hardware-accelerated canvas from API 29, and Compose always draws into
  one.

## Targets

- **`jvm`** (desktop): the app and the snapshot test run here.
- **`iosArm64` / `iosSimulatorArm64`**: declared on `engine` and `common`.
  Only buildable on macOS; the `ios` CI job compiles the simulator target.
- **`wasmJs`**: the `web` module is a browser build of the same demo.
- **`android`**: `common` is an Android library through the
  `com.android.kotlin.multiplatform.library` plugin and the `:android` app
  module wraps it.

## Compose Multiplatform Application

**Desktop**
- `./gradlew run` - run application
- `./gradlew package` - package native distribution into `build/compose/binaries`
- `./gradlew :engine:jvmTest :desktop:jvmTest` - unit tests plus an offscreen
  render of the teapot and cube, written to `desktop/build/snapshots/model3d.png`

**Web**
- `./gradlew :web:wasmJsBrowserDevelopmentRun` - serve the demo at localhost
- `./gradlew :web:wasmJsBrowserDistribution` - build a static site into
  `web/build/dist/wasmJs/productionExecutable`

**Embedding in another site**

The web bundle exports `mountDemo(containerId, demoId)`, so a host page can
load `compose3d.js` once and mount several demos into its own elements. The
bundle resolves Compose resources against the directory it was loaded from,
so it works from any page path. `web/src/wasmJsMain/resources/embed.html` is
a two-demo test page for this.

- `./gradlew :web:syncToSite` - copy the production bundle into
  `../gunnarss/site/src/jsMain/resources/public/compose3d` (override with
  `-PsiteDir=...`)

**Android**
- `./gradlew installDebug` - install Android application on an Android device (on a real device or on an emulator)
- `./gradlew :android:assembleDebug` - compile without installing

## Continuous integration

`.github/workflows/ci.yml`, on every push and pull request:
- `test` (ubuntu-latest, JDK 17): `:engine:jvmTest`, `:desktop:jvmTest`,
  `:web:wasmJsBrowserDistribution`, then `:android:assembleDebug` (the
  GitHub-hosted Ubuntu runner ships the Android SDK), then uploads the
  offscreen snapshot and the web bundle as build artifacts.
- `ios` (macos-latest): `:common:compileKotlinIosSimulatorArm64`.
