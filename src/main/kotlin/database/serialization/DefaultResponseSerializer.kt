package database.serialization

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
import kotlinx.serialization.json.JsonElement
import server.error.Error
import server.response.FailureResponse
import server.response.Response
import server.response.SuccessResponse

@OptIn(ExperimentalSerializationApi::class)
object DefaultResponseSerializer : KSerializer<Response> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Response") {
        element<Boolean>("success")
        element("data", JsonElement.serializer().descriptor, isOptional = true)
        element("error", Error.serializer().descriptor, isOptional = true)
    }

    override fun serialize(encoder: Encoder, value: Response) {
        encoder.encodeStructure(descriptor) {
            encodeBooleanElement(descriptor, 0, value.success)
            if (value is FailureResponse) {
                encodeNullableSerializableElement(descriptor, 2, Error.serializer(), value.error)
            } else if (value is SuccessResponse) {
                encodeNullableSerializableElement(descriptor, 1, JsonElement.serializer(), value.data)
            }
        }
    }

    override fun deserialize(decoder: Decoder): Response {
        var success: Boolean? = null

        var data: JsonElement? = null
        var error: Error? = null

        decoder.decodeStructure(BlockingSerializer.descriptor) {
            loop@ while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> success = decodeBooleanElement(descriptor, index)
                    1 -> data = decodeNullableSerializableElement(descriptor, index, JsonElement.serializer())
                    2 -> error = decodeSerializableElement(descriptor, index, Error.serializer())
                    CompositeDecoder.DECODE_DONE -> break@loop
                    else -> error("Unexpected index: $index")
                }
            }
        }

        return if (success == true) {
            SuccessResponse(data)
        } else {
            FailureResponse(error)
        }
    }
}
