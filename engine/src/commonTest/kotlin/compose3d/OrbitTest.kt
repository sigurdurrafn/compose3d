package compose3d

import com.curiouscreature.kotlin.math.Float3
import com.curiouscreature.kotlin.math.Mat4
import com.curiouscreature.kotlin.math.length
import kotlin.math.PI
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OrbitTest {
    private val camera = Camera()
    private val origin = Float3()
    private val quarterTurn = (PI / 2.0).toFloat()

    private fun assertClose(expected: Float, actual: Float, tolerance: Float = 1e-3f) {
        assertTrue(abs(expected - actual) <= tolerance, "expected $expected but was $actual")
    }

    @Test
    fun `zero yaw and pitch puts the camera on the +z side`() {
        orbit(camera, origin, yaw = 0f, pitch = 0f, distance = 5f, radius = 1f)
        assertClose(0f, camera.position.x)
        assertClose(0f, camera.position.y)
        assertClose(5f, camera.position.z)
    }

    @Test
    fun `a quarter turn of yaw swings the camera to +x`() {
        orbit(camera, origin, yaw = quarterTurn, pitch = 0f, distance = 5f, radius = 1f)
        assertClose(5f, camera.position.x)
        assertClose(0f, camera.position.y)
        assertClose(0f, camera.position.z)
    }

    @Test
    fun `positive pitch lifts the camera above the target`() {
        orbit(camera, origin, yaw = 0f, pitch = 0.5f, distance = 5f, radius = 1f)
        assertTrue(camera.position.y > 0f, "camera should be above the target")
        assertTrue(camera.position.z > 0f, "camera should stay on the +z side")
    }

    @Test
    fun `pitch is clamped short of the pole`() {
        orbit(camera, origin, yaw = 0f, pitch = 10f, distance = 5f, radius = 1f)
        assertTrue(camera.position.y < 5f, "camera must not reach the pole")
        assertClose(MAX_ORBIT_PITCH, MAX_ORBIT_PITCH)
        // The view matrix stays finite, which is what the clamp is protecting.
        val view = camera.viewMatrix()
        for (c in 0 until 4) {
            val col = view[c]
            for (v in listOf(col.x, col.y, col.z, col.w)) {
                assertTrue(v.isFinite(), "view matrix should stay finite, was $v")
            }
        }
    }

    @Test
    fun `distance is preserved whatever the angles`() {
        for (yaw in listOf(0f, 1f, 2.5f, -3f)) {
            for (pitch in listOf(-1f, 0f, 0.8f)) {
                orbit(camera, origin, yaw, pitch, distance = 7f, radius = 1f)
                assertClose(7f, length(camera.position - origin))
            }
        }
    }

    @Test
    fun `orbiting around an off-centre target keeps that target`() {
        val target = Float3(3f, -2f, 8f)
        orbit(camera, target, yaw = 1.2f, pitch = 0.4f, distance = 4f, radius = 1f)
        assertEquals(target, camera.target)
        assertClose(4f, length(camera.position - target))
    }

    @Test
    fun `clip planes follow the distance and stay positive`() {
        for (distance in listOf(0.01f, 0.5f, 3f, 100f)) {
            orbit(camera, origin, yaw = 0f, pitch = 0f, distance = distance, radius = 2f)
            assertTrue(camera.near > 0f, "near must stay positive, was ${camera.near}")
            assertTrue(camera.far > camera.near, "far must stay beyond near at distance $distance")
        }
    }

    @Test
    fun `framing distance fits the mesh in view`() {
        val cube = Mesh.cube()
        val distance = framingDistance(cube.bounds.radius, fov = 1f)
        orbit(camera, cube.bounds.center, yaw = 0f, pitch = 0f, distance = distance, radius = cube.bounds.radius)
        val out = Renderer().render(cube, Mat4(), camera, 400f, 400f, cullBackFaces = false)
        for (i in 0 until out.vertexCount) {
            assertTrue(
                out.positions[i * 2] in 0f..400f && out.positions[i * 2 + 1] in 0f..400f,
                "vertex $i fell outside the viewport",
            )
        }
    }

    @Test
    fun `an orbit camera at rest matches the framing camera`() {
        val cube = Mesh.cube()
        val framed = Camera.framing(cube.bounds)
        orbit(
            camera,
            cube.bounds.center,
            yaw = 0f,
            pitch = 0f,
            distance = framingDistance(cube.bounds.radius, fov = 1f),
            radius = cube.bounds.radius,
        )
        assertClose(framed.position.z, camera.position.z)
        val renderer = Renderer()
        assertEquals(
            renderer.render(cube, Mat4(), framed, 400f, 400f).triangleCount,
            renderer.render(cube, Mat4(), camera, 400f, 400f).triangleCount,
        )
    }
}
