package org.steamgifts.parser

import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.steamgifts.giveaway.Giveaway


val HEADERS = mapOf(
    "Accept" to "application/json, text/javascript, */*; q=0.01",
    "Accept-Encoding" to "gzip, deflate, br",
    "Content-Type" to "application/x-www-form-urlencoded; charset=UTF-8",
    "X-Requested-With" to "XMLHttpRequest"
)

const val BASE_URL = "https://steamgifts.com"

class Parser {
    private var cachedHTML: Document? = null
    private val parserInstance: Connection = Jsoup.connect(BASE_URL)
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

    fun getCanJoinGiveaway(giveawayUrl: String): Boolean {
        return false
    }

    private fun getIntValue(str: String): Int = str.filter { it.isDigit() }.toInt()

    private fun getPage(url: String): Document? {
        val randomSleepTime = (3..7).random().toLong()
        Thread.sleep(randomSleepTime * 1000)

        val page = try {
            parserInstance.newRequest(BASE_URL + url).get()
        } catch (e: Exception) {
            println("Error has occurred during HTML retrieval")
            println(e)
            null
        }

        return page.also { cachedHTML = it }

    }
}