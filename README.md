# 📚 DOKUMENTASI ADMINISTRASIONAL KOMPREHENSIF — SECT IDLE

## BAGIAN I: RINGKASAN EKSEKUTIF

### 1.1 Identitas Proyek
```
Nama Proyek      : Sect Idle (Idle Sect Business Management)
Pemilik          : bowoheru37-crypto
Repository       : github.com/bowoheru37-crypto/SectIdle
Repository ID    : 1351705873
Status           : Active Development (7 hari sejak inisiasi)
Visibility       : Public (Open Source)
Lisensi          : Tidak Ditentukan
Platform Target  : Android 5.0+ (API 21–36)
```

### 1.2 Deskripsi Singkat
**Sect Idle** adalah permainan simulasi bisnis/idle RPG berbasis Xianxia (budidaya immortal China) untuk Android. Pemain mengelola sekte kultivasi dengan:
- 🏰 Membangun dan meningkatkan fasilitas
- 👥 Merekrut & melatih murid/disipel
- ⚔️ Menghadapi pertempuran turn-based
- 💰 Mengelola ekonomi sekte & otomasi sumber daya
- 🤖 Integrasi Gemini AI untuk dialog & narasi dinamis

---

## BAGIAN II: SPESIFIKASI TEKNIS & ARSITEKTUR

### 2.1 Stack Teknologi

| Aspek | Detail |
|-------|--------|
| **Bahasa Pemrograman** | Java 99.6% + Kotlin 0.4% |
| **JDK Minimum** | Java 11 (target Java 11) |
| **Android Minimum** | API 21 (Android 5.0 Lollipop) |
| **Android Target** | API 36 (Android 15) |
| **Build System** | Gradle 8.x (Kotlin DSL) |
| **Framework UI** | Android Jetpack Compose + Native Canvas |
| **Persistence** | Room Database + SharedPreferences + JSON (Moshi) |
| **Rendering** | SurfaceView Double-Buffered Canvas 2D |
| **Networking** | Retrofit 2 + OkHttp 4 |
| **JSON Serialization** | Moshi (Kotlin Codegen) |
| **Cloud/Backend** | Firebase (Gemini API, AppCheck, reCAPTCHA) |
| **Testing** | Roborazzi (Screenshot), JUnit 4, Robolectric |
| **Audio** | Android SoundPool + MediaPlayer (Synthesis) |

### 2.2 Struktur Direktori & Organisasi Kode

```
SectIdle/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/sect/idle/
│   │   │   │   ├── core/                  ✓ Engine & Config
│   │   │   │   │   ├── GameConfig.java    - 14 realm, 11 task, 10 building, 16 personality
│   │   │   │   │   ├── GameLoop.java      - Thread-safe 30-60 FPS loop
│   │   │   │   │   ├── GameState.java     - State machine
│   │   │   │   │   ├── GameTime.java      - In-game time tracking
│   │   │   │   │   ├── MathUtils.java     - Math helpers
│   │   │   │   │   ├── Physics2D.java     - Collision & movement
│   │   │   │   │   ├── Vector2.java       - 2D vector math
│   │   │   │   │   └── Rect.java          - Rectangle bounds
│   │   │   │   │
│   │   │   │   ├── models/                ✓ Data Entities
│   │   │   │   │   ├── Disciple.java      - 7 stats, 14 realm, personality, talent
│   │   │   │   │   ├── Building.java      - 10 tipe bangunan, upgrade, production
│   │   │   │   │   ├── Item.java          - Equipment, consumables, rarity system
│   │   │   │   │   ├── Equipment.java     - Weapon, armor, accessory, artifact
│   │   │   │   │   ├── Skill.java         - Ability system
│   │   │   │   │   ├── StatusEffect.java  - Buff/debuff mechanics
│   │   │   │   │   ├── BattleUnit.java    - Combat wrapper untuk Disciple
│   │   │   │   │   ├── Fairy.java         - Pet system
│   │   │   │   │   ├── Quest.java         - Quest data
│   │   │   │   │   └── MarketListing.java - Trade system
│   │   │   │   │
│   │   │   │   ├── gameplay/              ✓ Game Systems
│   │   │   │   │   ├── SectData.java      - Singleton state holder (disciples, buildings, resources)
│   │   │   │   │   ├── SaveManager.java   - JSON persistence + compression
│   │   │   │   │   ├── TalentSystem.java  - Talent generation & bonuses
│   │   │   │   │   ├── HobbySystem.java   - Hobby assignment
│   │   │   │   │   ├── AlchemySystem.java - Pill crafting
│   │   │   │   │   ├── FarmingSystem.java - Resource gathering
│   │   │   │   │   ├── TrainingSystem.java- Stat training
│   │   │   │   │   ├── BattleEngine.java  - Turn-based combat logic
│   │   │   │   │   ├── TournamentSystem.java- Bracket battles & rewards
│   │   │   │   │   ├── WarSystem.java     - Sect-level warfare
│   │   │   │   │   ├── MarketSystem.java  - Buy/sell trading
│   │   │   │   │   ├── CultivationAiEngine.java- AI disciple cultivation
│   │   │   │   │   ├── JobSystem.java     - Job leveling
│   │   │   │   │   ├── AgeSystem.java     - Disciple aging & lifespan
│   │   │   │   │   ├── EffectManager.java - Status effect pooling
│   │   │   │   │   ├── GameplayFeedbackDispatcher.java- Event broadcasting
│   │   │   │   │   ├── GameViewModel.java - MVVM state holder
│   │   │   │   │   └── SecretManager.java - API key management
│   │   │   │   │
│   │   │   │   ├── ui/                    ✓ Scenes & UI
│   │   │   │   │   ├── GameActivity.java  - Main activity entry
│   │   │   │   │   ├── GameView.java      - Custom view (deprecated in favor of scenes)
│   │   │   │   │   ├── SectScene.java     - Main hub (54KB, isometric world)
│   │   │   │   │   ├── BattleScene.java   - Battle choreography (23KB)
│   │   │   │   │   ├── BattleView.java    - Battle rendering (31KB)
│   │   │   │   │   ├── BattleCustomView.java- Custom battle UI (26KB)
│   │   │   │   │   ├── BattleSurfaceView.java- SurfaceView-based battle (32KB)
│   │   │   │   │   ├── MenuScene.java     - Main menu (18KB)
│   │   │   │   │   ├── SlashMiniGame.java - QTE minigame (12KB)
│   │   │   │   │   ├── SceneManager.java  - Scene routing (11KB)
│   │   │   │   │   └── DialogManager.java - Centralized modals (93KB) ← MASSIVE
│   │   │   │   │
│   │   │   │   ├── render/                ✓ Graphics Pipeline
│   │   │   │   │   ├── RenderEngine.java  - Main render loop (14KB)
│   │   │   │   │   ├── IsometricWorld.java- Isometric projection & depth sort (6KB)
│   │   │   │   │   ├── Fake3D.java        - 2.5D parallax effect (4KB)
│   │   │   │   │   ├── LightSystem.java   - Dynamic lighting (6.6KB)
│   │   │   │   │   ├── ParticleEngineV2.java- Pooled particles (9.5KB)
│   │   │   │   │   ├── PostProcessor.java - Bloom, vignette, effects (5KB)
│   │   │   │   │   ├── SpriteAnimator.java- Frame-based animation (7.7KB)
│   │   │   │   │   ├── StateAnimationSystem.java- State machine animation (27.5KB)
│   │   │   │   │   ├── VisualFX.java      - Visual effect utilities (5.9KB)
│   │   │   │   │   ├── CombatRenderPipeline.java- Battle render pipeline (2.6KB)
│   │   │   │   │   └── RenderLayer.java   - Layer abstraction (3KB)
│   │   │   │   │
│   │   │   │   ├── procedural/            ✓ Content Generation
│   │   │   │   │   ├── SpriteMaker.java   - Procedural disciple avatars (16.6KB)
│   │   │   │   │   ├── ProceduralTexture.java- Perlin noise textures (12KB)
│   │   │   │   │   ├── SpriteSheetCreator.java- Sheet assembly (1KB)
│   │   │   │   │   ├── TextureAtlasBuilder.java- Atlas management (1.6KB)
│   │   │   │   │   └── TileSetEditor.java - Tilemap editor stub (1.2KB)
│   │   │   │   │
│   │   │   │   ├── systems/               ✓ Utilities
│   │   │   │   │   ├── AudioManager.java  - Sound + synthesis (29.9KB)
│   │   │   │   │   ├── AssetManager.java  - Resource loading (7.5KB)
│   │   │   │   │   ├── CameraSystem.java  - Camera tracking & zoom (7.3KB)
│   │   │   │   │   ├── LogManager.java    - Event logging (6.7KB)
│   │   │   │   │   ├── RNG.java           - Random number generation (3.7KB)
│   │   │   │   │   ├── SpriteSheet.java   - Sprite grid management (3.8KB)
│   │   │   │   │   ├── TextureAtlas.java  - Texture region mapping (2.7KB)
│   │   │   │   │   ├── TileMap.java       - Tilemap data (2.7KB)
│   │   │   │   │   ├── NumberFormatter.java- Number formatting (3.3KB)
│   │   │   │   │   └── CompressionUtil.java- GZIP compression (2.2KB)
│   │   │   │   │
│   │   │   │   └── utils/
│   │   │   │       └── ExceptionManager.java- Exception handling
│   │   │   │
│   │   │   ├── res/                       ✓ Resources
│   │   │   │   ├── values/
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   └── dimens.xml
│   │   │   │   ├── layout/
│   │   │   │   │   └── main.xml (splash screen)
│   │   │   │   ├── drawable/
│   │   │   │   │   └── (procedurally generated)
│   │   │   │   └── mipmap/
│   │   │   │       └── ic_launcher (app icon)
│   │   │   │
│   │   │   └── AndroidManifest.xml        ✓ App config
│   │   │
│   │   ├── androidTest/
│   │   │   └── (Instrumentation tests)
│   │   │
│   │   └── test/
│   │       └── (Unit tests + Roborazzi)
│   │
│   ├── build.gradle.kts                   ✓ Module build config
│   └── proguard-rules.pro                 ✓ Obfuscation rules
│
├── build.gradle.kts                       ✓ Root build config
├── gradle.properties                      ✓ Gradle tuning
├── settings.gradle.kts                    ✓ Project structure
├── gradle/                                ✓ Gradle wrapper
├── gradlew & gradlew.bat                  ✓ Scripts
├── metadata.json                          ✓ AI Studio metadata
├── .env.example                           ✓ Environment template
├── .gitignore                             ✓ VCS ignore
└── README.md                              ✓ (Minimal: "sect idle")
```

### 2.3 Dependency Tree (Key Libraries)

```gradle
// Platform BOMs
androidx.compose.bom
firebase.bom

// Core Android
androidx.activity:activity-compose
androidx.core:core-ktx
androidx.lifecycle:lifecycle-* (runtime-compose, viewmodel-compose, runtime-ktx)

// Compose UI
androidx.compose.material3
androidx.compose.ui:ui*
androidx.compose.material.icons.*

// Room Persistence
androidx.room:room-runtime
androidx.room:room-ktx
ksp: androidx.room:room-compiler

// Firebase
firebase:firebase-ai (Gemini)
firebase:firebase-appcheck-recaptcha
firebase:firebase-auth (commented out)
firebase:firebase-firestore (commented out)

// Networking & JSON
retrofit:retrofit
okhttp:okhttp
logging-interceptor
converter-moshi
moshi:moshi-kotlin
ksp: moshi:moshi-kotlin-codegen

// Coroutines
kotlinx-coroutines-android
kotlinx-coroutines-core
kotlinx-coroutines-test (test)

// Testing
androidx.test.runner
androidx.junit
junit
roborazzi (screenshot testing)
robolectric (offline Android testing)

// Secrets Management
com.google.android.libraries.identity.googleid (commented)
androidx.credentials (commented)
gradle-secrets-plugin
```

---

## BAGIAN III: KONFIGURASI GAME & BALANCING

### 3.1 Konfigurasi Inti (GameConfig)

#### **3.1.1 Device Optimization**
```java
TARGET_FPS = 60           // Target rendering
LOGIC_FPS = 30            // Game tick rate
TICK_MS = 1000            // 1 second per tick
DAY_TICKS = 60            // 60 ticks = 1 in-game day
SAVE_INTERVAL = 7         // Save every 7 days

// Entity Limits
MAX_DISCIPLES = 50        // Max managed disciples
MAX_ITEMS = 500           // Inventory cap
MAX_BUILDINGS = 20        // Sect building cap
MAX_BATTLE_UNITS = 40     // Battle participant cap
MAX_EFFECTS = 300         // Particle/effect cap
MAX_LOG_LINES = 100       // Chat log size
```

#### **3.1.2 Quality Tiers & Feature Toggles**

| Tier | Shadows | Lighting | Particles | Bloom | Reflection | Max Lights | Max Particles |
|------|---------|----------|-----------|-------|------------|-----------|---------------|
| **LOW** | ✗ | ✗ | ✗ | ✗ | ✗ | 0 | 0 |
| **MEDIUM** | ✓ | ✓ | ✓ | ✗ | ✗ | 12 | 200 |
| **HIGH** | ✓ | ✓ | ✓ | ✓ | ✓ | 20 | 400 |
| **ULTRA** | ✓ | ✓ | ✓ | ✓ | ✓ | 32 | 600 |

**Default untuk device 1GB RAM:** QUALITY_LOW (no particle overhead)

#### **3.1.3 Sistem Realm (Budidaya)**
```
14 Tingkat Realm:
0.  Mortal (凡人) — Base level
1.  Qi Refining (炼气期)
2.  Foundation Establishment (筑基期)
3.  Core Formation (金丹期)
4.  Nascent Soul (元婴期)
5.  Soul Transformation (化神期)
6.  Void Refinement (合体期)
7.  Body Integration (渡劫期)
8.  Tribulation Transcendence (大乘期)
9.  True Immortal (真仙)
10. Golden Immortal (金仙)
11. Primordial Immortal (太乙金仙)
12. Dao Ancestor (道祖)
13. Heavenly Dao (天道圣人) — Max

REALM_EXP_CAP = 1000      // Per-realm breakthrough threshold
REALM_SUB_STARS = 9       // ★ progression within realm
Scaling: Each realm grants +50 HP, +20 MP, +15 ATK, +10 DEF, +5 SPD
```

#### **3.1.4 Sistem Task (11 Tugas Disipel)**

| ID | Name | Base Income | Primary Stat | Bonus Element |
|----|----|-------------|--------------|----------------|
| 0 | Idle | 0 | — | — |
| 1 | Farming | 50 | STR | Wood (+15%) |
| 2 | Crafting | 80 | AGI+INT | Metal (+15%) |
| 3 | Alchemy | 120 | INT | Fire/Water (+10%) |
| 4 | Cultivation | 0 | INT+WIS | — (Pure XP gain) |
| 5 | Mining | 60 | STR+VIT | Earth (+15%) |
| 6 | Training | 0 | STR+AGI+VIT | — (Stat gains) |
| 7 | Guard | 0 | STR+VIT | — (Combat XP) |
| 8 | Research | 0 | INT+WIS | — (Knowledge) |
| 9 | Trading | 150 | CHA+LCK | — (Highest income) |
| 10 | Exploring | 30 | AGI+LCK | — (Items found) |

**Income Formula:** `Base × Efficiency × (100 + TaskLevel × 5) / 10000`

#### **3.1.5 Sistem Bangunan (10 Fasilitas)**

| ID | Name | Purpose | Unlock Requirement |
|----|------|---------|-------------------|
| 0 | Main Hall | Sect hub & storage | Starting |
| 1 | Library | Knowledge/research | —  |
| 2 | Alchemy Lab | Pill crafting | —  |
| 3 | Forge | Equipment crafting | —  |
| 4 | Spirit Garden | Herb farming | —  |
| 5 | Mine | Jade/stone extraction | —  |
| 6 | Market | Trading hub | —  |
| 7 | Arena | Tournaments & training | —  |
| 8 | Spirit Pool | Water resource & healing | —  |
| 9 | Tower | Defensive/research | —  |

**Production per upgrade level:** Resource output ×1.2 per level

#### **3.1.6 Sistem Sumber Daya (6 Mata Uang)**

```
RES_SPIRIT_STONE = 0      // Primary currency (Farming, etc.)
RES_JADE = 1              // Premium currency (Mining)
RES_ESSENCE = 2           // Alchemy byproduct
RES_REPUTATION = 3        // Sect prestige (wars, events)
RES_FAME = 4              // Public knowledge (tournaments)
RES_MERIT = 5             // Internal achievement points
```

#### **3.1.7 Sistem Kepribadian (16 Tipe)**

| ID | Name | Task Bonus | Wage Multiplier | Effect |
|----|----|-----------|-----------------|--------|
| 0 | None | — | 1.0x | — |
| 1 | Arrogant | +20% Cultivation | 1.5x | Cocky, demands high pay |
| 2 | Diligent | +15% all tasks | 1.0x | Reliable worker |
| 3 | Mysterious | +25% Cultivation | 1.0x | Unpredictable |
| 4 | Merchant | +25% Trading | 1.3x | Profit-driven |
| 5 | Loyal | +20% if loyalty > 70 | 1.0x | Sect-devoted |
| 6 | Lazy | -20% all tasks | 0.7x | Unmotivated |
| 7 | Cold-blooded | +20% Alchemy | 1.0x | Efficient, amoral |
| 8 | Fighter | +25% Training | 1.1x | Combat enthusiast |
| 9 | Scholar | +30% Alchemy/Research | 1.2x | Knowledge seeker |
| 10 | Orphan | +15% if age > 30 | 0.6x | Poor background |
| 11 | Kind | +15% Farming | 1.0x | Gentle, less efficient |
| 12 | Ruthless | +20% Guard/Mining | 1.1x | Harsh, effective |
| 13 | Wise | +20% Research/Cultivation | 1.2x | Strategic thinker |
| 14 | Curious | +25% Exploring | 1.1x | Adventurous |
| 15 | Proud | +20% Guard | 1.4x | Honor-driven, high pay |

#### **3.1.8 Sistem Talent (8 Tipe)**

| ID | Name | Bonus | Stat Focus |
|----|------|-------|-----------|
| 0 | None | 0% | — |
| 1 | Body | +20% Farming/Mining/Training | PHY |
| 2 | Mind | +20% Alchemy/Research | INT |
| 3 | Spirit | +25% Cultivation | WIS |
| 4 | Luck | +LCK/3 all tasks | LCK |
| 5 | Dual | +10% all tasks | BAL |
| 6 | Chaos | ±40% random per day | VAR |
| 7 | Heaven | +15% all tasks (rare) | All |

#### **3.1.9 Sistem Elemen (11 Tipe)**

```
0. None       — No affinity
1. Fire       — Alchemy +10%, Combat burn procs
2. Water      — Alchemy +10%, Healing boost
3. Wood       — Farming +15%, Plant-based tasks
4. Metal      — Crafting +15%, Weapon damage
5. Earth      — Mining +15%, Defense boost
6. Light      — Healing +20%, Anti-shadow
7. Shadow     — Stealth +15%, Curse potency
8. Wind       — Speed +10%, Agility bonus
9. Ice        — Freezing effects, Slowing
10. Lightning — Damage boost, Crit rate +5%
```

**Resonance System:** 2+ disciples with same element in battle = +15% all stats for party

#### **3.1.10 Sistem Rarity (6 Tingkat)**

| Tier | Color | Probability | Stat Bonus | Price |
|------|-------|------------|-----------|-------|
| Common | Gray | 40% | 1.0x | 1x |
| Uncommon | Green | 30% | 1.2x | 3x |
| Rare | Blue | 20% | 1.5x | 8x |
| Epic | Purple | 7% | 2.0x | 25x |
| Legend | Gold | 2.5% | 2.5x | 75x |
| Mythic | Red | 0.5% | 3.5x | 250x |

---

## BAGIAN IV: SISTEM DISIPEL (DISCIPLE SYSTEM)

### 4.1 Struktur Data Disipel (Comprehensive)

```java
public class Disciple {
    // ===== IDENTITY (16 fields) =====
    public String id;              // Unique timestamp-based ID
    public String name;            // Generated or custom
    public String title;           // Earned title (e.g., "Dragon Slayer")
    public boolean isMale;         // Gender flag
    public int age;                // 16–1000 (lifespan-dependent)
    public int lifespan;           // 100–300 base days
    public int birthDay;           // In-game day born
    public int animFrame;          // Current sprite frame (0–3)
    public float animTimer;        // Animation timer (0.0–1.0)
    public boolean isMoving;       // Movement state
    
    // ===== STATS (7 core) =====
    public int str, agi, intel, lck, vit, wis, cha;
    // STR  = Strength    (Damage, carry capacity)
    // AGI  = Agility     (Speed, dodge, accuracy)
    // INT  = Intelligence (Mana, casting power, alchemy)
    // LCK  = Luck        (Crit rate, random events)
    // VIT  = Vitality    (HP, endurance, defense)
    // WIS  = Wisdom      (Meditation, resistance, healing)
    // CHA  = Charisma    (Leadership, trading, social)
    
    // ===== CULTIVATION REALM =====
    public int realm;              // 0–13 (14 realms)
    public int realmExp;           // 0–1000 per realm
    public int realmTier;          // Sub-tier (1–9 stars)
    
    // ===== ELEMENT & TALENT =====
    public int element;            // 0–10 (11 elements)
    public int elementMastery;     // Proficiency level
    public int talentType;         // 0–7 (8 talent types)
    public int talentGrade;        // 1–5 (talent strength)
    public String talentName;      // Display name
    
    // ===== PERSONALITY & MOOD =====
    public int personality;        // 0–15 (16 types)
    public int behaviorState;      // Current behavior (idle/work/social)
    public int mood;               // 0–100 (affects efficiency)
    public int stress;             // 0–100 (accumulates from work)
    public int energy;             // 0–100 (depletes from work)
    public int maxEnergy;          // Base 100, modifiable
    
    // ===== SOCIAL ======
    public int loyalty;            // 0–100 (to sect)
    public int reputation;         // 0–999 (fame points)
    public int relationshipSect;   // 0–100 (sect affinity)
    public ArrayList<String> relationships;  // Ally IDs
    public ArrayList<String> grudges;        // Enemy IDs
    
    // ===== ECONOMY =====
    public int dailyWage;          // Salary per day
    public long totalContribution; // Lifetime earnings
    public long totalEarnings;     // Personal wealth
    
    // ===== TASK ASSIGNMENT =====
    public int currentTask;        // 0–10 (11 task types)
    public int taskEfficiency;     // 10–100 (derived)
    public int taskProgress;       // Per-task progress counter
    public int taskTarget;         // Task completion target
    public int taskLevel;          // Task specialization level
    
    // ===== SKILLS & LEVELING =====
    public ArrayList<Skill> skills;// List of learned abilities
    public int skillPoints;        // Unspent skill points
    public int level;              // 1–100 (character level)
    public long exp;               // Experience points
    public long maxExp;            // Exp needed for next level
    public int alchemySkill;       // Alchemy specialization
    public int bodyRefiningStage;  // Physical cultivation level
    public float efficiency;       // Task efficiency multiplier
    
    /
