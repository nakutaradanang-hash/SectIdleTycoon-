package com.sect.idle.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;
import com.sect.idle.R;
import com.sect.idle.data.db.entities.DiscipleLifecycleEntity;

import java.util.Locale;

/**
 * LocalizationManager - High-Performance Multi-Language Translation System
 * Supports 8 major languages:
 * 1. English (en)
 * 2. Japanese (ja / 日本語)
 * 3. Korean (ko / 한국어)
 * 4. Chinese (zh / 简体中文)
 * 5. Indonesian (in/id / Bahasa Indonesia)
 * 6. Arabic (ar / العربية) - with RTL support
 * 7. Spanish (es / Español)
 * 8. Portuguese (pt / Português)
 *
 * Strict Java 7 & Sketchware Pro v7.0.0 Compatibility.
 * Target Android 5.0+ with seamless zero-overhead runtime language switching.
 */
public final class LocalizationManager {

    private static final String PREF_NAME = "sect_locale_prefs";
    private static final String KEY_SELECTED_LANG = "key_user_language";

    public static final String LANG_EN = "en";
    public static final String LANG_JA = "ja";
    public static final String LANG_KO = "ko";
    public static final String LANG_ZH = "zh";
    public static final String LANG_ID = "in"; // Java legacy code for Indonesian
    public static final String LANG_AR = "ar";
    public static final String LANG_ES = "es";
    public static final String LANG_PT = "pt";

    public static final String[] SUPPORTED_LANGUAGES = new String[] {
            LANG_EN, LANG_JA, LANG_KO, LANG_ZH, LANG_ID, LANG_AR, LANG_ES, LANG_PT
    };

    public interface LanguageChangeListener {
        void onLanguageChanged(String newLanguageCode);
    }

    private static volatile LocalizationManager sInstance;
    private final Context appContext;
    private final SharedPreferences prefs;
    private String currentLanguage;

    private LocalizationManager(Context context) {
        this.appContext = context.getApplicationContext() != null ? context.getApplicationContext() : context;
        this.prefs = this.appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.currentLanguage = loadSavedLanguage();
    }

    public static LocalizationManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LocalizationManager.class) {
                if (sInstance == null) {
                    sInstance = new LocalizationManager(context);
                }
            }
        }
        return sInstance;
    }

    private String loadSavedLanguage() {
        String saved = prefs.getString(KEY_SELECTED_LANG, null);
        if (saved != null && isSupported(saved)) {
            return saved;
        }

        // Auto-detect system language if supported
        Locale defaultLocale = Locale.getDefault();
        String sysLang = defaultLocale.getLanguage();
        if ("id".equalsIgnoreCase(sysLang) || "in".equalsIgnoreCase(sysLang)) {
            return LANG_ID;
        }
        if (isSupported(sysLang)) {
            return sysLang;
        }

        return LANG_EN;
    }

    public boolean isSupported(String langCode) {
        if (langCode == null) return false;
        String lower = langCode.toLowerCase(Locale.US);
        if ("id".equals(lower)) return true;
        for (int i = 0; i < SUPPORTED_LANGUAGES.length; i++) {
            if (SUPPORTED_LANGUAGES[i].equalsIgnoreCase(lower)) {
                return true;
            }
        }
        return false;
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }

    public void setLanguage(String langCode) {
        if (langCode == null) return;
        String normalized = langCode.toLowerCase(Locale.US);
        if ("id".equals(normalized)) normalized = LANG_ID;

        if (!isSupported(normalized)) {
            normalized = LANG_EN;
        }

        this.currentLanguage = normalized;
        prefs.edit().putString(KEY_SELECTED_LANG, normalized).apply();
        applyLocale(appContext);
    }

    public boolean isRtl() {
        return LANG_AR.equalsIgnoreCase(currentLanguage);
    }

    public Locale getLocale() {
        if (LANG_ID.equalsIgnoreCase(currentLanguage) || "id".equalsIgnoreCase(currentLanguage)) {
            return new Locale("id", "ID");
        }
        if (LANG_ZH.equalsIgnoreCase(currentLanguage)) {
            return Locale.SIMPLIFIED_CHINESE;
        }
        if (LANG_JA.equalsIgnoreCase(currentLanguage)) {
            return Locale.JAPANESE;
        }
        if (LANG_KO.equalsIgnoreCase(currentLanguage)) {
            return Locale.KOREAN;
        }
        if (LANG_AR.equalsIgnoreCase(currentLanguage)) {
            return new Locale("ar");
        }
        if (LANG_ES.equalsIgnoreCase(currentLanguage)) {
            return new Locale("es");
        }
        if (LANG_PT.equalsIgnoreCase(currentLanguage)) {
            return new Locale("pt", "BR");
        }
        return Locale.ENGLISH;
    }

    public Context applyLocale(Context context) {
        Locale targetLocale = getLocale();
        Locale.setDefault(targetLocale);

        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        DisplayMetrics dm = resources.getDisplayMetrics();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(targetLocale);
            config.setLayoutDirection(targetLocale);
        } else {
            config.locale = targetLocale;
        }

        resources.updateConfiguration(config, dm);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.createConfigurationContext(config);
        }
        return context;
    }

    public static String getLanguageNativeName(String langCode) {
        if (langCode == null) return "English";
        String lower = langCode.toLowerCase(Locale.US);
        if (LANG_JA.equals(lower)) return "日本語 (Japanese)";
        if (LANG_KO.equals(lower)) return "한국어 (Korean)";
        if (LANG_ZH.equals(lower)) return "简体中文 (Chinese)";
        if (LANG_ID.equals(lower) || "id".equals(lower)) return "Bahasa Indonesia";
        if (LANG_AR.equals(lower)) return "العربية (Arabic)";
        if (LANG_ES.equals(lower)) return "Español (Spanish)";
        if (LANG_PT.equals(lower)) return "Português (Portuguese)";
        return "English";
    }

    public void showLanguageDialog(final Activity activity, final LanguageChangeListener listener) {
        if (activity == null || activity.isFinishing()) return;

        final String[] items = new String[SUPPORTED_LANGUAGES.length];
        int checkedItem = 0;
        for (int i = 0; i < SUPPORTED_LANGUAGES.length; i++) {
            items[i] = getLanguageNativeName(SUPPORTED_LANGUAGES[i]);
            if (SUPPORTED_LANGUAGES[i].equalsIgnoreCase(currentLanguage)) {
                checkedItem = i;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.lang_select_title));
        builder.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which >= 0 && which < SUPPORTED_LANGUAGES.length) {
                    String selected = SUPPORTED_LANGUAGES[which];
                    setLanguage(selected);
                    applyLocale(activity);
                    dialog.dismiss();
                    if (listener != null) {
                        listener.onLanguageChanged(selected);
                    }
                }
            }
        });
        builder.setNegativeButton(activity.getString(R.string.btn_cancel), null);
        builder.show();
    }

    public String getLocalizedRealmName(int realm) {
        try {
            switch (realm) {
                case DiscipleLifecycleEntity.REALM_QI_CONDENSATION:
                    return appContext.getString(R.string.realm_qi_condensation);
                case DiscipleLifecycleEntity.REALM_FOUNDATION_ESTABLISHMENT:
                    return appContext.getString(R.string.realm_foundation_establishment);
                case DiscipleLifecycleEntity.REALM_CORE_FORMATION:
                    return appContext.getString(R.string.realm_core_formation);
                case DiscipleLifecycleEntity.REALM_NASCENT_SOUL:
                    return appContext.getString(R.string.realm_nascent_soul);
                case DiscipleLifecycleEntity.REALM_SOUL_FORMATION:
                    return appContext.getString(R.string.realm_soul_transformation);
                case DiscipleLifecycleEntity.REALM_VOID_REFINEMENT:
                    return appContext.getString(R.string.realm_void_refinement);
                case DiscipleLifecycleEntity.REALM_BODY_INTEGRATION:
                    return appContext.getString(R.string.realm_body_integration);
                case DiscipleLifecycleEntity.REALM_TRIBULATION_TRANSCENDENCE:
                    return appContext.getString(R.string.realm_tribulation_transcendence);
                case DiscipleLifecycleEntity.REALM_TRUE_IMMORTAL:
                    return appContext.getString(R.string.realm_true_immortal);
                default:
                    return appContext.getString(R.string.realm_mortal);
            }
        } catch (Exception e) {
            return "Realm " + realm;
        }
    }

    public String getLocalizedStateName(int state) {
        try {
            switch (state) {
                case DiscipleLifecycleEntity.STATE_IDLE:
                    return appContext.getString(R.string.state_idle);
                case DiscipleLifecycleEntity.STATE_RESTING:
                    return appContext.getString(R.string.state_resting);
                case DiscipleLifecycleEntity.STATE_EATING:
                    return appContext.getString(R.string.state_eating);
                case DiscipleLifecycleEntity.STATE_CULTIVATING:
                    return appContext.getString(R.string.state_cultivating);
                case DiscipleLifecycleEntity.STATE_SECT_DUTY:
                    return appContext.getString(R.string.state_sect_duty);
                case DiscipleLifecycleEntity.STATE_BREAKTHROUGH:
                    return appContext.getString(R.string.state_breakthrough);
                case DiscipleLifecycleEntity.STATE_TRIBULATION:
                    return appContext.getString(R.string.state_tribulation);
                case DiscipleLifecycleEntity.STATE_QI_DEVIATION:
                    return appContext.getString(R.string.state_qi_deviation);
                case DiscipleLifecycleEntity.STATE_INJURED:
                    return appContext.getString(R.string.state_injured);
                case DiscipleLifecycleEntity.STATE_ASCENDED:
                    return appContext.getString(R.string.state_ascended);
                case DiscipleLifecycleEntity.STATE_DECEASED:
                    return appContext.getString(R.string.state_deceased);
                default:
                    return appContext.getString(R.string.state_idle);
            }
        } catch (Exception e) {
            return "State " + state;
        }
    }

    public String getLocalizedRankName(int stage) {
        try {
            switch (stage) {
                case DiscipleLifecycleEntity.STAGE_MORTAL_RECRUIT:
                    return appContext.getString(R.string.rank_mortal_aspirant);
                case DiscipleLifecycleEntity.STAGE_OUTER_DISCIPLE:
                    return appContext.getString(R.string.rank_outer_disciple);
                case DiscipleLifecycleEntity.STAGE_INNER_DISCIPLE:
                    return appContext.getString(R.string.rank_inner_disciple);
                case DiscipleLifecycleEntity.STAGE_CORE_DISCIPLE:
                    return appContext.getString(R.string.rank_core_disciple);
                case DiscipleLifecycleEntity.STAGE_ELDER:
                    return appContext.getString(R.string.rank_hall_elder);
                case DiscipleLifecycleEntity.STAGE_GRAND_ELDER:
                    return appContext.getString(R.string.rank_grand_elder);
                case DiscipleLifecycleEntity.STAGE_SECT_MASTER:
                    return appContext.getString(R.string.rank_sect_master);
                case DiscipleLifecycleEntity.STAGE_ANCESTOR:
                    return appContext.getString(R.string.rank_supreme_ancestor);
                default:
                    return appContext.getString(R.string.rank_outer_disciple);
            }
        } catch (Exception e) {
            return "Rank " + stage;
        }
    }

    public String getLocalizedElementalName(int element) {
        try {
            switch (element) {
                case DiscipleLifecycleEntity.ELEMENT_METAL:
                    return appContext.getString(R.string.element_metal);
                case DiscipleLifecycleEntity.ELEMENT_WOOD:
                    return appContext.getString(R.string.element_wood);
                case DiscipleLifecycleEntity.ELEMENT_WATER:
                    return appContext.getString(R.string.element_water);
                case DiscipleLifecycleEntity.ELEMENT_FIRE:
                    return appContext.getString(R.string.element_fire);
                case DiscipleLifecycleEntity.ELEMENT_EARTH:
                    return appContext.getString(R.string.element_earth);
                case DiscipleLifecycleEntity.ELEMENT_WIND:
                    return appContext.getString(R.string.element_wind);
                case DiscipleLifecycleEntity.ELEMENT_LIGHTNING:
                    return appContext.getString(R.string.element_lightning);
                default:
                    return appContext.getString(R.string.element_metal);
            }
        } catch (Exception e) {
            return "Element " + element;
        }
    }
}
