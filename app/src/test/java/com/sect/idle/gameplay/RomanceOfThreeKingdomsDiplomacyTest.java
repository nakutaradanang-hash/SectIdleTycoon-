package com.sect.idle.gameplay;

import com.sect.idle.models.Disciple;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit Tests for Romance of the Three Kingdoms Style Faction Diplomacy & Officer Court Systems.
 */
public class RomanceOfThreeKingdomsDiplomacyTest {

    private FactionDiplomacySystem diplomacy;
    private OfficerCouncilSystem council;
    private SectData sectData;

    @Before
    public void setUp() {
        diplomacy = FactionDiplomacySystem.getInstance();
        council = OfficerCouncilSystem.getInstance();
        sectData = SectData.getInstance();
        sectData.reset();
        sectData.spiritStones = 20000L;
    }

    @Test
    public void testFactionInitializationAndRelations() {
        assertNotNull(diplomacy.factions);
        assertEquals(6, diplomacy.factions.size());

        FactionDiplomacySystem.RivalFaction wudang = diplomacy.getFaction("FAC_WUDANG");
        assertNotNull(wudang);
        assertEquals("Wudang Mountain Sect", wudang.name);
        assertTrue(wudang.militaryPower > 0);
    }

    @Test
    public void testSendTributeStratagem() {
        FactionDiplomacySystem.RivalFaction wudang = diplomacy.getFaction("FAC_WUDANG");
        int initialFavor = wudang.favorScore;

        FactionDiplomacySystem.DiplomaticActionResult result =
                diplomacy.executeStratagem(sectData, "FAC_WUDANG", FactionDiplomacySystem.STRATAGEM_SEND_TRIBUTE, 1000);

        assertTrue("Tribute execution must succeed", result.success);
        assertTrue("Favor must increase after tribute", wudang.favorScore > initialFavor);
    }

    @Test
    public void testNonAggressionPactStratagem() {
        FactionDiplomacySystem.RivalFaction kunlun = diplomacy.getFaction("FAC_KUNLUN");
        kunlun.favorScore = 50;

        FactionDiplomacySystem.DiplomaticActionResult result =
                diplomacy.executeStratagem(sectData, "FAC_KUNLUN", FactionDiplomacySystem.STRATAGEM_NON_AGGRESSION, 500);

        assertTrue("Non aggression pact should succeed with positive favor", result.success);
        assertTrue(kunlun.nonAggressionPact);
        assertEquals(10, kunlun.pactRemainingTurns);
    }

    @Test
    public void testOfficerAppointmentsAndAptitude() {
        Disciple d = new Disciple("Zhuge Kongming", 1);
        d.wis = 95;
        d.intel = 90;
        d.str = 30;
        d.loyalty = 80;

        boolean appointed = council.appointOfficer(OfficerCouncilSystem.OFFICE_GRAND_ELDER, d);
        assertTrue("Appointing Prime Minister must succeed", appointed);

        OfficerCouncilSystem.OfficerAppointment app = council.getAppointment(OfficerCouncilSystem.OFFICE_GRAND_ELDER);
        assertNotNull(app);
        assertEquals("Zhuge Kongming", app.appointedDiscipleName);
        assertTrue("Grand Elder aptitude must be high for high WIS/INT", app.aptitudeScore > 200);
        assertTrue("Loyalty should increase after appointment", d.loyalty >= 95);
    }

    @Test
    public void testDefectionWarningOnLowLoyalty() {
        Disciple traitor = new Disciple("Wei Yan", 1);
        traitor.loyalty = 5;
        traitor.mood = 10;
        sectData.disciples.clear();
        sectData.disciples.add(traitor);

        String warning = council.tickOfficerLoyalty(sectData);
        // Traitor loyalty may drop further
        assertTrue(traitor.loyalty <= 5);
    }
}
