package data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestTopo {
    companion object {
        /** One route going up from a start to an anchor, drawn as a straight dashed line. */
        fun sampleTopo(pathId: Int = 1) = Topo(
            imageWidth = 1000,
            imageHeight = 1000,
            nodes = listOf(
                Topo.Node("start", 0.2, 0.9, Topo.NodeType.START),
                Topo.Node("anchor", 0.2, 0.1, Topo.NodeType.ANCHOR, label = "R1")
            ),
            edges = listOf(
                Topo.Edge(
                    id = "e1",
                    from = "start",
                    to = "anchor",
                    curves = listOf(Topo.Cubic(Topo.Point(0.2, 0.7), Topo.Point(0.2, 0.3), Topo.Point(0.2, 0.1))),
                    style = Topo.EdgeStyle.SOLID
                )
            ),
            routes = listOf(Topo.Route(pathId = pathId, edges = listOf("e1"))),
            bolts = listOf(Topo.Bolt(0.2, 0.5, pathId)),
            labels = listOf(Topo.Label(0.5, 0.5, "20 m")),
            copyright = Topo.Copyright("© EscalarAlcoiaiComtat", Topo.Corner.BOTTOM_RIGHT)
        )
    }

    @Test
    fun `test serialization round trip`() {
        val topo = sampleTopo()
        val json = Topo.json.encodeToString(Topo.serializer(), topo)
        assertEquals(topo, Topo.json.decodeFromString(Topo.serializer(), json))
    }

    @Test
    fun `test unknown keys are ignored`() {
        val json = """{"version":1,"imageWidth":10,"imageHeight":10,"somethingNew":true}"""
        assertEquals(Topo(imageWidth = 10, imageHeight = 10), Topo.json.decodeFromString(Topo.serializer(), json))
    }

    @Test
    fun `test valid topo has no problems`() {
        assertTrue(sampleTopo().validate().isEmpty())
    }

    @Test
    fun `test validation finds problems`() {
        val topo = sampleTopo()
        val invalid = topo.copy(
            version = Topo.VERSION + 1,
            edges = topo.edges + Topo.Edge("e2", "start", "missing", emptyList()),
            routes = topo.routes + Topo.Route(2, listOf("unknown")),
            bolts = listOf(Topo.Bolt(2.0, 0.5))
        )
        val problems = invalid.validate()
        assertTrue(problems.any { "version" in it }, problems.toString())
        assertTrue(problems.any { "unknown node missing" in it }, problems.toString())
        assertTrue(problems.any { "no curves" in it }, problems.toString())
        assertTrue(problems.any { "unknown edge" in it }, problems.toString())
        assertTrue(problems.any { "Bolt" in it }, problems.toString())
    }
}
