# 🔥 RUTİN — Hayatını Düzene Sokan Uygulama
### Kapsamlı Ürün ve Teknik Plan (Android)

> **Hedef:** Rutini olmayan birinin; güzel görünen, motive eden, hatırlatan, affeden ama bırakmayan bir uygulama sayesinde yapması gereken her şeyi zamanında yapması ve plan dışı olaylara rağmen rutininin bozulmadan devam etmesi.

---

## İçindekiler
1. [Vizyon ve Temel İlkeler](#1-vizyon-ve-temel-i̇lkeler)
2. [Kullanılan Psikolojik Prensipler](#2-kullanılan-psikolojik-prensipler)
3. [Görev Modeli](#3-görev-modeli)
4. [Akıllı Bildirim Motoru](#4-akıllı-bildirim-motoru)
5. [Uyku Saati ve Gün Döngüsü](#5-uyku-saati-ve-gün-döngüsü)
6. [Plan Dışı Olaylar — Rutin Kalkanı](#6-plan-dışı-olaylar--rutin-kalkanı)
7. [Odak Modu (Pomodoro ve Günlük Çalışma Hedefi)](#7-odak-modu-pomodoro-ve-günlük-çalışma-hedefi)
8. [Şablon Hafta](#8-şablon-hafta)
9. [Motivasyon Sistemi (Maksimum Kapsam)](#9-motivasyon-sistemi-maksimum-kapsam)
10. [Analiz ve Raporlar](#10-analiz-ve-raporlar)
11. [Widget'lar](#11-widgetlar)
12. [Onboarding (İlk Açılış)](#12-onboarding-i̇lk-açılış)
13. [Tasarım Dili](#13-tasarım-dili)
14. [Ekranlar](#14-ekranlar)
15. [Teknik Mimari](#15-teknik-mimari)
16. [Veri Modeli](#16-veri-modeli)
17. [Yol Haritası](#17-yol-haritası)
18. [Etik Sınırlar ve Tükenmişlik Koruması](#18-etik-sınırlar-ve-tükenmişlik-koruması)
19. [Açık Sorular](#19-açık-sorular)

---

## 1. Vizyon ve Temel İlkeler

| İlke | Anlamı |
|---|---|
| **Önce rutin, sonra verim** | Hedef "çok iş yapmak" değil, *her gün aynı çekirdeği* yapabilmek. Verim rutinden kendiliğinden gelir. |
| **Asla iki kez kaçırma** | Bir gün kaçırmak normaldir. Uygulama kullanıcıyı suçlamaz, ertesi gün geri dönmesini kolaylaştırır. |
| **Minimum her zaman sayılır** | Her alışkanlığın bir "tam" ve bir "mini" versiyonu vardır. Kötü bir günde mini versiyonu yapmak da seriyi korur. |
| **Tek dokunuş** | Bir işi tamamlamak için en fazla 1 dokunuş gerekir (bildirimden, widget'tan ya da uygulamadan). |
| **Zaman daraldıkça ısrar artar** | Erken yapan rahat bırakılır. Geciken nazikçe, sonra kararlılıkla dürtülür. |
| **Güzel olmalı** | Açmak keyif vermeli. Her tamamlama küçük bir kutlama olmalı. |
| **Kullanıcı patron** | Her şey eklenebilir, silinebilir, taşınabilir. Uygulama önerir ama dayatmaz. |

---

## 2. Kullanılan Psikolojik Prensipler

Uygulamanın her özelliği en az bir prensibe dayanır:

| # | Prensip | Uygulamadaki karşılığı |
|---|---|---|
| 1 | **Kayıptan kaçınma** (Loss Aversion) | 🔥 Seri. İnsanlar kazanmaktan çok kaybetmemek için çalışır. "32 günlük serini kaybetme!" |
| 2 | **Verilmiş ilerleme etkisi** (Endowed Progress) | Onboarding bitince seri 1'den başlar ve ilk rozet hemen verilir. İlerleme çubukları asla %0'dan başlamaz. |
| 3 | **Hedef eğimi** (Goal Gradient) | Hedefe yaklaştıkça motivasyon artar: "Son 500 ml! 🏁". Son %20'de çubuk parlar. |
| 4 | **Zeigarnik etkisi** | Yarım kalan halka her zaman göze batar. Widget'taki eksik halka tamamlanmak ister. |
| 5 | **Değişken ödül** (Variable Rewards) | Tamamlanan günlerde açılan sürpriz sandıklar. Ne çıkacağı belli değildir. |
| 6 | **Uygulama niyetleri** (Implementation Intentions) | "Eğer ___ olursa, ___ yapacağım." Her alışkanlık bir tetikleyiciye bağlanır. |
| 7 | **Alışkanlık zincirleme** (Habit Stacking) | "Sabah kahvesinden sonra → 1 bardak su". Zincirdeki bir iş bitince sıradaki önerilir. |
| 8 | **2 dakika kuralı** | Mini versiyon: "Sadece spor kıyafetini giy." Başlamak işin yarısıdır. |
| 9 | **Kimlik temelli alışkanlık** | "Ben düzenli çalışan biriyim." Her tamamlama o kimliğe bir oy olarak gösterilir. |
| 10 | **Taze başlangıç etkisi** (Fresh Start) | Pazartesi, ayın 1'i ve doğum günü "yeni sayfa" anlarıdır ve özel ekranlarla kutlanır. |
| 11 | **Zirve-son kuralı** (Peak-End) | Gün her zaman olumlu biter: gün sonu özeti en iyi anı öne çıkarır. |
| 12 | **Tutarlılık ve taahhüt** | Onboarding'de parmakla imzalanan kişisel söz. Haftalık hedefler sesli/yazılı taahhütle başlar. |
| 13 | **İlerleme ilkesi** (Progress Principle) | Küçük kazanımların görünür olması en büyük motivasyon kaynağıdır. Her şey görselleştirilir. |
| 14 | **Özerklik, yetkinlik, aidiyet** (Öz Belirleme Kuramı) | Kullanıcı seçer (özerklik), zorluk kendini ayarlar (yetkinlik), arkadaşla paylaşım (aidiyet). |
| 15 | **IKEA etkisi** | Kullanıcı yoldaşını ve adasını kendisi büyütüp süsler, bu yüzden bırakması zorlaşır. |
| 16 | **Cazibe paketleme** (Temptation Bundling) | "Sevdiğin podcast sadece yürürken" gibi eşleştirmeler önerilir. |
| 17 | **Sürtünmeyi azaltma** | Widget'tan tek dokunuşla tamamlama, bildirimde "+250 ml" butonu. |
| 18 | **Öz şefkat** | Kaçırılan günlerde utandırma yok: "Olur böyle. Yarın mini versiyonla dönelim mi?" |
| 19 | **Akış** (Flow) | Zorluk kullanıcının seviyesine göre otomatik artar ya da azalır. |
| 20 | **Gelecekteki ben** | "1 yıl sonraki sana mektup" yazılır ve kilometre taşlarında açılır. |

---

## 3. Görev Modeli

### 3.1 Görev türleri

| Tür | İkon | Açıklama | Örnek |
|---|---|---|---|
| **Çapa Rutin** | ⚓ | Sabit saatli ve her gün aynı zamanda yapılır. Rutinin iskeletidir. | 07:30 kalk, 08:00 kahvaltı, 23:30 yat |
| **Zorunlu Görev** | 🔒 | O gün mutlaka yapılmalıdır. Saati esnek olabilir ama bir son saati vardır. | Faturayı öde (18:00'e kadar) |
| **Sayaçlı Hedef** | 💧 | Gün boyunca biriktirilen miktar. | 4 L su, 10.000 adım, 20 sayfa kitap |
| **Süreli Hedef** | ⏱️ | Toplam süre hedefi, Odak Modu ile bağlantılıdır. | Günde 4 saat ders |
| **Esnek Görev** | 🌿 | Boş zamanlar için. Yapılırsa bonus kazandırır, yapılmazsa ceza yoktur. | Oda toplamak, film izlemek |
| **Tek Seferlik** | 📌 | Tekrarlanmayan iş. | Doktor randevusu |

### 3.2 Her görevin özellikleri

```
Başlık, ikon, renk, yaşam alanı (Sağlık / Zihin / Kariyer / Ev / Sosyal / Ruh)
Tür (yukarıdaki)
Hedef saat (T)              → "genelde bu saatte yapılır"
Erken pencere               → T'den ne kadar önce yapılabilir (varsayılan 60 dk)
Tolerans (grace)            → T'den sonra rahatsız etmeden beklenen süre (varsayılan 15 dk)
Son saat (D)                → Bu saatten sonra "kaçırıldı" sayılır
Tam versiyon / Mini versiyon → "4 L su" / "2 L su", "4 saat" / "1 saat"
Tekrar kuralı               → her gün, hafta içi, Pzt-Çrş-Cum, ayda 1…
Tetikleyici (opsiyonel)     → "X bittikten sonra" (alışkanlık zinciri)
Eğer-ise planı (opsiyonel)  → "Eğer yağmur yağarsa evde 20 dk esneme"
Neden kartı (opsiyonel)     → "Bunu neden yapıyorum?" Zor anlarda gösterilir
Öncelik                     → Kritik / Normal / Düşük (bildirim sıklığını etkiler)
Seri etkisi                 → Seriyi etkiler mi? (zorunlu ve çapa görevler: evet)
```

### 3.3 Durumlar

`Bekliyor → Aktif (pencere açık) → Gecikiyor → Kritik → ✅ Tamamlandı / 🟡 Mini tamamlandı / ⏭️ Ertelendi / ❌ Kaçırıldı / 🛡️ Korundu (Kalkan)`

---

## 4. Akıllı Bildirim Motoru

Uygulamanın kalbi burası. Amaç: **erken yapan rahat bırakılır, geciken giderek daha sık uyarılır, ama kullanıcı asla bildirim yağmuruna tutulmaz.**

### 4.1 Saatli görevler (Çapa ve Zorunlu) — Kademeli yükselme

Zaman çizelgesi (T = hedef saat, D = son saat):

```
  T-60dk        T-10dk      T      T+15dk            %50 kalan       %20 kalan       D
    |─── erken ───|──────────|────────|────────────────|───────────────|──────────────|
    │  sessiz,    │ Seviye 0 │Seviye 1│    Seviye 2    │   Seviye 3    │   Seviye 4   │
    │  yapılabilir│ "hazırlan"│"zamanı"│  her 30 dk     │  her 15 dk    │ her 5-10 dk  │
```

| Seviye | Ne zaman | Sıklık | Bildirim stili |
|---|---|---|---|
| **0 – Hazırlık** | T - 10 dk | 1 kez | Sessiz, küçük. "10 dk sonra: Kitap okuma 📖" |
| **1 – Zamanı** | T | 1 kez | Normal ses ve titreşim. "Kitap zamanı! 20 sayfa seni bekliyor." |
| **2 – Nazik dürtme** | T + tolerans | 30 dk'da bir | Normal. "Hâlâ vaktin var 🙂" + **Neden kartı** |
| **3 – Israr** | Kalan süre < %50 | 15 dk'da bir | Heads-up (ekranın üstünde açılır). "Serin bu göreve bağlı 🔥" |
| **4 – Kritik** | Kalan süre < %20 | 5-10 dk'da bir | Heads-up + özel ses + güçlü titreşim. Kritik görevlerde tam ekran hatırlatma (alarm gibi). "Son 30 dk! Mini versiyonu yapsan bile seri kurtulur." |

**Erken tamamlanırsa:** Planlanmış tüm bildirimler iptal edilir, başarı sesi çalar ve **+%20 "Erkenci" XP bonusu** verilir.

**Her bildirimde butonlar:**
- ✅ `Tamamladım`
- 🟡 `Mini yaptım`
- ⏰ `15 dk ertele`: Erteleme hakkı sınırlıdır (günde 3). Her erteleme bir sonraki bildirimi daha ısrarcı yapar.
- 🛡️ `Plan dışı olay`: Rutin Kalkanı'nı açar (bkz. Bölüm 6).

### 4.2 Sayaçlı hedefler (su vb.) — Beklenen ilerleme eğrisi

Gün içindeki "olması gereken" miktar lineer bir eğriyle hesaplanır:

```
aktif_başlangıç = uyanma saati
aktif_bitiş     = yatma saati - 60 dk
beklenen(t)     = hedef × (t - aktif_başlangıç) / (aktif_bitiş - aktif_başlangıç)
açık            = beklenen(t) - gerçekleşen
açık_oranı      = açık / hedef                     (0..1)

eğer açık ≤ 0          → bildirim YOK (öndesin, harika!)
aralık (dk)            = clamp( 90 × (1 - 2.5 × açık_oranı), 10, 90 )
```

| Durum | Örnek (4 L hedef) | Bildirim aralığı |
|---|---|---|
| Öndesin | Saat 14:00, beklenen 1.8 L, içilen 2.0 L | Yok 🎉 |
| Hafif geride | Beklenen 1.8 L, içilen 1.5 L (%7,5 açık) | ~73 dk |
| Geride | Beklenen 2.8 L, içilen 1.5 L (%32 açık) | ~18 dk |
| Çok geride | Akşam 20:00, beklenen 3.6 L, içilen 1.2 L | 10 dk (en sık) |

**Akıllı dokunuşlar:**
- Bildirimde tek dokunuşla **`+250 ml`** ve **`+500 ml`** butonları bulunur.
- Hedef-eğim mesajları: "Yarıyı geçtin! 💪", "Son 2 bardak! 🏁"
- Yatmaya 2 saat kala hedefin ulaşılamaz olduğu anlaşılırsa: "Bugün 3 L'ye ulaşırsan mini hedef tamam sayılır 🟡"
- Açık çok büyükse miktarı akşama yığmaz, gerçekçi bir mini hedef önerir (gece 2 L su içmek sağlıksızdır).

### 4.3 Süreli hedefler (günde 4 saat çalışma)

```
kalan_iş   = hedef_süre - tamamlanan_odak_süresi
kalan_zaman = yatma_saati - şimdi - planlı_zorunlu_işler
sıkışıklık  = kalan_iş / kalan_zaman     (0..1+)
```

| Sıkışıklık | Davranış |
|---|---|
| < 0.3 | Planlı blok saatinde tek hatırlatma |
| 0.3 – 0.6 | Her boşlukta "25 dk'lık bir blok sığar, başlayalım mı?" |
| 0.6 – 0.9 | Heads-up bildirimi ve takvimdeki boşlukları otomatik bloklama önerisi |
| > 0.9 | "Tam hedef bugün zor. Mini hedefe (1 saat) odaklanalım mı?" |

### 4.4 Bildirim bütçesi (spam koruması)

- **Saatlik üst sınır:** En fazla 4 bildirim (Kritik seviye hariç).
- **Gruplama:** Aynı anda 3 geciken iş varsa tek bildirim gelir: "3 iş seni bekliyor: 💧 Su, 📖 Kitap, 🏃 Yürüyüş".
- **Sessiz saatler:** Yatma saatinden uyanma saatine kadar hiçbir bildirim gelmez (alarm görevleri hariç).
- **Odak sırasında:** Pomodoro aktifken sadece Kritik seviye bildirimler gelir, diğerleri mola vaktine ertelenir.
- **Öğrenen zamanlama:** Kullanıcının hangi saatlerdeki bildirimlere tepki verdiği kaydedilir. Hiç tepki alınmayan saatlerde bildirim azaltılır, en çok tepki alınan saate "altın saat" denir ve önemli hatırlatmalar oraya kaydırılır.
- **Otomatik rutin öğrenme:** Bir görev hep 21:40 civarında yapılıyorsa ve hedef saati 20:00 ise öneri çıkar: "Kitabı hep 21:40'ta okuyorsun, hedef saati güncelleyelim mi?"

### 4.5 Mesaj tonu

Mesajlar **rastgele havuzlardan** seçilir (aynı mesaj üst üste gelmez). Seviye yükseldikçe ton değişir:

| Seviye | Ton | Örnek |
|---|---|---|
| 1 | Neşeli | "Su molası! 💧 Vücudun teşekkür edecek." |
| 2 | Hatırlatıcı | "Kitabın seni özledi 📖 Sadece 5 sayfa?" |
| 3 | Kararlı | "🔥 23 günlük serin bu göreve bağlı." |
| 4 | Acil ve destekleyici | "Son 20 dk. Mini versiyon bile yeter, hadi!" |

Kullanıcı ayarlardan bir **"Koç kişiliği"** seçer: 🤗 Nazik Arkadaş / 🎖️ Askeri Komutan / 😏 Esprili / 🧘 Sakin Bilge. Tüm mesajlar seçilen kişiliğe göre yazılır.

---

## 5. Uyku Saati ve Gün Döngüsü

Uyku rutinin temelidir. Gün uyku etrafında kurulur:

| Zaman | Olay |
|---|---|
| **Uyanma** | Günaydın ekranı: bugünün 3 önceliği, seri durumu, hava durumu (opsiyonel), günün sözü. "Güne başla" butonu ile gün başlar (Taze başlangıç). |
| **Yatma - 90 dk** | "Akşam kontrolü": kalan zorunlu işlerin listesi. Neyi mini versiyonla kurtarabileceğin önerilir. |
| **Yatma - 60 dk** | **Rüzgar Azaltma modu:** Uygulama karanlık ve sıcak tonlara geçer. Yeni iş önerilmez. "Ekranı bırakma" hatırlatması gelir. |
| **Yatma - 15 dk** | Gün sonu özeti (Zirve-Son): günün en iyi anı, kazanılan XP, seri durumu ve yarın için tek bir niyet ("Yarın ilk işim: ___"). Ruh hali seçimi: 😞 😐 🙂 😄 🤩 |
| **Yatma saati** | Sessiz saatler başlar. Yapılmayan işler **kınanmadan** "Yarına taşı / Bırak" seçenekleriyle arşivlenir. |
| **Uyku takibi** | Ertesi sabah "Kaçta uyudun?" diye sorulur (ya da telefonun kullanılmadığı süreden tahmin edilir). Tutarlı uyku saati ayrı bir seri olarak da sayılır. |

**Gün sınırı:** Gün gece yarısında değil, **kullanıcının yatma saatinde** biter. Gece 00:30'da yatan birinin 00:15'te yaptığı iş "dün"e sayılır.

---

## 6. Plan Dışı Olaylar — Rutin Kalkanı

Hayat plana uymaz. Bu bölüm, rutini olmayan birinin **ilk sürprizde her şeyi bırakmasını** engellemek için var.

### 6.1 "🛡️ Plan Dışı Olay" butonu
Ana ekranda, bildirimde ve widget'ta bulunur. Kullanıcı şunu seçer:

1. **Ne oldu?** Hastalık / Misafir / Acil iş / Yolculuk / Yorgunluk / Diğer
2. **Ne kadar sürecek?** 30 dk / 1 saat / 2 saat / Günün geri kalanı / Birkaç gün

### 6.2 Otomatik yeniden planlama

Uygulama şu sırayla **akıllı kaydırma** yapar:

1. **Esnek görevler** önce feda edilir ve yarına ya da haftanın boş bir gününe kaydırılır.
2. **Saatli görevler** olayın bitişinden sonraki ilk boşluğa kaydırılır. Son saatleri korunur.
3. **Süreli hedefler** kalan zamana göre sıkıştırılır (4 saat yerine 2 saat) ya da mini versiyona düşürülür.
4. **Çapa rutinler** mümkünse korunur. Uyku saati **asla** kaydırılmaz.
5. Kullanıcıya bir **önizleme** gösterilir: "Şöyle yeniden düzenledim, onaylıyor musun?" Kullanıcı sürükle-bırak ile düzeltebilir.

### 6.3 Seri koruması

- Plan dışı olay bildirilen günde **mini versiyonlar yapılırsa seri devam eder** (🟡 yarım alev).
- "Günün geri kalanı" seçildiyse ve hiçbir şey yapılamıyorsa, varsa **Seri Dondurma** otomatik kullanılır.
- **Hastalık modu:** Birkaç günlük hastalıkta tüm hedefler "iyileşme" versiyonuna düşer (su, uyku, ilaç). Seri dondurulur ama dondurma hakkı harcanmaz.
- **Tatil modu:** Önceden planlanır ve seri durdurulur, kaybedilmez.

### 6.4 Geri dönüş protokolü
Kullanıcı 2 veya daha fazla gün uygulamayı açmazsa:
- Suçlama yok: "Tekrar hoş geldin! 👋 En zor adım geri dönmekti, onu yaptın."
- **Kolay Başlangıç günü:** Sadece 3 çapa rutin ve mini versiyonlar. Başarıyla biterse **"Geri Dönüş" rozeti** ve 2 kat XP verilir.
- **Seri Onarma:** Seri kırıldıysa 48 saat içinde "Onarma Görevi" açılır (örneğin 2 gün boyunca tüm zorunlu işler tam). Başarılırsa eski seri geri gelir.

---

## 7. Odak Modu (Pomodoro ve Günlük Çalışma Hedefi)

### 7.1 Günlük süre hedefi
"Günde 4 saat İngilizce çalışacağım" dendiğinde uygulama şunları yapar:
- Hedefi **bloklara böler**: 4 saat ≈ 8 × 25 dk odak + molalar.
- Takvimdeki boşluklara **otomatik yerleştirme** önerir (tercih edilen saatlere göre: sabah insanı mı, gece kuşu mu?).
- Ana ekranda **ilerleme halkası** gösterir: "2s 15dk / 4s".

### 7.2 Odak oturumu
- **Tek dokunuşla başlat:** Göreve basılır, sayaç başlar.
- **Süre seçenekleri:** 25/5 (klasik), 50/10 (derin), 90/20 (ultra) ya da özel.
- **Ekran:** Büyük, sakin, animasyonlu bir halka. Arka planda yavaşça büyüyen bir ağaç ya da kristal.
- **Ambiyans sesleri:** Yağmur, kafe, orman, beyaz gürültü, lo-fi (uygulama içinde, internetsiz).
- **Rahatsız Etme:** Oturum başlarken telefonun DND modu açılabilir (izin verilirse).
- **Uygulama kalkanı (opsiyonel):** Oturum sırasında seçilen dikkat dağıtıcı uygulamalar (Instagram, TikTok…) açılınca tam ekran uyarı çıkar: "Odaktasın 🌱 12 dk kaldı. Gerçekten açmak istiyor musun?"
- **Ağaç kuralı:** Oturum yarıda bırakılırsa büyüyen bitki solar. Tamamlanırsa kullanıcının **Odak Ormanı**na eklenir.
- **Mola ekranı:** Hareket, su ve göz dinlendirme önerileri. Mola sırasında "+250 ml su" butonu da çıkar (alışkanlık zincirleme).
- **Oturum sonu:** "Ne kadar odaklandın?" (1-5) ve "Ne yaptın?" (kısa not). Bu veriler raporlarda kullanılır.

### 7.3 Çalışmaya yardım
- **Oturum niyeti:** Başlamadan önce "Bu 25 dk'da ne bitireceğim?" sorulur. Bitince "Bitirdin mi?" diye sorulur.
- **Isınma:** Başlamakta zorlanana "Sadece 5 dk" modu sunulur. 5 dk sonra "Devam mı?" diye sorulur. Çoğu zaman devam edilir (2 dakika kuralı).
- **Derin iş istatistiği:** Hangi saatte en yüksek odak puanını aldığın bulunur, planlama o saatlere göre önerilir.
- **Konu bazlı takip:** Matematik 12 saat, İngilizce 8 saat… Haftalık dağılım gösterilir.

---

## 8. Şablon Hafta

- Kullanıcı bir **ideal hafta** tasarlar (çapa rutinler, çalışma blokları, spor günleri…).
- **Birden fazla şablon** olabilir: 🎓 Okul Haftası, 💼 İş Haftası, 📚 Sınav Haftası, 🏖️ Tatil, 🤒 Hasta.
- Her pazartesi (ya da seçilen günde) aktif şablon **otomatik yüklenir**. Pazar akşamı hafta planlama ekranı açılır: "Gelecek hafta hangi şablon? Özel bir durum var mı?"
- **Haftaya özel istisnalar** şablonu bozmaz: "Bu çarşamba doktor var" sadece o haftaya eklenir.
- **Şablonu güncelle:** Bir haftada yapılan değişiklik beğenildiyse "Bunu şablona da kaydet" seçeneği sunulur.
- İşe girildiğinde yeni bir "İş Haftası" şablonu oluşturulur ve eski çapa rutinler yeni saatlere sürüklenir. Alışkanlıklar ve seriler korunur.
- **Hazır şablon kütüphanesi:** "Öğrenci", "Erken kalkan", "Ofis çalışanı", "Serbest çalışan". Başlangıç için seçilip düzenlenebilir.

---

## 9. Motivasyon Sistemi (Maksimum Kapsam)

### 9.1 🔥 Seriler
| Seri | Kural |
|---|---|
| **Ana Seri** | O gün tüm zorunlu ve çapa görevler (en az mini versiyonla) tamamlandıysa +1 |
| **Alışkanlık serileri** | Her alışkanlığın kendi serisi (Su: 45 gün, Kitap: 12 gün…) |
| **Mükemmel Gün serisi** | Tüm görevlerin tam versiyonla yapıldığı art arda günler (altın alev ✨🔥) |
| **Uyku serisi** | Yatma saatine ±30 dk uyulan günler |

- **Alev evrimi:** Seri uzadıkça alevin görünümü değişir: 🔥 Turuncu (1-6) → 🔥 Kırmızı (7-29) → 💜 Mor (30-99) → 💙 Mavi (100-364) → ⭐ Yıldız alevi (365+).
- **Seri Dondurma ❄️:** Haftada 1 ücretsiz hak. Coin ile en fazla 2 tane daha alınabilir.
- **Seri Onarma:** Bkz. 6.4.
- **Kilometre taşları:** 3, 7, 14, 21, 30, 50, 66 ("alışkanlık oluştu" günü), 100, 200, 365. Her birinde tam ekran kutlama animasyonu gösterilir.

### 9.2 ⭐ XP ve Seviye
| Eylem | XP |
|---|---|
| Çapa rutin (zamanında) | 15 |
| Zorunlu görev | 10 |
| Mini versiyon | 5 |
| Esnek görev | 5 |
| Odak oturumu (25 dk) | 10 |
| Erken tamamlama bonusu | +%20 |
| Kombo (art arda 3 görev, 1 saat içinde) | x1.5 |
| Mükemmel Gün | +50 |

- **Seviye unvanları:** Filiz 🌱 → Çırak → Kararlı → Disiplinli → Usta → Bilge → Efsane 👑
- Her seviyede **bir şey açılır**: yeni tema, yeni başarı sesi, yeni yoldaş aksesuarı, yeni widget stili.

### 9.3 🧬 Yaşam Alanları (RPG İstatistikleri)
Her görev bir yaşam alanına XP verir. Profilde bir **karakter kartı** ve **radar grafiği** gösterilir:

`❤️ Sağlık  🧠 Zihin  💼 Kariyer  🏠 Ev  👥 Sosyal  🕊️ Ruh`

- Dengesizlik fark edilirse öneri çıkar: "Sosyal alanın bu hafta düşük, bir arkadaşını arasan?"
- Her alanın kendi seviyesi vardır: "Sağlık Lv.12, Kariyer Lv.8".

### 9.4 🐣 Yoldaş (Sanal Evcil Hayvan)
- Onboarding'de bir yoldaş seçilir ve kullanıcı adını koyar (IKEA etkisi).
- **Kullanıcının rutinleriyle büyür:** Su içtikçe yoldaşın bitkisi sulanır, uyku düzenli oldukça yoldaş dinlenmiş görünür, çalıştıkça kütüphanesi büyür.
- **Ruh hali** kullanıcının gününü yansıtır. Asla ölmez ya da cezalandırılmaz (öz şefkat), sadece "seni özledi" der.
- Coin ile **kıyafet, şapka ve ev eşyası** alınabilir.
- Bildirimlerde yoldaş konuşur: "🐧 Pofuduk: Hadi birlikte su içelim!" Bu, bildirimi kişisel ve sevimli yapar.

### 9.5 🏝️ Hayat Adası
- Her tamamlanan gün adaya bir şey ekler: ağaç, ev, köprü, deniz feneri…
- 30 günde küçük bir köy, 365 günde bir şehir oluşur.
- Odak Ormanı (Bölüm 7) bu adanın bir parçasıdır.
- Kullanıcının **hayatının görsel bir özeti** olur ve bırakması zorlaşır.

### 9.6 🎁 Sandıklar ve Değişken Ödüller
- Mükemmel Gün sonunda bir sandık açılır.
- **İçerik (rastgele):** Coin, seri dondurma, nadir yoldaş aksesuarı, XP iksiri (1 saat 2x XP), yeni ses paketi, nadir ada yapısı.
- **Nadirlik seviyeleri:** Sıradan ⚪ / Nadir 🔵 / Epik 🟣 / Efsanevi 🟡.
- Sandık açma animasyonu çok tatmin edici olmalı: titreme, parlama ve özel ses.

### 9.7 🪙 Coin Ekonomisi
- Görevler, rozetler ve sandıklardan kazanılır.
- **Harcama alanları:** Tema, başarı sesleri, widget kaplamaları, yoldaş eşyaları, ada yapıları, seri dondurma.
- **Kişisel ödül mağazası:** Kullanıcı kendi gerçek ödüllerini tanımlar. "500 coin = Sevdiğim tatlıyı yerim 🍰", "2000 coin = Yeni oyun alırım 🎮". Bu, uygulama içi motivasyonu gerçek hayata bağlar.

### 9.8 🏅 Rozetler (100+)
Kategoriler ve örnekler:

| Kategori | Örnekler |
|---|---|
| **Seri** | Kıvılcım (3 gün), Ateş (7), Yangın (30), Volkan (100), Güneş (365) |
| **Su** | İlk Damla, Nehir (7 gün tam), Okyanus (toplam 1000 L) |
| **Odak** | İlk Pomodoro, Maratoncu (tek günde 6 saat), Derin Dalış (90 dk kesintisiz), 100 Saat Kulübü |
| **Zamanında** | Erkenci Kuş (10 kez erken tamamlama), Dakik (1 hafta hiç gecikme yok) |
| **Dayanıklılık** | Geri Dönüş (ara verdikten sonra dönüş), Anka Kuşu (onarılan seri), Fırtınada Bile (plan dışı olaya rağmen seri) |
| **Uyku** | Uyku Ustası (14 gün düzenli uyku) |
| **Gizli rozetler 🤫** | Şartı gizlidir. Örnek: "Gece Yarısı Kahramanı" (23:59'da kurtarılan seri), "Hafta Sonu Savaşçısı". Merak uyandırır. |

Rozetlerin **bronz, gümüş, altın ve elmas** seviyeleri vardır.

### 9.9 🎯 Görevler ve Meydan Okumalar
- **Günlük 3 Görev:** Her sabah rastgele 3 mini görev gelir ("Öğleden önce 1 L su", "2 pomodoro yap", "Bir görevi erken bitir"). Üçü de tamamlanırsa bonus sandık kazanılır.
- **Haftalık Meydan Okuma:** "Bu hafta her gün 10 dk yürü" gibi. Kazanılırsa özel rozet verilir.
- **Aylık Tema:** "Ekim: Okuma Ayı 📚". Ay boyunca okuma görevleri ekstra XP verir.
- **Sezonluk Etkinlikler:** "Kış Disiplini" etkinliği sırasında sınırlı süreli kozmetikler kazanılabilir (kıtlık etkisi).

### 9.10 🧠 Kimlik ve Anlam
- **Kimlik cümleleri:** Onboarding'de kullanıcı "Ben ___ biriyim" cümleleri yazar ("sağlığına önem veren", "her gün çalışan"). Görev tamamlanınca bu cümle kısa bir süre görünür: "Kimliğine +1 oy ✔️ Her gün çalışan biri".
- **Neden kartları:** Her alışkanlık için "Neden?" yazılır. Motivasyon düştüğünde (gecikmeler arttığında) bildirimde ve ekranda gösterilir.
- **Gelecekteki sana mektup:** 30, 100 ve 365. günde açılır.
- **Söz imzası:** Onboarding'de parmakla imza atılır. Bu imza zor anlarda gösterilir: "Buna söz vermiştin ✍️".
- **Vizyon panosu:** Hedef fotoğraflarından bir pano oluşturulur. Günaydın ekranında dönüşümlü olarak gösterilir.

### 9.11 🎉 Kutlama Tasarımı (Mikro Ödüller)
Her tamamlamanın kendine göre tepkisi olmalı:

| Olay | Görsel | Ses | Titreşim |
|---|---|---|---|
| Görev tamam | Kutu yeşile döner, parçacık patlaması | Kısa "ding" | Hafif tık |
| Sayaç artışı (+250 ml) | Su dalgası animasyonu | "Blup" | Mikro |
| Hedef tamam (4 L) | Konfeti + halka dolma | **Başarı melodisi** 🎺 | Çift titreşim |
| Kombo | "KOMBO x1.5!" yazısı | Yükselen notalar | Ritmik |
| Seri +1 | Alev büyür, sayaç döner | Ateş sesi + fanfar | Güçlü |
| Mükemmel Gün | Tam ekran altın kutlama + sandık | Orkestra fanfarı | Uzun |
| Kilometre taşı | Özel animasyon + paylaşılabilir kart | Epik müzik | Kalp atışı |

**Ses paketleri** açılabilir: Klasik, 8-bit Retro 👾, Orkestra, Doğa, Anime.

### 9.12 👥 Sosyal Motivasyon (Opsiyonel, Hafif)
- **Paylaşım kartı:** Seri ve kilometre taşları Instagram hikayesi formatında güzel bir görsel olarak paylaşılabilir.
- **Hesap verebilirlik ortağı:** Bir arkadaşla karşılıklı seri takibi (ileri faz, sunucu gerektirir).
- **Ortak seri:** İkiniz de tamamlarsanız ortak alev büyür (Duolingo "Friend Streak" benzeri).
- İlk sürümde sosyal özellikler **çevrimdışı paylaşım kartıyla** sınırlı tutulur.

### 9.13 📈 Uyarlanabilir Zorluk
- 7 gün boyunca %95'in üzerinde başarı varsa: "Harika gidiyorsun! Su hedefini 4.5 L'ye çıkaralım mı?"
- 7 gün boyunca %50'nin altında başarı varsa: "Hedefler biraz fazla olabilir. Birkaç tanesini mini versiyona düşürelim mi?" Amaç kullanıcıyı akış bölgesinde tutmak.
- **Alışkanlık mezuniyeti:** 66 gün boyunca %90'ın üzerinde yapılan bir alışkanlık "Otomatik" olarak işaretlenir. Bildirimleri azalır ve kullanıcı **Mezuniyet rozeti** kazanır.

---

## 10. Analiz ve Raporlar

| Rapor | İçerik |
|---|---|
| **Isı haritası** | GitHub tarzı yıllık takvim. Her kare günün tamamlanma oranı kadar koyu. |
| **Haftalık rapor** (Pazar akşamı) | Tamamlanma %, en iyi gün, en çok aksatılan görev, toplam odak süresi, su ortalaması, geçen haftaya göre değişim. Bir tane **"Gelecek hafta için tek öneri"**. |
| **Zamanlama analizi** | "Görevlerini hedef saatten ortalama 18 dk geç yapıyorsun. En çok geciktiğin: sabah sporu." |
| **Ruh hali korelasyonu** | "Spor yaptığın günler %34 daha mutlusun 🙂", "7 saatten az uyuduğun günler odak puanın düşük." |
| **Odak raporu** | Konu bazlı saatler, en verimli saat dilimi, ortalama odak puanı. |
| **Aylık özet ve yıllık "Wrapped"** | Spotify Wrapped tarzı, kaydırılabilir ve paylaşılabilir hikaye kartları: "Bu yıl 1.247 L su içtin, 412 saat odaklandın, en uzun serin 87 gün 🔥". |
| **Kişisel rekorlar** | En uzun seri, en çok odak yapılan gün, en erken kalkılan gün. |

---

## 11. Widget'lar

| Widget | Boyut | İçerik |
|---|---|---|
| **Seri** | 2×2 | Büyük alev + gün sayısı + bugünün halkası. Dokununca uygulama açılır. |
| **Bugün** | 4×2 | Sıradaki 3 iş, her birinde ✅ butonu (uygulamayı açmadan tamamlama) |
| **Su** | 2×2 | Dolan şişe animasyonu + **`+250 ml`** butonu |
| **Odak** | 2×1 | "2s 15dk / 4s" ve ▶️ başlat butonu |
| **Yoldaş** | 2×2 | Yoldaşın anlık hali ve konuşma balonu |
| **Hafta** | 4×1 | 7 günlük nokta dizisi (✅🟡❌) |
| **Kilit ekranı / Always-on** | — | Seri + sıradaki iş (desteklenen cihazlarda) |

- Widget'lar **tema ile uyumlu** olur ve kaplamaları coin ile açılabilir.
- **Anlık güncelleme:** Uygulamadan bir iş tamamlanınca widget hemen güncellenir.
- **Yatma saatinde** widget karanlık ve sakin bir görünüme geçer.

---

## 12. Onboarding (İlk Açılış)

Amaç: 3 dakikada kullanıcının ilk gününü hazır etmek ve **duygusal bağlılık** kurmak.

1. **Karşılama:** "Hayatını düzene sokmaya hazır mısın?" Animasyonlu ve sıcak bir giriş.
2. **Neden buradasın?** (çoklu seçim) Daha düzenli olmak / Ders veya iş verimi / Sağlık / Uyku düzeni / Kötü alışkanlık bırakmak
3. **Günün iskeleti:** Uyanma ve yatma saati. Kaydırmalı ve görsel saat seçiciyle.
4. **Başlangıç paketi:** Seçilen nedenlere göre hazır rutin önerileri sunulur (su, çalışma, spor, okuma…). Kullanıcı açıp kapatarak seçer. **Maksimum 5 ile başlamak önerilir** (fazlası tükenmişlik getirir).
5. **Her alışkanlık için mini versiyon** otomatik önerilir, kullanıcı düzenleyebilir.
6. **Kimlik cümlesi:** "Ben ___ biriyim."
7. **Yoldaş seçimi ve isim verme.**
8. **Koç kişiliği seçimi.**
9. **Söz ve imza:** "Kendime söz veriyorum: …" Parmakla imza atılır.
10. **İzinler:** Bildirim, tam zamanlı alarm, pil optimizasyonu muafiyeti. Her birinin **neden gerekli olduğu** sade bir dille açıklanır.
11. **Widget ekleme rehberi:** Animasyonla gösterilir.
12. **🎁 İlk hediye:** "Başlangıç" rozeti + seri 1. gün + 100 coin (Verilmiş ilerleme etkisi). İlk görev hemen yapılabilecek kadar kolaydır: "Şimdi 1 bardak su iç". İlk başarı ilk dakikada yaşanır.

---

## 13. Tasarım Dili

### 13.1 Genel his
**"Sıcak, oyunsu ama olgun."** Duolingo'nun enerjisi, Headspace'in sakinliği ve Things 3'ün düzeni bir arada.

### 13.2 Renkler
| Rol | Açık tema | Koyu tema (varsayılan) |
|---|---|---|
| Arka plan | `#FAF7F2` (kırık beyaz) | `#12121A` (gece laciverti) |
| Yüzey (kart) | `#FFFFFF` | `#1C1C28` |
| Birincil (alev) | `#FF6B35` | `#FF7A45` |
| Başarı | `#2ECC71` | `#3DDC84` |
| Su | `#3BA7FF` | `#4FB3FF` |
| Odak | `#8B5CF6` | `#A78BFA` |
| Uyarı (gecikme) | `#F5A524` | `#FFB84D` |
| Kritik | `#EF4444` | `#F87171` |
| Metin | `#1F1F29` | `#EDEDF5` |

Her yaşam alanının kendi rengi vardır: Sağlık kırmızı-pembe, Zihin mor, Kariyer mavi, Ev turuncu, Sosyal sarı, Ruh turkuaz.

### 13.3 Tipografi
- **Başlıklar:** Nunito ya da Baloo 2 (yuvarlak, samimi)
- **Gövde:** Inter
- **Sayılar (seri, sayaç):** Kalın ve büyük. Sayaç değişirken dönen bir animasyon oynar.

### 13.4 Şekil ve hareket
- Yuvarlak köşeler (20-28 dp), yumuşak gölgeler, hafif cam efekti.
- Her buton basıldığında hafif küçülüp geri gelir (squish).
- **Lottie animasyonları:** Alev, konfeti, sandık, yoldaş, su dalgası.
- Ekran geçişlerinde paylaşılan eleman animasyonu kullanılır (kart genişleyerek detay ekranına dönüşür).
- **Günün saatine göre arka plan:** Sabah pembe-turuncu gradyan, öğlen açık mavi, akşam mor, gece koyu. Uygulama "yaşıyor" hissi verir.

### 13.5 Temalar (açılabilir)
Varsayılan, Okyanus 🌊, Orman 🌲, Gün Batımı 🌅, Neon Gece 🌃, Minimal ⚪, Sakura 🌸, Retro 👾.

---

## 14. Ekranlar

```
┌────────────────────────────────────────────┐
│  Alt gezinme çubuğu                        │
│  [🏠 Bugün] [📅 Hafta] [⏱️ Odak] [📊 İlerleme] [🐧 Ben] │
└────────────────────────────────────────────┘
```

### 14.1 🏠 Bugün (ana ekran)
```
┌──────────────────────────────────┐
│ Günaydın İlker ☀️      🔥 23  🪙 540 │
│ ┌──────────────────────────────┐ │
│ │   ◯◯◯ Günün halkası %62      │ │
│ │  Zorunlu 5/8 · Su 2.5/4L · Odak 2s/4s │
│ └──────────────────────────────┘ │
│ 🎯 Günlük görevler  ▢▢▣           │
│ ─────── ŞİMDİ ───────            │
│ ⚓ 14:00 Öğle yürüyüşü  [✅]  ⚠️ 20dk gecikti │
│ 💧 Su  ████████░░░  [+250] [+500] │
│ ─────── SONRA ───────            │
│ ⏱️ 15:00 İngilizce (2 blok)  [▶️] │
│ 📖 21:00 Kitap 20 sayfa      [✅] │
│ ⚓ 23:30 Yatma               🌙   │
│ ─────── TAMAMLANDI (5) ▾ ─────── │
│                                  │
│          [🛡️ Plan dışı olay]      │
└──────────────────────────────────┘
```
- **Zaman çizelgesi** görünümü: "Şimdi" çizgisi canlı olarak ilerler.
- Kaydırma hareketleri: sağa kaydır = tamamla, sola kaydır = ertele/mini.
- Sürükleyerek saat değiştirme.
- Sağ altta ➕ butonu: hızlı görev ekleme. Doğal dil desteği: "yarın 15:00 dişçi".

### 14.2 📅 Hafta
- 7 sütunlu takvim (yatay kaydırmalı) ya da liste görünümü.
- Görevler günler ve saatler arasında **sürükle-bırak** ile taşınır.
- Üstte şablon seçici ve "Şablona kaydet" butonu.
- Her günün altında tamamlanma noktası (✅🟡❌).

### 14.3 ⏱️ Odak
- Günlük süre hedefleri, her biri için ilerleme halkası.
- Büyük ▶️ butonu ve ambiyans seçici.
- Odak Ormanı önizlemesi.

### 14.4 📊 İlerleme
- Isı haritası, alışkanlık serileri, grafikler, haftalık rapor, rekorlar, yaşam alanı radarı.

### 14.5 🐧 Ben
- Yoldaş ve Hayat Adası (ana görsel), seviye, rozet vitrini, mağaza, kişisel ödüller, kimlik cümleleri, vizyon panosu, ayarlar.

### 14.6 Diğer ekranlar
Görev ekle/düzenle, Günaydın, Gün sonu özeti, Rüzgar Azaltma, Plan dışı olay sihirbazı, Sandık açma, Kilometre taşı kutlaması, Haftalık planlama (Pazar), Onboarding.

---

## 15. Teknik Mimari

### 15.1 Teknoloji yığını
| Katman | Seçim | Neden |
|---|---|---|
| Dil | **Kotlin** | Android'in modern ve resmi dili |
| Arayüz | **Jetpack Compose + Material 3** | Modern, animasyon dostu, hızlı geliştirme |
| Veritabanı | **Room (SQLite)** | Çevrimdışı, güvenilir, sorgulanabilir |
| Ayarlar | **DataStore** | Tercihler ve basit durum |
| Zamanlama | **AlarmManager** (`setExactAndAllowWhileIdle`) + **WorkManager** | Tam zamanlı bildirimler ve periyodik işler (gün sonu, rapor) |
| Bildirim | NotificationCompat + kanallar + eylem butonları + BroadcastReceiver | Uygulamayı açmadan tamamlama |
| Odak sayacı | **Foreground Service** | Ekran kapalıyken de doğru çalışması için |
| Widget | **Jetpack Glance** | Compose tarzı widget yazımı |
| Ses | **SoundPool** (kısa efektler) + **MediaPlayer/ExoPlayer** (ambiyans) | Düşük gecikmeli başarı sesleri |
| Animasyon | **Lottie Compose** | Zengin ve hafif animasyonlar |
| DI | **Hilt** | Temiz mimari |
| Grafik | Compose Canvas (özel çizim) | Halkalar, ısı haritası, radar |
| Yedekleme | JSON dışa ve içe aktarma, Android Auto Backup | Veri kaybı olmasın |

**Minimum Android:** API 26 (Android 8.0). **Hedef:** en güncel API.

### 15.2 Mimari desen
```
UI (Compose Screens + ViewModels)
        │  StateFlow
Domain (UseCases: CompleteTask, ReplanDay, ComputeNotificationSchedule, AwardXp…)
        │
Data (Room DAO'ları, Repository'ler, DataStore)
        │
Sistem (AlarmScheduler, NotificationHelper, FocusService, WidgetUpdater, SoundPlayer)
```
- **Tek yönlü veri akışı (MVVM + UDF).**
- **Bildirim Motoru** saf bir Kotlin sınıfıdır: `(görevler, şimdi, ayarlar, geçmiş) → planlanacak bildirimler listesi`. Bu sayede **birim testleri** kolayca yazılır.

### 15.3 Bildirim zamanlama akışı
1. Herhangi bir değişiklikte (görev eklendi/tamamlandı, saat değişti, plan dışı olay) `NotificationPlanner` çalışır.
2. **Sadece bir sonraki bildirim** için alarm kurulur (onlarca alarm kurulmaz).
3. Alarm tetiklenince durum yeniden hesaplanır, bildirim gösterilir ve bir sonraki alarm kurulur (**zincirleme zamanlama**). Bu sayede sıklık her zaman güncel duruma göre dinamik kalır.
4. `BOOT_COMPLETED`, `TIME_SET` ve `TIMEZONE_CHANGED` durumlarında alarmlar yeniden kurulur.
5. Pil optimizasyonu muafiyeti istenir. Xiaomi/Huawei/Samsung gibi agresif pil yöneticileri için **üreticiye özel rehber ekranı** gösterilir (dontkillmyapp.com bilgileri).

### 15.4 Gerekli izinler
- `POST_NOTIFICATIONS` (Android 13+)
- `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`
- `RECEIVE_BOOT_COMPLETED`
- `FOREGROUND_SERVICE` (+ `FOREGROUND_SERVICE_SPECIAL_USE` / uygun tür)
- `VIBRATE`
- `USE_FULL_SCREEN_INTENT` (Kritik seviye tam ekran hatırlatma)
- `ACCESS_NOTIFICATION_POLICY` (opsiyonel, DND için)
- `PACKAGE_USAGE_STATS` (opsiyonel, uygulama kalkanı için)
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`

### 15.5 Gizlilik
- **Tüm veri cihazda** kalır. Hesap ve internet gerekmez.
- Analitik ya da reklam yoktur.
- İleride sosyal özellikler eklenirse opsiyonel ve açık rızalı olur.

---

## 16. Veri Modeli

```kotlin
Habit            // Tekrarlanan alışkanlık/görev tanımı
  id, title, icon, color, lifeArea, type (ANCHOR/REQUIRED/COUNTER/DURATION/FLEXIBLE/ONE_TIME)
  targetTime, earlyWindowMin, graceMin, deadlineTime
  fullTarget, miniTarget, unit          // 4000/2000 ml, 240/60 dk
  recurrence (RRULE benzeri), priority, affectsStreak
  triggerHabitId?, ifThenPlan?, whyText?, archived, createdAt

TaskInstance     // Belirli bir gündeki somut görev
  id, habitId?, date, scheduledStart, deadline
  status (PENDING/DONE/MINI/SNOOZED/MISSED/SHIELDED)
  progressValue, completedAt, snoozeCount, movedFrom?

ProgressLog      // Sayaç artışları (+250 ml…)
  id, taskInstanceId, amount, timestamp, source (APP/NOTIF/WIDGET)

FocusSession
  id, taskInstanceId?, subject, plannedMin, actualMin, startedAt, endedAt
  completed, focusScore, intention, note

WeekTemplate / TemplateItem
  id, name, emoji, isActive / templateId, habitId, dayOfWeek, time

DayRecord        // Gün özeti
  date, completionRate, streakState (FULL/MINI/FROZEN/BROKEN), mood, sleepTime, wakeTime
  xpEarned, perfectDay, disruption?

Disruption       // Plan dışı olay
  id, date, type, start, end, replanSnapshot

Streak           // ana + alışkanlık serileri
  id, habitId? (null = ana), current, longest, lastDate, freezesAvailable

Profile          // XP, seviye, coin, koç kişiliği, yoldaş, tema, kimlik cümleleri, imza
Achievement      // rozet tanımı + kazanılma tarihi + seviye
Quest            // günlük/haftalık görevler
Inventory        // kozmetikler, ada yapıları, sandıklar
PersonalReward   // kullanıcının gerçek ödülleri (ad, coin bedeli, alındı mı)
NotificationLog  // gönderildi/tepki verildi (öğrenen zamanlama için)
```

---

## 17. Yol Haritası

### 🚀 Faz 1 — MVP: "Çalışan Çekirdek" (öncelik: hemen)
- [ ] Proje iskeleti (Compose, Room, Hilt, tema, gezinme)
- [ ] Görev türleri: Çapa, Zorunlu, Sayaçlı, Esnek (tam ve mini versiyonlarla)
- [ ] Bugün ekranı (zaman çizelgesi, tamamlama, sayaç butonları)
- [ ] Hafta ekranı (listeleme, günler arası taşıma)
- [ ] **Akıllı Bildirim Motoru v1:** kademeli yükselme, su eğrisi, bildirim eylem butonları, sessiz saatler
- [ ] Başarı sesleri ve konfeti
- [ ] Ana seri + seri dondurma
- [ ] Uyanma ve yatma saati, gün sınırı
- [ ] Seri widget'ı + Su widget'ı
- [ ] Temel onboarding
- [ ] İmzalı APK

### ⏱️ Faz 2 — "Odak ve Esneklik"
- [ ] Odak Modu (pomodoro, foreground service, ambiyans sesleri)
- [ ] Süreli hedefler ve otomatik blok yerleştirme
- [ ] Plan Dışı Olay / Rutin Kalkanı ve otomatik yeniden planlama
- [ ] Şablon hafta (çoklu şablon, pazar planlama ekranı)
- [ ] Rüzgar Azaltma ve gün sonu özeti
- [ ] Bugün ve Odak widget'ları

### 🎮 Faz 3 — "Motivasyon Motoru"
- [ ] XP, seviye, unvanlar, kombolar
- [ ] Rozet sistemi (ilk 40 rozet, gizli rozetler dahil)
- [ ] Günlük 3 görev + haftalık meydan okuma
- [ ] Coin + mağaza + kişisel ödüller
- [ ] Sandıklar (değişken ödül)
- [ ] Alev evrimi ve kilometre taşı kutlamaları
- [ ] Koç kişilikleri ve mesaj havuzları

### 🐧 Faz 4 — "Duygusal Bağ"
- [ ] Yoldaş (animasyonlu, ruh hali, kıyafetler)
- [ ] Hayat Adası ve Odak Ormanı
- [ ] Yaşam alanları + radar
- [ ] Kimlik cümleleri, neden kartları, vizyon panosu, gelecek mektubu
- [ ] Temalar ve ses paketleri

### 📊 Faz 5 — "Zeka ve Analiz"
- [ ] Isı haritası, haftalık rapor, ruh hali korelasyonları
- [ ] Öğrenen bildirim zamanlaması + rutin saati önerileri
- [ ] Uyarlanabilir zorluk + alışkanlık mezuniyeti
- [ ] Aylık/yıllık "Wrapped"
- [ ] Paylaşım kartları
- [ ] Uygulama kalkanı (dikkat dağıtıcı uygulama uyarısı)

### 🌐 Faz 6 — "İleri" (opsiyonel)
- [ ] Bulut yedekleme / cihazlar arası senkron
- [ ] Arkadaşla ortak seri
- [ ] Wear OS saat desteği (bilekten su ekleme)
- [ ] Health Connect entegrasyonu (adım ve uyku otomatik)
- [ ] Doğal dil ile görev ekleme

---

## 18. Etik Sınırlar ve Tükenmişlik Koruması

Psikolojik teknikler **kullanıcının kendi hedefleri için** kullanılır, ona karşı değil:

- ❌ **Utandırma yok:** Başarısızlık mesajları asla aşağılayıcı değildir.
- ❌ **Gerçek parayla seri satışı yok:** Tüm ekonomi uygulama içi ve ücretsizdir.
- ✅ **Bildirim bütçesi:** Saatte en fazla 4 bildirim (kritik hariç). Uyku saatleri kutsaldır.
- ✅ **Aşırı yüklenme uyarısı:** Bir güne fiziksel olarak sığmayacak kadar görev eklenirse uyarı çıkar: "Bugüne 14 saatlik iş eklendi, bu gerçekçi değil. Bazılarını taşıyalım mı?"
- ✅ **Dinlenme bir görevdir:** Haftada 1 "Dinlenme günü" planlanabilir. Bu gün seriyi bozmaz (sadece çapa rutinler çalışır).
- ✅ **Ruh hali düşüşü algılama:** Birkaç gün üst üste 😞 seçilirse hedefler otomatik yumuşatılır ve destekleyici bir mesaj gösterilir.
- ✅ **Mezuniyet:** Amaç kullanıcıyı uygulamaya bağımlı yapmak değil, alışkanlıkları **otomatik** hâle getirmektir.

---

## 19. Açık Sorular

Geliştirmeye başlamadan önce netleşmesi gerekenler:

1. **Uygulamanın adı ne olsun?** (Öneriler: *Rutin*, *Alev*, *Ritim*, *Düzen*, *Kıvılcım*)
2. **Yoldaş hangi hayvan/karakter olsun?** (Penguen, tilki, kedi, ejderha, bitki…)
3. **Varsayılan koç kişiliği** hangisi olsun?
4. **İlk alışkanlıkların neler?** (Su 4 L, günde 4 saat çalışma… başka?) MVP'yi senin gerçek rutininle test edelim.
5. **Uyanma ve yatma saatin** yaklaşık kaç?
6. **Telefonunun markası ne?** (Xiaomi/Huawei ise bildirimlerin kesin gelmesi için ek ayarlar gerekir)

---

> **Sonraki adım:** Faz 1 (MVP) ile başlayıp telefonuna kurulabilir ilk APK'yı üretmek. Ardından her faz, gerçek kullanımından gelen geri bildirimle şekillenecek.
