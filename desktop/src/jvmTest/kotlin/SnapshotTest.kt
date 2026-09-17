import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.use
import com.curiouscreature.kotlin.math.Float3
import compose3d.Mesh
import compose3d.Shading
import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.EncodedImageFormat
import java.io.File
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

/**
 * Renders the teapot and the cube in every shading mode to a PNG under
 * build/snapshots, using Compose's offscreen scene so no display is needed.
 */
@OptIn(ExperimentalComposeUiApi::class)
class SnapshotTest {
    @Test
    fun `renders teapot and cube offscreen`() {
        val teapot = runBlocking { loadTeapotMesh() }
        val cube = Mesh.cube()
        val tile = 300
        val image = ImageComposeScene(width = tile * 3, height = tile * 2).use { scene ->
            scene.setContent {
                Column {
                    for (mesh in listOf(teapot, cube)) {
                        Row {
                            for (mode in Shading.values()) {
                                Model3D(
                                    mesh = mesh,
                                    modifier = Modifier.size(tile.dp).background(Color(0xFF1B1F26)),
                                    rotation = { Float3(-20f, 35f, 0f) },
                                    shading = mode,
                                )
                            }
                        }
                    }
                }
            }
            scene.render()
        }
        val out = File("build/snapshots/model3d.png")
        out.parentFile.mkdirs()
        val png = assertNotNull(image.encodeToData(EncodedImageFormat.PNG))
        out.writeBytes(png.bytes)

        // The centre of the shaded teapot tile must not be background.
        val bitmap = org.jetbrains.skia.Bitmap.makeFromImage(image)
        val background = bitmap.getColor(5, 5)
        assertNotEquals(background, bitmap.getColor(tile + tile / 2, tile / 2), "flat teapot tile is empty")
        assertNotEquals(background, bitmap.getColor(tile * 2 + tile / 2, tile / 2), "gouraud teapot tile is empty")
    }
}
