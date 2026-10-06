# Rencana Eksekusi: E3 — Core Streaming Engine (Penyiaran Layar & Suara)

Dokumen ini berisi rencana teknis per file untuk tahapan **Core Streaming Engine (Penyiaran Layar & Suara)**.

---

## 1. Ringkasan & Target
Mengintegrasikan `MediaProjection` (perekaman layar) dan `RootEncoder` (pedroSG94) di dalam Foreground Service (`StreamService`) untuk mengirimkan sinyal video/audio RTMPS ke Facebook Live. Mengelola penangkapan suara mikrofon dan audio internal aplikasi (`AudioPlaybackCapture` Android 10+) serta indikator status siaran di aplikasi.

---

## 2. Checklis Pengerjaan Per File

### Service & Data Layer

- [ ] **[NEW] `core/service/NotificationHelper.kt`**
  - Membuat Notification Channel `LIVE_STREAM_CHANNEL_ID`.
  - Membuat persistent notification "Live Sedang Berjalan" dengan timer durasi & tombol aksi cepat "Stop".

- [ ] **[NEW] `service/StreamService.kt`**
  - Foreground Service (`foregroundServiceType="mediaProjection|microphone"`).
  - Inisialisasi `RtmpsDisplay` / `RtmpDisplay` dari RootEncoder.
  - Menangani token `MediaProjection` dari `Intent` data hasil izin perekaman layar.
  - Mengelola audio:
    - Penangkapan mikrofon (`AudioRecord`).
    - Penangkapan audio internal aplikasi (Android 10 / API 29+ via `AudioPlaybackCaptureConfiguration`).
    - Penanganan pesan peringatan untuk Android 7–9 bahwa suara internal tidak didukung.
  - Memulai & menghentikan streaming RTMPS (`rtmpsDisplay.startStream(rtmpsUrl)`).

---

### Domain Layer

- [ ] **[NEW] `domain/model/StreamStatus.kt`**
  - Sealed class: `Idle`, `Preparing`, `Streaming(val durationSeconds: Long)`, `Reconnecting`, `Stopped`, `Error(val message: String)`.

- [ ] **[NEW] `domain/usecase/ToggleAudioUseCase.kt`**
  - UseCase untuk mute/unmute mikrofon dan audio internal secara terpisah.

---

### UI Layer

- [ ] **[NEW] `ui/screens/status/StreamStatusViewModel.kt`**
  - Mengikat `StreamService` via `Binder` / `StateFlow` status siaran.
  - Mengelola state timer durasi live, status mute mic, dan status mute audio internal.

- [ ] **[NEW] `ui/screens/status/StreamStatusScreen.kt`**
  - Layar Status Live In-App:
    - Badge merah "LIVE" + timer durasi siaran.
    - Status & tombol toggle mikrofon dan audio internal.
    - Tombol utama merah Danger "Stop Live".

---

## 3. Verifikasi E3
- Lakukan pengujian siaran layar HP ke Facebook Live.
- Pindah ke aplikasi lain (misal Spin Wheel / Browser) dan verifikasi siaran layar tetap berjalan stabil di Facebook Live.
