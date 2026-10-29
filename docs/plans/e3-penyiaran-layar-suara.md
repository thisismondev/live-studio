# Rencana Eksekusi: E3 - Core Streaming Engine (Penyiaran Layar & Suara)

> **Status: [x] Selesai Diimplementasikan (Siap Verifikasi).** Mode penyiaran mendukung dua opsi: **Seluruh Layar** (*Entire Screen*) dan **Aplikasi Tertentu** (*Specific Application*). Pada mode Aplikasi Tertentu, Live Studio menyediakan pemilih aplikasi internal (Launcher Apps + Pencarian + Ingat Terakhir), peluncuran otomatis aplikasi saat live mulai, dan sensor privasi otomatis (*Privacy Guard* via `UsageStatsManager` + `ImageObjectFilterRender`) yang menjeda video ke gambar placeholder saat host keluar dari aplikasi target (mikrofon tetap aktif). Host juga dapat mengganti aplikasi di tengah siaran.

Dokumen ini berisi rencana teknis per file untuk tahapan **Core Streaming Engine (Penyiaran Layar & Suara)**.

---

## 1. Ringkasan & Keputusan Desain (Design Decisions)
Mengintegrasikan **MediaProjection** (perekaman layar) dan **RootEncoder (pedroSG94)** ke dalam Foreground Service (**`StreamService`**) untuk mengirimkan sinyal video/audio RTMPS ke Facebook Live secara langsung dari perangkat pengguna.

### Keputusan Teknis Utama (Hasil /grill-me):
- **Dua Mode Penyiaran di Beranda**:
  1. **Seluruh Layar (*Entire Screen*)**: Menyiarkan seluruh aktivitas layar ponsel secara bebas tanpa sensor jeda privasi.
  2. **Aplikasi Tertentu (*Specific Application*)**: Menyiarkan hanya aplikasi tertentu yang dipilih oleh host (misal Spin Wheel / Browser Arisan) dengan perlindungan privasi otomatis.
- **Pemilih Aplikasi Internal (`AppPickerBottomSheet`)**:
  - Ditampilkan saat host memilih mode "Aplikasi" di Beranda atau menekan "Ganti Aplikasi" di layar status.
  - Menampilkan daftar aplikasi berpeluncur (*Launcher Apps*) yang terpasang di ponsel dengan ikon resmi dan nama aplikasi.
  - Dilengkapi **Kolom Pencarian (*Search Bar*)** untuk mencari aplikasi dengan cepat.
  - Menyimpan aplikasi terakhir yang dipilih di `PreferencesManager` agar host tidak perlu mencari ulang setiap kali live mingguan.
- **Izin Akses Penggunaan (`PACKAGE_USAGE_STATS`) Diminta di Awal**:
  - Dimasukkan ke layar **`PermissionsOverviewScreen`** bersama izin Mikrofon, Notifikasi, dan Overlay.
  - Tombol "Beri Izin" langsung mengarahkan pengguna ke halaman pengaturan Akses Penggunaan sistem Android.
- **Peluncuran Otomatis (*Auto-Launch*)**:
  - Saat live berhasil terhubung pada mode Aplikasi Tertentu, Live Studio langsung otomatis membuka aplikasi target ke layar depan agar host tidak perlu repot mencari ikon di layar Home.
- **Sensor Privasi Otomatis (*Privacy Guard*)**:
  - `StreamService` memantau aplikasi yang sedang aktif di layar depan (*foreground*) melalui `UsageStatsManager` setiap 500ms.
  - Jika host berada di dalam aplikasi target -> layar aplikasi tampil penuh.
  - Jika host keluar dari aplikasi target (ke Home, membuka WhatsApp, galeri, atau melihat notifikasi) -> video siaran otomatis ditutupi lapisan `ImageObjectFilterRender` (*"Siaran Layar Dijeda - Host sedang di luar [Nama Aplikasi]"*) tanpa memutus `ScreenSource` atau membuat ulang `VirtualDisplay` (aman terhadap batasan `MediaProjection` Android 14+).
  - **Suara Mikrofon Tetap Aktif**: Saat layar dijeda, audio mikrofon tetap tersiar agar host tetap dapat berbicara dan menyapa penonton, sementara suara internal aplikasi lain dibisukan agar suara notifikasi/chat tidak bocor.
  - Saat host kembali membuka aplikasi target -> lapisan jeda otomatis dilepas dan video kembali menyiarkan layar aplikasi.
- **Ganti Aplikasi di Tengah Siaran**:
  - Host dapat menekan tombol **"Ganti Aplikasi / Mode Layar"** di `StreamStatusScreen` atau tombol **"Ganti Aplikasi"** di notifikasi untuk memilih aplikasi lain (atau beralih ke Seluruh Layar) tanpa memutus siaran Facebook Live.
- **Kualitas Video Dinamis**: 
  - *Adaptive Bitrate Otomatis*: Mengatur bitrate secara real-time saat bandwidth jaringan naik/turun (`BitrateAdapter`).
  - *Profil Kualitas di Beranda*:
    1. **Otomatis / Standar (Default)**: 720x1280 (720p Portrait) @ 30fps, base bitrate 2500 kbps (2.5 Mbps).
    2. **Hemat Data**: 480x854 (480p Portrait) @ 30fps, base bitrate 1200 kbps (1.2 Mbps).
    3. **Kualitas Tinggi**: 1080x1920 (1080p Portrait) @ 30fps, base bitrate 4000 kbps (4.0 Mbps).
- **Penanganan Audio**:
  - *Default*: Mikrofon **ON** dan Suara Aplikasi **ON** (keduanya aktif sejak detik pertama).
  - *Kontrol Mute Terpisah*: Pengguna dapat mematikan/menyalakan (mute/unmute) Mikrofon dan Suara Aplikasi secara independen kapan saja selama live berlangsung.
  - *Kompatibilitas Android 7–9*: Suara aplikasi otomatis dinonaktifkan (`AudioPlaybackCapture` hanya ada di Android 10 / API 29+).
- **Strategi Reconnect Otomatis**:
  - Jika koneksi RTMPS terputus di tengah siaran, engine otomatis mencoba menyambung ulang (*auto-reconnect*) hingga **5 kali percobaan** dengan jeda waktu 3 detik per percobaan.

---

## 2. Checklis Pengerjaan Per File

### Service, Engine & Data Layer

- [x] **[NEW] `core/service/NotificationHelper.kt`**
  - Membuat Notification Channel `live_stream_channel`.
  - Fungsi `buildNotification` dengan tombol aksi **"Stop Live"** dan **"Ganti Aplikasi"**.

- [x] **[REVISI] `service/StreamService.kt`**
  - Service ber-anotasi `@AndroidEntryPoint`, dideklarasikan dengan `foregroundServiceType="mediaProjection|microphone"`.
  - Menerima parameter `shareMode` (Seluruh Layar atau Aplikasi Tertentu beserta `targetPackage` dan `targetAppName`).
  - Menjalankan `startForegroundAppMonitor()` (coroutine loop mengecek `UsageStatsManager` setiap 500ms saat mode Aplikasi Tertentu).
  - Mengelola lapisan privasi `ImageObjectFilterRender` saat pengguna keluar dari aplikasi target tanpa merusak sesi `VirtualDisplay` Android 14+.
  - Mikrofon tetap aktif saat video dijeda demi privasi; audio internal dibisukan sementara.
  - Aksi `ACTION_CHANGE_TARGET_APP` untuk mengganti aplikasi yang dipantau atau beralih ke Seluruh Layar saat live berjalan.

- [x] **[NEW] `data/streaming/StreamEngineController.kt`**
  - Singleton manager penyedia `StateFlow<StreamStatus>`.
  - Kontrol start, stop, mute mic, mute internal audio, pause/resume screen, ganti target aplikasi (`changeShareTarget`).

- [x] **[NEW] `core/util/AppHelper.kt` & [REVISI] `core/util/PermissionHelper.kt`**
  - Fungsi mengambil daftar aplikasi berpeluncur (`Launcher Apps`) yang terpasang di HP.
  - Fungsi memeriksa izin `PACKAGE_USAGE_STATS` (`PermissionHelper.hasUsageAccess(context)`).
  - Fungsi mendeteksi package aplikasi di layar depan (`AppHelper.getForegroundPackageName`).
  - Fungsi meluncurkan aplikasi target ke layar depan (`AppHelper.launchApp`).

- [x] **[REVISI] `data/local/PreferencesManager.kt`**
  - Menambahkan key `last_share_mode`, `last_target_app_pkg`, dan `last_target_app_name` untuk mengingat mode dan aplikasi arisan yang terakhir dipilih.

---

### Domain Layer

- [x] **[NEW] `domain/model/ShareMode.kt`**
  - Enum `ShareModeType` (`ENTIRE_SCREEN`, `SPECIFIC_APP`) dan data class `TargetApp(packageName, appName)`.

- [x] **[NEW] `domain/model/StreamStatus.kt`**
  - Status `Idle`, `Connecting`, `Streaming`, `ScreenPaused`, `Reconnecting`, `Stopped`, `Error` beserta informasi `shareMode` dan `targetApp`.

- [x] **[NEW] `domain/model/VideoQualityProfile.kt`**
  - Enum `AUTO_720P`, `SAVER_480P`, `HIGH_1080P`.

- [x] **[REVISI] `domain/repository/StreamRepository.kt` & `StreamRepositoryImpl.kt`**
  - Mendukung parameter `shareMode` dan `targetApp` pada `startStream()`.
  - Menambahkan fungsi `changeShareTarget(shareMode, targetApp)` dan penyimpanan preferensi berbagi.

---

### UI Layer & Navigation

- [x] **[REVISI] `ui/screens/permissions/PermissionsOverviewScreen.kt`**
  - Menambahkan kartu Izin **Deteksi Aplikasi (Sensor Privasi)** (`PACKAGE_USAGE_STATS`) yang membuka halaman pengaturan Akses Penggunaan sistem.

- [x] **[NEW] `ui/screens/home/components/AppPickerBottomSheet.kt`**
  - Modal Bottom Sheet untuk memilih aplikasi:
    - Kolom pencarian (*Search Bar*) dengan filter instan.
    - Daftar vertikal dengan ikon resmi dan nama aplikasi.
    - Indikator aplikasi yang terakhir digunakan.
    - Opsi "Seluruh Layar" saat dibuka dari layar status siaran.

- [x] **[REVISI] `ui/screens/home/HomeScreen.kt` & `HomeViewModel.kt`**
  - Menambahkan pemilih Mode Berbagi Layar:
    - Kartu pilihan: **Seluruh Layar** vs **Aplikasi**.
    - Jika Aplikasi dipilih, menampilkan kartu aplikasi terpilih (beserta ikon dan tombol **Ganti**) yang membuka `AppPickerBottomSheet`.
  - Saat live dimulai dan terhubung: otomatis meluncurkan aplikasi target ke layar depan.

- [x] **[REVISI] `ui/screens/status/StreamStatusScreen.kt` & `StreamStatusViewModel.kt`**
  - Menampilkan aplikasi/mode yang sedang disiarkan.
  - Tombol **"Buka [Nama Aplikasi] Sekarang"** untuk langsung kembali ke aplikasi target.
  - Tombol **"Ganti Aplikasi / Mode Layar"** membuka `AppPickerBottomSheet` untuk memilih aplikasi target baru atau beralih ke Seluruh Layar tanpa memutus siaran.

---

### Konfigurasi AndroidManifest

- [x] **[REVISI] `AndroidManifest.xml`**
  - Menambahkan `<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" />`.
  - Menambahkan `<queries>` untuk intent `MAIN` + `LAUNCHER` agar daftar aplikasi terpasang dapat dibaca di Android 11+.
  - Deklarasi `<service android:name=".service.StreamService" android:foregroundServiceType="mediaProjection|microphone" android:exported="false" />`.

---

## 3. Verifikasi E3
- [ ] Izin Deteksi Aplikasi (`PACKAGE_USAGE_STATS`) tampil dan dapat diaktifkan dari `PermissionsOverviewScreen`.
- [ ] Di Beranda, host dapat memilih antara **"Seluruh Layar"** atau **"Aplikasi"**.
- [ ] Pada mode Aplikasi, bottom sheet menampilkan daftar aplikasi berpeluncur dengan kolom pencarian dan mengingat aplikasi terakhir.
- [ ] Saat live terhubung pada mode Aplikasi, aplikasi target otomatis terbuka ke layar depan.
- [ ] Saat host keluar dari aplikasi target (ke Home atau buka aplikasi lain seperti WhatsApp), siaran otomatis menampilkan gambar jeda privasi, sementara audio mikrofon tetap tersiar.
- [ ] Saat host kembali ke aplikasi target, siaran video otomatis menyiarkan kembali layar aplikasi.
- [ ] Host dapat mengganti aplikasi yang dibagikan melalui tombol "Ganti Aplikasi" di layar status dan notifikasi tanpa memutus sesi live Facebook.
