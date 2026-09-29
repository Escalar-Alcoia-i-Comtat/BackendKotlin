package data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * The drawing of a sector's routes, described as data.
 *
 * The server renders it on top of the sector's topo background image (`Sectors.topo_image`) to produce the sector's
 * image. All coordinates are normalized to the background image: `0..1` from left to right and from top to bottom.
 *
 * The drawing is a graph: [nodes] (starts, anchors, belays, junctions) are joined by [edges], and each route in [routes]
 * is an ordered list of edges. Shared parts, like an anchor used by several routes, are therefore drawn once.
 */
@Serializable
data class Topo(
    val version: Int = VERSION,
    val imageWidth: Int,
    val imageHeight: Int,
    val nodes: List<Node> = emptyList(),
    val edges: List<Edge> = emptyList(),
    val routes: List<Route> = emptyList(),
    val bolts: List<Bolt> = emptyList(),
    val labels: List<Label> = emptyList(),
    val copyright: Copyright? = null
) {
    companion object {
        const val VERSION = 1

        /** Coordinates may slightly exceed the image, since lines can start or end on its border. */
        private const val COORDINATE_MARGIN = 0.05

        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }

    @Serializable
    data class Point(val x: Double, val y: Double)

    @Serializable
    data class Node(
        val id: String,
        val x: Double,
        val y: Double,
        val type: NodeType,
        /** Text shown next to the node, for example "R1" for a belay. */
        val label: String? = null
    )

    @Serializable
    enum class NodeType { START, ANCHOR, BELAY, JUNCTION }

    /**
     * A line from node [from] to node [to], made of consecutive cubic Bézier [curves]. The first curve starts at [from],
     * and each curve starts where the previous one ends. The last curve should end at [to].
     */
    @Serializable
    data class Edge(
        val id: String,
        val from: String,
        val to: String,
        val curves: List<Cubic>,
        val style: EdgeStyle = EdgeStyle.SOLID
    )

    @Serializable
    data class Cubic(val c1: Point, val c2: Point, val end: Point)

    @Serializable
    enum class EdgeStyle { SOLID, DASHED }

    /**
     * The line of the route with ID [pathId], as an ordered list of [edges].
     * [numberAt] and [gradeAt] set where the route's number and grade are drawn. When missing, the number goes at the
     * route's start, and the grade at the middle of the line.
     */
    @Serializable
    data class Route(
        val pathId: Int,
        val edges: List<String>,
        val numberAt: Point? = null,
        val gradeAt: Point? = null
    )

    @Serializable
    data class Bolt(val x: Double, val y: Double, val pathId: Int? = null)

    /**
     * Free text on the drawing, like a pitch length ("20 m"). [size] is the text height relative to the image width.
     * [font] is the family to draw it with: "Source Sans 3" (the default), "Signika" or "Work Sans".
     */
    @Serializable
    data class Label(
        val x: Double,
        val y: Double,
        val text: String,
        val size: Double? = null,
        val font: String? = null
    )

    @Serializable
    data class Copyright(val text: String, val corner: Corner = Corner.BOTTOM_RIGHT)

    @Serializable
    enum class Corner { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }

    /**
     * Checks that the topo can be rendered.
     * @return The list of problems found. Empty if the topo is valid.
     */
    fun validate(): List<String> {
        val problems = mutableListOf<String>()
        if (version > VERSION) problems += "Unsupported topo version $version (max $VERSION)"
        if (imageWidth <= 0 || imageHeight <= 0) problems += "Invalid image size ${imageWidth}x$imageHeight"

        val range = -COORDINATE_MARGIN..(1 + COORDINATE_MARGIN)
        fun check(what: String, x: Double, y: Double) {
            if (!x.isFinite() || !y.isFinite() || x !in range || y !in range) problems += "$what is out of bounds ($x, $y)"
        }

        val nodeIds = nodes.map { it.id }
        if (nodeIds.size != nodeIds.toSet().size) problems += "Duplicated node IDs"
        nodes.forEach { check("Node ${it.id}", it.x, it.y) }

        val edgeIds = edges.map { it.id }
        if (edgeIds.size != edgeIds.toSet().size) problems += "Duplicated edge IDs"
        for (edge in edges) {
            if (edge.from !in nodeIds) problems += "Edge ${edge.id} starts at unknown node ${edge.from}"
            if (edge.to !in nodeIds) problems += "Edge ${edge.id} ends at unknown node ${edge.to}"
            if (edge.curves.isEmpty()) problems += "Edge ${edge.id} has no curves"
            edge.curves.forEachIndexed { i, curve ->
                listOf(curve.c1, curve.c2, curve.end).forEach { check("Edge ${edge.id} curve $i", it.x, it.y) }
            }
        }

        for (route in routes) {
            route.edges.filter { it !in edgeIds }.forEach { problems += "Route ${route.pathId} uses unknown edge $it" }
            route.numberAt?.let { check("Route ${route.pathId} number", it.x, it.y) }
            route.gradeAt?.let { check("Route ${route.pathId} grade", it.x, it.y) }
        }
        bolts.forEach { check("Bolt", it.x, it.y) }
        labels.forEach { check("Label \"${it.text}\"", it.x, it.y) }

        return problems
    }
}
