package com.arnyminerz.escalaralcoiaicomtat.backend.database.table

import com.arnyminerz.escalaralcoiaicomtat.backend.database.SqlConsts

@OptIn(ExperimentalUnsignedTypes::class)
object Paths: BaseTable() {
    val displayName = varchar("display_name", SqlConsts.DISPLAY_NAME_LENGTH)
    val sketchId = uinteger("sketch_id")

    val height = uinteger("height").nullable()
    val grade = varchar("grade", 4).nullable()

    val pitches = varchar("pitches", SqlConsts.PITCH_INFO_LENGTH).nullable()

    val stringCount = uinteger("string_count").nullable()

    val paraboltCount = uinteger("parabolt_count").nullable()
    val burilCount = uinteger("buril_count").nullable()
    val pitonCount = uinteger("piton_count").nullable()
    val spitCount = uinteger("spit_count").nullable()
    val tensorCount = uinteger("tensor_count").nullable()

    val crackerRequired = bool("cracker_required")
    val friendRequired = bool("friend_required")
    val lanyardRequired = bool("lanyard")
    val nailRequired = bool("nail_required")
    val pitonRequired = bool("piton_required")
    val stapesRequired = bool("stapes_required")

    val sector = reference("sector", Sectors)
}
