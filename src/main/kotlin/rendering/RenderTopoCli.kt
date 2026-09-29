package rendering

import data.Grade
import data.Topo
import java.io.File
import javax.imageio.ImageIO
import kotlin.system.exitProcess
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer

/**
 * Renders topos from the command line, without a database or a running server. It takes the output of the app's topo
 * importer (`./gradlew :composeApp:importTopo` in the app) and draws it exactly like the server would.
 *
 * Usage: `./gradlew renderTopo -Pdir=<directory>`
 *
 * The directory must contain `topo.json`, `background.png` and `routes.json`; `rendered.png` is written next to them.
 * If the directory has no `topo.json`, every subdirectory that has one is rendered.
 */
fun main(args: Array<String>) {
    val dir = args.firstOrNull()?.takeIf { it.isNotBlank() }?.let(::File)
    if (dir == null || !dir.isDirectory) {
        System.err.println("Usage: renderTopo <directory with topo.json, background.png and routes.json>")
        exitProcess(1)
    }
    val directories = if (File(dir, "topo.json").exists()) {
        listOf(dir)
    } else {
        dir.listFiles { file -> File(file, "topo.json").exists() }.orEmpty().sortedBy { it.name }
    }
    for (directory in directories) {
        print("${directory.name}... ")
        try {
            RenderTopoCli.render(directory)
            println("ok")
        } catch (e: Exception) {
            println("error: ${e.message}")
        }
    }
}

object RenderTopoCli {
    /** A route's data, as written by the app's importer. */
    @Serializable
    data class RouteData(val pathId: Int, val sketchId: Int, val grade: String? = null)

    fun render(directory: File) {
        val topo = Topo.json.decodeFromString(Topo.serializer(), File(directory, "topo.json").readText())
        val problems = topo.validate()
        require(problems.isEmpty()) { "Invalid topo: ${problems.joinToString("; ")}" }

        val routes = File(directory, "routes.json").takeIf { it.exists() }
            ?.let { Topo.json.decodeFromString(ListSerializer(RouteData.serializer()), it.readText()) }
            ?.associate { route ->
                val grade = route.grade?.let { name -> Grade.entries.find { it.name == name } }
                route.pathId to TopoRenderer.RouteInfo(route.sketchId.toUInt(), grade, null)
            }
            // Without route data, draw every route with its ID as number and an unknown grade
            ?: topo.routes.associate { it.pathId to TopoRenderer.RouteInfo(it.pathId.toUInt(), null, null) }

        val background = ImageIO.read(File(directory, "background.png"))
            ?: error("Could not read background.png")
        ImageIO.write(TopoRenderer.render(topo, background, routes), "png", File(directory, "rendered.png"))
    }
}
