package data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TestPitchInfo {
    @Test
    fun `test JSON`() {
        val pitchInfo = PitchInfo(
            1U,
            Grade.G7C,
            100U,
            Ending.CHAIN_CARABINER,
            EndingInfo.EQUIPPED,
            EndingInclination.HORIZONTAL
        )
        assertEquals(pitchInfo, PitchInfo.fromJson(pitchInfo.toJson()))
    }

    @Test
    fun `test equals`() {
        val pitchInfo1 = PitchInfo(1U)
        val pitchInfo2 = PitchInfo(1U)
        val pitchInfo3 = PitchInfo(1U, grade = Grade.G7C)

        assertEquals(pitchInfo1, pitchInfo1)
        assertEquals(pitchInfo1, pitchInfo2)
        assertNotEquals(pitchInfo1, pitchInfo3)
    }
}
