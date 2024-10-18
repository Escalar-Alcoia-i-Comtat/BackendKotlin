package database.serialization

import ServerDatabase
import data.LatLng
import database.entity.Path
import database.entity.Sector
import database.entity.Zone
import java.time.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import storage.Storage

@OptIn(ExperimentalSerializationApi::class)
object SectorSerializer : KSerializer<Sector> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Sector") {
        element<Int>("id")
        element<Long>("timestamp")
        element<String>("display_name")
        element<Boolean>("kids_apt")
        element<String>("sun_time")
        element<Long?>("walking_time")
        element<String>("image")
        element<String?>("gpx")
        element<String>("point")
        element<String>("weight")
        element<Int>("zone_id")
        element<List<Path>?>("paths")
    }

    @Suppress("MaxLineLength")
    override fun serialize(encoder: Encoder, value: Sector) = ServerDatabase.instance.querySync {
        var idx = 0
        encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, idx++, value.id.value)
            encodeLongElement(descriptor, idx++, value.timestamp.toEpochMilli())
            encodeStringElement(descriptor, idx++, value.displayName)
            encodeBooleanElement(descriptor, idx++, value.kidsApt)
            encodeSerializableElement(descriptor, idx++, Sector.SunTime.serializer(), value.sunTime)
            encodeNullableSerializableElement(descriptor, idx++, Long.serializer(), value.walkingTime?.toLong())
            encodeStringElement(
                descriptor,
                idx++,
                value.image.toRelativeString(Storage.ImagesDir).substringBeforeLast('.')
            )
            encodeNullableSerializableElement(
                descriptor,
                idx++,
                String.serializer(),
                value.gpx?.toRelativeString(Storage.TracksDir)?.substringBeforeLast('.')
            )
            encodeNullableSerializableElement(descriptor, idx++, LatLng.serializer(), value.point)
            encodeStringElement(descriptor, idx++, value.weight)
            encodeIntElement(descriptor, idx++, value.zone.id.value)
            encodeNullableSerializableElement(descriptor, idx++, ListSerializer(Path.serializer()), value.paths)
        }
    }

    @Suppress("MagicNumber", "LoopWithTooManyJumpStatements", "CyclomaticComplexMethod")
    override fun deserialize(decoder: Decoder): Sector {
        var id = 0
        var timestamp = 0L
        var displayName = ""
        var kidsApt = false
        var sunTime = Sector.SunTime.Morning
        var walkingTime: UInt? = null
        var image = ""
        var gpx: String? = null
        var point: LatLng? = null
        var weight: String = ""
        var zoneId = 0

        decoder.decodeStructure(descriptor) {
            loop@ while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> id = decodeIntElement(descriptor, index)
                    1 -> timestamp = decodeLongElement(descriptor, index)
                    2 -> displayName = decodeStringElement(descriptor, index)
                    3 -> kidsApt = decodeBooleanElement(descriptor, index)
                    4 -> sunTime = decodeSerializableElement(descriptor, index, Sector.SunTime.serializer())
                    5 -> walkingTime = decodeNullableSerializableElement(descriptor, index, Long.serializer())?.toUInt()
                    6 -> image = decodeStringElement(descriptor, index)
                    7 -> gpx = decodeNullableSerializableElement(descriptor, index, String.serializer())
                    8 -> point = decodeNullableSerializableElement(descriptor, index, LatLng.serializer())
                    9 -> weight = decodeStringElement(descriptor, index)
                    10 -> zoneId = decodeIntElement(descriptor, index)
                    11 -> break // Ignore paths
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }
        }

        return ServerDatabase.instance.querySync {
            fun Sector.modifier(): Sector {
                this.timestamp = Instant.ofEpochMilli(timestamp)
                this.displayName = displayName
                this.kidsApt = kidsApt
                this.sunTime = sunTime
                this.walkingTime = walkingTime
                this.image = Storage.ImagesDir.resolve(image)
                this.gpx = gpx?.let { Storage.TracksDir.resolve(it) }
                this.point = point
                this.weight = weight
                this.zone = Zone[zoneId]
                // Note: "paths" is never initialized here, it's not intended to ever decode the list of paths
                return this
            }

            Sector.findById(id)?.apply { modifier() } ?: Sector.new(id) { modifier() }
        }
    }
}
