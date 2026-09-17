package compose3d

import com.curiouscreature.kotlin.math.Float3
import com.curiouscreature.kotlin.math.Mat4
import com.curiouscreature.kotlin.math.normalize
import com.curiouscreature.kotlin.math.translation
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RendererTest {
    private val renderer = Renderer()

    private fun assertClose(expected: Float, actual: Float, tolerance: Float = 1e-3f) {
        assertTrue(abs(expected - actual) <= tolerance, "expected $expected but was $actual")
    }

    @Test
    fun `origin projects to the viewport centre`() {
        val point = Mesh("p", floatArrayOf(0f, 0f, 0f, 0.01f, 0f, 0f, 0f, 0.01f, 0f), intArrayOf(0, 1, 2))
        val camera = Camera(position = Float3(0f, 0f, 5f), target = Float3())
        val out = renderer.render(point, Mat4(), camera, 400f, 300f, cullBackFaces = false)
        assertEquals(1, out.triangleCount)
        assertClose(200f, out.positions[0])
        assertClose(150f, out.positions[1])
    }

    @Test
    fun `x goes right and y goes up on screen with the default camera`() {
        val tri = Mesh("t", floatArrayOf(0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f), intArrayOf(0, 1, 2))
        val camera = Camera(position = Float3(0f, 0f, 5f), target = Float3())
        val out = renderer.render(tri, Mat4(), camera, 400f, 400f, cullBackFaces = false)
        assertTrue(out.positions[2] > out.positions[0], "+x should be right of the origin")
        assertTrue(out.positions[5] < out.positions[1], "+y should be above the origin")
    }

    @Test
    fun `perspective shrinks distant geometry`() {
        val tri = Mesh("t", floatArrayOf(0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f), intArrayOf(0, 1, 2))
        val camera = Camera(position = Float3(0f, 0f, 5f), target = Float3())
        val near = renderer.render(tri, Mat4(), camera, 400f, 400f, cullBackFaces = false)
        val nearWidth = near.positions[2] - near.positions[0]
        val far = renderer.render(tri, translation(Float3(0f, 0f, -5f)), camera, 400f, 400f, cullBackFaces = false)
        val farWidth = far.positions[2] - far.positions[0]
        assertClose(nearWidth / 2f, farWidth, 0.01f)
    }

    @Test
    fun `cube seen head-on keeps one face`() {
        val camera = Camera(position = Float3(0f, 0f, 5f), target = Float3())
        val out = renderer.render(Mesh.cube(), Mat4(), camera, 400f, 400f)
        assertEquals(2, out.triangleCount)
        val cullingOff = renderer.render(Mesh.cube(), Mat4(), camera, 400f, 400f, cullBackFaces = false)
        assertEquals(12, cullingOff.triangleCount)
    }

    @Test
    fun `cube seen from a corner keeps three faces`() {
        val camera = Camera(position = Float3(4f, 4f, 4f), target = Float3())
        val out = renderer.render(Mesh.cube(), Mat4(), camera, 400f, 400f)
        assertEquals(6, out.triangleCount)
    }

    @Test
    fun `triangles are emitted far to near`() {
        // Two triangles facing the camera, one at z=0 and one closer at z=2.
        val positions = floatArrayOf(
            -1f, -1f, 0f, 1f, -1f, 0f, 0f, 1f, 0f,
            -1f, -1f, 2f, 1f, -1f, 2f, 0f, 1f, 2f,
        )
        val mesh = Mesh("two", positions, intArrayOf(3, 4, 5, 0, 1, 2))
        val camera = Camera(position = Float3(0f, 0f, 6f), target = Float3())
        val out = renderer.render(mesh, Mat4(), camera, 400f, 400f, shading = Shading.FLAT)
        assertEquals(2, out.triangleCount)
        // The farther (z=0) triangle is smaller on screen and must come first.
        val firstWidth = out.positions[2] - out.positions[0]
        val secondWidth = out.positions[8] - out.positions[6]
        assertTrue(firstWidth < secondWidth, "far triangle should be drawn first")
    }

    @Test
    fun `triangles behind the near plane are dropped`() {
        val tri = Mesh("t", floatArrayOf(0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f), intArrayOf(0, 1, 2))
        val camera = Camera(position = Float3(0f, 0f, -1f), target = Float3(0f, 0f, -2f))
        val out = renderer.render(tri, Mat4(), camera, 400f, 400f, cullBackFaces = false)
        assertEquals(0, out.triangleCount)
    }

    @Test
    fun `triangles beyond the far plane are dropped`() {
        val tri = Mesh("t", floatArrayOf(0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f), intArrayOf(0, 1, 2))
        val camera = Camera(position = Float3(0f, 0f, 5f), target = Float3(), far = 10f)
        val out = renderer.render(
            tri,
            translation(Float3(0f, 0f, -20f)),
            camera,
            400f,
            400f,
            cullBackFaces = false,
        )
        assertEquals(0, out.triangleCount)
    }

    @Test
    fun `flat shading lights faces by their orientation`() {
        val light = Light(direction = Float3(0f, 0f, -1f), ambient = 0f)
        val camera = Camera(position = Float3(0f, 0f, 5f), target = Float3())
        val lit = renderer.render(Mesh.cube(), Mat4(), camera, 400f, 400f, light, 0xFFFFFFFF.toInt(), Shading.FLAT)
        assertEquals(0xFFFFFFFF.toInt(), lit.colors[0])

        // Light travelling straight down: the +z face is edge-on to it and gets only ambient.
        val overhead = Light(direction = Float3(0f, -1f, 0f), ambient = 0.5f)
        val dim = renderer.render(Mesh.cube(), Mat4(), camera, 400f, 400f, overhead, 0xFFFFFFFF.toInt(), Shading.FLAT)
        assertEquals(0xFF7F7F7F.toInt(), dim.colors[0])
    }

    @Test
    fun `gouraud output has one colour per vertex`() {
        val camera = Camera(position = Float3(4f, 4f, 4f), target = Float3())
        val out = renderer.render(Mesh.cube(), Mat4(), camera, 400f, 400f, shading = Shading.GOURAUD)
        assertEquals(out.vertexCount, out.colors.size)
        assertEquals(out.vertexCount * 2, out.positions.size)
    }

    @Test
    fun `cube normals point outwards`() {
        val cube = Mesh.cube()
        for (i in 0 until cube.vertexCount) {
            val p = normalize(Float3(cube.positions[i * 3], cube.positions[i * 3 + 1], cube.positions[i * 3 + 2]))
            val n = Float3(cube.normals[i * 3], cube.normals[i * 3 + 1], cube.normals[i * 3 + 2])
            assertTrue(p.x * n.x + p.y * n.y + p.z * n.z > 0f, "normal $n should face away from the centre at $p")
        }
    }

    @Test
    fun `framing camera fits the mesh`() {
        val camera = Camera.framing(Mesh.cube().bounds)
        val out = renderer.render(Mesh.cube(), Mat4(), camera, 400f, 400f, cullBackFaces = false)
        for (i in 0 until out.vertexCount) {
            assertTrue(out.positions[i * 2] in 0f..400f && out.positions[i * 2 + 1] in 0f..400f)
        }
    }
}
