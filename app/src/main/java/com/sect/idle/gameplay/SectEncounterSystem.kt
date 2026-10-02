package com.sect.idle.gameplay

import kotlin.random.Random

enum class EncounterType {
    TRAVELING_MERCHANT,
    DEMONIC_INVASION,
    ANCIENT_IMMORTAL_ABODE,
    HEAVENLY_EPIPHANY_RAIN,
    WANDERING_CULTIVATOR_SEEKER
}

data class EncounterChoice(
    val id: String,
    val title: String,
    val description: String,
    val costDescription: String,
    val rewardDescription: String,
    val requiresSpiritStones: Long = 0L,
    val requiresSpiritHerbs: Long = 0L,
    val requiresSpiritOres: Long = 0L
)

data class SectEncounter(
    val id: String,
    val title: String,
    val chineseTitle: String,
    val description: String,
    val iconEmoji: String,
    val eventType: EncounterType,
    val choices: List<EncounterChoice>,
    val timestamp: Long = System.currentTimeMillis()
)

data class EncounterResult(
    val title: String,
    val message: String,
    val stonesDelta: Long = 0L,
    val herbsDelta: Long = 0L,
    val oresDelta: Long = 0L,
    val pillsDelta: Long = 0L,
    val jadeDelta: Long = 0L,
    val essenceDelta: Long = 0L,
    val discipleHpDamagePercent: Int = 0,
    val recruitNewDisciple: Boolean = false
)

object SectEncounterSystem {

    /**
     * Generates a random sect encounter from available archetypes.
     */
    fun generateRandomEncounter(sectRealm: Int): SectEncounter {
        val types = EncounterType.values()
        val selectedType = types.random()
        val id = "enc_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}"

        return when (selectedType) {
            EncounterType.TRAVELING_MERCHANT -> SectEncounter(
                id = id,
                title = "Traveling Cloud Merchant",
                chineseTitle = "云游行商",
                description = "A mysterious nomadic peddler riding a giant flying crane lands outside the sect gate, offering exotic heavenly treasures from distant empires.",
                iconEmoji = "🧙‍♂️",
                eventType = EncounterType.TRAVELING_MERCHANT,
                choices = listOf(
                    EncounterChoice(
                        id = "buy_rare_pills",
                        title = "Purchase Sacred Spirit Pills",
                        description = "Exchange treasury stones for a batch of high-purity cultivation pills.",
                        costDescription = "🪙 -500 Spirit Stones",
                        rewardDescription = "💊 +8 Spirit Pills, ✨ +10 Dao Essence",
                        requiresSpiritStones = 500L
                    ),
                    EncounterChoice(
                        id = "trade_herbs_for_jade",
                        title = "Barter Bulk Herbs for Spirit Jade",
                        description = "Trade surplus raw mountain herbs for refined spirit jade crystals.",
                        costDescription = "🌱 -80 Spirit Herbs",
                        rewardDescription = "💎 +15 Spirit Jade",
                        requiresSpiritHerbs = 80L
                    ),
                    EncounterChoice(
                        id = "decline_merchant",
                        title = "Politely Decline",
                        description = "Wish the merchant safe travels and maintain sect treasury reserves.",
                        costDescription = "No cost",
                        rewardDescription = "No changes"
                    )
                )
            )

            EncounterType.DEMONIC_INVASION -> SectEncounter(
                id = id,
                title = "Demonic Beast Surge",
                chineseTitle = "万兽袭山",
                description = "A horde of berserk Shadow Beasts erupts from the mountain rift, charging directly toward the sect's outer disciple quarters!",
                iconEmoji = "🐺",
                eventType = EncounterType.DEMONIC_INVASION,
                choices = listOf(
                    EncounterChoice(
                        id = "activate_guardian_array",
                        title = "Overcharge Mountain Guardian Array",
                        description = "Channel ores and spirit stones into the defensive ward for 100% barrier protection.",
                        costDescription = "🪙 -400 Spirit Stones, ⛏️ -30 Ores",
                        rewardDescription = "100% Safe Defense, ⚔️ +50 Sect Power",
                        requiresSpiritStones = 400L,
                        requiresSpiritOres = 30L
                    ),
                    EncounterChoice(
                        id = "dispatch_disciples",
                        title = "Order Disciples to Slay the Beasts",
                        description = "Direct disciples into active combat. Earn massive monster cores at the risk of minor battle injuries.",
                        costDescription = "Disciples sustain 15-25% HP damage",
                        rewardDescription = "🪙 +1,200 Spirit Stones, 💎 +10 Jade, ⚔️ +150 Sect Power"
                    ),
                    EncounterChoice(
                        id = "feed_spirit_herbs",
                        title = "Scatter Calming Spirit Herb Baits",
                        description = "Lure the beast pack away toward distant valleys with soothing herbs.",
                        costDescription = "🌱 -60 Spirit Herbs",
                        rewardDescription = "Beasts pacified, avoids all casualties",
                        requiresSpiritHerbs = 60L
                    )
                )
            )

            EncounterType.ANCIENT_IMMORTAL_ABODE -> SectEncounter(
                id = id,
                title = "Ancient Grotto Discovered",
                chineseTitle = "古仙洞府",
                description = "During morning patrols, disciples uncover the mossy entrance to a thousand-year-old secluded immortal cave glowing with celestial runes.",
                iconEmoji = "⛩️",
                eventType = EncounterType.ANCIENT_IMMORTAL_ABODE,
                choices = listOf(
                    EncounterChoice(
                        id = "delve_deep",
                        title = "Send Expedition to Delve Deep",
                        description = "Explore the inner chamber risking ancient protective traps for legendary loot.",
                        costDescription = "🪙 -200 Spirit Stones (Formation tools)",
                        rewardDescription = "💎 +20 Spirit Jade, 💊 +10 Pills, ✨ +25 Dao Essence",
                        requiresSpiritStones = 200L
                    ),
                    EncounterChoice(
                        id = "harvest_outer_veins",
                        title = "Safely Harvest Outer Spirit Veins",
                        description = "Mine the entrance crystals and gather spiritual vegetation without entering.",
                        costDescription = "No cost",
                        rewardDescription = "🪙 +600 Spirit Stones, 🌱 +40 Spirit Herbs"
                    )
                )
            )

            EncounterType.HEAVENLY_EPIPHANY_RAIN -> SectEncounter(
                id = id,
                title = "Heavenly Auspicious Rain",
                chineseTitle = "天降祥瑞",
                description = "Golden celestial clouds gather over the mountain peak as divine rain showers the valley with pure cosmic spirit energy!",
                iconEmoji = "🌧️",
                eventType = EncounterType.HEAVENLY_EPIPHANY_RAIN,
                choices = listOf(
                    EncounterChoice(
                        id = "hold_meditation_ceremony",
                        title = "Convene Grand Meditation Gathering",
                        description = "Lead all disciples into synchronized breathing under the celestial rain.",
                        costDescription = "🪙 -150 Spirit Stones (Offering incense)",
                        rewardDescription = "💊 +5 Spirit Pills, ✨ +40 Dao Essence, +200 Sect Power",
                        requiresSpiritStones = 150L
                    ),
                    EncounterChoice(
                        id = "collect_heavenly_dew",
                        title = "Deploy Spirit Vessels to Collect Dew",
                        description = "Capture the liquid essence for alchemy and dragon vein enrichment.",
                        costDescription = "No cost",
                        rewardDescription = "🌱 +70 Spirit Herbs, 💎 +8 Spirit Jade"
                    )
                )
            )

            EncounterType.WANDERING_CULTIVATOR_SEEKER -> SectEncounter(
                id = id,
                title = "Wandering Genius Seeks Sect",
                chineseTitle = "散修登门",
                description = "A talented wandering rogue cultivator carrying an ancient jade token knocks at your mountain gate, wishing to join your sect lineage.",
                iconEmoji = "🥋",
                eventType = EncounterType.WANDERING_CULTIVATOR_SEEKER,
                choices = listOf(
                    EncounterChoice(
                        id = "recruit_disciple_gift",
                        title = "Welcome with Initiation Gift",
                        description = "Provide welcome stones and initiate the talent as an official sect disciple.",
                        costDescription = "🪙 -300 Spirit Stones",
                        rewardDescription = "👤 Recruits 1 New Talented Disciple!",
                        requiresSpiritStones = 300L
                    ),
                    EncounterChoice(
                        id = "exchange_dao_wisdom",
                        title = "Exchange Dao Insights as Guests",
                        description = "Spend the evening discussing martial sutras and parting as mutual allies.",
                        costDescription = "No cost",
                        rewardDescription = "✨ +20 Dao Essence, 🪙 +400 Spirit Stones"
                    )
                )
            )
        }
    }

    /**
     * Resolves the choice selected by the player and returns the concrete game result.
     */
    fun resolveChoice(encounter: SectEncounter, choiceId: String): EncounterResult {
        return when (choiceId) {
            // Merchant
            "buy_rare_pills" -> EncounterResult(
                title = "Pills Acquired!",
                message = "The merchant handed over 8 potent Spirit Pills and concentrated Dao Essence before taking off into the clouds.",
                stonesDelta = -500L,
                pillsDelta = 8L,
                essenceDelta = 10L
            )
            "trade_herbs_for_jade" -> EncounterResult(
                title = "Barter Completed!",
                message = "You exchanged 80 bundles of mountain herbs for 15 glistening Spirit Jade stones.",
                herbsDelta = -80L,
                jadeDelta = 15L
            )
            "decline_merchant" -> EncounterResult(
                title = "Merchant Departed",
                message = "The merchant bowed respectfully and flew away to the next cultivation province."
            )

            // Demonic Invasion
            "activate_guardian_array" -> EncounterResult(
                title = "Array Repelled the Beasts!",
                message = "The defensive barrier blazed with golden light, incinerating the demon beasts on contact. No disciples were harmed!",
                stonesDelta = -400L,
                oresDelta = -30L
            )
            "dispatch_disciples" -> EncounterResult(
                title = "Glorious Mountain Victory!",
                message = "Your disciples fought bravely and vanquished the beast pack! Harvested valuable beast cores and treasures.",
                stonesDelta = 1200L,
                jadeDelta = 10L,
                discipleHpDamagePercent = 20
            )
            "feed_spirit_herbs" -> EncounterResult(
                title = "Beast Pack Diverted!",
                message = "Lured by the fragrant spirit herbs, the beasts consumed the bait and peacefully wandered into the outer wilderness.",
                herbsDelta = -60L
            )

            // Ancient Abode
            "delve_deep" -> EncounterResult(
                title = "Immortal Cache Unearthed!",
                message = "Disciples bypassed ancient wards and recovered ancient pill flasks, refined jade, and primeval Dao Essence!",
                stonesDelta = -200L,
                jadeDelta = 20L,
                pillsDelta = 10L,
                essenceDelta = 25L
            )
            "harvest_outer_veins" -> EncounterResult(
                title = "Harvest Successful!",
                message = "Gathered 600 raw Spirit Stones and 40 rare herbs growing around the grotto mouth.",
                stonesDelta = 600L,
                herbsDelta = 40L
            )

            // Epiphany Rain
            "hold_meditation_ceremony" -> EncounterResult(
                title = "Mass Dao Epiphany!",
                message = "The spiritual rainfall bathed the disciples in cosmic energy, refining their meridians and deepening sect destiny.",
                stonesDelta = -150L,
                pillsDelta = 5L,
                essenceDelta = 40L
            )
            "collect_heavenly_dew" -> EncounterResult(
                title = "Heavenly Dew Collected!",
                message = "Captured 70 bottles of dew herbs and condensed 8 sparkling Spirit Jades.",
                herbsDelta = 70L,
                jadeDelta = 8L
            )

            // Seeker
            "recruit_disciple_gift" -> EncounterResult(
                title = "New Disciple Inducted!",
                message = "The wandering cultivator took the sect vow and has joined your roster as a dedicated immortal pupil!",
                stonesDelta = -300L,
                recruitNewDisciple = true
            )
            "exchange_dao_wisdom" -> EncounterResult(
                title = "Enlightening Dao Discourse",
                message = "The exchange of sutras inspired your elders (+20 Essence) and the guest offered 400 Spirit Stones in gratitude.",
                stonesDelta = 400L,
                essenceDelta = 20L
            )

            else -> EncounterResult(
                title = "Event Resolved",
                message = "The encounter has ended."
            )
        }
    }
}
