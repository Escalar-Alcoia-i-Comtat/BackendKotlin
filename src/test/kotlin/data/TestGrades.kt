package data

import kotlin.test.Test
import kotlin.test.assertEquals

class TestGrades {
    @Test
    fun `Test grade conversions`() {
        assertEquals(Grade.G4A, Grade.fromString("4º"))
        assertEquals(Grade.G5A, Grade.fromString("5º"))
        assertEquals(Grade.G5_PLUS, Grade.fromString("5+"))
        assertEquals(Grade.G6A, Grade.fromString("6a"))
        assertEquals(Grade.G6B_PLUS, Grade.fromString("6b+"))
        assertEquals(Grade.G7C, Grade.fromString("7c"))
        assertEquals(Grade.G9C_PLUS, Grade.fromString("9c+"))
        assertEquals(Grade.A2, Grade.fromString("A2"))
        assertEquals(Grade.A_EQUIPPED, Grade.fromString("Ae"))
        assertEquals(Grade.UNKNOWN, Grade.fromString("¿?"))
    }
}
