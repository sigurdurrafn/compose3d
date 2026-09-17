package compose3d

/**
 * Parses Wavefront OBJ text into a [Mesh].
 *
 * Handles `v` and `f` records, `a`, `a/b`, `a//c` and `a/b/c` index forms,
 * negative (relative) indices, and fan-triangulates polygons with more than
 * three corners. Normals in the file are ignored; smooth normals are derived
 * from the geometry instead. Everything else is skipped.
 */
fun parseObj(text: String, name: String = "obj"): Mesh {
    val positions = FloatArrayBuilder()
    val indices = IntArrayBuilder()
    val corners = IntArray(8).let { IntArrayBuilder(it) }

    for (rawLine in text.lineSequence()) {
        val commentStart = rawLine.indexOf('#')
        val line = (if (commentStart < 0) rawLine else rawLine.substring(0, commentStart)).trim()
        if (line.length < 2) continue
        when {
            line[0] == 'v' && line[1].isWhitespace() -> {
                val parts = line.split(WHITESPACE)
                if (parts.size < 4) continue
                positions.add(parts[1].toFloat())
                positions.add(parts[2].toFloat())
                positions.add(parts[3].toFloat())
            }
            line[0] == 'f' && line[1].isWhitespace() -> {
                val parts = line.split(WHITESPACE)
                corners.clear()
                val vertexCount = positions.size / 3
                for (i in 1 until parts.size) {
                    val token = parts[i]
                    val slash = token.indexOf('/')
                    val raw = (if (slash < 0) token else token.substring(0, slash)).toInt()
                    // OBJ indices are 1-based; negative ones count back from the last vertex.
                    corners.add(if (raw < 0) vertexCount + raw else raw - 1)
                }
                for (i in 1 until corners.size - 1) {
                    indices.add(corners[0])
                    indices.add(corners[i])
                    indices.add(corners[i + 1])
                }
            }
        }
    }
    return Mesh(name, positions.toArray(), indices.toArray())
}

private val WHITESPACE = Regex("\\s+")

private class FloatArrayBuilder(private var data: FloatArray = FloatArray(1024)) {
    var size = 0
        private set

    fun add(v: Float) {
        if (size == data.size) data = data.copyOf(size * 2)
        data[size++] = v
    }

    fun toArray(): FloatArray = data.copyOf(size)
}

private class IntArrayBuilder(private var data: IntArray = IntArray(1024)) {
    var size = 0
        private set

    fun add(v: Int) {
        if (size == data.size) data = data.copyOf(size * 2)
        data[size++] = v
    }

    operator fun get(i: Int) = data[i]

    fun clear() {
        size = 0
    }

    fun toArray(): IntArray = data.copyOf(size)
}
