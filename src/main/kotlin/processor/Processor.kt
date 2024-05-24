package org.steamgifts.processor

import org.steamgifts.giveaway.Giveaway

class Processor() {
    fun processGiveaways(giveaways: List<Giveaway>) {
        giveaways.sortedBy { it.price }.forEachIndexed { index, giveaway -> giveaway.setPriceRank(index) }
        giveaways.sortedBy { it.participants }.forEachIndexed { index, giveaway -> giveaway.setParticipantsRank(index) }
        giveaways.sortedBy { it.performance }.forEachIndexed { index, giveaway -> giveaway.setPerformanceRank(index) }
        giveaways.forEach { it.markAsProcessed() }
    }
}