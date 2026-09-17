package compose3d

import com.curiouscreature.kotlin.math.Float3
import com.curiouscreature.kotlin.math.Mat4
import com.curiouscreature.kotlin.math.inverse
import com.curiouscreature.kotlin.math.normalize
import com.curiouscreature.kotlin.math.transpose
import kotlin.math.sqrt

enum class Shading { WIREFRAME, FLAT, GOURAUD }

/**
 * Single directional light. [direction] is the direction the light travels in,
 * in world space. [ambient] is the fraction of the base colour a surface keeps
 * when it faces away from the light.
 */
class Light(
    direction: Float3 = Float3(-0.6f, -1f, -0.8f),
    val ambient: Float = 0.25f,
) {
    val direction: Float3 = normalize(direction)
}

/**
 * Triangles ready to be drawn, in screen space, sorted far to near.
 *
 * [positions] holds x,y per vertex and [colors] one ARGB colour per vertex;
 * three consecutive vertices form a triangle. Both arrays are sized exactly to
 * [vertexCount] so they can be handed straight to a `drawVertices` call.
 */
class RenderOutput {
    var positions: FloatArray = FloatArray(0)
        internal set
    var colors: IntArray = IntArray(0)
        internal set
    var triangleCount: Int = 0
        internal set
    val vertexCount: Int get() = triangleCount * 3
}

/**
 * Transforms a [Mesh] into screen-space triangles.
 *
 * Pipeline per frame: model → world (lighting, culling) → clip → NDC →
 * viewport, then a painter's sort on view-space depth. Triangles with any
 * vertex behind the near plane are rejected rather than clipped, which is the
 * one shortcut in here that shows on screen when the camera enters a mesh.
 *
 * The instance keeps its scratch buffers between frames, so reuse one per
 * drawing surface rather than allocating per frame.
 */
class Renderer {
    private var world = FloatArray(0)      // xyz per vertex
    private var clip = FloatArray(0)       // xyzw per vertex
    private var vertexShade = FloatArray(0) // light intensity per vertex (Gouraud)
    private var triPositions = FloatArray(0) // 6 per triangle, unsorted
    private var triColors = IntArray(0)      // 3 per triangle, unsorted
    private var sortKeys = LongArray(0)
    private val m = FloatArray(16)
    private val mvp = FloatArray(16)
    private val nm = FloatArray(9)

    fun render(
        mesh: Mesh,
        modelMatrix: Mat4,
        camera: Camera,
        viewportWidth: Float,
        viewportHeight: Float,
        light: Light = Light(),
        baseColor: Int = 0xFFB0B0B0.toInt(),
        shading: Shading = Shading.GOURAUD,
        cullBackFaces: Boolean = true,
        out: RenderOutput = RenderOutput(),
    ): RenderOutput {
        val vc = mesh.vertexCount
        val tc = mesh.triangleCount
        ensureCapacity(vc, tc)

        val view = camera.viewMatrix()
        val proj = camera.projectionMatrix(viewportWidth / viewportHeight)
        columnMajor(modelMatrix, m)
        columnMajor(proj * view * modelMatrix, mvp)
        normalMatrix(modelMatrix, nm)

        // Vertex stage.
        val p = mesh.positions
        val n = mesh.normals
        val lx = -light.direction.x
        val ly = -light.direction.y
        val lz = -light.direction.z
        val ambient = light.ambient
        for (i in 0 until vc) {
            val x = p[i * 3]; val y = p[i * 3 + 1]; val z = p[i * 3 + 2]
            val wi = i * 3
            world[wi] = m[0] * x + m[4] * y + m[8] * z + m[12]
            world[wi + 1] = m[1] * x + m[5] * y + m[9] * z + m[13]
            world[wi + 2] = m[2] * x + m[6] * y + m[10] * z + m[14]
            val ci = i * 4
            clip[ci] = mvp[0] * x + mvp[4] * y + mvp[8] * z + mvp[12]
            clip[ci + 1] = mvp[1] * x + mvp[5] * y + mvp[9] * z + mvp[13]
            clip[ci + 2] = mvp[2] * x + mvp[6] * y + mvp[10] * z + mvp[14]
            clip[ci + 3] = mvp[3] * x + mvp[7] * y + mvp[11] * z + mvp[15]
            if (shading == Shading.GOURAUD) {
                val nx0 = n[wi]; val ny0 = n[wi + 1]; val nz0 = n[wi + 2]
                var nx = nm[0] * nx0 + nm[3] * ny0 + nm[6] * nz0
                var ny = nm[1] * nx0 + nm[4] * ny0 + nm[7] * nz0
                var nz = nm[2] * nx0 + nm[5] * ny0 + nm[8] * nz0
                val inv = 1f / sqrt(nx * nx + ny * ny + nz * nz).coerceAtLeast(1e-12f)
                nx *= inv; ny *= inv; nz *= inv
                val lambert = (nx * lx + ny * ly + nz * lz).coerceAtLeast(0f)
                vertexShade[i] = ambient + (1f - ambient) * lambert
            }
        }

        // Triangle stage: near-plane rejection, culling, shading, viewport transform.
        val halfW = viewportWidth * 0.5f
        val halfH = viewportHeight * 0.5f
        val eye = camera.position
        val near = camera.near
        val far = camera.far
        val idx = mesh.indices
        var kept = 0
        for (t in 0 until tc) {
            val ia = idx[t * 3]; val ib = idx[t * 3 + 1]; val ic = idx[t * 3 + 2]
            val wa = clip[ia * 4 + 3]; val wb = clip[ib * 4 + 3]; val wcc = clip[ic * 4 + 3]
            if (wa < near || wb < near || wcc < near) continue
            if (wa > far || wb > far || wcc > far) continue

            // Face normal in world space, from the CCW winding.
            val ax = world[ia * 3]; val ay = world[ia * 3 + 1]; val az = world[ia * 3 + 2]
            val abx = world[ib * 3] - ax; val aby = world[ib * 3 + 1] - ay; val abz = world[ib * 3 + 2] - az
            val acx = world[ic * 3] - ax; val acy = world[ic * 3 + 1] - ay; val acz = world[ic * 3 + 2] - az
            val fnx = aby * acz - abz * acy
            val fny = abz * acx - abx * acz
            val fnz = abx * acy - aby * acx
            if (cullBackFaces) {
                val facing = fnx * (eye.x - ax) + fny * (eye.y - ay) + fnz * (eye.z - az)
                if (facing <= 0f) continue
            }

            val o = kept * 6
            emitVertex(ia, wa, halfW, halfH, o)
            emitVertex(ib, wb, halfW, halfH, o + 2)
            emitVertex(ic, wcc, halfW, halfH, o + 4)

            val co = kept * 3
            when (shading) {
                Shading.FLAT -> {
                    val len = sqrt(fnx * fnx + fny * fny + fnz * fnz).coerceAtLeast(1e-12f)
                    val lambert = ((fnx * lx + fny * ly + fnz * lz) / len).coerceAtLeast(0f)
                    val c = shade(baseColor, ambient + (1f - ambient) * lambert)
                    triColors[co] = c; triColors[co + 1] = c; triColors[co + 2] = c
                }
                Shading.GOURAUD -> {
                    triColors[co] = shade(baseColor, vertexShade[ia])
                    triColors[co + 1] = shade(baseColor, vertexShade[ib])
                    triColors[co + 2] = shade(baseColor, vertexShade[ic])
                }
                Shading.WIREFRAME -> {
                    triColors[co] = baseColor; triColors[co + 1] = baseColor; triColors[co + 2] = baseColor
                }
            }

            // Painter's sort key: view-space depth (clip w) in the high bits, triangle in the low bits.
            // Positive floats order the same as their bit patterns.
            val depth = (wa + wb + wcc) * (1f / 3f)
            sortKeys[kept] = (depth.toRawBits().toLong() shl 32) or kept.toLong()
            kept++
        }

        sortKeys.sort(0, kept)

        // Emit far to near.
        if (out.positions.size != kept * 6) out.positions = FloatArray(kept * 6)
        if (out.colors.size != kept * 3) out.colors = IntArray(kept * 3)
        val op = out.positions
        val oc = out.colors
        for (i in 0 until kept) {
            val src = (sortKeys[kept - 1 - i] and 0xFFFFFFFFL).toInt()
            triPositions.copyInto(op, i * 6, src * 6, src * 6 + 6)
            triColors.copyInto(oc, i * 3, src * 3, src * 3 + 3)
        }
        out.triangleCount = kept
        return out
    }

    private fun emitVertex(v: Int, w: Float, halfW: Float, halfH: Float, at: Int) {
        val inv = 1f / w
        val ndcX = clip[v * 4] * inv
        val ndcY = clip[v * 4 + 1] * inv
        triPositions[at] = (ndcX + 1f) * halfW
        triPositions[at + 1] = (1f - ndcY) * halfH // screen y grows downwards
    }

    private fun ensureCapacity(vertices: Int, triangles: Int) {
        if (world.size < vertices * 3) {
            world = FloatArray(vertices * 3)
            clip = FloatArray(vertices * 4)
            vertexShade = FloatArray(vertices)
        }
        if (sortKeys.size < triangles) {
            triPositions = FloatArray(triangles * 6)
            triColors = IntArray(triangles * 3)
            sortKeys = LongArray(triangles)
        }
    }

    private fun columnMajor(mat: Mat4, dst: FloatArray) {
        for (c in 0 until 4) {
            val col = mat[c]
            dst[c * 4] = col.x; dst[c * 4 + 1] = col.y; dst[c * 4 + 2] = col.z; dst[c * 4 + 3] = col.w
        }
    }

    /** Upper-left 3x3 of the inverse transpose, so normals survive non-uniform scale. */
    private fun normalMatrix(model: Mat4, dst: FloatArray) {
        val it = transpose(inverse(model))
        for (c in 0 until 3) {
            val col = it[c]
            dst[c * 3] = col.x; dst[c * 3 + 1] = col.y; dst[c * 3 + 2] = col.z
        }
    }

    private fun shade(argb: Int, intensity: Float): Int {
        val k = intensity.coerceIn(0f, 1f)
        val a = argb ushr 24
        val r = ((argb shr 16 and 0xFF) * k).toInt()
        val g = ((argb shr 8 and 0xFF) * k).toInt()
        val b = ((argb and 0xFF) * k).toInt()
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }
}
