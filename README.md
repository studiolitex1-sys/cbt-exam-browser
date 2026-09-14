# CBT Exam Browser (Android)

Aplikasi Android Kios Ujian Sekolah berbasis Web CBT dengan proteksi anti-nyontek yang ketat dan panel pengawas cerdas.

## 🔐 Kredensial & Konfigurasi Bawaan
- **Token Masuk Ujian**: `132456` *(Dapat diubah, diacak, atau di-reset ke default di panel pengawas)*
- **Password Pengawas / Keluar Ujian**: `00132` *(Dapat diubah atau di-reset ke default di panel pengawas)*
- **URL Server CBT**: `http://192.168.10.99/cbt`
- **Akses Panel Pengawas**: Ketuk ikon perisai/kunci sebanyak 5 kali berturut-turut pada layar atau melalui tombol tersamar di bilah atas, lalu masukkan kata sandi pengawas (`00132`).

## 🛡️ Fitur Utama
1. **Sistem Gerbang Token Awal**:
   - Proteksi anti-nyontek belum aktif sebelum token valid dimasukkan.
   - Pilihan WiFi ruangan ujian (*Ruang 01* - *Ruang 15*) dengan format SSID `Ruang0<nomor>` dan sandi `<@Ruang0<nomor>>`.
   - Tombol salin sandi cepat dan pintasan langsung ke Pengaturan WiFi Android.
2. **Sistem Anti-Nyontek Ketat (Otomatis Aktif saat Ujian Dimulai)**:
   - **Screenshot & Screen Recording Blocker**: Proteksi `FLAG_SECURE` mencegah tangkapan layar, perekaman video, serta preview di recent apps.
   - **Anti Jendela Mengambang & Kehilangan Fokus**: Mendeteksi saat siswa mencoba membuka aplikasi lain, calculator floating window, split screen, atau menarik bilah notifikasi.
   - **Deteksi Koneksi VPN Real-time**: Memindai interface jaringan VPN/Proxy dan memblokir layar jika VPN diaktifkan.
   - **Deteksi Emulator**: Memastikan ujian hanya dijalankan di perangkat smartphone/tablet fisik asli.
   - **Web CBT Terisolasi**: Mematikan menu copy-paste, seleksi teks, serta menyediakan tombol reload cerdas saat server offline.
3. **Kata Sandi Keluar**: Siswa tidak dapat keluar dari aplikasi tanpa memasukkan kata sandi pengawas (`13456`).
4. **Panel Pengawas Rahasia**:
   - Anti-nyontek otomatis non-aktif tanpa perlu password saat pengawas masuk ke panel.
   - Diagnostik lengkap (IP, SSID WiFi, status baterai, VPN, opsi pengembang, USB debugging).
   - Log pelanggaran keamanan kronologis siswa.
   - Pengaturan fleksibel untuk mengubah token, password keluar, URL server, dan saklar anti-nyontek.

## 🚀 Cara Download APK
- Buka tab **Releases** atau **Actions** di GitHub repository ini:
  [https://github.com/studiolitex1-sys/cbt-exam-browser](https://github.com/studiolitex1-sys/cbt-exam-browser)
- Unduh file `app-debug.apk` dan pasang di perangkat Android Anda.
