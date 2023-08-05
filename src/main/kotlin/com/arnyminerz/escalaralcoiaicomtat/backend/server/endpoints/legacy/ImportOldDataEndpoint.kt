package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.legacy

import HTTP_PORT
import com.arnyminerz.escalaralcoiaicomtat.backend.Logger
import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.data.Builder
import com.arnyminerz.escalaralcoiaicomtat.backend.data.DataPoint
import com.arnyminerz.escalaralcoiaicomtat.backend.data.Ending
import com.arnyminerz.escalaralcoiaicomtat.backend.data.EndingInclination
import com.arnyminerz.escalaralcoiaicomtat.backend.data.EndingInfo
import com.arnyminerz.escalaralcoiaicomtat.backend.data.GradeValue
import com.arnyminerz.escalaralcoiaicomtat.backend.data.LatLng
import com.arnyminerz.escalaralcoiaicomtat.backend.data.PitchInfo
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Area
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Blocking
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Path
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Sector
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Zone
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.EndpointBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.system.EnvironmentVariables
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.getStringOrNull
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.json
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.removeAccents
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.toJson
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.forms.FormBuilder
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentLength
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.util.getValue
import io.ktor.util.pipeline.PipelineContext
import io.ktor.utils.io.ByteReadChannel
import java.io.File
import java.util.Locale
import me.tongfei.progressbar.ProgressBar
import org.json.JSONArray
import org.json.JSONObject

object ImportOldDataEndpoint : EndpointBase() {
    private const val BUILDER_PIECES_COUNT = 4
    private const val BUILDER_INDEX_LAT = 0
    private const val BUILDER_INDEX_LON = 1
    private const val BUILDER_INDEX_LAB = 2
    private const val BUILDER_INDEX_ICO = 3

    private const val DOWNLOAD_BYTE_BUFFER_SIZE = 1024 * 100

    val client: HttpClient by lazy { HttpClient(CIO) }

    private val tempFile: File by lazy { File(System.getProperty("user.home")) }
    private val tempDir: File by lazy { File(tempFile, "eaic-tmp") }

    private var hostname: String = ""

    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val hostname: String? by call.parameters

        if (hostname == null) {
            respondFailure(Errors.MissingData)
            return
        }
        this@ImportOldDataEndpoint.hostname = hostname!!

        ServerDatabase.instance.query {
            val areasEmpty = Area.all().empty()
            val zonesEmpty = Zone.all().empty()
            val sectorsEmpty = Sector.all().empty()
            val pathsEmpty = Path.all().empty()
            val blockingEmpty = Blocking.all().empty()

            check(areasEmpty && zonesEmpty && sectorsEmpty && pathsEmpty && blockingEmpty) {
                "Database must be empty for running import."
            }
        }

        if (tempDir.exists()) {
            if (tempDir.deleteRecursively()) {
                Logger.debug("Deleted Temp dir ($tempDir)")
            } else {
                Logger.error("Could not delete old temp dir! $tempDir")
            }
        }

        if (!tempDir.exists()) {
            if (tempDir.mkdirs()) {
                Logger.debug("Created Temp dir ($tempDir)")
            } else {
                Logger.error("Could not create temp dir! $tempDir")
            }
        }

        Logger.debug("Starting data import from https://$hostname/...")

        Logger.debug("Importing areas...")
        val areaIdPairs = fetchAreas()
        Logger.debug("Importing zones...")
        val zoneIdPairs = fetchZones(areaIdPairs)
        Logger.debug("Importing sectors...")
        val sectorIdPairs = fetchSectors(zoneIdPairs)
        Logger.debug("Importing paths...")
        fetchPaths(sectorIdPairs)

        Logger.debug("Import complete!")

        tempDir.deleteRecursively()

        respondSuccess()
    }

    private suspend fun downloadFile(path: String): File =
        client.get("https://$hostname/v1/files/download?path=$path").let {
            val fileName = path.substringAfterLast('/')
            val file = File(tempDir, fileName.removeAccents().replace(" ", "_"))

            Logger.debug("  Writing $path into $file")
            var offset = 0
            val byteBufferSize = DOWNLOAD_BYTE_BUFFER_SIZE
            val channel = it.body<ByteReadChannel>()
            val contentLength = it.contentLength() ?: 0L
            val data = ByteArray(contentLength.toInt())
            do {
                val currentRead = channel.readAvailable(data, offset, byteBufferSize)
                offset += currentRead
            } while (currentRead >= 0)

            file.writeBytes(data)

            file
        }

    private suspend inline fun fetch(
        name: String,
        forEach: MutableMap<String, Int>.(objectId: String, data: JSONObject, pb: ProgressBar) -> Unit
    ) = client.get("https://$hostname/v1/list/$name").let {
        val pairs = hashMapOf<String, Int>()

        val result = it.bodyAsText().json.getJSONObject("result")

        Logger.debug("Got ${result.length()} $name.")

        val pb = ProgressBar("Fetching $name", result.length().toLong())
        for (key in result.keys()) {
            val data = result.getJSONObject(key)
            forEach(pairs, key, data, pb)
            pb.stepBy(1)
        }
        pb.close()

        pairs
    }

    private inline fun <reified T : Any> processList(
        json: JSONObject,
        name: String,
        converter: (String) -> T,
        setGeneral: (value: T) -> Unit = {}
    ): Array<T?>? = json.getStringOrNull(name)
        ?.takeIf { it.isNotBlank() && !it.equals("NULL", true) }
        ?.replace("\r", "")
        ?.split("\n")
        ?.let { items ->
            Logger.debug("  Processing ${items.size} items of $name.")

            val list = Array<T?>(size = items.size) { null }

            var startsAt0 = false
            for (item in items) {
                val pieces = item.split(">")
                val index = pieces[0].takeIf { pieces.size >= 2 }?.toIntOrNull()
                val value = converter(pieces.getOrNull(1) ?: pieces[0])

                if (index == null) {
                    setGeneral(value)
                } else {
                    if (index == 0) startsAt0 = true
                    val offset = if (startsAt0) index else index - 1
                    if (offset >= items.size) continue

                    list[offset] = value
                }
            }

            list
        }

    private suspend fun create(endpoint: String, builder: FormBuilder.() -> Unit): Int {
        val response = client.submitFormWithBinaryData(
            "http://127.0.0.1:$HTTP_PORT/$endpoint",
            formData = formData(builder)
        ) {
            header(HttpHeaders.Authorization, "Bearer ${EnvironmentVariables.Authentication.AuthToken.value}")
        }
        val body = response.bodyAsText()
        // Get body as JSON
        val json = body.json

        check(json.has("data")) { "Body didn't return any data. Body: $body" }

        // Get response data
        val data = json.getJSONObject("data")

        // Extract the element's ID
        return data.getInt("${endpoint}_id")
    }

    private suspend fun fetchAreas() = fetch("Areas") { objectId, area, pb ->
        // Download image
        val image = area.getString("image")
        Logger.debug("Downloading $image for area $objectId...")
        val imageFile = downloadFile(image)

        // Download KMZ
        val kmz = area.getString("kmz")
        Logger.debug("Downloading $kmz for area $objectId...")
        val kmzFile = downloadFile(kmz)

        Logger.debug("Creating area $objectId...")
        pb.setExtraMessage(objectId)

        val areaId = create("area") {
            append("displayName", area.getString("displayName"))
            append("webUrl", area.getString("webURL"))
            append("image", imageFile.readBytes(), Headers.build {
                append(HttpHeaders.ContentType, "image/jpeg")
                append(HttpHeaders.ContentDisposition, "filename=area.jpg")
            })
            append("kmz", kmzFile.readBytes(), Headers.build {
                append(HttpHeaders.ContentType, "application/vnd")
                append(HttpHeaders.ContentDisposition, "filename=area.kmz")
            })
        }

        // Append the id of the new area corresponding to the old objectId
        put(objectId, areaId)
    }

    private suspend fun fetchZones(areaIdPairs: Map<String, Int>) = fetch("Zones") { objectId, zone, pb ->
        // Download image
        val image = zone.getString("image")
        Logger.debug("Downloading $image for zone $objectId...")
        val imageFile = downloadFile(image)

        // Download KMZ
        val kmz = zone.getString("kmz")
        Logger.debug("Downloading $kmz for zone $objectId...")
        val kmzFile = downloadFile(kmz)

        val points = zone.getStringOrNull("points")?.let { pointsStr ->
            val lines = pointsStr
                .split("\n")
                .map { it.split(";") }
            val builder = mutableListOf<DataPoint>()
            for (pieces in lines) {
                if (pieces.size < BUILDER_PIECES_COUNT) continue
                val lat = pieces[BUILDER_INDEX_LAT].toDouble()
                val lon = pieces[BUILDER_INDEX_LON].toDouble()
                val label = pieces[BUILDER_INDEX_LAB]
                val icon = pieces[BUILDER_INDEX_ICO]

                builder.add(
                    DataPoint(LatLng(lat, lon), label, icon)
                )
            }
            builder
        }

        val areaId = areaIdPairs[zone.getString("area")]
        if (areaId == null) {
            Logger.warn("Could not find an area with objectId associated to ${zone.getString("area")}.")
            return@fetch
        }

        Logger.debug("Creating zone $objectId (area=$areaId)...")
        pb.setExtraMessage(objectId)

        val zoneId = create("zone") {
            append("displayName", zone.getString("displayName"))
            append("webUrl", zone.getString("webURL"))
            append(
                "point",
                LatLng(zone.getDouble("latitude"), zone.getDouble("longitude")).toJson().toString()
            )
            points?.let { append("points", it.toJson().toString()) }

            append("image", imageFile.readBytes(), Headers.build {
                append(HttpHeaders.ContentType, "image/jpeg")
                append(HttpHeaders.ContentDisposition, "filename=zone.jpg")
            })
            append("kmz", kmzFile.readBytes(), Headers.build {
                append(HttpHeaders.ContentType, "application/vnd")
                append(HttpHeaders.ContentDisposition, "filename=zone.kmz")
            })

            append("area", areaId)
        }

        // Append the id of the new zone corresponding to the old objectId
        put(objectId, zoneId)
    }

    private suspend fun fetchSectors(zoneIdPairs: Map<String, Int>) = fetch("Sectors") { objectId, sector, pb ->
        // Download image
        val image = sector.getString("image")
        Logger.debug("Downloading $image for sector $objectId...")
        val imageFile = downloadFile(image)

        val zoneId = zoneIdPairs[sector.getString("zone")]
        if (zoneId == null) {
            Logger.warn("Could not find a zone with objectId associated to ${sector.getString("zone")}.")
            return@fetch
        }

        Logger.debug("Creating sector $objectId (zone=$zoneId)...")
        pb.setExtraMessage(objectId)

        val sectorId = create("sector") {
            append("displayName", sector.getString("displayName"))
            append("kidsApt", sector.getBoolean("kidsApt"))
            append(
                "point",
                LatLng(sector.getDouble("latitude"), sector.getDouble("longitude")).toJson().toString()
            )
            append(
                "sunTime",
                sector.getString("sunTime")
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            )
            append("weight", sector.getString("weight"))
            append("walkingTime", sector.getInt("walkingTime"))

            append("image", imageFile.readBytes(), Headers.build {
                append(HttpHeaders.ContentType, "image/jpeg")
                append(HttpHeaders.ContentDisposition, "filename=sector.jpg")
            })

            append("zone", zoneId)
        }

        // Append the id of the new area corresponding to the old objectId
        put(objectId, sectorId)
    }

    @Suppress("LongMethod")
    private suspend fun fetchPaths(sectorIdPairs: Map<String, Int>) = fetch("Paths") { objectId, path, pb ->
        val sectorId = sectorIdPairs[path.getString("sector")]
        if (sectorId == null) {
            Logger.warn("Could not find a sector with objectId associated to ${path.getString("sector")}.")
            return@fetch
        }

        Logger.debug("Creating path $objectId (sector=$sectorId)...")
        pb.setExtraMessage(objectId)

        // >¿?\r\n1>¿?\r\n2>¿?\r\n3>¿?\r\n4>¿?\r\n5>¿?

        var generalGrade: String? = null
        val gradeList = processList(path, "grade", { it }) { generalGrade = it }

        // >98\r\n1>28\r\n2>35\r\n3>35

        var generalHeight: UInt? = null
        val heightList = processList(path, "height", { it.toUInt() }) { generalHeight = it }

        // >rappel\r\n1>plate_ring\r\n2>plate_ring\r\n3>plate_ring

        var generalEnding: String? = null
        val endingList = processList(path, "ending", { it.uppercase() }) { generalEnding = it }

        // 1>rappel diagonal\r\n2>rappel diagonal\r\n3>rappel horizontal

        val pitchesInfoList = processList(
            path,
            "pitch_info",
            { raw ->
                val pieces = raw.uppercase().split(" ")
                val info = EndingInfo.entries.find { it.name == pieces[0] }
                val inclination = EndingInclination.entries.find { it.name == pieces[1] }
                info to inclination
            }
        )

        Logger.debug("  Building pitches...")
        val pitches = Array(
            size = listOf(gradeList, heightList, endingList, pitchesInfoList).maxOf { it?.size ?: 0 }
        ) { index ->
            val grade = gradeList?.getOrNull(index)
            val height = heightList?.getOrNull(index)
            val ending = endingList?.getOrNull(index)
            val pitchesInfo = pitchesInfoList?.getOrNull(index)

            PitchInfo(
                pitch = index.toUInt(),
                gradeValue = grade?.let { GradeValue.fromString(it) },
                height = height,
                ending = ending?.let { Ending.valueOf(it) },
                info = pitchesInfo?.first,
                inclination = pitchesInfo?.second
            )
        }

        create("path") {
            append("displayName", path.getString("displayName"))
            append("sketchId", path.getInt("sketchId"))

            generalHeight?.let { append("height", it.toLong()) }
            generalGrade?.let { append("grade", it) }
            generalEnding?.let { append("ending", it) }

            append("pitches", pitches.toList().toJson().toString())

            append("stringCount", path.getInt("stringCount"))
            append("paraboltCount", path.getInt("paraboltCount"))
            append("burilCount", path.getInt("burilCount"))
            append("pitonCount", path.getInt("pitonCount"))
            append("spitCount", path.getInt("spitCount"))
            append("tensorCount", path.getInt("tensorCount"))

            append("crackerRequired", path.getBoolean("crackerRequired"))
            append("friendRequired", path.getBoolean("friendRequired"))
            append("lanyardRequired", path.getBoolean("lanyardRequired"))
            append("nailRequired", path.getBoolean("nailRequired"))
            append("pitonRequired", path.getBoolean("pitonRequired"))
            append("stapesRequired", path.getBoolean("stripsRequired"))

            path.getStringOrNull("builtBy")
                ?.takeIf { it.isNotBlank() && !it.equals("NULL", true) }
                ?.let {
                    val pieces = it.split(";")
                    val builtBy = Builder(pieces.getOrNull(0), pieces.getOrNull(1))
                    append("builder", builtBy.toJson().toString())
                }

            path.getStringOrNull("rebuilders")
                ?.takeIf { it.isNotBlank() && !it.equals("NULL", true) }
                ?.let {
                    val lines = it.replace("\r", "").split("\n")
                    val array = JSONArray()
                    for (line in lines) {
                        val pieces = line.split(";")
                        val reBuilder = Builder(pieces.getOrNull(0), pieces.getOrNull(1))
                        array.put(reBuilder.toJson())
                    }
                    append("reBuilder", array.toString())
                }

            append("sector", sectorId)
        }
    }
}
