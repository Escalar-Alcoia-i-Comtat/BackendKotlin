import java.io.PrintStream
import java.time.LocalDateTime
import org.jetbrains.annotations.VisibleForTesting
import utils.AnsiColors

object Logger {
    @LogLevel
    var level = LogLevel.INFO

    @VisibleForTesting
    var collectTrace = false

    @Volatile
    var trace = emptyList<String>()
        private set

    @VisibleForTesting
    var out: PrintStream = System.out

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
        if (level >= Logger.level) out.println(color + message + AnsiColors.RESET)

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

@KoverIgnore
annotation class LogLevel {
    companion object {
        const val DEBUG = 0
        const val INFO = 1
        const val WARN = 2
        const val ERROR = 3

        val LevelLetters = arrayOf('D', 'I', 'W', 'E')
    }
}
