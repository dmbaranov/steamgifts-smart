package org.steamgifts.processor

import org.steamgifts.giveaway.Giveaway
import org.steamgifts.parser.Parser

const val CACHE_SIZE = 200

class Processor(val parser: Parser) {
    val giveawaysCache = mutableListOf<String>()

    fun processGiveaways(giveaways: List<Giveaway>) {
        giveaways.sortedBy { it.price }.forEachIndexed { index, giveaway -> giveaway.priceRank = index }
        giveaways.sortedBy { it.participants }.forEachIndexed { index, giveaway -> giveaway.participantsRank = index }
        giveaways.sortedBy { it.performance }.forEachIndexed { index, giveaway -> giveaway.performanceRank = index }
        giveaways.forEach { it.markAsProcessed() }
    }

    fun filterGiveaways(giveaways: List<Giveaway>, currentPoints: Int) =
        giveaways.filter { it.processed && it.price <= currentPoints }

    fun attemptJoinGiveaway(giveaway: Giveaway, currentPoints: Int): Boolean {
        if (giveawaysCache.contains(giveaway.url)) {
            println("Skipping ${giveaway.title}, reason: cache ")
            return false
        }

        if (giveaway.price > currentPoints) {
            println("Skipping ${giveaway.title}, reason: not enough points")
            return false
        }

        val canJoinGiveaway = parser.getCanJoinGiveaway(giveaway.url)

        if (!canJoinGiveaway) {
            println("Skipping ${giveaway.title}, reason: couldn't join giveaway")
            return false
        }

        val joinResult = giveaway.joinGiveaway()

        if (!joinResult) {
            println("Skipping ${giveaway.title}, reason: something went wrong")
            return false
        }

        println("Joined giveaway ${giveaway.title}")
        cacheGiveaway(giveaway)
        return true
    }

    private fun cacheGiveaway(giveaway: Giveaway) {
        if (giveawaysCache.size >= CACHE_SIZE) {
            giveawaysCache.clear()
        }

        giveawaysCache.add(giveaway.url)
    }
}