package org.steamgifts.giveaway

data class Giveaway(
    val title: String,
    val url: String,
    val price: Int,
    val participants: Int,
    val worthByPrice: Float,
    val worthByParticipants: Float,
    val likelyhood: Float,
    val priceRank: Int,
    val worthRank: Int,
    val likelyhoodRank: Int,
    val finalRank: Int
)