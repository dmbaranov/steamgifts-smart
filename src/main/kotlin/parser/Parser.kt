package org.steamgifts.parser

import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.extractIt
import it.skrape.fetcher.skrape
import it.skrape.selects.html5.a
import it.skrape.selects.html5.div
import org.steamgifts.giveaway.PartialGiveaway
import java.util.ArrayList

class Parser {
    fun getRawGiveawayList(): List<PartialGiveaway> {
        val giveawaysList: List<PartialGiveaway> = skrape(HttpFetcher) {
            request {
                url = "https://steamgifts.com"
            }.also { println("sending the request") }


            extractIt<ArrayList<PartialGiveaway>> {
                htmlDocument {
                    div {
                        withClass = "giveaway__row-outer-wrap"
                        findAll {
                            forEach { giveawayContainer ->
                                it.add(
                                    PartialGiveaway(
                                        title = giveawayContainer.findFirst(".giveaway__heading__name") {
                                            text
                                        },
                                        url = giveawayContainer.findFirst(".giveaway__heading__name") {
                                            attribute("href")
                                        },
                                        participants = giveawayContainer.findFirst(".giveaway__links") {
                                            a {
                                                findFirst {
                                                    text.filter { it.isDigit() }.toInt()
                                                }
                                            }
                                        },
                                        price = giveawayContainer.findLast(".giveaway__heading__thin") {
                                            text.filter { it.isDigit() }.toInt()
                                        },
                                        copies = giveawayContainer.findFirst(".giveaway__heading__thin") {
                                            if (text.contains("Copies")) text.filter { it.isDigit() }.toInt() else 1
                                        }
                                    )
                                )
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
}