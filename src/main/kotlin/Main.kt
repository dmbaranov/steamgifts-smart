package org.steamgifts

import org.steamgifts.parser.Parser
import org.steamgifts.processor.Processor
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess


fun main() {
    val parser = Parser()
    val processor = Processor(parser)
    var loopCount = 0

    while (true) {
        println("Starting loop ${++loopCount}")

        val giveaways = parser.getRawGiveaways()
        val points = parser.getCurrentPoints()

        if (points == null) {
            println("Cannot get points, something went wrong")
            exitProcess(1)
        }

        val processedGiveaways = processor.filterGiveaways(giveaways, points).also { processor.processGiveaways(it) }
        val sortedGiveaways = processedGiveaways.sortedBy { it.rank }

        println("Handling ${sortedGiveaways.size}, current points: $points")

        sortedGiveaways.forEach { processor.attemptJoinGiveaway(it, points) }

        println("Loop done, going to sleep")
        TimeUnit.MINUTES.sleep((120..180).random().toLong())
    }
}