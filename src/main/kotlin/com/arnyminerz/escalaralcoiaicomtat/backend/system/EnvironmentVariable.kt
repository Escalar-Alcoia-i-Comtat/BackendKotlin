package com.arnyminerz.escalaralcoiaicomtat.backend.system

sealed class EnvironmentVariable(
    val name: String
) {
    private val _sysValue: String? by lazy { System.getenv(name) }

    private var _value: String? = _sysValue

    var value: String?
        get() = _value
        set(value) { _value = value }
}
