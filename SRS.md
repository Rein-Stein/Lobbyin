# Software Requirements Specification (SRS) - Lobbyin

## 1. Pendahuluan
### 1.1 Tujuan
Dokumen ini merinci kebutuhan sistem untuk aplikasi **Lobbyin**, sebuah platform manajemen peminjaman ruang kelas yang efisien untuk lingkungan akademis. Aplikasi ini dirancang untuk menggantikan proses manual dengan sistem reservasi berbasis digital.

### 1.2 Lingkup Produk
Lobbyin adalah aplikasi Android yang menyediakan antarmuka bagi mahasiswa dan dosen untuk melakukan booking ruangan secara real-time. Sistem menggunakan Firebase sebagai basis data pusat untuk memastikan sinkronisasi jadwal di seluruh perangkat pengguna.

## 2. Deskripsi Keseluruhan
### 2.1 Fitur Utama
*   **Sistem Autentikasi:** Login dan Registrasi pengguna dengan pemisahan peran (Mahasiswa, Dosen, Admin).
*   **Dashboard Interaktif:** Menampilkan jumlah total kelas dan sisa ruangan yang tersedia secara real-time.
*   **Reservasi Ruangan:** Pemilihan gedung (Gedung F), tanggal, dan sesi waktu (Sesi 1-4).
*   **Input Data Booking:** Pengisian Nama, NIM, dan Mata Kuliah untuk keperluan administrasi.
*   **Manajemen Admin:** Panel khusus untuk Admin menambah, mengedit, atau menghapus data ruangan (nama, lantai, kapasitas, fasilitas).
*   **Pembersihan Data Otomatis:** Menghapus data booking yang sudah lewat tanggalnya untuk mengoptimalkan database.

### 2.2 Perspektif Produk
Aplikasi ini beroperasi di sistem operasi Android (min SDK 24) dan memerlukan koneksi internet untuk sinkronisasi Firebase Realtime Database.

## 3. Kebutuhan Antarmuka Eksternal
*   **User Interface (UI):** Mengikuti standar Material Design 3 dengan skema warna biru dongker sesuai identitas logo Lobbyin.
*   **Database:** Firebase Realtime Database untuk data dinamis dan Room Database untuk persistensi lokal pengguna.

## 4. Aturan Bisnis
*   Satu ruangan tidak dapat dibooking oleh dua entitas pada sesi dan tanggal yang sama.
*   Admin memiliki otoritas penuh atas daftar inventaris ruangan.
*   Setiap sesi memiliki durasi waktu yang telah ditentukan (Contoh: Sesi 1 = 07:00 - 09:30).
