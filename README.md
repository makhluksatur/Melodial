# 🎵 Melodial

**Melodial** adalah aplikasi pemutar musik Android yang mengintegrasikan fitur streaming dan manajemen musik lokal. Aplikasi ini dirancang dengan arsitektur yang efisien menggunakan sistem **background processing** untuk memastikan pengalaman pengguna yang lancar (*smooth*).

---

## 🚀 Fitur Utama

### ⭐ Sistem Favorit Offline

Menyimpan lagu favorit ke database lokal menggunakan **SQLite/Room** sehingga tetap dapat diakses tanpa koneksi internet.

### 🎶 Manajemen Playlist

Menambahkan lagu ke dalam daftar putar (*playlist*) kustom sesuai kebutuhan pengguna.

### ⚡ Async Database Operations

Menggunakan **ExecutorService** untuk menjalankan operasi database di *background thread* sehingga menghindari *UI lag* atau *freeze*.

### 👤 Manajemen User

Sistem login terintegrasi untuk sinkronisasi data favorit berdasarkan email pengguna.

### 📱 UI Responsif

Implementasi **ViewBinding** untuk interaksi antarmuka yang lebih cepat, aman, dan mudah dikelola.

### 🛠️ Modern Tech Stack

Menggunakan **Material Design 3** dan pustaka pemrosesan gambar **Coil** untuk tampilan yang modern dan performa optimal.

---

## 🛠️ Tech Stack

| Teknologi             | Keterangan                                                    |
| --------------------- | ------------------------------------------------------------- |
| Language              | Java & Kotlin                                                 |
| UI Framework          | Android ViewBinding, Material Design 3, Jetpack Compose (BOM) |
| Local Database        | Room Persistence Library & SQLite                             |
| Networking            | Retrofit & OkHttp                                             |
| Image Loading         | Coil                                                          |
| Concurrency           | Coroutines (Kotlin) & ExecutorService (Java)                  |
| Dependency Management | Firebase BOM                                                  |

---

## 📂 Struktur Proyek

```plaintext
app/src/main/java/com/example/
├── FavoriteFragment.java    # Logika pengelolaan lagu favorit
├── SongAdapter.java         # Adapter untuk list musik
├── DatabaseHelper.java      # Manajemen SQLite/Room
├── UserPreferences.java     # Penyimpanan sesi & email user
├── MainActivity.java        # Kontrol utama pemutar musik
└── ...
```

---

## 📝 Aturan Penulisan Commit (Semantic Commit)

Proyek ini mewajibkan penggunaan **Semantic Commit Messages** untuk menjaga riwayat perubahan yang rapi dan mudah dibaca, sesuai dengan standar CodePolitan.

Format commit yang digunakan:

```plaintext
<type>(<scope>): <subject>
```

### Jenis Commit

| Type     | Penjelasan                                           | Contoh                                                   |
| -------- | ---------------------------------------------------- | -------------------------------------------------------- |
| feat     | Menambah fitur baru                                  | `feat(fav): add toggle favorite functionality`           |
| fix      | Memperbaiki bug                                      | `fix(player): resolve crash when skipping tracks`        |
| docs     | Perubahan dokumentasi                                | `docs(readme): add semantic commit guide`                |
| style    | Perapian kode (formatting, missing semi-colons)      | `style(adapter): cleanup unused imports`                 |
| refactor | Perubahan kode yang bukan fitur maupun perbaikan bug | `refactor(db): use ExecutorService for background tasks` |
| chore    | Update build task, package manager, library          | `chore(deps): bump coil version to 2.7.0`                |
| test     | Menambah atau mengubah unit test                     | `test(auth): add login validation test`                  |

---

## ⚙️ Cara Menjalankan

### 1. Clone Repository

```bash
git clone https://github.com/username/melodial.git
```

### 2. Buka di Android Studio

Gunakan **Android Studio Ladybug** atau versi yang lebih baru.

### 3. Sinkronisasi Gradle

Pastikan seluruh dependency berhasil diunduh tanpa error.

### 4. Jalankan Aplikasi

Gunakan emulator atau perangkat Android dengan **minimum API Level 24**.

---

## 🤝 Kontribusi

Jika Anda ingin berkontribusi:

1. Fork repository ini.
2. Buat branch baru.

```bash
git checkout -b feat/fitur-keren
```

3. Commit perubahan menggunakan aturan **Semantic Commit**.
4. Push ke branch tersebut.

```bash
git push origin feat/fitur-keren
```

5. Buat Pull Request.

---

## 📄 Lisensi

Proyek ini menggunakan lisensi **MIT License**. Silakan lihat file **LICENSE** untuk informasi lebih lanjut.

---

<div align="center">

### 🎵 Melodial

*Semester 4 Mobile Development Project*

</div>
