package com.arnyminerz.escalaralcoiaicomtat.backend.database.entity

import com.arnyminerz.escalaralcoiaicomtat.backend.data.BlockingRecurrenceYearly
import com.arnyminerz.escalaralcoiaicomtat.backend.data.BlockingTypes
import com.arnyminerz.escalaralcoiaicomtat.backend.database.table.BlockingTable
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonOf
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.serialization.JsonSerializable
import java.time.Instant
import java.time.LocalDateTime
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.json.JSONObject

class Blocking(id: EntityID<Int>) : BaseEntity(id), JsonSerializable {
    companion object : IntEntityClass<Blocking>(BlockingTable)

    override var timestamp: Instant by BlockingTable.timestamp

    var type: BlockingTypes by BlockingTable.type

    var recurrence: BlockingRecurrenceYearly?
        get() = if (arrayOf(_fromDay, _fromMonth, _toDay, _toMonth).all { it != null })
            BlockingRecurrenceYearly(_fromDay!!, _fromMonth!!, _toDay!!, _toMonth!!)
        else
            null
        set(value) {
            println("Updating recurrence: $value")
            _fromDay = value?.fromDay
            _fromMonth = value?.fromMonth
            _toDay = value?.toDay
            _toMonth = value?.toMonth

            _endDate = null
        }

    var endDate: LocalDateTime?
        get() = _endDate
        set(value) {
            _fromDay = null
            _fromMonth = null
            _toDay = null
            _toMonth = null

            _endDate = value
        }

    var path by Path referencedOn BlockingTable.path


    private var _fromDay by BlockingTable.fromDay
    private var _fromMonth by BlockingTable.fromMonth
    private var _toDay by BlockingTable.toDay
    private var _toMonth by BlockingTable.toMonth

    private var _endDate by BlockingTable.endDate

    override fun toJson(): JSONObject = jsonOf(
        "id" to id.value,
        "type" to type,
        "recurrence" to recurrence,
        "end_date" to endDate
    )
}
