# 🚀 CI/CD Pipeline & Deployment Strategy Documentation

This document outlines the Continuous Integration and Continuous Deployment (CI/CD) architecture, deployment strategies, security scanning, and multi-environment pipeline for **Sect Idle**.

---

## 1. Pipeline Overview & Architecture

The CI/CD pipeline is implemented in `.github/workflows/ci_cd.yml` using GitHub Actions. It automates testing, static code quality checks, security compliance, builds, and multi-environment deployments.

```
                  ┌───────────────────────────────┐
                  │    Git Push / PR / Tag /      │
                  │       Workflow Dispatch       │
                  └──────────────┬────────────────┘
                                 │
                                 ▼
                  ┌───────────────────────────────┐
                  │  Stage 1: Code Quality & Lint │
                  │  (Wrapper check, Android Lint)│
                  └──────────────┬────────────────┘
                                 │
                                 ▼
                  ┌───────────────────────────────┐
                  │   Stage 2: Security Scans     │
                  │ (Secret Leak & Policy Checks) │
                  └──────────────┬────────────────┘
                                 │
                                 ▼
                  ┌───────────────────────────────┐
                  │  Stage 3: Automated Testing   │
                  │ (Robolectric + APM + Systems) │
                  └──────────────┬────────────────┘
                                 │
                                 ▼
                  ┌───────────────────────────────┐
                  │  Stage 4: Build & Packaging   │
                  │ (Matrix: Dev APK / Prod AAB)  │
                  └──────────────┬────────────────┘
                                 │
         ┌───────────────────────┼───────────────────────┐
         │                       │                       │
         ▼                       ▼                       ▼
┌───────────────────┐   ┌─────────────────┐   ┌──────────────────────┐
│ Deploy: Dev       │   │ Deploy: Staging │   │ Deploy: Production   │
│ - Artifact Store  │   │ - Pre-Release   │   │ - Signed APK & AAB   │
│ - Debug Build     │   │ - Internal QA   │   │ - GitHub Release v*  │
└───────────────────┘   └─────────────────┘   └──────────────────────┘
```

---

## 2. Multi-Environment Deployment Matrix

| Environment | Trigger Branch / Event | Target Artifacts | Optimization & Minification | Deployment Channel |
| :--- | :--- | :--- | :--- | :--- |
| **Development** | `develop` branch, feature PRs | `app-debug.apk` | Disabled (Fast Build) | GitHub Actions Artifacts (30-day retention) |
| **Staging** | `staging` branch, manual dispatch | `app-release.apk` | Enabled (R8 + ProGuard) | GitHub Pre-Releases & Internal QA |
| **Production** | `main`, `master`, tags `v*` | `app-release.apk` + `app-release.aab` | Enabled (R8 + ProGuard + Resource Shrink) | GitHub Official Releases & Google Play Store |

---

## 3. Pipeline Stages in Detail

### 🔍 Stage 1: Code Quality & Static Analysis (`quality_and_lint`)
- **Gradle Wrapper Validation**: Verifies the SHA-256 checksum of `gradle-wrapper.jar` to prevent supply chain tampering.
- **Android Lint**: Runs `./gradlew lintDebug` to detect syntax, layout performance, and XML configuration issues.
- **Artifact Archival**: Publishes HTML lint reports for review.

### 🛡️ Stage 2: Security & Vulnerability Scans (`security_scans`)
- **Secret Leak Prevention**: Scans repository files to ensure no private keys (`.pem`, `.jks`) or unencrypted Google API tokens are committed in source control.
- **Android Manifest Security**: Validates permissions to ensure zero prohibited broad storage permissions, enforcing the Android Photo Picker standard.

### 🧪 Stage 3: Automated Testing Suite (`automated_tests`)
- **Unit & Robolectric Tests**: Executes `./gradlew testDebugUnitTest`.
- **Game Engine Systems**: Tests APM manager, Sect treasury, disciple recruitment, training math, and war calculation mechanics.
- **Test Reporting**: Ingests JUnit XML results and generates step summary tables.

### 📦 Stage 4: Multi-Environment Build & Packaging (`build_application`)
- **Keystore Handling**: Uses custom signing keys from repository secrets (`KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD`) or generates a local cryptographic fallback.
- **Production Artifact Generation**:
  - `assembleRelease`: Minified APK.
  - `bundleRelease`: Google Play App Bundle (`.aab`).
- **Cryptographic Hashing**: Computes SHA-256 hashes stored in `SHA256SUMS.txt`.
- **R8 Mapping Archival**: Saves `mapping.txt` for production crash stack trace de-obfuscation.

### 🚀 Stage 5: Continuous Deployment (`deploy_*`)
- **Development**: Archives artifacts for developer download.
- **Staging**: Creates pre-releases tagged `staging-build-<run_number>`.
- **Production**: Creates official release tags (e.g., `v1.0.0`) with release notes, binaries, and checksums.

---

## 4. Repository Secrets Configuration

To configure signing for Production releases in GitHub Settings -> Secrets and Variables -> Actions:

| Secret Name | Description | Example / Note |
| :--- | :--- | :--- |
| `KEYSTORE_BASE64` | Base64-encoded release `.jks` file | Used for CI signing |
| `STORE_PASSWORD` | Keystore password | Secret password |
| `KEY_PASSWORD` | Key alias password | Secret password |
| `GITHUB_TOKEN` | Built-in GitHub Actions token | Automatically provided |

---

## 5. Deployment Strategies & Rollback Procedure

1. **Tag-Based Production Deploy**:
   ```bash
   git tag v1.0.1
   git push origin v1.0.1
   ```
2. **Manual Dispatch Deploy**:
   - Go to Actions -> **Enterprise CI/CD Pipeline** -> **Run workflow**.
   - Select the target environment (`development`, `staging`, or `production`).
3. **Rollback Strategy**:
   - If a production build introduces an issue, roll back by tagging the previous stable commit (e.g. `v1.0.2` pointing to the previous commit) or using GitHub Release asset promotion.
