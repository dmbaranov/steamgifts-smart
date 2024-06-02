package org.steamgifts.parser

import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.extractIt
import it.skrape.fetcher.skrape
import it.skrape.selects.html5.div
import org.steamgifts.giveaway.Giveaway
import java.util.ArrayList

val HEADERS = mapOf(
    "Accept" to "application/json, text/javascript, */*; q=0.01",
//    "Accept-Encoding" to "gzip, deflate, br",
//    "Content-Type" to "application/x-www-form-urlencoded; charset=UTF-8",
    "User-Agent" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36",
    "X-Requested-With" to "XMLHttpRequest"
)

class Parser {
    fun getRawGiveaways(): List<Giveaway> {
        return skrape(HttpFetcher) {
            request {
                url = "https://steamgifts.com"
                headers = HEADERS
            }

            extractIt<ArrayList<Giveaway>> {
                htmlDocument {
                    relaxed = true

                    div {
                        withClass = "giveaway__row-outer-wrap"
                        findAll {
                            forEach { giveawayContainer ->
                                val giveaway = Giveaway(
                                    title = giveawayContainer.findFirst(".giveaway__heading__name") { text },
                                    url = giveawayContainer.findFirst(".giveaway__heading__name") { attribute("href") },
                                    participants = giveawayContainer.findFirst(".giveaway__links a") { getIntValue(text) },
                                    price = giveawayContainer.findLast(".giveaway__heading__thin") { getIntValue(text) },
                                    copies = giveawayContainer.findFirst(".giveaway__heading__thin") {
                                        if (text.contains("Copies")) getIntValue(text) else 1
                                    }
                                )

                                it.add(giveaway)
                            }
                        }
                    }
                }
            }
        }
    }

    fun getCurrentPoints(): Int {
        return 1
        return skrape(HttpFetcher) {
            request {
                url = "https://steamgifts.com"
                headers = HEADERS
            }

            extractIt<Int> {
                htmlDocument {
                    relaxed = true

                    div {
                        withClass = "nav__right-container"
                        findFirst { getIntValue(text) }
                    }
                }
            }
        }
    }

    fun getCanJoinGiveaway(giveawayUrl: String): Boolean {
        return skrape(HttpFetcher) {
            request {
                url = giveawayUrl
                headers = HEADERS
            }

            extractIt<Boolean> {
                htmlDocument {
                    relaxed = true

                    div {
                        withClass = ".sidebar__entry-insert:not(is-hidden)"
                        findAll {
                            size == 1
                        }
                    }
                }
            }
        }
    }


    private fun getIntValue(str: String): Int = str.filter { it.isDigit() }.toInt()
}