package org.steamgifts.api

import org.steamgifts.giveaway.Giveaway

interface Api {
    fun getRawGiveaways(): List<Giveaway>

    fun getCurrentPoints(cached: Boolean = true): Int?

    fun enterGiveaway(giveaway: Giveaway): Boolean
}