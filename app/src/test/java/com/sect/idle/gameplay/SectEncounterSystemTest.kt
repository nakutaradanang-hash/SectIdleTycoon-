package com.sect.idle.gameplay

import org.junit.Assert
import org.junit.Test

class SectEncounterSystemTest {

    @Test
    fun testGenerateRandomEncounter() {
        val encounter = SectEncounterSystem.generateRandomEncounter(sectRealm = 1)

        Assert.assertNotNull(encounter)
        Assert.assertNotNull(encounter.id)
        Assert.assertNotNull(encounter.title)
        Assert.assertNotNull(encounter.chineseTitle)
        Assert.assertNotNull(encounter.description)
        Assert.assertTrue(encounter.choices.isNotEmpty())
    }

    @Test
    fun testMerchantChoiceResolution() {
        val encounter = SectEncounter(
            id = "test_enc",
            title = "Traveling Merchant",
            chineseTitle = "云游行商",
            description = "Merchant arrives",
            iconEmoji = "🧙‍♂️",
            eventType = EncounterType.TRAVELING_MERCHANT,
            choices = emptyList()
        )

        val buyPillsResult = SectEncounterSystem.resolveChoice(encounter, "buy_rare_pills")
        Assert.assertEquals(-500L, buyPillsResult.stonesDelta)
        Assert.assertEquals(8L, buyPillsResult.pillsDelta)
        Assert.assertEquals(10L, buyPillsResult.essenceDelta)

        val barterHerbsResult = SectEncounterSystem.resolveChoice(encounter, "trade_herbs_for_jade")
        Assert.assertEquals(-80L, barterHerbsResult.herbsDelta)
        Assert.assertEquals(15L, barterHerbsResult.jadeDelta)
    }

    @Test
    fun testDemonicInvasionChoiceResolution() {
        val encounter = SectEncounter(
            id = "test_enc_inv",
            title = "Demonic Surge",
            chineseTitle = "万兽袭山",
            description = "Beasts attack",
            iconEmoji = "🐺",
            eventType = EncounterType.DEMONIC_INVASION,
            choices = emptyList()
        )

        val defendArrayResult = SectEncounterSystem.resolveChoice(encounter, "activate_guardian_array")
        Assert.assertEquals(-400L, defendArrayResult.stonesDelta)
        Assert.assertEquals(-30L, defendArrayResult.oresDelta)
        Assert.assertEquals(0, defendArrayResult.discipleHpDamagePercent)

        val dispatchResult = SectEncounterSystem.resolveChoice(encounter, "dispatch_disciples")
        Assert.assertEquals(1200L, dispatchResult.stonesDelta)
        Assert.assertEquals(10L, dispatchResult.jadeDelta)
        Assert.assertTrue(dispatchResult.discipleHpDamagePercent > 0)
    }

    @Test
    fun testWanderingSeekerRecruitmentResolution() {
        val encounter = SectEncounter(
            id = "test_enc_seeker",
            title = "Wandering Cultivator",
            chineseTitle = "散修登门",
            description = "Seeker seeks sect",
            iconEmoji = "🥋",
            eventType = EncounterType.WANDERING_CULTIVATOR_SEEKER,
            choices = emptyList()
        )

        val recruitResult = SectEncounterSystem.resolveChoice(encounter, "recruit_disciple_gift")
        Assert.assertEquals(-300L, recruitResult.stonesDelta)
        Assert.assertTrue(recruitResult.recruitNewDisciple)
    }
}
