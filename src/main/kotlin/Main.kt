package org.steamgifts


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.steamgifts.api.Api
import org.steamgifts.api.SteamgiftsApi
import org.steamgifts.logger.Logger
import org.steamgifts.processor.Processor
import java.util.Properties
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.minutes

object ConfigLoader {
    fun loadConfig(): Properties {
        val props = this.javaClass.classLoader.getResourceAsStream("application.properties")
            .use { Properties().apply { load(it) } }

        return props
    }
}

fun main(): Unit = runBlocking {
    val config = ConfigLoader.loadConfig()
    val steamgiftsApi = SteamgiftsApi(config.getProperty("AUTH_COOKIE"))

    launch(Dispatchers.Default) { startLoop(steamgiftsApi) }
}

suspend fun startLoop(api: Api) {
    val processor = Processor(api)
    var loopCount = 0

    while (true) {
        Logger.log("Starting loop ${++loopCount}")
        val giveaways = api.getRawGiveaways()
        val points = api.getCurrentPoints()

        if (points == null) {
            Logger.error("Cannot get points, something went wrong")
            exitProcess(1)
        }

        val processedGiveaways = processor.filterGiveaways(giveaways, points).let { processor.processGiveaways(it) }

        Logger.log("Handling ${processedGiveaways.size}, current points: $points")

        processedGiveaways.forEach { processor.attemptJoinGiveaway(it, points) }

        Logger.log("Loop done, going to sleep")
        delay((120..180).random().minutes)
    }
}