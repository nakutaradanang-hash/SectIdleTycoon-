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
Platform Target  : Android 5.0+ (API 21–36), Web PWA, Windows, Linux, macOS, iOS
```

### 1.2 Deskripsi Singkat
**Sect Idle** adalah permainan simulasi bisnis/idle RPG berbasis Xianxia (budidaya immortal China) untuk Android dan Web/PWA. Pemain mengelola sekte kultivasi dengan:
- 🏰 Membangun dan meningkatkan fasilitas
- 👥 Merekrut & melatih murid/disipel
- ⚔️ Menghadapi pertempuran turn-based
- 💰 Mengelola ekonomi sekte & otomasi sumber daya
- 🤖 Integrasi Gemini AI untuk dialog & narasi dinamis
- 🌐 Kemampuan Web Game Progressive Web App (PWA) dengan dukungan instalasi instan & gameplay offline

---

## BAGIAN II: SPESIFIKASI TEKNIS & ARSITEKTUR

### 2.1 Stack Teknologi

| Aspek | Detail |
|-------|--------|
| **Bahasa Pemrograman** | Java 99.6% + Kotlin 0.4% + Web PWA (HTML5/JS) |
| **JDK Minimum** | Java 11 (target Java 11) |
| **Android Minimum** | API 21 (Android 5.0 Lollipop) |
| **Android Target** | API 36 (Android 15) |
| **Build System** | Gradle 8.x (Kotlin DSL) + Shell Packaging Scripts |
| **Framework UI** | Android Jetpack Compose + Native Canvas + Responsive PWA UI |
| **Persistence** | Room Database + SharedPreferences + LocalStorage (PWA) |
| **Rendering** | SurfaceView Double-Buffered Canvas 2D / HTML5 Canvas |
| **Networking** | Retrofit 2 + OkHttp 4 + PWA Service Worker Cache |
| **Audio** | Android SoundPool + Web Audio API Synthesizer |

---

## BAGIAN III: ARTEFAK MULTIPLATFORM & DISTRIBUSI PWA

### 3.1 Ringkasan Artefak Distribution Matrix

| Platform | Format Artefak | Script Build / Package |
| :--- | :--- | :--- |
| 🤖 **Android (Universal)** | `sect-idle-release.apk` / `sect-idle-release.aab` | `./gradlew assembleRelease bundleRelease` |
| 🌸 **Web Game (PWA)** | `sect-idle-web-pwa.zip` / `sect-idle-web-pwa.tar.gz` | `./scripts/package-web-pwa.sh` |
| 🍏 **iOS Mobile** | `sect-idle-ios-simulator.ipa` | `./scripts/package-mobile-ios.sh` |
| 🪟 **Windows PC** | `sect-idle-windows-x64.zip` | `scripts\package-desktop-windows.bat` |
| 🐧 **Linux PC** | `sect-idle-linux-x64.tar.gz` | `./scripts/package-desktop-linux.sh` |
| 🍎 **macOS Universal** | `sect-idle-macos-universal.tar.gz` | `./scripts/package-desktop-macos.sh` |

### 3.2 PWA Web Game Installer
Artefak `sect-idle-web-pwa.zip` berisi seluruh bundel aplikasi web Progressive Web App:
- `index.html`: Web App Shell & interface game PWA.
- `manifest.json`: Spasifikasi Web App Manifest untuk instalasi desktop/mobile.
- `sw.js`: Service Worker untuk manajemen offline caching & instant play.
- `app.js`: Engine game loop, audio synthesizer, dan PWA installer handler.
- `icon-192.svg` & `icon-512.svg`: Ikon PWA resolusi tinggi.

#### Cara Memasang & Memulai PWA Web Game:
1. Jalankan script pembungkus:
   ```bash
   chmod +x scripts/package-web-pwa.sh
   ./scripts/package-web-pwa.sh
   ```
2. Ekstrak artefak `build/dist/sect-idle-web-pwa.zip` ke web server statis / hosting (misalnya GitHub Pages, Netlify, Vercel, Nginx).
3. Akses URL web game di browser modern dan klik tombol **"⚡ Install PWA App"** untuk memasang game secara mandiri di layar utama atau desktop.
