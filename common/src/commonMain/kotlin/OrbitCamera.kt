import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import compose3d.Bounds
import compose3d.Camera
import compose3d.MAX_ORBIT_PITCH
import compose3d.Mesh
import compose3d.framingDistance
import compose3d.orbit
import kotlin.math.PI
import kotlin.math.pow

/** A drag across the full viewport turns the camera half a circle. */
private const val DRAG_TURN = PI.toFloat()

/** How far one notch of scroll moves the camera, as a factor of the distance. */
private const val SCROLL_STEP = 1.1f

/**
 * Camera that orbits a fixed target, driven by [Modifier.orbit] or by setting
 * [yaw], [pitch] and [distance] directly.
 *
 * The angles are Compose state, so reading [camera] inside a draw lambda means
 * a gesture redraws without recomposing anything.
 */
@Stable
class OrbitCameraState(
    bounds: Bounds,
    private val initialYaw: Float = 0f,
    private val initialPitch: Float = 0f,
    fov: Float = 1f,
) {
    private val target = bounds.center
    private val radius = bounds.radius.coerceAtLeast(1e-3f)
    private val restDistance = framingDistance(radius, fov)

    /** Reused between frames; [camera] rewrites it rather than allocating. */
    private val shared = Camera(fov = fov)

    private var pitchState by mutableStateOf(initialPitch.coerceIn(-MAX_ORBIT_PITCH, MAX_ORBIT_PITCH))
    private var distanceState by mutableStateOf(restDistance)

    /** Angle around the world y axis, in radians. Zero looks along -z. */
    var yaw by mutableStateOf(initialYaw)

    /** Angle above the horizon, in radians, clamped short of the poles. */
    var pitch: Float
        get() = pitchState
        set(value) {
            pitchState = value.coerceIn(-MAX_ORBIT_PITCH, MAX_ORBIT_PITCH)
        }

    /** Distance from the target, clamped to a range around the framed distance. */
    var distance: Float
        get() = distanceState
        set(value) {
            distanceState = value.coerceIn(restDistance * 0.2f, restDistance * 5f)
        }

    /**
     * The camera for the current angles. The returned instance is shared and
     * rewritten on each call, so use it before calling again.
     */
    fun camera(): Camera {
        orbit(shared, target, yaw, pitch, distance, radius)
        return shared
    }

    /** Turns the camera by a drag of [panX] by [panY] pixels across a viewport [extent] pixels across. */
    fun dragBy(panX: Float, panY: Float, extent: Float) {
        if (extent <= 0f) return
        val perPixel = DRAG_TURN / extent
        // Dragging right should swing the near side of the mesh to the right,
        // which means the camera travels the other way.
        yaw -= panX * perPixel
        pitch += panY * perPixel
    }

    /** Applies a pinch, where [zoom] above one moves the camera closer. */
    fun zoomBy(zoom: Float) {
        if (zoom > 0f) distance /= zoom
    }

    /** Applies [notches] of scroll wheel, positive scrolling out. */
    fun scrollBy(notches: Float) {
        distance *= SCROLL_STEP.pow(notches)
    }

    /** Returns to the angles and distance the state started with. */
    fun reset() {
        yaw = initialYaw
        pitch = initialPitch
        distance = restDistance
    }
}

/** Remembers an [OrbitCameraState] framing [mesh]. */
@Composable
fun rememberOrbitCamera(
    mesh: Mesh,
    initialYaw: Float = 0f,
    initialPitch: Float = 0f,
    fov: Float = 1f,
): OrbitCameraState = remember(mesh, fov) {
    OrbitCameraState(mesh.bounds, initialYaw, initialPitch, fov)
}

/**
 * Drives [state] from drag and pinch gestures, and from the scroll wheel on
 * platforms that have one.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.orbit(state: OrbitCameraState): Modifier = this
    .pointerInput(state) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                if (event.type != PointerEventType.Scroll) continue
                var notches = 0f
                for (change in event.changes) notches += change.scrollDelta.y
                if (notches != 0f) {
                    state.scrollBy(notches)
                    for (change in event.changes) change.consume()
                }
            }
        }
    }
    .pointerInput(state) {
        detectTransformGestures { _, pan, zoom, _ ->
            // Read the size per gesture so a resized viewport keeps the same feel.
            state.dragBy(pan.x, pan.y, minOf(size.width, size.height).toFloat())
            if (zoom != 1f) state.zoomBy(zoom)
        }
    }
