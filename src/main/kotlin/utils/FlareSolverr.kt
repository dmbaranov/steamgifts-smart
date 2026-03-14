package org.steamgifts.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.jsoup.nodes.Document
import org.jsoup.Jsoup
import org.steamgifts.logger.Logger
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class FlareSolverr(private val url: String) {
    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()
    private val gson = Gson()

    fun get(targetUrl: String, cookies: List<Cookie> = emptyList()): Document? {
        val body = gson.toJson(mapOf(
            "cmd" to "request.get",
            "url" to targetUrl,
            "maxTimeout" to 60000,
            "cookies" to cookies.map { it.toMap() }
        ))

        val response = execute(body) ?: return null
        return Jsoup.parse(response)
    }

    fun post(targetUrl: String, postData: String, cookies: List<Cookie> = emptyList()): String? {
        val body = gson.toJson(mapOf(
            "cmd" to "request.post",
            "url" to targetUrl,
            "postData" to postData,
            "maxTimeout" to 60000,
            "cookies" to cookies.map { it.toMap() }
        ))

        return execute(body)
    }

    private fun execute(jsonBody: String): String? {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .timeout(Duration.ofSeconds(90))
            .build()

        return try {
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            val json = gson.fromJson(response.body(), JsonObject::class.java)

            if (json.get("status").asString != "ok") {
                Logger.error("FlareSolverr error: ${json.get("message").asString}")
                return null
            }

            json.getAsJsonObject("solution").get("response").asString
        } catch (e: Exception) {
            Logger.error("FlareSolverr request failed", e)
            null
        }
    }

    data class Cookie(val name: String, val value: String, val domain: String, val path: String = "/") {
        fun toMap() = mapOf("name" to name, "value" to value, "domain" to domain, "path" to path)
    }
}
