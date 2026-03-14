package org.steamgifts.utils

import org.steamgifts.logger.Logger
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class Telegram(private val botToken: String, private val chatId: String) {
    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    fun sendSilentMessage(text: String) {
        val encodedText = java.net.URLEncoder.encode(text, "UTF-8")
        val url = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$encodedText&disable_notification=true"

        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .timeout(Duration.ofSeconds(10))
            .build()

        try {
            httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        } catch (e: Exception) {
            Logger.error("Failed to send Telegram message", e)
        }
    }
}
