# Rencana Eksekusi: E3 - Core Streaming Engine (Penyiaran Layar & Suara)

> **Status: [ ] Direvisi.** Implementasi awal memakai `RtmpDisplay` (RootEncoder 2.4.9) dan mengasumsikan berbagi layar penuh. Rencana direvisi agar pengguna dapat **memilih aplikasi yang dibagikan** (seperti Zoom/Discord) dan menggantinya saat siaran berjalan. Kode yang ada akan disesuaikan pada tahap implementasi; item yang terdampak dikembalikan menjadi belum.
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
- **Berbagi Aplikasi Pilihan (ala Zoom/Discord)**:
  - Pemilih aplikasi disediakan **sistem Android**. Di Android 14+ dialog izin menawarkan "Bagikan satu aplikasi" atau "Seluruh layar"; daftar aplikasi tidak dapat dikustomisasi. Di Android 7-13 hanya tersedia seluruh layar.
  - Aplikasi **tidak memaksa** pilihan. Sebelum dialog sistem muncul, ditampilkan panduan singkat: *"Pilih aplikasi undian Anda"*.
  - Urutan saat **Mulai Live**: Facebook API membuat siaran -> panduan -> dialog pemilih sistem -> siaran terhubung dengan aplikasi terpilih. Penonton tidak melihat layar kosong di awal.
  - Jika berbagi layar berhenti di tengah siaran (aplikasi ditutup atau pengguna menghentikan berbagi dari sistem), **siaran Facebook tetap hidup** dan penonton melihat gambar **"Berbagi layar dijeda"**. Pengguna dapat memilih aplikasi lain lagi tanpa memulai siaran baru.
  - Pengguna memilih atau mengganti aplikasi lewat tombol **"Pilih Aplikasi"** di `StreamStatusScreen` dan tombol aksi **"Ganti Aplikasi"** di notifikasi siaran. Tombol melayang menyusul di E5. Keduanya membuka dialog sistem lagi.
  - Aplikasi yang dibagikan harus tetap tampil di depan agar tersiar.
- **Migrasi Engine ke `GenericStream`**: RootEncoder dinaikkan ke **2.8.1** (versi terbaru menurut dokumentasi resmi). `StreamService` berpindah dari `RtmpDisplay` ke `GenericStream` dengan `ScreenSource`. Saat dijeda, sumber video diganti ke gambar placeholder; saat aplikasi baru dipilih, diganti ke `ScreenSource` baru. Nama API tetap diverifikasi lewat build di Android Studio karena dokumentasi dan kode lama berbeda antar versi. Migrasi ini juga memudahkan overlay kamera di E4.
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

- [x] **[REVISI] `data/local/PreferencesManager.kt`**
  - Menambahkan key `video_quality_profile` untuk menyimpan profil resolusi yang dipilih.

---

### Domain Layer

- [ ] **[NEW] `domain/model/StreamStatus.kt`**
  - Sealed interface:
    - `data object Idle : StreamStatus`
    - `data object Connecting : StreamStatus`
    - `data class Streaming(val durationSeconds: Long, val bitrateKbps: Int, val isMicMuted: Boolean, val isAppAudioMuted: Boolean) : StreamStatus`
    - `data class ScreenPaused(val durationSeconds: Long, val bitrateKbps: Int) : StreamStatus` (siaran hidup, berbagi layar dijeda)
    - `data class Reconnecting(val attempt: Int, val maxAttempts: Int) : StreamStatus`
    - `data class Stopped(val summaryDurationSeconds: Long) : StreamStatus`
    - `data class Error(val message: String) : StreamStatus`

- [x] **[NEW] `domain/model/VideoQualityProfile.kt`**
  - Enum `AUTO_720P` (720x1280, 2500 kbps, Label: "Otomatis (720p)"), `SAVER_480P` (480x854, 1200 kbps, Label: "Hemat Data (480p)"), `HIGH_1080P` (1080x1920, 4000 kbps, Label: "Tinggi (1080p)").

- [x] **[NEW] `domain/repository/StreamRepository.kt`**
  - Interface contract:
    - `val streamStatus: StateFlow<StreamStatus>`
    - `fun startStream(resultCode: Int, data: Intent, rtmpsUrl: String, quality: VideoQualityProfile)`
    - `fun stopStream()`
    - `fun toggleMic(mute: Boolean)`
    - `fun toggleAppAudio(mute: Boolean)`

- [x] **[NEW] `domain/usecase/StartStreamUseCase.kt`, `StopStreamUseCase.kt`, `ToggleAudioUseCase.kt`, `GetStreamStatusUseCase.kt`**
  - UseCases pembungkus aksi streaming untuk UI.

---

### UI Layer & Navigation

- [x] **[REVISI] `ui/navigation/Screen.kt` & `AppNavigation.kt`**
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

### Revisi untuk Berbagi Aplikasi Pilihan

- [ ] **[REVISI] `gradle/libs.versions.toml`**
  - Naikkan `rootEncoder` dari `2.4.9` ke `2.8.1`.
  - Opsional, belum dipakai di E3: `com.github.pedroSG94.RootEncoder:extra-sources:2.8.1` (menyediakan `CameraXSource` dan `CameraUvcSource`, relevan untuk kamera depan di E4).

- [ ] **[REVISI] `service/StreamService.kt`**
  - Ganti `RtmpDisplay` dengan `GenericStream` + `ScreenSource` + sumber audio (mikrofon dan audio internal).
  - Daftarkan `MediaProjection.Callback`; saat `onStop`, ganti sumber video ke placeholder "Berbagi layar dijeda" dan ubah status ke `ScreenPaused` (jangan hentikan RTMP).
  - Aksi baru `ACTION_UPDATE_PROJECTION` (menerima hasil dialog sistem yang baru) untuk mengganti ke `ScreenSource` baru.
  - Pastikan token `MediaProjection` dibuat setelah `startForeground` (Android 14+).

- [ ] **[NEW] `res/drawable/screen_paused_placeholder` (atau Bitmap yang dirender dari teks)**
  - Gambar "Berbagi layar dijeda" untuk penonton.

- [ ] **[REVISI] `core/service/NotificationHelper.kt`**
  - Tambahkan tombol aksi "Ganti Aplikasi" yang membuka layar status dan memicu dialog pemilih sistem.

- [ ] **[NEW] `ui/screens/home/components/ShareGuidanceDialog.kt`**
  - Panduan singkat "Pilih aplikasi undian Anda" sebelum dialog sistem. Di Android 7-13 teksnya menyesuaikan (hanya seluruh layar tersedia).

- [ ] **[REVISI] `ui/screens/status/StreamStatusScreen.kt` & `StreamStatusViewModel.kt`**
  - Tombol "Pilih Aplikasi" / "Ganti Aplikasi" yang memicu `createScreenCaptureIntent()`.
  - Tampilan status `ScreenPaused` (badge "DIJEDA" dan ajakan memilih aplikasi).

---

### Konfigurasi AndroidManifest

- [x] **[REVISI] `AndroidManifest.xml`**
  - Deklarasikan `<service android:name=".service.StreamService" android:foregroundServiceType="mediaProjection|microphone" android:exported="false" />`.

---

> **Catatan implementasi:** kode E3 versi awal (`RtmpDisplay`, berbagi layar penuh) sudah ditulis tetapi **belum dikompilasi atau diuji di perangkat**, dan akan direvisi sesuai bagian "Revisi untuk Berbagi Aplikasi Pilihan" di atas.
## 3. Verifikasi E3
- [ ] Dialog sistem `MediaProjection` muncul dengan benar saat Mulai Live ditekan.
- [ ] Foreground service `StreamService` aktif dengan notifikasi persisten di system bar.
- [ ] Video dan audio (mikrofon + audio internal) berhasil ditransmisikan ke URL RTMPS Facebook Live.
- [ ] Berpindah ke aplikasi lain (misal Browser/Spin Wheel) tidak menghentikan siaran di Facebook Live.
- [ ] Mute Mikrofon dan Mute Suara Aplikasi berfungsi secara independen.
- [ ] Auto-reconnect aktif saat simulasi kehilangan sinyal internet.
- [ ] Tombol "Stop Live" menghentikan encoder dan foreground service secara bersih tanpa memory leak.
- [ ] Android 14+: dialog sistem menawarkan "Bagikan satu aplikasi"; siaran hanya menampilkan aplikasi terpilih.
- [ ] Menutup aplikasi yang dibagikan membuat penonton melihat "Berbagi layar dijeda" tanpa mengakhiri siaran Facebook.
- [ ] Memilih aplikasi baru dari `StreamStatusScreen` atau notifikasi melanjutkan siaran yang sama.
- [ ] Android 7-13: alur tetap berjalan dengan berbagi seluruh layar.