import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.Paint
import org.jetbrains.skia.VertexMode

private val vertexPaint = Paint().apply { isAntiAlias = false }

actual fun DrawScope.drawTriangles(positions: FloatArray, colors: IntArray) {
    if (positions.isEmpty()) return
    drawContext.canvas.nativeCanvas.drawVertices(
        VertexMode.TRIANGLES,
        positions,
        colors,
        null,
        null,
        // Skia blends vertex colours with the paint colour; DST keeps the vertex colours untouched.
        BlendMode.DST,
        vertexPaint,
    )
}
