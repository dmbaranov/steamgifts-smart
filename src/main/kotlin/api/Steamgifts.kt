package org.steamgifts.api

import kotlinx.coroutines.delay
import org.jsoup.nodes.Document
import org.steamgifts.giveaway.Giveaway
import org.steamgifts.logger.Logger
import org.steamgifts.utils.FlareSolverr
import kotlin.time.Duration.Companion.seconds

const val BASE_URL = "https://www.steamgifts.com"

class SteamgiftsApi(
    private val authCookie: String,
    private val flareSolverr: FlareSolverr
) : Api {
    private var cachedHTML: Document? = null
    private val cookies = listOf(
        FlareSolverr.Cookie("PHPSESSID", authCookie, ".steamgifts.com")
    )

    override suspend fun getRawGiveaways(): List<Giveaway> {
        val doc = getPage("/") ?: return listOf()

        return try {
            doc.select("[data-game-id]").map {
                Giveaway(
                    title = it.select(".giveaway__heading__name").text(),
                    url = it.select(".giveaway__heading__name").attr("href"),
                    rawParticipants = getIntValue(it.select(".giveaway__links a").first()!!.text()),
                    price = getIntValue(it.select(".giveaway__heading__thin").last()!!.text()),
                    copies = it.select(".giveaway__heading__thin").first()!!.text().let {
                        if (it.contains("Copies")) getIntValue(it) else 1
                    }
                )
            }
        } catch (e: Exception) {
            Logger.error("Could not parse giveaways", e)
            listOf()
        }
    }

    override suspend fun getCurrentPoints(cached: Boolean): Int? {
        val doc = if (cached && cachedHTML != null) cachedHTML else getPage("/")
        if (doc == null) return null
        return doc.select(".nav__points").first()?.let { getIntValue(it.text()) }
    }

    override suspend fun enterGiveaway(giveaway: Giveaway): Boolean {
        val doc = getPage(giveaway.url) ?: throw Exception("could not receive giveaway page")

        val enterButton = doc.select("div[data-do=entry_insert]").first()
        if (enterButton == null || enterButton.hasClass("is-hidden")) {
            throw Exception("no enter button")
        }

        val xsrfToken = doc.select("input[name=xsrf_token]").first()?.attr("value")
            ?: throw Exception("no xsrf token")

        val postData = "xsrf_token=$xsrfToken&do=entry_insert&code=${giveaway.code}"
        val response = flareSolverr.post("$BASE_URL/ajax.php", postData, cookies)
            ?: throw Exception("enter request failed")

        return response.contains("success")
    }

    private fun getIntValue(str: String): Int = str.filter { it.isDigit() }.toInt()

    private suspend fun getPage(url: String): Document? {
        delay((3..7).random().seconds)
        val doc = flareSolverr.get("$BASE_URL$url", cookies)
        cachedHTML = doc ?: cachedHTML
        return doc
    }
}
