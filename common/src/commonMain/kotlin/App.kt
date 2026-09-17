import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.curiouscreature.kotlin.math.Float3
import compose3d.Mesh
import compose3d.Shading

@Composable
fun App(mesh: Mesh) {
    MaterialTheme {
        var shading by remember { mutableStateOf(Shading.GOURAUD) }
        var cull by remember { mutableStateOf(true) }
        var spin by remember { mutableStateOf(true) }
        var drawn by remember { mutableStateOf(0) }

        val turn by rememberInfiniteTransition().animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(durationMillis = 12000, easing = LinearEasing), RepeatMode.Restart),
        )

        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${mesh.name}: ${mesh.triangleCount} triangles, $drawn drawn", style = MaterialTheme.typography.subtitle1)

            Model3D(
                mesh = mesh,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(Color(0xFF1B1F26)),
                rotation = { Float3(-20f, if (spin) turn else 30f, 0f) },
                shading = shading,
                cullBackFaces = cull,
                onFrame = { drawn = it.triangleCount },
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (mode in Shading.values()) {
                    val selected = mode == shading
                    Button(
                        onClick = { shading = mode },
                        colors = if (selected) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
                    ) {
                        Text(mode.name.lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Switch(checked = cull, onCheckedChange = { cull = it })
                Text("Cull back faces")
                Switch(checked = spin, onCheckedChange = { spin = it })
                Text("Spin")
            }
        }
    }
}
