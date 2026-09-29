package data

import org.escalaralcoiaicomtat.common.topo.Topo

/** Topos for tests. The topo format itself is tested in the common module. */
object TopoSamples {
    /** One route going up from a start to an anchor, drawn as a straight line. */
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
        copyright = Topo.Copyright(Topo.Corner.BOTTOM_RIGHT)
    )
}
