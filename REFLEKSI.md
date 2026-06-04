# REFLEKSI PROYEK LOBBYIN

### **PRAKTIK:**

*   **Lessons Learned:**
    Kami belajar mengintegrasikan **Firebase Realtime Database** untuk sinkronisasi data antar perangkat secara instan serta mengelola persistensi data lokal menggunakan **Room Database**. Kami juga memahami pentingnya manajemen *lifecycle* Android, terutama saat menangani input dinamis seperti pemilihan tanggal dan sesi waktu agar aplikasi tetap stabil.

*   **Daftar Bug & Kendala:**
    *   **Kendala:** Sinkronisasi format waktu dan zona waktu antar perangkat yang sempat menyebabkan kesalahan validasi jadwal (bentrok).
    *   **Bug:** Notifikasi terkadang tidak muncul pada versi Android terbaru (API 33 ke atas) karena memerlukan izin `POST_NOTIFICATIONS` yang harus ditangani secara eksplisit.
    *   **Kendala:** Penanganan *race condition* sederhana ketika dua pengguna mencoba menekan tombol "Booking" pada ruangan dan sesi yang sama di detik yang hampir bersamaan.

*   **Rencana Iterasi Berikutnya:**
    *   Menambahkan fitur **Riwayat Peminjaman** agar pengguna dapat melihat daftar ruangan yang pernah mereka pinjam.
    *   Implementasi **QR Code Scanner** untuk proses *check-in* saat pengguna sudah berada di ruangan yang dipesan.
    *   Meningkatkan sistem validasi di sisi *backend/rules* Firebase untuk memastikan tidak ada peminjaman ganda yang lolos.

*   **Catatan Etika & Privasi:**
    Aplikasi menjaga kerahasiaan data mahasiswa (NIM dan Nama) yang diinputkan. Data tersebut hanya digunakan untuk keperluan administrasi peminjaman ruangan kampus. Kami memastikan bahwa hak akses Admin dibatasi secara ketat untuk mencegah penyalahgunaan data atau penghapusan jadwal secara sepihak tanpa alasan yang valid.
