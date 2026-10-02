# Sect Idle Cultivation - Architecture, Security, UI/UX & Optimization Technical Documentation

## 1. Executive Summary & Architecture Overview
Sect Idle Cultivation is an offline-first, high-performance Xianxia simulation game engine designed with modern Jetpack Compose for Android 5.0+ and low-memory embedded Android environments (such as the itel A70, 2GB RAM devices).

The architecture adheres strictly to **Clean MVVM (Model-View-ViewModel)** with decoupled simulation engines, autonomous coroutine tickers, and zero-allocation object pools for hot render paths.

```
+---------------------------------------------------------------------------------+
|                               Jetpack Compose UI                                |
|  - SectScene (Isometric Canvas Grid & Floating Islands)                         |
|  - BattleScene (Real-Time Team Combat Simulator)                                 |
|  - Hubs & Modals (Inventory, Disciples, Alchemy, Quests, Tournaments)           |
+---------------------------------------------------------------------------------+
                                      | (StateFlow / Actions)
                                      v
+---------------------------------------------------------------------------------+
|                         GameViewModel & Navigation Coordinator                  |
|  - Tab transitions, Active Scene Routing, Modal Inspection, Toast Alerts        |
+---------------------------------------------------------------------------------+
                                      |
         +----------------------------+----------------------------+
         v                                                         v
+------------------------------------+  +-----------------------------------------+
|     CoroutineGameLoop (Ticker)     |  |          Gameplay Subsystems            |
|  - Resource Generation Ticks       |  |  - DiscipleManager & Breakthrough Engine|
|  - Offline Progress Accumulator    |  |  - AlchemySystem & Recipe Crafter       |
|  - Speed Dilation (1x, 2x, 5x)     |  |  - FarmingSystem & Mining System        |
+------------------------------------+  |  - BattleEngine & Tournament Arena      |
                                        +-----------------------------------------+
                                                                   |
                                                                   v
+---------------------------------------------------------------------------------+
|                        Core State, Memory & Security Layer                      |
|  - SectData Singleton (Atomic in-memory game state)                             |
|  - MemoryPool & BitmapCache (Zero-allocation pooling, LRU caches)               |
|  - DataValidator (XSS, SQLi, Bound clamping, Safe arithmetic)                   |
|  - SecurityManager (Anti-tamper checksums, SHA-256, Constant-time comparisons)  |
|  - ExceptionManager (ELK/ECS structured telemetry, Crash forensics)             |
+---------------------------------------------------------------------------------+
```

---

## 2. UI/UX Refinements & User Experience
The user interface has been comprehensively polished according to Material Design 3 and Android modern design guidelines:

### Edge-to-Edge & Navigation Inset Handling
- Mandatory `enableEdgeToEdge()` activation in all Activity entry points (`SectHubActivity.kt`, `GameActivity.java`).
- `NavigationBar` components in Jetpack Compose explicitly handle `windowInsets = WindowInsets.navigationBars` to prevent bottom tab clipping on devices utilizing 3-button navigation or modern gesture pills.
- All primary interactive elements enforce a minimum touch target size of 48.dp (`minimumInteractiveComponentSize()`) with visual ripples on click.

### Aesthetic Elevation & Xianxia Atmosphere
- **Distinctive Color Palette**: Rich Midnight Jade (`JadeCyan` `#00E5A3`), Celestial Gold (`#FFD54F`), Spirit Purple (`#B388FF`), and Void Black (`#0B0E14`) layered cards.
- **Dynamic Headers & Hero Banners**: Grand 3D Xianxia sect panorama banners with subtle vertical gradient scrims to preserve contrast and visual depth.
- **Micro-Interactions**: Smooth `AnimatedContent` tab transitions, modal dialog overlays, and toast feedback for cultivation breakthroughs.

---

## 3. Memory Optimization & Zero-Allocation Patterns
Low-end Android devices suffer from frequent Garbage Collection (GC) pauses and Out-Of-Memory (OOM) fatal crashes when objects are dynamically allocated during 60 FPS Canvas render and simulation loops.

### Object Pooling Strategy (`MemoryPool.java`)
- **Vector2 Pool**: Pre-allocated ring buffer of 64 vectors for coordinate calculations.
- **Rect Pool**: Pre-allocated ring buffer of 32 rectangles for dirty bounding box checks and hit testing.
- **Float Array Pool**: Dynamic cached array buffers for trigonometric transformations.
- **StringBuilder Pool**: Thread-safe reusable StringBuilders preventing string concatenation garbage.

### Zero-Allocation Combat Logic (`BattleEngine.java`)
- `findTarget(int team)`: Refactored from dynamic list allocations into a two-pass counting and indexing scan with zero memory allocation per combat tick.

### Telemetry & Structured Logging (`ExceptionManager.java`)
- **Bounded In-Memory Ring Buffer**: Caps maximum stored log events at 250 records (`MAX_STRUCTURED_LOGS`), preventing unconstrained memory accumulation.
- **Forensic Breadcrumb Trail**: Tracks the 50 most recent state transitions with microsecond timestamps.
- **Error Throttling & Deduplication**: High-frequency exceptions are throttled to 1 entry per 3,000ms window with repetition counts (`occurrenceCount`), preventing logcat and heap saturation.

---

## 4. Security, Validation & Error Handling Layer
All user inputs, import strings, and state transitions pass through strict multi-tier verification before reaching the game state:

### Input Sanitization & Whitelisting (`DataValidator.java`)
- **Cross-Site Scripting (XSS)**: Rejects and strips `<script>`, `<iframe>`, `javascript:`, and HTML tags.
- **SQL Injection**: Checks and neutralizes SQL keywords (`UNION`, `SELECT`, `DROP`, comment tokens `--`, `/*`).
- **Path Traversal**: Sanitizes file paths, stripping `../`, `..\`, and null bytes `\0`.
- **Arithmetic Overflow Protection**:
  - `safeAdd(long a, long b)`: Saturates at `Long.MAX_VALUE` / `Long.MIN_VALUE`.
  - `safeMultiply(long a, long b)`: Detects overflow before multiplication and caps at max bounds.
  - `safeSubtractNonNegative(long a, long b)`: Underflow clamps to `0L`.

### Cryptographic Anti-Tampering (`SecurityManager.java`)
- **Device-Unique Salt**: Generates cryptographically unique salts stored in secure private preferences.
- **Payload Signatures**: Computes CRC32 and SHA-256 hashes of game state strings (`statePayload::SIG::checksum`).
- **Constant-Time Verification**: Uses `constantTimeEquals()` to defend against timing side-channel attacks during signature checks.

### SurfaceView Teardown & Lifecycle Guards
- `BattleSurfaceView` and `GameView` validate `surfaceHolder.getSurface().isValid()` before and after canvas operations.
- Interception of `IllegalArgumentException` and `IllegalStateException` during `unlockCanvasAndPost()` prevents buffer queue disconnection crashes on low-end chipsets.
- Implemented `onDetachedFromWindow()` to cleanly stop render threads and unregister Choreographer frame callbacks.

---

## 5. Standardized Error Handling, ErrorCode Registry & Fault Isolation

The application implements a defense-in-depth error handling architecture governed by `ExceptionManager`, `ErrorCode`, and `CrashHandler`:

### ErrorCode Domain Registry (`ErrorCode.java`)
- **System & Runtime (1000-1999)**: `SYS_NPE_GUARD`, `SYS_OOM_DISTRESS`, `SYS_GC_FREEZE`, `SYS_FATAL_CRASH`, `SYS_THREAD_INTERRUPT`, `SYS_RESOURCE_LEAK`.
- **Persistence & Data (2000-2999)**: `SAVE_CORRUPTION`, `SAVE_CHECKSUM_MISMATCH`, `SAVE_IO_ERROR`, `SAVE_ENCODING_ERROR`.
- **Combat & Game Engine (3000-3999)**: `COMBAT_AI_INVALID_STATE`, `COMBAT_RENDER_OVERFLOW`, `COMBAT_CALCULATION_NAN`, `COMBAT_TARGET_NOT_FOUND`.
- **Sect & Economy (4000-4999)**: `SECT_RESOURCE_UNDERFLOW`, `SECT_DISCIPLE_CAP_REACHED`, `SECT_BUILDING_UPGRADE_INVALID`.
- **Audio & Multimedia (5000-5999)**: `AUDIO_PCM_BUFFER_OVERRUN`, `AUDIO_SYNTH_UNDERFLOW`, `AUDIO_RESOURCE_DISPOSED`.
- **Security & Validation (6000-6999)**: `SEC_TAMPER_DETECTED`, `SEC_INVALID_SIGNATURE`, `SEC_INPUT_MALFORMED`, `SEC_RATE_LIMIT_EXCEEDED`.
- **UI & Rendering (7000-7999)**: `UI_BAD_TOKEN_GUARD`, `UI_SURFACE_DESTROYED`, `UI_BITMAP_RECYCLED_ACCESS`, `UI_EVENT_QUEUE_OVERFLOW`.

### Safe Procedural Execution Wrappers
- `ExceptionManager.executeSafely(SafeRunnable, category, actionDesc)`: Executes blocks with automatic exception interception, category tagging, and non-crashing boolean feedback.
- `ExceptionManager.executeSafely(SafeSupplier<T>, fallback, category, actionDesc)`: Executes blocks with automatic default fallback return values on any failure.
- `ExceptionManager.reportError(ErrorCode, throwable, message, context)`: Reports structured diagnostic events with subsystem mapping, severity levels, and automated recovery suggestions.

---

## 6. Comprehensive Automated Test Suite & Performance Benchmarks

| Test Class | Scope / Subsystem | Key Scenarios Validated |
|---|---|---|
| `ExceptionManagerErrorHandlingTest.java` | Error Handling & Telemetry | ErrorCode lookups, SafeRunnable/SafeSupplier isolation, Breadcrumb ring buffer bounding, Log deduplication throttling, Memory pressure reporting. |
| `PerformanceAnalysisToolsTest.java` | APM & Performance Tools | Real-time FPS/MS metrics, Draw call counters, Memory leak detector heuristics, Jank/Freeze frame latency thresholds, Adaptive quality throttling. |
| `CultivatorCombatAITest.java` | 3D Combat AI & Physics | Role determination heuristics, 3D kinematic trajectories, Flying sword array trigonometry, Shield fading, Dead unit gravity fall. |
| `SaveManagerResilienceTest.java` | Persistence & Anti-Tamper | Corrupt JSON recovery, Tampered HMAC signature rejection, Null/Empty string resilience, Multi-slot backup recovery. |
| `EconomyAndResourceResilienceTest.java` | Economy & Disciples | Atomic multi-resource deduction, Underflow prevention, Negative parameter guards, Disciple task assignments & breakthroughs. |
| `DataValidatorSecurityEdgeCasesTest.java` | Security & Input Sanitization | XSS injection neutralization, SQLi keyword stripping, Arithmetic overflow saturation, Path traversal sanitization. |
| `ApmManagerTest.java` | Application Performance Monitoring | Metric collection, Snapshot generation, CPU/Memory telemetry aggregation, Alert threshold triggers. |
| `CombatSimulationIntegrationTest.java` | Combat Engine | High-intensity team battle loop, Action order sorting, Element counter advantages, Battle log validation. |
| `SectFullLifecycleIntegrationTest.java` | Full Game Loop | End-to-end 7-step Xianxia lifecycle progression. |
| `FixedMathAndWorldScaleEngineTest.java` | Layer 0 Engine & FixedMath | Deterministic 16.16 fixed-point math, Origin rebasing, Thread worker dispatch, Memory budgeting. |
| `RomanceOfThreeKingdomsDiplomacyTest.java` | Romance of Three Kingdoms Systems | 6 Rival factions, Stratagems (Tribute, Non-Aggression, Coalition), Officer Council appointments, Loyalty & Defection. |
| `Civilization4XSystemsTest.java` | Civilization 4X Systems | Hex grid map exploration, Outpost claiming, Dao tech tree research, Sect policy doctrines, Ancient wonders & 4X victory conditions. |
| `TheSimsLifeSimAndClimateTest.java` | The Sims Life Sim & Climate | Disciple needs (Energy, Hunger, Stress, Mood), Moodlets, Social interactions & Dao companionship, 4 seasons & dynamic weather. |
| `SectGrandStrategyManagerMasterTest.java` | Master Orchestration | Full grand day progression loop integrating all 10 architectural layers. |

---

## 7. Multi-Layer Grand Strategy, 4X Civilization & Life Simulation Architecture

Inspired by **Romance of the Three Kingdoms**, **Civilization Series**, and **The Sims Series**, optimized for Android 5.0+ and Sketchware Pro v7.0.0:

### Layer 0: Foundation & Deterministic Physics
- **`FixedMath.java`**: 16.16 zero-GC fixed-point math, fast Taylor-series trigonometry (`sin`, `cos`, `sqrt`, `clamp`), avoiding floating-point drift across heterogeneous ARM architectures.
- **`WorldScaleEngine.java`**: Double-precision coordinates with origin rebasing (shifting the local coordinate frame to prevent sub-pixel jitter), deterministic random seed locking for 100% turn replays, dynamic thread pool worker dispatching, and memory budgeting.

### Layer 1 & 2: World Simulation, Climate & Hex Grid
- **`RealmHexGridMap.java` (Civilization 4X Hex Grid)**: 12x12 world hex grid with Fog of War exploration, terrain yields (Plains, Mountain, Spirit Lake, Ancient Ruins, Dragon Vein, Volcano), territory claiming, outpost construction, and turn-based resource harvesting.
- **`ClimateSeasonSystem.java` (World Simulation)**: 4 dynamic seasons (Spring Blossom, Summer Solstice, Autumn Harvest, Winter Frost), 6 dynamic weather states (Sunny, Rain, Thunderstorm, Blizzard, Spirit Fog, Celestial Aurora), temperature simulation, and seasonal harvest multipliers.

### Layer 3 & 4: Romance of the Three Kingdoms Systems
- **`FactionDiplomacySystem.java`**: 6 rival sect factions with dynamic favor scores (-100 to +100), treaty states (War, Hostile, Neutral, Friendly, Allied, Vassal), and tactical stratagems (Send Tribute, Non-Aggression Pact, Trade Pact, Espionage Infiltration, False Flag Slander, Grand Coalition).
- **`OfficerCouncilSystem.java`**: Imperial/Sect court appointments (Grand Elder, General Marshal, Chief Diplomat, Head of Alchemy, Master of Formations, Inspector General), aptitude scoring, loyalty decay/growth, and low-loyalty defection warnings.

### Layer 5: Civilization 4X Tech Tree, Policies & Wonders
- **`DaoTechTreeSystem.java`**: 4 Eras of Dao progression (Foundation, Golden Core, Void Ascension, Celestial Dao), tech prerequisites, and research point generation from library studies.
- **`SectPolicySystem.java`**: Governance doctrine policy cards (Martial Expansion, Silk Road Monopoly, Hermit Solitude, Heavenly Harmony, Demonic Blood Cultivation) with strategic buffs and tradeoffs.
- **`WonderMonumentSystem.java`**: Ancient Wonders of the World (Tower of Nine Heavens, Primordial Dragon Spring, Great Wall of Formations, Lotus Pagoda) and 4 distinct victory conditions:
  1. *Martial Hegemony Victory*: Subjugate or ally with all 6 rival factions.
  2. *Celestial Ascension Victory*: Construct the Tower of Nine Heavens & complete Godhood research.
  3. *Cultural Enlightenment Victory*: Erect the Lotus Pagoda & amass 10,000 Dao Insight.
  4. *Economic Jade Monopoly Victory*: Accumulate 1,000,000 Spirit Stones and dominate realm trade.

### Layer 6: The Sims Life Simulation & Needs
- **`DiscipleLifeSimSystem.java`**: Individual disciple needs (Energy, Hunger, Stress, Social, Mood), emotional moodlets (Normal, Inspired, Enlightened, Serene, Enraged, Heartbroken, Qi Deviation), herbal feeding, and interactive social actions (Chat, Martial Spar, Propose Dao Companionship / Marriage).

### Master Turn Orchestrator
- **`SectGrandStrategyManager.java`**: Harmoniously synchronizes climate, hex grid harvest, Dao tech research, wonder progress, diplomacy, officer loyalty, life needs, and victory checks in a single deterministic simulation turn.

