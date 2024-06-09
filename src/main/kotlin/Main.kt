package org.steamgifts

import org.steamgifts.api.Api
import org.steamgifts.processor.Processor
import java.util.Properties
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess


fun main() {
    val config = ConfigLoader.loadConfig()

    val apiClient = Api(config.getProperty("AUTH_COOKIE"))
    val processor = Processor(apiClient)
    var loopCount = 0

    while (true) {
        println("Starting loop ${++loopCount}")

        val giveaways = apiClient.getRawGiveaways()
        val points = apiClient.getCurrentPoints()

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

object ConfigLoader {
    fun loadConfig(): Properties {
        val props = this.javaClass.classLoader.getResourceAsStream("application.properties")
            .use { Properties().apply { load(it) } }

        return props
    }
}