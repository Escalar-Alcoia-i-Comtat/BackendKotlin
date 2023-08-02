package com.arnyminerz.escalaralcoiaicomtat.backend.database.table

import com.arnyminerz.escalaralcoiaicomtat.backend.database.SqlConsts
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Sector

object Sectors : BaseTable() {
    val displayName = varchar("display_name", SqlConsts.DISPLAY_NAME_LENGTH)

    val imagePath = varchar("image", SqlConsts.FILE_LENGTH)

    val latitude = double("latitude").nullable()
    val longitude = double("longitude").nullable()

    val kidsApt = bool("kids_apt")
    val sunTime = enumeration<Sector.SunTime>("sun_time")
    val walkingTime = uinteger("walking_time").nullable()
    val weight = varchar("weight", SqlConsts.WEIGHT_LENGTH).default("0000")

    val zone = reference("zone", Zones)
}
