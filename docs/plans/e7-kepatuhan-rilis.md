# Rencana Eksekusi: E7 - Kepatuhan & Rilis

Dokumen ini berisi rencana non-kode untuk lolos **Facebook App Review** dan kebijakan **Google Play** agar aplikasi bisa dirilis publik untuk banyak admin arisan.

---

## 1. Ringkasan & Strategi
- **Facebook App Review** dikerjakan **paralel** sejak E1 selesai, sambil E2-E3 berjalan dalam mode Development. Review bisa memakan waktu berminggu-minggu dan sering ditolak, jadi tidak boleh menunggu fitur selesai.
- **Kepatuhan Google Play** disiapkan **mendekati rilis**.
- Video demo dibuat **sekali** dan dipakai untuk Facebook App Review dan Play Console.

> [!WARNING]
> Menunda kepatuhan Play sampai mendekati rilis berisiko: Play Console sering menolak MediaProjection, overlay, dan foreground service tanpa justifikasi dan video demo. Sisihkan waktu buffer untuk revisi.

---

## 2. Checklist

### A. Facebook App Review (mulai setelah E1 selesai)
- [ ] Ajukan izin `publish_video` (beserta `email` dan `public_profile`).
- [ ] Siapkan video demo alur login hingga siaran live.
- [ ] Pastikan URL Privacy Policy, Terms of Service, dan Data Deletion aktif (sumber: `docs/PRIVACY_POLICY.md`, `docs/TERMS_OF_SERVICE.md`, `docs/DATA_DELETION.md`).
- [ ] Siapkan akun tester dan instruksi review.
- [ ] Setelah disetujui, ubah Facebook App ke mode Live.

### B. Google Play (mendekati rilis)
- [ ] Video demo dan deklarasi use case **MediaProjection** (fitur inti aplikasi).
- [ ] Justifikasi `SYSTEM_ALERT_WINDOW`.
- [ ] Justifikasi `FOREGROUND_SERVICE_MEDIA_PROJECTION`, `FOREGROUND_SERVICE_MICROPHONE`, dan `FOREGROUND_SERVICE_CAMERA`.
- [ ] Formulir **Data Safety** (layar, mikrofon, kamera, token Facebook).
- [ ] Layar consent yang jelas sebelum perekaman layar dimulai.
- [ ] Verifikasi ulang kebijakan backup (token dikecualikan).

---

## 3. Verifikasi E7
- [ ] Facebook App Review disetujui dan app berstatus Live.
- [ ] Data Safety dan deklarasi izin di Play Console lolos review.
