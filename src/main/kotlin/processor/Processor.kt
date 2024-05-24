package org.steamgifts.processor

import org.steamgifts.giveaway.Giveaway

class Processor() {
    fun processGiveaways(giveaways: List<Giveaway>) {
        giveaways
            .sortedWith(compareBy { it.price })
            .mapIndexed { index, giveaway -> giveaway.setPriceRank(index) }
            .sortedWith(compareBy { it.participants })
            .mapIndexed { index, giveaway -> giveaway.setParticipantsRank(index) }
            .sortedWith(compareBy { it.performance })
            .mapIndexed { index, giveaway -> giveaway.setPerformanceRank(index) }
            .sortedWith(compareBy { it.rank })
            .map { it.markAsProcessed() }
    }

    private fun getWorth() {}

    private fun getLikelyHood() {}

    private fun getPriceRank() {}

    private fun getWorthRank() {}

    private fun getLikelyhoodRank() {}

    private fun getFinalRank() {}
}