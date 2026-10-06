# Design Brief — Live Studio

## Status
[x] Approved  
Tanggal: 6 Oktober 2026  
Penulis: AI-assisted / Development Team  
Referensi: [PRD — Live Arisan Streamer.md](PRD%20%E2%80%94%20Live%20Arisan%20Streamer.md) | [ADR — Live Studio.md](ADR%20%E2%80%94%20Live%20Studio.md)

---

## 1. Konteks & Tujuan
**Live Studio** adalah aplikasi Android native untuk menyiarkan layar HP langsung ke Facebook Live dengan **1 HP**. Pengguna utama adalah **Admin Arisan** yang membutuhkan tampilan bersih, mudah dipakai, dan terpercaya tanpa istilah teknis yang rumit.

Aplikasi ini berfokus pada **Mode Portrait Only** dengan UI sederhana berbasis **Jetpack Compose Material 3**, menerapkan prinsip **Edge-to-Edge** dan **Compose Styles** agar tampilan terlihat modern, rapi, dan konsisten di seluruh layar HP Android.

---

## 2. Prinsip Desain Utama (Design Guidelines)

### A. Portrait Mode Only
- Aplikasi terkunci dalam orientasi **Portrait** (`android:screenOrientation="portrait"`).
- Tata letak vertikal tunggal yang sederhana (*Single Column / Vertical Stack*), meminimalkan scroll horizontal.
- Semua elemen UI dan tombol mudah dijangkau dengan satu tangan (jempol).

### B. Edge-to-Edge Display
- Menggunakan `enableEdgeToEdge()` di Activity.
- Layar menggambar penuh hingga di bawah Status Bar dan Navigation Bar.
- **Handling Insets**:
  - Menggunakan `Scaffold` dengan `WindowInsets.safeDrawing` untuk menghindari elemen terpotong.
  - Untuk input teks (`OutlinedTextField`), menggunakan `android:windowSoftInputMode="adjustResize"` dan `Modifier.fitInside(WindowInsetsRulers.Ime.current)` / `imePadding()` agar keyboard tidak menutupi tombol/input.
  - Elemen daftar (`LazyColumn`) menggunakan `contentPadding` dari `Scaffold` agar item terbawah tetap bisa di-scroll secara penuh di atas navigation bar.

### C. Unified Styles & Component System
- Menggunakan **Jetpack Compose Material 3** dan pendekatan `ComponentStyles` untuk konsistensi token desain (Color, Typography, Shape):
  - **Button Style**: Sudut membulat sedang (`shape = RoundedCornerShape(12.dp)`), tinggi minimal `48.dp` (touch target aman).
  - **TextField Style**: Outlined style dengan penanganan label jelas dan pesan error di bawahnya.
  - **Card Style**: Permukaan kartu bersudut membulat (`16.dp`) untuk memisahkan grup informasi.
  - **Floating Overlay Style**: Gelembung melayang ringkas dengan batas bidang melingkar (`CircleShape`) dan elevasi jelas agar terlihat di atas aplikasi lain.

---

## 3. Palet Warna & Tipografi

| Peran | Terang (Light Mode) | Gelap (Dark Mode) | Alasan |
|---|---|---|---|
| **Primary (Utama)** | Indigo `#4F46E5` | Indigo Muda `#A5B4FC` | Warna utama tindakan, kontras & modern. |
| **On Primary** | Putih `#FFFFFF` | Indigo Gelap `#1E1B4B` | Teks di atas warna utama. |
| **Background** | Putih Kebiruan `#F8F9FC` | Hampir Hitam `#0F1115` | Latar belakang penuh (Edge-to-Edge). |
| **Surface (Kartu)** | Putih `#FFFFFF` | Kebiruan Gelap `#1A1D24` | Permukaan form dan kartu. |
| **Text Primary** | `#14161C` | `#EDEEF2` | Teks utama dengan keterbacaan tinggi. |
| **Text Secondary** | `#5B6070` | `#A3A8B8` | Subtitle / teks pendukung. |
| **Status Live & Stop** | Merah `#D92D20` | Merah Muda `#F97066` | Indikator aktif siaran dan tombol berhenti. |
| **Peringatan / Reconnect** | Oranye `#B54708` | Kuning `#FEC84B` | Indikator menyambung ulang. |

**Tipografi**: Menggunakan font sistem standar Android (Roboto / Default Material 3 Typography) untuk performa ringan.

---

## 4. Alur Layar & Komponen UI (11 Screens/Components)

1. **Layar 1 — Login Facebook**
   - Logo placeholder, judul "Live Studio", sub-judul ramah, dan tombol "Masuk dengan Facebook" (Primary Indigo Button).
2. **Layar 2 — Penjelasan Izin (Permissions Overview)**
   - Daftar izin dalam bentuk kartu ringkas (Perekaman Layar, Mikrofon, Kamera Depan, Overlay, Notifikasi) dengan bahasa awam dan tombol "Berikan Izin".
3. **Layar 3 — Beranda / Persiapan Live**
   - Form Input Judul Siaran (`OutlinedTextField`), Dropdown Pilihan Privasi (Publik / Teman), dan Tombol Utama "Mulai Live".
4. **Layar 4 — Pesan Syarat Akun / Error**
   - Kartu penjelasan ramah bila akun belum memenuhi syarat live Facebook atau koneksi gagal.
5. **Layar 5 — Status Live In-App**
   - Indikator badge merah "LIVE", timer durasi, status mikrofon/suara/kamera, dan tombol "Stop Live".
6. **Layar 6 — Gelembung Melayang (Floating Control)**
   - Floating bubble kecil melingkar yang dapat digeser di layar. Ketika diketuk, membuka menu cepat: *Mute Mic*, *Mute App Audio*, *Toggle Cam*, dan *Stop Live*.
7. **Layar 7 — Kotak Kamera Depan**
   - Jendela kamera kecil ukuran tetap bersudut tumpul yang dapat digeser (*draggable*) di atas layar.
8. **Layar 8 — Notifikasi Persisten**
   - System notification dengan penanda "Live Berjalan", durasi, dan tombol aksi cepat "Stop".
9. **Layar 9 — Tampilan Reconnecting**
   - Banner/dialog oranye "Menyambung ulang..." (countdown 30 detik).
10. **Layar 10 — Dialog Konfirmasi Stop**
    - Dialog pop-up: "Hentikan Siaran?" dengan tombol "Ya, Hentikan" (Merah) dan "Batal" (Outlined).
11. **Layar 11 — Ringkasan Live Selesai**
    - Kartu sukses: "Siaran Selesai", durasi siaran, pesan bahwa video tersimpan di profil Facebook, dan tombol "Kembali ke Beranda".

---

## 5. Aksesibilitas & Ukuran Sentuh
- **Touch Target**: Minimal **48 × 48 dp** pada seluruh tombol, terutama pada gelembung melayang.
- **Kontras Teks**: Memenuhi standar WCAG 2.1 AA (minimal 4.5:1 untuk teks normal).
- **Indikator Ganda**: Status tidak hanya bergantung pada warna, tetapi juga menyertakan teks/ikon (misal: Warna Merah + Teks "LIVE" + Ikon Titik).
- **Dynamic Type**: Mengikuti ukuran font sistem tanpa memotong tata letak vertikal.
