package com.ilker.rutin.core;

import java.util.ArrayList;

/** Belirli bir mantıksal güne ait somut görev. */
public class Task {
    public static final int PENDING = 0, DONE = 1, MINI = 2, MISSED = 3, SHIELDED = 4, MOVED = 5, SKIPPED = 6;

    public long id;
    public long habitId;           // 0 = tek seferlik
    public int day;                // mantıksal epoch gün
    public String title = "";
    public String emoji = "✅";
    public int type = Habit.REQUIRED;
    public int area;
    public int targetMin = -1;     // takvim dakikası
    public int deadlineMin = -1;   // takvim dakikası (-1 = kurala göre)
    public int graceMin = 15;
    public int full = 1, mini = 0, step = 1;
    public int origFull = 0;       // plan dışı olayda düşürüldüyse orijinal hedef
    public String unit = "", miniText = "";
    public int priority = Habit.P_NORMAL;
    public boolean streak = true;
    public boolean custom;         // kullanıcı bu kopyayı elle değiştirdi (yeniden üretimde korunur)
    public boolean graduated;      // alışkanlık mezun: daha az bildirim
    public int fromDay = -1;       // başka günden taşındıysa

    // Durum
    public int status = PENDING;
    public int progress;
    public long doneAt;            // yerel epoch dakika
    public boolean early, onTime;
    public long snoozeUntil;
    public int snoozes;
    public long lastNotif;
    public int notifCount;
    public long lastProgressAt;
    public boolean miniOk;         // plan dışı olay: mini yeterli
    public int xpGiven, coinsGiven;
    public ArrayList<Integer> blocks = new ArrayList<>(); // süreli hedef için planlı blok başlangıçları (takvim dk)

    public boolean isOpen() { return status == PENDING; }

    public boolean isComplete() { return status == DONE || status == MINI; }

    public boolean counts() { return status != MOVED && status != SKIPPED; }

    public boolean isQuantity() { return type == Habit.COUNTER || type == Habit.DURATION; }

    public boolean hasMini() { return isQuantity() ? mini > 0 && mini < full : miniText != null && !miniText.isEmpty(); }

    public double ratio() {
        if (isComplete() && !isQuantity()) return 1;
        if (!isQuantity()) return 0;
        return full <= 0 ? 1 : Math.min(1.0, progress / (double) full);
    }

    public String amount(int v) { return Fmt.amount(v, unit, type); }
}
