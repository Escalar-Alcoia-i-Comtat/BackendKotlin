package system

sealed class EnvironmentVariable(
    val name: String,
    private val default: String? = null
) {
    private val _sysValue: String? by lazy { System.getenv(name) ?: default }

    private var _value: String? = _sysValue

    var value: String?
        get() = _value
        set(value) { _value = value }

    val isSet: Boolean get() = _value != null

    /**
     * Gets the value of the environment variable.
     * @throws NullPointerException if the value is not set.
     */
    fun getValue(): String = value!!
}
