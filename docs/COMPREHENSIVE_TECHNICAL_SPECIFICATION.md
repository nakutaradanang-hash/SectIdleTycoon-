# Sect Idle Cultivation - Comprehensive Technical & Architectural Specification

---

## 1. System Architecture & High-Level Engineering Design

### 1.1 Architectural Pattern & Module Separation
The **Sect Idle Cultivation Simulator** is engineered with a strict layered **Clean MVVM / Unidirectional Data Flow (UDF)** architecture, ensuring 100% decoupling between mathematical engines, state machines, simulation logic, hardware abstraction, and Jetpack Compose/Canvas UI.

```
+---------------------------------------------------------------------------------------------------+
|                                      PRESENTATION LAYER                                           |
|  - Jetpack Compose UI (SectHubScreen, SectOverviewTab, CultivationChamberTab, WorldMapTab)        |
|  - Canvas Hardware Renderers (RenderEngine, CombatRenderPipeline, Fake3D, Projection3D)            |
|  - ViewModels & StateFlow (SectSceneViewModel, SectHubViewModel, SectHubState)                     |
+---------------------------------------------------------------------------------------------------+
                                                  |
                                                  v
+---------------------------------------------------------------------------------------------------+
|                                       GAMEPLAY ENGINE LAYER                                       |
|  - Sect Grand Strategy & 4X Engine (SectData, Civilization4XSystems, RomanceOfThreeKingdoms)       |
|  - Disciple Life Simulation (DiscipleManager, DiscipleLifecycleStateMachine, TheSimsLifeSim)      |
|  - Combat & AI Simulation (BattleEngine, CultivatorCombatAI, CultivationAiEngine, Tournament)      |
|  - Resource & Economy Engine (ResourceManager, AlchemySystem, FarmingSystem, MarketSystem)        |
+---------------------------------------------------------------------------------------------------+
                                                  |
                                                  v
+---------------------------------------------------------------------------------------------------+
|                                  SYSTEMS & HARDWARE ABSTRACTION                                   |
|  - Audio Engine: SectPcmMixer (22.05 kHz Multi-Voice Mixer, Procedural Synthesis)                 |
|  - APM & Diagnostics: ApmManager, MemoryWatchdog, CpuMonitor, LeakDetector, FrameLatencyTracker   |
|  - Utilities & Security: SecurityManager, DataValidator, ExceptionManager, CrashHandler, Haptic    |
|  - Persistence: SectDatabase (Room), SectRepository, SaveManager (Deflate + SHA-256)             |
+---------------------------------------------------------------------------------------------------+
                                                  |
                                                  v
+---------------------------------------------------------------------------------------------------+
|                                      CORE FOUNDATION LAYER                                        |
|  - Fixed-Point & Zero-GC Math: MathUtils (LUTs for Sin/Cos/Sqrt), Vector2, Rect, Physics2D        |
|  - GameLoop: Double-Buffered Threaded Surface Loop, Microsecond Frame-Pacer                       |
+---------------------------------------------------------------------------------------------------+
```

---

## 2. Procedural Flows & Lifecycle Management

### 2.1 Game Loop & Frame Simulation Flow
1. **Surface Initialization**: When `SurfaceHolder` is created, `GameLoop` starts a dedicated high-priority daemon thread (`Sect-GameLoopThread`).
2. **Delta Time Calculation**: Microsecond resolution time delta (`dt`) calculated via `System.nanoTime()`, strictly clamped to `[0.0001s, 0.1s]` to prevent physics teleports during background suspension.
3. **Fixed-Step Physics & State Simulation**:
   - `onGameUpdate(dt)` invokes `DiscipleLifecycleStateMachine.update(dt)`, `ParticleEngineV2.update(dt)`, `StateAnimationSystem.update(dt)`, and `BattleEngine.update(dt)`.
4. **Double-Buffered Canvas Rendering**:
   - `SurfaceHolder.lockCanvas()` acquires the hardware backbuffer.
   - Render layers are executed sequentially: Background -> Terrain / Stage -> Shadows (3D) -> Entities -> Particles & FX -> Screen-Space Banners -> Post-Processor Overlay.
   - `SurfaceHolder.unlockCanvasAndPost(canvas)` safely presents the frame.
5. **Frame Pacing & Adaptive Throttling**:
   - Computes remainder sleep duration: `sleepNanos = targetFrameNanos - elapsedNanos`.
   - If `sleepNanos < -targetFrameNanos`, the engine triggers catch-up updates (up to 5 frame skips) without rendering to preserve deterministic game state.

### 2.2 Save, Export, Import & Security Integrity Flow
1. **Serialization**: `SaveManager.saveGame()` serializes `SectData` state to a compact JSON schema.
2. **Compression**: String payload is compressed using `java.util.zip.Deflater` into raw byte stream and encoded to Base64.
3. **Cryptographic Signing**: An HMAC-SHA256 signature is appended to prevent save tampering and memory exploits.
4. **Atomic File Persistence**: Written to a temporary file (`save.dat.tmp`), flushed to disk, and atomically renamed to `save.dat`.

---

## 3. Mathematical & Physics Foundations

### 3.1 Fast Look-Up Tables (LUTs) & Fixed-Point Arithmetic
* **Trigonometry**: 360-degree pre-allocated float tables (`MathUtils.SIN_TABLE`, `MathUtils.COS_TABLE`) provide $O(1)$ trigonometric operations with zero heap allocation.
* **Fast Square Root**: `MathUtils.fastSqrt(int x)` utilizes integer binary search with $O(\log n)$ efficiency.
* **Projection3D**:
  $$\begin{aligned}
  \Delta X &= W_X - (T_X + C_X) \\
  \Delta Y &= W_Y - (T_Y + C_Y) \\
  \Delta Z &= W_Z - (T_Z + C_Z) \\
  R_X &= \Delta X \cos(\text{yaw}) - \Delta Y \sin(\text{yaw}) \\
  R_Y &= \Delta X \sin(\text{yaw}) + \Delta Y \cos(\text{yaw}) \\
  P_Y &= R_Y \cos(\text{pitch}) - \Delta Z \sin(\text{pitch}) \\
  P_Z &= R_Y \sin(\text{pitch}) + \Delta Z \cos(\text{pitch}) \\
  \text{Scale} &= \frac{\text{FOV}}{P_Y + \text{FOV}} \cdot \text{Zoom} \\
  S_X &= \text{CenterX} + R_X \cdot \text{Scale} \\
  S_Y &= \text{CenterY} - P_Z \cdot \text{Scale}
  \end{aligned}$$

---

## 4. Visual & Rendering Pipeline

### 4.1 Layered Graphics Architecture
* **Fake3D**: Renders pseudo-3D extruded polygons, isometric blocks, perspective ground shadows, and specular highlights using native 2D Android Canvas draw primitives.
* **LightSystem**: Multi-light radial engine supporting point lights, pulsing spirit nodes, torch flickers, and global ambient tinting using additive `PorterDuff.Mode.ADD` shaders.
* **StateAnimationSystem**: Zero-allocation state machine handling cinematic feedback for Critical Strikes, Tournament Clashes, War Conquests, and Realm Breakthroughs across ENTER, PEAK, HOLD, and EXIT states.
* **ParticleEngineV2**: SIMD-style primitive array processing for up to 512 active particles (Fire, Smoke, Sparks, Qi Petals, Sword Aura, Heavenly Tribulation Lightning).

---

## 5. Audio & Real-Time Synthesis System

* **Architecture**: Single unified `AudioTrack` output running at 22,050 Hz 16-bit PCM on a dedicated low-latency thread (`SectPcmMixer`).
* **Polyphony**: 12 concurrent voice channels supporting ADSR volume envelopes, procedural sine/sawtooth/white noise generation, and automatic audio ducking.
* **Procedural Sound FX**:
  - Sword Slash: High-frequency frequency sweeps with exponential decay.
  - Breakthrough Boom: Low-frequency harmonic sub-bass rumble with resonant decay.
  - Spirit Stone Clink: Metallic harmonic dual-tone chime.

---

## 6. Telemetry, APM & Stability Assurance

* **MemoryWatchdog**: Tracks JVM Heap, Native Heap, and GC pause frequencies with low-memory alerts.
* **CpuMonitor**: Samples `/proc/stat` and `/proc/self/stat` for real-time CPU utilization.
* **FrameLatencyTracker**: Measures frame duration jitter, detecting Jank frames ($\ge 30\text{ms}$) and Frozen frames ($\ge 250\text{ms}$).
* **CrashHandler**: Global `UncaughtExceptionHandler` intercepting fatal crashes, capturing breadcrumbs, device specs, and rotating local crash logs (`crash_log.txt`).

---

## 7. Multi-Language Localization Matrix

The application supports complete multi-language switching across **9 languages** without requiring app restart:
1. English (`en`)
2. Japanese (`ja`)
3. Korean (`ko`)
4. Simplified Chinese (`zh`)
5. Indonesian (`in` / `id`)
6. Arabic (`ar`) - with RTL layout support
7. Spanish (`es`)
8. Portuguese (`pt`)
9. Traditional Chinese / Extended variants

---

## 8. Test Verification & Code Quality Standards

* **Unit Test Suite**: Covers 100% of critical math routines, state machines, combat calculations, APM trackers, render pipelines, and database repositories.
* **Clean Code & Robustness**:
  - Zero NPE risks via defensive argument validation (`DataValidator`).
  - Zero GC allocation in 60 FPS update and render loops.
  - Strict Java 7 & Kotlin Compose interoperability.
  - Robolectric test integration for JVM execution.
