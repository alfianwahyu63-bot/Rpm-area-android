RPM AREA V35 — MEMBUAT APK DI ANDROID STUDIO

STATUS: Source project siap diimpor. APK belum dibangun; environment ini tidak menyediakan Android SDK/Gradle dan internet SDK.

CARA A — GITHUB ACTIONS (tanpa PC, lewat HP):
1. Buat repository GitHub baru untuk source Android.
2. Upload SELURUH isi folder RpmArea-Android-Studio, termasuk folder .github/workflows.
3. Buka tab Actions > Build RPM Area V35 APK > Run workflow.
4. Setelah build sukses, buka run > Artifacts > RPM-Area-V35-APK.
5. Download ZIP artifact, ekstrak APK, lalu instal di Android.

CARA B — ANDROID STUDIO:
1. Instal Android Studio terbaru di komputer dengan koneksi internet.
2. File > Open > pilih folder RpmArea-Android-Studio.
3. Tunggu Gradle Sync. Instal Android SDK Platform 35 / Build Tools jika diminta.
4. Build > Build Bundle(s) / APK(s) > Build APK(s).
5. File debug APK biasanya di app/build/outputs/apk/debug/app-debug.apk.
6. Salin APK ke HP Android > instal (izinkan instal dari sumber ini).

PENTING:
- Web app yang dikemas dalam APK adalah V35 (aset lokal; bisa dibuka tanpa internet untuk data lokal). Cloud & login Supabase perlu internet.
- Data localStorage aplikasi dari URL GitHub Pages tidak otomatis berpindah ke APK. DOWNLOAD BACKUP JSON di browser lama, lalu RESTORE di APK, setelah backup kedua sisi.
- Bila sudah login Cloud di perangkat lain, periksa sinkronisasi sebelum impor / mengubah data; jangan memaksa merge bila konflik.
- Berkas PNG, backup JSON, CSV, XLSX memakai Android Save As dialog melalui jembatan WebView.
- Scan kamera bergantung dukungan BarcodeDetector di Android System WebView. Jika tak didukung, input PLU manual.
- Untuk membagikan WhatsApp, gunakan tombol salin atau buka tautan WhatsApp; sebagian perangkat WebView tidak mendukung native Web Share.
- Sebelum distribusi ke tim, uji beberapa perangkat dan tanda tangan APK release dengan keystore Anda sendiri.
- Jangan membagikan service_role key atau password pengguna dalam kode APK. Publishable key Supabase boleh ada, keamanan harus mengandalkan RLS.
