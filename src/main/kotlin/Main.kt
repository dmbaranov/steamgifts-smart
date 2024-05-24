package org.steamgifts

import org.steamgifts.parser.Parser
import org.steamgifts.processor.Processor

/**
 * COMPLEX SOLUTION
 * const worth = 1; // when we win, it's a worth
 *
 * const populatedData = data.map(item => ({
 *     ...item,
 *     participants: item.rawParticipants / item.copies,
 * })).map(item => ({
 *     ...item,
 *     worthByPrice: worth / item.price,
 *     worthByParticipants: worth / item.participants,
 * })).map(item => ({
 *     ...item,
 *     likelyhood: (item.worthByPrice + item.worthByParticipants) / 2,
 * }));
 *
 * const sortedByPriceRank = populatedData.sort((a, b) => b.worthByPrice === a.worthByPrice ? b.worthByParticipants - a.worthByParticipants : b.worthByPrice - a.worthByPrice).map((item, index) => ({
 *     ...item,
 *     priceRank: index + 1,
 * }));
 *
 * const sortedByWorthRank = sortedByPriceRank.sort((a, b) => b.worthByParticipants - a.worthByParticipants).map((item, index) => ({
 *     ...item,
 *     worthRank: index + 1,
 * }));
 *
 * const sortedByLikelyhoodRank = sortedByWorthRank.sort((a, b) => b.likelyhood - a.likelyhood).map((item, index) => ({
 *     ...item,
 *     likelyhoodRank: index + 1,
 * }));
 *
 * const result = sortedByLikelyhoodRank.map((item) => ({
 *     ...item,
 *     avgRank: (item.priceRank + item.worthRank + item.likelyhoodRank) / 3,
 * })).sort((a, b) => a.avgRank - b.avgRank);
 */

/**
 * EASY SOLUTION
 * const testSorted = data.map(i => ({
 *     ...i,
 *     participants: i.rawParticipants / i.copies,
 * })).sort((a, b) => a.price / (1 / (a.rawParticipants / a.copies)) - b.price / (1 / (b.rawParticipants / b.copies)));
 */

fun main() {
    val parser = Parser()
    val processor = Processor()
    val (giveaways, points) = parser.getRawGiveawayListAndPoints()
    val processedGiveaways = processor.processGiveaways(giveaways)
}

