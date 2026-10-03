package com.ilker.rutin.core;

/** Bir günün özeti (kapanışta hesaplanır, gün içinde canlı güncellenir). */
public class DayRec {
    public static final int OPEN = 0, FULL = 1, MINI = 2, FROZEN = 3, BROKEN = 4, NEUTRAL = 5, SHIELD = 6;

    public int day;
    public int state = OPEN;
    public int total, done, mini;
    public double ratio;
    public int xp;
    public int mood;               // 0 = girilmedi, 1..5
    public boolean perfect;
    public int focusMin;
    public boolean disrupted;
    public boolean sleepOk;
    public boolean met;            // gün gereksinimi karşılandı (seri uzadı)
    public boolean easy;           // kolay başlangıç günü
    public String reflection = "";
    public String intention = "";  // ertesi gün için niyet
    public int notifs;
    public int early;
    public int snoozes;
    public boolean frozenFree;
    public int combo;              // günün en yüksek kombosu
    public int waterNoon;          // öğlene kadar içilen su
    public boolean started;        // 'Güne Başla' basıldı
    public boolean summaryDone;    // gün sonu özeti görüldü
    public boolean questsChest;
    public boolean goalHit;        // günlük XP hedefi
    public boolean spun;           // şans çarkı çevrildi    // 3 görev sandığı verildi

    public boolean good() { return state == FULL || state == MINI; }
}
