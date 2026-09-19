package compose3d

import com.curiouscreature.kotlin.math.Float3
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Largest pitch an orbit camera may reach, a little short of the pole where
 * the camera's forward axis would line up with its up axis and the view
 * matrix would collapse.
 */
const val MAX_ORBIT_PITCH: Float = (PI / 2.0).toFloat() - 0.01f

/**
 * Distance at which a sphere of the given [radius] fills the vertical field of
 * view [fov] (in radians), with [margin] to spare.
 */
fun framingDistance(radius: Float, fov: Float, margin: Float = 1.1f): Float =
    radius.coerceAtLeast(1e-3f) * margin / sin(fov / 2f)

/**
 * Moves [camera] to a point on the sphere of radius [distance] around
 * [target], looking back at it.
 *
 * [yaw] turns around the world y axis and starts on the +z side, so zero
 * reproduces the default head-on view. [pitch] lifts towards +y and is clamped
 * to [MAX_ORBIT_PITCH]. The near and far planes follow the distance so that a
 * mesh of the given [radius] stays between them at any zoom level.
 *
 * The camera's vectors are updated in place, so this can run every frame
 * without allocating.
 */
fun orbit(
    camera: Camera,
    target: Float3,
    yaw: Float,
    pitch: Float,
    distance: Float,
    radius: Float,
) {
    val p = pitch.coerceIn(-MAX_ORBIT_PITCH, MAX_ORBIT_PITCH)
    val d = distance.coerceAtLeast(1e-3f)
    val horizontal = cos(p) * d

    camera.position.x = target.x + horizontal * sin(yaw)
    camera.position.y = target.y + d * sin(p)
    camera.position.z = target.z + horizontal * cos(yaw)

    camera.target.x = target.x
    camera.target.y = target.y
    camera.target.z = target.z

    // Keep the whole mesh in front of the near plane even when the camera is
    // pulled in close, and never let near reach zero.
    camera.near = (d - radius).coerceAtLeast(d * 0.01f)
    camera.far = d + radius * 2f
}
