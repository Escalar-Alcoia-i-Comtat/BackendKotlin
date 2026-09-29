package server.response.query

import org.escalaralcoiaicomtat.common.topo.Topo
import kotlinx.serialization.Serializable
import server.response.ResponseData

/**
 * The topo of a sector, for editing it.
 * @param topoImage UUID of the background the topo is drawn on, downloadable from the download endpoint.
 */
@Serializable
data class TopoResponseData(
    val topo: Topo,
    val topoImage: String
) : ResponseData
