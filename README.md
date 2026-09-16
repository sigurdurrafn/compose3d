# compose3d
Software 3D pipeline drawn through Compose, with Skia doing the triangle fill.

`engine` is plain Kotlin with no Compose dependency: `Mesh` (flat vertex and
index buffers), `Camera`, `Light`, `Renderer` and an OBJ parser. `Renderer`
transforms a mesh into screen space, culls back faces, lights it (flat or
Gouraud) and sorts the triangles far to near. `common` holds the `Model3D`
composable, which hands the sorted triangles to the platform canvas through
`drawVertices`. `desktop` and `android` are thin launchers.

Known limits of the current renderer: painter's sort instead of a depth buffer,
triangles crossing the near plane are dropped rather than clipped, and the
Android `drawVertices` actual is not written yet.

### Compose Multiplatform Application

**Desktop**
- `./gradlew run` - run application
- `./gradlew package` - package native distribution into `build/compose/binaries`
- `./gradlew :engine:jvmTest :desktop:jvmTest` - unit tests plus an offscreen
  render of the teapot and cube, written to `desktop/build/snapshots/model3d.png`

**Android**
- `./gradlew installDebug` - install Android application on an Android device (on a real device or on an emulator)
