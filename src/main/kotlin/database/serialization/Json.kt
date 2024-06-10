package database.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
val Json = Json {
    explicitNulls = false
    isLenient = true
}
