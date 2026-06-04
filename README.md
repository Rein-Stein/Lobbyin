# Lobbyin - Room Booking System

<p align="center">
  <img src="app/src/main/res/drawable/logo_lobbyin.png" width="200" alt="Lobbyin Logo">
</p>

**Lobbyin** adalah aplikasi Android modern yang dirancang untuk menyederhanakan proses peminjaman ruang kelas di lingkungan kampus. Dengan fokus pada kemudahan penggunaan dan sinkronisasi real-time.

## Fitur Utama
*   **Real-time Dashboard:** Pantau jumlah kelas tersedia langsung dari layar utama.
*   **Flexible Booking:** Pilih tanggal dan sesi waktu yang diinginkan dengan mudah.
*   **Role-Based Access:** Fitur berbeda untuk Mahasiswa, Dosen, dan Admin.
*   **Admin Panel:** Kelola database ruangan (tambah, edit, hapus) langsung dari aplikasi.
*   **Auto-Maintenance:** Sistem secara otomatis menghapus data reservasi yang sudah usang setiap hari.
*   **Notifikasi:** Sistem akan memberikan notifikasi berhasil Booking 5 detik setelah Membooking.

## Panduan Instalasi
1.  **Clone atau Download:** Unduh source code project ini.
2.  **Buka di Android Studio:** Gunakan versi Android Studio Ladybug atau yang lebih baru.
3.  **Sync Gradle:** Pastikan koneksi internet stabil untuk mengunduh dependencies (Firebase, Room, Material Design).
4.  **Konfigurasi Firebase:** Pastikan file `google-services.json` yang valid tersedia di folder `app/`.
5.  **Run:** Klik tombol 'Run' dan pilih perangkat Android (fisik atau emulator).

## Catatan Rilis (v1.0.0)
*   Implementasi sistem login dan registrasi berbasis Role.
*   Integrasi Firebase Realtime Database untuk manajemen ruangan dan booking.
*   Fitur filter ketersediaan ruangan berdasarkan tanggal.
*   UI/UX berbasis Material 3 dengan tema gelap (Dark Blue).

## Lisensi
Project ini dikembangkan untuk tujuan edukasi dan manajemen internal kampus.
