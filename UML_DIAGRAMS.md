# Diagram UML - Lobbyin

Dokumen ini berisi representasi visual dari arsitektur dan fungsionalitas aplikasi Lobbyin.

## 1. Use Case Diagram
Diagram ini menunjukkan interaksi antara pengguna (Mahasiswa, Dosen, Admin) dan fungsi-fungsi utama dalam sistem.

```mermaid
graph LR
    %% Aktor
    M[Mahasiswa / Dosen]
    A[Admin]

    subgraph Lobbyin_System [Sistem Lobbyin]
        UC1([Login / Register])
        UC2([Cek Ketersediaan Ruangan])
        UC3([Booking Ruangan])
        UC4([Lihat Riwayat Booking])
        UC5([Kelola Data Ruangan])
        UC6([Auto-Cleanup Data])
    end

    %% Relasi User
    M --> UC1
    M --> UC2
    M --> UC3
    M --> UC4

    %% Relasi Admin
    A --> UC1
    A --> UC4
    A --> UC5
    
    %% Relasi Sistem
    UC6 -.->|Internal| Lobbyin_System
```

## 2. Class Diagram
Diagram ini menunjukkan struktur data dan kelas utama yang digunakan dalam pengembangan aplikasi.

```mermaid
classDiagram
    class User {
        +int id
        +String username
        +String password
        +String role
    }

    class ClassRoom {
        +String id
        +String name
        +int floor
        +String status
        +String capacity
        +String facilities
        +constructor()
    }

    class Booking {
        +String id
        +String room
        +String date
        +String time
        +String nama
        +String nim
        +String matkul
    }

    class Comment {
        +String role
        +String text
    }

    Booking "1" *-- "many" Comment : memiliki
    User "1" --> "many" Booking : melakukan
    Booking "many" o-- "1" ClassRoom : mereferensi
```


