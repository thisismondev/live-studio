# Rencana Eksekusi: E2 - Fitur Persiapan Siaran (Prepare Live)

Dokumen ini berisi rencana teknis per file untuk tahapan **Persiapan Siaran (Prepare Live)**.

---

## 1. Ringkasan & Keputusan Desain (Design Decisions)
Mengimplementasikan alur persiapan siaran sebelum streaming dimulai, meliputi:
- **Alur Izin (`PermissionsOverviewScreen`)**: Ditampilkan satu kali setelah login sebelum masuk ke Beranda. Menjelaskan kebutuhan izin Audio, Notifikasi, dan Tampil di Atas Aplikasi Lain (Overlay). Izin Perekaman Layar (*MediaProjection*) tetap dipicu oleh sistem saat tombol "Mulai Live" diklik.
- **Formulir Siaran (`HomeScreen`)**: Input judul siaran (validasi wajib diisi) dan pemilihan privasi (**Publik** / `EVERYONE` atau **Teman** / `ALL_FRIENDS`). Mengingat judul dan opsi privasi terakhir dari DataStore Preferences (`PreferencesManager`) untuk kemudahan admin arisan.
- **Header Profil Pengguna**: Menampilkan foto profil, nama pengguna dari sesi login Facebook, serta tombol Logout.
- **Integrasi Facebook Live Video API**: Memanggil endpoint `POST /me/live_videos` via `GraphRequest` Facebook SDK untuk mendapatkan `secure_stream_url` (URL RTMPS) dan `id` (`live_video_id`).
- **Penanganan Syarat Akun Facebook (`AccountRequirementBottomSheet`)**: Ditampilkan dalam bentuk Modal Bottom Sheet interaktif di Beranda jika akun Facebook ditolak (misal: belum mengaktifkan Mode Profesional atau akun baru), dengan tombol "Coba Lagi" dan "Buka Facebook", tanpa menghapus teks judul yang sudah diketik pengguna.
- **Navigasi & Jembatan ke E3**: `AppNavigation.kt` mengelola alur `Login` -> `PermissionsOverview` -> `Home`. Tombol "Mulai Live" yang sukses akan memicu callback `onStartBroadcast(broadcastConfig)` dengan konfirmasi dialog siap sambung ke engine streaming di E3.

---

## 2. Checklis Pengerjaan Per File

### Data Layer

- [x] **[NEW] `data/remote/FacebookLiveApi.kt`**
  - Menggunakan `GraphRequest` Facebook SDK dengan token dari sesi aktif.
  - Endpoint `POST /me/live_videos` dengan parameter:
    - `title`: Judul siaran.
    - `description`: Deskripsi siaran (otomatis diselaraskan dengan judul).
    - `privacy`: JSON string `{"value": "EVERYONE"}` atau `{"value": "ALL_FRIENDS"}`.
    - `status`: `"LIVE_NOW"`.
  - Mengembalikan `Result<Pair<String, String>>` berisi `(secure_stream_url, live_video_id)`.
  - Parsing kesalahan Graph API spesifik untuk mendeteksi syarat akun / mode profesional.

- [x] **[REVISI] `data/local/PreferencesManager.kt`**
  - Menambahkan key DataStore untuk `last_broadcast_title` dan `last_privacy_option`.
  - Menambahkan fungsi `saveLastBroadcastSettings(title: String, privacy: String)` dan `getLastBroadcastSettings(): Flow<Pair<String, String>>`.

- [x] **[NEW] `data/repository/LiveRepositoryImpl.kt`**
  - Mengimplementasikan `LiveRepository`.
  - Memanggil `FacebookLiveApi.createLiveVideo(...)`.
  - Penyimpanan judul & privasi dilakukan `HomeViewModel` lewat `SaveBroadcastSettingsUseCase` setelah siaran berhasil dibuat (bukan di repository).

---

### Domain Layer

- [x] **[NEW] `domain/model/PrivacyOption.kt`**
  - Enum `PUBLIC("EVERYONE", "Publik")` dan `FRIENDS("ALL_FRIENDS", "Teman")`.

- [x] **[NEW] `domain/model/BroadcastConfig.kt`**
  - Data class `title: String`, `privacy: PrivacyOption`, `rtmpsUrl: String`, `liveVideoId: String`.

- [x] **[NEW] `domain/repository/LiveRepository.kt`**
  - Interface contract:
    - `suspend fun createLiveStream(title: String, privacy: PrivacyOption): Result<BroadcastConfig>`
    - `suspend fun saveLastSettings(title: String, privacy: PrivacyOption)`
    - `fun getLastSettings(): Flow<Pair<String, PrivacyOption>>`

- [x] **[NEW] `domain/usecase/CreateLiveStreamUseCase.kt`**
  - Validasi judul siaran (tidak boleh kosong/hanya spasi).
  - Mengeksekusi `liveRepository.createLiveStream(...)`.

- [x] **[NEW] `domain/usecase/GetBroadcastSettingsUseCase.kt` & `SaveBroadcastSettingsUseCase.kt`**
  - UseCase untuk membaca dan menyimpan judul serta privasi terakhir.

---

### UI Layer & Navigation

- [x] **[NEW] `ui/navigation/Screen.kt` & `ui/navigation/AppNavigation.kt`**
  - NavHost Compose mengatur rute:
    - `Screen.Login`
    - `Screen.Permissions`
    - `Screen.Home`
  - Mengecek sesi login awal: jika sudah login, cek status izin -> jika izin lengkap langsung ke `Home`, jika belum ke `Permissions`.

- [x] **[NEW] `ui/screens/permissions/PermissionsOverviewScreen.kt`**
  - Layar edukasi izin Material 3 (Audio, Notifikasi, Overlay) dengan bahasa ramah awam.
  - Kartu status izin interaktif dan tombol aksi "Beri Izin / Lanjut ke Beranda".

- [x] **[NEW] `ui/screens/home/HomeUiState.kt` & `ui/screens/home/HomeViewModel.kt`**
  - StateFlow `HomeUiState` (`title`, `privacy`, `isLoading`, `errorMessage`, `showRequirementSheet`, `createdBroadcast: BroadcastConfig?`).
  - Fungsi input: `onTitleChanged(String)`, `onPrivacySelected(PrivacyOption)`, `onStartLiveClicked()`, `onDismissError()`.

- [x] **[NEW] `ui/screens/home/HomeScreen.kt`**
  - Header profil admin (avatar bulat, nama, dan tombol Logout).
  - Input teks judul siaran (`OutlinedTextField`, keyboard actions, IME padding).
  - Pilihan radio button / segmented chip untuk Privasi (Publik / Teman).
  - Tombol aksi utama "Mulai Live" dengan state Loading.
  - Dialog konfirmasi sukses "Siaran Siap Dimulai" menampilkan URL RTMPS untuk verifikasi E2.

- [x] **[NEW] `ui/screens/home/components/AccountRequirementBottomSheet.kt`**
  - Modal Bottom Sheet panduan langkah aktivasi Mode Profesional Facebook dengan tombol "Coba Lagi" dan intent buka aplikasi Facebook.

---

### Dependency Injection (DI)

- [x] **[REVISI] `di/AppModule.kt`**
  - Bind `LiveRepository` ke `LiveRepositoryImpl`.

---

> **Catatan implementasi:** kode E2 telah selesai diimplementasikan, dikompilasi, dan diverifikasi di Android Studio.
> - Avatar profil memakai huruf inisial (belum ada library gambar seperti Coil).
> - Izin wajib sebelum Beranda: Mikrofon dan Overlay. Notifikasi opsional.
> - Dialog sukses menyamarkan stream key pada URL RTMPS.
> - `clearUserSession()` kini hanya menghapus kunci sesi, sehingga judul dan privasi terakhir tetap tersimpan setelah logout.
## 3. Verifikasi E2
- [x] Navigasi bekerja lancar: Pengguna login diarahkan ke Layar Izin (jika belum lengkap), lalu masuk ke Beranda.
- [x] Input judul tervalidasi dengan baik (tombol Mulai Live nonaktif atau memvalidasi jika judul kosong).
- [x] Pilihan privasi (Publik/Teman) dan judul siaran tersimpan serta dimuat kembali saat aplikasi dibuka ulang.
- [x] Pemanggilan API Facebook `POST /me/live_videos` berhasil mengembalikan URL RTMPS valid (`rtmps://...`) dan `live_video_id` nyata dari Facebook.
- [x] Penanganan error syarat akun / token kadaluwarsa menampilkan panduan ramah tanpa membuat crash aplikasi.
