Melodial 🎵
Melodial adalah aplikasi pemutar musik Android yang mengintegrasikan fitur streaming dan manajemen musik lokal. Aplikasi ini dirancang dengan arsitektur yang efisien menggunakan sistem background processing untuk memastikan pengalaman pengguna yang lancar (smooth).
🚀 Fitur Utama
•
Sistem Favorit Offline: Menyimpan lagu favorit ke database lokal menggunakan SQLite/Room sehingga tetap dapat diakses tanpa koneksi internet.
•
Manajemen Playlist: Menambahkan lagu ke dalam daftar putar kustom.
•
Async Database Operations: Menggunakan ExecutorService untuk operasi database di background thread guna menghindari UI lag/freeze.
•
Manajemen User: Sistem login terintegrasi untuk sinkronisasi data favorit berbasis email pengguna.
•
UI Responsif: Implementasi ViewBinding untuk interaksi UI yang cepat dan aman.
•
Modern Tech Stack: Menggunakan Material Design 3 dan pustaka pemroses gambar Coil.
🛠️ Tech Stack
•
Language: Java & Kotlin
•
UI Framework: Android ViewBinding, Material Design 3, Jetpack Compose (BOM)
•
Local Database: Room Persistence Library & SQLite
•
Networking: Retrofit & OkHttp
•
Image Loading: Coil
•
Concurrency: Coroutines (Kotlin) & ExecutorService (Java)
•
Dependency Management: Firebase BOM
📂 Struktur Proyek
Java
app/src/main/java/com/example/
├── FavoriteFragment.java    # Logika pengelolaan lagu favorit
├── SongAdapter.java         # Adapter untuk list musik
├── DatabaseHelper.java      # Manajemen SQLite/Room
├── UserPreferences.java     # Penyimpanan sesi & email user
├── MainActivity.java        # Kontrol utama pemutar musik
└── ...
📝 Aturan Penulisan Commit (Semantic Commit)
Proyek ini mewajibkan penggunaan Semantic Commit Messages untuk menjaga riwayat perubahan yang rapi dan mudah dibaca, sesuai dengan standar CodePolitan.
Setiap pesan commit harus mengikuti format: <type>(<scope>): <subject>
Jenis Type yang Digunakan:
Type
Penjelasan
Contoh
feat
Menambah fitur baru
feat(fav): add toggle favorite functionality
fix
Memperbaiki bug
fix(player): resolve crash when skipping tracks
docs
Perubahan dokumentasi
docs(readme): add semantic commit guide
style
Perapian kode (formatting, missing semi-colons)
style(adapter): cleanup unused imports
refactor
Perubahan kode yang bukan fitur maupun perbaikan bug
refactor(db): use ExecutorService for background tasks
chore
Update build task, package manager, library
chore(deps): bump coil version to 2.7.0
test
Menambah atau mengubah unit test
test(auth): add login validation test
⚙️ Cara Menjalankan
1.
Clone Repositori
Shell Script
git clone https://github.com/username/melodial.git
2.
Buka di Android Studio Gunakan versi Android Studio Ladybug atau yang lebih baru.
3.
Sinkronisasi Gradle Pastikan semua dependencies terunduh dengan benar.
4.
Jalankan Aplikasi Gunakan Emulator dengan API level minimal 24.
🤝 Kontribusi
Jika Anda ingin berkontribusi:
1.
Fork repositori ini.
2.
Buat branch baru (git checkout -b feat/fitur-keren).
3.
Commit perubahan Anda menggunakan aturan Semantic Commit.
4.
Push ke branch tersebut (git push origin feat/fitur-keren).
5.
Buat Pull Request.
📄 Lisensi
Proyek ini berada di bawah lisensi MIT. Lihat file LICENSE untuk informasi lebih lanjut.
Melodial - Semester 4 Mobile Development Project.
