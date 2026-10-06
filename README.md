# Live Studio — Live Arisan Streamer

[![Android Target SDK](https://img.shields.io/badge/Target%20SDK-37-brightgreen.svg)](https://developer.android.com)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24-blue.svg)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)
[![UI Framework](https://img.shields.io/badge/UI-Jetpack%20Compose-green.svg)](https://developer.android.com/jetpack/compose)

**Live Studio** adalah aplikasi Android native yang memungkinkan penyelenggara/admin arisan menyiarkan layar HP mereka langsung ke **Facebook Live** secara otomatis dengan **1 HP saja** (menggantikan cara lama yang merepotkan dan membutuhkan 2 HP).

---

## 🎯 Solusi & Fitur Utama

- **1 HP Live Streaming**: Tidak perlu HP kedua untuk mengarahkan kamera ke layar spin wheel/browser.
- **Auto RTMPS Streaming**: Terhubung otomatis ke Facebook Live Video API tanpa perlu menyalin stream key manual.
- **Screen & Internal Audio Capture**: Menyiarkan tampilan layar HP dan suara aplikasi internal (Android 10+) beserta mikrofon.
- **Front Camera Overlay**: Menampilkan wajah penyiar dalam kotak kamera kecil yang bisa digeser di atas layar (*draggable overlay*).
- **Floating Controls**: Gelembung tombol melayang (*System Alert Window*) untuk aksi cepat: *Mute Mic*, *Mute App Audio*, *Toggle Camera*, dan *Stop Live*.
- **Edge-to-Edge & Portrait Mode Only**: Tampilan modern mengikuti tren Android terbaru, dioptimalkan khusus untuk orientasi vertikal (Portrait).

---

## 🛠️ Tech Stack & Arsitektur

| Komponen | Teknologi |
|---|---|
| **Bahasa** | Kotlin |
| **UI Framework** | Jetpack Compose + Material 3 (Portrait Mode Only) |
| **Prinsip UI** | Edge-to-Edge (`enableEdgeToEdge`, Scaffold Safe Drawing, IME Insets) + Compose Styles |
| **Arsitektur Kode** | Clean Architecture Sederhana (Data Layer → Domain Layer → UI Layer) |
| **Autentikasi & API** | Facebook Android SDK (Facebook Login & Live Video Graph API) |
| **Engine Streaming** | `RootEncoder` / pedroSG94 (MediaProjection + RTMP/RTMPS) |
| **Kamera Depan** | CameraX (`ProcessCameraProvider`) |
| **Latar Belakang** | Foreground Services (`StreamService` & `OverlayService`) |
| **Penyimpanan Lokal** | Jetpack DataStore Preferences (Tema, Judul, Privasi terakhir) |

---

## 📁 Struktur Dokumentasi Proyek (`/docs`)

Semua spesifikasi dan rencana pengembangan terdokumentasi lengkap di folder `docs/`:

- **Dokumen Produk (`/docs/agile`)**:
  - 📄 [`Vision Doc — Live Arisan Streamer.md`](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/docs/agile/Vision%20Doc%20%E2%80%94%20Live%20Arisan%20Streamer.md): Visi produk & problem statement.
  - 📄 [`PRD — Live Arisan Streamer.md`](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/docs/agile/PRD%20%E2%80%94%20Live%20Arisan%20Streamer.md): Requirement fungsional (FR-01 s/d FR-29) & non-fungsional.
  - 📄 [`ADR — Live Studio.md`](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/docs/agile/ADR%20%E2%80%94%20Live%20Studio.md): Keputusan arsitektur, tech stack, dan tahapan per layer.
  - 📄 [`Design Brief — Live Studio.md`](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/docs/agile/Design%20Brief%20%E2%80%94%20Live%20Studio.md): Panduan UI/UX Material 3, Edge-to-Edge, dan 11 komponen layar.

- **Rencana Eksekusi per Epic (`/docs/plans`)**:
  - 📋 [`README.md (Dokumentasi Index)`](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/docs/README.md): Indeks dokumen dan status pengembangan.
  - 📋 [`e0-persiapan-uji-kelayakan.md`](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/docs/plans/e0-persiapan-uji-kelayakan.md): Epic 0 (Setup Level & Spike).
  - 📋 [`e1-login-facebook.md`](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/docs/plans/e1-login-facebook.md): Epic 1 (Facebook Login).
  - 📋 [`e2-persiapan-siaran.md`](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/docs/plans/e2-persiapan-siaran.md): Epic 2 (Persiapan Siaran).
  - 📋 [`e3-penyiaran-layar-suara.md`](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/docs/plans/e3-penyiaran-layar-suara.md): Epic 3 (Core Streaming Engine).
  - 📋 [`e4-kamera-depan.md`](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/docs/plans/e4-kamera-depan.md): Epic 4 (Kamera Depan Overlay).
  - 📋 [`e5-tombol-melayang-notifikasi.md`](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/docs/plans/e5-tombol-melayang-notifikasi.md): Epic 5 (Floating Control & Notifikasi).
  - 📋 [`e6-koneksi-mengakhiri-siaran.md`](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/docs/plans/e6-koneksi-mengakhiri-siaran.md): Epic 6 (Reconnection & Stop Flow).

---

## 🚀 Tahapan Pengerjaan Proyek

1. **Tahap Setup Awal**:
   - Level Project (`libs.versions.toml`, `build.gradle.kts`)
   - Level Core Module (`core/util/`, `core/theme/`, `core/service/`)
   - Level App Module (`AndroidManifest.xml`, `MainActivity.kt`)
2. **Tahap Eksekusi Per Fitur**:
   - Data Layer → Domain Layer → UI Layer (diikuti dengan unit test & verifikasi manual di perangkat fisik).

---

## 📝 Lisensi & Privasi

Aplikasi ini dikembangkan untuk penggunaan publik secara gratis tanpa iklan.
Detail kebijakan privasi dapat dibaca di [`docs/PRIVACY_POLICY.md`](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/docs/PRIVACY_POLICY.md).
