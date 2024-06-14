package org.steamgifts

import org.steamgifts.api.SteamgiftsApi
import org.steamgifts.logger.Logger
import org.steamgifts.processor.Processor
import java.util.Properties
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess


fun main() {
    val config = ConfigLoader.loadConfig()

    val steamgiftsApi = SteamgiftsApi(config.getProperty("AUTH_COOKIE"))
    val processor = Processor(steamgiftsApi)
    var loopCount = 0

    while (true) {
        Logger.log("Starting loop ${++loopCount}")

        val giveaways = steamgiftsApi.getRawGiveaways()
        val points = steamgiftsApi.getCurrentPoints()

        if (points == null) {
            Logger.error("Cannot get points, something went wrong")
            exitProcess(1)
        }

        val processedGiveaways = processor.filterGiveaways(giveaways, points).also { processor.processGiveaways(it) }
        val sortedGiveaways = processedGiveaways.sortedBy { it.rank }

        Logger.log("Handling ${sortedGiveaways.size}, current points: $points")

        sortedGiveaways.forEach { processor.attemptJoinGiveaway(it, points) }

        Logger.log("Loop done, going to sleep")
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