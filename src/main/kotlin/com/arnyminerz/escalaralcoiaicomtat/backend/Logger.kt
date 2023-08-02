package com.arnyminerz.escalaralcoiaicomtat.backend

import com.arnyminerz.escalaralcoiaicomtat.backend.utils.AnsiColors

object Logger {
    fun info(message: String) = println(AnsiColors.BLUE + message + AnsiColors.RESET)

    fun warn(message: String) = println(AnsiColors.YELLOW + message + AnsiColors.RESET)

    fun error(message: String) = println(AnsiColors.YELLOW + message + AnsiColors.RESET)
}
