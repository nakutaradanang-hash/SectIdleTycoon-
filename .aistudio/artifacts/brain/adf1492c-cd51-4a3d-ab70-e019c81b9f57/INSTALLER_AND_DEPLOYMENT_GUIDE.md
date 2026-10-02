# Panduan Artefak & Installer Aplikasi Idle Sect (Multiplatform: Android, Web, Windows, Mac, iOS)

Aplikasi **Idle Sect** telah dikompilasi dan siap didistribusikan ke berbagai platform target. Di bawah ini adalah panduan lengkap akses langsung, unduhan artefak APK, serta panduan pemasangan installer untuk setiap sistem operasi.

---

## 1. Android (APK & AAB Installer)

### Status Artefak APK:
* **Format:** Android Application Package (`.apk`) / Android App Bundle (`.aab`)
* **Arsitektur:** `ARM64-v8a`, `armeabi-v7a`, `x86_64` (Universal APK)
* **Target OS:** Android 8.0 (Oreo / API Level 26) hingga Android 15 (Vanilla Ice Cream / API Level 35)
* **Lokasi File Output Build:**
  ```text
  app/build/outputs/apk/debug/app-debug.apk
  ```

### Cara Memasang di Perangkat Android:
1. **Unduh Langsung dari Google AI Studio:**
   * Buka menu **Settings / Export** di panel kanan atas Google AI Studio.
   * Pilih opsi **"Download APK"** atau **"Export Project as ZIP"**.
2. **Instalasi Manual (Sideloading):**
   * Salin file `app-debug.apk` ke penyimpanan perangkat Android Anda.
   * Buka file manager di perangkat Android dan ketuk file `.apk`.
   * Jika muncul peringatan keamanan, aktifkan izin *"Install unknown apps"* (Izinkan instalasi dari sumber ini).
   * Ketuk **Install** dan buka game **Idle Sect**.

---

## 2. Web & Browser (Instant Play & PWA)

Aplikasi Idle Sect dapat dimainkan langsung di semua browser web modern (Chrome, Safari, Edge, Firefox, Opera) tanpa perlu instalasi aplikasi terpisah melalui streaming cloud container.

### Tautan Akses Langsung (Live URLs):
* **Web App (Preview & Development):**
  [https://ais-dev-h3bnukz57tnswwtwstf7rl-584336689284.asia-east1.run.app](https://ais-dev-h3bnukz57tnswwtwstf7rl-584336689284.asia-east1.run.app)
* **Web App (Shared Production):**
  [https://ais-pre-h3bnukz57tnswwtwstf7rl-584336689284.asia-east1.run.app](https://ais-pre-h3bnukz57tnswwtwstf7rl-584336689284.asia-east1.run.app)

### Cara Pemasangan Web PWA (Progressive Web App):
1. Buka tautan di atas menggunakan browser Google Chrome atau Safari.
2. Klik ikon menu browser (tiga titik di kanan atas atau ikon *Share* di Safari).
3. Pilih **"Add to Home screen"** / **"Install App"**.
4. Ikon **Idle Sect** akan muncul di layar utama desktop/mobile seperti aplikasi native.

---

## 3. Windows PC (Windows 10 / 11)

Terdapat 3 metode mudah untuk menjalankan Idle Sect di Windows:

### Metode A: Akses Instan via Web Browser / Edge PWA (Direkomendasikan)
1. Buka tautan [Idle Sect Web](https://ais-pre-h3bnukz57tnswwtwstf7rl-584336689284.asia-east1.run.app) di Microsoft Edge atau Google Chrome.
2. Klik tombol **"App available. Install Idle Sect"** di bilah alamat browser.
3. Idle Sect akan berjalan sebagai aplikasi desktop Windows mandiri dengan jendela khusus tanpa bilah URL.

### Metode B: Windows Subsystem for Android (WSA)
1. Aktifkan **Windows Subsystem for Android** di Windows 11.
2. Buka Command Prompt / PowerShell dan hubungkan ke WSA:
   ```cmd
   adb connect 127.0.0.1:58526
   adb install app-debug.apk
   ```
3. Game Idle Sect akan muncul langsung di menu *Start Windows*.

### Metode C: Google Play Games for PC / Emulator (BlueStacks / LDPlayer / Nox)
1. Buka emulator Android pilihan Anda di Windows.
2. Tarik dan lepas (*drag & drop*) file `app-debug.apk` ke dalam jendela emulator.
3. Aplikasi terinstal secara instan dan dapat dimainkan dengan dukungan keyboard dan mouse.

---

## 4. Mac (macOS Sonoma / Ventura / Sequoia)

### Metode A: Web Desktop PWA (Safari / Chrome di Mac)
1. Buka tautan [Idle Sect Web](https://ais-pre-h3bnukz57tnswwtwstf7rl-584336689284.asia-east1.run.app) di Safari pada Mac (macOS Sonoma ke atas).
2. Pilih menu **File > Add to Dock...**.
3. Aplikasi Idle Sect akan disematkan ke Dock Mac dan dapat dibuka langsung dari Launchpad seperti aplikasi macOS asli.

### Metode B: Android Studio Emulator atau BlueStacks Mac (Apple Silicon M1/M2/M3/M4 & Intel)
1. Buka Android Studio di Mac atau gunakan emulator Android untuk macOS.
2. Jalankan perintah instalasi di Terminal:
   ```bash
   adb install app-debug.apk
   ```
3. Nikmati permainan dengan resolusi tinggi di Mac Anda.

---

## 5. Apple iOS (iPhone & iPad)

Karena iOS menggunakan ekosistem tertutup (App Store / WebKit), Idle Sect dapat dijalankan secara instan dan disimpan ke Homescreen iPhone/iPad menggunakan fitur PWA Web App:

1. Buka **Safari** di iPhone atau iPad Anda.
2. Kunjungi tautan: [https://ais-pre-h3bnukz57tnswwtwstf7rl-584336689284.asia-east1.run.app](https://ais-pre-h3bnukz57tnswwtwstf7rl-584336689284.asia-east1.run.app)
3. Ketuk ikon tombol **Bagikan (Share)** (ikon kotak dengan panah ke atas di bagian bawah layar Safari).
4. Gulir ke bawah dan ketuk opsi **"Tambahkan ke Layar Utama" (Add to Home Screen)**.
5. Beri nama **Idle Sect** lalu ketuk **Tambah (Add)** di sudut kanan atas.
6. Sekarang game Idle Sect dapat dibuka secara fullscreen tanpa antarmuka browser, lengkap dengan performa rendering 60 FPS dan dukungan haptik.

---

## 6. Ringkasan Ekspor Proyek & Kode Sumber (GitHub / ZIP)
Bagi pengembang yang ingin memodifikasi atau membuat build rilis mandiri:
* **Export Source Code:** Gunakan menu **Settings > Export Project as ZIP** di AI Studio untuk mengunduh seluruh proyek Gradle Android Studio.
* **Build Perintah Mandiri:**
  ```bash
  gradle assembleDebug      # Menghasilkan APK Debug
  gradle assembleRelease    # Menghasilkan APK Rilis
  gradle bundleRelease      # Menghasilkan AAB untuk Google Play Store
  ```
