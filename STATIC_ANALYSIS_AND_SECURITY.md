# 🛡️ Automated Static Analysis, Security Scanning & Clean Code Engineering Guide

This document outlines the architecture, automated tooling, static analysis rules, security vulnerability scanners, and error handling strategies implemented across the **Sect Idle** project.

---

## 1. Automated Static Code Analysis Suite

The codebase integrates multi-tier automated static analysis that executes during local development, via Gradle verification tasks, and automatically within the CI/CD pipeline.

### Tooling Matrix

| Tool / Script | Verification Scope | Trigger / Task | Actionable Feedback |
| :--- | :--- | :--- | :--- |
| **Android Lint** | Layouts, performance, security, accessibility, deprecated APIs, memory leaks | `./gradlew lintDebug` | HTML (`lint-results-debug.html`), XML, and terminal logs |
| **`scripts/run-static-analysis.sh`** | Dead code, orphaned resources, duplicate function signatures, hot-path allocations, uncontrolled lifecycle calls | `codeQualityCheck` / CI Stage 1 | Colorized CLI output, violation counts, non-zero exit codes |
| **`scripts/run-security-scan.sh`** | Hardcoded secrets, private keys, Google Play permission policies, insecure ciphers, PRNG audit | `securityScan` / CI Stage 2 | Security audit checklist and deployment gating |
| **Zero-GC Allocation Validator** | Enforces zero object instantiation (`new Paint`, `new RectF`, etc.) in `onDraw`/render loops | Static script analysis | Specific line-by-line warnings for pre-allocation |

---

## 2. Security Vulnerability Scanning & Compliance

### Security Rules Enforced

1. **Zero Hardcoded Secrets Policy**:
   - Automated regex scanners inspect all source code (`.java`, `.kt`, `.xml`) for exposed Google API keys (`AIza...`), OAuth secrets, and raw private keys (`-----BEGIN RSA PRIVATE KEY-----`).
   - Secrets and environment configuration are managed via `.env` / `.env.example` with the Secrets Gradle Plugin.

2. **Google Play Developer Policy Compliance**:
   - Prohibits broad media/storage permissions (`READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, `READ_MEDIA_IMAGES`).
   - Android Photo Picker (`ActivityResultContracts.PickVisualMedia`) is mandated for any media selection.

3. **Cryptographic Standards**:
   - Cryptographic salts and checksums use **SHA-256** and **HMAC-SHA256**.
   - Deprecated weak ciphers (such as `DES` and `RC4`) are strictly blocked by security scans.
   - Non-deterministic security tokens utilize `java.security.SecureRandom`.

4. **Save Data Anti-Tampering**:
   - `SecurityManager` generates cryptographic HMAC checksums for local persistence files.
   - Corrupt, modified, or truncated save payloads are detected, quarantined, and safely recovered from verified rolling backups.

---

## 3. Comprehensive Error Handling & Fault Isolation

The application implements a multi-layer defense-in-depth error handling architecture to ensure stability under all runtime scenarios.

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Application Layer                               │
│        (GameView, BattleScene3D, SectScene, DialogManager)             │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│                       Defensive Guard Layer                            │
│  • ExceptionManager.executeSafely(SafeRunnable / SafeSupplier)         │
│  • Safe default fallback returns                                       │
│  • Bounded input sanitization (DataValidator)                          │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│                   Asynchronous Structured Logging                      │
│  • Non-blocking ConcurrentLinkedQueue worker thread                   │
│  • Elastic Common Schema (ECS) JSON log formatting                     │
│  • Circular bounded in-memory buffer (250 items, Zero memory leak)    │
│  • Forensic Breadcrumb recording (last 50 operational actions)         │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│                Centralized Uncaught Crash Interception                 │
│  • CrashHandler & ExceptionManager global hooks                        │
│  • Persistent emergency dump writing (crash_log.txt)                   │
│  • APM anomaly recording & graceful session recovery                   │
└────────────────────────────────────────────────────────────────────────┘
```

### Specific Error Scenario Mitigations

- **NullPointerExceptions (NPE)**:
  - All public APIs, listeners, and collections enforce defensive non-null checks, default empty fallbacks, and safe execution wrappers.
- **OutOfMemoryError (OOM) & GC Stutter**:
  - Hot render loops use pre-allocated pools for `Paint`, `RectF`, `Matrix`, and particles.
  - Scene lifecycle methods (`destroy()`) explicitly recycle bitmaps and clear collection caches.
  - `onTrimMemory` and `onLowMemory` callbacks purge cached particle textures and force lightweight rendering.
- **Performance Lags & Freezes**:
  - `PerformanceProfiler` and `ApmManager` continuously sample frame times; anomalies exceeding 32ms (jank) and 300ms (freeze) are recorded with breadcrumb context.
- **File Descriptor Leaks**:
  - File I/O streams in `LogManager`, `SaveManager`, and `CrashHandler` utilize deterministic `try-finally` or `try-with-resources` patterns with explicit flush and close operations.

---

## 4. CI/CD Integration & Developer Verification Commands

Developers can run local quality and security audits before pushing code:

```bash
# Run full static code analysis and clean code checks
bash scripts/run-static-analysis.sh

# Run security vulnerability and policy compliance scans
bash scripts/run-security-scan.sh

# Run Gradle code quality verification
./gradlew codeQualityCheck

# Run Gradle security scan
./gradlew securityScan

# Execute complete unit, Robolectric, and APM test suites
./gradlew testDebugUnitTest
```
