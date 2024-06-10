package database.serialization

import ServerDatabase
import data.DataPoint
import data.LatLng
import database.entity.Area
import database.entity.Zone
import database.table.Areas
import database.table.Zones
import java.net.URL
import java.time.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import org.jetbrains.exposed.dao.id.EntityID
import storage.Storage

@OptIn(ExperimentalSerializationApi::class)
object ZoneSerializer : KSerializer<Zone> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Zone") {
        element<Int>("id")
        element<Long>("timestamp")
        element<String>("display_name")
        element<String>("image")
        element<String>("kmz")
        element<String>("web_url")
        element<String>("point")
        element<String>("points")
        element<Int>("area_id")
    }

    override fun serialize(encoder: Encoder, value: Zone) = ServerDatabase.instance.querySync {
        var idx = 0
        encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, idx++, value.id.value)
            encodeLongElement(descriptor, idx++, value.timestamp.toEpochMilli())
            encodeStringElement(descriptor, idx++, value.displayName)
            encodeStringElement(descriptor, idx++, value.image.toRelativeString(Storage.ImagesDir))
            encodeStringElement(descriptor, idx++, value.kmz.toRelativeString(Storage.TracksDir))
            encodeStringElement(descriptor, idx++, value.webUrl.toString())
            encodeNullableSerializableElement(descriptor, idx++, LatLng.serializer(), value.point)
            encodeSerializableElement(descriptor, idx++, ListSerializer(DataPoint.serializer()), value.points.toList())
            encodeIntElement(descriptor, idx++, value.area.id.value)
        }
    }

    @Suppress("MagicNumber")
    override fun deserialize(decoder: Decoder): Zone {
        var id = 0
        var timestamp = 0L
        var displayName = ""
        var image = ""
        var kmz = ""
        var webUrl = ""
        var point: LatLng? = null
        var points: List<DataPoint> = emptyList()
        var areaId = 0

        decoder.decodeStructure(descriptor) {
            loop@ while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> id = decodeIntElement(descriptor, index)
                    1 -> timestamp = decodeLongElement(descriptor, index)
                    2 -> displayName = decodeStringElement(descriptor, index)
                    3 -> image = decodeStringElement(descriptor, index)
                    4 -> kmz = decodeStringElement(descriptor, index)
                    5 -> webUrl = decodeStringElement(descriptor, index)
                    6 -> point = decodeNullableSerializableElement(descriptor, index, LatLng.serializer())
                    7 -> points = decodeSerializableElement(descriptor, index, ListSerializer(DataPoint.serializer()))
                    8 -> areaId = decodeIntElement(descriptor, index)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }
        }

        return Zone(EntityID(id, Zones)).apply {
            this.timestamp = Instant.ofEpochMilli(timestamp)
            this.displayName = displayName
            this.image = Storage.ImagesDir.resolve(image)
            this.kmz = Storage.TracksDir.resolve(kmz)
            this.webUrl = URL(webUrl)
            this.point = point
            this.points.addAll(points)
            this.area = Area(EntityID(areaId, Areas))
        }
    }
}
