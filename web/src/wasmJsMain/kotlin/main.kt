import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import compose3d.lessons.Demos
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.configureWebResources
import org.w3c.dom.Element
import org.w3c.dom.HTMLScriptElement

/** The container the standalone page (index.html) provides. */
private const val STANDALONE_CONTAINER = "compose3d"

@OptIn(ExperimentalResourceApi::class)
fun main() {
    // Compose resources resolve against the page URL by default, which breaks
    // when the bundle is embedded in a page at another path (a blog post at
    // /blog/foo). Resolve them against the directory the script came from.
    val script = document.querySelector("script[src$='compose3d.js']") as? HTMLScriptElement
    val base = script?.src?.substringBeforeLast('/')
    if (base != null) {
        configureWebResources { resourcePathMapping { path -> "$base/$path" } }
    }

    if (document.getElementById(STANDALONE_CONTAINER) != null) {
        mountDemo(STANDALONE_CONTAINER, "explorer")
    }
}

/** Demos mounted by [mountDemo] and not yet unmounted, by handle. */
private val mounted = mutableMapOf<Int, Mount>()
private var nextHandle = 0

private class Mount(val container: Element) {
    var active by mutableStateOf(true)
}

/**
 * Mounts the demo named [demoId] into the element with id [containerId] and
 * returns a handle for [unmountDemo].
 *
 * Exported so a host page can place several demos on one page while loading
 * the bundle once. [demoId] is a [Demos] id; unknown ids mount the first
 * demo, the explorer.
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalJsExport::class)
@JsExport
fun mountDemo(containerId: String, demoId: String): Int {
    val container = requireNotNull(document.getElementById(containerId)) {
        "No element with id '$containerId'"
    }
    val handle = nextHandle++
    val mount = Mount(container)
    mounted[handle] = mount
    ComposeViewport(container) {
        if (mount.active) {
            (Demos.find(demoId) ?: Demos.all.first()).content()
        }
    }
    return handle
}

/**
 * Stops the demo behind [handle], for a host page to call when the demo's
 * container leaves the page. Unknown or already unmounted handles are ignored.
 *
 * Compose offers no public way to dispose a viewport, so this does the two
 * parts that matter: it drops the demo from the composition, which cancels
 * its effects and stops any animation asking for frames, and then releases
 * the canvas's WebGL context, since browsers cap live contexts at around 16
 * and a reader moving between posts would otherwise use them up.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun unmountDemo(handle: Int) {
    val mount = mounted.remove(handle) ?: return
    mount.active = false
    // Let Compose draw the now empty frame before the context goes away.
    window.requestAnimationFrame {
        window.requestAnimationFrame { releaseWebGl(mount.container) }
    }
}

/** Loses the WebGL context of every Compose canvas under [container]. */
private fun releaseWebGl(container: Element): Unit = js(
    """{
    for (const host of container.querySelectorAll('div')) {
        const canvas = host.shadowRoot && host.shadowRoot.querySelector('canvas');
        if (!canvas) continue;
        const gl = canvas.getContext('webgl2') || canvas.getContext('webgl');
        const ext = gl && gl.getExtension('WEBGL_lose_context');
        if (ext) ext.loseContext();
    }
    container.replaceChildren();
}"""
)
