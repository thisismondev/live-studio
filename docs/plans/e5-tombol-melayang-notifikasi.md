# Rencana Eksekusi: E5 — Gelembung Melayang (Floating Control) & Notifikasi

Dokumen ini berisi rencana teknis per file untuk tahapan **Gelembung Melayang (Floating Control) & Notifikasi Persisten**.

---

## 1. Ringkasan & Target
Mengimplementasikan gelembung kontrol melayang (Floating Control Bubble) bersudut melingkar (`CircleShape`) yang dapat digeser (*draggable*) di atas aplikasi lain. Ketika diketuk, gelembung membuka menu cepat: *Mute Mic*, *Mute App Audio*, *Toggle Cam*, dan *Stop Live*.

---

## 2. Checklis Pengerjaan Per File

### Service Layer

- [ ] **[MODIFY] `service/OverlayService.kt`**
  - Menambahkan gelembung melayang melingkar (`Floating Control Bubble`) menggunakan `WindowManager`.
  - Mengimplementasikan listener sentuh (`OnTouchListener`) untuk menggeser gelembung melayang ke tepi layar.
  - Mengimplementasikan tampilan Popup Menu Cepat saat gelembung diketuk:
    - Tombol Mute / Unmute Mikrofon.
    - Tombol Mute / Unmute Suara Aplikasi.
    - Tombol Toggle Kamera Depan.
    - Tombol Utama Merah "Stop Live".

- [ ] **[MODIFY] `core/service/NotificationHelper.kt`**
  - Menambahkan Intent Action pada notifikasi persisten untuk aksi cepat:
    - Action `ACTION_STOP_LIVE` untuk menghentikan siaran langsung dari notifikasi.

---

### Domain Layer

- [ ] **[NEW] `domain/usecase/ControlStreamUseCase.kt`**
  - UseCase terpusat untuk mengirim perintah dari gelembung melayang / notifikasi ke `StreamService` dan `OverlayService`.

---

## 3. Verifikasi E5
- Verifikasi gelembung melayang dapat digeser dan menempel rapi di tepi layar.
- Ketuk gelembung dan uji fungsi menu cepat (*Mute Mic*, *Mute App Audio*, *Toggle Cam*, dan *Stop Live*).
- Verifikasi aksi Stop pada notifikasi persisten bekerja dengan baik.
