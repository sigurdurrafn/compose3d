package compose3d.lessons

import OrbitCameraState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.curiouscreature.kotlin.math.Mat4
import compose3d.Mesh
import compose3d.projectVertices
import orbit

/** Colours shared by the lesson demos, picked to sit in the site's dark theme. */
internal object LessonColors {
    val background = Color(0xFF1B1F26)
    val text = Color(0xFFE6E6E6)
    val dim = Color(0xFF8B95A5)
    val vertex = Color(0xFFE6E6E6)
    val edge = Color(0xFF6F7F96)
    /** The site's accent yellow, for whatever the demo is pointing at. */
    val highlight = Color(0xFFF3DB5B)
    /** The base colour [Model3D] shades with, so filled faces match the shaded views. */
    val surface = Color(0xFFCF8A4B)
}

internal val LabelStyle = TextStyle(color = LessonColors.text, fontSize = 13.sp)
internal val CaptionStyle = TextStyle(color = LessonColors.dim, fontSize = 13.sp)

/** Radians per second for the idle spin. */
private const val SPIN_RATE = 0.35f

private val IDENTITY = Mat4()

/**
 * The frame every lesson demo draws in: dark background, drag to orbit (the
 * wheel is left to the page), and a slow spin until the reader first
 * touches it, so a still frame of a point cloud still reads as 3D.
 *
 * [controls] go in a strip below the view rather than over it, so on a
 * narrow screen they never cover the model.
 */
@Composable
internal fun OrbitFrame(
    camera: OrbitCameraState,
    modifier: Modifier = Modifier,
    controls: (@Composable () -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    var spinning by remember { mutableStateOf(true) }
    LaunchedEffect(spinning, camera) {
        if (!spinning) return@LaunchedEffect
        var previous = 0L
        while (true) {
            withFrameNanos { now ->
                if (previous != 0L) camera.yaw += (now - previous) / 1_000_000_000f * SPIN_RATE
                previous = now
            }
        }
    }
    Column(
        modifier
            .fillMaxSize()
            .background(LessonColors.background)
            .pointerInput(Unit) {
                // Watch presses on the way down without consuming them, so
                // the orbit gesture and any buttons still get them.
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        if (event.type == PointerEventType.Press) spinning = false
                    }
                }
            },
    ) {
        Box(
            Modifier.weight(1f).fillMaxWidth().orbit(camera, zoomOnScroll = false),
            content = content,
        )
        if (controls != null) {
            Box(Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp)) { controls() }
        }
    }
}

/**
 * A canvas that projects [mesh] through [camera] each frame and hands the
 * result to [draw]: x, y and depth per vertex, as [projectVertices] writes it.
 */
@Composable
internal fun ProjectedCanvas(
    mesh: Mesh,
    camera: OrbitCameraState,
    modifier: Modifier = Modifier,
    draw: DrawScope.(projected: FloatArray) -> Unit,
) {
    val projected = remember(mesh) { FloatArray(mesh.vertexCount * 3) }
    Canvas(modifier.fillMaxSize()) {
        if (size.width <= 0f || size.height <= 0f) return@Canvas
        projectVertices(mesh, IDENTITY, camera.camera(), size.width, size.height, projected)
        draw(projected)
    }
}

/** A small outlined button; [selected] fills it. */
@Composable
internal fun Pill(text: String, selected: Boolean = false, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    BasicText(
        text,
        style = LabelStyle.copy(color = if (selected) LessonColors.background else LessonColors.text),
        modifier = Modifier
            .border(1.dp, LessonColors.dim, shape)
            .background(if (selected) LessonColors.text else Color.Transparent, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}
