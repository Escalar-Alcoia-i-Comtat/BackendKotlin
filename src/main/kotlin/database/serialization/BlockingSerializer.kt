package database.serialization

import ServerDatabase
import data.BlockingRecurrenceYearly
import data.BlockingTypes
import database.entity.Blocking
import database.entity.Path
import database.serialization.external.InstantSerializer
import database.serialization.external.LocalDateTimeSerializer
import java.time.Instant
import java.time.LocalDateTime
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

@OptIn(ExperimentalSerializationApi::class)
object BlockingSerializer : KSerializer<Blocking> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Blocking") {
        element<Int>("id")
        element("timestamp", InstantSerializer.descriptor)
        element<BlockingTypes>("type")
        element<BlockingRecurrenceYearly?>("recurrence")
        element("end_date", LocalDateTimeSerializer.descriptor, isOptional = true)
        element<Int>("path_id")
    }

    @Suppress("MaxLineLength")
    override fun serialize(encoder: Encoder, value: Blocking) = ServerDatabase.instance.querySync {
        var idx = 0
        encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, idx++, value.id.value)
            encodeSerializableElement(descriptor, idx++, InstantSerializer, value.timestamp)
            encodeSerializableElement(descriptor, idx++, BlockingTypes.serializer(), value.type)
            encodeNullableSerializableElement(descriptor, idx++, BlockingRecurrenceYearly.serializer(), value.recurrence)
            encodeNullableSerializableElement(descriptor, idx++, LocalDateTimeSerializer, value.endDate)
            encodeIntElement(descriptor, idx++, value.path.id.value)
        }
    }

    @Suppress("MaxLineLength", "MagicNumber")
    override fun deserialize(decoder: Decoder): Blocking {
        var id: Int? = null
        var timestamp: Instant? = null
        var type: BlockingTypes? = null
        var recurrence: BlockingRecurrenceYearly? = null
        var endDate: LocalDateTime? = null
        var pathId: Int? = null
        
        decoder.decodeStructure(descriptor) {
            loop@ while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> id = decodeIntElement(descriptor, index)
                    1 -> timestamp = decodeSerializableElement(descriptor, index, InstantSerializer)
                    2 -> type = decodeSerializableElement(descriptor, index, BlockingTypes.serializer())
                    3 -> recurrence = decodeNullableSerializableElement(descriptor, index, BlockingRecurrenceYearly.serializer())
                    4 -> endDate = decodeNullableSerializableElement(descriptor, index, LocalDateTimeSerializer)
                    5 -> pathId = decodeIntElement(descriptor, index)
                    CompositeDecoder.DECODE_DONE -> break@loop
                    else -> error("Unexpected index: $index")
                }
            }
        }

        id ?: error("Blocking id not found")
        
        return ServerDatabase.instance.querySync {
            fun Blocking.modify(): Blocking {
                this.timestamp = timestamp!!
                this.type = type!!
                this.recurrence = recurrence
                this.endDate = endDate
                this.path = Path.findById(pathId!!) ?: error("Path with id $pathId not found")
                return this
            }

            Blocking.findById(id!!) ?: Blocking.new(id!!) { modify() }
        }
    }
}
