# Rencana Eksekusi: E1 — Fitur Login Facebook via Firebase Auth

Dokumen ini berisi rencana teknis per file untuk tahapan **Fitur Login Facebook & Manajemen Sesi via Firebase Auth**.

---

## 1. Ringkasan & Target
Mengimplementasikan alur autentikasi pengguna menggunakan **Firebase Auth** yang terintegrasi dengan **Facebook Login SDK** (`publish_video` permission scope). Pengguna yang berhasil login akan memiliki identitas terpusat di **Firebase Auth (Firebase UID)**, dan aplikasi tetap mendapatkan **Facebook Access Token** untuk digunakan pada pemanggilan Facebook Live Graph API. Sesi dikelola secara presisten menggunakan Jetpack DataStore Preferences, serta menyediakan alur Logout dan penanganan token kedaluwarsa.

---

## 2. Checklis Pengerjaan Per File

### Data Layer

- [ ] **[NEW] `data/local/PreferencesManager.kt`**
  - Mengelola `DataStore<Preferences>`.
  - Fungsi untuk menyimpan & membaca sesi pengguna:
    - `saveUserSession(userSession: UserSession)`
    - `getUserSession(): Flow<UserSession?>`
    - `clearUserSession()`

- [ ] **[NEW] `data/repository/AuthRepositoryImpl.kt`**
  - Mengimplementasikan `AuthRepository`.
  - Mengintegrasikan `LoginManager.getInstance()` dari Facebook SDK untuk mendapatkan `AccessToken`.
  - Menukar `AccessToken` ke Firebase Auth via `FirebaseAuth.getInstance().signInWithCredential(FacebookAuthProvider.getCredential(token))`.
  - Menyimpan `UserSession` (Firebase UID & Facebook AccessToken) ke `PreferencesManager`.

---

### Domain Layer

- [ ] **[NEW] `domain/model/UserSession.kt`**
  - Data class: `firebaseUid: String`, `facebookUserId: String`, `userName: String`, `userPhotoUrl: String?`, `facebookAccessToken: String`, `tokenExpirationTimestamp: Long`.

- [ ] **[NEW] `domain/repository/AuthRepository.kt`**
  - Interface contract:
    - `fun getLoginState(): Flow<UserSession?>`
    - `suspend fun loginWithFacebook(activity: Activity): Result<UserSession>`
    - `suspend fun logout(): Result<Unit>`

- [ ] **[NEW] `domain/usecase/LoginFacebookUseCase.kt`**
  - Class yang mengeksekusi `authRepository.loginWithFacebook(activity)`.

- [ ] **[NEW] `domain/usecase/GetLoginStateUseCase.kt`**
  - Class yang mengembalikan `Flow<UserSession?>` dari `authRepository.getLoginState()`.

- [ ] **[NEW] `domain/usecase/LogoutUseCase.kt`**
  - Class yang mengeksekusi `authRepository.logout()`.

---

### UI Layer

- [ ] **[NEW] `ui/screens/login/LoginViewModel.kt`**
  - Mengelola `StateFlow<LoginUiState>` (`Idle`, `Loading`, `Success`, `Error`).
  - Fungsi `onLoginClicked(activity: Activity)` dan `onLogoutClicked()`.

- [ ] **[NEW] `ui/screens/login/LoginScreen.kt`**
  - Layar Jetpack Compose (Edge-to-Edge, Portrait).
  - Elemen UI: Logo placeholder, Judul "Live Studio", Sub-judul "Siarkan Undian Arisan dengan 1 HP", Tombol Primary Indigo "Masuk dengan Facebook".
  - Penanganan state Loading & Error Dialog / Snackbar.

---

## 3. Verifikasi E1
- Lakukan pengujian login Facebook di emulator/device dengan Facebook Tester Account.
- Verifikasi bahwa pengguna terautentikasi di Firebase Auth (`FirebaseUser` tersedia) dan `UserSession` tersimpan di DataStore.
- Verifikasi bahwa sesi tersimpan saat aplikasi ditutup dan dibuka kembali.
