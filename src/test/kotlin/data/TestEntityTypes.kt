package data

import database.EntityTypes
import kotlin.test.Test
import kotlin.test.assertEquals

class TestEntityTypes {
    @Test
    fun testName() {
        assertEquals("AREA", EntityTypes.AREA.name)
        assertEquals("ZONE", EntityTypes.ZONE.name)
        assertEquals("SECTOR", EntityTypes.SECTOR.name)
        assertEquals("PATH", EntityTypes.PATH.name)
        assertEquals("BLOCKING", EntityTypes.BLOCKING.name)
    }
}
