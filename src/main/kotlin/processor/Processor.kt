package org.steamgifts.processor

import org.steamgifts.giveaway.Giveaway
import org.steamgifts.parser.Parser

const val CACHE_SIZE = 200

class Processor(val parser: Parser) {
    val giveawaysCache = mutableListOf<String>()

    fun processGiveaways(giveaways: List<Giveaway>) {
        giveaways.sortedBy { it.price }.forEachIndexed { index, giveaway -> giveaway.setPriceRank(index) }
        giveaways.sortedBy { it.participants }.forEachIndexed { index, giveaway -> giveaway.setParticipantsRank(index) }
        giveaways.sortedBy { it.performance }.forEachIndexed { index, giveaway -> giveaway.setPerformanceRank(index) }
        giveaways.forEach { it.markAsProcessed() }

    }

    fun attemptJoinGiveaway(giveaway: Giveaway, currentPoints: Int) {
        if (giveawaysCache.contains(giveaway.url)) {
            println("Skipping ${giveaway.title}, reason: cache ")
            return
        }

        if (giveaway.price > currentPoints) {
            println("Skipping ${giveaway.title}, reason: not enough points")
            return
        }

        val canJoinGiveaway = parser.getCanJoinGiveaway(giveaway.url)

        if (!canJoinGiveaway) {
            println("Skipping ${giveaway.title}, reason: couldn't join giveaway")
            return
        }

        val joinResult = giveaway.joinGiveaway()

        if (!joinResult) {
            println("Skipping ${giveaway.title}, reason: something went wrong")
            return
        }

        println("Joined giveaway ${giveaway.title}")
        cacheGiveaway(giveaway)
    }

    private fun cacheGiveaway(giveaway: Giveaway) {
        if (giveawaysCache.size >= CACHE_SIZE) {
            giveawaysCache.clear()
        }

        giveawaysCache.add(giveaway.url)
    }
}