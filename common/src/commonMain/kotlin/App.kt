import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.curiouscreature.kotlin.math.Float3

@Composable
fun App(mesh: Mesh) {
    MaterialTheme {
        Column {
            val drawingVertices = remember {  mutableStateOf(false) }
            val camerax = remember { mutableStateOf(0.1f) }
            Text("Fancy 3d world of imagination!!!")
            World(mesh, drawingVertices, camerax)
            Button({
                drawingVertices.value = !drawingVertices.value
            }) {
                Text("Toggle vertices")
            }

            Button({
                camerax.value = camerax.value + 0.1f
            }) {
                Text("Camera")
            }

            Text("Camera: ${camerax.value}")
        }
    }
}

@Composable
fun World(mesh: Mesh = cube, drawVertices: MutableState<Boolean>, camerax: MutableState<Float>) {

    val camera = Camera(
        position = Float3(20f, 110.1f, 110.0f),
        target = Float3(0.1f, camerax.value, 1.0f)
    )
    Text("Camera: ${camera.target.y}")
    val animatedProgress by rememberInfiniteTransition().animateFloat(
        initialValue = 0.01f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
        ),
    )

    Canvas(Modifier.size(400.dp, 400.dp)) {
        mesh.rotation = Float3(animatedProgress + 90, animatedProgress + 180, 0.01f)
        camera.target.y += 0.1f
        camera.target.x += 0.1f
        camera.target.z -= 0.1f
        val lines = render3d(camera, mesh)
        lines.forEach { (one, two, three) ->
            if (drawVertices.value) {
                drawCircle(color = Color.Cyan, radius = 5f, center = Offset(one.x, one.y))
                drawCircle(color = Color.Cyan, radius = 5f, center = Offset(two.x, two.y))
                drawCircle(color = Color.Cyan, radius = 5f, center = Offset(three.x, three.y))
            }
            drawLine(
                color = Color.Red,
                start = Offset(one.x, one.y),
                end = Offset(two.x, two.y)
            )
            drawLine(
                color = Color.Red,
                start = Offset(two.x, two.y),
                end = Offset(three.x, three.y)
            )
            drawLine(
                color = Color.Red,
                start = Offset(three.x, three.y),
                end = Offset(one.x, one.y)
            )
        }
    }
}