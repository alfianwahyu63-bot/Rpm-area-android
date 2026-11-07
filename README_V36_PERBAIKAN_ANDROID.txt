RPM AREA V36 - PERBAIKAN ANDROID
===================================
1. Force close saat menyimpan PNG: diperbaiki bug regex Java di Intent.ACTION_CREATE_DOCUMENT (PatternSyntaxException), serta alokasi kanvas dibatasi agar lebih ringan.
2. Bagikan foto+teks: memakai sistem share Android (content:// read-only) dengan gambar PNG dan caption, bukan navigator.share WebView.
3. Konten menabrak status bar: WebView diberi system bar/display cutout insets di Android 11+.

CARA MEMBANGUN APK LEWAT GITHUB
- Unggah seluruh isi ZIP ini ke root repository Android (app/, .github/workflows/, dll), tidak mengunggah ZIP sebagai satu file.
- Pastikan .github/workflows/build-apk.yml tersimpan (folder .github tersembunyi di sejumlah file manager).
- Di GitHub Actions pilih "Build RPM Area V36 APK", lalu "Run workflow".
- Buka job sukses dan download artifact RPM-Area-V36-APK; di dalamnya ada app-debug.apk.
- APK versi 36 memakai applicationId sama, sehingga dapat diinstal sebagai UPDATE di atas V35 jika tanda tangan debug tidak berubah. GitHub Actions debug key biasanya berubah tiap build; jika tanda tangan berbeda, Android menolak update. JANGAN uninstall V35 sebelum backup lokal JSON dan verifikasi cloud; data lokal bisa hilang saat uninstall.
- Pilih Simpan Gambar PNG untuk membuka dialog Save As di Android; Bagikan Foto+Teks membuka chooser aplikasi, pilih WhatsApp, dan periksa caption sebelum kirim.
- Jangan masukkan service_role Supabase ke APK, gunakan publishable key + RLS.

PENGUJIAN
- Source telah diperiksa dengan node --check dan parsing XML.
- Crash spesifik PatternSyntaxException sudah direproduksi melalui Java 21.
- Kode baru belum diuji di HP user maupun dikompilasi sebagai APK dalam lingkungan ini.
