import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.curiouscreature.kotlin.math.Float3
import com.curiouscreature.kotlin.math.rotation
import com.curiouscreature.kotlin.math.translation
import compose3d.Camera
import compose3d.Light
import compose3d.Mesh
import compose3d.RenderOutput
import compose3d.Renderer
import compose3d.Shading

/**
 * Draws [mesh] centred in the composable, rotated by [rotation] (Euler angles
 * in degrees, read at draw time so animating it only redraws, never
 * recomposes). The camera is framed so the whole mesh fits, whatever its size.
 */
@Composable
fun Model3D(
    mesh: Mesh,
    modifier: Modifier = Modifier,
    rotation: () -> Float3 = { Float3() },
    shading: Shading = Shading.GOURAUD,
    cullBackFaces: Boolean = true,
    color: Color = Color(0xFFCF8A4B),
    light: Light = remember { Light() },
    onFrame: ((RenderOutput) -> Unit)? = null,
) {
    val renderer = remember { Renderer() }
    val output = remember { RenderOutput() }
    val camera = remember(mesh) {
        // The mesh is moved to the origin below, so frame a sphere sitting there.
        Camera.framing(compose3d.Bounds(mesh.bounds.min - mesh.bounds.center, mesh.bounds.max - mesh.bounds.center))
    }
    val center = remember(mesh) { mesh.bounds.center }
    val argb = color.toArgb()

    Canvas(modifier) {
        if (size.width <= 0f || size.height <= 0f) return@Canvas
        val model = rotation(rotation()) * translation(-center)
        renderer.render(mesh, model, camera, size.width, size.height, light, argb, shading, cullBackFaces, output)
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
