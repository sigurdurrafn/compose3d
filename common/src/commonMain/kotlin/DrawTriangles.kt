import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * Draws a triangle list with per-vertex colours through the platform canvas.
 * [positions] holds x,y per vertex, [colors] one ARGB colour per vertex, and
 * every three vertices form one triangle. Skia interpolates the colours across
 * each triangle, which is what turns per-vertex lighting into Gouraud shading.
 */
expect fun DrawScope.drawTriangles(positions: FloatArray, colors: IntArray)
