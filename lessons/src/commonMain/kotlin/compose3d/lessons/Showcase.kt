package compose3d.lessons

import App
import Model3D
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import compose3d.Mesh
import loadTeapotMesh
import orbit
import rememberOrbitCamera

/** The full demo app: shading modes, culling and spin over the teapot. */
@Composable
internal fun ExplorerDemo() {
    // The teapot is a Compose resource, so the browser fetches it after
    // start; the cube stands in until it arrives.
    var mesh by remember { mutableStateOf(Mesh.cube()) }
    LaunchedEffect(Unit) {
        runCatching { loadTeapotMesh() }.getOrNull()?.let { mesh = it }
    }
    App(mesh = mesh)
}

/** A bare shaded cube with no controls. */
@Composable
internal fun CubeDemo() {
    val mesh = remember { Mesh.cube() }
    val camera = rememberOrbitCamera(mesh, initialYaw = 0.6f, initialPitch = 0.4f)
    Model3D(
        mesh = mesh,
        camera = camera::camera,
        modifier = Modifier
            .fillMaxSize()
            .background(LessonColors.background)
            .orbit(camera, zoomOnScroll = false),
    )
}
