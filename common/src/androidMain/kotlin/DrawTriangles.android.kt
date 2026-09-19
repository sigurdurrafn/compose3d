import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas

// android.graphics.Canvas.drawVertices always blends vertex colours with the
// paint colour, the same trap that BlendMode.DST works around on Skia in
// DrawTriangles.skiko.kt. A plain white paint keeps the vertex colours
// untouched instead.
private val vertexPaint = Paint().apply { color = AndroidColor.WHITE }

actual fun DrawScope.drawTriangles(positions: FloatArray, colors: IntArray) {
    if (positions.isEmpty()) return
    drawContext.canvas.nativeCanvas.drawVertices(
        AndroidCanvas.VertexMode.TRIANGLES,
        positions.size,
        positions,
        0,
        null,
        0,
        colors,
        0,
        null,
        0,
        0,
        vertexPaint,
    )
}
