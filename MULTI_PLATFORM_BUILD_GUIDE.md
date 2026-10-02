# 🌌 Xianxia Immortal Sect Idle — Multi-Platform Build & Coroutine Engine Guide

Panduan lengkap mengenai arsitektur **Background Game Loop (Kotlin Coroutines)** dan **Sistem Build Artefak Multi-Platform GitHub Actions** untuk seluruh perangkat (Desktop, Mobile, dan Web PWA: Windows, Linux, macOS, iOS, Android, dan Web Browsers).

---

## 1. ⚙️ Background Game Loop (Kotlin Coroutines)

Game loop utama telah dimodernisasi menggunakan **Kotlin Coroutines (`CoroutineGameLoop.kt`)** yang berjalan secara mandiri di background thread (`Dispatchers.Default` / `SupervisorJob`).

### 🌟 Fitur & Kemampuan Utama
- **Thread-Safe & Zero Memory Leaks**: Dikelola secara independen menggunakan `SupervisorJob` dan atomik state (`AtomicBoolean`, `AtomicLong`).
- **Penghitungan Sumber Daya Latar Belakang (*Background Resource Generation*)**:
  - **Spirit Stones**: Diproduksi dari *Spirit Mine*, *Main Hall*, dan *Disciple Mining Task*.
  - **Spirit Herbs**: Diproduksi secara berkelanjutan dari *Spirit Garden* dan *Disciple Farming Task*.
  - **Spirit Ores & Pills**: Diproduksi otomatis dari *Mine* dan *Alchemy Pavilion*.
  - **Sect Power**: Dikalkulasi secara real-time berdasarkan total stats dan realm seluruh murid.
- **Progresi Murid (*Disciple Cultivation Engine*)**:
  - **Akumulasi EXP Kultivasi**: Bertambah tiap tick berdasarkan *Wisdom*, *Talent Multiplier*, dan tugas meditasi.
  - **Terobosan Realm (*Realm Breakthrough*)**: Pengecekan otomatis saat EXP mencapai batas ambang dengan kalkulasi peluang sukses berbasis *Luck*, *Realm Tier*, dan *Talent Bonus*.
  - **Manajemen Vitalitas & Mood**: Murid yang bekerja akan menghabiskan energi secara proporsional; murid yang istirahat akan memulihkan energi dan mengurangi stres.
- **Simulasi Progresi Offline (*Fast-Forward Offline Catch-Up*)**:
  - Saat pemain kembali setelah menutup aplikasi, `calculateOfflineProgression(offlineSeconds)` menghitung hasil bertani, menambang, dan kultivasi secara instan tanpa mengunci UI.
- **Integrasi Reaktif (*StateFlow & SharedFlow*)**:
  - `loopState: StateFlow<GameLoopTickState>` untuk diobservasi oleh Jetpack Compose / ViewModel.
  - `eventFlow: SharedFlow<GameLoopEvent>` untuk menangani notifikasi terobosan realm, event sekte, dan hujan rezeki spiritual.

---

## 2. 🌐 Sistem Build Artefak Multi-Platform (GitHub Actions)

Alur kerja CI/CD GitHub Actions (`.github/workflows/multiplatform-artifacts.yml`) telah dikonfigurasi untuk membangun, mengemas, memverifikasi checksum, dan mengunggah artefak untuk **seluruh platform & OS**:

### 📱 Distribusi Mobile

| Target Platform | Format Artefak | Keterangan & Lingkungan Build |
| :--- | :--- | :--- |
| **Android (Universal)** | `sect-idle-release.apk` | Paket APK siap pasang dengan kompresi R8 & optimasi ProGuard. |
| **Android (Google Play)** | `sect-idle-release.aab` | Android App Bundle untuk distribusi resmi Play Store. |
| **iOS (Mobile)** | `sect-idle-ios-simulator.ipa` | Paket simulator & bridge bundle yang dibangun di runner `macos-latest`. |

### 💻 Distribusi Desktop

| Target Platform | Format Artefak | Keterangan & Lingkungan Build |
| :--- | :--- | :--- |
| **Windows (x64)** | `sect-idle-windows-x64.zip` | Portable package lengkap dengan *Batch Launcher* (`Launch-SectIdle.bat`) dan *PowerShell Runner*. |
| **Linux (x64 / ARM64)** | `sect-idle-linux-x64.tar.gz` | Paket standalone tarball dengan binary launcher (`sect-idle-linux`) dan berkas `.desktop` untuk app menu. |
| **macOS (Universal)** | `sect-idle-macos-universal.tar.gz` | Paket `.app` Universal Bundle yang kompatibel dengan Apple Silicon (M1/M2/M3/M4) dan Intel x64. |

### 🌸 Distribusi Web Game & PWA (Progressive Web App)

| Target Platform | Format Artefak | Keterangan & Lingkungan Build |
| :--- | :--- | :--- |
| **Web Browser / Standalone PWA** | `sect-idle-web-pwa.zip` & `sect-idle-web-pwa.tar.gz` | Bundel PWA Web Game lengkap dengan App Shell (`index.html`), Web Manifest (`manifest.json`), Service Worker (`sw.js`), Audio Synth, & Caching Offline. |

#### ⚡ Cara Menjalankan & Memasang PWA Web Game Installer:
1. Ekstrak `sect-idle-web-pwa.zip` atau `sect-idle-web-pwa.tar.gz` ke server web / hosting statis (GitHub Pages, Netlify, Vercel, Nginx, Apache).
2. Buka URL aplikasi di browser modern (Chrome, Edge, Safari, Firefox).
3. Klik tombol **"⚡ Install PWA App"** atau menu browser **"Add to Home Screen / Install App"** untuk memasang game sebagai aplikasi desktop/mobile mandiri (*standalone window*) tanpa bilah URL.
4. Game dapat dimainkan secara **offline** tanpa koneksi internet menggunakan dukungan Service Worker caching & `localStorage` game save.

---

## 3. 🔐 Integritas & Keamanan Checksum SHA-256

Setiap kali build multi-platform selesai, GitHub Actions secara otomatis menghasilkan berkas manifes `SHA256SUMS-ALL-PLATFORMS.txt` yang memuat sidik jari kriptografis seluruh artefak untuk mencegah *tampering* dan memverifikasi integritas file.

---

## 4. 🚀 Cara Memicu Build Multi-Platform

1. **Otomatis via Tag Rilis**:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```
2. **Manual via GitHub Web Interface (*Workflow Dispatch*)**:
   - Buka tab **Actions** di repositori GitHub.
   - Pilih workflow **"Multi-Platform Artifact Matrix (Desktop & Mobile)"**.
   - Klik **"Run workflow"** dan pilih opsi distribusi (`all-platforms`, `mobile-only`, atau `desktop-only`).
