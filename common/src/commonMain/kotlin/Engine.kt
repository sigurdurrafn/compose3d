import com.curiouscreature.kotlin.math.*

class Camera(
    var position: Float3 = Float3(),
    var target: Float3 = Float3()
)

class Mesh(val name: String, val vertices: Array<Float3>) {
    var position = Float3()
    var rotation = Float3()
}

val cube = Mesh(
    "Cube", arrayOf(
        Float3(-1f, 1f, 1f),
        Float3(1f, 1f, 1f),
        Float3(-1f, -1f, 1f),
        Float3(-1f, -1f, -1f),
        Float3(-1f, 1f, -1f),
        Float3(1f, 1f, -1f),
        Float3(1f, -1f, 1f),
        Float3(1f, -1f, -1f),
    )
)

fun project(coord: Float3, transMat: Mat4): Float2 {
    val (x, y, _, _) = transMat * (Float4(coord, 0f))
    return Float2(x, y)
}

fun render(camera: Camera, mesh: Mesh): List<Float2> {

    val viewMatrix = lookAt(camera.position, camera.target)
    val projectionMatrix = perspective(0.78f, 1f, 0.01f, 20f)
    val worldMatrix = rotation(mesh.rotation) * translation(mesh.position)
    val transformMatrix = projectionMatrix * viewMatrix * worldMatrix

    return mesh.vertices.map { project(it, transformMatrix) }
}

