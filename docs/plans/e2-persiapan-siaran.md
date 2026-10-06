# Rencana Eksekusi: E2 — Fitur Persiapan Siaran (Prepare Live)

Dokumen ini berisi rencana teknis per file untuk tahapan **Persiapan Siaran (Prepare Live)**.

---

## 1. Ringkasan & Target
Mengimplementasikan form persiapan siaran (input judul siaran & opsi privasi Publik/Teman), pemanggilan Facebook Live Video API (`POST /me/live_videos`) via `GraphRequest` untuk mendapatkan URL RTMPS, penanganan kesalahan syarat akun (misal pengikut kurang dari 100 / akun baru), serta layar penjelasan izin aplikasi.

---

## 2. Checklis Pengerjaan Per File

### Data Layer

- [ ] **[NEW] `data/remote/FacebookLiveApi.kt`**
  - Menggunakan `GraphRequest` Facebook SDK.
  - Endpoint `POST /me/live_videos` dengan parameter:
    - `title`: Judul siaran.
    - `description`: Deskripsi siaran.
    - `privacy`: JSON string `{"value": "EVERYONE"}` atau `{"value": "ALL_FRIENDS"}`.
    - `status`: `"LIVE_NOW"`.
  - Mengembalikan URL RTMPS (`secure_stream_url`) dan `live_video_id` (`id`).

- [ ] **[NEW] `data/repository/LiveRepositoryImpl.kt`**
  - Mengimplementasikan `LiveRepository`.
  - Memanggil `FacebookLiveApi.createLiveVideo(...)`.
  - Menyimpan judul & privasi siaran terakhir ke `PreferencesManager`.

---

### Domain Layer

- [ ] **[NEW] `domain/model/PrivacyOption.kt`**
  - Enum `PUBLIC("EVERYONE", "Publik")` dan `FRIENDS("ALL_FRIENDS", "Teman")`.

- [ ] **[NEW] `domain/model/BroadcastConfig.kt`**
  - Data class `title: String`, `privacy: PrivacyOption`, `rtmpsUrl: String`, `liveVideoId: String`.

- [ ] **[NEW] `domain/repository/LiveRepository.kt`**
  - Interface contract:
    - `suspend fun createLiveStream(title: String, privacy: PrivacyOption): Result<BroadcastConfig>`
    - `suspend fun saveLastSettings(title: String, privacy: PrivacyOption)`
    - `fun getLastSettings(): Flow<Pair<String, PrivacyOption>>`

- [ ] **[NEW] `domain/usecase/CreateLiveStreamUseCase.kt`**
  - Class mengeksekusi validasi input judul (tidak boleh kosong) dan pemanggilan `liveRepository.createLiveStream(...)`.

- [ ] **[NEW] `domain/usecase/SaveBroadcastSettingsUseCase.kt` & `GetBroadcastSettingsUseCase.kt`**
  - UseCase untuk membaca & menyimpan judul serta privasi terakhir.

---

### UI Layer

- [ ] **[NEW] `ui/screens/permissions/PermissionsOverviewScreen.kt`**
  - Layar edukasi izin aplikasi (Perekaman Layar, Mikrofon, Kamera Depan, Overlay, Notifikasi) dengan bahasa awam & tombol "Berikan Izin".

- [ ] **[NEW] `ui/screens/home/HomeViewModel.kt`**
  - StateFlow `HomeScreenUiState` (`title: String`, `privacy: PrivacyOption`, `isLoading: Boolean`, `errorMessage: String?`).
  - Fungsi `onTitleChanged(String)`, `onPrivacySelected(PrivacyOption)`, `onStartLiveClicked()`.

- [ ] **[NEW] `ui/screens/home/HomeScreen.kt`**
  - Layar Beranda utama Jetpack Compose:
    - `OutlinedTextField` untuk Judul Siaran (dengan `imePadding()` / `adjustResize`).
    - Dropdown / Radio group pilihan Privasi (Publik / Teman).
    - Tombol utama Primary Indigo "Mulai Live".

- [ ] **[NEW] `ui/screens/error/AccountRequirementScreen.kt`**
  - Kartu penjelasan ramah bila akun belum memenuhi syarat Facebook Live (mode profesional, usia akun, pengikut).

---

## 3. Verifikasi E2
- Lakukan pengujian pembuatan siaran via Graph API dengan akun tester.
- Verifikasi bahwa URL RTMPS `rtmps://...` dan `live_video_id` berhasil didapatkan.
