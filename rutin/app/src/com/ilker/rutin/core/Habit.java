package com.ilker.rutin.core;

/** Tekrarlanan alışkanlık / görev tanımı. Günlük somut kopyaları {@link Task} olarak üretilir. */
public class Habit {
    public static final int ANCHOR = 0, REQUIRED = 1, COUNTER = 2, DURATION = 3, FLEX = 4, ONCE = 5;
    public static final String[] TYPE_NAME = {"Çapa Rutin", "Zorunlu", "Sayaçlı Hedef", "Süreli Hedef", "Esnek", "Tek Seferlik"};
    public static final String[] TYPE_EMOJI = {"⚓", "🔒", "🔢", "⏱️", "🌿", "📌"};
    public static final String[] TYPE_DESC = {
            "Her gün aynı saatte. Rutininin iskeleti.",
            "O gün mutlaka yapılmalı, bir son saati var.",
            "Gün boyunca biriktirilen miktar (su, sayfa, adım).",
            "Toplam süre hedefi, Odak Modu ile çalışır.",
            "Boş zamanda. Yapılırsa bonus, yapılmazsa ceza yok.",
            "Tek bir güne ait, tekrarlanmayan iş."};

    public static final int P_LOW = 0, P_NORMAL = 1, P_CRITICAL = 2;

    public long id;
    public String title = "";
    public String emoji = "✅";
    public int type = REQUIRED;
    public int area = 0;              // Area.*
    public int daysMask = 127;        // bit0 = Pazartesi ... bit6 = Pazar
    public int onceDay = -1;          // ONCE için epoch gün
    public int targetMin = -1;        // takvim dakikası (0..1439) ya da -1
    public int deadlineMin = -1;      // takvim dakikası ya da -1 (varsayılan kural)
    public int graceMin = 15;
    public int full = 1;              // tam hedef (ml, dk, sayfa...) ; ikili görevler için 1
    public int mini = 0;              // mini hedef; 0 = mini yok
    public String unit = "";
    public int step = 1;              // sayaç artışı
    public String miniText = "";      // ikili görevlerde mini versiyonun tarifi
    public String why = "";           // Neden kartı
    public String ifThen = "";        // Eğer-ise planı
    public long afterId = 0;          // alışkanlık zinciri: bu işten sonra
    public int priority = P_NORMAL;
    public boolean streak = true;     // seriyi etkiler mi
    public boolean archived = false;
    public int createdDay;

    // İstatistik
    public int curStreak, bestStreak, totalDone, totalMini;
    public int lastDoneDay = -99999;
    public boolean graduated;
    public int graduatedDay = -1;
    public java.util.ArrayList<Integer> doneMinutes = new java.util.ArrayList<>(); // son tamamlanma dakikaları (öğrenen zamanlama)

    public boolean scheduledOn(int day) {
        if (archived) return false;
        if (type == ONCE) return day == onceDay;
        if (day < createdDay) return false;
        return (daysMask & (1 << Tm.weekday(day))) != 0;
    }

    public boolean isQuantity() { return type == COUNTER || type == DURATION; }

    public boolean hasMini() { return isQuantity() ? mini > 0 && mini < full : miniText != null && !miniText.isEmpty(); }

    public String fmtAmount(int v) { return Fmt.amount(v, unit, type); }

    public static boolean defaultStreak(int type) { return type != FLEX; }
}
