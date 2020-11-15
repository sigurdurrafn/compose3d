import com.curiouscreature.kotlin.math.*

class Camera(
    var position: Float3 = Float3(),
    val target: Float3 = Float3()
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

    val projected = transMat.times(Float4(coord, 0f))

    return Float2(projected.x, projected.y)
}

fun render(camera: Camera, mesh: Mesh): List<Float2> {
//    println("lookAt(${camera.position}, ${camera.target})")

    val viewMatrix = lookAt(camera.position, camera.target)
    val projectionMatrix = perspective(0.78f, 1f, 0.01f, 2f)
    val worldMatrix = rotation(mesh.rotation) * translation(mesh.position)
    val transformMatrix = worldMatrix * viewMatrix * projectionMatrix

    println(
        """ViewMatrix:
        |$viewMatrix
        |projection:
        |$projectionMatrix
        |world:
        |$worldMatrix
        |transformMatrix:
        |$transformMatrix
        |""".trimMargin()
    )

    return mesh.vertices.map { project( it, transformMatrix) }
}

//var transformMatrix = worldMatrix * viewMatrix * projectionMatrix;

val cube2 = Mesh(
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

