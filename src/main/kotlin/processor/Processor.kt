package org.steamgifts.processor

import org.steamgifts.api.Api
import org.steamgifts.giveaway.Giveaway

const val CACHE_SIZE = 200

class Processor(val apiClient: Api) {
    val giveawaysCache = mutableListOf<String>()

    fun processGiveaways(giveaways: List<Giveaway>): List<Giveaway> {
        val newList = giveaways.map { it.copy() }

        val withPrice = newList
            .sortedWith(compareBy<Giveaway> { it.price }.thenBy { it.participants })
            .mapIndexed { index, giveaway -> giveaway.copy(priceRank = index) }

        val withParticipants = withPrice
            .sortedWith(compareBy<Giveaway> { it.participants }.thenBy { it.price })
            .mapIndexed { index, giveaway -> giveaway.copy(participantsRank = index) }

        val withPerformance = withParticipants
            .sortedBy { it.performance }
            .mapIndexed { index, giveaway -> giveaway.copy(performanceRank = index) }

        return withPerformance
    }

    fun filterGiveaways(giveaways: List<Giveaway>, currentPoints: Int) =
        giveaways.filter { it.price <= currentPoints }

    fun attemptJoinGiveaway(giveaway: Giveaway, currentPoints: Int): Boolean {
        if (giveawaysCache.contains(giveaway.url)) {
            println("Skipping ${giveaway.title}, reason: cache ")
            return false
        }


        if (giveaway.price > currentPoints) {
            println("Skipping ${giveaway.title}, reason: not enough points")
            return false
        }

        val enteredGiveaway = apiClient.enterGiveaway(giveaway)

        if (!enteredGiveaway) {
            println("Skipping ${giveaway.title}, reason: couldn't enter giveaway")
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