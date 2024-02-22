package assertions

/**
 * Asserts that the given UUID is valid.
 * @throws AssertionError If the given UUID is not valid.
 */
fun assertIsUUID(uuid: String) {
    val regex = Regex("^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89ab][a-f0-9]{3}-[a-f0-9]{12}\$")
    assert(uuid.matches(regex)) { "The given UUID is not valid. Got: $uuid" }
}
