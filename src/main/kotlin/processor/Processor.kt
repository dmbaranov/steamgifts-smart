package org.steamgifts.processor

import org.steamgifts.giveaway.Giveaway
import org.steamgifts.giveaway.PartialGiveaway

class Processor(private val rawGiveaways: List<PartialGiveaway>) {
    fun getGiveaways(): List<Giveaway> {
        return listOf();
//        return Giveaway(title, url, price, participants);
    }

    private fun getWorth() {}

    private fun getLikelyHood() {}

    private fun getPriceRank() {}

    private fun getWorthRank() {}

    private fun getLikelyhoodRank() {}

    private fun getFinalRank() {}
}