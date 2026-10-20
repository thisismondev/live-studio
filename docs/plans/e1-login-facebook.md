# Rencana Eksekusi: E1 - Fitur Login Facebook via Facebook Login SDK

> **Status: [x] Selesai.** Implementasi autentikasi menggunakan **Facebook Login SDK** telah selesai diimplementasikan, diverifikasi, dan diuji langsung pada emulator Android Studio.

Dokumen ini berisi rencana teknis per file untuk tahapan **Fitur Login Facebook & Manajemen Sesi via Facebook Login SDK**.

---

## 1. Ringkasan & Target
Mengimplementasikan autentikasi pengguna menggunakan **Facebook Login SDK** (`LoginManager` + `CallbackManager`) dengan permission `public_profile` dan `publish_video`. Aplikasi memperoleh **long-lived Facebook Access Token** dengan masa berlaku asli (`AccessToken.expirationTime`) untuk memanggil Facebook Live Video Graph API.

Keputusan desain:
- **Identitas tunggal**: `facebookUserId`. Tidak ada `firebaseUid`. Firebase tetap terpasang di proyek (Google Services + BoM) untuk fitur pasca-MVP, tetapi **Firebase Auth tidak dipakai**.
- **Token terenkripsi**: token disimpan di penyimpanan terenkripsi berbasis Android Keystore (`SecureTokenStorage`), bukan DataStore polos. DataStore hanya menyimpan data profil non-sensitif (nama, foto, `facebookUserId`, dan expiry token).
- **Expiry asli**: `tokenExpiresAt` diambil dari `AccessToken.expirationTime`, tidak dihitung manual. Saat token hampir/sudah kedaluwarsa, aplikasi memanggil refresh (`AccessToken.refreshCurrentAccessToken`) atau meminta login ulang.
- **Backup**: `allowBackup="true"` tetap dipertahankan, tetapi berkas token **dikecualikan** dari backup dan device transfer pada `backup_rules.xml` dan `data_extraction_rules.xml`.
- **App Review**: izin `publish_video` memerlukan Facebook App Review. Pengajuan dilakukan paralel sejak E1 selesai (lihat [E7](e7-kepatuhan-rilis.md)).

---

## 2. Checklis Pengerjaan Per File

### Data Layer

- [x] **`data/local/PreferencesManager.kt`**
  - Mengelola `DataStore<Preferences>` untuk data profil non-sensitif (`userName`, `userPhotoUrl`, `facebookUserId`, `tokenExpiresAt`).
  - Menggabungkan token dari `SecureTokenStorage` untuk membentuk `UserSession`.

- [x] **`data/local/SecureTokenStorage.kt`**
  - Menyimpan dan membaca Facebook access token terenkripsi (AES/GCM/NoPadding, Android Keystore).
  - Berkas disimpan di `context.noBackupFilesDir` sehingga aman dari backup/transfer.

- [x] **`data/repository/AuthRepositoryImpl.kt`**
  - Mengimplementasikan `AuthRepository`.
  - Menggunakan `LoginManager.getInstance().logIn` dengan `CallbackManager` untuk request permission `public_profile` dan `publish_video`.
  - Mengambil data profil via `GraphRequest.newMeRequest`, menyimpan token ke `SecureTokenStorage` dan profil ke `PreferencesManager`.
  - Menyediakan `refreshTokenIfNeeded()` dengan `AccessToken.refreshCurrentAccessTokenAsync`.

---

### Domain Layer

- [x] **`domain/model/UserSession.kt`**
  - Data class: `facebookUserId: String`, `userName: String`, `userPhotoUrl: String?`, `facebookAccessToken: String`, `tokenExpiresAt: Long`.

- [x] **`domain/repository/AuthRepository.kt`**
  - Interface contract:
    - `fun getLoginState(): Flow<UserSession?>`
    - `suspend fun loginWithFacebook(activity: Activity): Result<UserSession>`
    - `suspend fun refreshTokenIfNeeded(): Result<UserSession>`
    - `suspend fun logout(): Result<Unit>`

- [x] **`domain/usecase/LoginFacebookUseCase.kt`**
  - Mengeksekusi `authRepository.loginWithFacebook(activity)`.

- [x] **`domain/usecase/GetLoginStateUseCase.kt`**
  - Mengembalikan `Flow<UserSession?>` dari `authRepository.getLoginState()`.

- [x] **`domain/usecase/LogoutUseCase.kt`**
  - Mengeksekusi `authRepository.logout()` (menghapus token terenkripsi, membersihkan DataStore, dan `LoginManager.logOut()`).

---

### UI Layer

- [x] **`ui/screens/login/LoginUiState.kt`**
  - Sealed interface `LoginUiState` (`Idle`, `Loading`, `Success(val userSession: UserSession)`, `Error(val message: String)`).

- [x] **`ui/screens/login/LoginViewModel.kt`**
  - Mengelola `StateFlow<LoginUiState>`.
  - Memanggil `onLoginClicked(activity: Activity)` dan `onLogoutClicked()`.

- [x] **`ui/screens/login/LoginScreen.kt`**
  - Layar Jetpack Compose (Edge-to-Edge, Portrait).
  - Elemen UI: Logo placeholder, Judul "Live Studio", Sub-judul "Siarkan Undian Arisan dengan 1 HP", Tombol "Masuk dengan Facebook".
  - Penanganan state Loading & Error Dialog / Snackbar.

---

### Konfigurasi Pendukung

- [x] **`AndroidManifest.xml`**
  - Meta-data `com.facebook.sdk.ApplicationId` dan `com.facebook.sdk.ClientToken`.
  - Registrasi `com.facebook.CustomTabActivity` dan queries package `com.facebook.katana`.

- [x] **`backup_rules.xml` & `data_extraction_rules.xml`**
  - Pengecualian berkas `fb_token.enc` dan folder `no_backup` dari cloud backup dan device transfer.

---

## 3. Verifikasi E1
- [x] Login Facebook berhasil di emulator Android Studio dengan akun developer.
- [x] `UserSession` tersimpan, token berada di penyimpanan terenkripsi Keystore (`fb_token.enc`).
- [x] `tokenExpiresAt` tercatat sesuai expiry asli Facebook SDK.
- [x] Sesi login bertahan saat aplikasi dibuka ulang.
- [x] Berkas token aman dan tidak ikut ter-backup.
