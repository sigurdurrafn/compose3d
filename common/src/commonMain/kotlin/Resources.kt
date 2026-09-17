import compose3d.Mesh
import compose3d.parseObj
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.resource

/**
 * Loads the teapot mesh from `teapot.obj`, bundled as a Compose Multiplatform
 * resource (`common/src/commonMain/resources/teapot.obj`) so every target
 * shares one copy and one loader, instead of desktop reading it off its own
 * classpath.
 */
@OptIn(ExperimentalResourceApi::class)
suspend fun loadTeapotMesh(): Mesh {
    val bytes = resource("teapot.obj").readBytes()
    return parseObj(bytes.decodeToString(), "teapot")
}
