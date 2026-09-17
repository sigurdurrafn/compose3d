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

The only platform without a filled-triangle path. `common` is a JVM-only
module today, so the Android app picks up the desktop Skiko actual, which
cannot work at runtime.

- [ ] Give `common` an `android` target with `androidMain` next to `jvmMain`
      (blocked on item 3 for a sane AGP/Compose pairing, can be done together).
- [ ] Implement `DrawScope.drawTriangles` with
      `android.graphics.Canvas.drawVertices(TRIANGLES, count, verts, 0, null, 0, colors, 0, null, 0, 0, paint)`.
      Android's API takes counts and offsets, so no per-frame array copy is
      needed there.
- [ ] Check the minimum API level at which hardware-accelerated canvases
      honour `drawVertices` with per-vertex colours, and either raise `minSdk`
      or fall back to `drawPath` per triangle below it.
- [ ] Run the demo on a device or emulator and confirm colours match desktop
      (watch for the paint colour tinting the vertex colours, the same trap as
      `BlendMode.DST` on Skia).

Done when the Android app shows the shaded cube and teapot.

## 2. Orbit camera and gestures

The demo only auto-rotates. A viewer needs drag to orbit and pinch to zoom.

- [ ] `OrbitCameraState` holder: yaw, pitch, distance, target. Pitch clamped
      just short of the poles. Exposes a `Camera` for the renderer.
- [ ] `Modifier.orbit(state)` built on `detectTransformGestures` (pan,
      zoom) plus mouse scroll on desktop.
- [ ] Drive animation with `withFrameNanos` in a `LaunchedEffect` instead of
      `rememberInfiniteTransition`, so idle scenes stop redrawing.
- [ ] Keep all per-frame reads inside the draw lambda so gestures redraw
      without recomposing.
- [ ] Optional: velocity-based inertia on release.

Done when the teapot can be orbited and zoomed on desktop with mouse and on
Android with touch.

## 3. Toolchain upgrade and new targets

Current stack: Kotlin 1.8.10, Compose Multiplatform 1.3.1, AGP 7.3.1,
Gradle 7.4, `compileSdk` 33. This blocks iOS and wasm, and Gradle 7.4 does
not run on JDK 21.

- [ ] Kotlin 2.x with the Compose compiler Gradle plugin.
- [ ] Compose Multiplatform 1.8 or later.
- [ ] AGP 8.x, Gradle 8.x, JDK 17 toolchain, `compileSdk` 35.
- [ ] Version catalog (`gradle/libs.versions.toml`).
- [ ] Add `iosArm64`/`iosSimulatorArm64` and `wasmJs` targets to `engine`
      and `common`. The Skiko `drawVertices` actual should serve iOS and wasm
      unchanged; verify.
- [ ] Load the teapot through Compose resources so all targets share it.
- [ ] GitHub Actions workflow running `:engine:jvmTest` and `:desktop:jvmTest`
      on push, uploading `desktop/build/snapshots/model3d.png` as an artifact.

Done when `./gradlew build` passes on JDK 17 and the demo runs in a browser.

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
