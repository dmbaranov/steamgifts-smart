package org.steamgifts.api

import kotlinx.coroutines.delay
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.steamgifts.giveaway.Giveaway
import org.steamgifts.logger.Logger
import kotlin.time.Duration.Companion.seconds


const val BASE_URL = "https://www.steamgifts.com"
val HEADERS = mapOf(
    "Accept" to "application/json, text/javascript, */*; q=0.01",
    "Accept-Encoding" to "gzip, deflate, br",
    "Content-Type" to "application/x-www-form-urlencoded; charset=UTF-8",
    "X-Requested-With" to "XMLHttpRequest"
)

class SteamgiftsApi(private val authCookie: String) : Api {
    private var cachedHTML: Document? = null
    private val requests: Connection = Jsoup.connect(BASE_URL)
        .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36")
        .headers(HEADERS)
        .cookie("PHPSESSID", this.authCookie)

    override suspend fun getRawGiveaways(): List<Giveaway> {
        val doc = this.getPage("/")

        if (doc == null) {
            return listOf()
        }

        val giveaways = try {
            doc.select("[data-game-id]").map {
                Giveaway(
                    title = it.select(".giveaway__heading__name").text(),
                    url = it.select(".giveaway__heading__name").attr("href"),
                    participants = this.getIntValue(it.select(".giveaway__links a").first()!!.text()),
                    price = this.getIntValue(it.select(".giveaway__heading__thin").last()!!.text()),
                    copies = it.select(".giveaway__heading__thin").first()!!.text().let {
                        if (it.contains("Copies")) getIntValue(it) else 1
                    }
                )
            }
        } catch (e: Exception) {
            Logger.error("Could not parse giveaways", e)
            return listOf()
        }

        return giveaways
    }

    override suspend fun getCurrentPoints(cached: Boolean): Int? {
        // TODO: check if it's true from the interface
        val doc = if (cached && cachedHTML != null) cachedHTML else this.getPage("/")

        if (doc == null) {
            return null
        }


        val pointsElement = doc.select(".nav__points").first()

        if (pointsElement != null) {
            return getIntValue(pointsElement.text())
        }

        return null
    }

    override suspend fun enterGiveaway(giveaway: Giveaway): Boolean {
        val doc = this.getPage(giveaway.url)

        if (doc == null) {
            return false
        }

        val enterGiveawayButton = doc.select("div[data-do=entry_insert]").first()

        if (enterGiveawayButton == null || enterGiveawayButton.hasClass("is-hidden")) {
            return false
        }

        val xsrfToken = doc.select("input[name=xsrf_token]").first()?.attr("value")

        if (xsrfToken == null) {
            return false
        }

        try {
            val response = this.requests.newRequest("$BASE_URL/ajax.php")
                .method(Connection.Method.POST)
                .data("xsrf_token", xsrfToken)
                .data("do", "entry_insert")
                .data("code", giveaway.code)
                .execute()

            return response.body().contains("success") && response.statusCode() in 200..299
        } catch (e: Exception) {
            Logger.error("Failed to enter giveaway", e)
            return false
        }
    }

    private fun getIntValue(str: String): Int = str.filter { it.isDigit() }.toInt()

    private suspend fun getPage(url: String): Document? {
        delay((3..7).random().seconds)

        val page = try {
            requests.newRequest(BASE_URL + url).get()
        } catch (e: Exception) {
            Logger.error("Error has occurred during HTML retrieval", e)
            null
        }

        // TODO: assign only if not null
        return page.also { cachedHTML = it }
    }
}