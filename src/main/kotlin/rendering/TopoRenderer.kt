package rendering

import Logger
import ServerDatabase
import data.Grade
import data.Topo
import database.EntityTypes
import database.entity.Path
import database.entity.Sector
import database.entity.info.LastUpdate
import database.table.Paths
import distribution.Notifier
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.Shape
import java.awt.font.TextLayout
import java.awt.geom.AffineTransform
import java.awt.geom.Ellipse2D
import java.awt.geom.Path2D
import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import java.io.File
import java.time.Instant
import java.util.UUID
import javax.imageio.ImageIO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.eq
import storage.Storage

/**
 * Renders a sector's [Topo] on top of its topo background, producing the image served as the sector's image.
 *
 * Route colours and grade labels follow the app (`GradeValue.color` and `asString()` in the app's `Grade.kt`), so
 * rendered images match the rest of the app. Sizes are relative to the image width, measured from the original
 * Illustrator drawings.
 */
object TopoRenderer {
    private const val LINE_WIDTH = 0.004
    private const val OUTLINE_WIDTH = 0.0075
    private const val DASH_LENGTH = 0.0127
    private const val DASH_GAP = 0.0063
    private const val BOLT_RADIUS = 0.0038
    private const val ANCHOR_RADIUS = 0.0095
    private const val BADGE_RADIUS = 0.0145
    private const val TEXT_SIZE = 0.02
    private const val LABEL_SIZE = 0.018
    private const val COPYRIGHT_SIZE = 0.016
    private const val COPYRIGHT_MARGIN = 0.015
    private const val TEXT_OUTLINE = 0.003
    private const val NUMBER_OFFSET = 0.028
    private const val BADGE_PADDING = 0.004
    private const val BADGE_RING = 0.0015
    private const val CURVE_STEPS = 16

    private val White = Color(0xFFFFFF)
    private val Dark = Color(0x1E1C20)
    private val BoltFill = Color(0xE8E8E6)
    private val BoltStroke = Color(0x3A393D)

    // Same as the app's light theme grade colours (ui/theme/Color.kt)
    private val Grade1 = Color(0x006E07)
    private val Grade2 = Color(0x0055D4)
    private val Grade3 = Color(0xBF0027)
    private val Grade4 = Color(0x984061)
    private val GradeA = Color(0x825500)
    private val GradeP = Color(0x8800EB)

    /** The data of a route needed to draw it. */
    data class RouteInfo(val sketchId: UInt, val grade: Grade?, val aidGrade: Grade?) {
        /** The grade shown on the drawing: the sports grade, or the aid grade if there's none. */
        val displayGrade: Grade? get() = grade ?: aidGrade
    }

    /** Colour of the routes with the given [grade], matching `GradeValue.color` in the app. */
    fun color(grade: Grade?): Color {
        if (grade == null || grade == Grade.UNKNOWN) return GradeP
        if (grade.name.startsWith("A")) return GradeA
        val number = grade.name[1].digitToIntOrNull() ?: return GradeP
        return when {
            number <= 5 -> Grade1
            number <= 6 -> Grade2
            number <= 7 -> Grade3
            else -> Grade4
        }
    }

    /** Text of the given [grade], matching `asString()` in the app (for example "6b+", "5º", "A2+", "Ae"). */
    fun label(grade: Grade): String {
        if (grade == Grade.UNKNOWN) return "¿?"
        var string = grade.name
        if (string.startsWith("A")) {
            if (string.endsWith("_PLUS")) string = string.substringBeforeLast("_PLUS") + '+'
            if (string.endsWith("_EQUIPPED")) string = string.substringBeforeLast("_EQUIPPED") + 'e'
            return string
        }
        if (string.startsWith("G")) string = string.substring(1)
        if (string.endsWith("_PLUS")) string = string.substringBeforeLast("_PLUS") + '+'
        if (string.length == 1) string += 'º'
        return string.lowercase()
    }

    /**
     * Draws [topo] on top of [background].
     * @param routes Data of the sector's routes, by path ID. Routes of the topo missing here are skipped.
     */
    fun render(topo: Topo, background: BufferedImage, routes: Map<Int, RouteInfo>): BufferedImage {
        val width = background.width
        val height = background.height
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g = image.createGraphics()
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            g.drawImage(background, 0, 0, null)

            Drawing(g, topo, width.toDouble(), height.toDouble(), routes).draw()
        } finally {
            g.dispose()
        }
        return image
    }

    private class Drawing(
        private val g: Graphics2D,
        private val topo: Topo,
        private val width: Double,
        private val height: Double,
        routes: Map<Int, RouteInfo>
    ) {
        private val nodes = topo.nodes.associateBy { it.id }
        private val edges = topo.edges.associateBy { it.id }
        private val routes = topo.routes
            .mapNotNull { route -> routes[route.pathId]?.let { route to it } }
            .sortedBy { (_, info) -> info.sketchId }

        private fun x(value: Double) = value * width
        private fun y(value: Double) = value * height
        private fun size(value: Double) = (value * width).toFloat()

        private fun edgeShape(edge: Topo.Edge): Shape? {
            val start = nodes[edge.from] ?: return null
            val end = nodes[edge.to]
            return Path2D.Double().apply {
                moveTo(x(start.x), y(start.y))
                edge.curves.forEachIndexed { index, curve ->
                    // Snap the end of the last curve to the end node, so lines always meet at shared nodes
                    val point = if (index == edge.curves.lastIndex && end != null) Topo.Point(end.x, end.y) else curve.end
                    curveTo(x(curve.c1.x), y(curve.c1.y), x(curve.c2.x), y(curve.c2.y), x(point.x), y(point.y))
                }
            }
        }

        private fun lineStroke(style: Topo.EdgeStyle): BasicStroke {
            val dash = if (style == Topo.EdgeStyle.DASHED) floatArrayOf(size(DASH_LENGTH), size(DASH_GAP)) else null
            return BasicStroke(size(LINE_WIDTH), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, dash, 0f)
        }

        fun draw() {
            // Outlines of all the routes first, so no outline covers another route's line
            g.color = White
            g.stroke = BasicStroke(size(OUTLINE_WIDTH), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            for ((route, _) in routes) route.edges.mapNotNull { edges[it]?.let(::edgeShape) }.forEach(g::draw)

            for ((route, info) in routes) {
                g.color = color(info.displayGrade)
                for (edge in route.edges.mapNotNull { edges[it] }) {
                    g.stroke = lineStroke(edge.style)
                    edgeShape(edge)?.let(g::draw)
                }
            }

            drawBolts()
            drawNodes()
            for ((route, info) in routes) drawRouteTexts(route, info)
            for (label in topo.labels) {
                drawText(label.text, x(label.x), y(label.y), size(label.size ?: LABEL_SIZE), White, Dark, TopoFonts.forFamily(label.font))
            }
            topo.copyright?.let(::drawCopyright)
        }

        private fun drawBolts() {
            val radius = size(BOLT_RADIUS).toDouble()
            g.stroke = BasicStroke(size(BOLT_RADIUS / 2.5))
            for (bolt in topo.bolts) {
                val circle = Ellipse2D.Double(x(bolt.x) - radius, y(bolt.y) - radius, radius * 2, radius * 2)
                g.color = BoltFill
                g.fill(circle)
                g.color = BoltStroke
                g.draw(circle)
            }
        }

        private fun drawNodes() {
            for (node in topo.nodes) {
                if (node.type != Topo.NodeType.ANCHOR && node.type != Topo.NodeType.BELAY) continue
                val radius = size(if (node.type == Topo.NodeType.BELAY) ANCHOR_RADIUS * 0.8 else ANCHOR_RADIUS).toDouble()
                val circle = Ellipse2D.Double(x(node.x) - radius, y(node.y) - radius, radius * 2, radius * 2)
                g.color = White
                g.fill(circle)
                g.color = Dark
                g.stroke = BasicStroke(size(TEXT_OUTLINE))
                g.draw(circle)
                node.label?.let { label ->
                    drawText(label, x(node.x) + radius * 2.4, y(node.y), size(LABEL_SIZE), White, Dark, TopoFonts.label)
                }
            }
        }

        /** Position of the route's first point, or null if its first edge is unknown. */
        private fun routeStart(route: Topo.Route): Point2D? {
            val first = route.edges.firstNotNullOfOrNull { edges[it] } ?: return null
            val node = nodes[first.from] ?: return null
            return Point2D.Double(x(node.x), y(node.y))
        }

        /** The point halfway along the route's line, measured on a flattened version of its curves. */
        private fun routeMiddle(route: Topo.Route): Point2D? {
            val points = mutableListOf<Point2D>()
            for (edge in route.edges.mapNotNull { edges[it] }) {
                val start = nodes[edge.from] ?: continue
                var previous = Point2D.Double(x(start.x), y(start.y))
                if (points.isEmpty()) points += previous
                for (curve in edge.curves) {
                    val end = Point2D.Double(x(curve.end.x), y(curve.end.y))
                    for (step in 1..CURVE_STEPS) {
                        points += cubicPoint(previous, curve, end, step.toDouble() / CURVE_STEPS)
                    }
                    previous = end
                }
            }
            if (points.size < 2) return points.firstOrNull()

            val lengths = points.zipWithNext { a, b -> a.distance(b) }
            var remaining = lengths.sum() / 2
            for ((index, length) in lengths.withIndex()) {
                if (remaining <= length && length > 0) {
                    val t = remaining / length
                    val a = points[index]
                    val b = points[index + 1]
                    return Point2D.Double(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)
                }
                remaining -= length
            }
            return points.last()
        }

        private fun cubicPoint(start: Point2D, curve: Topo.Cubic, end: Point2D, t: Double): Point2D {
            val u = 1 - t
            val c1x = x(curve.c1.x)
            val c1y = y(curve.c1.y)
            val c2x = x(curve.c2.x)
            val c2y = y(curve.c2.y)
            return Point2D.Double(
                u * u * u * start.x + 3 * u * u * t * c1x + 3 * u * t * t * c2x + t * t * t * end.x,
                u * u * u * start.y + 3 * u * u * t * c1y + 3 * u * t * t * c2y + t * t * t * end.y
            )
        }

        private fun drawRouteTexts(route: Topo.Route, info: RouteInfo) {
            val color = color(info.displayGrade)

            // The route's number, in a badge of the route's colour
            val numberPosition = route.numberAt?.let { Point2D.Double(x(it.x), y(it.y)) }
                ?: routeStart(route)?.let { Point2D.Double(it.x, it.y + size(NUMBER_OFFSET)) }
            numberPosition?.let { position ->
                val number = info.sketchId.toString()
                val textSize = size(TEXT_SIZE * 0.85)
                val textWidth = TextLayout(number, TopoFonts.number.deriveFont(textSize), g.fontRenderContext).bounds.width
                // Wide numbers ("12a") get a wider badge
                val radius = maxOf(size(BADGE_RADIUS).toDouble(), textWidth / 2 + size(BADGE_PADDING))
                val badge = Ellipse2D.Double(position.x - radius, position.y - radius, radius * 2, radius * 2)
                g.color = color
                g.fill(badge)
                // A thin white ring keeps the badge visible on dark rock
                g.color = White
                g.stroke = BasicStroke(size(BADGE_RING))
                g.draw(badge)
                drawText(number, position.x, position.y, textSize, White, null, TopoFonts.number)
            }

            val grade = info.displayGrade ?: return
            val gradePosition = route.gradeAt?.let { Point2D.Double(x(it.x), y(it.y)) } ?: routeMiddle(route) ?: return
            val radius = size(BADGE_RADIUS).toDouble()
            val badge = Ellipse2D.Double(gradePosition.x - radius, gradePosition.y - radius, radius * 2, radius * 2)
            g.color = White
            g.fill(badge)
            drawText(label(grade), gradePosition.x, gradePosition.y, size(TEXT_SIZE * 0.85), color, null, TopoFonts.grade)
        }

        private fun drawCopyright(copyright: Topo.Copyright) {
            val font = TopoFonts.copyright.deriveFont(size(COPYRIGHT_SIZE))
            val layout = TextLayout(copyright.text, font, g.fontRenderContext)
            val margin = size(COPYRIGHT_MARGIN).toDouble()
            val textWidth = layout.bounds.width
            val textHeight = layout.bounds.height
            val left = copyright.corner == Topo.Corner.TOP_LEFT || copyright.corner == Topo.Corner.BOTTOM_LEFT
            val top = copyright.corner == Topo.Corner.TOP_LEFT || copyright.corner == Topo.Corner.TOP_RIGHT
            val centerX = if (left) margin + textWidth / 2 else width - margin - textWidth / 2
            val centerY = if (top) margin + textHeight / 2 else height - margin - textHeight / 2
            drawText(copyright.text, centerX, centerY, size(COPYRIGHT_SIZE), White, Dark, TopoFonts.copyright)
        }

        /** Draws [text] in [baseFont], centered on ([centerX], [centerY]), with an optional [outline] for legibility. */
        private fun drawText(
            text: String,
            centerX: Double,
            centerY: Double,
            textSize: Float,
            fill: Color,
            outline: Color?,
            baseFont: Font
        ) {
            if (text.isBlank()) return
            val font = baseFont.deriveFont(textSize)
            val layout = TextLayout(text, font, g.fontRenderContext)
            val bounds = layout.bounds
            val transform = AffineTransform.getTranslateInstance(
                centerX - bounds.x - bounds.width / 2,
                centerY - bounds.y - bounds.height / 2
            )
            val shape = layout.getOutline(transform)
            if (outline != null) {
                g.color = outline
                g.stroke = BasicStroke(size(TEXT_OUTLINE) * 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
                g.draw(shape)
            }
            g.color = fill
            g.fill(shape)
        }
    }

    /**
     * Renders the topo of the sector with ID [sectorId], stores the result as the sector's image, and notifies clients.
     * Does nothing if the sector doesn't have a topo and a topo background.
     * @param notify Whether to notify clients of the change. Endpoints that notify on their own pass `false`.
     * @return `true` if the image was rendered.
     */
    suspend fun renderSector(sectorId: Int, notify: Boolean = true): Boolean {
        val input = ServerDatabase.instance.query {
            val sector = Sector.findById(sectorId) ?: return@query null
            val topo = sector.topo ?: return@query null
            val background = sector.topoImage ?: return@query null
            val routes = Path.find { Paths.sector eq sector.id }
                .associate { it.id.value to RouteInfo(it.sketchId, it.grade, it.aidGrade) }
            Triple(topo, background, routes)
        } ?: return false
        val (topo, backgroundFile, routes) = input

        val file = withContext(Dispatchers.IO) {
            val background = ImageIO.read(backgroundFile) ?: error("Could not read topo background $backgroundFile")
            val rendered = render(topo, background, routes)
            val target = File(Storage.ImagesDir, "${UUID.randomUUID()}.webp")
            if (!ImageIO.write(rendered, "webp", target)) error("No image writer available for webp")
            target
        }

        ServerDatabase.instance.query {
            val sector = Sector.findById(sectorId) ?: return@query
            val previous = runCatching { sector.image }.getOrNull()
            sector.image = file
            sector.timestamp = Instant.now()
            // Never delete the background, even if it was used as the image
            if (previous != null && previous != sector.topoImage) previous.delete()
            LastUpdate.set()
        }
        if (notify) Notifier.getInstance().notifyUpdated(EntityTypes.SECTOR, sectorId)
        return true
    }

    /**
     * Re-renders the image of the sector with ID [sectorId] if it has a topo, for example after one of its routes
     * changed. Errors are logged and don't propagate, since rendering must never break other requests.
     */
    suspend fun rerenderIfNeeded(sectorId: Int) {
        try {
            renderSector(sectorId)
        } catch (e: Exception) {
            Logger.error("Could not render the topo of sector $sectorId", e)
        }
    }
}
