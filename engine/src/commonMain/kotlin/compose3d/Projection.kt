package compose3d

import com.curiouscreature.kotlin.math.Float4
import com.curiouscreature.kotlin.math.Mat4

/**
 * Projects every vertex of [mesh] to viewport pixels, writing x, y and depth
 * per vertex into [out].
 *
 * This is the vertex half of [Renderer] written plainly, one matrix product
 * per vertex, for code that draws vertices itself: points, labels, a single
 * highlighted triangle. Depth is the distance in front of the camera (clip
 * w). Vertices behind the near plane get NaN for x and y.
 */
fun projectVertices(
    mesh: Mesh,
    modelMatrix: Mat4,
    camera: Camera,
    viewportWidth: Float,
    viewportHeight: Float,
    out: FloatArray = FloatArray(mesh.vertexCount * 3),
): FloatArray {
    require(out.size >= mesh.vertexCount * 3) { "out needs room for x, y and depth per vertex" }
    val aspect = viewportWidth / viewportHeight
    val modelViewProjection = camera.projectionMatrix(aspect) * camera.viewMatrix() * modelMatrix
    val p = mesh.positions
    for (i in 0 until mesh.vertexCount) {
        val clip = modelViewProjection * Float4(p[i * 3], p[i * 3 + 1], p[i * 3 + 2], 1f)
        val o = i * 3
        if (clip.w < camera.near) {
            out[o] = Float.NaN
            out[o + 1] = Float.NaN
        } else {
            // Perspective divide to normalised device coordinates (-1..1),
            // then scale to pixels. Screen y grows downwards.
            out[o] = (clip.x / clip.w + 1f) * 0.5f * viewportWidth
            out[o + 1] = (1f - clip.y / clip.w) * 0.5f * viewportHeight
        }
        out[o + 2] = clip.w
    }
    return out
}
