# Rencana Eksekusi: E4 — Fitur Kamera Depan Overlay

Dokumen ini berisi rencana teknis per file untuk tahapan **Fitur Kamera Depan Overlay**.

---

## 1. Ringkasan & Target
Mengintegrasikan `CameraX` di dalam `WindowManager` overlay window untuk menampilkan kotak kamera depan berukuran tetap yang dapat digeser (*draggable*) di atas aplikasi lain saat siaran berlangsung (default: mati).

---

## 2. Checklis Pengerjaan Per File

### Service & Data Layer

- [ ] **[NEW] `service/OverlayService.kt`**
  - Foreground Service (`foregroundServiceType="camera"`).
  - Mengatur `WindowManager.LayoutParams` dengan tipe `TYPE_APPLICATION_OVERLAY`.
  - Membuat `PreviewView` CameraX berukuran tetap (misal: `120.dp` × `160.dp` dengan sudut membulat `12.dp`).
  - Mengikat `ProcessCameraProvider` dengan kamera depan (`CameraSelector.DEFAULT_FRONT_CAMERA`).
  - Menambahkan Touch Listener (`MotionEvent.ACTION_MOVE`) untuk fitur geser (*draggable*) kotak kamera di seluruh area layar.

---

### Domain Layer

- [ ] **[NEW] `domain/usecase/ToggleCameraUseCase.kt`**
  - UseCase untuk mengaktifkan/mematikan overlay kamera depan dan mengirim perintah ke `OverlayService`.

---

### UI Layer / Gesture Handling

- [ ] **[NEW] Touch Listener & Gesture Helper di `OverlayService.kt`**
  - Penanganan batas layar (*screen bounds*) agar kotak kamera tidak terseret keluar dari area layar HP.

---

## 3. Verifikasi E4
- Nyalakan kamera depan saat live streaming berlangsung.
- Verifikasi bahwa kotak kamera muncul di atas aplikasi lain (misal Spin Wheel/Browser) dan dapat digeser dengan lancar tanpa lag.
