package compose3d.lessons

import Model3D
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import compose3d.Mesh
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import loadTeapotMesh
import rememberOrbitCamera

/**
 * A cube with each corner stored once: 8 vertices shared by 12 triangles.
 *
 * The library's [Mesh.cube] stores 24 vertices instead, 4 per face, because
 * each face needs its own normal for lighting. That is a later lesson; for
 * seeing how indices share vertices, the 8-corner version is the clear one.
 */
internal fun lessonCube(): Mesh {
    // region lesson:cube-buffers
    val positions = floatArrayOf(
        -1f, -1f, -1f, // 0
         1f, -1f, -1f, // 1
         1f,  1f, -1f, // 2
        -1f,  1f, -1f, // 3
        -1f, -1f,  1f, // 4
         1f, -1f,  1f, // 5
         1f,  1f,  1f, // 6
        -1f,  1f,  1f, // 7
    )
    val indices = intArrayOf(
        0, 3, 2, 0, 2, 1, // back   (-z)
        4, 5, 6, 4, 6, 7, // front  (+z)
        1, 2, 6, 1, 6, 5, // right  (+x)
        0, 4, 7, 0, 7, 3, // left   (-x)
        3, 7, 6, 3, 6, 2, // top    (+y)
        0, 1, 5, 0, 5, 4, // bottom (-y)
    )
    val cube = Mesh("cube", positions, indices)
    // endregion
    return cube
}

/** The cube's 8 vertices as points, numbered by their place in `positions`. */
@Composable
internal fun MeshVerticesDemo() {
    val mesh = remember { lessonCube() }
    val camera = rememberOrbitCamera(mesh, initialYaw = 0.5f, initialPitch = 0.45f)
    val text = rememberTextMeasurer()
    OrbitFrame(camera) {
        ProjectedCanvas(mesh, camera) { projected ->
            drawVertices(projected, mesh.vertexCount, text)
        }
        Caption("${mesh.vertexCount} vertices, ${mesh.positions.size} floats in positions")
    }
}

/** Builds the cube one triangle at a time, showing which three indices each one uses. */
@Composable
internal fun MeshTrianglesDemo() {
    val mesh = remember { lessonCube() }
    val camera = rememberOrbitCamera(mesh, initialYaw = 0.5f, initialPitch = 0.45f)
    val text = rememberTextMeasurer()
    var shown by remember { mutableIntStateOf(1) }
    OrbitFrame(
        camera,
        controls = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val t = shown - 1
                val idx = mesh.indices
                BasicText(
                    if (t < 0) "No triangles yet" else
                        "Triangle $t: indices[${t * 3}..${t * 3 + 2}] = " +
                            "${idx[t * 3]}, ${idx[t * 3 + 1]}, ${idx[t * 3 + 2]}",
                    style = LabelStyle,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Words rather than arrow glyphs: the browser build's fallback font draws those as emoji.
                    Pill("Back") { shown = (shown - 1).coerceAtLeast(0) }
                    Pill("Next") { shown = (shown + 1).coerceAtMost(mesh.triangleCount) }
                    Pill("All", selected = shown == mesh.triangleCount) { shown = mesh.triangleCount }
                    Pill("Reset") { shown = 1 }
                }
            }
        },
    ) {
        ProjectedCanvas(mesh, camera) { projected ->
            val idx = mesh.indices
            for (t in 0 until shown) {
                val current = t == shown - 1
                val a = idx[t * 3]; val b = idx[t * 3 + 1]; val c = idx[t * 3 + 2]
                val path = trianglePath(projected, a, b, c) ?: continue
                drawPath(path, if (current) LessonColors.highlight.copy(alpha = 0.35f) else LessonColors.surface.copy(alpha = 0.18f))
                drawPath(
                    path,
                    if (current) LessonColors.highlight else LessonColors.edge,
                    style = Stroke(width = if (current) 2.dp.toPx() else 1.dp.toPx()),
                )
                if (current) drawWinding(projected, a, b, c, text)
            }
            val highlighted = if (shown > 0) {
                val t = shown - 1
                setOf(idx[t * 3], idx[t * 3 + 1], idx[t * 3 + 2])
            } else {
                emptySet()
            }
            drawVertices(projected, mesh.vertexCount, text, highlighted)
        }
        Caption("${mesh.triangleCount} triangles, ${mesh.indices.size} indices")
    }
}

private enum class TeapotView(val label: String) { POINTS("Points"), WIREFRAME("Wireframe"), SHADED("Shaded") }

/** The Utah teapot as the same two buffers, at a scale where they stop being readable by hand. */
@Composable
internal fun MeshTeapotDemo() {
    var mesh by remember { mutableStateOf<Mesh?>(null) }
    LaunchedEffect(Unit) { mesh = runCatching { loadTeapotMesh() }.getOrNull() }
    val loaded = mesh
    if (loaded == null) {
        Box(Modifier.padding(12.dp)) { BasicText("Loading the teapot…", style = CaptionStyle) }
        return
    }
    var view by remember { mutableStateOf(TeapotView.POINTS) }
    val camera = rememberOrbitCamera(loaded, initialYaw = 0.6f, initialPitch = 0.35f)
    OrbitFrame(
        camera,
        controls = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (option in TeapotView.entries) {
                    Pill(option.label, selected = option == view) { view = option }
                }
            }
        },
    ) {
        when (view) {
            TeapotView.POINTS -> ProjectedCanvas(loaded, camera) { projected ->
                val points = ArrayList<Offset>(loaded.vertexCount)
                for (i in 0 until loaded.vertexCount) {
                    val x = projected[i * 3]
                    if (!x.isNaN()) points.add(Offset(x, projected[i * 3 + 1]))
                }
                drawPoints(points, PointMode.Points, LessonColors.vertex.copy(alpha = 0.8f), 2.dp.toPx(), StrokeCap.Round)
            }
            TeapotView.WIREFRAME -> ProjectedCanvas(loaded, camera) { projected ->
                val path = Path()
                val idx = loaded.indices
                for (t in 0 until loaded.triangleCount) {
                    appendTriangle(path, projected, idx[t * 3], idx[t * 3 + 1], idx[t * 3 + 2])
                }
                drawPath(path, LessonColors.edge.copy(alpha = 0.6f), style = Stroke(width = 0.75f))
            }
            TeapotView.SHADED -> Model3D(loaded, camera = camera::camera, modifier = Modifier.fillMaxSize(), color = LessonColors.surface)
        }
        Caption("${loaded.vertexCount} vertices, ${loaded.triangleCount} triangles")
    }
}

@Composable
private fun BoxScope.Caption(text: String) {
    BasicText(text, style = CaptionStyle, modifier = Modifier.align(Alignment.TopStart).padding(12.dp))
}

/**
 * Draws [count] projected vertices as dots with their index beside them.
 * Nearer dots are larger and brighter, a cheap depth cue for a point cloud.
 */
private fun DrawScope.drawVertices(
    projected: FloatArray,
    count: Int,
    text: TextMeasurer,
    highlighted: Set<Int> = emptySet(),
) {
    var near = Float.POSITIVE_INFINITY
    var far = Float.NEGATIVE_INFINITY
    for (i in 0 until count) {
        val depth = projected[i * 3 + 2]
        if (depth < near) near = depth
        if (depth > far) far = depth
    }
    // Far to near, so nearer labels land on top.
    val order = (0 until count).sortedByDescending { projected[it * 3 + 2] }
    for (i in order) {
        val x = projected[i * 3]
        val y = projected[i * 3 + 1]
        if (x.isNaN()) continue
        val closeness = if (far > near) (far - projected[i * 3 + 2]) / (far - near) else 1f
        val isHighlighted = i in highlighted
        val color = if (isHighlighted) LessonColors.highlight else LessonColors.vertex.copy(alpha = 0.45f + 0.55f * closeness)
        drawCircle(color, radius = (3f + 2.5f * closeness).dp.toPx(), center = Offset(x, y))
        drawText(
            text,
            i.toString(),
            topLeft = Offset(x + 7.dp.toPx(), y - 18.dp.toPx()),
            style = TextStyle(color = color, fontSize = LabelStyle.fontSize),
        )
    }
}

private fun trianglePath(projected: FloatArray, a: Int, b: Int, c: Int): Path? {
    val path = Path()
    return if (appendTriangle(path, projected, a, b, c)) path else null
}

/** Adds the outline of triangle a, b, c to [path]; false if a corner is behind the camera. */
private fun appendTriangle(path: Path, projected: FloatArray, a: Int, b: Int, c: Int): Boolean {
    val ax = projected[a * 3]; val bx = projected[b * 3]; val cx = projected[c * 3]
    if (ax.isNaN() || bx.isNaN() || cx.isNaN()) return false
    path.moveTo(ax, projected[a * 3 + 1])
    path.lineTo(bx, projected[b * 3 + 1])
    path.lineTo(cx, projected[c * 3 + 1])
    path.close()
    return true
}

/**
 * Draws a circular arrow at the triangle's centre showing the order its
 * corners come in, labelled with the side the viewer sees: anticlockwise on
 * screen is the front, clockwise the back.
 */
private fun DrawScope.drawWinding(projected: FloatArray, a: Int, b: Int, c: Int, text: TextMeasurer) {
    val ax = projected[a * 3]; val ay = projected[a * 3 + 1]
    val bx = projected[b * 3]; val by = projected[b * 3 + 1]
    val cx = projected[c * 3]; val cy = projected[c * 3 + 1]
    // Screen y grows downwards, so a negative cross product is anticlockwise to the eye.
    val cross = (bx - ax) * (cy - ay) - (by - ay) * (cx - ax)
    val clockwise = cross > 0f
    val center = Offset((ax + bx + cx) / 3f, (ay + by + cy) / 3f)
    val radius = 9.dp.toPx()
    val start = -90f
    val sweep = if (clockwise) 270f else -270f
    val stroke = 1.5.dp.toPx()
    drawArc(
        LessonColors.highlight,
        startAngle = start,
        sweepAngle = sweep,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(stroke),
    )
    // Arrowhead at the end of the arc, pointing along it.
    val end = ((start + sweep) * PI / 180.0).toFloat()
    val tip = Offset(center.x + radius * cos(end), center.y + radius * sin(end))
    val direction = if (clockwise) Offset(-sin(end), cos(end)) else Offset(sin(end), -cos(end))
    val outward = Offset(cos(end), sin(end))
    val size = 4.dp.toPx()
    val head = Path().apply {
        moveTo(tip.x + direction.x * size, tip.y + direction.y * size)
        lineTo(tip.x - direction.x * size + outward.x * size, tip.y - direction.y * size + outward.y * size)
        lineTo(tip.x - direction.x * size - outward.x * size, tip.y - direction.y * size - outward.y * size)
        close()
    }
    drawPath(head, LessonColors.highlight)
    drawText(
        text,
        if (clockwise) "back" else "front",
        topLeft = Offset(center.x - radius, center.y + radius + 4.dp.toPx()),
        style = LabelStyle.copy(color = LessonColors.highlight),
    )
}
