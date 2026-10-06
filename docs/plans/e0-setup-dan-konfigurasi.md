# Rencana Eksekusi: E0 — Setup & Konfigurasi Core Proyek

Dokumen ini berisi rencana teknis per file untuk tahapan **Setup & Konfigurasi Core Proyek** sebagai fondasi utama aplikasi **Live Studio**.

---

## 1. Ringkasan & Target
Menyiapkan seluruh dependensi pustaka, konfigurasi `build.gradle.kts`, deklarasi izin & meta-data pada `AndroidManifest.xml`, resource string Bahasa Indonesia, kelas pembantu core (`Result.kt` & `Constants.kt`), serta fondasi warna & tema Jetpack Compose Material 3 (`Color.kt` & `Theme.kt`).

---

## 2. Checklis Pengerjaan Per File

### Level 1: Project & Gradle Configuration

- [x] **[libs.versions.toml](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/gradle/libs.versions.toml)**
  - Tambahkan versi:
    - `googleGmsGoogleServices = "4.5.0"`
    - `firebaseAuth = "24.2.0"`
    - `rootEncoder = "2.4.9"` (pedroSG94)
    - `camerax = "1.4.1"`
    - `datastore = "1.1.2"`
    - `navigationCompose = "2.8.7"`
  - Deklarasikan plugin di section `[plugins]`:
    - `google-gms-google-services = { id = "com.google.gms.google-services", version.ref = "googleGmsGoogleServices" }`
  - Deklarasikan pustaka pada section `[libraries]`:
    - `firebase-auth = { group = "com.google.firebase", name = "firebase-auth", version.ref = "firebaseAuth" }`
    - `pedro-rootencoder = { group = "com.github.pedroSG94.RootEncoder", name = "library", version.ref = "rootEncoder" }`
    - `androidx-camera-camera2 = { group = "androidx.camera", name = "camera-camera2", version.ref = "camerax" }`
    - `androidx-camera-lifecycle = { group = "androidx.camera", name = "camera-lifecycle", version.ref = "camerax" }`
    - `androidx-camera-view = { group = "androidx.camera", name = "camera-view", version.ref = "camerax" }`
    - `androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }`
    - `androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }`
    - `androidx-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }`

- [x] **[build.gradle.kts (Root)](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/build.gradle.kts)**
  - Tambahkan plugin `alias(libs.plugins.google.gms.google.services) apply false`.

- [x] **[settings.gradle.kts](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/settings.gradle.kts)**
  - Tambahkan repository `maven { url = uri("https://jitpack.io") }` di dalam `dependencyResolutionManagement.repositories`.

- [x] **[app/build.gradle.kts](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/app/build.gradle.kts)**
  - Apply plugin `alias(libs.plugins.google.gms.google.services)`.
  - Terapkan dependensi pustaka di section `dependencies`:
    - `implementation(libs.firebase.auth)`
    - `implementation(libs.pedro.rootencoder)`
    - `implementation(libs.androidx.camera.camera2)`
    - `implementation(libs.androidx.camera.lifecycle)`
    - `implementation(libs.androidx.camera.view)`
    - `implementation(libs.androidx.datastore.preferences)`
    - `implementation(libs.androidx.navigation-compose)`
    - `implementation(libs.androidx.material.icons.extended)`

- [x] **[app/google-services.json](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/app/google-services.json)**
  - Konfigurasi file `google-services.json` resmi Firebase.

---

### Level 2: Manifest & Core Resources

- [x] **[AndroidManifest.xml](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/app/src/main/AndroidManifest.xml)**
  - Tambahkan izin sistem:
    ```xml
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MICROPHONE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_CAMERA" />
    ```
  - Konfigurasi `MainActivity`: `android:screenOrientation="portrait"`, `android:windowSoftInputMode="adjustResize"`.

- [x] **[strings.xml](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/app/src/main/res/values/strings.xml)**
  - Deklarasikan `app_name` ("Live Studio") dan seluruh teks UI Bahasa Indonesia.

---

### Level 3: Core Package & Design System (Kelas Pembantu & Tema)

- [x] **[NEW] `core/util/Result.kt`**
  - Buat sealed interface `Result<out T>` (`Success<out T>`, `Error(val exception: Throwable, val message: String? = null)`, `Loading`).

- [x] **[NEW] `core/util/Constants.kt`**
  - Buat `object Constants`:
    - `const val FB_GRAPH_API_BASE_URL = "https://graph.facebook.com/v19.0/"`
    - `const val NOTIFICATION_CHANNEL_ID = "live_stream_channel"`
    - `const val NOTIFICATION_CHANNEL_NAME = "Live Streaming Service"`
    - `const val NOTIFICATION_ID = 1001`
    - `const val PREFS_NAME = "live_studio_preferences"`

- [x] **[MODIFY] [Color.kt](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/app/src/main/java/id/co/mondo/livestudio/ui/theme/Color.kt)**
  - Deklarasikan token warna Material 3:
    - Primary Indigo: `PrimaryLight = Color(0xFF4F46E5)`, `PrimaryDark = Color(0xFFA5B4FC)`
    - Background: `BackgroundLight = Color(0xFFF8F9FC)`, `BackgroundDark = Color(0xFF0F1115)`
    - Surface: `SurfaceLight = Color(0xFFFFFFFF)`, `SurfaceDark = Color(0xFF1A1D24)`
    - Live Status Red: `LiveRedLight = Color(0xFFD92D20)`, `LiveRedDark = Color(0xFFF97066)`
    - Reconnect Orange: `WarningOrangeLight = Color(0xFFB54708)`, `WarningOrangeDark = Color(0xFFFEC84B)`

- [x] **[MODIFY] [Theme.kt](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/app/src/main/java/id/co/mondo/livestudio/ui/theme/Theme.kt)**
  - Buat `LiveStudioTheme` menyesuaikan Light/Dark Mode dengan M3 ColorScheme.

---

## 3. Verifikasi Build E0
- [x] Jalankan `./gradlew assembleDebug` untuk memastikan tidak ada konflik dependensi atau sintaksis (Build SUCCESSFUL).
