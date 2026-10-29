# PRD — Live Arisan Streamer

## Status
[ ] Draft  [ ] In Review  [x] Approved
Last updated: 6 Oktober 2026 (diperbarui: konsolidasi §12 Pembagian Tahapan MVP & §13 Parking Lot) | Author: AI-assisted | Related: Vision Doc — Live Arisan Streamer (Approved)

## 1. User Problem
Admin arisan perorangan yang mengundi arisan lewat Facebook Live tidak bisa membagikan layar dari aplikasi Facebook di HP. Akibatnya mereka memakai 2 HP: satu untuk siaran live, satu lagi untuk spin wheel atau undian, dengan kamera HP pertama diarahkan ke layar HP kedua. Hasilnya merepotkan dan tampilan ke penonton (sekitar 50–100 orang per sesi) kurang jelas. Masalah ini terjadi setiap kali ada undian, mingguan atau bulanan. Aplikasi ini menggantikan 2 HP dengan 1 HP yang langsung menyiarkan layar ke Facebook.

## 2. User Personas
**Admin Arisan (primer)**
- **Peran:** Perorangan yang menyelenggarakan arisan dan mengundi pemenang lewat Facebook Live memakai profil pribadi.
- **Tujuan:** Menyiarkan undian dengan jelas dan tepercaya ke anggota arisan, memakai satu HP saja.
- **Pain points:** Harus menyiapkan 2 HP; tampilan hasil rekam kamera buram; cara memakai alat live yang rumit.
- **Konteks pemakaian:** Live mingguan atau bulanan, biasanya di rumah, jaringan seluler atau Wi-Fi rumah, bukan pengguna teknis. Perlu antarmuka sangat sederhana dan berbahasa Indonesia.

**Penyiar umum (sekunder, bukan fokus MVP)**
- Siapa saja yang perlu menyiarkan layar HP ke Facebook (demo, tutorial, live sambil membuka browser).

## 3. Business Goals
| Tujuan | Target terukur |
|---|---|
| Menghilangkan kebutuhan 2 HP pada sesi live arisan | 2 dari 2 pengguna awal berhenti memakai 2 HP (MVP) |
| Siaran yang stabil | ≥ 90% sesi live selesai tanpa crash atau terputus |
| Memulai live dengan cepat | ≤ 2 menit dari buka aplikasi sampai live mulai |
| Rilis cepat | MVP dalam target 2 minggu (fleksibel) |
| Gratis, tanpa iklan, untuk publik | Tidak ada target pendapatan |

## 4. User Journeys
**J1 — Login pertama kali**
1. Pengguna membuka aplikasi → sistem menampilkan layar sambutan dan tombol "Masuk dengan Facebook".
2. Pengguna menekan tombol → sistem membuka proses login Facebook dan meminta izin yang diperlukan.
3. Pengguna menyetujui → sistem menyimpan sesi login dan menampilkan layar utama.
4. Jika pengguna menolak izin → sistem menjelaskan bahwa izin dibutuhkan dan menawarkan mencoba lagi.

**J2 — Memulai live**
1. Pengguna di layar utama mengisi judul siaran dan memilih privasi (Publik / Teman).
2. Pengguna menekan "Mulai Live" → sistem meminta izin perekaman layar dan mikrofon (bila belum diberikan).
3. Sistem membuat siaran di Facebook dan mulai mengirim layar ke Facebook.
4. Sistem menampilkan tombol melayang dan notifikasi bahwa live sedang berjalan.
5. Jika akun tidak memenuhi syarat live (misalnya pengikut kurang dari 100 atau akun terlalu baru) → sistem menampilkan pesan yang jelas tentang syarat yang belum terpenuhi.

**J3 — Selama live berjalan**
1. Pengguna berpindah ke aplikasi spin wheel atau browser → sistem terus menyiarkan layar yang tampil.
2. Pengguna bisa mematikan atau menyalakan mikrofon, suara aplikasi, dan kamera depan lewat tombol melayang.
3. Saat kamera depan dinyalakan → sistem menampilkan kotak kamera kecil di layar siaran. Pengguna bisa menggesernya.
4. Penonton melihat layar dan mendengar suara sesuai pengaturan.

**J4 — Koneksi terputus**
1. Koneksi internet terputus → sistem menampilkan status "Menyambung ulang…" dan mencoba menyambung otomatis.
2. Jika tersambung kembali dalam 30 detik → siaran berlanjut.
3. Jika lebih dari 30 detik → sistem menghentikan siaran dan memberi tahu pengguna.

**J5 — Mengakhiri live**
1. Pengguna menekan "Stop" (di tombol melayang, notifikasi, atau di dalam aplikasi) → sistem meminta konfirmasi.
2. Setelah dikonfirmasi → sistem menghentikan siaran dan menutup tombol melayang.
3. Sistem menampilkan ringkasan singkat bahwa siaran selesai. Video tersimpan di profil Facebook pengguna (perilaku bawaan Facebook).

## 5. Functional Requirements

**Autentikasi**
- FR-01: Sistem **shall** menyediakan login dengan Facebook (OAuth, Facebook SDK) dan meminta izin `publish_video`.
- FR-02: Sistem **shall** menjaga pengguna tetap login sampai pengguna logout atau token kedaluwarsa.
- FR-03: Sistem **shall** menyediakan tombol logout.
- FR-04: Sistem **should** meminta login ulang dengan pesan jelas bila token kedaluwarsa.

**Persiapan siaran**
- FR-05: Sistem **shall** meminta pengguna mengisi judul siaran sebelum live.
- FR-06: Sistem **shall** menyediakan pilihan privasi siaran: Publik dan Teman.
- FR-07: Sistem **shall** membuat siaran di Facebook lewat Live Video API dan memakai stream URL (RTMPS) secara otomatis, tanpa pengguna menyalin stream key.
- FR-08: Sistem **shall** menampilkan pesan yang jelas bila akun tidak memenuhi syarat live Facebook (usia akun, mode profesional, jumlah pengikut) atau bila pembuatan siaran gagal.

**Penyiaran layar dan suara**
- FR-09: Sistem **shall** menangkap layar perangkat dan menyiarkannya ke Facebook Live melalui RTMPS.
- FR-09a: Sistem **shall** menyediakan pilihan mode penyiaran: **Seluruh Layar** (*Entire Screen*) atau **Aplikasi Tertentu** (*Specific Application*).
- FR-09b: Pada mode Aplikasi Tertentu, sistem **shall** menyediakan pemilih aplikasi dari daftar aplikasi berpeluncur (*Launcher Apps*) dengan kolom pencarian dan mengingat aplikasi terakhir yang dipilih.
- FR-09c: Pada mode Aplikasi Tertentu, sistem **shall** otomatis meluncurkan aplikasi target setelah siaran terhubung, dan otomatis menyensor tampilan video dengan gambar jeda privasi (*Privacy Guard*) saat pengguna keluar dari aplikasi tersebut (suara mikrofon tetap tersiar).
- FR-09d: Sistem **shall** memungkinkan pengguna mengganti aplikasi target di tengah siaran yang sedang berjalan tanpa memutus sesi live.
- FR-10: Sistem **shall** menangkap suara mikrofon dan menyiarkannya.
- FR-11: Sistem **shall** menangkap suara dari aplikasi yang dibagikan di Android 10 (API 29) ke atas.
- FR-12: Sistem **shall** menampilkan pemberitahuan di perangkat Android 7–9 bahwa suara aplikasi tidak tersedia dan hanya suara mikrofon yang disiarkan.
- FR-13: Pengguna **shall** dapat menyalakan atau mematikan (mute) mikrofon dan suara aplikasi secara terpisah, kapan saja saat live.
- FR-14: Orientasi aplikasi **shall** dikunci portrait.

**Kamera depan**
- FR-15: Kamera depan **shall** mati secara default.
- FR-16: Pengguna **shall** dapat menyalakan dan mematikan kamera depan kapan saja saat live.
- FR-17: Saat menyala, kamera depan **shall** tampil sebagai kotak kecil berukuran tetap yang ikut tersiar ke penonton.
- FR-18: Pengguna **shall** dapat menggeser posisi kotak kamera.

**Kontrol saat live**
- FR-19: Sistem **shall** menampilkan tombol melayang (floating) di atas aplikasi lain berisi kontrol: mute mikrofon, mute suara aplikasi, kamera depan, dan Stop.
- FR-20: Sistem **shall** meminta izin "tampil di atas aplikasi lain" dan menjelaskan alasannya sebelum live pertama.
- FR-21: Sistem **shall** menampilkan notifikasi persisten selama live dengan aksi cepat (minimal Stop).
- FR-22: Sistem **should** menampilkan penanda siaran sedang berjalan (misalnya durasi) di notifikasi.

**Ketahanan koneksi**
- FR-23: Sistem **shall** mencoba menyambung ulang otomatis saat koneksi terputus.
- FR-24: Sistem **shall** menghentikan siaran bila koneksi tidak pulih dalam 30 detik, dan memberi tahu pengguna.

**Mengakhiri siaran**
- FR-25: Pengguna **shall** dapat menghentikan siaran lewat tombol melayang, notifikasi, atau aplikasi, dengan konfirmasi sebelum benar-benar berhenti.
- FR-26: Sistem **shall** menutup siaran di Facebook dengan benar saat berhenti, termasuk bila dihentikan otomatis.

**Umum**
- FR-27: Seluruh teks antarmuka **shall** berbahasa Indonesia.
- FR-28: Aplikasi **shall** berjalan di Android SDK 24 ke atas dengan target SDK 37.
- FR-29: Aplikasi **shall** menyediakan pilihan tema Terang / Gelap / Ikuti sistem (default Ikuti sistem).

## 6. Non-Functional Requirements
- **Performa:** Siaran default 720p, 30 fps, bitrate sekitar 2,5 Mbps (asumsi untuk 50–100 penonton lewat jaringan seluler). Perlu diuji di perangkat kelas menengah.
- **Kecepatan memulai:** Dari buka aplikasi sampai live mulai ≤ 2 menit untuk pengguna yang sudah login.
- **Ketersediaan/stabilitas:** ≥ 90% sesi selesai tanpa crash atau terputus (diukur manual dari laporan pengguna awal).
- **Keamanan:** Token Facebook disimpan aman di perangkat. Aplikasi tanpa server sendiri (tanpa backend). Siaran memakai RTMPS (terenkripsi).
- **Privasi:** Aplikasi hanya meminta izin yang diperlukan (perekaman layar, mikrofon, kamera, tampil di atas aplikasi lain, notifikasi). Menyediakan halaman Kebijakan Privasi (URL) untuk Meta dan Google Play. Tanpa pelacakan atau analitik.
- **Aksesibilitas:** Ukuran teks dan tombol cukup besar untuk pengguna awam. Standar formal (misalnya WCAG) TBD, tidak menjadi syarat MVP.
- **Kompatibilitas:** Android 7.0 (SDK 24) ke atas. Di Android 7–9 suara aplikasi tidak tersedia (lihat FR-12).
- **Kepatuhan:** Mengikuti kebijakan platform Meta (App Review, business verification, syarat akun live) dan kebijakan Google Play untuk aplikasi screen capture dan foreground service.

## 7. Out of Scope
- iOS.
- Platform selain Facebook (TikTok, YouTube, dan lain-lain).
- Live ke Page atau Group (fokus awal profil pribadi).
- Spin wheel atau undian bawaan di dalam aplikasi.
- Editing video, filter, atau overlay selain kotak kamera.
- Pengelolaan data peserta atau iuran arisan.
- Menampilkan komentar penonton di dalam aplikasi (opsional, dievaluasi setelah MVP).
- Analitik, laporan crash otomatis, dan backend sendiri.

## 8. Dependencies
| Dependency | Type | Team / System | Status |
|---|---|---|---|
| Facebook Login + Live Video API (`publish_video`) | External | Meta | Perlu App Review untuk rilis publik; selama Development hanya akun tester |
| Business verification Meta | External | Meta | TBD — apakah bisa dipenuhi sebagai individu (owner: Anda) |
| Meta Developer App | External | Meta | TBD — apakah sudah dimiliki atau perlu dibuat (owner: Anda) |
| Library RootEncoder (pedroSG94) | External | Open-source (GitHub) | Dipilih |
| API MediaProjection, foreground service, overlay Android | External | Android OS | Tersedia; ada batasan per versi Android |
| Halaman Kebijakan Privasi (URL) | Internal | Anda | TBD — belum dibuat |
| Akun Google Play Console | External | Google | Hanya bila dirilis ke Play Store; belum dibuat |
| Akun Facebook pengguna awal memenuhi syarat live | External | Pengguna awal | TBD — belum dipastikan |

## 9. Risks
| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| App Review atau business verification Meta ditolak atau sulit dipenuhi sebagai individu | Tinggi | Tinggi (rilis publik terblokir) | Mulai dari mode Development dengan 2 tester; cek syarat verifikasi sejak awal; review dikerjakan sebagai jalur terpisah |
| Akun pengguna tidak memenuhi syarat live (usia < 60 hari, bukan mode profesional, < 100 pengikut) | Sedang | Tinggi | Cek akun pengguna awal lebih dulu; pesan jelas di aplikasi |
| Kamera depan dan mikrofon dari latar belakang dibatasi Android 14+ (foreground service type, izin saat dipakai) | Sedang | Sedang | Validasi di ADR; gunakan foreground service dengan tipe yang sesuai |
| Tombol melayang dan kotak kamera geser terlalu rumit atau tidak stabil di berbagai merek HP | Sedang | Sedang | Uji di beberapa perangkat; tombol melayang sebagai kontrol utama, notifikasi sebagai cadangan |
| Suara aplikasi tidak tersedia di Android 7–9 | Pasti | Rendah-Sedang | Pemberitahuan jelas (FR-12); suara mikrofon tetap tersedia |
| Aplikasi yang diblokir dari perekaman layar | Rendah | Sedang | Uji spin wheel dan browser yang dipakai pengguna awal |
| Baterai dan panas HP saat live lama | Sedang | Sedang | Batasi default 720p/30 fps; uji sesi 30–60 menit |
| Penyiar tunggal (1 developer) dan target 2 minggu terlalu ketat | Sedang | Sedang | Batasi MVP pada FR di atas; tunda komentar penonton |

## 10. KPIs & Success Metrics
| Metric | Baseline | Target | Timeline |
|---|---|---|---|
| Pengguna awal yang tidak lagi memakai 2 HP | 0 dari 2 | 2 dari 2 | MVP |
| Sesi live selesai tanpa crash atau terputus | 0 (saat ini wajib 2 HP) | ≥ 90% | Setelah MVP dipakai |
| Waktu dari buka aplikasi sampai live mulai | Belum diukur | ≤ 2 menit | MVP |

Pengukuran dilakukan manual, berdasarkan laporan dari pengguna awal (tanpa alat analitik).

## 11. Open Questions
| Question | Owner | Due date | Status |
|---|---|---|---|
| Apakah business verification Meta bisa dipenuhi sebagai individu? | Anda | TBD | Open |
| Apakah dua pengguna awal memenuhi syarat live Facebook (usia ≥ 60 hari, mode profesional, ≥ 100 pengikut)? | Anda | Sebelum mulai uji | Open |
| Apakah Meta Developer App sudah ada? | Anda | TBD | Open |
| Apakah pilihan privasi (Publik/Teman) didukung penuh oleh Live Video API untuk profil pribadi? | Anda (verifikasi di ADR) | TBD | Open |
| Apakah komentar penonton layak ditambahkan setelah MVP (izin, endpoint, beban teknis)? | Anda | Setelah MVP | Open |
| Apakah kotak kamera yang bisa digeser bisa ikut tersiar stabil saat aplikasi lain di depan (RootEncoder + foreground service)? | Anda (verifikasi di ADR) | TBD | Open |
| Pembatasan Android 14+ untuk target SDK 37: apakah ada izin atau tipe layanan tambahan? | Anda (verifikasi di ADR) | TBD | Open |

## 12. Pembagian Tahapan MVP

### MVP-inti (Bisa dicoba pengguna awal lebih cepat)
- **Cakupan Epic**: E0 (Spike & Setup), E1 (Login Facebook), E2 (Persiapan Siaran), E3 (Perekaman Layar & Suara), E6 (Koneksi & Mengakhiri Siaran).
- **Catatan**: Kontrol siaran dan tombol Stop sementara ada di dalam aplikasi utama dan notifikasi dasar foreground service.

### MVP-lengkap
- **Cakupan Epic**: MVP-inti ditambah E4 (Kamera Depan Overlay) dan E5 (Gelembung Kontrol Melayang / Floating Bubble).

### Gerbang Keputusan (Go / No-Go) setelah E0
Setelah E0 (Spike Tests), putuskan berdasarkan hasil eksperimen:
1. Siaran ke profil pribadi via API berhasil dengan akun uji.
2. Mikrofon dan suara aplikasi internal dapat disiarkan bersamaan.
3. Penanganan tampilan overlay (gelembung & kotak kamera).
4. Persyaratan akun tester Facebook Live terpenuhi.

## 13. Parking Lot & Roadmap Masa Depan

Ide-ide fitur yang ditunda untuk dikembangkan setelah fase MVP selesai:
- **Komentar Penonton**: Menampilkan komentar langsung dari penonton Facebook Live di dalam aplikasi.
- **Spin Wheel Bawaan**: Menyediakan modul acak/undian terintegrasi agar pengguna tidak perlu membuka aplikasi lain.
- **Live ke Facebook Page / Group**: Menambah dukungan penyiaran ke Halaman Bisnis dan Grup Facebook.
- **Dukungan Platform Lain**: Penyiaran ke YouTube Live atau TikTok Live.
- **Laporan Crash & Log Lokal**: Integrasi Firebase Crashlytics atau ekspor log lokal untuk troubleshooting.
- **Fitur Firebase Pasca-MVP**: Firebase sudah terpasang (Google Services + BoM) tanpa Firebase Auth; Crashlytics dan Analytics ditambahkan setelah MVP selesai.
- **Optimasi Tablet & Dual Screen**: Penyesuaian tata letak untuk perangkat tablet dan layar lipat.
