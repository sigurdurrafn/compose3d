import androidx.compose.animation.animatedFloat
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.onActive
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.curiouscreature.kotlin.math.Float3

@Composable
fun App() {


    MaterialTheme {
        World()
    }
}

@Composable
fun World() {

    val camera = Camera(
        position = Float3(10f, 0.1f, 10.0f),
        target = Float3(0.1f, 0.1f, 1.0f)
    )
    val mesh = cube

    val animatedProgress = animatedFloat(0f)
    onActive {
        animatedProgress.animateTo(
            targetValue = 360f,
            anim = repeatable(
                iterations = AnimationConstants.Infinite,
                animation = tween(durationMillis = 10000, easing = LinearEasing),
            ),
        )
    }
    Canvas(Modifier.size(400.dp, 400.dp)) {
        mesh.rotation = Float3(animatedProgress.value + 90, animatedProgress.value + 180, animatedProgress.value)
        camera.position = Float3(0.01f, 0.01f, -animatedProgress.value)
//        mesh.position = Float3(animatedProgress.value, 0.0f, 0.0f)
        val lines = render(camera, mesh)
        lines.forEach {
            drawCircle(color = Color.Cyan, radius = 10f, center = Offset(it.x + 200, it.y + 200))
            println("Coordinates: $it")
        }
        lines.windowed(size = 2, step = 1) { (start, end) ->
            drawLine(color = Color.Red, start= Offset(start.x + 200, start.y + 200), end = Offset(end.x + 200, end.y + 200))
        }
    }
}

expect fun getPlatformName(): String