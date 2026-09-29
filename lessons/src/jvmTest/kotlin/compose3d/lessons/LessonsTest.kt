package compose3d.lessons

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LessonsTest {
    @Test
    fun `demo ids are unique`() {
        val ids = Demos.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size, "duplicate ids in $ids")
    }

    @Test
    fun `lesson cube shares its 8 corners between 12 triangles`() {
        val cube = lessonCube()
        assertEquals(8, cube.vertexCount)
        assertEquals(12, cube.triangleCount)
        assertEquals((0 until 8).toSet(), cube.indices.toSet())
    }

    @Test
    fun `lesson cube triangles wind anticlockwise seen from outside`() {
        // The cube is centred on the origin, so a face normal pointing away
        // from the origin means the triangle's front faces outwards.
        val cube = lessonCube()
        val p = cube.positions
        for (t in 0 until cube.triangleCount) {
            val (a, b, c) = (0..2).map { cube.indices[t * 3 + it] * 3 }
            val ab = FloatArray(3) { p[b + it] - p[a + it] }
            val ac = FloatArray(3) { p[c + it] - p[a + it] }
            val n = floatArrayOf(
                ab[1] * ac[2] - ab[2] * ac[1],
                ab[2] * ac[0] - ab[0] * ac[2],
                ab[0] * ac[1] - ab[1] * ac[0],
            )
            val outward = n[0] * p[a] + n[1] * p[a + 1] + n[2] * p[a + 2]
            assertTrue(outward > 0f, "triangle $t faces inwards")
        }
    }
}
