# Roadmap

Goal: a `Model3D` composable that renders 3D meshes on every Compose
Multiplatform target with no GPU context setup. Vertex work happens on the
CPU in `engine`, Skia fills the triangles through `drawVertices`.

Done so far (PR #6): flat-buffer `Mesh`, correct view/projection, near and far
plane rejection, back-face culling, flat and Gouraud lighting, painter's sort,
OBJ parsing, offscreen snapshot test.

Items are roughly in the order they should be done. Each has a definition of
done so it is clear when to stop.

## 1. Android `drawTriangles` actual

- [x] Give `common` an `android` target with `androidMain` next to the Skiko
      source set (landed with item 3).
- [x] Implement `DrawScope.drawTriangles` with
      `android.graphics.Canvas.drawVertices`
      (`common/src/androidMain/kotlin/DrawTriangles.android.kt`). Android's
      API takes counts and offsets, so no per-frame array copy is needed
      there.
- [x] Check the minimum API level at which hardware-accelerated canvases
      honour `drawVertices` with per-vertex colours. The answer is API 29,
      and Compose always draws into a hardware-accelerated canvas, so below
      29 the renderer would silently draw nothing. `minSdk` is now 29 in both
      Android modules rather than carrying a `drawPath` fallback.
- [x] Point the Android app at the teapot instead of the cube, so a device
      run exercises the real mesh.
- [ ] Run the demo on a device or emulator and confirm colours match desktop
      (watch for the paint colour tinting the vertex colours, the same trap as
      `BlendMode.DST` on Skia).

Done when the Android app shows the shaded cube and teapot. Only the device
run is left; it needs hardware or an emulator, which CI does not provide.

## 2. Orbit camera and gestures

- [x] `OrbitCameraState` holder: yaw, pitch, distance, target. Pitch clamped
      just short of the poles, distance clamped around the framed distance.
      Exposes a `Camera` for the renderer, reusing one instance rather than
      allocating per frame. The angle maths lives in `engine`
      (`compose3d/Orbit.kt`) so it is unit-testable without Compose.
- [x] `Modifier.orbit(state)` built on `detectTransformGestures` for drag and
      pinch, plus a raw pointer handler for scroll wheels.
- [x] Drive animation with `withFrameNanos` in a `LaunchedEffect` instead of
      `rememberInfiniteTransition`, so a still scene asks for no frames.
- [x] Keep all per-frame reads inside the draw lambda: `Model3D` now takes
      `camera: () -> Camera` and `transform: () -> Mat4` rather than values,
      so gestures redraw without recomposing.
- [ ] Optional: velocity-based inertia on release.

Done when the teapot can be orbited and zoomed on desktop with mouse and on
Android with touch. Desktop and the offscreen snapshot are covered; the
touch half shares the device run above.

## 3. Toolchain upgrade and new targets

Current stack: Kotlin 1.8.10, Compose Multiplatform 1.3.1, AGP 7.3.1,
Gradle 7.4, `compileSdk` 33. This blocks iOS and wasm, and Gradle 7.4 does
not run on JDK 21.

- [x] Kotlin 2.x (2.4.20, the newest non-preview release on Maven Central)
      with the `org.jetbrains.kotlin.plugin.compose` compiler plugin, applied
      in every module with `@Composable` code (`common`, `desktop`,
      `android`).
- [x] Compose Multiplatform 1.8 or later (1.12.0, the newest non-preview
      release).
- [x] AGP 9.1.1 (Jetpack Compose 1.12 requires 9.1+; Kotlin 2.4.20 supports
      up to 9.3.1), Gradle 9.5.0, JDK 17 toolchain on every module,
      `compileSdk` 37, `minSdk` 23. `common` uses the
      `com.android.kotlin.multiplatform.library` plugin and the app module
      uses AGP's built-in Kotlin, both required by AGP 9.
- [x] Version catalog (`gradle/libs.versions.toml`), `pluginManagement` +
      `plugins {}` instead of `buildscript {}`.
- [x] Add `iosArm64`/`iosSimulatorArm64` and `wasmJs` targets to `engine`
      and `common`. The Skiko `drawTriangles` actual lives in a shared
      `skikoMain` source set and serves desktop, iOS and wasm unchanged
      (`common/src/skikoMain/kotlin/DrawTriangles.skiko.kt`).
- [x] Add an `android` target to `common`
      (`com.android.kotlin.multiplatform.library`) with its own `androidMain`
      `drawTriangles` actual
      using `android.graphics.Canvas.drawVertices`
      (`common/src/androidMain/kotlin/DrawTriangles.android.kt`), guarded by
      the same `BlendMode.DST`-style trap: a plain white paint so vertex
      colours are not tinted.
- [x] Load the teapot through Compose resources: moved to
      `common/src/commonMain/composeResources/files/teapot.obj`, read via the
      generated `Res.readBytes("files/teapot.obj")`
      (`common/src/commonMain/kotlin/Resources.kt`), so every target shares
      it.
- [x] GitHub Actions workflow (`.github/workflows/ci.yml`): a `test` job
      (ubuntu-latest) runs `:engine:jvmTest`, `:desktop:jvmTest`,
      `:common:compileKotlinWasmJs` and `:android:assembleDebug`, and uploads
      `desktop/build/snapshots/model3d.png` as an artifact; an optional `ios`
      job (macos-latest) runs `:common:compileKotlinIosSimulatorArm64`.

Done when `./gradlew build` passes on JDK 17 and the demo runs in a browser.

Verification status: CI compiles and tests every target except a real
device run. Remaining: run the Android app on a device or emulator (see
item 1) and add a wasm browser entry point so the demo runs in a browser.

## 4. Renderer correctness and performance

- [ ] Near-plane clipping instead of rejection (Sutherland-Hodgman against
      `w = near`, producing one or two triangles). Removes the holes that
      appear when the camera enters a mesh.
- [ ] Frustum culling of whole triangles outside the viewport before the
      sort. Cheap win for zoomed-in views.
- [ ] Depth buffer option. The painter's sort breaks on interpenetrating or
      concave geometry. A software z-buffer into an `ImageBitmap` is the
      portable answer; measure before committing to it.
- [ ] Drop the per-frame `RenderOutput` reallocation. Skiko requires exact
      array sizes, so either keep two output sizes and reallocate only on
      growth, or pass an index array and keep positions at capacity.
- [ ] Use OBJ `vn` records when present instead of always recomputing smooth
      normals. Requires splitting vertices that share a position but not a
      normal.
- [ ] Per-face or per-vertex colours on `Mesh`, and a `Scene` of several
      meshes with their own transforms drawn in one sorted pass.
- [ ] Orthographic camera.
- [ ] Specular term (Blinn-Phong) so Gouraud surfaces read as curved.
- [ ] Micro-benchmark: teapot frame time on desktop, tracked in the README.

Done when the teapot renders correctly from inside its bounding sphere and
frame time for the teapot is documented.

## 5. Library shape

- [ ] Stable public API: `Mesh`, `Camera`, `Light`, `Shading`, `Model3D`,
      `OrbitCameraState`. Everything else `internal`.
- [ ] Split `engine` (pure Kotlin) and `common` (Compose) into publishable
      artifacts with a group id, and publish to Maven Central or GitHub
      Packages.
- [ ] STL (binary and ASCII) loader alongside OBJ.
- [ ] KDoc on the public API and a usage section in the README.

Done when a fresh Compose Multiplatform project can add one dependency and
show a mesh.

## 6. Use-case demos

Each is a small sample module that proves the library covers a need
nothing else in the Compose Multiplatform ecosystem serves.

- [ ] 3D scatter or surface chart built from a `FloatArray` of samples.
- [ ] OBJ/STL file preview: drop a file on the desktop window and view it.
- [ ] Explorable math: show the model, view and projection matrices for the
      current frame and let the user edit them live. Fits the project's
      origin as a learning tool.
- [ ] Composable as texture: capture a composable with
      `rememberGraphicsLayer().toImageBitmap()` and map it onto a mesh via
      `drawVertices` texture coordinates. This is the bridge to 3D UI
      effects that `Modifier.graphicsLayer` cannot do (curved or folded
      surfaces).

## Housekeeping

- [ ] Remove `files/teacup.obj` or wire it into the demo.
- [ ] Golden-image comparison in `SnapshotTest` with a tolerance, instead of
      only checking that pixels are not background.
- [ ] Wireframe mode draws one `drawLine` per edge. Batch into
      `drawPoints(PointMode.Lines)` or a single `Path`.
