package database.serialization

import ServerDatabase
import database.entity.BlogEntry
import database.serialization.external.InstantSerializer
import java.time.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

object BlogEntrySerializer : KSerializer<BlogEntry> {
    private const val IDX_ID = 0
    private const val IDX_TIMESTAMP = 1
    private const val IDX_SUMMARY = 2
    private const val IDX_CONTENT = 3

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Blocking") {
        element<Int>("id")
        element("timestamp", InstantSerializer.descriptor)
        element<String>("summary")
        element<String>("content")
    }

    override fun serialize(encoder: Encoder, value: BlogEntry) = ServerDatabase.instance.querySync {
        encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, IDX_ID, value.id.value)
            encodeSerializableElement(descriptor, IDX_TIMESTAMP, InstantSerializer, value.timestamp)
            encodeStringElement(descriptor, IDX_SUMMARY, value.summary)
            encodeStringElement(descriptor, IDX_CONTENT, value.content)
        }
    }

    override fun deserialize(decoder: Decoder): BlogEntry {
        var id: Int? = null
        var timestamp: Instant? = null
        var summary: String? = null
        var content: String? = null

        decoder.decodeStructure(descriptor) {
            loop@ while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    IDX_ID -> id = decodeIntElement(descriptor, index)
                    IDX_TIMESTAMP -> timestamp = decodeSerializableElement(descriptor, index, InstantSerializer)
                    IDX_SUMMARY -> summary = decodeStringElement(descriptor, index)
                    IDX_CONTENT -> content = decodeStringElement(descriptor, index)
                    CompositeDecoder.DECODE_DONE -> break@loop
                    else -> error("Unexpected index: $index")
                }
            }
        }

        id ?: error("BlogEntry id not found")

        return ServerDatabase.instance.querySync {
            fun BlogEntry.modify(): BlogEntry {
                this.timestamp = timestamp!!
                this.summary = summary!!
                this.content = content!!
                return this
            }

            BlogEntry.findById(id) ?: BlogEntry.new(id) { modify() }
        }
    }
}
