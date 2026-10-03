package com.ilker.rutin.core;

import java.util.ArrayList;
import java.util.Random;

/** Ek motivasyon sistemleri: günlük XP hedefi, şans çarkı, alışkanlık seviyeleri, koleksiyon kartları, kendinle yarış. */
public final class Rewards {
    private Rewards() {}

    // ---------------- Günlük XP hedefi ----------------
    public static final int[] GOALS = {30, 50, 100, 150};
    public static final String[] GOAL_NAMES = {"🌿 Rahat", "🙂 Normal", "💪 Ciddi", "🔥 Yoğun"};

    // ---------------- Alışkanlık seviyeleri ----------------
    public static final int[] HABIT_LV = {0, 5, 15, 30, 60, 100, 200, 365};
    public static final String[] HABIT_LV_NAME = {"Tohum", "Filiz", "Fidan", "Genç Ağaç", "Ağaç", "Ulu Ağaç", "Orman", "Efsane"};
    public static final String[] HABIT_LV_EMOJI = {"🌰", "🌱", "🌿", "🪴", "🌳", "🌲", "🏞️", "👑"};

    public static int habitLevel(int totalDone) {
        int l = 0;
        for (int i = 0; i < HABIT_LV.length; i++) if (totalDone >= HABIT_LV[i]) l = i;
        return l;
    }

    public static String habitLevelName(int totalDone) {
        int l = habitLevel(totalDone);
        return HABIT_LV_EMOJI[l] + " " + HABIT_LV_NAME[l];
    }

    /** Sonraki seviyeye kalan tekrar (son seviyede 0). */
    public static int habitToNext(int totalDone) {
        int l = habitLevel(totalDone);
        return l >= HABIT_LV.length - 1 ? 0 : HABIT_LV[l + 1] - totalDone;
    }

    // ---------------- Koleksiyon kartları ----------------
    public static final class Card {
        public final String emoji, title, text; public final int rarity;
        Card(String e, String t, String x, int r) { emoji = e; title = t; text = x; rarity = r; }
    }

    public static final Card[] CARDS = {
            new Card("🌱", "İlk Adım", "Her büyük yolculuk tek bir adımla başlar.", 0),
            new Card("💧", "Damla", "Damlaya damlaya göl olur.", 0),
            new Card("⏰", "Dakiklik", "Zamanında yapılan küçük iş, geç kalmış büyük işten iyidir.", 0),
            new Card("🔗", "Zincir", "Alışkanlıklar zincirdir: birini diğerine bağla.", 0),
            new Card("🧱", "Tuğla", "Her gün bir tuğla koy; bir gün duvar olur.", 0),
            new Card("🌅", "Taze Başlangıç", "Her sabah yeni bir sayfadır.", 0),
            new Card("🐢", "Kaplumbağa", "Yavaş ama durmadan giden kazanır.", 0),
            new Card("🧭", "Pusula", "Nereye gittiğini bilen yolunu bulur.", 0),
            new Card("🎯", "Odak", "Aynı anda tek bir şey. Bu anda.", 0),
            new Card("🛏️", "Düzen", "Güne yatağını toplayarak başla; ilk zafer senin.", 0),
            new Card("📖", "Sayfa", "Günde 10 sayfa, yılda 15 kitap eder.", 0),
            new Card("🚶", "Yürüyüş", "Hareket eden beden, berrak zihin.", 0),
            new Card("🔥", "Kıvılcım", "Motivasyon kıvılcımdır; alışkanlık odundur.", 1),
            new Card("🪞", "Kimlik", "Her tekrar, olmak istediğin kişiye bir oydur.", 1),
            new Card("⏳", "2 Dakika", "Başlamak için sadece 2 dakika yeter.", 1),
            new Card("🧊", "Dondurma", "Kötü gün olur; asıl kural iki kez kaçırmamak.", 1),
            new Card("🌉", "Köprü", "Eğer-ise planı, niyet ile eylem arasındaki köprüdür.", 1),
            new Card("🧗", "Tırmanış", "Zor olan her gün biraz daha kolaylaşır.", 1),
            new Card("🎻", "Ritim", "Rutin, hayatın müziğidir.", 1),
            new Card("🌊", "Dalga", "Dalgayı durduramazsın ama sörf yapmayı öğrenebilirsin.", 1),
            new Card("🦉", "Gece Kuşu", "Gün uyku saatinde biter, pes ettiğinde değil.", 1),
            new Card("🧘", "Sükûnet", "Sakin zihin, isabetli karar.", 1),
            new Card("🏔️", "Zirve", "Zirveye bakma, bir sonraki adıma bak.", 2),
            new Card("🦅", "Anka", "Düşmek son değil; küllerinden doğ.", 2),
            new Card("💎", "Elmas", "Baskı altında kalan kömür elmasa dönüşür.", 2),
            new Card("🧠", "Plastisite", "Beyin tekrarla şekillenir; bugün onu eğitiyorsun.", 2),
            new Card("🏹", "Hedef", "Hedef büyük, adım küçük olsun.", 2),
            new Card("🗝️", "Anahtar", "Disiplin, özgürlüğün anahtarıdır.", 2),
            new Card("🌳", "Kök", "Kökleri derin olan ağaç fırtınadan korkmaz.", 2),
            new Card("🪐", "Yörünge", "Rutin seni kendi yörüngende tutar.", 2),
            new Card("👑", "Hükümdar", "Kendine hükmeden, dünyaya hükmeder.", 3),
            new Card("☀️", "Güneş", "365 gün: artık sen bir alışkanlık ustasısın.", 3),
            new Card("🐉", "Ejder", "İçindeki ejderi eğittin: ertelemeyi yendin.", 3),
            new Card("🌌", "Galaksi", "Küçük yıldızlar bir araya gelince galaksi olur.", 3),
            new Card("⚡", "Şimşek", "Harekete geçmek, düşünmekten hızlıdır.", 3),
            new Card("🏆", "Efsane", "Efsaneler bir günde değil, her gün yazılır.", 3),
    };

    public static final String[] CARD_RARITY = {"Sıradan", "Nadir", "Epik", "Efsanevi"};

    /** Rastgele bir kart verir; tekrar gelirse coin'e çevrilir. Dönen metin kullanıcıya gösterilir. */
    public static String grantCard(State s, Random r, int maxRarity) {
        int rar = 0;
        int x = r.nextInt(100);
        if (x >= 60) rar = 1;
        if (x >= 88) rar = 2;
        if (x >= 97) rar = 3;
        rar = Math.min(rar, maxRarity);
        ArrayList<Integer> pool = new ArrayList<>();
        for (int i = 0; i < CARDS.length; i++) if (CARDS[i].rarity == rar) pool.add(i);
        int pick = pool.get(r.nextInt(pool.size()));
        Card c = CARDS[pick];
        if (s.cards.contains(pick)) {
            int coins = 10 + rar * 15;
            s.p.coins += coins;
            return "🃏 " + c.emoji + " " + c.title + " (tekrar → 🪙 " + coins + ")";
        }
        s.cards.add(pick);
        return "🃏 Yeni kart: " + c.emoji + " " + c.title + " (" + CARD_RARITY[c.rarity] + ")";
    }

    // ---------------- Şans çarkı ----------------
    public static final String[] SPIN = {"🪙 20", "🪙 50", "⭐ 30 XP", "🧊 Dondurma", "🎁 Sandık", "⚗️ İksir", "🃏 Kart", "💰 100"};
    public static final int[] SPIN_W = {24, 15, 20, 6, 10, 9, 12, 4};
    public static final int[] SPIN_COLORS = {0xFFFFC53D, 0xFFFF9A3C, 0xFFA78BFA, 0xFF4FB3FF, 0xFFFF6B8A, 0xFF3DDC84, 0xFF3DD9C9, 0xFFFFD84D};

    public static boolean canSpin(Engine e, long now) {
        DayRec r = e.s.dayOrNull(e.today(now));
        return r != null && r.met && !r.spun && e.p.onboarded;
    }

    /** Çarkı çevirir: ödül indeksini döndürür (arayüz animasyonu buna göre durdurur). */
    public static int spin(Engine e, long now, Random rnd, Ev.List ev) {
        int td = e.today(now);
        DayRec r = e.s.day(td);
        if (r.spun) return -1;
        r.spun = true;
        e.p.spins++;
        int tot = 0;
        for (int w : SPIN_W) tot += w;
        int x = rnd.nextInt(tot), idx = 0;
        while (x >= SPIN_W[idx]) { x -= SPIN_W[idx]; idx++; }
        String text;
        switch (idx) {
            case 0: e.p.coins += 20; text = "🪙 20 coin"; break;
            case 1: e.p.coins += 50; text = "🪙 50 coin"; break;
            case 2: e.gainXp(30, "Şans çarkı", td, now, ev); text = "⭐ 30 XP"; break;
            case 3:
                if (e.p.freezes < Game.MAX_FREEZES) { e.p.freezes++; text = "🧊 Seri Dondurma"; }
                else { e.p.coins += 80; text = "🪙 80 coin (dondurma deposu dolu)"; }
                break;
            case 4: e.addChest(1, td, "Şans çarkı", ev); text = "🎁 Nadir sandık"; break;
            case 5: e.p.boostUntil = Math.max(e.p.boostUntil, now) + 60; text = "⚗️ 1 saat 2x XP"; break;
            case 6: text = grantCard(e.s, rnd, 2); break;
            default: e.p.coins += 100; text = "💰 JACKPOT! 100 coin"; break;
        }
        ev.add(Ev.SPIN, idx, text);
        Badges.check(e.s, td, ev);
        return idx;
    }

    // ---------------- Kendinle yarış ----------------
    /** {bu hafta bugüne kadar XP, geçen hafta aynı güne kadar XP} */
    public static int[] ghost(State s, int today) {
        int ws = Tm.weekStart(today);
        int wd = Tm.weekday(today);
        int a = 0, b = 0;
        for (int i = 0; i <= wd; i++) {
            DayRec x = s.dayOrNull(ws + i);
            if (x != null) a += x.xp;
            DayRec y = s.dayOrNull(ws - 7 + i);
            if (y != null) b += y.xp;
        }
        return new int[]{a, b};
    }
}
