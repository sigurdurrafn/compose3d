import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import compose3d.Mesh
import compose3d.demo.App
import compose3d.demo.loadTeapotMesh
import kotlinx.coroutines.runBlocking

fun main() {
    val mesh = runBlocking { runCatching { loadTeapotMesh() }.getOrDefault(Mesh.cube()) }
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Compose 3D",
            state = rememberWindowState(width = 640.dp, height = 800.dp),
        ) {
            App(mesh)
        }
    }
}
