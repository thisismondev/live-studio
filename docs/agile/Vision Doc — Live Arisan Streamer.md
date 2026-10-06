# Vision Doc — Live Arisan Streamer

*Produk: **Live Studio**. Use-case awal: **Live Arisan Streamer** (siaran undian arisan ke Facebook Live dari satu HP). Dokumen lain memakai nama produk (ADR, Design Brief) atau nama use-case (Vision, PRD) untuk konteks yang sama.*

## Status
[ ] Draft  [ ] In Review  [x] Approved
Last updated: 6 Oktober 2026 | Author: AI-assisted

## Problem Statement
Admin arisan yang mengundi arisan lewat Facebook Live tidak bisa membagikan layar (screen share) dari aplikasi Facebook di HP mereka. Karena itu mereka memakai 2 HP: satu untuk menyiarkan live Facebook, satu lagi untuk menjalankan spin wheel atau undian arisan, dan kamera HP pertama diarahkan ke layar HP kedua. Cara ini merepotkan, butuh 2 perangkat, dan hasil tampilan ke penonton kurang jelas (pantulan, blur, posisi kamera goyang). Masalah ini dialami admin arisan perorangan setiap kali ada undian (mingguan atau bulanan) dengan penonton sekitar 50–100 orang per sesi.

## Target Users
**Primer:** Admin arisan perorangan di Indonesia yang menyiarkan undian lewat Facebook Live memakai profil pribadi. Mereka bukan pengguna teknis, jadi aplikasi harus sangat sederhana dan berbahasa Indonesia.

**Sekunder:** Siapa saja yang perlu menyiarkan layar HP ke Facebook (misalnya demo aplikasi, tutorial, atau live sambil membuka browser). Ini bukan fokus MVP.

Saat ini ada 2 pengguna awal. Rencananya aplikasi dibuka untuk publik.

## Desired Outcome
Admin arisan cukup memakai **satu HP** dan **satu aplikasi untuk memulai live**, tanpa membuka aplikasi Facebook lagi. Alurnya: login dengan Facebook, tekan "Mulai Live", lalu berpindah ke aplikasi spin wheel atau browser. Penonton melihat layar tersebut dengan jelas dan mendengar suara dari aplikasi yang dibagikan maupun suara admin. Admin bisa menyalakan atau mematikan (mute) masing-masing sumber suara, dan bisa menampilkan kamera depan kapan saja (default mati).

## Business Objectives
- Aplikasi gratis, tanpa iklan, dan dirilis untuk publik. Tidak ada target pendapatan.
- Tujuan utama: menghilangkan kebutuhan 2 HP pada sesi live arisan.
- Tujuan sekunder: menjadi alat yang diandalkan komunitas admin arisan dan penjual online.

## Success Metrics
Disederhanakan sesuai permintaan: hanya 3 ukuran.

| Metric | Baseline | Target | Timeline |
|---|---|---|---|
| Pengguna awal yang tidak lagi memakai 2 HP | 0 dari 2 | 2 dari 2 | MVP |
| Sesi live yang selesai tanpa crash atau terputus | 0 (saat ini wajib 2 HP) | ≥ 90% | Setelah MVP dipakai |
| Waktu dari buka aplikasi sampai live mulai | Belum diukur | ≤ 2 menit | MVP |

## Constraints
- **Budget:** Tidak ada dana khusus (asumsi, dikerjakan sendiri). Biaya potensial hanya bila dirilis ke Google Play: akun Google Play Console (biaya sekali bayar, sekitar US$25, verifikasi harga terbaru sebelum daftar).
- **Deadline:** Target MVP 2 minggu untuk MVP-inti kondisi ideal (full-time, E0 mulus). Rincian Sprint 1–4 (~4 minggu, MVP-inti + MVP-lengkap) lihat MVP — Live Studio §5. Fleksibel, tetapi makin cepat makin baik. Catatan: MVP yang dipakai 2 pengguna awal (mode Development). Rilis publik bergantung pada App Review Meta, yang durasinya di luar kendali dan dikerjakan sebagai jalur terpisah.
- **Tim:** Satu developer (Anda sendiri).
- **Teknis:**
  - Android native, minimum SDK 24, target SDK 37.
  - Login memakai Facebook SDK (OAuth). Setelah login, aplikasi memanggil Live Video API (`POST /me/live_videos`) untuk membuat siaran dan mendapatkan stream URL (RTMPS) secara otomatis, sehingga pengguna tidak perlu menyalin stream key manual.
  - Streaming dan penangkapan layar memakai library RootEncoder (com.github.pedroSG94). Penangkapan layar memakai MediaProjection dengan foreground service.
  - Orientasi dikunci portrait.
  - **Suara aplikasi lain:** Android hanya mengizinkan menangkap suara internal mulai Android 10 (API 29). Di Android 7–9 (SDK 24–28), hanya suara mikrofon yang tersedia. Ini keterbatasan yang diterima (minimum SDK 24 tetap), tetapi perlu ditampilkan kepada pengguna di perangkat tersebut.
- **Regulasi/kebijakan (hasil verifikasi dokumentasi Meta, Juli 2026):**
  - Live ke profil pribadi lewat API memakai izin `publish_video`, yang **wajib lolos App Review**, dan fitur ini hanya tersedia dengan **verifikasi bisnis (business verification)**.
  - Syarat akun Facebook untuk bisa live: usia akun minimal 60 hari, dan profil harus berada di mode profesional dengan minimal 100 pengikut. Syarat ini berlaku untuk pengguna Anda, bukan hanya aplikasi.
  - Selama aplikasi masih dalam mode Development dan belum diulas, hanya akun yang punya peran di aplikasi (admin, developer, tester) yang bisa memakainya. Dua pengguna awal bisa ditambahkan sebagai tester.
  - Perlu mengikuti kebijakan Google Play untuk aplikasi screen capture (deklarasi foreground service dan izin perekaman layar).

## Out of Scope
- iOS.
- Platform selain Facebook (TikTok, YouTube, dan lain-lain).
- Live ke Page atau Group (fokus awal profil pribadi).
- Spin wheel atau undian bawaan di dalam aplikasi.
- Editing video, filter, atau overlay.
- Pengelolaan data peserta atau iuran arisan.
- Menampilkan komentar penonton di dalam aplikasi: **tidak masuk MVP**, dievaluasi sebagai fitur opsional setelah MVP (lihat Open Questions).

## Open Questions
1. **Komentar penonton (opsional):** Apakah terlalu berat? Secara teknis tampaknya ringan (cukup mengambil komentar dari siaran yang sama lewat API, atau menampilkannya sebagai daftar kecil). Namun izin dan endpoint yang dibutuhkan belum diverifikasi, dan menampilkan komentar di atas layar yang sedang dibagikan akan menambah kerumitan (overlay). Saran: tunda sampai MVP berjalan, lalu evaluasi.
2. **App Review dan business verification Meta:** Apakah Anda bisa memenuhi verifikasi bisnis sebagai individu? Ini penentu apakah aplikasi bisa dipakai publik. Perlu dicek lebih awal.
3. Apakah dua pengguna awal sudah memenuhi syarat akun (usia ≥ 60 hari, mode profesional, ≥ 100 pengikut)?
4. Beberapa aplikasi memblokir perekaman layar (misalnya aplikasi perbankan atau DRM). Kemungkinan tidak terdampak untuk spin wheel dan browser, tetapi perlu diuji.
5. Pastikan Android SDK platform 37 tersedia di lingkungan pengembangan Anda untuk target SDK 37.

## Next Step
→ PRD to be drafted — see child page: PRD — Live Arisan Streamer
