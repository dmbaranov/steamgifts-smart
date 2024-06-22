package org.steamgifts.processor

import org.steamgifts.api.Api
import org.steamgifts.giveaway.Giveaway
import org.steamgifts.logger.Logger

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
            .sortedByDescending { it.performance }
            .mapIndexed { index, giveaway -> giveaway.copy(performanceRank = index) }

        return withPerformance.sortedBy { it.rank }
    }

    fun filterGiveaways(giveaways: List<Giveaway>, currentPoints: Int) =
        giveaways.filter { it.price <= currentPoints }

    suspend fun attemptJoinGiveaway(giveaway: Giveaway, currentPoints: Int): Boolean {
        if (giveawaysCache.contains(giveaway.url)) {
            Logger.log("Skipping ${giveaway.title}, reason: cache ")
            return false
        }

        if (giveaway.price > currentPoints) {
            Logger.log("Skipping ${giveaway.title}, reason: not enough points")
            return false
        }

        val enteredGiveaway = try {
            apiClient.enterGiveaway(giveaway)
        } catch (e: Exception) {
            Logger.log("Skipping ${giveaway.title}, reason: ${e.message}")
            false
        }

        if (!enteredGiveaway) {
            return false
        }

        Logger.log("Joined giveaway ${giveaway.title}")
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