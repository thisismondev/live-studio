# Kebijakan Privasi / Privacy Policy — Live Studio

**Tanggal Efektif / Effective Date**: 6 Oktober 2026  
**Aplikasi / Application**: Live Studio (Android)

---

## Bahasa Indonesia

### 1. Pendahuluan
Kebijakan Privasi ini menjelaskan bagaimana aplikasi **Live Studio** ("kami") mengelola data dan privasi Anda saat menggunakan aplikasi kami. Kami berkomitmen untuk melindungi privasi pengguna kami. **Live Studio** tidak mengumpulkan, menyimpan, atau menjual data pribadi Anda ke server pihak ketiga milik kami.

### 2. Informasi yang Diproses dan Penggunaannya
Live Studio membutuhkan beberapa izin perangkat untuk dapat menyiarkan layar dan audio ke akun Facebook Anda:

*   **Autentikasi Facebook (`public_profile`, `publish_video`)**:
    Aplikasi menggunakan Facebook SDK untuk mengotentikasi pengguna dan mendapatkan izin pembuatan siaran langsung (*Live Video*). Token akses disimpan secara lokal di perangkat Anda oleh Facebook SDK dan hanya digunakan untuk berkomunikasi langsung dengan API resmi Meta Facebook.
*   **Perekaman Layar (*MediaProjection*)**:
    Digunakan untuk menangkap tampilan layar HP Anda secara *real-time* dan mengirimkannya ke server siaran Facebook Live yang Anda pilih. Tampilan layar tidak pernah disimpan di perangkat atau diunggah ke lokasi lain selain aliran siaran (*stream*) resmi Facebook Anda.
*   **Mikrofon dan Suara Audio Internal**:
    Digunakan untuk menangkap suara Anda dan suara dari aplikasi yang sedang dibagikan agar penonton siaran dapat mendengar proses pengundian. Audio diproses secara langsung untuk siaran (*streaming*) dan tidak disimpan di perangkat.
*   **Kamera Depan (Opsional)**:
    Jika Anda mengaktifkan fitur kamera depan, pratinjau kamera akan ditampilkan di dalam kotak kecil (*overlay*) pada layar Anda agar wajah penyiar ikut tersiar. Data kamera diproses secara lokal pada perangkat Anda.
*   **Tampil di Atas Aplikasi Lain (*Overlay Permission*)**:
    Digunakan untuk menampilkan gelembung kontrol (*floating bubble*) dan kotak kamera agar Anda dapat mengontrol siaran saat membuka aplikasi lain (seperti *spin wheel* atau peramban).

### 3. Penyimpanan Data Lokal
Semua preferensi aplikasi (seperti pilihan tema terang/gelap, judul siaran terakhir, dan pengaturan privasi) disimpan secara lokal di perangkat Anda menggunakan Android Jetpack DataStore. Kami tidak menggunakan server *backend* atau basis data eksternal milik sendiri.

### 4. Pembagian Data
Kami **tidak pernah** menjual, menyewakan, atau membagikan data Anda kepada pihak ketiga. Data siaran (video dan audio) dikirimkan secara terenkripsi (*RTMPS*) langsung dari perangkat Anda ke platform Facebook (Meta Platforms, Inc.) sesuai dengan syarat dan kebijakan privasi Facebook yang berlaku.

### 5. Penghapusan Data (*Data Deletion*)
Karena kami tidak menyimpan data pribadi pengguna di server kami, Anda dapat menghapus semua data aplikasi kapan saja dengan cara:
1. Melakukan *Logout* dari aplikasi Live Studio.
2. Menghapus data/cache aplikasi melalui menu Pengaturan (*Settings*) Android Anda.
3. Menghapus penginstalan (*uninstall*) aplikasi Live Studio dari perangkat Anda.
4. Mencabut akses aplikasi Live Studio melalui Pengaturan Akun Facebook Anda (*Settings & Privacy -> Apps and Websites*).

### 6. Hubungan Kontak
Jika Anda memiliki pertanyaan mengenai Kebijakan Privasi ini, Anda dapat menghubungi pengembang melalui repositori resmi atau email dukungan pengembang.

---

## English Version

### 1. Introduction
This Privacy Policy explains how the **Live Studio** application ("we", "us") handles your data and privacy when using our app. We are committed to protecting your privacy. **Live Studio** does not collect, store, or sell your personal data to any external third-party servers owned by us.

### 2. Information Processed and Its Usage
Live Studio requires specific device permissions to stream your screen and audio directly to your Facebook account:

*   **Facebook Authentication (`public_profile`, `publish_video`)**:
    The app uses the official Facebook SDK to authenticate users and obtain authorization to create Live Videos. Access tokens are managed locally on your device by the Facebook SDK and are only used to communicate directly with official Meta Facebook APIs.
*   **Screen Capture (*MediaProjection*)**:
    Used to capture your device screen in real-time and broadcast it to your Facebook Live stream. Screen content is never saved to local storage or uploaded anywhere other than your official Facebook Live stream.
*   **Microphone and Internal Audio**:
    Used to capture your voice and internal app audio so viewers can hear the live draw process. Audio is processed strictly for live streaming purposes and is not saved.
*   **Front Camera (Optional)**:
    If enabled, a small front camera preview box is rendered locally on your device screen so your face is included in the live stream. Camera data is processed strictly on-device.
*   **Display Over Other Apps (*Overlay Permission*)**:
    Used to display a floating control bubble and camera overlay box, allowing you to control live streaming while using other apps (such as a spin wheel or web browser).

### 3. Local Data Storage
All app preferences (such as light/dark theme preference, last live stream title, and privacy settings) are saved locally on your device using Android Jetpack DataStore. We do not operate any external backend server or database.

### 4. Data Sharing
We **never** sell, rent, or share your data with third parties. Streaming data (video and audio) is transmitted securely via encrypted RTMPS directly from your device to Facebook (Meta Platforms, Inc.) in accordance with Facebook's terms and privacy policy.

### 5. Data Deletion Instructions
Since we do not store any personal user data on our own servers, you can clear all app-related data at any time by:
1. Logging out of the Live Studio app.
2. Clearing app data/cache in your device's Android Settings.
3. Uninstalling the Live Studio app from your device.
4. Revoking Live Studio app access via your Facebook Account Settings (*Settings & Privacy -> Apps and Websites*).

### 6. Contact Us
If you have any questions regarding this Privacy Policy, please contact the developer via the official repository or support channel.
