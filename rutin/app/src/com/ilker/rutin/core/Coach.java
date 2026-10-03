package com.ilker.rutin.core;

import java.util.Random;

/**
 * Koç kişilikleri ve mesaj havuzları. Aynı mesaj üst üste gelmesin diye rastgele seçilir.
 * Yer tutucular: {t} başlık, {e} emoji, {s} seri, {r} kalan, {m} kalan dakika, {n} yoldaş adı, {u} kullanıcı adı, {k} sayı
 */
public final class Coach {
    private Coach() {}

    public static final String[] NAMES = {"Nazik Arkadaş", "Komutan", "Esprili", "Sakin Bilge"};
    public static final String[] EMOJI = {"🤗", "🎖️", "😏", "🧘"};
    public static final String[] SAMPLE = {
            "Su molası! 💧 Vücudun sana teşekkür edecek.",
            "ASKER! Su içme vakti. Hemen. 💧",
            "Böbreklerin aradı, 'bir bardak su yollar mısın' dedi 💧",
            "Bir yudum su, bir nefes. Acele yok, sadece şimdi. 💧"};

    public static final int PREP = 0, ON = 1, NUDGE = 2, PUSH = 3, CRIT = 4, W_START = 5, W_BEHIND = 6, W_FAR = 7, W_LAST = 8,
            D_ON = 9, D_PRESS = 10, D_HIGH = 11, MORNING = 12, RISK = 13, WIND = 14, SUMMARY = 15, PLAN = 16, GROUP = 17, CHEER = 18,
            BLOCK = 19, W_HALF = 20;

    // [kategori][kişilik][varyant]
    static final String[][][] M = new String[21][][];

    static {
        M[PREP] = new String[][]{
                {"10 dk sonra: {e} {t}. Hazırlanmaya başla 🙂", "Birazdan {e} {t} zamanı. Ortamı hazırla!"},
                {"10 dakika sonra {e} {t}. Hazır ol!", "Geri sayım: 10 dk. {e} {t}."},
                {"Spoiler: 10 dk sonra {e} {t} var 👀", "10 dk sonra {e} {t}. Şimdiden ısınıyoruz 🔥"},
                {"10 dakika sonra {e} {t}. Zihnini yavaşça hazırla.", "Birazdan {e} {t}. Bir nefes al, hazırlan."}};
        M[ON] = new String[][]{
                {"{e} {t} zamanı! Hadi birlikte yapalım 💪", "{e} {t} için harika bir an. Başlayalım mı?", "{n}: {e} {t} zamanı geldi, sana güveniyorum!"},
                {"{e} {t} ZAMANI. Başla!", "Görev: {e} {t}. Saat geldi, erteleme yok.", "{e} {t}. Şimdi. Tartışma yok."},
                {"{e} {t} seni bekliyor, randevuya geç kalma 😎", "Takvim diyor ki: {e} {t}. Takvimle tartışılmaz.", "{e} {t} zamanı! Bahane stoklarımız tükendi 😅"},
                {"{e} {t} için an geldi. Sadece başla, gerisi akar.", "Şimdi {e} {t}. Tek bir adım yeter.", "{e} {t}: Yolun kendisi amaçtır."}};
        M[NUDGE] = new String[][]{
                {"Hâlâ vaktin var 🙂 {e} {t} seni bekliyor.", "{e} {t} için küçük bir hatırlatma. Sadece 2 dakika ile başla?", "{n} {e} {t} için seni bekliyor 🐾"},
                {"{e} {t} hâlâ yapılmadı. Toparlan!", "Hatırlatma: {e} {t}. Harekete geç.", "{e} {t} bekliyor. Disiplin şimdi başlar."},
                {"{e} {t} biraz alındı, 'unuttun mu beni' diyor 🥲", "{e} {t} hâlâ orada. Kendi kendine yapılmıyor maalesef.", "Beyin: 'sonra'. Ben: '{e} {t} şimdi' 😏"},
                {"{e} {t} hâlâ bekliyor. Kendini yargılamadan, sadece başla.", "Ertelemek doğaldır; başlamak bir seçimdir. {e} {t}.", "Küçük bir adım: {e} {t}."}};
        M[PUSH] = new String[][]{
                {"🔥 {s} günlük serin {e} {t} işine bağlı. Yapabilirsin!", "{e} {t} için zaman daralıyor ⏳ Mini versiyon bile olur!", "Hadi! {e} {t} bitince çok iyi hissedeceksin."},
                {"🔥 {s} GÜNLÜK SERİ TEHLİKEDE. {e} {t}. Hemen!", "Zaman daralıyor. {e} {t}. Şimdi harekete geç!", "{e} {t}. Bu bir rica değil."},
                {"{e} {t} ile aranızdaki mesafe açılıyor. Bir mesaj at ona 😅", "Serin ({s} gün 🔥) sana bakıyor. Gözleri dolu.", "Zaman daralıyor, {e} {t} hâlâ bekar 💔"},
                {"Zaman daralıyor ama hâlâ yeterli. {e} {t}.", "{s} günlük emek, küçük bir adıma bakıyor. {e} {t}.", "Şimdi yapılan küçük iş, sonra yapılacak büyük işten iyidir. {e} {t}."}};
        M[CRIT] = new String[][]{
                {"Son {m} dk! {e} {t} — mini versiyon bile seriyi kurtarır 🙏", "Son çağrı ⏰ {e} {t}. Birlikte bitirelim!", "{n} parmaklarını çaprazladı: {e} {t}, son {m} dk!"},
                {"SON {m} DAKİKA! {e} {t}. HEMEN!", "KRİTİK: {e} {t}. {m} dk kaldı. Kalk!", "Son şans. {e} {t}. {m} dk."},
                {"Son {m} dk! {e} {t} yapılmazsa serin trip atacak 🔥😤", "Alarm alarm! {e} {t} için {m} dk kaldı 🚨", "Kahraman anı: {m} dk içinde {e} {t}. Kamera sende 🎬"},
                {"{m} dakika kaldı. Mükemmel olması gerekmiyor; mini versiyon yeter. {e} {t}.", "Son {m} dk. Nefes al ve sadece başla. {e} {t}.", "Şimdi. Küçük de olsa. {e} {t}."}};
        M[W_START] = new String[][]{
                {"Güne bir bardak suyla başla 💧 Vücudun uykudan susuz uyandı.", "Günaydın! İlk bardak su senden 💧"},
                {"Güne su ile başla. 1 bardak. Şimdi.", "Sabah protokolü: 1 bardak su."},
                {"Günaydın! Hücrelerin 'su!' diye pankart açtı 💧", "İlk su bardağı: günün en kolay zaferi 💧"},
                {"Güne suyla başla. Beden bir nehir gibidir, akmalı. 💧", "Bir bardak su, güne nazik bir başlangıç."}};
        M[W_HALF] = new String[][]{
                {"Yarıyı geçtin! 💪 {r} kaldı.", "Harika gidiyorsun, yarı yol tamam 💧"},
                {"Yarı yol tamam. Devam!", "%50 tamam. Kalan: {r}."},
                {"Yarıyı geçtin, artık yarı insan yarı su samurusun 🦦", "Yarı yol! Bardak yarı dolu 😄"},
                {"Yolun yarısı. Aynı sakinlikle devam.", "Yarıyı geçtin. Acele etme, bırakma."}};
        M[W_BEHIND] = new String[][]{
                {"Su molası! 💧 {r} kaldı, bir bardak iç?", "{n} susadı, sen de susamışsındır 💧 Bir bardak?", "Küçük bir su molası vakti 💧"},
                {"Su hedefinin gerisindesin. 1 bardak. Şimdi.", "Su! {r} kaldı. İç!", "Hidrasyon raporu: geride. Düzelt."},
                {"Böbreklerin aradı, 'bir bardak su yollar mısın' dedi 💧", "Bitkilerini suluyorsun, kendini de sula 🌱💧", "Su içmek bedava, üstelik kalori yok 😏💧"},
                {"Bir yudum su, bir nefes. 💧", "Su gibi ol: yumuşak ama durmayan. Bir bardak?", "Bedenin sessizce su istiyor. 💧"}};
        M[W_FAR] = new String[][]{
                {"Epey geride kaldık 💧 {r} kaldı. Şimdi 2 bardak iç, yakalarsın!", "Su hedefi için tempoyu artıralım! {r} kaldı 💧"},
                {"SU AÇIĞI BÜYÜK: {r}. 2 bardak, hemen!", "Geride kaldın. {r}. Toparlan!"},
                {"Su hedefin seni ghostluyor 👻 {r} kaldı!", "Çöl modundasın 🏜️ {r} kaldı, vaha lazım!"},
                {"Açığı telaşsız kapat: şimdi 2 bardak. {r} kaldı.", "Geride olmak son olmak değildir. {r} kaldı."}};
        M[W_LAST] = new String[][]{
                {"Son düzlük! 🏁 {r} kaldı, bitirelim mi?", "Neredeyse bitti! Son {r} 💧"},
                {"Son {r}. Bitir!", "Hedefe {r} kaldı. Tamamla!"},
                {"Son {r}! Konfeti hazırda bekliyor 🎉", "{r} kaldı, finale hazır mısın? 🏁"},
                {"Son adımlar. {r} kaldı.", "Bitiş çizgisi göründü. {r}."}};
        M[D_ON] = new String[][]{
                {"⏱️ {t} zamanı! Bir odak bloğu başlatalım mı?", "{t} için 25 dk'lık bir blok sığar. Başlayalım mı? ⏱️"},
                {"{t} bloğu başlıyor. Odak modu: AÇIK.", "Çalışma saati: {t}. Başla."},
                {"{t} için 25 dk. Telefon da sen de odak moduna 📵", "Pomodoro domatesi hazır 🍅 {t} başlasın mı?"},
                {"{t} için sakin bir odak zamanı. Sadece 25 dakika.", "Zihnini topla: {t} bloğu."}};
        M[D_PRESS] = new String[][]{
                {"{t}: bugün {r} kaldı. Bir blok daha? ⏱️", "{t} hedefin için zaman azalıyor, bir blok başlatalım mı?"},
                {"{t}: {r} açık var. Blok başlat!", "Zaman daralıyor. {t}: {r} kaldı."},
                {"{t} hedefi seni arıyor, açmıyorsun 📞 {r} kaldı.", "{r} daha {t}. Kahveni al, başla ☕"},
                {"{t}: {r} kaldı. Bir blok, sadece bir blok.", "Küçük bloklar büyük işler yapar. {t}."}};
        M[D_HIGH] = new String[][]{
                {"{t}: tam hedef bugün zor olabilir. Mini hedefe odaklanalım mı? 🟡", "{t} için son fırsatlar! Mini hedef bile seriyi korur."},
                {"{t}: kritik. En azından mini hedefi bitir!", "Son fırsat: {t}. Mini hedef, şimdi!"},
                {"{t}: plan B devrede 🟡 Mini hedef de hedeftir!", "Tam hedef uzak ama mini hedef el sallıyor 👋"},
                {"Bugün tamamı olmayabilir; mini hedef yeter. {t}.", "Az da olsa yap. {t}."}};
        M[BLOCK] = new String[][]{
                {"⏱️ Planlı {t} bloğun başlıyor! Hazır mısın?", "Şimdi {t} bloğu. Başlat'a bas, gerisi kolay."},
                {"Planlı blok: {t}. Başla.", "{t} bloğu. ŞİMDİ."},
                {"{t} bloğu kapıda, zili çalıyor 🔔", "{t} zamanı! Dikkat dağıtıcıları sessize al 📵"},
                {"Planladığın {t} bloğu. Kendine verdiğin sözü tut.", "Şimdi {t}. Sadece bu an."}};
        M[MORNING] = new String[][]{
                {"Günaydın {u}! ☀️ Bugün {k} işin var. Güne başlayalım mı?", "Günaydın {u}! {n} seni bekliyordu ☀️"},
                {"Kalk {u}! Bugün {k} görev var. Güne başla!", "Günaydın asker. {k} görev. Başla."},
                {"Günaydın {u}! Yorgan savaşı kazandın, şimdi sıra {k} görevde 😄", "Günaydın! Kahve mi önce, zafer mi? ☀️"},
                {"Günaydın {u}. Yeni bir gün, yeni bir sayfa. ☀️", "Günaydın. Bugün {k} küçük adım seni bekliyor."}};
        M[RISK] = new String[][]{
                {"🔥 {s} günlük serin tehlikede! {k} iş kaldı, birlikte kurtaralım.", "Serin seni bekliyor 🔥 {k} iş kaldı. Mini versiyonlar bile yeter!"},
                {"🔥 {s} GÜNLÜK SERİ TEHLİKEDE. {k} iş. Hemen!", "Seri alarmı! {k} görev açık."},
                {"Serin ({s} 🔥) şu an çok gergin. {k} iş kaldı 😬", "{s} günlük seri: 'beni bırakma' 🥺 {k} iş kaldı."},
                {"{s} günün emeği bugün bir adım istiyor. {k} iş kaldı.", "Seri tehlikede ama vakit var. {k} iş."}};
        M[WIND] = new String[][]{
                {"🌙 Yatmaya 1 saat var. Ekranları yavaş yavaş bırakma vakti.", "🌙 Rüzgârı azaltma vakti. Yarın için enerjini koru."},
                {"Yatmaya 60 dk. Ekranı bırak, hazırlan.", "Gece protokolü başlıyor. 1 saat."},
                {"🌙 1 saat sonra uyku. TikTok 'bir video daha' diyecek, inanma 😏", "Uyku modu yükleniyor... %0 🌙"},
                {"🌙 Gün yavaşlıyor. Sen de yavaşla.", "Işıkları kıs, zihnini dinlendir. 🌙"}};
        M[SUMMARY] = new String[][]{
                {"✨ Gün sonu özetin hazır! Bugün nasıldı?", "Günü güzel bir kapanışla bitirelim ✨"},
                {"Gün raporu hazır. İncele ve yat.", "Günün değerlendirmesi. 1 dakika."},
                {"Günün bölüm finali hazır 🎬 İzlemeye gel!", "Bugünün özeti: spoiler, iyi şeyler var ✨"},
                {"Günü nazikçe kapat. Özetin hazır ✨", "Bir dakika dur ve bugüne bak."}};
        M[PLAN] = new String[][]{
                {"🗺️ Gelecek haftayı 5 dakikada planlayalım mı?", "Pazar planlaması: yeni hafta, yeni sayfa 🗺️"},
                {"Haftalık planlama zamanı. 5 dk.", "Yeni hafta için emir: planla."},
                {"Gelecek haftanın senaryosunu yazma vakti 🗺️", "Pazartesi sürpriz olmasın, planlayalım 🗺️"},
                {"Yeni haftaya niyetle gir. 5 dakikalık plan. 🗺️", "Planlamak, geleceğe nazikçe hazırlanmaktır."}};
        M[GROUP] = new String[][]{
                {"{k} iş seni bekliyor 👀", "{k} küçük iş var, hadi birer birer!"},
                {"{k} görev açık. Harekete geç!", "{k} iş bekliyor. Başla."},
                {"{k} iş sırada bekliyor, sıra numarası sende 🎫", "{k} görev seninle tanışmak istiyor 👋"},
                {"{k} iş var. Birer birer, sakince.", "{k} küçük adım."}};
        M[CHEER] = new String[][]{
                {"Harika! 🎉", "Süpersin! 💪", "Bir adım daha! ✨"},
                {"Tamamlandı. Aferin asker!", "Görev başarılı! 🎖️", "İşte bu!"},
                {"Efsane! 😎", "Alkışlar senin için 👏", "Bu kadar kolay mıydı? 😏"},
                {"Güzel. Bu anı hisset.", "Bir adım daha. 🌿", "Tamam. Nefes al."}};
    }

    static final Random R = new Random();

    public static String msg(int cat, int coach, State s, Task t, String rem, int minutesLeft, int count) {
        String[] pool = M[cat][Math.max(0, Math.min(3, coach))];
        String x = pool[R.nextInt(pool.length)];
        State.Profile p = s.p;
        return x.replace("{t}", t != null ? t.title : "")
                .replace("{e}", t != null ? t.emoji : "")
                .replace("{s}", String.valueOf(Math.max(1, p.streak)))
                .replace("{r}", rem == null ? "" : rem)
                .replace("{m}", String.valueOf(Math.max(1, minutesLeft)))
                .replace("{n}", p.companionName == null || p.companionName.isEmpty() ? "Yoldaşın" : p.companionName)
                .replace("{u}", p.name == null || p.name.isEmpty() ? "" : p.name)
                .replace("{k}", String.valueOf(count));
    }

    // ---------------- Yoldaş konuşmaları ----------------
    public static String companionLine(Engine e, long now) {
        State s = e.s;
        int td = e.today(now);
        DayRec r = s.dayOrNull(td);
        int nd = e.nowDm(now);
        int sl = e.sleepDm();
        String n = s.p.name == null || s.p.name.isEmpty() ? "" : s.p.name;
        if (e.p.comebackDay == td) return "Seni çok özledim! 🥹 Bugün kolay bir gün: sadece çapa rutinler yeter.";
        if (r != null && r.perfect) return "Mükemmel bir gün! Seninle gurur duyuyorum ✨";
        if (r != null && r.met) {
            String[] a = {"Serimiz güvende! 🔥 Gerisi bonus.", "Bugünün gereksinimleri tamam! Şimdi keyfini çıkar 😊", "Yaptın! Kalanlar tam hedef için 💪"};
            return a[(int) (now / 30 % a.length)];
        }
        if (nd < e.wakeDm() + 120) return "Günaydın" + (n.isEmpty() ? "" : " " + n) + "! Bugün harika bir gün olacak ☀️";
        if (nd > sl - 90) return "Gün bitmek üzere. Kalanları mini versiyonla kurtarabiliriz! 🌙";
        int open = 0;
        for (Task t : s.tasksOf(td)) if (t.isOpen() && t.streak) open++;
        if (open == 0) return "Her şey yolunda görünüyor 🌿";
        String[] a = {"Birlikte " + open + " işi bitirelim! 💪", "Bir iş bitince bir sonrakine geçmek daha kolay. Kombo yapalım mı? 🎯", "Küçük adımlar, büyük değişim 🐾", "Sana inanıyorum! Bir sonraki iş seni bekliyor."};
        return a[(int) (now / 20 % a.length)];
    }

    public static final String[] QUOTES = {
            "Damlaya damlaya göl olur.",
            "Bugün yaptığın küçük şey, yarının büyük farkıdır.",
            "Motivasyon başlatır, alışkanlık sürdürür.",
            "Mükemmel olmak zorunda değilsin, sadece devam et.",
            "Her tamamladığın iş, olmak istediğin kişiye verilmiş bir oydur.",
            "Asla iki kez kaçırma.",
            "Başlamak işin yarısıdır.",
            "Sabır acıdır, meyvesi tatlıdır.",
            "Yavaş ilerlemek, hiç ilerlememekten iyidir.",
            "Disiplin, istediğin şeyle şimdi istediğin şey arasında seçim yapmaktır.",
            "Rutin sıkıcı değildir; özgürlüğün temelidir.",
            "Kötü bir gün, kötü bir hayat değildir.",
            "Bir işi en iyi bitirmenin yolu, onu başlatmaktır.",
            "Küçük adımlar da seni hedefe götürür.",
            "Kendinle yarış, başkalarıyla değil.",
            "Bugün, bir yıl önce dilediğin gündür.",
            "Emek olmadan yemek olmaz.",
            "Ağaç yaşken eğilir; alışkanlık erken kazanılır.",
            "Zaman, en adil sermayedir: herkese günde 24 saat.",
            "Planı olmayan, başkasının planının parçası olur.",
            "Yorulduğunda dinlen, ama bırakma.",
            "Hedef büyük, adım küçük olsun.",
            "Bir günü kazan. Sonra bir gün daha.",
            "İstikrar, yetenekten daha güçlüdür.",
            "Sen bir süreçsin, bitmiş bir ürün değil.",
    };

    public static String quote(int day) { return QUOTES[Math.floorMod(day * 7 + 3, QUOTES.length)]; }
}
