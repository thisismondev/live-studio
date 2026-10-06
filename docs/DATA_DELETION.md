# Panduan Penghapusan Data Pengguna / User Data Deletion Instructions — Live Studio

**Aplikasi / Application**: Live Studio (Android)  
**Tautan Referensi / Reference Link**: Facebook User Data Deletion Callback / Data Safety Compliance

---

## Bahasa Indonesia

### Pernyataan Komitmen Privasi Data
Aplikasi **Live Studio** beroperasi secara **klien-saja (*client-side only*)**. Kami **tidak mengumpulkan, menyimpan, atau mengelola data pribadi pengguna di server *backend* eksternal milik sendiri**. Oleh karena itu, kami tidak menyimpan basis data pengguna di *cloud*.

Tergantung pada penggunaan Anda, data yang disimpan terkait aplikasi ini meliputi:
1. **Token Akses Facebook (*Facebook Access Token*)**: Disimpan secara lokal di perangkat Anda oleh Facebook SDK untuk otentikasi.
2. **Preferensi Lokal (*Local Preferences*)**: Judul siaran terakhir, pilihan privasi, dan preferensi tema yang disimpan di perangkat Anda via Android DataStore.

---

### Cara Menghapus Data Anda

Anda dapat menghapus seluruh data yang terkait dengan aplikasi Live Studio kapan saja melalui langkah-langkah berikut:

#### Langkah 1: Menghapus Data Lokal pada Perangkat Android Anda
1. Buka **Pengaturan (*Settings*)** di HP Android Anda.
2. Pilih **Aplikasi (*Apps / Application Manager*)** -> Cari dan pilih **Live Studio**.
3. Pilih **Penyimpanan & Cache (*Storage & Cache*)**.
4. Ketuk **Hapus Data (*Clear Data / Clear Storage*)** dan **Hapus Cache (*Clear Cache*)**.
5. *Opsional*: Hapus penginstalan (*uninstall*) aplikasi Live Studio dari HP Anda.

#### Langkah 2: Mencabut Akses & Menghapus Sesi Facebook
Untuk mencabut akses aplikasi Live Studio dan menghapus token akses dari akun Facebook Anda:
1. Buka aplikasi **Facebook** atau masuk ke [facebook.com](https://www.facebook.com).
2. Buka **Pengaturan & Privasi (*Settings & Privacy*)** -> **Pengaturan (*Settings*)**.
3. Gulir ke bawah dan pilih **Aplikasi dan Situs Web (*Apps and Websites*)**.
4. Cari **Live Studio** dalam daftar aplikasi aktif.
5. Ketuk tombol **Hapus (*Remove*)** untuk mencabut seluruh izin dan menghapus keterhubungan aplikasi.

---

## English Version

### Data Privacy Commitment Statement
The **Live Studio** application operates **strictly client-side**. We **do not collect, store, or manage any user personal data on our own external servers**. Consequently, we do not maintain any cloud-based user databases.

Depending on your usage, the only data associated with the app includes:
1. **Facebook Access Token**: Stored locally on your device by the official Facebook SDK for authentication.
2. **Local Preferences**: Your last live stream title, privacy setting, and theme preferences saved locally on your device via Android DataStore.

---

### How to Delete Your Data

You can delete all data associated with the Live Studio application at any time by following these steps:

#### Step 1: Delete Local Data on Your Android Device
1. Open **Settings** on your Android device.
2. Go to **Apps / App Management** -> Locate and select **Live Studio**.
3. Tap **Storage & Cache**.
4. Tap **Clear Data / Clear Storage** and **Clear Cache**.
5. *(Optional)* Uninstall the Live Studio app from your device.

#### Step 2: Revoke Access & Remove Facebook Session
To revoke Live Studio's access and remove stored tokens from your Facebook account:
1. Open the **Facebook** app or log in to [facebook.com](https://www.facebook.com).
2. Go to **Settings & Privacy** -> **Settings**.
3. Scroll down and select **Apps and Websites**.
4. Locate **Live Studio** under active apps.
5. Click **Remove** to revoke all permissions and disconnect the application.
