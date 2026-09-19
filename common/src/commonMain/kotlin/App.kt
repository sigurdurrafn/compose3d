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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import compose3d.Mesh
import compose3d.Shading

/** Radians per second while the spin toggle is on. */
private const val SPIN_RATE = 0.5f

@Composable
fun App(mesh: Mesh) {
    MaterialTheme {
        var shading by remember { mutableStateOf(Shading.GOURAUD) }
        var cull by remember { mutableStateOf(true) }
        var spin by remember { mutableStateOf(true) }
        var drawn by remember { mutableStateOf(0) }

        val orbitCamera = rememberOrbitCamera(mesh, initialPitch = 0.35f)

        // Unlike an infinite transition, this stops asking for frames the
        // moment the toggle goes off, so a still scene redraws not at all.
        LaunchedEffect(spin, orbitCamera) {
            if (!spin) return@LaunchedEffect
            var previous = 0L
            while (true) {
                withFrameNanos { now ->
                    if (previous != 0L) {
                        orbitCamera.yaw += (now - previous) / 1_000_000_000f * SPIN_RATE
                    }
                    previous = now
                }
            }
        }

        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "${mesh.name}: ${mesh.triangleCount} triangles, $drawn drawn",
                style = MaterialTheme.typography.subtitle1,
            )

            Model3D(
                mesh = mesh,
                camera = orbitCamera::camera,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color(0xFF1B1F26))
                    .orbit(orbitCamera),
                shading = shading,
                cullBackFaces = cull,
                onFrame = { drawn = it.triangleCount },
            )

            Text("Drag to turn, pinch or scroll to zoom.", style = MaterialTheme.typography.caption)

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
                Button(onClick = { orbitCamera.reset() }) { Text("Reset") }
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
