package org.steamgifts.giveaway


data class Giveaway(
    val title: String,
    val url: String,
    val price: Int,
    val rawParticipants: Int,
    val copies: Int,
    val priceRank: Int = 0,
    val participantsRank: Int = 0,
    val performanceRank: Int = 0
) {
    private val worth = 1
    val code get() = url.split("/")[2]
    val participants get() = rawParticipants / copies
    val worthByPrice get() = worth.toDouble() / price.toDouble()
    val worthByParticipants get() = worth.toDouble() / participants.toDouble() / copies.toDouble()
    val performance get() = (worthByPrice + worthByParticipants).toDouble() / 2.toDouble()
    val rank get() = (priceRank + participantsRank + performanceRank).toDouble() / 3.toDouble()
}