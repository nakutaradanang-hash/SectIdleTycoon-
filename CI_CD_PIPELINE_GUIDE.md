# 🚀 CI/CD Pipeline & Automated Testing Architecture Guide

## 1. Overview & Objectives
This project is configured with an automated Continuous Integration (CI) and Continuous Deployment (CD) pipeline powered by GitHub Actions. Every commit, pull request, or merge triggers rigorous automated validation, static analysis, unit test suites, APM performance verification, and APK build packaging.

---

## 2. Pipeline Architecture & Workflow Stages

```
                        ┌─────────────────────────────────┐
                        │   Developer Git Push / PR       │
                        │ (branches: main, master, dev)   │
                        └────────────────┬────────────────┘
                                         │
                                         ▼
            ┌────────────────────────────────────────────────────────┐
            │ Stage 1: Static Analysis & Lint Validation            │
            │  • JDK 17 & Android SDK environment setup              │
            │  • Gradle wrapper checksum & integrity verification    │
            │  • Android Lint execution (layout, safety, memory)     │
            │  • Artifact: lint-results-debug.html                   │
            └────────────────────────────┬───────────────────────────┘
                                         │ (on success)
                                         ▼
            ┌────────────────────────────────────────────────────────┐
            │ Stage 2: Automated Unit & Integration Testing          │
            │  • Robolectric sandbox execution (Android SDK 36)      │
            │  • APM Telemetry & Anomaly Detection Tests             │
            │  • Sect Data, Resource, Training, War Game Logic Tests │
            │  • Automated JUnit XML test results reporting          │
            │  • Artifact: unit-test-reports (HTML & XML)            │
            └────────────────────────────┬───────────────────────────┘
                                         │ (on success)
                                         ▼
            ┌────────────────────────────────────────────────────────┐
            │ Stage 3: Build, Package & Continuous Deployment        │
            │  • Compile & Assemble Debug APK                        │
            │  • Verify resource bundling & DEX optimization         │
            │  • Artifact: idle-sect-cultivation-debug-apk           │
            │  • Post GitHub Step Summary with metrics               │
            └────────────────────────────────────────────────────────┘
```

---

## 3. Workflow Configuration (`.github/workflows/ci_cd.yml`)

### Triggers
- **Push**: Triggers automatically on pushes to `main`, `master`, and `develop`.
- **Pull Request**: Runs automated validation on any pull request targeting `main`, `master`, or `develop`.
- **Manual Trigger (`workflow_dispatch`)**: Allows developers to manually trigger runs with custom parameters from the GitHub Actions console.

### Concurrency Management
`cancel-in-progress: true` is enabled under a shared concurrency group. If a developer pushes multiple commits in rapid succession, obsolete runs are automatically canceled to conserve runner resources and ensure only the latest build completes.

---

## 4. Test Suite Coverage & Clean Code Standards

The automated test suite runs local JVM unit and integration tests using **Robolectric** without requiring physical emulators or slow device bridges.

### Key Test Suites Implemented:

1. **APM Engine Tests (`ApmManagerTest.kt`)**:
   - `testCpuMonitorSampling`: Validates `/proc/stat` and process CPU delta calculation.
   - `testMemoryWatchdogMetrics`: Validates heap measurement, max memory limits, and percentage accuracy.
   - `testFrameLatencyTrackerMetrics`: Simulates 60 FPS normal rendering, jank threshold crossings (>32ms), and freeze states (>300ms).
   - `testErrorRateTrackerMetrics`: Validates NPE, OOM, fatal crash interception, and sliding-window error velocity calculations.
   - `testLeakDetectorLifecycle`: Validates `WeakReference` lifecycle tracking and candidate retention rules.
   - `testAlertDispatchingAndListeners`: Validates warning/critical alert routing and debounce timing.
   - `testJsonDiagnosticsExport`: Validates JSON schema structure for diagnostics.

2. **Core Sect Game State Tests (`SectDataTest.kt`)**:
   - Default resource state initialization (Spirit Stones, Herbs, Pills, Jade).
   - Disciple addition, removal, and active task accounting.
   - Hard reset functionality and building regeneration.

3. **Resource Reactive Transactions (`ResourceManagerTest.kt`)**:
   - Multi-currency earning and spending validations.
   - Overspend prevention and balance integrity.
   - Thread-safe UI listener notification dispatch.

4. **Cultivation & Breakthrough Logic (`TrainingSystemTest.kt`)**:
   - Realm experience scaling curve calculations.
   - Spirit stone cost scaling per cultivation realm.
   - Multi-factor breakthrough success chance calculation (wisdom, luck, intelligence, facility levels).

5. **Territory Conquest & War Engine (`WarSystemTest.kt`)**:
   - Disciple raid combat strength aggregation.
   - Sect campaign combat simulation against rival factions.
   - Round-by-round battle log generation and rich tag formatting.

---

## 5. Team Developer Workflow

### Step-by-Step Guide for Developers

1. **Creating Feature Branches**:
   ```bash
   git checkout -b feature/new-gameplay-system
   ```

2. **Running Local Tests Before Push**:
   Run the test suite locally using Gradle:
   ```bash
   gradle :app:testDebugUnitTest
   ```
   All tests should report `BUILD SUCCESSFUL`.

3. **Verifying Code Linting**:
   ```bash
   gradle :app:lintDebug
   ```

4. **Pushing to Remote Repository**:
   ```bash
   git add .
   git commit -m "feat: implement enhanced alchemy crafting system with APM hooks"
   git push origin feature/new-gameplay-system
   ```

5. **Reviewing Automated Pipeline Results in GitHub**:
   - Open your Pull Request on GitHub.
   - Under the **Checks** tab, the `Idle Sect Cultivation CI/CD Pipeline` will display live execution steps:
     - 🔍 *Code Style & Static Analysis*
     - 🧪 *Unit & APM Verification Tests*
     - 📦 *Build & Deliver APKs*
   - In the **Step Summary**, review test pass counts, execution times, and download the compiled `.apk` artifact directly for QA testing.

---

## 6. Artifact Retention Policies
- **Debug APKs**: Retained for **30 days** on GitHub Actions for QA installation and device testing.
- **Lint HTML Reports**: Retained for **14 days** for code quality audits.
- **Unit Test Reports**: Retained for **14 days** for regression tracking.
