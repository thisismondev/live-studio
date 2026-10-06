# Documentation Index — Live Studio

Selamat datang di direktori dokumentasi proyek **Live Studio** (aplikasi Android native untuk siaran langsung layar HP ke Facebook Live).

---

## 1. Dokumen Produk & Arsitektur (`/docs/agile`)

| Dokumen | Deskripsi / Fokus Utama | Status |
|---|---|---|
| **[`Vision Doc`](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/docs/agile/Vision%20Doc%20—%20Live%20Arisan%20Streamer.md)** | Latar belakang masalah, target pengguna, outcome, dan metrik keberhasilan. | Approved |
| **[`PRD`](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/docs/agile/PRD%20—%20Live%20Arisan%20Streamer.md)** | Persyaratan Fungsional (FR-01 s/d FR-29), Alur Pengguna (J1–J5), Pembagian MVP (§12), & Parking Lot (§13). | Approved |
| **[`ADR`](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/docs/agile/ADR%20—%20Live%20Studio.md)** | Arsitektur (Data-Domain-UI Layer), Tech Stack (Compose, RootEncoder, DataStore, Firebase Auth), & Tahapan Setup. | Approved |
| **[`Design Brief`](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/docs/agile/Design%20Brief%20—%20Live%20Studio.md)** | Panduan UI/UX Material 3, Edge-to-Edge, Portrait Mode Only, & Alur 11 Layar/Komponen. | Approved |

### Urutan Baca Dokumen Produk:
`Vision Doc` → `PRD` → `ADR` → `Design Brief`

---

## 2. Rencana Eksekusi per Epic (`/docs/plans`)

Setiap berkas rencana eksekusi memuat checkbox terstruktur berdasarkan **Setup Level** dan **Layer Eksekusi (Data -> Domain -> UI)**:

| Epic | Berkas Rencana | Tahap Rilis | Status |
|---|---|---|---|
| **E0 — Setup & Konfigurasi Core Proyek** | [`e0-setup-dan-konfigurasi.md`](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/docs/plans/e0-setup-dan-konfigurasi.md) | MVP-inti | `[x] Selesai` |
| **E1 — Login Facebook via Firebase Auth** | [`e1-login-facebook.md`](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/docs/plans/e1-login-facebook.md) | MVP-inti | `[ ] Belum mulai` |
| **E2 — Persiapan Siaran** | [`e2-persiapan-siaran.md`](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/docs/plans/e2-persiapan-siaran.md) | MVP-inti | `[ ] Belum mulai` |
| **E3 — Penyiaran Layar dan Suara** | [`e3-penyiaran-layar-suara.md`](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/docs/plans/e3-penyiaran-layar-suara.md) | MVP-inti | `[ ] Belum mulai` |
| **E6 — Koneksi dan Mengakhiri Siaran** | [`e6-koneksi-mengakhiri-siaran.md`](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/docs/plans/e6-koneksi-mengakhiri-siaran.md) | MVP-inti | `[ ] Belum mulai` |
| **E5 — Tombol Melayang dan Notifikasi** | [`e5-tombol-melayang-notifikasi.md`](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/docs/plans/e5-tombol-melayang-notifikasi.md) | MVP-lengkap | `[ ] Belum mulai` |
| **E4 — Kamera Depan** | [`e4-kamera-depan.md`](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/docs/plans/e4-kamera-depan.md) | MVP-lengkap | `[ ] Belum mulai` |

---

## 3. Aturan Pembaruan Dokumen Rencana & Checkbox

1. **Centang Checkbox Setelah Tugas Selesai:** Setiap kali suatu tugas/fitur pada berkas rencana eksekusi (`/docs/plans/e*.md`) selesai dibuat atau diperbarui, ubah status checkbox dari `- [ ]` menjadi `- [x]`.
2. **Pembaruan Status Epic:**
   - Ketika pengerjaan Epic sedang berlangsung, perbarui statusnya pada tabel di atas menjadi `[/] Sedang berjalan`.
   - Setelah seluruh checkbox pada suatu file rencana eksekusi tercentang (`- [x]`), perbarui status Epic tersebut menjadi `[x] Selesai`.
3. **Penyelarasan dengan Kode & Perubahan Teknis:** Setiap terjadi perubahan arsitektur/pendekatan teknis, segera sesuaikan file rencana di `/docs/plans/` serta dokumentasikan catatan riwayat pembaruannya di `CHANGELOG.md`.

---

## 4. Dokumen Pendukung & Changelog

- **[`PRIVACY_POLICY.md`](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/docs/PRIVACY_POLICY.md)**: Kebijakan Privasi resmi (Bahasa Indonesia & Inggris) untuk Meta App & Google Play Console.
- **[`CHANGELOG.md`](file:///D:/RAQHIB%20ATAILLAH/MyProject/Mobile/LiveStudio/docs/plans/CHANGELOG.md)**: Catatan riwayat pembaruan rencana dan eksekusi.
