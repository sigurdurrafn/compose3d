import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.ComposeViewport
import compose3d.Mesh
import kotlinx.browser.document
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.configureWebResources
import org.w3c.dom.HTMLScriptElement

/** The container the standalone page (index.html) provides. */
private const val STANDALONE_CONTAINER = "compose3d"

@OptIn(ExperimentalResourceApi::class)
fun main() {
    // Compose resources resolve against the page URL by default, which breaks
    // when the bundle is embedded in a page at another path (a blog post at
    // /blog/foo). Resolve them against the directory the script came from.
    val script = document.querySelector("script[src$='compose3d.js']") as? HTMLScriptElement
    val base = script?.src?.substringBeforeLast('/')
    if (base != null) {
        configureWebResources { resourcePathMapping { path -> "$base/$path" } }
    }

    if (document.getElementById(STANDALONE_CONTAINER) != null) {
        mountDemo(STANDALONE_CONTAINER, "explorer")
    }
}

/**
 * Mounts the demo named [demoId] into the element with id [containerId].
 *
 * Exported so a host page can place several demos on one page while loading
 * the bundle once. Unknown ids mount the explorer.
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalJsExport::class)
@JsExport
fun mountDemo(containerId: String, demoId: String) {
    val container = requireNotNull(document.getElementById(containerId)) {
        "No element with id '$containerId'"
    }
    ComposeViewport(container) {
        when (demoId) {
            "cube" -> CubeDemo()
            else -> ExplorerDemo()
        }
    }
}

@Composable
private fun ExplorerDemo() {
    // The teapot is a Compose resource, so the browser fetches it after
    // start; the cube stands in until it arrives.
    var mesh by remember { mutableStateOf(Mesh.cube()) }
    LaunchedEffect(Unit) {
        runCatching { loadTeapotMesh() }.getOrNull()?.let { mesh = it }
    }
    App(mesh = mesh)
}

/** A bare model with no controls, the shape most inline article demos will take. */
@Composable
private fun CubeDemo() {
    val mesh = remember { Mesh.cube() }
    val camera = rememberOrbitCamera(mesh, initialYaw = 0.6f, initialPitch = 0.4f)
    Model3D(
        mesh = mesh,
        camera = camera::camera,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B1F26))
            .orbit(camera, zoomOnScroll = false),
    )
}
