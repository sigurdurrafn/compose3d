package compose3d.lessons

import androidx.compose.runtime.Composable

/** A demo a host page can mount by [id]. */
class Demo(val id: String, val title: String, val content: @Composable () -> Unit)

/**
 * Every demo, by id. Published posts refer to demos by id, so treat an id as
 * public once a post uses it.
 */
object Demos {
    val all: List<Demo> = listOf(
        Demo("explorer", "Renderer explorer") { ExplorerDemo() },
        Demo("cube", "Cube") { CubeDemo() },
        // Post 1: meshes.
        Demo("mesh-vertices", "A cube's vertices") { MeshVerticesDemo() },
        Demo("mesh-triangles", "Triangles from indices") { MeshTrianglesDemo() },
        Demo("mesh-teapot", "The teapot") { MeshTeapotDemo() },
    )

    fun find(id: String): Demo? = all.firstOrNull { it.id == id }
}
