package org.steamgifts.parser

import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.extractIt
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.html5.div
import org.steamgifts.giveaway.Giveaway
import java.util.ArrayList

class Parser {
    fun getRawGiveawayListAndPoints(): Pair<List<Giveaway>, Int> {
        var currentPoints: Int = 0
        val giveawaysList: List<Giveaway> = skrape(HttpFetcher) {
            request {
                // TODO: add User-Agent as well, also check for other headers
                url = "https://steamgifts.com"
            }

            response {
                htmlDocument {
                    relaxed = true

                    div {
                        withClass = "nav__right-container"
                        findFirst {
                            currentPoints = getIntValue(text)
                        }
                    }
                }
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
        }.also { it.filter { it.price < currentPoints } }



        return Pair(giveawaysList, currentPoints)
    }


    private fun getIntValue(str: String): Int = str.filter { it.isDigit() }.toInt()
}