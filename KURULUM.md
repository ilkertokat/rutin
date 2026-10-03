# 🔥 Rutin: Kurulum ve Kullanım

## Telefona kurulum
1. `Rutin.apk` dosyasını telefona at (USB, Drive, WhatsApp'ta kendine gönder…).
2. Dosyaya dokun. "Bilinmeyen kaynaklardan yükleme" izni isterse ver.
3. Play Protect uyarı verirse **Ayrıntılar → Yine de yükle** de. Uygulama Play Store dışından geldiği için bu uyarı her APK'da çıkar; zararlı olduğu anlamına gelmez.
4. İlk açılışta 3 dakikalık kurulum var: adın, uyku saatlerin, başlangıç alışkanlıkların, yoldaşın, koçun ve imzalı sözün.

## İlk gün mutlaka yap
- **Bildirim iznini** ver.
- **Pil optimizasyonundan muaf tut.** Kurulumda "İzin ver"e bas. Xiaomi, Huawei ya da Samsung kullanıyorsan ayrıca şuraya gir: Ayarlar → Uygulamalar → Rutin → Pil → **Kısıtlama yok**, ardından **Otomatik başlatma: açık**. Bunu yapmazsan hatırlatıcılar gecikebilir.
- **Widget ekle:** Ana ekrana uzun bas → Widget'lar → Rutin. Seri, Su (+250 ml butonlu) ve Bugün (işaretleme butonlu) widget'ları var.

## Neler var?
| Özellik | Nerede |
|---|---|
| Kademeli akıllı bildirimler (erken yaparsan susar, geciktikçe sıklaşır; su için beklenen seviyeye göre) | Otomatik |
| Bildirimden tek dokunuşla tamamlama, mini versiyon, erteleme, +250 ml | Bildirim butonları |
| 🔥 Seri, 🧊 dondurma, 🦅 seri onarma, 🟡 mini versiyonlar | Bugün ekranında alev simgesine dokun |
| 🛡️ Plan dışı olay (günü otomatik yeniden düzenler), hastalık ve tatil modu | Bugün ekranının altı / Ayarlar |
| ⏱️ Odak modu: pomodoro, ambiyans sesleri, odak ormanı, önerilen bloklar | Odak sekmesi |
| 📋 Şablon haftalar, haftalık planlama, haftalık rapor | Hafta sekmesi |
| ⭐ XP, seviyeler, 45 rozet (100'den fazla kademe), günlük 3 görev, haftalık meydan okuma | Bugün ve Ben |
| 🎡 Günlük şans çarkı, 🎯 günlük XP hedefi, 🏁 geçen haftaki sen ile yarış | Bugün |
| 🎁 Sandıklar, 🃏 36 koleksiyon kartı, 🛍️ mağaza (8 tema, 4 ses paketi, şapka ve aksesuarlar, ada yapıları) | Ben |
| 🐧 Yoldaş ve 🏝️ Hayat Adası (her iyi gün adaya bir yapı ekler) | Ben |
| 🌳 Alışkanlık seviyeleri (Tohum → Efsane), 🎓 alışkanlık mezuniyeti | Görev ayrıntısı |
| ☀️ Günaydın ekranı, 🌙 gün sonu özeti, ruh hali, "yarın ilk işim" niyeti | Bugün (sabah ve akşam) |
| 📊 Isı haritası, ruh hali ilişkileri, yaşam alanı radarı, rekorlar, ay özeti | İlerleme |
| 🍰 Kendi gerçek ödüllerin (coin karşılığı) | Ben → Ödüllerim |
| 💾 Yedekleme (dışa ve içe aktarma) | Ben → Ayarlar |

## Güncelleme
Yeni sürümü aynı yöntemle üzerine kur; verilerin korunur. APK hep aynı imza anahtarıyla derlenir (`%USERPROFILE%\.android-apk-tools\debug.keystore`). Bu anahtarı silme, yoksa güncelleme için önce uygulamayı kaldırman gerekir. Büyük değişikliklerden önce Ayarlar → Dışa aktar ile yedek almak iyi olur.

## Geliştirici notu
- Kaynak kod: `rutin/app/src`
- Derleme: `powershell -ExecutionPolicy Bypass -File rutin\build.ps1`. Önce 175 çekirdek testini çalıştırır, sonra imzalı APK üretir.
- Gizli test menüsü: Ayarlar'ın en altındaki sürüm yazısına 7 kez dokun. Zamanı ileri alma, coin ve XP ekleme seçenekleri burada.
