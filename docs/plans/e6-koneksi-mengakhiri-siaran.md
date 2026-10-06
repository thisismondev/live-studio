# Rencana Eksekusi: E6 — Ketahanan Koneksi & Mengakhiri Live

Dokumen ini berisi rencana teknis per file untuk tahapan **Ketahanan Koneksi, Mengakhiri Live, dan Perakitan Aplikasi Lengkap**.

---

## 1. Ringkasan & Target
Mengimplementasikan fitur reconnect otomatis (timeout 30 detik) saat koneksi terputus, dialog konfirmasi stop siaran, penutupan siaran di Facebook via Graph API (`POST /{live-video-id}`), layar ringkasan siaran selesai, serta perakitan navigasi penuh pada `AppNavHost.kt` dan `MainActivity.kt`.

---

## 2. Checklis Pengerjaan Per File

### Data & Service Layer

- [ ] **[MODIFY] `service/StreamService.kt`**
  - Mengimplementasikan logika auto-reconnect saat sinyal RTMP terputus.
  - Memulai hitung mundur timeout 30 detik. Jika koneksi terhubung kembali dalam 30 detik, siaran berlanjut; jika tidak, siaran dihentikan secara otomatis.

- [ ] **[MODIFY] `data/remote/FacebookLiveApi.kt`**
  - Menambahkan panggilan Graph API `POST /{live-video-id}?end_live_video=true` untuk menutup sesi siaran di Facebook.

---

### Domain Layer

- [ ] **[NEW] `domain/usecase/StopLiveStreamUseCase.kt`**
  - Class mengeksekusi pembersihan service (`StreamService` & `OverlayService`), penutupan sesi Facebook Live API, dan pengembalian ringkasan siaran (durasi siaran).

---

### UI Layer & Navigation Assembly

- [ ] **[NEW] `ui/components/StopConfirmDialog.kt`**
  - Pop-up Dialog konfirmasi: "Hentikan Siaran?" dengan tombol "Ya, Hentikan" (Merah) dan "Batal" (Outlined).

- [ ] **[NEW] `ui/screens/summary/LiveSummaryScreen.kt`**
  - Layar Ringkasan Siaran Selesai:
    - Kartu Sukses "Siaran Selesai".
    - Informasi durasi siaran dan catatan bahwa video tersimpan di profil Facebook.
    - Tombol "Kembali ke Beranda".

- [ ] **[NEW] `ui/navigation/Screen.kt`**
  - Sealed class rute navigasi Compose: `Login`, `PermissionsOverview`, `HomePrepareLive`, `StreamStatus`, `AccountRequirement`, `LiveSummary`.

- [ ] **[NEW] `ui/navigation/AppNavHost.kt`**
  - `NavHost` penanganan alur navigasi lengkap antar layar berdasarkan status login & siaran.

- [ ] **[MODIFY] [MainActivity.kt](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/app/src/main/java/id/co/mondo/livestudio/MainActivity.kt)**
  - Mengaktifkan `enableEdgeToEdge()`.
  - Mengunci orientasi Portrait.
  - Inisialisasi `CallbackManager` Facebook Login.
  - Menempatkan `AppNavHost` di dalam `LiveStudioTheme`.

---

## 3. Verifikasi E6 & End-to-End
- Uji alur simulasi koneksi terputus dan auto-reconnect.
- Uji alur mengakhiri siaran dengan tombol Stop -> Dialog Konfirmasi -> Layar Ringkasan Live Selesai.
- Jalankan `./gradlew assembleDebug` untuk menguji kelayakan build penuh aplikasi.
