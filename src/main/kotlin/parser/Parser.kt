package org.steamgifts.parser

import it.skrape.core.document
import it.skrape.fetcher.BrowserFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.eachHref
import it.skrape.selects.html5.a
import org.steamgifts.giveaway.PartialGiveaway

class Parser {
    fun parse(): List<PartialGiveaway> {
        val giveawaysList: List<String> = skrape(BrowserFetcher) {
            request {
                url = "https://www.google.com/"
            }

            response {
                document.a { findAll { eachHref } }
            }
        }

        // filter out too expensive giveaways
        println("parsing..., ${giveawaysList.size}")
        return listOf()
    }
}