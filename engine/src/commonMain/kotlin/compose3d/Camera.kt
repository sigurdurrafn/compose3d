package compose3d

import com.curiouscreature.kotlin.math.Float3
import com.curiouscreature.kotlin.math.Mat4
import com.curiouscreature.kotlin.math.degrees
import com.curiouscreature.kotlin.math.inverse
import com.curiouscreature.kotlin.math.lookAt
import com.curiouscreature.kotlin.math.perspective

/**
 * Perspective camera. [fov] is the vertical field of view in radians.
 */
class Camera(
    var position: Float3 = Float3(0f, 0f, 5f),
    var target: Float3 = Float3(),
    var up: Float3 = Float3(y = 1f),
    var fov: Float = 1f,
    var near: Float = 0.1f,
    var far: Float = 100f,
) {
    /**
     * World-to-camera transform. `lookAt` in kotlin-math builds the camera's
     * own frame (camera-to-world), so the view matrix is its inverse.
     */
    fun viewMatrix(): Mat4 = inverse(lookAt(position, target, up))

    /** Camera-to-clip transform for a viewport with the given width/height ratio. */
    fun projectionMatrix(aspect: Float): Mat4 = perspective(degrees(fov), aspect, near, far)

    companion object {
        /**
         * Camera placed on [direction] from the mesh centre, far enough back
         * that the mesh's bounding sphere fits in the vertical field of view.
         */
        fun framing(bounds: Bounds, direction: Float3 = Float3(0f, 0f, 1f), fov: Float = 1f, margin: Float = 1.1f): Camera {
            val radius = bounds.radius.coerceAtLeast(1e-3f)
            val distance = framingDistance(radius, fov, margin)
            val dir = com.curiouscreature.kotlin.math.normalize(direction)
            return Camera(
                position = bounds.center + dir * distance,
                target = bounds.center,
                fov = fov,
                near = (distance - radius * 2f).coerceAtLeast(distance * 0.01f),
                far = distance + radius * 2f,
            )
        }
    }
}
