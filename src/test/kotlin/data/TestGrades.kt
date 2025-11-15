package data

import kotlin.test.Test
import kotlin.test.assertEquals

class TestGrades {
    @Test
    fun `test fromString name`() {
        for (grade in Grade.entries) {
            assertEquals(
                grade,
                Grade.fromString(grade.name),
                "Conversion failed for name \"${grade.name}\" => $grade",
            )
        }
    }
}
