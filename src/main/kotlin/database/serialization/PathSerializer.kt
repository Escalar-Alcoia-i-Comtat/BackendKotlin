package database.serialization

import ServerDatabase
import data.Builder
import data.Ending
import data.Grade
import data.PitchInfo
import database.entity.Path
import database.entity.Sector
import database.table.Paths
import database.table.Sectors
import java.io.File
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
import org.jetbrains.exposed.dao.id.EntityID
import storage.Storage

@OptIn(ExperimentalSerializationApi::class)
object PathSerializer : KSerializer<Path> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Path") {
        element<Int>("id")
        element<Long>("timestamp")

        element<String>("display_name")
        element<UInt>("sketch_id")

        element<UInt?>("height")
        element<Grade?>("grade")
        element<Ending?>("ending")

        element<List<PitchInfo>?>("pitches")

        element<UInt?>("string_count")

        element<UInt?>("parabolt_count")
        element<UInt?>("buril_count")
        element<UInt?>("piton_count")
        element<UInt?>("spit_count")
        element<UInt?>("tensor_count")

        element<Boolean>("cracker_required")
        element<Boolean>("friend_required")
        element<Boolean>("lanyard_required")
        element<Boolean>("nail_required")
        element<Boolean>("piton_required")
        element<Boolean>("stapes_required")

        element<Boolean>("show_description")
        element<String?>("description")

        element<Builder?>("builder")
        element<List<Builder>?>("re_builder")

        element<List<String>?>("images")

        element<Int>("sector_id")
    }

    @Suppress("MaxLineLength")
    override fun serialize(encoder: Encoder, value: Path) = ServerDatabase.instance.querySync {
        var idx = 0
        encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, idx++, value.id.value)
            encodeLongElement(descriptor, idx++, value.timestamp.toEpochMilli())

            encodeStringElement(descriptor, idx++, value.displayName)
            encodeSerializableElement(descriptor, idx++, UInt.serializer(), value.sketchId)

            encodeNullableSerializableElement(descriptor, idx++, UInt.serializer(), value.height)
            encodeNullableSerializableElement(descriptor, idx++, Grade.serializer(), value.grade)
            encodeNullableSerializableElement(descriptor, idx++, Ending.serializer(), value.ending)

            encodeNullableSerializableElement(descriptor, idx++, ListSerializer(PitchInfo.serializer()), value.pitches)

            encodeNullableSerializableElement(descriptor, idx++, UInt.serializer(), value.stringCount)

            encodeNullableSerializableElement(descriptor, idx++, UInt.serializer(), value.paraboltCount)
            encodeNullableSerializableElement(descriptor, idx++, UInt.serializer(), value.burilCount)
            encodeNullableSerializableElement(descriptor, idx++, UInt.serializer(), value.pitonCount)
            encodeNullableSerializableElement(descriptor, idx++, UInt.serializer(), value.spitCount)
            encodeNullableSerializableElement(descriptor, idx++, UInt.serializer(), value.tensorCount)

            encodeBooleanElement(descriptor, idx++, value.crackerRequired)
            encodeBooleanElement(descriptor, idx++, value.friendRequired)
            encodeBooleanElement(descriptor, idx++, value.lanyardRequired)
            encodeBooleanElement(descriptor, idx++, value.nailRequired)
            encodeBooleanElement(descriptor, idx++, value.pitonRequired)
            encodeBooleanElement(descriptor, idx++, value.stapesRequired)

            encodeBooleanElement(descriptor, idx++, value.showDescription)
            encodeNullableSerializableElement(descriptor, idx++, String.serializer(), value.description)

            encodeNullableSerializableElement(descriptor, idx++, Builder.serializer(), value.builder)
            encodeNullableSerializableElement(descriptor, idx++, ListSerializer(Builder.serializer()), value.reBuilder)

            encodeNullableSerializableElement(descriptor, idx++, ListSerializer(String.serializer()), value.images?.map { it.toRelativeString(Storage.ImagesDir) })

            encodeIntElement(descriptor, idx++, value.sector.id.value)
        }
    }

    @Suppress("MagicNumber", "MaxLineLength", "CyclomaticComplexMethod", "LongMethod")
    override fun deserialize(decoder: Decoder): Path {
        var id = 0
        var timestamp = 0L
        var displayName = ""
        var sketchId = 0u
        var height: UInt? = null
        var grade: Grade? = null
        var ending: Ending? = null
        var pitches: List<PitchInfo>? = null
        var stringCount: UInt? = null
        var paraboltCount: UInt? = null
        var burilCount: UInt? = null
        var pitonCount: UInt? = null
        var spitCount: UInt? = null
        var tensorCount: UInt? = null
        var crackerRequired = false
        var friendRequired = false
        var lanyardRequired = false
        var nailRequired = false
        var pitonRequired = false
        var stapesRequired = false
        var showDescription = false
        var description: String? = null
        var builder: Builder? = null
        var reBuilder: List<Builder>? = null
        var images: List<File>? = null
        var sectorId = 0

        decoder.decodeStructure(descriptor) {
            loop@ while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> id = decodeIntElement(descriptor, index)
                    1 -> timestamp = decodeLongElement(descriptor, index)
                    2 -> displayName = decodeStringElement(descriptor, index)
                    3 -> sketchId = decodeSerializableElement(descriptor, index, UInt.serializer())
                    4 -> height = decodeNullableSerializableElement(descriptor, index, UInt.serializer())
                    5 -> grade = decodeNullableSerializableElement(descriptor, index, Grade.serializer())
                    6 -> ending = decodeNullableSerializableElement(descriptor, index, Ending.serializer())
                    7 -> pitches = decodeNullableSerializableElement(descriptor, index, ListSerializer(PitchInfo.serializer()))
                    8 -> stringCount = decodeNullableSerializableElement(descriptor, index, UInt.serializer())
                    9 -> paraboltCount = decodeNullableSerializableElement(descriptor, index, UInt.serializer())
                    10 -> burilCount = decodeNullableSerializableElement(descriptor, index, UInt.serializer())
                    11 -> pitonCount = decodeNullableSerializableElement(descriptor, index, UInt.serializer())
                    12 -> spitCount = decodeNullableSerializableElement(descriptor, index, UInt.serializer())
                    13 -> tensorCount = decodeNullableSerializableElement(descriptor, index, UInt.serializer())
                    14 -> crackerRequired = decodeBooleanElement(descriptor, index)
                    15 -> friendRequired = decodeBooleanElement(descriptor, index)
                    16 -> lanyardRequired = decodeBooleanElement(descriptor, index)
                    17 -> nailRequired = decodeBooleanElement(descriptor, index)
                    18 -> pitonRequired = decodeBooleanElement(descriptor, index)
                    19 -> stapesRequired = decodeBooleanElement(descriptor, index)
                    20 -> showDescription = decodeBooleanElement(descriptor, index)
                    21 -> description = decodeNullableSerializableElement(descriptor, index, String.serializer())
                    22 -> builder = decodeNullableSerializableElement(descriptor, index, Builder.serializer())
                    23 -> reBuilder = decodeNullableSerializableElement(descriptor, index, ListSerializer(Builder.serializer()))
                    24 -> images = decodeNullableSerializableElement(descriptor, index, ListSerializer(String.serializer()))?.map { Storage.ImagesDir.resolve(it) }
                    25 -> sectorId = decodeIntElement(descriptor, index)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }
        }

        return Path(EntityID(id, Paths)).apply {
            this.timestamp = Instant.ofEpochMilli(timestamp)
            this.displayName = displayName
            this.sketchId = sketchId
            this.height = height
            this.grade = grade
            this.ending = ending
            this.pitches = pitches
            this.stringCount = stringCount
            this.paraboltCount = paraboltCount
            this.burilCount = burilCount
            this.pitonCount = pitonCount
            this.spitCount = spitCount
            this.tensorCount = tensorCount
            this.crackerRequired = crackerRequired
            this.friendRequired = friendRequired
            this.lanyardRequired = lanyardRequired
            this.nailRequired = nailRequired
            this.pitonRequired = pitonRequired
            this.stapesRequired = stapesRequired
            this.showDescription = showDescription
            this.description = description
            this.builder = builder
            this.reBuilder = reBuilder
            this.images = images
            this.sector = Sector(EntityID(sectorId, Sectors))
        }
    }
}
