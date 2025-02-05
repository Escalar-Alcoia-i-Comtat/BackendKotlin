package data

import io.ktor.http.content.PartData
import kotlinx.serialization.Serializable
import org.jetbrains.annotations.VisibleForTesting

@Serializable
data class ExternalTrack(
    val type: Type,
    val url: String
) {
    companion object {
        /**
         * Decodes a part from a form into a list of [data.ExternalTrack]`s`. Format:
         * ```
         * <Type>;<URL>
         * ```
         * Example:
         * ```
         * Wikiloc;https://
         * ```
         *
         * @param partData The received part data.
         * @param isLenient If `false`, the function will throw an [IllegalArgumentException] when a line is not valid.
         */
        fun decodeFromPart(partData: PartData.FormItem, isLenient: Boolean = true): List<ExternalTrack> {
            return decodeFromPart(partData.value, isLenient)
        }

        @VisibleForTesting
        fun decodeFromPart(partValue: String, isLenient: Boolean): MutableList<ExternalTrack> {
            val lines = partValue.split('\n').filterNot(String::isEmpty)
            val list = mutableListOf<ExternalTrack>()
            for (line in lines) {
                val typeStr = line.substringBefore(';')
                val url = line.substringAfter(';')
                try {
                    val type = Type.valueOf(typeStr)
                    list += ExternalTrack(type, url)
                } catch (_: IllegalArgumentException) {
                    require(isLenient) { "Got an invalid type: $typeStr" }
                }
            }
            return list
        }
    }

    enum class Type {
        Wikiloc
    }
}
