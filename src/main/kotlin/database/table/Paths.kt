package database.table

import database.SqlConsts

object Paths: BaseTable() {
    /**
     * The maximum amount of images allowed in a path.
     */
    const val MAX_IMAGES = 10

    val displayName = varchar("display_name", SqlConsts.DISPLAY_NAME_LENGTH)
    val sketchId = uinteger("sketch_id")

    val height = uinteger("height").nullable()
    val grade = varchar("grade", SqlConsts.GRADE_LENGTH).nullable()
    val ending = varchar("ending", SqlConsts.ENDING_LENGTH).nullable()

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

    val showDescription = bool("show_description").default(false)
    val description = varchar("description", SqlConsts.DESCRIPTION_LENGTH).nullable()

    val builder = varchar("builder", SqlConsts.BUILDER_LENGTH).nullable()
    val reBuilder = varchar("re_builder", SqlConsts.RE_BUILDER_LENGTH).nullable()

    val images = varchar("images", SqlConsts.FILE_LENGTH * MAX_IMAGES).nullable().default(null)

    val sector = reference("sector", Sectors)
}
