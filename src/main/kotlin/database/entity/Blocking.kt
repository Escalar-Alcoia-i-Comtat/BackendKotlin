package database.entity

import data.BlockingRecurrenceYearly
import data.BlockingTypes
import database.table.BlockingTable
import java.time.Instant
import java.time.LocalDateTime
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.json.JSONObject
import utils.jsonOf
import utils.serialization.JsonSerializable

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
        "timestamp" to timestamp,
        "type" to type,
        "recurrence" to recurrence,
        "end_date" to endDate,
        "path_id" to path.id.value,
    )
}
