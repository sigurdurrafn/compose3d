package compose3d

import com.curiouscreature.kotlin.math.Float3
import com.curiouscreature.kotlin.math.Mat4
import com.curiouscreature.kotlin.math.rotation
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class ProjectionTest {
    private fun assertClose(expected: Float, actual: Float, tolerance: Float = 1e-3f) {
        assertTrue(abs(expected - actual) <= tolerance, "expected $expected but was $actual")
    }

    @Test
    fun `agrees with the renderer on every vertex`() {
        val mesh = Mesh.cube()
        val model = rotation(Float3(20f, 35f, 0f))
        val camera = Camera(position = Float3(1f, 2f, 6f), target = Float3())
        val projected = projectVertices(mesh, model, camera, 640f, 480f)

        // One triangle at a time, so the renderer's sort cannot reorder them.
        for (t in 0 until mesh.triangleCount) {
            val corners = mesh.indices.copyOfRange(t * 3, t * 3 + 3)
            val single = Mesh("t", mesh.positions, corners)
            val out = Renderer().render(single, model, camera, 640f, 480f, cullBackFaces = false)
            for (k in 0 until 3) {
                val v = corners[k]
                assertClose(out.positions[k * 2], projected[v * 3])
                assertClose(out.positions[k * 2 + 1], projected[v * 3 + 1])
            }
        }
    }

    @Test
    fun `depth grows with distance from the camera`() {
        val mesh = Mesh("p", floatArrayOf(0f, 0f, 0f, 0f, 0f, -3f), IntArray(0))
        val camera = Camera(position = Float3(0f, 0f, 5f), target = Float3())
        val projected = projectVertices(mesh, Mat4(), camera, 100f, 100f)
        assertClose(5f, projected[2])
        assertClose(8f, projected[5])
    }

    @Test
    fun `vertices behind the camera have no screen position`() {
        val mesh = Mesh("p", floatArrayOf(0f, 0f, 10f), IntArray(0))
        val camera = Camera(position = Float3(0f, 0f, 5f), target = Float3())
        val projected = projectVertices(mesh, Mat4(), camera, 100f, 100f)
        assertTrue(projected[0].isNaN() && projected[1].isNaN())
    }
}
