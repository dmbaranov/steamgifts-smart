package org.steamgifts.logger

import java.time.LocalDateTime

object Logger {
    private val now
        get() = LocalDateTime.now()

    fun log(message: String) {
        println("[I][$now]: $message")
    }

    fun error(message: String, exception: Exception? = null) {
        println("[E][$now]: $message")
        exception?.printStackTrace()
    }
}