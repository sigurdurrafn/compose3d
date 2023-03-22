import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.curiouscreature.kotlin.math.Float3
import com.curiouscreature.kotlin.math.Mat3
import java.io.File

fun main() = application {
    val file = File("src/resources/teapot.obj")
    Window(onCloseRequest = ::exitApplication) {
        App(Mesh("mesh", readObjFile(file)))
    }
}

/**
 * Reads a .obj file and returns an array of triangles
 */
fun readObjFile(file: File): Array<Mat3> {
    val vertices = ArrayList<Float3>()
    val faces = ArrayList<Mat3>()
    file.forEachLine { line ->
        if (line.startsWith("v ")) {
            val (x, y, z) = line.split(" ")
            vertices.add(Float3(x.toFloat(), y.toFloat(), z.toFloat()))
        }
        if (line.startsWith("f ")) {
            val (a, b, c) = line.split(" ")
            faces.add(Mat3(vertices[a.toInt() - 1], vertices[b.toInt() - 1], vertices[c.toInt() - 1]))
        }
    }
    return faces.toTypedArray()
}