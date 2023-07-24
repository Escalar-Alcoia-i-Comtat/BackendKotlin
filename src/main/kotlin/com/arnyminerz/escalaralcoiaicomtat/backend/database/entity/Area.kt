package com.arnyminerz.escalaralcoiaicomtat.backend.database.entity

import com.arnyminerz.escalaralcoiaicomtat.backend.database.table.Areas
import com.arnyminerz.escalaralcoiaicomtat.backend.storage.Storage
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonOf
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.serialization.JsonSerializable
import java.io.File
import java.net.URL
import java.time.Instant
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.json.JSONObject

/**
 * Represents the data structure of an Area, which contains Zones.
 */
class Area(id: EntityID<Int>): DataEntity(id), JsonSerializable {
    companion object: IntEntityClass<Area>(Areas)

    override var timestamp: Instant by Areas.timestamp
    override var displayName: String by Areas.displayName

    var image: File
        get() = File(Storage.ImagesDir, _image)
        set(value) { _image = value.toRelativeString(Storage.ImagesDir) }

    override var webUrl: URL
        get() = URL(_webUrl)
        set(value) { _webUrl = value.toString() }

    private var _image: String by Areas.imagePath

    private var _webUrl: String by Areas.webUrl

    override fun toJson(): JSONObject = jsonOf(
        "id" to id.value,
        "timestamp" to timestamp.toEpochMilli(),
        "display_name" to displayName,
        "image" to _image,
        "web_url" to webUrl
    )
}
