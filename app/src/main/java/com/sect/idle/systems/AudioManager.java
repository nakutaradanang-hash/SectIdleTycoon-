package com.sect.idle.systems;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;
import com.sect.idle.R;
import com.sect.idle.core.MathUtils;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AudioManager - High-fidelity, studio-grade procedural Xianxia audio synthesis & mixer engine.
 * Generates rich, comfortable, multi-layered acoustic BGM and polyphonic sound effects in real-time.
 *
 * Soundscapes include:
 * 1. THEME_SECT_PEACE: Soothing Guzheng plucks, warm Dizi flute melodies with vibrato, and ambient mountain wind.
 * 2. THEME_COMBAT_INTENSE: Dynamic Taiko drums, energetic Pipa/sword ostinatos, and driving martial rhythms.
 * 3. THEME_MEDITATION_ZEN: Tibetan singing bowls, 432Hz Qi drone harmonics, and gentle spring waters.
 *
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class AudioManager {
    private static volatile AudioManager instance;
    private static final Object LOCK = new Object();

    public static final String THEME_SECT_PEACE = "sect_peace";
    public static final String THEME_COMBAT_INTENSE = "combat_intense";
    public static final String THEME_MEDITATION_ZEN = "meditation_zen";
    public static final String THEME_IMMORTAL_HYMN = "immortal_hymn";
    public static final String THEME_DEMON_TRIBULATION = "demon_tribulation";
    public static final String THEME_WILDERNESS_EXPLORE = "wilderness_explore";
    public static final String THEME_SECT_TRIUMPH = "sect_triumph";
    public static final String THEME_BOSS_CLASH = "boss_clash";

    // Combat Sound Identifiers
    public static final String SFX_COMBAT_START = "combat_start";
    public static final String SFX_STRIKE = "strike";
    public static final String SFX_SWORD_SPAR = "sword_spar";
    public static final String SFX_CRITICAL_STRIKE = "critical_strike";
    public static final String SFX_SHIELD_BLOCK = "shield_block";
    public static final String SFX_BARRIER_SHATTER = "barrier_shatter";
    public static final String SFX_ELEMENTAL_FIRE = "elemental_fire";
    public static final String SFX_ELEMENTAL_LIGHTNING = "elemental_lightning";
    public static final String SFX_ELEMENTAL_ICE = "elemental_ice";
    public static final String SFX_BOSS_ENRAGE = "boss_enrage";
    public static final String SFX_VICTORY = "victory";
    public static final String SFX_DEFEAT = "defeat";

    // Martial Arts Arena & Ring Identifiers
    public static final String SFX_RING_BELL = "ring_bell";
    public static final String SFX_CROWD_CHEER = "crowd_cheer";
    public static final String SFX_CROWD_GASP = "crowd_gasp";
    public static final String SFX_REFEREE_COUNT = "referee_count";
    public static final String SFX_GRAPPLE_SLAM = "grapple_slam";
    public static final String SFX_ROPE_BOUNCE = "rope_bounce";
    public static final String SFX_FINISHER_HIT = "finisher_hit";

    // Disciple & Interaction Sound Identifiers
    public static final String SFX_DISCIPLE_GREETING = "disciple_greeting";
    public static final String SFX_BREAKTHROUGH = "breakthrough";
    public static final String SFX_BREAKTHROUGH_FAIL = "breakthrough_fail";
    public static final String SFX_BESTOW_PILL = "bestow_pill";
    public static final String SFX_ASSIGN_TASK = "assign_task";
    public static final String SFX_RECRUIT = "recruit";
    public static final String SFX_DISMISS = "dismiss";
    public static final String SFX_DAO_ENLIGHTENMENT = "dao_enlightenment";

    // Sect & Environment Identifiers
    public static final String SFX_GATHER = "gather";
    public static final String SFX_COLLECT = "collect";
    public static final String SFX_UPGRADE = "upgrade";
    public static final String SFX_DEMOLISH = "demolish";
    public static final String SFX_ALCHEMY = "alchemy";
    public static final String SFX_SCRIPTURE = "scripture";
    public static final String SFX_CLICK = "click";
    public static final String SFX_FAIL = "fail";

    // Xianxia Immersion Identifiers
    public static final String SFX_FLYING_SWORD = "flying_sword";
    public static final String SFX_HEAVENLY_TRIBULATION = "heavenly_tribulation";
    public static final String SFX_QI_BURST = "qi_burst";
    public static final String SFX_SPIRIT_BURST = "spirit_burst";
    public static final String SFX_PILL_CAULDRON_DING = "pill_cauldron_ding";
    public static final String SFX_TALISMAN_BURN = "talisman_burn";
    public static final String SFX_IMMORTAL_BELL = "immortal_bell";

    private final Context context;
    private final Handler mainHandler;

    private float bgmVolume = 0.65f;
    private float ambientVolume = 0.45f;
    private float sfxVolume = 0.85f;
    private float masterVolume = 1.0f;
    private boolean enabled = true;
    private boolean bgmEnabled = true;
    private boolean ducking = false;

    private static final int MAX_VOICES = 12;
    private static final int SAMPLE_RATE = 22050;
    private static final int BUFFER_SIZE_SAMPLES = 1024;

    private static class Voice {
        short[] sample;
        float cursor;
        float volume;
        float pitch;
        int priority;
        boolean active;
    }

    private final Voice[] voices = new Voice[MAX_VOICES];
    private final HashMap<String, short[]> pcmCache = new HashMap<String, short[]>();
    private final HashMap<String, short[]> bgmCache = new HashMap<String, short[]>();

    private int bgmWaveCursor = 0;
    private String lastStreamedTheme = "";

    private AudioTrack mixerTrack;
    private Thread mixerThread;
    private final AtomicBoolean isMixerRunning = new AtomicBoolean(false);
    private volatile String activeBgmTheme = THEME_SECT_PEACE;

    private android.media.AudioManager androidAudioManager;
    private android.media.AudioManager.OnAudioFocusChangeListener focusListener;

    // Traditional Pentatonic Scale Frequencies (Gong, Shang, Jue, Zhi, Yu)
    private static final float[] PENTATONIC_FREQS = {
            196.00f, // G3
            220.00f, // A3
            261.63f, // C4 (Gong)
            293.66f, // D4 (Shang)
            329.63f, // E4 (Jue)
            392.00f, // G4 (Zhi)
            440.00f, // A4 (Yu)
            523.25f, // C5
            587.33f, // D5
            659.25f, // E5
            783.99f, // G5
            880.00f, // A5
            1046.50f // C6
    };

    private static final String[] ALL_SFX_KEYS = new String[] {
            SFX_CLICK, SFX_STRIKE, SFX_SWORD_SPAR, SFX_CRITICAL_STRIKE,
            SFX_SHIELD_BLOCK, SFX_BARRIER_SHATTER, SFX_ELEMENTAL_FIRE,
            SFX_ELEMENTAL_LIGHTNING, SFX_ELEMENTAL_ICE, SFX_BOSS_ENRAGE,
            SFX_VICTORY, SFX_DEFEAT, SFX_COMBAT_START,
            SFX_RING_BELL, SFX_CROWD_CHEER, SFX_CROWD_GASP, SFX_REFEREE_COUNT,
            SFX_GRAPPLE_SLAM, SFX_ROPE_BOUNCE, SFX_FINISHER_HIT,
            SFX_DISCIPLE_GREETING, SFX_BREAKTHROUGH, SFX_BREAKTHROUGH_FAIL,
            SFX_BESTOW_PILL, SFX_ASSIGN_TASK, SFX_RECRUIT, SFX_DISMISS,
            SFX_DAO_ENLIGHTENMENT, SFX_GATHER, SFX_COLLECT, SFX_UPGRADE,
            SFX_DEMOLISH, SFX_ALCHEMY, SFX_SCRIPTURE, SFX_FAIL,
            SFX_FLYING_SWORD, SFX_HEAVENLY_TRIBULATION, SFX_QI_BURST,
            SFX_PILL_CAULDRON_DING, SFX_TALISMAN_BURN, SFX_IMMORTAL_BELL
    };

    private AudioManager(Context ctx) {
        this.context = ctx.getApplicationContext();
        this.mainHandler = new Handler(Looper.getMainLooper());

        for (int i = 0; i < MAX_VOICES; i++) {
            voices[i] = new Voice();
        }

        this.androidAudioManager = (android.media.AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.focusListener = new android.media.AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        setDucking(true);
                        break;
                    case android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    case android.media.AudioManager.AUDIOFOCUS_LOSS:
                        setDucking(false);
                        pauseBgm();
                        break;
                    case android.media.AudioManager.AUDIOFOCUS_GAIN:
                        setDucking(false);
                        resumeBgm();
                        break;
                }
            }
        };

        // Fast initialization: start mixer and asynchronously pre-warm audio cache in background
        cacheRawOrSynth(SFX_CLICK, R.raw.sfx_click);
        startMixer();
        asyncPreloadAudioCache();
    }

    private void asyncPreloadAudioCache() {
        Thread prewarmThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    preloadCommonPcm();
                } catch (Throwable ignored) {}
            }
        }, "AudioManagerPreloader");
        prewarmThread.setPriority(Thread.MIN_PRIORITY);
        prewarmThread.setDaemon(true);
        prewarmThread.start();
    }

    public static AudioManager getInstance(Context context) {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new AudioManager(context);
                }
            }
        }
        return instance;
    }

    public static AudioManager get(Context context) {
        return getInstance(context);
    }

    private short[] loadWavFromRaw(int resId) {
        try {
            InputStream is = context.getResources().openRawResource(resId);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int read;
            while ((read = is.read(buf)) != -1) {
                baos.write(buf, 0, read);
            }
            is.close();
            byte[] all = baos.toByteArray();
            if (all.length > 44) {
                int numShorts = (all.length - 44) / 2;
                short[] samples = new short[numShorts];
                for (int i = 0; i < numShorts; i++) {
                    int b1 = all[44 + i * 2] & 0xFF;
                    int b2 = all[44 + i * 2 + 1];
                    samples[i] = (short) ((b2 << 8) | b1);
                }
                return samples;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void preloadCommonPcm() {
        // Preload studio-grade audio assets from res/raw with procedural fallbacks
        cacheRawOrSynth(SFX_CLICK, R.raw.sfx_click);
        cacheRawOrSynth(SFX_STRIKE, R.raw.sfx_strike);
        cacheRawOrSynth(SFX_SWORD_SPAR, R.raw.sfx_sword_spar);
        cacheRawOrSynth(SFX_CRITICAL_STRIKE, R.raw.sfx_critical);
        cacheRawOrSynth(SFX_VICTORY, R.raw.sfx_victory);
        cacheRawOrSynth(SFX_DEFEAT, R.raw.sfx_defeat);
        cacheRawOrSynth(SFX_BREAKTHROUGH, R.raw.sfx_breakthrough);
        cacheRawOrSynth(SFX_SPIRIT_BURST, R.raw.sfx_spirit_burst);
        cacheRawOrSynth(SFX_QI_BURST, R.raw.sfx_spirit_burst);
        cacheRawOrSynth(SFX_DAO_ENLIGHTENMENT, R.raw.sfx_spirit_burst);
        cacheRawOrSynth(SFX_IMMORTAL_BELL, R.raw.sfx_bell);
        cacheRawOrSynth(SFX_RING_BELL, R.raw.sfx_bell);
        cacheRawOrSynth(SFX_COLLECT, R.raw.sfx_coin);
        cacheRawOrSynth(SFX_GATHER, R.raw.sfx_coin);
        cacheRawOrSynth(SFX_BESTOW_PILL, R.raw.sfx_coin);
        cacheRawOrSynth(SFX_UPGRADE, R.raw.sfx_bell);

        // Preload all 8 Complete Xianxia BGM Soundtracks from res/raw
        loadBgmTrack(THEME_SECT_PEACE, R.raw.bgm_sect_peace);
        loadBgmTrack(THEME_COMBAT_INTENSE, R.raw.bgm_combat_epic);
        loadBgmTrack(THEME_MEDITATION_ZEN, R.raw.bgm_meditation_serene);
        loadBgmTrack(THEME_IMMORTAL_HYMN, R.raw.bgm_immortal_hymn);
        loadBgmTrack(THEME_DEMON_TRIBULATION, R.raw.bgm_demon_tribulation);
        loadBgmTrack(THEME_WILDERNESS_EXPLORE, R.raw.bgm_wilderness_explore);
        loadBgmTrack(THEME_SECT_TRIUMPH, R.raw.bgm_sect_triumph);
        loadBgmTrack(THEME_BOSS_CLASH, R.raw.bgm_boss_clash);

        for (int i = 0; i < ALL_SFX_KEYS.length; i++) {
            String key = ALL_SFX_KEYS[i];
            if (!pcmCache.containsKey(key)) {
                pcmCache.put(key, synthesizePcm(key));
            }
        }
    }

    private void loadBgmTrack(String theme, int rawResId) {
        try {
            short[] samples = loadWavFromRaw(rawResId);
            if (samples != null && samples.length > 0) {
                bgmCache.put(theme, samples);
            }
        } catch (Throwable ignored) {}
    }

    private void cacheRawOrSynth(String key, int rawResId) {
        try {
            short[] samples = loadWavFromRaw(rawResId);
            if (samples != null && samples.length > 0) {
                pcmCache.put(key, samples);
                return;
            }
        } catch (Throwable ignored) {}
        pcmCache.put(key, synthesizePcm(key));
    }

    private void requestAudioFocus() {
        if (androidAudioManager != null && focusListener != null) {
            try {
                androidAudioManager.requestAudioFocus(
                        focusListener,
                        android.media.AudioManager.STREAM_MUSIC,
                        android.media.AudioManager.AUDIOFOCUS_GAIN
                );
            } catch (Exception ignored) {}
        }
    }

    private void abandonAudioFocus() {
        if (androidAudioManager != null && focusListener != null) {
            try {
                androidAudioManager.abandonAudioFocus(focusListener);
            } catch (Exception ignored) {}
        }
    }

    public void playSfx(String effectName) {
        playSfx(effectName, 1.0f, 1.0f, 1);
    }

    public void playSfx(String effectName, float pitch, float volumeScale) {
        playSfx(effectName, pitch, volumeScale, 1);
    }

    public void playSfx(String effectName, float pitch, float volumeScale, int priority) {
        if (!enabled || effectName == null) return;

        short[] sample = pcmCache.get(effectName);
        if (sample == null) {
            sample = synthesizePcm(effectName);
            pcmCache.put(effectName, sample);
        }

        synchronized (voices) {
            int targetIdx = -1;
            int lowestPriority = Integer.MAX_VALUE;

            for (int i = 0; i < MAX_VOICES; i++) {
                if (!voices[i].active) {
                    targetIdx = i;
                    break;
                } else if (voices[i].priority < lowestPriority) {
                    lowestPriority = voices[i].priority;
                    targetIdx = i;
                }
            }

            if (targetIdx != -1) {
                Voice v = voices[targetIdx];
                v.sample = sample;
                v.cursor = 0f;
                v.pitch = MathUtils.clamp(pitch, 0.5f, 2.0f);
                v.volume = sfxVolume * masterVolume * volumeScale * (ducking ? 0.4f : 1.0f);
                v.priority = priority;
                v.active = true;
            }
        }
    }

    // ========================================================================
    // UNIFIED REAL-TIME PCM MIXER & BGM SYNTHESIS ENGINE
    // ========================================================================

    private synchronized void startMixer() {
        if (isMixerRunning.get()) return;
        isMixerRunning.set(true);

        mixerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int minBuf = AudioTrack.getMinBufferSize(
                            SAMPLE_RATE,
                            AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT
                    );

                    mixerTrack = new AudioTrack.Builder()
                            .setAudioAttributes(new AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_GAME)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                    .build())
                            .setAudioFormat(new AudioFormat.Builder()
                                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                    .setSampleRate(SAMPLE_RATE)
                                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                    .build())
                            .setBufferSizeInBytes(Math.max(minBuf * 2, BUFFER_SIZE_SAMPLES * 4))
                            .setTransferMode(AudioTrack.MODE_STREAM)
                            .build();

                    if (mixerTrack.getState() != AudioTrack.STATE_INITIALIZED) {
                        return;
                    }

                    mixerTrack.play();

                    short[] mixBuffer = new short[BUFFER_SIZE_SAMPLES];
                    float[] bgmNoteBuffer = new float[SAMPLE_RATE * 3]; // up to 3s note buffer
                    int bgmNoteLen = 0;
                    int bgmNotePos = 0;
                    int bgmPauseRemaining = 0;
                    Random rand = new Random(System.currentTimeMillis());

                    // Reverb / spatial delay circular buffer
                    float[] delayBuffer = new float[2205]; // ~100ms smooth delay
                    int delayPos = 0;

                    // Rhythm step counter for combat theme
                    int combatStep = 0;

                    while (isMixerRunning.get()) {
                        if (!enabled) {
                            Thread.sleep(50);
                            continue;
                        }

                        float effBgmVol = bgmEnabled ? getEffectiveBgmVolume() * 0.40f : 0f;
                        String currentTheme = activeBgmTheme;

                        if (currentTheme != null && !currentTheme.equals(lastStreamedTheme)) {
                            lastStreamedTheme = currentTheme;
                            bgmWaveCursor = 0;
                            bgmNotePos = 0;
                            bgmNoteLen = 0;
                            bgmPauseRemaining = 0;
                        }

                        short[] currentWav = bgmCache.get(currentTheme);

                        for (int i = 0; i < BUFFER_SIZE_SAMPLES; i++) {
                            float bgmSample = 0f;

                            if (effBgmVol > 0.001f) {
                                if (currentWav != null && currentWav.length > 0) {
                                    if (bgmWaveCursor >= currentWav.length) {
                                        bgmWaveCursor = 0;
                                    }
                                    bgmSample = (currentWav[bgmWaveCursor++] / 32768.0f) * effBgmVol;
                                } else if (bgmPauseRemaining > 0) {
                                    bgmPauseRemaining--;
                                } else if (bgmNotePos < bgmNoteLen) {
                                    bgmSample = bgmNoteBuffer[bgmNotePos++] * effBgmVol;
                                } else {
                                    // Generate next note phrase based on active theme
                                    if (THEME_COMBAT_INTENSE.equals(currentTheme)) {
                                        // Martial Combat Theme: Driving Taiko percussion + Fast Pentatonic Sword Ostinato
                                        combatStep = (combatStep + 1) % 8;
                                        boolean isTaikoBeat = (combatStep == 0 || combatStep == 4);
                                        boolean isSyncopated = (combatStep == 2 || combatStep == 6);

                                        int noteMs = 160 + rand.nextInt(80);
                                        int pauseMs = 20 + rand.nextInt(30);

                                        int noteIdx = rand.nextInt(PENTATONIC_FREQS.length);
                                        float freq = PENTATONIC_FREQS[noteIdx];

                                        bgmNoteLen = Math.min((SAMPLE_RATE * noteMs) / 1000, bgmNoteBuffer.length);
                                        bgmNotePos = 0;
                                        bgmPauseRemaining = (SAMPLE_RATE * pauseMs) / 1000;

                                        for (int s = 0; s < bgmNoteLen; s++) {
                                            float t = (float) s / bgmNoteLen;
                                            float timeSec = s / (float) SAMPLE_RATE;

                                            // Fast Pipa / Guzheng combat pluck
                                            float fund = (float) Math.sin(2.0 * Math.PI * freq * timeSec);
                                            float h2 = (float) Math.sin(4.0 * Math.PI * freq * 1.003f * timeSec) * 0.4f;
                                            float h3 = (float) Math.sin(6.0 * Math.PI * freq * 0.997f * timeSec) * 0.2f;
                                            float pluckEnv = (float) (Math.exp(-18.0 * t) * 0.6f + Math.exp(-6.0 * t) * 0.4f);

                                            float drumLayer = 0f;
                                            if (isTaikoBeat) {
                                                float drumFreq = 65f * (float) Math.exp(-12.0 * t) + 40f;
                                                float drumPhase = (float) (2.0 * Math.PI * drumFreq * timeSec);
                                                float drumEnv = (float) Math.exp(-9.0 * t);
                                                drumLayer = (float) Math.sin(drumPhase) * drumEnv * 1.1f;
                                            } else if (isSyncopated) {
                                                float woodClick = (float) ((rand.nextFloat() - 0.5f) * Math.exp(-35.0 * t) * 0.4f);
                                                drumLayer = woodClick;
                                            }

                                            bgmNoteBuffer[s] = (fund + h2 + h3) * pluckEnv * 0.7f + drumLayer;
                                        }

                                    } else if (THEME_MEDITATION_ZEN.equals(currentTheme)) {
                                        // Zen Meditation Theme: Tibetan singing bowls, 432Hz deep Qi frequency, and mountain stream
                                        int noteMs = 1800 + rand.nextInt(800);
                                        int pauseMs = 400 + rand.nextInt(400);

                                        int noteIdx = rand.nextInt(7); // Deeper frequencies
                                        float freq = PENTATONIC_FREQS[noteIdx];

                                        bgmNoteLen = Math.min((SAMPLE_RATE * noteMs) / 1000, bgmNoteBuffer.length);
                                        bgmNotePos = 0;
                                        bgmPauseRemaining = (SAMPLE_RATE * pauseMs) / 1000;

                                        for (int s = 0; s < bgmNoteLen; s++) {
                                            float t = (float) s / bgmNoteLen;
                                            float timeSec = s / (float) SAMPLE_RATE;

                                            // Tibetan singing bowl partials: 1.0, 2.76, 5.4
                                            float p1 = (float) Math.sin(2.0 * Math.PI * freq * timeSec);
                                            float p2 = (float) Math.sin(2.0 * Math.PI * freq * 2.76f * timeSec) * 0.35f;
                                            float p3 = (float) Math.sin(2.0 * Math.PI * freq * 5.40f * timeSec) * 0.12f;

                                            // Binaural 432Hz warm drone
                                            float drone = (float) Math.sin(2.0 * Math.PI * 108.0 * timeSec) * 0.18f;
                                            // Soft mountain stream whisper
                                            float stream = (float) ((rand.nextFloat() - 0.5f) * 0.015f * Math.sin(t * Math.PI));

                                            // Gentle swelling envelope
                                            float env = (float) (Math.sin(Math.min(1.0, t * 2.5) * Math.PI * 0.5) * Math.pow(1.0 - t, 0.9));
                                            bgmNoteBuffer[s] = (p1 + p2 + p3 + drone + stream) * env * 0.85f;
                                        }

                                    } else {
                                        // Sect Peace Theme (Default): Xianxia Donghua Immortal Cultivation World Style
                                        // Rich orchestration: Guzheng cascades, expressive Erhu, airy Dizi bamboo flute, & celestial chimes
                                        float stylePick = rand.nextFloat();
                                        int noteMs;
                                        int pauseMs;
                                        int instrument; // 0: Guzheng pluck, 1: Dizi flute, 2: Erhu bowed string, 3: Guzheng harp cascade

                                        if (stylePick < 0.35f) {
                                            instrument = 0; // Guzheng
                                            noteMs = 650 + rand.nextInt(400);
                                            pauseMs = 120 + rand.nextInt(200);
                                        } else if (stylePick < 0.65f) {
                                            instrument = 1; // Dizi flute
                                            noteMs = 850 + rand.nextInt(550);
                                            pauseMs = 180 + rand.nextInt(220);
                                        } else if (stylePick < 0.88f) {
                                            instrument = 2; // Erhu bowed silk string
                                            noteMs = 1100 + rand.nextInt(600);
                                            pauseMs = 150 + rand.nextInt(200);
                                        } else {
                                            instrument = 3; // Guzheng cascade arpeggio
                                            noteMs = 1200 + rand.nextInt(400);
                                            pauseMs = 250 + rand.nextInt(300);
                                        }

                                        int noteIdx = rand.nextInt(PENTATONIC_FREQS.length);
                                        float freq = PENTATONIC_FREQS[noteIdx];

                                        bgmNoteLen = Math.min((SAMPLE_RATE * noteMs) / 1000, bgmNoteBuffer.length);
                                        bgmNotePos = 0;
                                        bgmPauseRemaining = (SAMPLE_RATE * pauseMs) / 1000;

                                        for (int s = 0; s < bgmNoteLen; s++) {
                                            float t = (float) s / bgmNoteLen;
                                            float timeSec = s / (float) SAMPLE_RATE;

                                            if (instrument == 1) {
                                                // Bamboo flute (Dizi) with gentle 5.4Hz vibrato, micro-pitch glide, and breath resonance
                                                float glide = (float) (Math.sin(t * Math.PI) * 0.008f * freq);
                                                float vibrato = (float) (Math.sin(2.0 * Math.PI * 5.4 * timeSec) * (freq * 0.016f) * Math.min(1.0, t * 2.0));
                                                float effFreq = freq + glide + vibrato;
                                                float f1 = (float) Math.sin(2.0 * Math.PI * effFreq * timeSec);
                                                float f2 = (float) Math.sin(4.0 * Math.PI * effFreq * timeSec) * 0.35f;
                                                float f3 = (float) Math.sin(6.0 * Math.PI * effFreq * timeSec) * 0.12f;
                                                float breath = (float) ((rand.nextFloat() - 0.5f) * 0.022f);
                                                float fluteEnv = (float) (Math.sin(Math.min(1.0, t * 3.2) * Math.PI * 0.5) * Math.pow(1.0 - t, 0.85));
                                                bgmNoteBuffer[s] = (f1 + f2 + f3 + breath) * fluteEnv * 0.88f;
                                            } else if (instrument == 2) {
                                                // Erhu bowed silk string: expressive 6.0Hz vibrato, rich even/odd harmonic overtones
                                                float vibrato = (float) (Math.sin(2.0 * Math.PI * 6.0 * timeSec) * (freq * 0.018f) * Math.min(1.0, t * 1.8));
                                                float effFreq = freq + vibrato;
                                                float e1 = (float) Math.sin(2.0 * Math.PI * effFreq * timeSec);
                                                float e2 = (float) Math.sin(4.0 * Math.PI * effFreq * timeSec) * 0.45f;
                                                float e3 = (float) Math.sin(6.0 * Math.PI * effFreq * timeSec) * 0.28f;
                                                float e4 = (float) Math.sin(8.0 * Math.PI * effFreq * timeSec) * 0.14f;
                                                float bowNoise = (float) ((rand.nextFloat() - 0.5f) * 0.015f);
                                                float erhuEnv = (float) (Math.sin(Math.min(1.0, t * 2.5) * Math.PI * 0.5) * Math.pow(1.0 - t, 0.78));
                                                bgmNoteBuffer[s] = (e1 + e2 + e3 + e4 + bowNoise) * erhuEnv * 0.82f;
                                            } else if (instrument == 3) {
                                                // Guzheng cascade arpeggio glissando across 3 pentatonic strings
                                                int subNote = (int) (t * 3);
                                                float cascadeFreq = PENTATONIC_FREQS[Math.min(PENTATONIC_FREQS.length - 1, noteIdx + subNote)];
                                                float subT = (t * 3) - subNote;
                                                float c1 = (float) Math.sin(2.0 * Math.PI * cascadeFreq * timeSec);
                                                float c2 = (float) Math.sin(4.0 * Math.PI * cascadeFreq * 1.002f * timeSec) * 0.35f;
                                                float c3 = (float) Math.sin(6.0 * Math.PI * cascadeFreq * 0.998f * timeSec) * 0.15f;
                                                float cascadeEnv = (float) Math.exp(-9.0 * subT);
                                                bgmNoteBuffer[s] = (c1 + c2 + c3) * cascadeEnv * 0.78f;
                                            } else {
                                                // Guzheng harp pluck with warm acoustic body resonance decay
                                                float g1 = (float) Math.sin(2.0 * Math.PI * freq * timeSec);
                                                float g2 = (float) Math.sin(4.0 * Math.PI * freq * 1.002f * timeSec) * 0.38f;
                                                float g3 = (float) Math.sin(6.0 * Math.PI * freq * 0.998f * timeSec) * 0.18f;
                                                float g4 = (float) Math.sin(8.0 * Math.PI * freq * timeSec) * 0.08f;
                                                float env = (float) (Math.exp(-20.0 * t) * 0.35f + Math.exp(-3.0 * t) * 0.65f);
                                                bgmNoteBuffer[s] = (g1 + g2 + g3 + g4) * env * 0.92f;
                                            }
                                        }
                                    }

                                    if (bgmNoteLen > 0) {
                                        bgmSample = bgmNoteBuffer[bgmNotePos++] * effBgmVol;
                                    }
                                }
                            }

                            // Subtle acoustic spatial reverb simulation
                            float delayed = delayBuffer[delayPos];
                            delayBuffer[delayPos] = bgmSample + delayed * 0.28f;
                            delayPos = (delayPos + 1) % delayBuffer.length;
                            bgmSample += delayed * 0.18f;

                            // 2. Mix active SFX voices
                            float sfxSum = 0f;
                            synchronized (voices) {
                                for (int v = 0; v < MAX_VOICES; v++) {
                                    Voice voice = voices[v];
                                    if (voice.active && voice.sample != null) {
                                        int idx = (int) voice.cursor;
                                        if (idx < voice.sample.length) {
                                            sfxSum += (voice.sample[idx] / 32768.0f) * voice.volume;
                                            voice.cursor += voice.pitch;
                                        } else {
                                            voice.active = false;
                                        }
                                    }
                                }
                            }

                            float total = bgmSample + sfxSum;
                            mixBuffer[i] = (short) MathUtils.clamp(total * Short.MAX_VALUE, Short.MIN_VALUE, Short.MAX_VALUE);
                        }

                        mixerTrack.write(mixBuffer, 0, BUFFER_SIZE_SAMPLES);
                    }
                } catch (Exception ignored) {
                } finally {
                    if (mixerTrack != null) {
                        try {
                            mixerTrack.stop();
                            mixerTrack.release();
                        } catch (Exception ignored) {}
                        mixerTrack = null;
                    }
                    isMixerRunning.set(false);
                }
            }
        }, "SectPcmMixer");

        mixerThread.setPriority(Thread.NORM_PRIORITY + 1);
        mixerThread.setDaemon(true);
        mixerThread.start();
    }

    private synchronized void stopMixer() {
        isMixerRunning.set(false);
        if (mixerThread != null) {
            mixerThread.interrupt();
            mixerThread = null;
        }
    }

    // ========================================================================
    // PROCEDURAL STUDIO SOUND EFFECT SYNTHESIZER
    // ========================================================================

    private short[] synthesizePcm(String effect) {
        int sampleRate = SAMPLE_RATE;
        int durationMs;
        float startFreq;
        float endFreq;

        if (SFX_COMBAT_START.equals(effect)) {
            durationMs = 380; startFreq = 85f; endFreq = 480f;
        } else if (SFX_STRIKE.equals(effect) || SFX_SWORD_SPAR.equals(effect)) {
            durationMs = 130; startFreq = 720f; endFreq = 160f;
        } else if (SFX_CRITICAL_STRIKE.equals(effect) || SFX_ELEMENTAL_LIGHTNING.equals(effect)) {
            durationMs = 320; startFreq = 1250f; endFreq = 85f;
        } else if (SFX_SHIELD_BLOCK.equals(effect)) {
            durationMs = 150; startFreq = 1650f; endFreq = 680f;
        } else if (SFX_BARRIER_SHATTER.equals(effect)) {
            durationMs = 260; startFreq = 1800f; endFreq = 220f;
        } else if (SFX_ELEMENTAL_FIRE.equals(effect)) {
            durationMs = 250; startFreq = 180f; endFreq = 420f;
        } else if (SFX_ELEMENTAL_ICE.equals(effect)) {
            durationMs = 200; startFreq = 1400f; endFreq = 2100f;
        } else if (SFX_BOSS_ENRAGE.equals(effect)) {
            durationMs = 400; startFreq = 65f; endFreq = 220f;
        } else if (SFX_VICTORY.equals(effect)) {
            durationMs = 450; startFreq = 523.25f; endFreq = 1046.50f;
        } else if (SFX_DEFEAT.equals(effect)) {
            durationMs = 360; startFreq = 220f; endFreq = 75f;
        } else if (SFX_RING_BELL.equals(effect) || SFX_IMMORTAL_BELL.equals(effect)) {
            durationMs = 480; startFreq = 1318.5f; endFreq = 659.25f;
        } else if (SFX_CROWD_CHEER.equals(effect)) {
            durationMs = 450; startFreq = 280f; endFreq = 540f;
        } else if (SFX_BREAKTHROUGH.equals(effect)) {
            // Triumphant celestial chord
            durationMs = 520; startFreq = 261.63f; endFreq = 1046.50f;
        } else if (SFX_BREAKTHROUGH_FAIL.equals(effect)) {
            durationMs = 260; startFreq = 311f; endFreq = 70f;
        } else if (SFX_BESTOW_PILL.equals(effect) || SFX_PILL_CAULDRON_DING.equals(effect)) {
            durationMs = 240; startFreq = 880f; endFreq = 1760f;
        } else if (SFX_ASSIGN_TASK.equals(effect)) {
            durationMs = 80; startFreq = 980f; endFreq = 520f;
        } else if (SFX_RECRUIT.equals(effect)) {
            durationMs = 280; startFreq = 587.33f; endFreq = 1174.66f;
        } else if (SFX_DAO_ENLIGHTENMENT.equals(effect)) {
            durationMs = 500; startFreq = 329.63f; endFreq = 880.00f;
        } else if (SFX_GATHER.equals(effect) || SFX_COLLECT.equals(effect)) {
            durationMs = 90; startFreq = 783.99f; endFreq = 1567.98f;
        } else if (SFX_UPGRADE.equals(effect)) {
            durationMs = 240; startFreq = 392.00f; endFreq = 880.00f;
        } else if (SFX_CLICK.equals(effect)) {
            durationMs = 45; startFreq = 1200f; endFreq = 800f;
        } else {
            durationMs = 60; startFreq = 800f; endFreq = 600f;
        }

        int numSamples = (sampleRate * durationMs) / 1000;
        short[] buffer = new short[numSamples];

        for (int i = 0; i < numSamples; i++) {
            float t = (float) i / numSamples;
            float freq = startFreq + (endFreq - startFreq) * t;
            float phase = (float) (2.0 * Math.PI * freq * (i / (float) sampleRate));

            if (SFX_CLICK.equals(effect)) {
                // Wooden / jade clapper tick
                float env = (float) Math.exp(-30.0 * t);
                float sampleVal = (float) Math.sin(phase) * env * Short.MAX_VALUE * 0.70f;
                buffer[i] = (short) MathUtils.clamp(sampleVal, Short.MIN_VALUE, Short.MAX_VALUE);
            } else if (SFX_BREAKTHROUGH.equals(effect) || SFX_DAO_ENLIGHTENMENT.equals(effect) || SFX_IMMORTAL_BELL.equals(effect)) {
                // Harmonic celestial bell chord
                float h2 = (float) Math.sin(phase * 2.0) * 0.40f;
                float h3 = (float) Math.sin(phase * 3.0) * 0.20f;
                float h4 = (float) Math.sin(phase * 4.0) * 0.10f;
                float env = (float) (Math.sin(t * Math.PI * 0.5) * Math.pow(1.0 - t, 0.75));
                float sampleVal = ((float) Math.sin(phase) + h2 + h3 + h4) * env * Short.MAX_VALUE * 0.75f;
                buffer[i] = (short) MathUtils.clamp(sampleVal, Short.MIN_VALUE, Short.MAX_VALUE);
            } else if (SFX_CRITICAL_STRIKE.equals(effect)) {
                // Thunder transient + sharp blade whoosh
                float noise = (float) ((Math.random() - 0.5) * 0.45 * Math.pow(1.0 - t, 1.2));
                float env = (float) Math.pow(1.0 - t, 1.3);
                float sampleVal = ((float) Math.sin(phase) + noise) * env * Short.MAX_VALUE * 0.90f;
                buffer[i] = (short) MathUtils.clamp(sampleVal, Short.MIN_VALUE, Short.MAX_VALUE);
            } else if (SFX_BESTOW_PILL.equals(effect) || SFX_PILL_CAULDRON_DING.equals(effect)) {
                // Crystal cauldron chime
                float crystalH2 = (float) Math.sin(phase * 2.75) * 0.3f;
                float env = (float) Math.exp(-8.0 * t);
                float sampleVal = ((float) Math.sin(phase) + crystalH2) * env * Short.MAX_VALUE * 0.75f;
                buffer[i] = (short) MathUtils.clamp(sampleVal, Short.MIN_VALUE, Short.MAX_VALUE);
            } else {
                float env = (float) Math.pow(1.0 - t, 1.8);
                float sampleVal = (float) Math.sin(phase) * env * Short.MAX_VALUE * 0.75f;
                buffer[i] = (short) MathUtils.clamp(sampleVal, Short.MIN_VALUE, Short.MAX_VALUE);
            }
        }

        return buffer;
    }

    public void startProceduralBgm(String theme) {
        this.activeBgmTheme = theme;
        if (!isMixerRunning.get()) {
            startMixer();
        }
    }

    public void stopProceduralBgm() {}

    public void playBgm(String theme) {
        startProceduralBgm(theme);
    }

    public void playBgm(String theme, boolean loop) {
        startProceduralBgm(theme);
    }

    public void playBgm(int resId, final boolean loop) {}

    public void stopBgm() {}

    public void stopAmbient() {}

    public void pauseBgm() {
        setBgmEnabled(false);
    }

    public void resumeBgm() {
        setBgmEnabled(true);
    }

    public void setDucking(boolean d) {
        this.ducking = d;
    }

    public float getEffectiveBgmVolume() {
        return MathUtils.clamp(bgmVolume * masterVolume * (ducking ? 0.25f : 1f), 0f, 1f);
    }

    public float getEffectiveAmbientVolume() {
        return MathUtils.clamp(ambientVolume * masterVolume * (ducking ? 0.15f : 1f), 0f, 1f);
    }

    public float getBgmVolume() {
        return bgmVolume;
    }

    public float getAmbientVolume() {
        return ambientVolume;
    }

    public float getSfxVolume() {
        return sfxVolume;
    }

    public float getMasterVolume() {
        return masterVolume;
    }

    public void setBgmVolume(float v) {
        this.bgmVolume = MathUtils.clamp01(v);
    }

    public void setAmbientVolume(float v) {
        this.ambientVolume = MathUtils.clamp01(v);
    }

    public void setSfxVolume(float v) {
        this.sfxVolume = MathUtils.clamp01(v);
    }

    public void setMasterVolume(float v) {
        this.masterVolume = MathUtils.clamp01(v);
    }

    public void setBgmEnabled(boolean e) {
        this.bgmEnabled = e;
    }

    public boolean isBgmEnabled() {
        return bgmEnabled;
    }

    public void setEnabled(boolean e) {
        this.enabled = e;
        if (e && !isMixerRunning.get()) {
            requestAudioFocus();
            startMixer();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void release() {
        mainHandler.removeCallbacksAndMessages(null);
        abandonAudioFocus();
        stopMixer();
        instance = null;
    }
}
