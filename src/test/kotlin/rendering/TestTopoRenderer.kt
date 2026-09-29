package rendering

import data.Grade
import data.TestTopo
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestTopoRenderer {
    private fun grayBackground(size: Int = 1000) = BufferedImage(size, size, BufferedImage.TYPE_INT_RGB).apply {
        createGraphics().apply {
            color = Color(128, 128, 128)
            fillRect(0, 0, size, size)
            dispose()
        }
    }

    private fun BufferedImage.colorAt(x: Double, y: Double) = Color(getRGB((x * width).toInt(), (y * height).toInt()))

    @Test
    fun `test grade colors match the app`() {
        assertEquals(Color(0x006E07), TopoRenderer.color(Grade.G5_PLUS))
        assertEquals(Color(0x0055D4), TopoRenderer.color(Grade.G6A))
        assertEquals(Color(0xBF0027), TopoRenderer.color(Grade.G7B_PLUS))
        assertEquals(Color(0x984061), TopoRenderer.color(Grade.G8A))
        assertEquals(Color(0x825500), TopoRenderer.color(Grade.A2))
        assertEquals(Color(0x8800EB), TopoRenderer.color(Grade.UNKNOWN))
        assertEquals(Color(0x8800EB), TopoRenderer.color(null))
    }

    @Test
    fun `test grade labels match the app`() {
        assertEquals("5º", TopoRenderer.label(Grade.G5))
        assertEquals("6b+", TopoRenderer.label(Grade.G6B_PLUS))
        assertEquals("7c", TopoRenderer.label(Grade.G7C))
        assertEquals("A3+", TopoRenderer.label(Grade.A3_PLUS))
        assertEquals("Ae", TopoRenderer.label(Grade.A_EQUIPPED))
        assertEquals("¿?", TopoRenderer.label(Grade.UNKNOWN))
    }

    @Test
    fun `test rendering draws routes, bolts and copyright`() {
        val background = grayBackground()
        val rendered = TopoRenderer.render(
            TestTopo.sampleTopo(pathId = 5),
            background,
            mapOf(5 to TopoRenderer.RouteInfo(sketchId = 1U, grade = Grade.G6A, aidGrade = null))
        )
        assertEquals(background.width, rendered.width)
        assertEquals(background.height, rendered.height)

        // A point on the line, away from the grade badge (middle), the bolt and the ends
        assertEquals(Color(0x0055D4), rendered.colorAt(0.2, 0.35))
        // The grade badge is drawn at the middle of the line, with a white background
        val badge = rendered.colorAt(0.2 + 0.012, 0.5)
        assertTrue(badge.red > 240 && badge.green > 240 && badge.blue > 240, "Badge should be white, was $badge")
        // The background is untouched away from the drawing
        assertEquals(Color(128, 128, 128), rendered.colorAt(0.8, 0.3))
        // The copyright is drawn in the bottom right corner, in white
        val corner = (900 until 1000).flatMap { x -> (960 until 1000).map { y -> Color(rendered.getRGB(x, y)) } }
        assertTrue(corner.any { it.red > 240 && it.green > 240 && it.blue > 240 }, "No copyright text found")
    }

    @Test
    fun `test bundled fonts are used`() {
        // Static fonts include their weight in the family name, like "Source Sans 3 SemiBold"
        assertTrue(TopoFonts.number.family.startsWith("Source Sans 3"), TopoFonts.number.family)
        assertTrue(TopoFonts.grade.family.startsWith("Source Sans 3"), TopoFonts.grade.family)
        assertTrue(TopoFonts.label.family.startsWith("Source Sans 3"), TopoFonts.label.family)
        assertTrue(TopoFonts.copyright.family.startsWith("Signika"), TopoFonts.copyright.family)
        assertTrue(TopoFonts.other.family.startsWith("Work Sans"), TopoFonts.other.family)
    }

    @Test
    fun `test routes without data are skipped`() {
        val background = grayBackground()
        val rendered = TopoRenderer.render(TestTopo.sampleTopo(pathId = 5), background, emptyMap())
        assertEquals(Color(128, 128, 128), rendered.colorAt(0.2, 0.35))
    }
}
