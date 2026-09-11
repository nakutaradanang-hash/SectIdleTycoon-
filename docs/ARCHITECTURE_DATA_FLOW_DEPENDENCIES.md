# Sect Idle - System Architecture, Data Flow & Dependency Documentation

## 1. Executive Overview & Architectural Principles

The **Sect Idle (Xianxia Cultivation Simulator)** is built upon a high-performance, multi-layered reactive architecture optimized for smooth 60 FPS mobile rendering, thread safety, and zero memory leaks.

```
+-----------------------------------------------------------------------------------+
|                                PRESENTATION LAYER                                 |
|   +--------------------------+  +-------------------------+  +----------------+   |
|   | SectScene (Jetpack/GL)   |  | SectHubActivity/Compose |  | BattleScene    |   |
|   +--------------------------+  +-------------------------+  +----------------+   |
|                |                             |                       |            |
|                v                             v                       v            |
|   +--------------------------+  +-------------------------+  +----------------+   |
|   | SectSceneViewModel       |  | SectHubViewModel        |  | StateFlow / VM |   |
|   +--------------------------+  +-------------------------+  +----------------+   |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
|                                 GAMEPLAY LAYER                                    |
|   +-------------------+  +-------------------+  +-------------------------------+ |
|   | SectData (State)  |  | DiscipleManager   |  | BattleEngine / Tournament     | |
|   | (Thread-Safe)     |  | Alchemy / Farming |  | TalentSystem / Economy        | |
|   +-------------------+  +-------------------+  +-------------------------------+ |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
|                        SYSTEMS & HARDWARE ABSTRACTIONS                            |
|   +-------------------+  +-------------------+  +-------------------------------+ |
|   | AudioManager      |  | CameraSystem 2D/3D|  | RNG / NumberFormatter / Util  | |
|   | (Realtime Mixer)  |  | (Viewport & Cull) |  | SaveManager / Compression     | |
|   +-------------------+  +-------------------+  +-------------------------------+ |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
|                             CORE FOUNDATION LAYER                                 |
|   +-------------------+  +-------------------+  +-------------------------------+ |
|   | MathUtils (LUTs)  |  | Physics2D / Body  |  | Vector2 / Rect (Zero-Alloc)   | |
|   +-------------------+  +-------------------+  +-------------------------------+ |
+-----------------------------------------------------------------------------------+
```

---

## 2. Component Dependency & Data Flow Analysis

### 2.1 Core Foundation Layer (`com.sect.idle.core`)
* **Modules**: `MathUtils`, `Vector2`, `Rect`, `Physics2D`, `GameTime`, `GameConfig`.
* **Data Flow**:
  * Pure, stateless mathematical utilities with precomputed Look-Up Tables (LUTs) for trigonometric functions (`sin`, `cos`) and fast integer square roots (`fastSqrt`).
  * Objects like `Vector2` and `Rect` employ in-place mutation and object pools (`rectPool`, `vecPool`) to eliminate GC allocations during frame render loops.
* **Dependencies**: No external dependencies; zero Android SDK coupling in core math.

### 2.2 Domain Models (`com.sect.idle.models`)
* **Modules**: `Disciple`, `Building`, `BattleUnit`, `Equipment`, `Skill`, `Talent`, `StatusEffect`.
* **Data Flow**:
  * `Disciple` serves as the primary entity containing stats (7 core attributes), realm progression, elemental roots, lifespan, and job assignments.
  * When entering combat, a `BattleUnit` is instantiated from a `Disciple` snapshot with cached defense, elemental multipliers, and talent mitigations.
  * `Building` encapsulates upgrade costs, worker limits, and passive resource yield rates.
* **Consistency**: Thread-safe access via atomic getters and synchronous state recalculation routines (`recalcCombat()`, `recalculateStats()`).

### 2.3 Gameplay Management Layer (`com.sect.idle.gameplay`)
* **Modules**: `SectData`, `DiscipleManager`, `AlchemySystem`, `FarmingSystem`, `BattleEngine`, `TournamentSystem`, `TalentSystem`, `HobbySystem`, `MarketSystem`, `SaveManager`.
* **Data Flow**:
  * `SectData` is the single source of truth for sect inventory (Spirit Stones, Herbs, Ore, Jade) and disciple rosters.
  * User/system actions dispatch mutations through specialized managers (`DiscipleManager`, `AlchemySystem`) which atomically modify `SectData` and trigger `markEconomyDirty()`.
  * `SaveManager` compresses the game state with `CompressionUtil` (Deflate + Base64) and computes checksums to guarantee save integrity.

### 2.4 Audio & System Layer (`com.sect.idle.systems`)
* **Modules**: `AudioManager`, `CameraSystem`, `RNG`, `NumberFormatter`, `CompressionUtil`, `APM / Telemetry`.
* **Data Flow**:
  * `AudioManager` runs a dedicated background audio synthesis and mixing thread (`SectPcmMixer`) at 22,050 Hz.
  * Audio requests from UI or Gameplay (`playSfx`, `playBgmTheme`) are queued asynchronously into a lock-free multi-voice mixer (`Voice[12]`) with automatic priority preemption, pitch shifting, and audio focus ducking.
  * Assets from `res/raw` (8 BGM tracks and procedural SFX) are streamed with fallback algorithmic synthesis.

### 2.5 Presentation & UI Layer (`com.sect.idle.ui`, `com.sect.idle.hub`)
* **Modules**: `SectScene`, `SectSceneViewModel`, `SectHubActivity`, `SectOverviewTab`, `DiscipleList`, `WorldMapTab`.
* **Data Flow**:
  * Unidirectional Data Flow (UDF): ViewModels expose immutable `StateFlow<T>` streams collected by Compose UI with `collectAsStateWithLifecycle()`.
  * User interactions (taps, building upgrades, realm transitions) invoke ViewModel intents, which synchronously trigger audio feedback and mutate domain state.

---

## 3. Resource Conflict Avoidance & Performance Optimization

1. **Audio Mixing Overhead**:
   * Consolidated all audio playback into a single continuous `AudioTrack` stream. This prevents Android OpenSL/AudioTrack allocation limits (32 tracks max) from being exceeded during intense battle sound effects.
2. **Zero-Allocation Rendering**:
   * `CameraSystem` and `Physics2D` utilize reusable scratch vectors (`vecPool`) to prevent garbage collector pauses during high-frequency particle and camera updates.
3. **Save State Atomicity**:
   * `SaveManager` employs atomic file writes (`.tmp` -> rename) along with Deflate byte compression and SHA-256 validation to avoid file corruption on unexpected app exit.
4. **Lifecycle-Aware Coroutines**:
   * All asynchronous UI workflows (3D texture preloading, day/night transitions, FPS telemetry) are bound to `viewModelScope`, ensuring automatic cancellation upon Activity destruction to prevent memory leaks.

---

## 4. Test Verification Matrix

| Module | Unit Tests | Integration Tests | Target Validation |
| :--- | :--- | :--- | :--- |
| `com.sect.idle.core` | `MathUtilsTest`, `Vector2Test`, `Physics2DTest`, `RectTest`, `GameTimeTest` | - | Trigonometry LUTs, zero-allocation math, physics kinematics, collision layers |
| `com.sect.idle.models` | `DiscipleTest`, `BuildingTest`, `BattleUnitTest` | - | Attribute recalculation, combat stats, talent bonuses, building upgrades |
| `com.sect.idle.systems`| `NumberFormatterTest`, `CompressionUtilTest`, `RNGTest`, `CameraSystemTest` | `AudioVisualSyncIntegrationTest` | Base64/Deflate roundtrips, camera projection, audio mixing, theme switching |
| `com.sect.idle.gameplay`| `AlchemySystemTest`, `BattleEngineTest`, `DiscipleManagerTest`, `FarmingSystemTest`, `TournamentSystemTest` | `SectFullLifecycleIntegrationTest`, `CombatSimulationIntegrationTest` | Full sect economy lifecycle, multi-unit battles, realm breakthroughs |
| `com.sect.idle.ui` | `SectSceneViewModelTest` | `AudioVisualSyncIntegrationTest` | UDF state updates, scene transitions, day/night cycle, hardware detection |
