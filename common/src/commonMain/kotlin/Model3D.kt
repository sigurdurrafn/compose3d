import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.curiouscreature.kotlin.math.Mat4
import compose3d.Camera
import compose3d.Light
import compose3d.Mesh
import compose3d.RenderOutput
import compose3d.Renderer
import compose3d.Shading

/** Shared because the renderer only reads the model matrix. */
private val IDENTITY = Mat4()

/**
 * Draws [mesh] as seen by [camera].
 *
 * [camera] and [transform] are called at draw time rather than read during
 * composition, so moving the camera or animating the transform redraws
 * without recomposing. Pair it with [rememberOrbitCamera] and
 * [Modifier.orbit] for a view the user can turn.
 */
@Composable
fun Model3D(
    mesh: Mesh,
    camera: () -> Camera,
    modifier: Modifier = Modifier,
    transform: () -> Mat4 = { IDENTITY },
    shading: Shading = Shading.GOURAUD,
    cullBackFaces: Boolean = true,
    color: Color = Color(0xFFCF8A4B),
    light: Light = remember { Light() },
    onFrame: ((RenderOutput) -> Unit)? = null,
) {
    val renderer = remember { Renderer() }
    val output = remember { RenderOutput() }
    val argb = color.toArgb()

    Canvas(modifier) {
        if (size.width <= 0f || size.height <= 0f) return@Canvas
        renderer.render(
            mesh,
            transform(),
            camera(),
            size.width,
            size.height,
            light,
            argb,
            shading,
            cullBackFaces,
            output,
        )
        if (shading == Shading.WIREFRAME) {
            val p = output.positions
            var i = 0
            while (i < p.size) {
                val a = Offset(p[i], p[i + 1])
                val b = Offset(p[i + 2], p[i + 3])
                val c = Offset(p[i + 4], p[i + 5])
                drawLine(color, a, b)
                drawLine(color, b, c)
                drawLine(color, c, a)
                i += 6
            }
        } else {
            drawTriangles(output.positions, output.colors)
        }
        onFrame?.invoke(output)
    }
}
