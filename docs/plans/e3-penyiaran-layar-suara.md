# Rencana Eksekusi: E3 - Core Streaming Engine (Penyiaran Layar & Suara)

Dokumen ini berisi rencana teknis per file untuk tahapan **Core Streaming Engine (Penyiaran Layar & Suara)**.

---

## 1. Ringkasan & Keputusan Desain (Design Decisions)
Mengintegrasikan **MediaProjection** (perekaman layar) dan **RootEncoder (pedroSG94)** ke dalam Foreground Service (**`StreamService`**) untuk mengirimkan sinyal video/audio RTMPS ke Facebook Live secara langsung dari perangkat pengguna.

### Keputusan Teknis Utama:
- **Kualitas Video Dinamis**: 
  - *Adaptive Bitrate Otomatis*: Mengatur bitrate secara real-time saat bandwidth jaringan naik/turun untuk mencegah buffering/freeze siaran.
  - *Profil Kualitas di Beranda*:
    1. **Otomatis / Standar (Default)**: 720x1280 (720p Portrait) @ 30fps, base bitrate 2500 kbps (2.5 Mbps).
    2. **Hemat Data**: 480x854 (480p Portrait) @ 30fps, base bitrate 1200 kbps (1.2 Mbps).
    3. **Kualitas Tinggi**: 1080x1920 (1080p Portrait) @ 30fps, base bitrate 4000 kbps (4.0 Mbps).
  - Pilihan profil terakhir disimpan di `PreferencesManager`.
- **Penanganan Audio**:
  - *Default*: Mikrofon **ON** dan Suara Aplikasi **ON** (keduanya aktif sejak detik pertama).
  - *Kontrol Mute Terpisah*: Pengguna dapat mematikan/menyalakan (mute/unmute) Mikrofon dan Suara Aplikasi secara independen kapan saja selama live berlangsung.
  - *Kompatibilitas Android 7–9*: Suara aplikasi otomatis dinonaktifkan (karena `AudioPlaybackCapture` hanya ada di Android 10 / API 29+), dengan notifikasi info bahwa hanya mikrofon yang disiarkan.
- **Strategi Reconnect Otomatis**:
  - Jika koneksi RTMPS terputus di tengah siaran (misal sinyal seluler drop), engine otomatis mencoba menyambung ulang (*auto-reconnect*) hingga **5 kali percobaan** dengan jeda waktu 3 detik per percobaan.
  - Status UI dan Notifikasi menampilkan pesan **"Menghubungkan Ulang... (x/5)"**.
  - Jika setelah 5 kali tetap gagal, siaran otomatis diakhiri dan status kesalahan ditampilkan ke pengguna.
- **Alur Transisi UI**:
  - Klik "Mulai Live" di `HomeScreen` -> pemicu izin sistem `MediaProjection` (Pengguna wajib memilih "Seluruh Layar" / *Entire Screen*).
  - Navigasi berpindah ke **`StreamStatusScreen`** (Layar Status Siaran) yang menampilkan badge LIVE merah, timer siaran berjalan, status bitrate, tombol kontrol audio, dan tombol "Stop Live".
  - Notifikasi persisten foreground service tetap berjalan di bar status saat pengguna menekan tombol Home untuk beralih ke aplikasi spin wheel/browser.

---

## 2. Checklis Pengerjaan Per File

### Service, Engine & Data Layer

- [ ] **[NEW] `core/service/NotificationHelper.kt`**
  - Membuat Notification Channel `live_stream_channel` (Importance Low/Default agar tidak berisik saat update timer).
  - Fungsi `buildLiveNotification(context, durationText, isReconnecting, onStopIntent)` dengan tombol aksi cepat **"Stop"**.

- [ ] **[NEW] `service/StreamService.kt`**
  - Service ber-anotasi `@AndroidEntryPoint`, dideklarasikan dengan `foregroundServiceType="mediaProjection|microphone"`.
  - Mengelola siklus hidup `RtmpDisplay` / RootEncoder engine.
  - Mengelola penangkapan layar via `MediaProjection` token dari Activity result.
  - Mengelola penangkapan audio mikrofon & audio internal (`AudioPlaybackCapture` di Android 10+).
  - Menangani callback koneksi `ConnectChecker`:
    - `onConnectionSuccessRtmp()` -> Ubah status jadi `Streaming`, reset percobaan reconnect.
    - `onConnectionFailedRtmp(reason)` -> Picu auto-reconnect (maks 5x) atau ubah status jadi `Error`.
    - `onDisconnectRtmp()` -> Picu penanganan putus koneksi.
    - `onNewBitrateRtmp(bitrate)` -> Perbarui indikator bitrate di UI.
  - Menerima action perintah via Intent: `ACTION_START`, `ACTION_STOP`, `ACTION_TOGGLE_MIC`, `ACTION_TOGGLE_APP_AUDIO`.

- [ ] **[NEW] `data/streaming/StreamEngineController.kt`**
  - Singleton manager penyedia `StateFlow<StreamStatus>`.
  - Jembatan kontrol antara ViewModel dan `StreamService` untuk perintah start, stop, mute mic, dan mute internal audio.

- [ ] **[REVISI] `data/local/PreferencesManager.kt`**
  - Menambahkan key `video_quality_profile` untuk menyimpan profil resolusi yang dipilih.

---

### Domain Layer

- [ ] **[NEW] `domain/model/StreamStatus.kt`**
  - Sealed interface:
    - `data object Idle : StreamStatus`
    - `data object Connecting : StreamStatus`
    - `data class Streaming(val durationSeconds: Long, val bitrateKbps: Int, val isMicMuted: Boolean, val isAppAudioMuted: Boolean) : StreamStatus`
    - `data class Reconnecting(val attempt: Int, val maxAttempts: Int) : StreamStatus`
    - `data class Stopped(val summaryDurationSeconds: Long) : StreamStatus`
    - `data class Error(val message: String) : StreamStatus`

- [ ] **[NEW] `domain/model/VideoQualityProfile.kt`**
  - Enum `AUTO_720P` (720x1280, 2500 kbps, Label: "Otomatis (720p)"), `SAVER_480P` (480x854, 1200 kbps, Label: "Hemat Data (480p)"), `HIGH_1080P` (1080x1920, 4000 kbps, Label: "Tinggi (1080p)").

- [ ] **[NEW] `domain/repository/StreamRepository.kt`**
  - Interface contract:
    - `val streamStatus: StateFlow<StreamStatus>`
    - `fun startStream(resultCode: Int, data: Intent, rtmpsUrl: String, quality: VideoQualityProfile)`
    - `fun stopStream()`
    - `fun toggleMic(mute: Boolean)`
    - `fun toggleAppAudio(mute: Boolean)`

- [ ] **[NEW] `domain/usecase/StartStreamUseCase.kt`, `StopStreamUseCase.kt`, `ToggleAudioUseCase.kt`, `GetStreamStatusUseCase.kt`**
  - UseCases pembungkus aksi streaming untuk UI.

---

### UI Layer & Navigation

- [ ] **[REVISI] `ui/navigation/Screen.kt` & `AppNavigation.kt`**
  - Menambahkan rute `Screen.StreamStatus` (`"stream_status"`).
  - Navigasi otomatis dari `Home` ke `StreamStatus` setelah `MediaProjection` disetujui.
  - Navigasi kembali ke `Home` saat siaran dihentikan / selesai.

- [ ] **[REVISI] `ui/screens/home/HomeScreen.kt` & `HomeViewModel.kt`**
  - Menambahkan pemilih Profil Kualitas Video (Otomatis, Hemat Data, Kualitas Tinggi).
  - Mengintegrasikan ActivityResultLauncher untuk `MediaProjectionManager.createScreenCaptureIntent()`.
  - Callback saat Facebook Live API sukses: langsung memicu request izin rekam layar sistem, lalu menjalankan `StreamService`.

- [ ] **[NEW] `ui/screens/status/StreamStatusViewModel.kt`**
  - Mengamati `streamStatus` dari repository.
  - Mengelola aksi tombol Mute Mic, Mute Audio App, dan Stop Live.

- [ ] **[NEW] `ui/screens/status/StreamStatusScreen.kt`**
  - Header kartu status: Badge merah berkedip "LIVE" + Timer durasi format `JJ:MM:DD`.
  - Kartu pemantauan sinyal & indikator bitrate real-time (kbps).
  - Dua kartu tombol interaktif Mute/Unmute:
    - Mikrofon (Status: Aktif / Dibisukan).
    - Suara Aplikasi Internal (Status: Aktif / Dibisukan / Tidak Didukung di Android 7–9).
  - Tombol aksi utama "Stop Live" (Material 3 Danger/Error container).
  - Teks panduan: *"Anda dapat menekan tombol Home untuk beralih ke aplikasi Spin Wheel / Browser. Siaran tetap berjalan di latar belakang."*

---

### Konfigurasi AndroidManifest

- [ ] **[REVISI] `AndroidManifest.xml`**
  - Deklarasikan `<service android:name=".service.StreamService" android:foregroundServiceType="mediaProjection|microphone" android:exported="false" />`.

---

## 3. Verifikasi E3
- [ ] Dialog sistem `MediaProjection` muncul dengan benar saat Mulai Live ditekan.
- [ ] Foreground service `StreamService` aktif dengan notifikasi persisten di system bar.
- [ ] Video dan audio (mikrofon + audio internal) berhasil ditransmisikan ke URL RTMPS Facebook Live.
- [ ] Berpindah ke aplikasi lain (misal Browser/Spin Wheel) tidak menghentikan siaran di Facebook Live.
- [ ] Mute Mikrofon dan Mute Suara Aplikasi berfungsi secara independen.
- [ ] Auto-reconnect aktif saat simulasi kehilangan sinyal internet.
- [ ] Tombol "Stop Live" menghentikan encoder dan foreground service secara bersih tanpa memory leak.
