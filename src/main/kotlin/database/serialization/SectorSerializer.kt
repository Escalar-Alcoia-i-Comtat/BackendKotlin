package database.serialization

import ServerDatabase
import data.ExternalTrack
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
    private const val IDX_ID = 0
    private const val IDX_TIMESTAMP = 1
    private const val IDX_DISPLAY_NAME = 2
    private const val IDX_KIDS_APT = 3
    private const val IDX_SUN_TIME = 4
    private const val IDX_WALKING_TIME = 5
    private const val IDX_IMAGE = 6
    private const val IDX_GPX = 7
    private const val IDX_TRACKS = 8
    private const val IDX_POINT = 9
    private const val IDX_WEIGHT = 10
    private const val IDX_ZONE_ID = 11
    private const val IDX_PATHS = 12

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Sector") {
        element<Int>("id")
        element<Long>("timestamp")
        element<String>("display_name")
        element<Boolean>("kids_apt")
        element<String>("sun_time")
        element<Long?>("walking_time")
        element<String>("image")
        element<String?>("gpx")
        element<List<ExternalTrack>>("tracks")
        element<String>("point")
        element<String>("weight")
        element<Int>("zone_id")
        element<List<Path>?>("paths")
    }

    @Suppress("MaxLineLength")
    override fun serialize(encoder: Encoder, value: Sector) = ServerDatabase.instance.querySync {
        encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, IDX_ID, value.id.value)
            encodeLongElement(descriptor, IDX_TIMESTAMP, value.timestamp.toEpochMilli())
            encodeStringElement(descriptor, IDX_DISPLAY_NAME, value.displayName)
            encodeBooleanElement(descriptor, IDX_KIDS_APT, value.kidsApt)
            encodeSerializableElement(descriptor, IDX_SUN_TIME, Sector.SunTime.serializer(), value.sunTime)
            encodeNullableSerializableElement(descriptor, IDX_WALKING_TIME, Long.serializer(), value.walkingTime?.toLong())
            encodeStringElement(
                descriptor,
                IDX_IMAGE,
                value.image.toRelativeString(Storage.ImagesDir).substringBeforeLast('.')
            )
            encodeNullableSerializableElement(
                descriptor,
                IDX_GPX,
                String.serializer(),
                value.gpx?.toRelativeString(Storage.TracksDir)?.substringBeforeLast('.')
            )
            encodeNullableSerializableElement(descriptor, IDX_TRACKS, ListSerializer(ExternalTrack.serializer()), value.tracks)
            encodeNullableSerializableElement(descriptor, IDX_POINT, LatLng.serializer(), value.point)
            encodeStringElement(descriptor, IDX_WEIGHT, value.weight)
            encodeIntElement(descriptor, IDX_ZONE_ID, value.zone.id.value)
            encodeNullableSerializableElement(descriptor, IDX_PATHS, ListSerializer(Path.serializer()), value.paths)
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
        var tracks: List<ExternalTrack>? = null
        var point: LatLng? = null
        var weight = ""
        var zoneId = 0

        decoder.decodeStructure(descriptor) {
            loop@ while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    IDX_ID -> id = decodeIntElement(descriptor, index)
                    IDX_TIMESTAMP -> timestamp = decodeLongElement(descriptor, index)
                    IDX_DISPLAY_NAME -> displayName = decodeStringElement(descriptor, index)
                    IDX_KIDS_APT -> kidsApt = decodeBooleanElement(descriptor, index)
                    IDX_SUN_TIME -> sunTime = decodeSerializableElement(descriptor, index, Sector.SunTime.serializer())
                    IDX_WALKING_TIME -> walkingTime = decodeNullableSerializableElement(descriptor, index, Long.serializer())?.toUInt()
                    IDX_IMAGE -> image = decodeStringElement(descriptor, index)
                    IDX_GPX -> gpx = decodeNullableSerializableElement(descriptor, index, String.serializer())
                    IDX_TRACKS -> tracks = decodeNullableSerializableElement(descriptor, index, ListSerializer(ExternalTrack.serializer()))
                    IDX_POINT -> point = decodeNullableSerializableElement(descriptor, index, LatLng.serializer())
                    IDX_WEIGHT -> weight = decodeStringElement(descriptor, index)
                    IDX_ZONE_ID -> zoneId = decodeIntElement(descriptor, index)
                    IDX_PATHS -> break // Ignore paths
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
                this.tracks = tracks
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
