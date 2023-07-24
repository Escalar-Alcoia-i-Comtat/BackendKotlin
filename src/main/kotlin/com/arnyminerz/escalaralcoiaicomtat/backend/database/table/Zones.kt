package com.arnyminerz.escalaralcoiaicomtat.backend.database.table

import com.arnyminerz.escalaralcoiaicomtat.backend.database.SqlConsts

object Zones : DataTable() {
    val imagePath = varchar("image", SqlConsts.FILE_LENGTH)
    val kmzPath = varchar("kmz", SqlConsts.FILE_LENGTH)

    val latitude = double("latitude").nullable()
    val longitude = double("longitude").nullable()

    val points = varchar("points", SqlConsts.POINTS_LENGTH)

    val area = reference("area", Areas)
}
