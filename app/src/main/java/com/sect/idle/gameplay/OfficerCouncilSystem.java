package com.sect.idle.gameplay;

import com.sect.idle.models.Disciple;
import com.sect.idle.systems.RNG;
import com.sect.idle.utils.ErrorCode;
import com.sect.idle.utils.ExceptionManager;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * OfficerCouncilSystem - Romance of the Three Kingdoms Style Court Governance & Ministry.
 * Allows assigning prominent disciples to ministerial titles (Grand Elder, General Marshal,
 * Chief Diplomat, Master of Formations, Head of Alchemy), providing empire-wide bonuses,
 * and tracking Disciple Loyalty, Ambition, and Defection risks.
 *
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class OfficerCouncilSystem {

    private static volatile OfficerCouncilSystem instance;
    private static final Object LOCK = new Object();

    // Ministerial Office Titles
    public static final int OFFICE_NONE = 0;
    public static final int OFFICE_GRAND_ELDER = 1;       // Boosts Sect EXP & Cultivation Speed (+25%)
    public static final int OFFICE_GENERAL_MARSHAL = 2;   // Boosts Disciple ATK & Defense in Battle (+20%)
    public static final int OFFICE_CHIEF_DIPLOMAT = 3;    // Boosts Diplomacy Favor Gain & Trade Revenue (+30%)
    public static final int OFFICE_HEAD_ALCHEMY = 4;      // Boosts Alchemy Crafting Yield & Herb Efficiency (+35%)
    public static final int OFFICE_MASTER_FORMATIONS = 5; // Boosts Sect Defensive Barrier & Mining Yield (+25%)
    public static final int OFFICE_INSPECTOR_GENERAL = 6; // Prevents Disciple Corruption & Raises Loyalty (+10)

    public static class OfficerAppointment {
        public int officeId;
        public String officeTitle;
        public String appointedDiscipleId;
        public String appointedDiscipleName;
        public int aptitudeScore; // Based on disciple stats (INT, STR, WIS, CHA)

        public OfficerAppointment(int officeId, String officeTitle) {
            this.officeId = officeId;
            this.officeTitle = officeTitle;
            this.appointedDiscipleId = null;
            this.appointedDiscipleName = "Unassigned";
            this.aptitudeScore = 0;
        }
    }

    public final ArrayList<OfficerAppointment> offices;
    private final HashMap<Integer, OfficerAppointment> officeMap;

    public static OfficerCouncilSystem getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new OfficerCouncilSystem();
                }
            }
        }
        return instance;
    }

    private OfficerCouncilSystem() {
        this.offices = new ArrayList<OfficerAppointment>(6);
        this.officeMap = new HashMap<Integer, OfficerAppointment>(8);
        initOffices();
    }

    private void initOffices() {
        offices.clear();
        officeMap.clear();

        registerOffice(new OfficerAppointment(OFFICE_GRAND_ELDER, "Grand Elder (Prime Minister)"));
        registerOffice(new OfficerAppointment(OFFICE_GENERAL_MARSHAL, "Grand Marshal (Chief of War)"));
        registerOffice(new OfficerAppointment(OFFICE_CHIEF_DIPLOMAT, "Chief Diplomat (Foreign Ministry)"));
        registerOffice(new OfficerAppointment(OFFICE_HEAD_ALCHEMY, "Head of Alchemy (Pill Pavilion)"));
        registerOffice(new OfficerAppointment(OFFICE_MASTER_FORMATIONS, "Master of Formations (Wards)"));
        registerOffice(new OfficerAppointment(OFFICE_INSPECTOR_GENERAL, "Inspector General (Disciplinary)"));
    }

    private void registerOffice(OfficerAppointment app) {
        offices.add(app);
        officeMap.put(app.officeId, app);
    }

    /**
     * Appoints a disciple to a ministerial post, recalculating aptitude and boosting loyalty.
     */
    public boolean appointOfficer(int officeId, Disciple disciple) {
        OfficerAppointment appointment = officeMap.get(officeId);
        if (appointment == null || disciple == null) {
            return false;
        }

        // Calculate aptitude based on matching stats
        int score = 0;
        switch (officeId) {
            case OFFICE_GRAND_ELDER:
                score = (disciple.wis * 2) + disciple.intel + disciple.realm * 10;
                break;
            case OFFICE_GENERAL_MARSHAL:
                score = (disciple.str * 2) + disciple.vit + disciple.atk;
                break;
            case OFFICE_CHIEF_DIPLOMAT:
                score = (disciple.cha * 2) + disciple.intel + disciple.wis;
                break;
            case OFFICE_HEAD_ALCHEMY:
                score = (disciple.intel * 2) + disciple.wis + disciple.alchemySkill * 10;
                break;
            case OFFICE_MASTER_FORMATIONS:
                score = (disciple.wis * 2) + disciple.intel + disciple.def;
                break;
            case OFFICE_INSPECTOR_GENERAL:
                score = (disciple.str + disciple.intel + disciple.wis) + disciple.loyalty;
                break;
            default:
                score = disciple.realm * 10;
                break;
        }

        appointment.appointedDiscipleId = disciple.id;
        appointment.appointedDiscipleName = disciple.name;
        appointment.aptitudeScore = Math.max(10, score);

        // Appointing to office increases loyalty (+15)
        disciple.loyalty = Math.min(100, disciple.loyalty + 15);
        disciple.title = appointment.officeTitle;

        ExceptionManager.get().logUserAction("OFFICER_APPOINTMENT", disciple.name, "Assigned to " + appointment.officeTitle);
        return true;
    }

    /**
     * Ticks officer loyalty and checks for defection/rebellion under low morale (< 20).
     */
    public String tickOfficerLoyalty(SectData sect) {
        if (sect == null || sect.disciples == null) return null;

        StringBuilder report = new StringBuilder();
        for (int i = 0; i < sect.disciples.size(); i++) {
            Disciple d = sect.disciples.get(i);
            if (d != null) {
                // If paid wage or mood high -> maintain loyalty
                if (d.mood > 70) {
                    d.loyalty = Math.min(100, d.loyalty + 1);
                } else if (d.mood < 30) {
                    d.loyalty = Math.max(0, d.loyalty - 2);
                }

                // Low loyalty warning or defection risk
                if (d.loyalty < 15 && RNG.nextInt(100) < 10) {
                    report.append("⚠️ [DEFECTION WARNING] Disciple ").append(d.name)
                          .append(" has critically low loyalty (").append(d.loyalty).append("%)! Consider granting a reward or promotion.\n");
                }
            }
        }
        return report.length() > 0 ? report.toString() : null;
    }

    public OfficerAppointment getAppointment(int officeId) {
        return officeMap.get(officeId);
    }
}
