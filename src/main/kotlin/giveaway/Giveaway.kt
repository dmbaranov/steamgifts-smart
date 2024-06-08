package org.steamgifts.giveaway


class Giveaway(val title: String, val url: String, val price: Int, val participants: Int, val copies: Int) {
    private val worth = 1
    var processed = false
    var priceRank = 0
    var participantsRank = 0
    var performanceRank = 0
    val code get() = url.split("/")[2]
    val worthByPrice get() = worth / price
    val worthByParticipants get() = worth / participants / copies
    val performance get() = (worthByPrice + worthByParticipants) / 2
    val rank get() = (priceRank + participantsRank + performanceRank) / 3


    fun markAsProcessed() {
        this.processed = true
    }
}