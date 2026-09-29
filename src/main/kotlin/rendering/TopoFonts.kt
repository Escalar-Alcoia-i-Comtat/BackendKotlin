package rendering

import Logger
import java.awt.Font

/**
 * Fonts used to draw text on topos. The original Illustrator topos use fonts that can't be distributed, so each one is
 * replaced by a free font with a similar look (all bundled in `resources/fonts`, under the SIL Open Font License):
 *
 * | Illustrator     | Replacement     | Used for                                |
 * |-----------------|-----------------|-----------------------------------------|
 * | Candara         | Source Sans 3   | Route numbers, grades and labels        |
 * | Eras Medium ITC | Signika         | Copyright                               |
 * | Segoe UI        | Work Sans       | Other texts                             |
 *
 * Source Sans 3 is the current name of Source Sans Pro. If a font can't be loaded, a generic sans-serif font is used.
 */
object TopoFonts {
    /** Illustrator font families, and the family that replaces each one. */
    val replacements = mapOf(
        "Candara" to "Source Sans 3",
        "Eras Medium ITC" to "Signika",
        "Segoe UI" to "Work Sans"
    )

    /** Route numbers (Candara). */
    val number: Font by lazy { load("SourceSans3-SemiBold", Font.BOLD) }

    /** Grades (Candara Bold). */
    val grade: Font by lazy { load("SourceSans3-Bold", Font.BOLD) }

    /** Belay labels and free labels (Candara). */
    val label: Font by lazy { load("SourceSans3-SemiBold", Font.BOLD) }

    /** Copyright (Eras Medium ITC). */
    val copyright: Font by lazy { load("Signika-Medium", Font.PLAIN) }

    /** Other texts (Segoe UI). */
    val other: Font by lazy { load("WorkSans-Regular", Font.PLAIN) }

    /** The font of the given replacement [family] ("Source Sans 3", "Signika" or "Work Sans"), for free labels. */
    fun forFamily(family: String?): Font = when {
        family == null -> label
        family.startsWith("Signika", ignoreCase = true) -> copyright
        family.startsWith("Work Sans", ignoreCase = true) -> other
        else -> label
    }

    private fun load(name: String, fallbackStyle: Int): Font = try {
        val stream = TopoFonts::class.java.getResourceAsStream("/fonts/$name.ttf")
            ?: error("Font $name not found in resources")
        stream.use { Font.createFont(Font.TRUETYPE_FONT, it) }
    } catch (e: Exception) {
        Logger.error("Could not load font $name, using a generic one", e)
        Font(Font.SANS_SERIF, fallbackStyle, 1)
    }
}
