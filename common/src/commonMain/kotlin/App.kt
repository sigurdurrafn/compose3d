import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Text
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

@Composable
fun App() {
    var text by remember { mutableStateOf("Hello, World!") }

    MaterialTheme {
        Button(onClick = {
            text = "Hello, ${getPlatformName()}"
        }) {
            Text(text)
            Canvas(Modifier){
                drawLine(color = Color.Cyan, Offset(0f, 0f), Offset(100f, 100f))
            }
        }
    }
}

expect fun getPlatformName(): String