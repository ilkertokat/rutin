package com.ilker.rutin.core;

import java.util.ArrayList;
import java.util.Random;

/** Seviye, unvan, mağaza, sandık ve ada kuralları. */
public final class Game {
    private Game() {}

    // ---------------- Seviye ----------------
    public static long levelStart(int level) { return 40L * (level - 1) * level; }

    public static int level(long xp) {
        int l = 1;
        while (levelStart(l + 1) <= xp) l++;
        return l;
    }

    public static double levelProgress(long xp) {
        int l = level(xp);
        long a = levelStart(l), b = levelStart(l + 1);
        return (xp - a) / (double) (b - a);
    }

    public static final String[] TITLES = {"Filiz", "Çırak", "Kararlı", "Disiplinli", "Usta", "Bilge", "Efsane"};
    public static final String[] TITLE_EMOJI = {"🌱", "🔨", "💪", "🎯", "🥋", "🦉", "👑"};

    public static int titleIndex(int level) {
        if (level < 5) return 0;
        if (level < 10) return 1;
        if (level < 15) return 2;
        if (level < 20) return 3;
        if (level < 30) return 4;
        if (level < 40) return 5;
        return 6;
    }

    public static String title(int level) { return TITLE_EMOJI[titleIndex(level)] + " " + TITLES[titleIndex(level)]; }

    // ---------------- Seri alevi ----------------
    /** Seri uzunluğuna göre alev evresi: 0 turuncu, 1 kırmızı, 2 mor, 3 mavi, 4 yıldız. */
    public static int flameStage(int streak) {
        if (streak >= 365) return 4;
        if (streak >= 100) return 3;
        if (streak >= 30) return 2;
        if (streak >= 7) return 1;
        return 0;
    }

    public static final String[] FLAME_NAME = {"Turuncu Alev", "Kızıl Alev", "Mor Alev", "Mavi Alev", "Yıldız Alevi"};
    public static final int[] MILESTONES = {3, 7, 14, 21, 30, 50, 66, 100, 150, 200, 250, 300, 365, 500, 730, 1000};

    public static boolean isMilestone(int s) { for (int m : MILESTONES) if (m == s) return true; return false; }

    public static int nextMilestone(int s) { for (int m : MILESTONES) if (m > s) return m; return s + 100; }

    // ---------------- Mağaza ----------------
    public static final int C_THEME = 0, C_SOUND = 1, C_HAT = 2, C_ACC = 3, C_ISLAND = 4, C_FREEZE = 5;
    public static final String[] CAT_NAME = {"Temalar", "Ses Paketleri", "Şapkalar", "Aksesuarlar", "Ada", "Seri Dondurma"};

    public static final class Item {
        public final String id, name, emoji; public final int cat, price, minLevel, rarity;
        Item(String id, String name, String emoji, int cat, int price, int minLevel, int rarity) {
            this.id = id; this.name = name; this.emoji = emoji; this.cat = cat; this.price = price; this.minLevel = minLevel; this.rarity = rarity;
        }
    }

    public static final Item[] ITEMS = {
            new Item("t0", "Gece", "🌙", C_THEME, 0, 1, 0),
            new Item("t1", "Okyanus", "🌊", C_THEME, 300, 2, 1),
            new Item("t2", "Orman", "🌲", C_THEME, 300, 3, 1),
            new Item("t3", "Gün Batımı", "🌅", C_THEME, 400, 4, 1),
            new Item("t4", "Sakura", "🌸", C_THEME, 500, 6, 2),
            new Item("t5", "Neon Gece", "🌃", C_THEME, 600, 8, 2),
            new Item("t6", "Aydınlık", "☀️", C_THEME, 300, 2, 1),
            new Item("t7", "Retro", "👾", C_THEME, 700, 10, 2),
            new Item("s0", "Klasik", "🔔", C_SOUND, 0, 1, 0),
            new Item("s1", "Retro 8-bit", "👾", C_SOUND, 250, 3, 1),
            new Item("s2", "Yumuşak Marimba", "🎐", C_SOUND, 250, 4, 1),
            new Item("s3", "Parlak Orkestra", "🎺", C_SOUND, 450, 7, 2),
            new Item("h_beanie", "Bere", "🧶", C_HAT, 150, 1, 0),
            new Item("h_party", "Parti Şapkası", "🎉", C_HAT, 200, 2, 1),
            new Item("h_flower", "Çiçek Taç", "🌼", C_HAT, 180, 3, 1),
            new Item("h_grad", "Mezuniyet Kepi", "🎓", C_HAT, 400, 6, 2),
            new Item("h_crown", "Kral Tacı", "👑", C_HAT, 900, 12, 3),
            new Item("a_scarf", "Atkı", "🧣", C_ACC, 150, 1, 0),
            new Item("a_bow", "Papyon", "🎀", C_ACC, 150, 2, 0),
            new Item("a_glasses", "Gözlük", "👓", C_ACC, 220, 3, 1),
            new Item("a_headph", "Kulaklık", "🎧", C_ACC, 320, 5, 2),
            new Item("i_boat", "Tekne", "⛵", C_ISLAND, 250, 2, 1),
            new Item("i_bridge", "Köprü", "🌉", C_ISLAND, 300, 3, 1),
            new Item("i_balloon", "Sıcak Hava Balonu", "🎈", C_ISLAND, 350, 4, 1),
            new Item("i_windmill", "Yel Değirmeni", "🌬️", C_ISLAND, 400, 5, 2),
            new Item("i_lighthouse", "Deniz Feneri", "🗼", C_ISLAND, 500, 6, 2),
            new Item("i_rainbow", "Gökkuşağı", "🌈", C_ISLAND, 600, 8, 2),
            new Item("i_castle", "Kale", "🏰", C_ISLAND, 1200, 15, 3),
            new Item("freeze", "Seri Dondurma", "🧊", C_FREEZE, 200, 1, 0),
    };

    public static Item item(String id) { for (Item i : ITEMS) if (i.id.equals(id)) return i; return null; }

    public static final int MAX_FREEZES = 3;

    // ---------------- Ada ----------------
    // İyi günlerle kazanılan yapılar
    public static final int I_PINE = 0, I_TREE = 1, I_FLOWER = 2, I_BUSH = 3, I_HOUSE = 4, I_BIGHOUSE = 5, I_ROCK = 6;
    // Satın alınan / sandıktan çıkan yapılar
    public static final int I_BOAT = 10, I_BRIDGE = 11, I_BALLOON = 12, I_WINDMILL = 13, I_LIGHTHOUSE = 14, I_RAINBOW = 15, I_CASTLE = 16;
    public static final int I_GOLDTREE = 20, I_FOUNTAIN = 21, I_STATUE = 22;

    public static int islandCodeFor(String itemId) {
        switch (itemId) {
            case "i_boat": return I_BOAT;
            case "i_bridge": return I_BRIDGE;
            case "i_balloon": return I_BALLOON;
            case "i_windmill": return I_WINDMILL;
            case "i_lighthouse": return I_LIGHTHOUSE;
            case "i_rainbow": return I_RAINBOW;
            case "i_castle": return I_CASTLE;
        }
        return -1;
    }

    /** İyi geçen n'inci gün için adaya eklenecek yapı. */
    public static int islandForDay(int goodDayIndex, boolean perfect, long seed) {
        if (goodDayIndex > 0 && goodDayIndex % 30 == 29) return I_BIGHOUSE;
        if (goodDayIndex % 7 == 6) return I_HOUSE;
        Random r = new Random(seed * 7919 + goodDayIndex);
        if (perfect && r.nextInt(3) == 0) return I_FLOWER;
        int x = r.nextInt(10);
        if (x < 4) return I_PINE;
        if (x < 7) return I_TREE;
        if (x < 9) return I_BUSH;
        return I_ROCK;
    }

    // ---------------- Sandık ----------------
    public static final String[] RARITY = {"Sıradan", "Nadir", "Epik", "Efsanevi"};
    public static final int[] RARITY_COLOR = {0xFFB8C0CC, 0xFF4FB3FF, 0xFFB57BFF, 0xFFFFC53D};

    public static int rollRarity(Random r, int minRarity) {
        int x = r.nextInt(100);
        int rar = x < 60 ? 0 : x < 88 ? 1 : x < 98 ? 2 : 3;
        return Math.max(rar, minRarity);
    }

    /** Sandığı açar ve ödülü duruma uygular. Dönen metin kullanıcıya gösterilir. */
    public static ArrayList<String> openChest(State s, State.Chest c, Random r, long now) {
        ArrayList<String> out = new ArrayList<>();
        State.Profile p = s.p;
        int[][] coinRange = {{20, 45}, {60, 110}, {150, 220}, {350, 500}};
        int coins = coinRange[c.rarity][0] + r.nextInt(coinRange[c.rarity][1] - coinRange[c.rarity][0] + 1);
        p.coins += coins;
        out.add("🪙 " + coins + " coin");
        if (c.rarity >= 1 && r.nextInt(100) < (c.rarity == 1 ? 35 : 60)) {
            if (p.freezes < MAX_FREEZES) { p.freezes++; out.add("🧊 Seri Dondurma"); }
            else { p.coins += 60; out.add("🪙 +60 coin (dondurma deposu dolu)"); }
        }
        if (c.rarity >= 1 && r.nextInt(100) < 40) {
            p.boostUntil = Math.max(p.boostUntil, now) + 60;
            out.add("⚗️ XP İksiri (1 saat 2x XP)");
        }
        if (c.rarity >= 2) {
            // Sahip olunmayan bir kozmetik
            ArrayList<Item> pool = new ArrayList<>();
            for (Item it : ITEMS) if ((it.cat == C_HAT || it.cat == C_ACC || it.cat == C_ISLAND) && !s.owns(it.id) && it.rarity <= c.rarity) pool.add(it);
            if (!pool.isEmpty()) {
                Item it = pool.get(r.nextInt(pool.size()));
                s.p.owned.add(it.id);
                if (it.cat == C_ISLAND) s.island.add(islandCodeFor(it.id));
                out.add(it.emoji + " " + it.name);
            } else { p.coins += 150; out.add("🪙 +150 coin"); }
        }
        if (c.rarity == 3) {
            int[] rare = {I_GOLDTREE, I_FOUNTAIN, I_STATUE};
            int code = rare[r.nextInt(rare.length)];
            s.island.add(code);
            out.add(code == I_GOLDTREE ? "🌳 Altın Ağaç (ada)" : code == I_FOUNTAIN ? "⛲ Çeşme (ada)" : "🗿 Heykel (ada)");
        }
        int cardChance = c.rarity == 0 ? 45 : c.rarity == 1 ? 70 : 100;
        if (r.nextInt(100) < cardChance) out.add(Rewards.grantCard(s, r, Math.min(3, c.rarity + 1)));
        if (c.rarity == 3) out.add(Rewards.grantCard(s, r, 3));
        p.chestsOpened++;
        return out;
    }
}
