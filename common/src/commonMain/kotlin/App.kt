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

    val animatedProgress = animatedFloat(0.01f)
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
        mesh.rotation = Float3(animatedProgress.value + 90, animatedProgress.value + 180, 0.01f)
        val lines = render3d(camera, mesh)
        lines.forEach { (one, two, three) ->
            drawCircle(color = Color.Cyan, radius = 5f, center = Offset(one.x, one.y))
            drawCircle(color = Color.Cyan, radius = 5f, center = Offset(two.x, two.y))
            drawCircle(color = Color.Cyan, radius = 5f, center = Offset(three.x, three.y))
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

expect fun getPlatformName(): String