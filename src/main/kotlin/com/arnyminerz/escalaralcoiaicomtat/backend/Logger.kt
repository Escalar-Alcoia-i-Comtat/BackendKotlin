package com.arnyminerz.escalaralcoiaicomtat.backend

import com.arnyminerz.escalaralcoiaicomtat.backend.utils.AnsiColors

object Logger {
    @LogLevel
    var level = LogLevel.INFO

    fun debug(message: String) {
        if (LogLevel.DEBUG >= level) println(AnsiColors.WHITE + message + AnsiColors.RESET)
    }

    fun info(message: String) {
        if (LogLevel.INFO >= level) println(AnsiColors.BLUE + message + AnsiColors.RESET)
    }

    fun warn(message: String) {
        if (LogLevel.WARN >= level) println(AnsiColors.YELLOW + message + AnsiColors.RESET)
    }

    fun error(message: String) {
        if (LogLevel.ERROR >= level) println(AnsiColors.YELLOW + message + AnsiColors.RESET)
    }
}

annotation class LogLevel {
    companion object {
        const val DEBUG = 0
        const val INFO = 1
        const val WARN = 2
        const val ERROR = 3
    }
}
