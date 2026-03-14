package org.steamgifts


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.steamgifts.api.Api
import org.steamgifts.api.SteamgiftsApi
import org.steamgifts.logger.Logger
import org.steamgifts.processor.Processor
import org.steamgifts.utils.FlareSolverr
import org.steamgifts.utils.Telegram
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
    val flareSolverrUrl = System.getenv("FLARESOLVERR_URL")
        ?: config.getProperty("FLARESOLVERR_URL", "http://localhost:8191/v1")
    val flareSolverr = FlareSolverr(flareSolverrUrl)
    val steamgiftsApi = SteamgiftsApi(config.getProperty("STEAMGIFTS_AUTH_COOKIE"), flareSolverr)

    val telegramToken = System.getenv("TELEGRAM_BOT_TOKEN")
        ?: config.getProperty("TELEGRAM_BOT_TOKEN", "")
    val telegramChatId = System.getenv("TELEGRAM_CHAT_ID")
        ?: config.getProperty("TELEGRAM_CHAT_ID", "")
    val telegram = if (telegramToken.isNotEmpty() && telegramChatId.isNotEmpty()) {
        Telegram(telegramToken, telegramChatId)
    } else null

    launch(Dispatchers.Default) { startLoop(steamgiftsApi, telegram) }
}

suspend fun startLoop(api: Api, telegram: Telegram?) {
    val processor = Processor(api)
    var loopCount = 0

    while (true) {
        Logger.log("Starting loop ${++loopCount}")
        val giveaways = api.getRawGiveaways()
        val points = api.getCurrentPoints()

        if (api.hasWonGiveaway()) {
            Logger.log("Won a giveaway!")
            telegram?.sendSilentMessage("You won a giveaway on SteamGifts! Check https://www.steamgifts.com/giveaways/won")
        }

        if (points == null) {
            Logger.error("Cannot get points, something went wrong")
            exitProcess(1)
        }

        val processedGiveaways = processor.filterGiveaways(giveaways, points).let { processor.processGiveaways(it) }

        Logger.log("Handling ${processedGiveaways.size} giveaways, current points: $points")

        processedGiveaways.forEach { processor.attemptJoinGiveaway(it, points) }

        Logger.log("Loop done, going to sleep")
        delay((120..180).random().minutes)
    }
}