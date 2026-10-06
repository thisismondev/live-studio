# ADR — Live Studio (Arisan Live Streamer)

## Status
[x] Approved  
Tanggal: 6 Oktober 2026  
Penulis: Development Team  
Referensi: [PRD — Live Arisan Streamer.md](PRD%20%E2%80%94%20Live%20Arisan%20Streamer.md)  

---

## 1. Ringkasan & Konteks (Context)
Aplikasi **Live Studio** dirancang untuk membantu Admin Arisan menyiarkan layar HP mereka langsung ke Facebook Live hanya dengan **1 HP** (menggantikan cara lama yang membutuhkan 2 HP). 

Sebagai **Junior Developer**, fokus utama kita adalah membuat aplikasi yang:
1. **Sederhana & Tanpa Backend**: Semua logika berjalan di perangkat (client-side only).
2. **Cepat & Stabil**: Menggunakan library yang sudah matang dan API standar Android.
3. **Mudah Dipahami**: Menggunakan arsitektur standar Android (MVVM + Clean UI Jetpack Compose).

---

## 2. Keputusan Teknis Utama (Tech Stack)

| Komponen | Teknologi yang Dipilih | Alasan & Cara Pakai |
|---|---|---|
| **Bahasa** | Kotlin | Bahasa standar Android modern, ringkas, dan aman dari NullPointerException. |
| **UI Framework** | Jetpack Compose + Material 3 | Mempermudah pembuatan UI secara deklaratif (Single Activity Architecture). |
| **Arsitektur UI** | MVVM + Clean Architecture Sederhana | Memisahkan *Data Layer*, *Domain Layer*, dan *UI Layer*. |
| **Target Android** | Min SDK 24 (Android 7.0) <br> Target SDK 37 | SDK 24 mencakup mayoritas HP pengguna arisan. Target SDK 37 memenuhi standar Google Play terbaru. |
| **Autentikasi & API** | Firebase Auth + Facebook Login SDK | Digunakan untuk **Firebase Auth (Facebook Provider)**, memegang sesi user terpusat (Firebase UID), serta membuat sesi siaran via **Facebook Live Video API** (`GraphRequest`). |
| **Engine Streaming** | `RootEncoder` (pedroSG94) | Library open-source teruji untuk merekam layar (`MediaProjection`) dan mengirim stream RTMPS ke Facebook Live. |
| **Kamera Depan** | CameraX | Mengakses kamera depan HP untuk overlay wajah penyiar saat siaran live. |
| **Penyimpanan Lokal** | Jetpack DataStore (Preferences) | Untuk menyimpan pengaturan sederhana (tema gelap/terang, judul siaran terakhir, privasi terakhir). Tanpa database SQLite/Room. |
| **Background Execution** | Foreground Service (`StreamService`) | Agar siaran layar tetap berjalan stabil di latar belakang saat pengguna berpindah ke aplikasi spin wheel/browser. |

---

## 3. Arsitektur Aplikasi (Data, Domain, & UI Layer)

Aplikasi dibangun dengan pemisahan 3 layer utama agar kodenya rapi dan mudah dirawat:

```
┌─────────────────────────────────────────────────────────────┐
│                         UI LAYER                            │
│   (Jetpack Compose Screens, ViewModels, UI State / Flow)    │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                        DOMAIN LAYER                         │
│       (Use Cases, Domain Models, Repository Interfaces)     │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                         DATA LAYER                          │
│ (Repository Impl, Data Sources, Facebook SDK, RootEncoder)  │
└─────────────────────────────────────────────────────────────┘
```

---

## 4. Alur Kerja Utama (Main Data Flow)

1. **Alur Login**:
   - Pengguna buka aplikasi -> Klik "Masuk dengan Facebook" -> `AuthRepository` memproses credential via Firebase Auth (`FacebookAuthProvider`) -> Sesi Firebase UID & Facebook Access Token tersimpan -> Masuk Beranda.
2. **Alur Memulai Live**:
   - Pengguna isi Judul & Pilih Privasi (Publik/Teman).
   - Pengguna klik "Mulai Live".
   - `LiveRepository` membuat siaran via Graph API -> Mendapatkan URL RTMPS `rtmps://...`.
   - Aplikasi meminta izin `MediaProjection` (Perekaman Layar) & `SYSTEM_ALERT_WINDOW` (Overlay).
   - `StreamService` dan `OverlayService` dijalankan di foreground.
   - `RootEncoder` mulai mengirimkan sinyal video/audio ke URL RTMPS.
3. **Alur Saat Live (Latar Belakang)**:
   - Pengguna berpindah ke aplikasi spin wheel / browser.
   - Screen capture (`MediaProjection`) mengambil tampilan layar + kamera overlay.
   - Audio microphone & audio internal (Android 10+) ditangkap dan dicampur oleh encoder.
4. **Alur Mengakhiri Live**:
   - Pengguna klik "Stop" di gelembung melayang / notifikasi / aplikasi.
   - Dialog konfirmasi muncul -> Pengguna mengonfirmasi.
   - `StreamService` & `OverlayService` dihentikan.
   - `LiveRepository` mengirim request mengakhiri siaran ke Facebook Graph API.

---

## 5. Izin Android & Batasan Penting (Android Permissions & Constraints)

| Fitur / Izin | Keterangan & Batasan |
|---|---|
| **Izin Overlay (`SYSTEM_ALERT_WINDOW`)** | Pengguna harus memberikan izin secara manual di Pengaturan HP agar gelembung melayang & kotak kamera dapat muncul. |
| **Izin Screen Record (`MediaProjection`)** | Dialog sistem akan muncul setiap kali siaran akan dimulai. Pengguna wajib memilih **"Seluruh Layar" (Entire Screen)** di Android 14+ agar aplikasi lain ikut tersiar. |
| **Suara Aplikasi Internal (`AudioPlaybackCapture`)** | Hanya tersedia di **Android 10 (API 29) ke atas**. Pada Android 7-9, aplikasi akan menampilkan notifikasi bahwa hanya suara mikrofon yang bisa direkam. |
| **Foreground Service Type** | Wajib mendeklarasikan tipe service pada `AndroidManifest.xml`: `mediaProjection`, `microphone`, dan `camera` untuk memenuhi standar Android 14+. |

---

## 6. Tahapan Pengerjaan (Setup & Eksekusi Per Fitur)

Pengerjaan dibagi menjadi **Tahap Setup Awal** dan **Tahap Eksekusi Per Fitur (Data -> Domain -> UI Layer)**:

### A. Tahap Setup Awal (Project Setup)

1. **Level Project (Core Configuration)**
   - Konfigurasi Root Gradle (`build.gradle.kts`, `settings.gradle.kts`, Version Catalog `libs.versions.toml`).
   - Setup dependensi global: Kotlin, Jetpack Compose BOM, Facebook SDK, RootEncoder, CameraX, DataStore, Coroutines.
   - Setup Meta Developer App & pendaftaran Facebook Tester Account.

2. **Level Core Module / Core Package**
   - Setup struktur package `core` (Utils, Constants, Extension functions, Result Wrapper).
   - Setup Base Helper untuk Service (`ForegroundService` helpers, Notification Channel helper).
   - Setup Theme & Design System dasar (Colors, Typography, Shapes Material 3).

3. **Level App Module**
   - Konfigurasi `app/build.gradle.kts` (Application ID, Min SDK 24, Target SDK 37, Compose Enabled).
   - Setup `AndroidManifest.xml` (Izin: Camera, Mic, Overlay, MediaProjection, Foreground Service types).
   - Setup `MainActivity.kt` & Navigation Container dasar.

---

### B. Tahap Eksekusi Per Fitur (Data -> Domain -> UI Layer)

Setiap fitur dieksekusi secara berurutan melewati **Data Layer**, **Domain Layer**, lalu **UI Layer**:

#### 1. Fitur Login Facebook
- **Data Layer**: Integrasi Firebase Auth + Facebook Login SDK, `AuthRepositoryImpl`, simpan token/sesi Firebase UID & Facebook AccessToken ke local DataStore.
- **Domain Layer**: Interface `AuthRepository`, `GetLoginStateUseCase`, `LoginFacebookUseCase`, `LogoutUseCase`.
- **UI Layer**: `LoginViewModel`, `LoginScreen` (Compose) dengan tombol Facebook Login & penanganan UI State (Loading, Success, Error).

#### 2. Fitur Persiapan Siaran (Prepare Live)
- **Data Layer**: `LiveRepositoryImpl` memanggil Facebook Graph API (`POST /me/live_videos`) via `GraphRequest`. `PreferencesRepository` untuk simpan judul & privasi terakhir.
- **Domain Layer**: Interface `LiveRepository`, `CreateLiveStreamUseCase`, `SaveBroadcastSettingsUseCase`.
- **UI Layer**: `HomeScreenViewModel`, `HomeScreen` (Form Input Judul, Dropdown Privasi, Validasi & Tombol "Mulai Live").

#### 3. Fitur Penyiaran Layar & Suara (Core Streaming Engine)
- **Data Layer**: Integrasi `MediaProjection` & `RootEncoder` (pedroSG94) di dalam `StreamService` (Foreground Service), `AudioCapture` (Android 10+).
- **Domain Layer**: Interface `StreamEngine`, `StartStreamUseCase`, `ToggleAudioUseCase`.
- **UI Layer**: Permission Handler Dialog (`MediaProjection`, Mic, Camera), `StreamStatusScreen` / Indikator Status Live.

#### 4. Fitur Kamera Depan Overlay
- **Data Layer**: Integrasi `CameraX` (ProcessCameraProvider, Preview) di dalam jendela melayang (`WindowManager`).
- **Domain Layer**: `CameraOverlayManager` / `ToggleCameraUseCase`.
- **UI Layer**: Floating Draggable Window untuk kamera depan (Touch Listener / Gesture handling di Compose/View).

#### 5. Fitur Gelembung Melayang (Floating Control) & Notifikasi
- **Data Layer**: `OverlayService` (System Alert Window / WindowManager), Helper Notifikasi Persisten.
- **Domain Layer**: `ControlStreamUseCase` (Mute Mic, Mute App Audio, Toggle Cam, Stop).
- **UI Layer**: UI Gelembung Melayang (Floating Bubble) + Quick Menu Control & Persistent Notification Action.

#### 6. Fitur Penanganan Koneksi & Mengakhiri Live
- **Data Layer**: Network Monitor (`ConnectivityManager`), `LiveRepositoryImpl.closeLiveStream()` via Graph API.
- **Domain Layer**: `AutoReconnectUseCase` (timeout 30 detik), `StopLiveStreamUseCase`.
- **UI Layer**: Dialog Konfirmasi Stop Live, Indikator / Toast Status Reconnecting, Screen Ringkasan Live Selesai.

---

## 7. Hal yang DILARANG / DITUNDA (Out of Scope / Non-Goals)

Agar proyek selesai tepat waktu dan tidak rumit:
- ❌ **Jangan buat backend server sendiri** (semua langsung HP <-> Facebook).
- ❌ **Jangan buat database Room/SQLite** (cukup Jetpack DataStore untuk preference).
- ❌ **Jangan buat fitur Spin Wheel bawaan** (pengguna memakai aplikasi spin wheel yang sudah ada di HP mereka).
- ❌ **Jangan tambahkan fitur Live ke YouTube/TikTok** (fokus 100% pada Facebook Live).
- ❌ **Jangan tambahkan fitur komentar penonton dulu** (ditunda untuk versi setelah MVP).
