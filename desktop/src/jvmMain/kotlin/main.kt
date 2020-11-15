import androidx.compose.desktop.Window
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.ExperimentalKeyInput
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.shortcuts
import androidx.compose.ui.platform.TextInputServiceAmbient

@OptIn(ExperimentalKeyInput::class)
fun main() = Window {
//    val modifier = Modifier.shortcuts {
//        on(Key.W){
//            print("w")
//        }
//    }



    App()
}