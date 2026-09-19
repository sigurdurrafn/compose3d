import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import compose3d.Mesh

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(viewportContainerId = "compose3d", configure = {}) {
        // The teapot is a Compose resource, so the browser fetches it after
        // start; the cube stands in until it arrives.
        var mesh by remember { mutableStateOf(Mesh.cube()) }
        LaunchedEffect(Unit) {
            runCatching { loadTeapotMesh() }.getOrNull()?.let { mesh = it }
        }
        App(mesh = mesh)
    }
}
