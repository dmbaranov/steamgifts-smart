package org.steamgifts.parser

import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.extractIt
import it.skrape.fetcher.skrape
import it.skrape.selects.html5.div
import org.steamgifts.giveaway.PartialGiveaway
import java.util.ArrayList

class Parser {
    fun getRawGiveawayList(): List<PartialGiveaway> {
        val giveawaysList: List<PartialGiveaway> = skrape(HttpFetcher) {
            request {
                // TODO: add User-Agent as well, also check for other headers
                url = "https://steamgifts.com"
            }.also { println("sending the request") }


            extractIt<ArrayList<PartialGiveaway>> {
                htmlDocument {
                    div {
                        withClass = "giveaway__row-outer-wrap"
                        findAll {
                            forEach { giveawayContainer ->
                                val giveaway = PartialGiveaway(
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

        // filter out too expensive giveaways
        giveawaysList.forEach { println(it.title) }
        return listOf()
    }

    fun getCurrentPoints(): Int {
        return 0
    }

    private fun getIntValue(str: String): Int = str.filter { it.isDigit() }.toInt()
}