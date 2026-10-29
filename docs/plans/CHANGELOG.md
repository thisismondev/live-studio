# Changelog Rencana Eksekusi Live Studio

## [E3] Core Streaming Engine (Penyiaran Layar & Suara) - Selesai
*Tanggal & Waktu: 21 Oktober 2026, 12:45 WIB*

- **Streaming Engine**:
  - Mengintegrasikan RootEncoder 2.8.1 (`GenericStream`, `ScreenSource`, `MixAudioSource`, `BitrateAdapter`).
  - Menyiarkan layar HP secara mulus ke Facebook Live dengan adaptive bitrate dinamis dan auto-reconnect (hingga 5x percobaan).
  - Kontrol audio independen: sakelar mute mikrofon dan mute audio aplikasi internal (Android 10+).
- **Mode Berbagi Layar (Seluruh Layar vs Aplikasi)**:
  - Dua opsi penyiaran di Beranda: **Seluruh Layar** (*Entire Screen*) dan **Aplikasi** (*Specific App*).
  - *In-App App Picker Bottom Sheet*: daftar aplikasi berpeluncur (*Launcher Apps*) dengan kolom pencarian instan (*Search Bar*), ikon resmi, dan riwayat aplikasi terakhir via DataStore.
- **Sensor Privasi Otomatis (*Privacy Guard*)**:
  - Pemantauan foreground package via `UsageStatsManager` (`PACKAGE_USAGE_STATS`) setiap 500ms.
  - Saat host keluar dari aplikasi target (membuka WhatsApp, chat pribadi, galeri, atau ke Home): video otomatis ditutup lapisan OpenGL `ImageObjectFilterRender` (*"Siaran Layar Dijeda"*) tanpa merusak sesi `VirtualDisplay` Android 14+.
  - Mikrofon tetap aktif menyiarkan suara host saat layar dijeda; audio internal aplikasi lain otomatis dibisukan agar suara chat tidak bocor.
  - Lapisan jeda dilepas seketika (<16ms) begitu host kembali membuka aplikasi target.
  - Menggunakan *in-memory cached bitmap* untuk mencegah alokasi memori berlebih saat jeda/lanjut berulang kali.
- **Alur Peluncuran & Kontrol Siaran**:
  - Auto-launch aplikasi target ke layar depan saat live terhubung.
  - Layar status `StreamStatusScreen` dengan penanda LIVE, durasi JJ:MM:DD, indikator bitrate, kontrol audio, tombol buka aplikasi, dan tombol Stop.
  - Tombol "Ganti Aplikasi / Mode Layar" di layar status dan notifikasi memungkinkan host berganti aplikasi di tengah live tanpa memutus sesi Facebook Live.
- **Perizinan & Kepatuhan**:
  - Menambahkan kartu izin "Deteksi Aplikasi (Sensor Privasi)" di `PermissionsOverviewScreen`.
  - Deklarasi `<queries>` intent Launcher di `AndroidManifest.xml` untuk kompatibilitas package visibility Android 11+.

---
## [E2] Fitur Persiapan Siaran (Prepare Live) - Selesai
*Tanggal & Waktu: 21 Oktober 2026, 01:38 WIB*

- Domain Layer: pembuatan model `PrivacyOption.kt`, `BroadcastConfig.kt`, hirarki exception `LiveErrors.kt`, interface `LiveRepository.kt`, serta UseCases (`CreateLiveStreamUseCase`, `GetBroadcastSettingsUseCase`, `SaveBroadcastSettingsUseCase`).
- Data Layer: implementasi `FacebookLiveApi.kt` (`POST /me/live_videos` via GraphRequest Facebook SDK), `LiveRepositoryImpl.kt`, serta penambahan penyimpanan judul & privasi terakhir ke DataStore pada `PreferencesManager.kt`.
- UI Layer: pembuatan alur navigasi Compose `AppNavigation.kt`, layar edukasi izin `PermissionsOverviewScreen.kt` dengan `PermissionHelper`, Beranda `HomeScreen.kt` + `HomeViewModel.kt`, dan `AccountRequirementBottomSheet.kt` untuk penanganan akun non-profesional.
- Pembaruan `MainActivity.kt` menggunakan `AppNavigation` terpusat.
- DI: Registrasi `LiveRepository` di `di/AppModule.kt`.
- Pengujian dan verifikasi berhasil dilakukan di Android Studio.

---
## [E1/Facebook SDK] Migrasi ke Facebook Login SDK & Verifikasi Emulator - Selesai
*Tanggal & Waktu: 21 Oktober 2026, 01:00 WIB*

- Hapus seluruh dependensi `firebase-auth`. Pertahankan Firebase BoM `35.0.2` dan plugin `google-services` untuk fitur pasca-MVP.
- Tambahkan library `com.facebook.android:facebook-login:18.3.0` dan konfigurasi pembacaan Facebook App ID & Client Token dari `local.properties` via `resValue`.
- Implementasi `SecureTokenStorage.kt` berbasis Android Keystore (AES-GCM) dengan file di `noBackupFilesDir`.
- Update `PreferencesManager.kt` untuk hanya menyimpan data non-sensitif (`facebookUserId`, `userName`, `userPhotoUrl`, `tokenExpiresAt`).
- Implementasi `AuthRepositoryImpl.kt` dengan `LoginManager` (scopes: `public_profile`, `publish_video`), `GraphRequest` untuk profil, dan `refreshTokenIfNeeded`.
- Konfigurasi `AndroidManifest.xml` (meta-data Facebook, `CustomTabActivity`, `<queries>` paket Facebook).
- Pengecualian berkas token di `backup_rules.xml` dan `data_extraction_rules.xml`.
- Pengujian langsung di emulator Android Studio dengan akun developer berhasil terverifikasi.

---
## [Docs] Revisi Rencana: Ganti Firebase Auth ke Facebook Login SDK, Tambah E7 - Rencana (belum ada perubahan kode)
*Tanggal & Waktu: 20 Oktober 2026, 23:56 WIB*

- E1 dibuka kembali (`[ ] Direvisi`): login memakai Facebook Login SDK, bukan Firebase Auth. Identitas tunggal `facebookUserId`, `tokenExpiresAt` dari `AccessToken.expirationTime`, token disimpan terenkripsi (`SecureTokenStorage`), refresh token ditambahkan.
- E0, ADR, dan docs README disesuaikan: `firebase-auth` dihapus dari rencana; Google Services plugin, `google-services.json`, dan Firebase BoM tetap dipasang untuk fitur pasca-MVP. Facebook Login SDK ditambahkan.
- Kebijakan backup: `allowBackup="true"` dipertahankan, berkas token dikecualikan dari backup dan device transfer.
- PRD Parking Lot: Crashlytics/Analytics dicatat sebagai pasca-MVP.
- Epic baru E7 (`e7-kepatuhan-rilis.md`): Facebook App Review paralel sejak E1 selesai, kepatuhan Google Play mendekati rilis.
- Kode belum diubah; entri E0/E1 di bawah mencatat implementasi lama berbasis Firebase Auth.

---
## [E0/Hilt] Integrasi Dagger Hilt & Update E0 — Selesai
*Tanggal & Waktu: 6 Oktober 2026, 16:01 WIB*

- Integrasi Dagger Hilt `2.60.1` & KSP Plugin `2.0.21-1.0.28` pada Version Catalog, `build.gradle.kts` (root), dan `app/build.gradle.kts`.
- Pembuatan kelas Application `LiveStudioApp.kt` (`@HiltAndroidApp`) dan pendaftaran di `AndroidManifest.xml`.
- Pembuatan Hilt Module `di/AppModule.kt` (`@Module`, `@InstallIn`) untuk menyediakan singleton dependencies (`FirebaseAuth`, `PreferencesManager`, `AuthRepository`, dan UseCases).
- Penambahan anotasi `@AndroidEntryPoint` pada `MainActivity.kt` dan `@HiltViewModel` pada `LoginViewModel.kt`.
- Penambahan flag `android.disallowKotlinSourceSets=false` di `gradle.properties`.
- Pembaruan dokumen `docs/plans/e0-setup-dan-konfigurasi.md` dan `docs/agile/ADR — Live Studio.md`.
- Pengujian build `./gradlew assembleDebug` berhasil dengan **BUILD SUCCESSFUL**.

---

## [E1] Fitur Login Facebook via Firebase Auth — Selesai
*Tanggal & Waktu: 6 Oktober 2026, 15:30 WIB*

- Pembaruan dokumen rencana eksekusi `docs/plans/e1-login-facebook.md` dengan alur Firebase Auth OAuth.
- Pembuatan Domain Layer: model `UserSession.kt`, interface contract `AuthRepository.kt`, serta UseCases (`LoginFacebookUseCase.kt`, `GetLoginStateUseCase.kt`, `LogoutUseCase.kt`).
- Pembuatan Data Layer: `PreferencesManager.kt` (manajemen DataStore Preferences) dan `AuthRepositoryImpl.kt` (integrasi `FirebaseAuth.OAuthProvider("facebook.com")`).
- Pembuatan UI Layer: `LoginUiState.kt`, `LoginViewModel.kt`, dan `LoginScreen.kt` (Jetpack Compose Screen M3, Edge-to-Edge).
- Pengujian build `./gradlew assembleDebug` berhasil dengan **BUILD SUCCESSFUL**.

---

## [E0] Setup & Konfigurasi Core Proyek — Selesai
*Tanggal & Waktu: 6 Oktober 2026, 15:20 WIB*

- Konfigurasi Gradle Version Catalog dengan Google Services Plugin, Firebase Auth, RootEncoder, CameraX, DataStore Preferences, Navigation Compose, dan Material Icons Extended.
- Integrasi repositori JitPack di `settings.gradle.kts`.
- Penambahan file konfigurasi `google-services.json` resmi Firebase.
- Konfigurasi `AndroidManifest.xml` (sistem izin, camera uses-feature, orientation portrait).
- Penambahan string UI Bahasa Indonesia pada `strings.xml`.
- Pembuatan kelas utilitas pembantu `Result.kt` dan `Constants.kt`.
- Penyiapan token warna dan tema Material 3 pada `Color.kt` dan `Theme.kt`.
- Pengujian build `./gradlew assembleDebug` berhasil dengan **BUILD SUCCESSFUL**.
