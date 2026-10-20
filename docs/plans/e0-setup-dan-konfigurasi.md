# Rencana Eksekusi: E0 — Setup & Konfigurasi Core Proyek

Dokumen ini berisi rencana teknis per file untuk tahapan **Setup & Konfigurasi Core Proyek** sebagai fondasi utama aplikasi **Live Studio**.

---

## 1. Ringkasan & Target
Menyiapkan seluruh dependensi pustaka (termasuk **Dagger Hilt** untuk Dependency Injection), konfigurasi `build.gradle.kts`, deklarasi izin & meta-data pada `AndroidManifest.xml`, resource string Bahasa Indonesia, kelas `Application` (`@HiltAndroidApp`), DI Hilt Module (`AppModule.kt`), kelas pembantu core (`Result.kt` & `Constants.kt`), serta fondasi warna & tema Jetpack Compose Material 3 (`Color.kt` & `Theme.kt`).

---

## 2. Checklis Pengerjaan Per File

### Level 1: Project & Gradle Configuration

- [x] **[libs.versions.toml](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/gradle/libs.versions.toml)**
  - Tambahkan versi:
    - `googleGmsGoogleServices = "4.5.0"`
    - `firebaseBom` (Firebase BoM, tetap dipasang untuk fitur pasca-MVP)
    - `facebookLogin` (Facebook Login SDK)
    - `rootEncoder = "2.4.9"` (pedroSG94)
    - `camerax = "1.4.1"`
    - `datastore = "1.1.2"`
    - `navigationCompose = "2.8.7"`
    - `hilt = "2.60.1"`
    - `hiltNavigationCompose = "1.2.0"`
    - `ksp = "2.0.21-1.0.28"`
  - Deklarasikan plugin di section `[plugins]`:
    - `google-gms-google-services = { id = "com.google.gms.google-services", version.ref = "googleGmsGoogleServices" }`
    - `hilt-android = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }`
    - `ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }`
  - Deklarasikan pustaka pada section `[libraries]`:
    - `firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }`
    - `facebook-login = { group = "com.facebook.android", name = "facebook-login", version.ref = "facebookLogin" }`
    - `pedro-rootencoder = { group = "com.github.pedroSG94.RootEncoder", name = "library", version.ref = "rootEncoder" }`
    - `androidx-camera-camera2 = { group = "androidx.camera", name = "camera-camera2", version.ref = "camerax" }`
    - `androidx-camera-lifecycle = { group = "androidx.camera", name = "camera-lifecycle", version.ref = "camerax" }`
    - `androidx-camera-view = { group = "androidx.camera", name = "camera-view", version.ref = "camerax" }`
    - `androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }`
    - `androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }`
    - `androidx-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }`
    - `hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }`
    - `hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }`
    - `androidx-hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hiltNavigationCompose" }`

- [x] **[build.gradle.kts (Root)](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/build.gradle.kts)**
  - Tambahkan plugin `alias(libs.plugins.google.gms.google.services) apply false`, `alias(libs.plugins.hilt.android) apply false`, dan `alias(libs.plugins.ksp) apply false`.

- [x] **[settings.gradle.kts](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/settings.gradle.kts)**
  - Tambahkan repository `maven { url = uri("https://jitpack.io") }` di dalam `dependencyResolutionManagement.repositories`.

- [x] **[app/build.gradle.kts](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/app/build.gradle.kts)**
  - Apply plugin `alias(libs.plugins.google.gms.google.services)`, `alias(libs.plugins.ksp)`, dan `alias(libs.plugins.hilt.android)`.
  - Terapkan dependensi pustaka di section `dependencies`:
    - `implementation(platform(libs.firebase.bom))` (tanpa `firebase-auth`)
    - `implementation(libs.facebook.login)`
    - `implementation(libs.pedro.rootencoder)`
    - `implementation(libs.androidx.camera.camera2)`
    - `implementation(libs.androidx.camera.lifecycle)`
    - `implementation(libs.androidx.camera.view)`
    - `implementation(libs.androidx.datastore.preferences)`
    - `implementation(libs.androidx.navigation.compose)`
    - `implementation(libs.androidx.material.icons.extended)`
    - `implementation(libs.hilt.android)`
    - `ksp(libs.hilt.compiler)`
    - `implementation(libs.androidx.hilt.navigation.compose)`

- [x] **[app/google-services.json](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/app/google-services.json)**
  - Konfigurasi file `google-services.json` resmi Firebase.

---

### Level 2: Manifest & Core Resources

- [x] **[AndroidManifest.xml](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/app/src/main/AndroidManifest.xml)**
  - Mendaftarkan kelas `android:name=".LiveStudioApp"` di tag `<application>`.
  - Tambahkan izin sistem: `INTERNET`, `RECORD_AUDIO`, `CAMERA`, `SYSTEM_ALERT_WINDOW`, `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE` (types: mediaProjection, microphone, camera).
  - Konfigurasi `MainActivity`: `android:screenOrientation="portrait"`, `android:windowSoftInputMode="adjustResize"`.

- [x] **[strings.xml](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/app/src/main/res/values/strings.xml)**
  - Deklarasikan `app_name` ("Live Studio") dan seluruh teks UI Bahasa Indonesia.

---

### Level 3: Core Package, Hilt DI & Design System

- [x] **[NEW] `LiveStudioApp.kt`**
  - Buat kelas `Application` ber-anotasi `@HiltAndroidApp`.

- [x] **[NEW] `di/AppModule.kt`**
  - Buat `@Module` & `@InstallIn(SingletonComponent::class)` yang menyediakan singleton instance untuk `PreferencesManager`, `SecureTokenStorage`, `AuthRepository`, dan UseCases.

- [x] **[MODIFY] [MainActivity.kt](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/app/src/main/java/id/co/mondo/livestudio/MainActivity.kt)**
  - Tambahkan anotasi `@AndroidEntryPoint`.

- [x] **[NEW] `core/util/Result.kt`**
  - Buat sealed interface `Result<out T>` (`Success<out T>`, `Error(val exception: Throwable, val message: String? = null)`, `Loading`).

- [x] **[NEW] `core/util/Constants.kt`**
  - Buat `object Constants` berisi konstanta dasar proyek.

- [x] **[MODIFY] [Color.kt](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/app/src/main/java/id/co/mondo/livestudio/ui/theme/Color.kt)** & **[Theme.kt](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/app/src/main/java/id/co/mondo/livestudio/ui/theme/Theme.kt)**
  - Token warna Material 3 dan `LiveStudioTheme`.

---

## 3. Verifikasi Build E0
- [x] Jalankan `./gradlew assembleDebug` untuk memastikan tidak ada konflik dependensi, anotasi Hilt, atau sintaksis (Build SUCCESSFUL).
