package compose3d

import com.curiouscreature.kotlin.math.Float3
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Axis-aligned bounds of a mesh in model space.
 */
class Bounds(val min: Float3, val max: Float3) {
    val center: Float3 get() = (min + max) * 0.5f

    /** Radius of the sphere centred on [center] that encloses the box. */
    val radius: Float get() {
        val d = max - min
        return 0.5f * sqrt(d.x * d.x + d.y * d.y + d.z * d.z)
    }
}

// region lesson:mesh-class
/**
 * Indexed triangle mesh stored in flat buffers.
 *
 * [positions] holds xyz per vertex, [indices] holds three vertex indices per
 * triangle. Front faces are counter-clockwise when seen from outside, which is
 * the OBJ convention. [normals] holds one unit normal per vertex and is derived
 * from the geometry (area-weighted, smooth) when not supplied.
 */
class Mesh(
    val name: String,
    val positions: FloatArray,
    val indices: IntArray,
    normals: FloatArray? = null,
) {
    init {
        require(positions.size % 3 == 0) { "positions must hold xyz triples" }
        require(indices.size % 3 == 0) { "indices must hold triangles" }
        require(normals == null || normals.size == positions.size) { "one normal per vertex" }
    }

    val vertexCount: Int get() = positions.size / 3
    val triangleCount: Int get() = indices.size / 3
    // endregion

    val normals: FloatArray = normals ?: computeSmoothNormals(positions, indices)

    val bounds: Bounds by lazy {
        if (vertexCount == 0) return@lazy Bounds(Float3(), Float3())
        val lo = Float3(Float.POSITIVE_INFINITY)
        val hi = Float3(Float.NEGATIVE_INFINITY)
        for (i in 0 until vertexCount) {
            val x = positions[i * 3]
            val y = positions[i * 3 + 1]
            val z = positions[i * 3 + 2]
            if (x < lo.x) lo.x = x; if (x > hi.x) hi.x = x
            if (y < lo.y) lo.y = y; if (y > hi.y) hi.y = y
            if (z < lo.z) lo.z = z; if (z > hi.z) hi.z = z
        }
        Bounds(lo, hi)
    }

    companion object {
        /**
         * Unit cube centred on the origin with side length [size]. Faces are
         * not shared between sides so that the smooth normals stay flat.
         */
        fun cube(size: Float = 2f): Mesh {
            val h = size / 2f
            // Each face: 4 corners in CCW order seen from outside, then two triangles.
            val faces = arrayOf(
                // -z
                floatArrayOf(-h, -h, -h, -h, h, -h, h, h, -h, h, -h, -h),
                // +z
                floatArrayOf(h, -h, h, h, h, h, -h, h, h, -h, -h, h),
                // +x
                floatArrayOf(h, -h, -h, h, h, -h, h, h, h, h, -h, h),
                // -x
                floatArrayOf(-h, -h, h, -h, h, h, -h, h, -h, -h, -h, -h),
                // +y
                floatArrayOf(-h, h, -h, -h, h, h, h, h, h, h, h, -h),
                // -y
                floatArrayOf(h, -h, h, -h, -h, h, -h, -h, -h, h, -h, -h),
            )
            val positions = FloatArray(6 * 4 * 3)
            val indices = IntArray(6 * 2 * 3)
            for (f in faces.indices) {
                faces[f].copyInto(positions, f * 12)
                val base = f * 4
                val o = f * 6
                indices[o] = base; indices[o + 1] = base + 1; indices[o + 2] = base + 2
                indices[o + 3] = base; indices[o + 4] = base + 2; indices[o + 5] = base + 3
            }
            return Mesh("Cube", positions, indices)
        }
    }
}

internal fun computeSmoothNormals(positions: FloatArray, indices: IntArray): FloatArray {
    val normals = FloatArray(positions.size)
    var t = 0
    while (t < indices.size) {
        val a = indices[t] * 3
        val b = indices[t + 1] * 3
        val c = indices[t + 2] * 3
        val abx = positions[b] - positions[a]
        val aby = positions[b + 1] - positions[a + 1]
        val abz = positions[b + 2] - positions[a + 2]
        val acx = positions[c] - positions[a]
        val acy = positions[c + 1] - positions[a + 1]
        val acz = positions[c + 2] - positions[a + 2]
        // Cross product; its length is twice the triangle area, which is the weight we want.
        val nx = aby * acz - abz * acy
        val ny = abz * acx - abx * acz
        val nz = abx * acy - aby * acx
        for (v in intArrayOf(a, b, c)) {
            normals[v] += nx
            normals[v + 1] += ny
            normals[v + 2] += nz
        }
        t += 3
    }
    var i = 0
    while (i < normals.size) {
        val x = normals[i]
        val y = normals[i + 1]
        val z = normals[i + 2]
        val len = sqrt(x * x + y * y + z * z)
        val inv = 1f / max(len, 1e-12f)
        normals[i] = x * inv
        normals[i + 1] = y * inv
        normals[i + 2] = z * inv
        i += 3
    }
    return normals
}
