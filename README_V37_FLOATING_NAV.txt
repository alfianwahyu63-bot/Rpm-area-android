RPM AREA V37 – FLOATING PILL NAVIGATION

Perubahan (tampilan saja):
- Navigasi Android dan mobile web menjadi bilah gelap berbentuk kapsul dengan sudut penuh.
- Menu aktif naik dengan garis bingkai merah muda, titik indikator, dan animasi halus.
- Tujuh menu tetap: Home, Input, Target, Toko, Arsip, Analitik, Setelan.
- Aman untuk layar sempit (320px) dan memperhatikan area navigasi HP.
- Versi desktop tetap mempertahankan sidebar lama.
- Input, PLU, Target, data harian, Supabase, laporan WhatsApp tidak diubah.

BUAT APK:
1. Backup data aplikasi lebih dahulu, termasuk backup lokal/cloud Supabase.
2. Upload ISI zip sumber Android ke repository GitHub Android (root repository).
3. Buka Actions > Build RPM Area V37 APK > Run workflow.
4. Jika build sukses, unduh artifact RPM-Area-V37-APK dan ekstrak app-debug.apk.
5. Jika APK tidak mau update di atas V36, jangan hapus aplikasi lama sebelum data
   dan login tersimpan aman; instal debug APK dari tanda tangan berbeda mungkin perlu instal ulang.

Catatan: aplikasi tidak dibuild menjadi APK di sini, sehingga perlu uji langsung
pada HP. Cache Service Worker versi Web telah diubah agar style baru terambil.
