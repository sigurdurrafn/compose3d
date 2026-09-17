package compose3d

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ObjTest {
    @Test
    fun `parses plain triangles`() {
        val mesh = parseObj(
            """
            # comment
            v 0 0 0
            v 1 0 0
            v 0 1 0
            f 1 2 3
            """.trimIndent()
        )
        assertEquals(3, mesh.vertexCount)
        assertContentEquals(intArrayOf(0, 1, 2), mesh.indices)
    }

    @Test
    fun `fan-triangulates quads and strips texture and normal indices`() {
        val mesh = parseObj(
            """
            v 0 0 0
            v 1 0 0
            v 1 1 0
            v 0 1 0
            vn 0 0 1
            vt 0 0
            f 1/1/1 2/1/1 3/1/1 4/1/1
            f 1//1 2//1 3//1
            """.trimIndent()
        )
        assertContentEquals(intArrayOf(0, 1, 2, 0, 2, 3, 0, 1, 2), mesh.indices)
    }

    @Test
    fun `resolves negative indices relative to the vertices seen so far`() {
        val mesh = parseObj(
            """
            v 0 0 0
            v 1 0 0
            v 0 1 0
            f -3 -2 -1
            """.trimIndent()
        )
        assertContentEquals(intArrayOf(0, 1, 2), mesh.indices)
    }

    @Test
    fun `tolerates extra whitespace and unknown records`() {
        val mesh = parseObj("o thing\nv  0   0 0 \nv 1 0 0\r\nv 0 1 0\ns off\nf 1  2 3\n")
        assertEquals(1, mesh.triangleCount)
    }

    @Test
    fun `strips inline comments from records`() {
        val mesh = parseObj("v 0 0 0\nv 1 0 0\nv 0 1 0\nf 1 2 3 # body\n")
        assertEquals(1, mesh.triangleCount)
    }

    @Test
    fun `tolerates tab-delimited records`() {
        val mesh = parseObj("v\t0\t0\t0\nv\t1\t0\t0\nv\t0\t1\t0\nf\t1\t2\t3\n")
        assertEquals(3, mesh.vertexCount)
        assertContentEquals(intArrayOf(0, 1, 2), mesh.indices)
    }
}
