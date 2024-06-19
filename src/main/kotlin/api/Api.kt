package org.steamgifts.api

import org.steamgifts.giveaway.Giveaway

interface Api {
    suspend fun getRawGiveaways(): List<Giveaway>

    suspend fun getCurrentPoints(cached: Boolean = true): Int?

    suspend fun enterGiveaway(giveaway: Giveaway): Boolean
}