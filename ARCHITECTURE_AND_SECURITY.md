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

## 5. Comprehensive Integration Test Suite Matrix

| Integration Test Class | Covered Subsystems | Key Test Scenarios |
|---|---|---|
| `EconomyAndInventoryIntegrationTest.java` | `SectData`, `ResourceManager`, `MarketSystem`, `Building`, `Item` | Resource consumption, Building upgrades, Market price fluctuation, Item stacking overflow, Equipment durability degradation, Arithmetic overflow clamping. |
| `DiscipleCombatLifecycleIntegrationTest.java` | `DiscipleManager`, `Disciple`, `Equipment`, `BattleEngine`, `TalentSystem` | Disciple generation & validation, Equipment stat recalculation, Exp accumulation & Realm breakthrough, Multi-team battle execution, Bound checking. |
| `SaveLoadSecurityIntegrationTest.java` | `SaveManager`, `SectData`, `SecurityManager`, `DataValidator` | End-to-end save/load persistence, Signed backup export/import, Tampered save rejection, XSS/SQLi payload injection resistance, Cryptographic hashing. |
| `UIStateGameLoopIntegrationTest.kt` | `GameViewModel`, `CoroutineGameLoop`, `SectData`, Compose State | 5-Tab navigation state transitions, Modal inspection/dismissal, Game speed multiplier cycling (1x/2x/5x), Background coroutine ticker progression. |
| `AudioVisualSyncIntegrationTest.java` | `AudioManager`, SoundPool, BGM Audio Ducking | Volume attenuation, Audio ducking during critical events, Theme BGM playback switching, Multi-SFX concurrent playback. |
| `CombatSimulationIntegrationTest.java` | `BattleEngine`, `BattleUnit`, Elemental Matrix | High-intensity team battle loop, Action order sorting, Element counter advantages, Battle log validation. |
| `SectFullLifecycleIntegrationTest.java` | Full Game Loop (Sect, Farming, Alchemy, Tournaments) | Complete 7-step Xianxia lifecycle progression. |
