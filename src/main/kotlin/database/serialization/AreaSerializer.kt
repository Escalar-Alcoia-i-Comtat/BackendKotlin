package database.serialization

import database.entity.Area
import database.entity.Zone
import database.table.Areas
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
object AreaSerializer : KSerializer<Area> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Area") {
        element<Int>("id")
        element<Long>("timestamp")
        element<String>("display_name")
        element<String>("image")
        element<String>("web_url")
        element<List<Zone>?>("zones")
    }

    override fun serialize(encoder: Encoder, value: Area) {
        var idx = 0
        encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, idx++, value.id.value)
            encodeLongElement(descriptor, idx++, value.timestamp.toEpochMilli())
            encodeStringElement(descriptor, idx++, value.displayName)
            encodeStringElement(descriptor, idx++, value.image.toRelativeString(Storage.ImagesDir))
            encodeStringElement(descriptor, idx++, value.webUrl.toString())
            encodeNullableSerializableElement(descriptor, idx++, ListSerializer(Zone.serializer()), value.zones)
        }
    }

    @Suppress("MagicNumber")
    override fun deserialize(decoder: Decoder): Area {
        var id = 0
        var timestamp = 0L
        var displayName = ""
        var image = ""
        var webUrl = ""

        decoder.decodeStructure(descriptor) {
            loop@ while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> id = decodeIntElement(descriptor, index)
                    1 -> timestamp = decodeLongElement(descriptor, index)
                    2 -> displayName = decodeStringElement(descriptor, index)
                    3 -> image = decodeStringElement(descriptor, index)
                    4 -> webUrl = decodeStringElement(descriptor, index)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }
        }

        return Area(EntityID(id, Areas)).apply {
            this.timestamp = Instant.ofEpochMilli(timestamp)
            this.displayName = displayName
            this.image = Storage.ImagesDir.resolve(image)
            this.webUrl = URL(webUrl)
        }
    }
}
