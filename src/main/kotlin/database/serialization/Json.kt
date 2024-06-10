package database.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import server.response.FailureResponse
import server.response.Response
import server.response.SuccessResponse

@OptIn(ExperimentalSerializationApi::class)
val Json = Json {
    explicitNulls = false
    isLenient = true
    encodeDefaults = true
    ignoreUnknownKeys = true

    serializersModule = SerializersModule {
        polymorphic(Response::class) {
            defaultDeserializer { DefaultResponseSerializer }
            subclass(SuccessResponse::class)
            subclass(FailureResponse::class)
        }
    }
}
