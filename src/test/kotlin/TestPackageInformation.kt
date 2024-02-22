import kotlin.test.Test
import org.junit.jupiter.api.assertDoesNotThrow
import system.Package

class TestPackageInformation {
    @Test
    fun `test version`() {
        assertDoesNotThrow {
            Package.getVersion()
        }
    }
}
