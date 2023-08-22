package com.arnyminerz.escalaralcoiaicomtat.backend

import com.arnyminerz.escalaralcoiaicomtat.backend.utils.AnsiColors
import java.time.LocalDateTime

object Logger {
    @LogLevel
    var level = LogLevel.INFO

    private var collectTrace = false

    @Volatile
    var trace = emptyList<String>()
        private set

    fun clear() {
        trace = emptyList()
    }

    fun startCollect() {
        clear()
        collectTrace = true
    }

    fun stopCollect() {
        collectTrace = false
    }

    private fun print(@LogLevel level: Int, color: String, message: String) {
        if (level >= Logger.level) println(color + message + AnsiColors.RESET)

        if (collectTrace) {
            synchronized(trace) {
                trace = trace.toMutableList().apply {
                    val timestamp = LocalDateTime.now()
                    add(timestamp.toString() + " :: " + LogLevel.LevelLetters[level] + " > " + message)
                }
            }
        }
    }

    fun debug(message: String) {
        print(LogLevel.DEBUG, AnsiColors.WHITE, message)
    }

    fun info(message: String) {
        print(LogLevel.INFO, AnsiColors.BLUE, message)
    }

    fun warn(message: String) {
        print(LogLevel.WARN, AnsiColors.YELLOW, message)
    }

    fun error(message: String) {
        print(LogLevel.ERROR, AnsiColors.YELLOW, message)
    }

    fun error(message: String, throwable: Throwable) {
        print(LogLevel.ERROR, AnsiColors.YELLOW, message)
        throwable.printStackTrace()
    }
}

annotation class LogLevel {
    companion object {
        const val DEBUG = 0
        const val INFO = 1
        const val WARN = 2
        const val ERROR = 3

        val LevelLetters = arrayOf('D', 'I', 'W', 'E')
    }
}
