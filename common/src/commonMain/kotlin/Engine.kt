import com.curiouscreature.kotlin.math.*

class Camera(
    var position: Float3 = Float3(),
    var target: Float3 = Float3()
)

class Mesh(val name: String, val triangles: Array<Mat3>) {
    var position = Float3()
    var rotation = Float3()
}

val cube = Mesh(
    "Cube", arrayOf(

        // SOUTH
        Mat3(Float3(-1.0f, -1.0f, -1.0f), Float3(-1.0f, 1.0f, -1.0f), Float3(1.0f, 1.0f, -1.0f)),
        Mat3(Float3(-1.0f, -1.0f, -1.0f), Float3(1.0f, 1.0f, -1.0f), Float3(1.0f, -1.0f, -1.0f)),

        // EAST
        Mat3(Float3(1.0f, -1.0f, -1.0f), Float3(1.0f, 1.0f, -1.0f), Float3(1.0f, 1.0f, 1.0f)),
        Mat3(Float3(1.0f, -1.0f, -1.0f), Float3(1.0f, 1.0f, 1.0f), Float3(1.0f, -1.0f, 1.0f)),

        // NORTH
        Mat3(Float3(1.0f, -1.0f, 1.0f), Float3(1.0f, 1.0f, 1.0f), Float3(-1.0f, 1.0f, 1.0f)),
        Mat3(Float3(1.0f, -1.0f, 1.0f), Float3(-1.0f, 1.0f, 1.0f), Float3(-1.0f, -1.0f, 1.0f)),

        // WEST
        Mat3(Float3(-1.0f, -1.0f, 1.0f), Float3(-1.0f, 1.0f, 1.0f), Float3(-1.0f, 1.0f, -1.0f)),
        Mat3(Float3(-1.0f, -1.0f, 1.0f), Float3(-1.0f, 1.0f, -1.0f), Float3(-1.0f, -1.0f, -1.0f)),

        // TOP
        Mat3(Float3(-1.0f, 1.0f, -1.0f), Float3(-1.0f, 1.0f, 1.0f), Float3(1.0f, 1.0f, 1.0f)),
        Mat3(Float3(-1.0f, 1.0f, -1.0f), Float3(1.0f, 1.0f, 1.0f), Float3(1.0f, 1.0f, -1.0f)),

        // BOTTOM
        Mat3(Float3(1.0f, -1.0f, 1.0f), Float3(-1.0f, -1.0f, 1.0f), Float3(-1.0f, -1.0f, -1.0f)),
        Mat3(Float3(1.0f, -1.0f, 1.0f), Float3(-1.0f, -1.0f, -1.0f), Float3(1.0f, -1.0f, -1.0f)),
        )
)

fun project(coord: Float3, transMat: Mat4): Float2 {
    val (x, y, _, _) = transMat * (Float4(coord, 0f))
    return Float2(x, y)
}

/**
 * Project triangle to 2d
 * this was a shot in the dark, might be a bug here. Looks ok
 */
fun projectTriangle(triangle: Mat3, transMat: Mat4): List<Float2> {

    val newMat = Mat4(triangle.x, triangle.y, triangle.z, Float3())
    val projected = transMat * newMat

    return listOf(
        Float2(projected[0][0], projected[0][1]),
        Float2(projected[1][0], projected[1][1]),
        Float2(projected[2][0], projected[2][1]),
    )
}

fun render3d(camera: Camera, mesh: Mesh): List<List<Float2>> {

    val viewMatrix = lookAt(camera.position, camera.target)
    val projectionMatrix = perspective(0.78f, 1f, 0.01f, 20f)
    val worldMatrix = rotation(mesh.rotation) * translation(mesh.position)
    val transformMatrix = projectionMatrix * viewMatrix * worldMatrix

    return mesh.triangles.map { projectTriangle(it, transformMatrix) }
}

