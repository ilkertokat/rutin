package com.ilker.rutin.core;

/** Rozet tanımları ve ölçütleri. Kademeli rozetler: Bronz, Gümüş, Altın, Elmas. */
public final class Badges {
    private Badges() {}

    public static final String[] TIER = {"", "Bronz", "Gümüş", "Altın", "Elmas"};
    public static final int[] TIER_COLOR = {0xFF555566, 0xFFCD7F4A, 0xFFC9D1DC, 0xFFFFC53D, 0xFF7FE7FF};

    public static final class B {
        public final String id, emoji, name, desc; public final boolean hidden; public final int[] tiers; public final int metric;
        B(String id, String emoji, String name, String desc, boolean hidden, int metric, int... tiers) {
            this.id = id; this.emoji = emoji; this.name = name; this.desc = desc; this.hidden = hidden; this.metric = metric; this.tiers = tiers;
        }
        public String descFor(int tier) {
            int t = tiers[Math.max(0, Math.min(tiers.length - 1, tier - 1))];
            return desc.replace("{n}", String.valueOf(t));
        }
    }

    static final int M_START = 0, M_BEST = 1, M_WATER_FIRST = 2, M_WATER_DAYS = 3, M_WATER_L = 4, M_FOCUS_SESS = 5,
            M_FOCUS_DAYMAX = 6, M_FOCUS_LONG = 7, M_FOCUS_H = 8, M_EARLY = 9, M_PUNCTUAL = 10, M_COMEBACK = 11,
            M_REPAIR = 12, M_STORM = 13, M_SLEEP = 14, M_LATE = 15, M_WEEKEND = 16, M_COMBO = 17, M_MORNING = 18,
            M_PERFECT = 19, M_QUESTS = 20, M_CHESTS = 21, M_LEVEL = 22, M_GRAD = 23, M_BALANCE = 24, M_PLANS = 25,
            M_REFLECT = 26, M_FLEX = 27, M_USEDAYS = 28, M_TOTAL = 29, M_REWARD = 30, M_STYLE = 31, M_ISLAND = 32,
            M_SILENT = 33, M_MSTART = 34, M_PROMISE = 35, M_SPINS = 36, M_GOALS = 37, M_CARDS = 38, M_HERO = 39, M_HABITLV = 40;

    public static final B[] ALL = {
            new B("start", "🌱", "Başlangıç", "Yolculuğa ilk adımı attın", false, M_START, 1),
            new B("kivilcim", "✨", "Kıvılcım", "3 günlük seri", false, M_BEST, 3),
            new B("ates", "🔥", "Ateş", "7 günlük seri", false, M_BEST, 7),
            new B("yangin", "❤️‍🔥", "Yangın", "30 günlük seri", false, M_BEST, 30),
            new B("volkan", "🌋", "Volkan", "100 günlük seri", false, M_BEST, 100),
            new B("gunes", "☀️", "Güneş", "365 günlük seri", false, M_BEST, 365),
            new B("damla", "💧", "İlk Damla", "İlk suyunu kaydettin", false, M_WATER_FIRST, 1),
            new B("nehir", "🏞️", "Nehir", "{n} gün su hedefini tamamla", false, M_WATER_DAYS, 7, 30, 100, 365),
            new B("okyanus", "🌊", "Okyanus", "Toplam {n} litre su", false, M_WATER_L, 100, 500, 1000, 5000),
            new B("pomodoro", "🍅", "İlk Pomodoro", "İlk odak oturumunu tamamladın", false, M_FOCUS_SESS, 1),
            new B("maraton", "🏃", "Maratoncu", "Tek günde {n} dakika odak", false, M_FOCUS_DAYMAX, 180, 240, 360, 480),
            new B("derin", "🤿", "Derin Dalış", "Tek oturumda {n} dk kesintisiz odak", false, M_FOCUS_LONG, 50, 90),
            new B("odaksaat", "💯", "Odak Saatleri", "Toplam {n} saat odak", false, M_FOCUS_H, 10, 50, 100, 500),
            new B("erkenci", "🐦", "Erkenci Kuş", "{n} görevi erken bitir", false, M_EARLY, 10, 50, 200, 1000),
            new B("dakik", "⏰", "Dakik", "Art arda {n} gün hiç gecikme", false, M_PUNCTUAL, 3, 7, 21, 50),
            new B("donus", "🔄", "Geri Dönüş", "Ara verdikten sonra {n} kez geri döndün", false, M_COMEBACK, 1, 3, 10),
            new B("anka", "🦅", "Anka Kuşu", "Kırılan seriyi {n} kez onardın", false, M_REPAIR, 1, 3, 10),
            new B("firtina", "⛈️", "Fırtınada Bile", "Plan dışı olaya rağmen seriyi {n} kez korudun", false, M_STORM, 1, 5, 20),
            new B("uyku", "😴", "Uyku Ustası", "{n} gün düzenli uyku", false, M_SLEEP, 7, 14, 30, 100),
            new B("geceyarisi", "🦉", "Gece Yarısı Kahramanı", "Seriyi gece yarısından sonra kurtardın", true, M_LATE, 1),
            new B("haftasonu", "⚔️", "Hafta Sonu Savaşçısı", "{n} kez mükemmel hafta sonu", true, M_WEEKEND, 1, 4, 12),
            new B("kombo", "🎯", "Kombo Kralı", "{n}'li kombo yap", false, M_COMBO, 3, 5, 8),
            new B("sabah", "🌅", "Sabah İnsanı", "{n} kez sabah 9'dan önce 3 görev", true, M_MORNING, 1, 10, 50),
            new B("mukemmel", "💎", "Mükemmeliyetçi", "{n} Mükemmel Gün", false, M_PERFECT, 1, 7, 30, 100),
            new B("avci", "🏹", "Görev Avcısı", "{n} günlük görev tamamla", false, M_QUESTS, 10, 50, 200, 500),
            new B("koleksiyon", "🎁", "Koleksiyoncu", "{n} sandık aç", false, M_CHESTS, 5, 25, 100, 250),
            new B("seviye", "🏆", "Yükselen Yıldız", "Seviye {n}'e ulaş", false, M_LEVEL, 5, 10, 20, 40),
            new B("mezun", "🎓", "Mezun", "{n} alışkanlık otomatiğe bağlandı", false, M_GRAD, 1, 3, 5, 10),
            new B("denge", "⚖️", "Dengeli Yaşam", "4+ yaşam alanında iş yapılan {n} gün", false, M_BALANCE, 1, 10, 50, 150),
            new B("planlayici", "🗺️", "Planlayıcı", "{n} kez haftayı planla", false, M_PLANS, 1, 4, 12, 52),
            new B("gunlukcu", "📓", "Günlükçü", "{n} gün sonu değerlendirmesi", false, M_REFLECT, 7, 30, 100, 365),
            new B("esnek", "🌿", "Esnek Kahraman", "{n} esnek görev yap", false, M_FLEX, 10, 50, 200, 500),
            new B("sadik", "📅", "Sadık", "{n} aktif gün", false, M_USEDAYS, 7, 30, 100, 365),
            new B("makine", "✅", "Görev Makinesi", "Toplam {n} görev", false, M_TOTAL, 50, 250, 1000, 5000),
            new B("odul", "🍰", "Kendini Ödüllendir", "{n} kişisel ödül al", false, M_REWARD, 1, 5, 20),
            new B("stil", "🎩", "Stilist", "Yoldaşın için {n} eşya", false, M_STYLE, 1, 3, 6),
            new B("mimar", "🏝️", "Ada Mimarı", "Adanda {n} yapı", false, M_ISLAND, 10, 50, 150, 365),
            new B("sessiz", "🤫", "Sessiz Güç", "Hiç hatırlatma gerekmeden {n} gün tamamla", true, M_SILENT, 1, 5, 20),
            new B("gunaydin", "🌞", "Güne Merhaba", "{n} kez 'Güne Başla'", false, M_MSTART, 7, 30, 100),
            new B("soz", "✍️", "Söz Tutan", "İmzaladığın sözü 30 gün tuttun", true, M_PROMISE, 1),
            new B("cark", "🎡", "Şanslı", "Şans çarkını {n} kez çevir", false, M_SPINS, 1, 10, 50, 200),
            new B("hedefci", "🎯", "Hedef Odaklı", "{n} gün günlük XP hedefini tuttur", false, M_GOALS, 3, 14, 60, 200),
            new B("album", "🃏", "Koleksiyoncu Ruhu", "{n} farklı kart topla", false, M_CARDS, 5, 15, 30, 36),
            new B("kahraman", "🦸", "Hafta Kahramanı", "{n} hafta 5+ iyi gün", false, M_HERO, 1, 4, 12, 52),
            new B("usta", "🌳", "Alışkanlık Ustası", "Bir alışkanlığı {n}. seviyeye çıkar", false, M_HABITLV, 3, 5, 6, 7),
    };

    public static B get(String id) { for (B b : ALL) if (b.id.equals(id)) return b; return null; }

    public static int totalTiers() { int n = 0; for (B b : ALL) n += b.tiers.length; return n; }

    public static long metric(State s, int m) {
        State.Profile p = s.p;
        switch (m) {
            case M_START: return p.onboarded ? 1 : 0;
            case M_BEST: return Math.max(p.bestStreak, p.streak);
            case M_WATER_FIRST: return p.waterTotal > 0 ? 1 : 0;
            case M_WATER_DAYS: {
                int n = 0;
                for (Task t : s.tasks) if (t.type == Habit.COUNTER && "ml".equals(t.unit) && t.progress >= t.full && t.full > 0) n++;
                return n;
            }
            case M_WATER_L: return p.waterTotal / 1000;
            case M_FOCUS_SESS: { int n = 0; for (State.FocusLog f : s.focus) if (f.completed) n++; return n; }
            case M_FOCUS_DAYMAX: return p.maxFocusDay;
            case M_FOCUS_LONG: return p.longestSession;
            case M_FOCUS_H: return p.totalFocusMin / 60;
            case M_EARLY: return p.totalEarly;
            case M_PUNCTUAL: return Math.max(p.bestPunctual, p.punctualRun);
            case M_COMEBACK: return p.comebacks;
            case M_REPAIR: return p.repairs;
            case M_STORM: return p.stormSaves;
            case M_SLEEP: return Math.max(p.bestSleepStreak, p.sleepStreak);
            case M_LATE: return p.lateSaves;
            case M_WEEKEND: return p.weekendPerfect;
            case M_COMBO: return p.bestCombo;
            case M_MORNING: return p.morningTriple;
            case M_PERFECT: return p.perfectDays;
            case M_QUESTS: return p.questsDone;
            case M_CHESTS: return p.chestsOpened;
            case M_LEVEL: return Game.level(p.xp);
            case M_GRAD: return p.graduates;
            case M_BALANCE: return p.balanceDays;
            case M_PLANS: return p.plans;
            case M_REFLECT: return p.reflections;
            case M_FLEX: return p.flexDone;
            case M_USEDAYS: { int n = 0; for (DayRec r : s.days) if (r.done + r.mini > 0) n++; return n; }
            case M_TOTAL: return p.totalDone;
            case M_REWARD: return p.redeemed;
            case M_STYLE: {
                int n = 0;
                for (String o : p.owned) if (o.startsWith("h_") || o.startsWith("a_")) n++;
                return n;
            }
            case M_ISLAND: return s.island.size();
            case M_SILENT: return p.silentDays;
            case M_MSTART: return p.morningStarts;
            case M_SPINS: return p.spins;
            case M_GOALS: return p.goalDays;
            case M_CARDS: return s.cards.size();
            case M_HERO: return p.heroWeeks;
            case M_HABITLV: { int mx = 0; for (Habit h : s.habits) mx = Math.max(mx, Rewards.habitLevel(h.totalDone)); return mx; }
            case M_PROMISE: return (p.signature != null && !p.signature.isEmpty() && Math.max(p.bestStreak, p.streak) >= 30) ? 1 : 0;
        }
        return 0;
    }

    /** Yeni kazanılan rozet kademelerini ekler ve olay üretir. */
    public static void check(State s, int day, Ev.List ev) {
        for (B b : ALL) {
            long v = metric(s, b.metric);
            int have = s.badgeTier(b.id);
            int reach = 0;
            for (int i = 0; i < b.tiers.length; i++) if (v >= b.tiers[i]) reach = i + 1;
            for (int t = have + 1; t <= reach; t++) {
                State.BadgeEarn e = new State.BadgeEarn();
                e.id = b.id; e.tier = b.tiers.length == 1 ? 0 : t; e.day = day;
                if (b.tiers.length == 1) e.tier = 1;
                s.badges.add(e);
                Ev x = ev.add(Ev.BADGE, t, b.emoji + " " + b.name);
                x.extra = b.id;
                s.p.coins += 25 * t;
            }
        }
    }
}
