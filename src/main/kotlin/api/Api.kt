package org.steamgifts.api

import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.steamgifts.giveaway.Giveaway
import java.util.concurrent.TimeUnit


const val BASE_URL = "https://www.steamgifts.com"
val HEADERS = mapOf(
    "Accept" to "application/json, text/javascript, */*; q=0.01",
    "Accept-Encoding" to "gzip, deflate, br",
    "Content-Type" to "application/x-www-form-urlencoded; charset=UTF-8",
    "X-Requested-With" to "XMLHttpRequest"
)

class Api {
    private var cachedHTML: Document? = null
    private val requests: Connection = Jsoup.connect(BASE_URL)
        .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36")
        .headers(HEADERS)

    fun getRawGiveaways(): List<Giveaway> {
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
            println("Could not parse giveaways")
            println(e)
            return listOf()
        }

        return giveaways
    }

    fun getCurrentPoints(cached: Boolean = true): Int? {
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

    fun enterGiveaway(giveaway: Giveaway): Boolean {
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
            println("Failed to enter giveaway")
            println(e)
            return false
        }
    }

    private fun getIntValue(str: String): Int = str.filter { it.isDigit() }.toInt()

    private fun getPage(url: String): Document? {
        val randomSleepTime = (3..7).random().toLong()
        TimeUnit.SECONDS.sleep(randomSleepTime)

        val page = try {
            requests.newRequest(BASE_URL + url).get()
        } catch (e: Exception) {
            println("Error has occurred during HTML retrieval")
            println(e)
            null
        }

        return page.also { cachedHTML = it }

    }
}