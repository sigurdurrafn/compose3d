import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import compose3d.Mesh
import compose3d.parseObj

fun main() = application {
    val mesh = loadResourceMesh("teapot.obj") ?: Mesh.cube()
    Window(
        onCloseRequest = ::exitApplication,
        title = "Compose 3D",
        state = rememberWindowState(width = 640.dp, height = 800.dp),
    ) {
        App(mesh)
    }
}

/** Loads an OBJ file from the classpath, or returns null when it is missing. */
fun loadResourceMesh(name: String): Mesh? {
    val stream = Thread.currentThread().contextClassLoader.getResourceAsStream(name) ?: return null
    return stream.bufferedReader().use { parseObj(it.readText(), name.substringBeforeLast('.')) }
}
