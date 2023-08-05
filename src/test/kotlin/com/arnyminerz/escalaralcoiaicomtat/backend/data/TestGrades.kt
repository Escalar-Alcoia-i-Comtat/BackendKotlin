package com.arnyminerz.escalaralcoiaicomtat.backend.data

import kotlin.test.Test
import kotlin.test.assertEquals

class TestGrades {
    @Test
    fun `Test grade conversions`() {
        assertEquals(SportsGrade.G4A, GradeValue.fromString("4º"))
        assertEquals(SportsGrade.G5A, GradeValue.fromString("5º"))
        assertEquals(SportsGrade.G5_PLUS, GradeValue.fromString("5+"))
        assertEquals(SportsGrade.G6A, GradeValue.fromString("6a"))
        assertEquals(SportsGrade.G6B_PLUS, GradeValue.fromString("6b+"))
        assertEquals(SportsGrade.G7C, GradeValue.fromString("7c"))
        assertEquals(SportsGrade.G9C_PLUS, GradeValue.fromString("9c+"))
        assertEquals(SportsGrade.UNKNOWN, GradeValue.fromString("¿?"))
    }
}
