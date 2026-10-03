package com.ilker.rutin.core;

/**
 * Zaman yardımcıları. Tüm zamanlar "yerel epoch dakikası" (1970'ten beri yerel saat dakikası) olarak tutulur.
 * Mantıksal gün: kullanıcının uyku ortasında (rollover) değişir; böylece gece 00:30'da yapılan iş "dün"e sayılır.
 * Gün içi dakika (dm): mantıksal günün takvim gece yarısından itibaren dakika; rollover'dan önceki saatler +1440 alır.
 */
public final class Tm {
    private Tm() {}

    public static final String[] DAY_SHORT = {"Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz"};
    public static final String[] DAY_LONG = {"Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi", "Pazar"};
    public static final String[] DAY_LETTER = {"P", "S", "Ç", "P", "C", "C", "P"};
    public static final String[] MONTH = {"Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran", "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"};

    /** Uyku ortası: uyku ile uyanma arasındaki dairesel orta nokta (takvim dakikası 0..1439). */
    public static int rollover(int wakeMin, int sleepMin) {
        int s = norm(sleepMin), w = norm(wakeMin);
        int len = w - s; if (len <= 0) len += 1440;
        return norm(s + len / 2);
    }

    public static int norm(int m) { return ((m % 1440) + 1440) % 1440; }

    /** Yerel epoch dakikasından mantıksal gün. */
    public static int day(long localMin, int rollover) {
        return (int) Math.floorDiv(localMin - rollover, 1440L);
    }

    /** Mantıksal gün içi dakika (rollover..rollover+1439). */
    public static int dm(long localMin, int rollover) {
        int d = day(localMin, rollover);
        return (int) (localMin - d * 1440L);
    }

    /** Takvim dakikasını (0..1439) mantıksal gün içi dakikaya çevirir. */
    public static int toDm(int calMin, int rollover) {
        calMin = norm(calMin);
        return calMin < rollover ? calMin + 1440 : calMin;
    }

    public static long abs(int day, int dm) { return day * 1440L + dm; }

    /** Pazartesi = 0 ... Pazar = 6 (1970-01-01 Perşembe). */
    public static int weekday(int epochDay) { return Math.floorMod(epochDay + 3, 7); }

    public static int weekStart(int epochDay) { return epochDay - weekday(epochDay); }

    public static String hhmm(int minute) {
        int m = norm(minute);
        return String.format("%02d:%02d", m / 60, m % 60);
    }

    public static String dur(int minutes) {
        if (minutes < 60) return minutes + " dk";
        int h = minutes / 60, m = minutes % 60;
        return m == 0 ? h + " sa" : h + " sa " + m + " dk";
    }

    public static String durShort(int minutes) {
        int h = minutes / 60, m = minutes % 60;
        if (h == 0) return m + "dk";
        return m == 0 ? h + "s" : h + "s" + m;
    }

    // ---- Takvim (Gregoryen) dönüşümleri, java.time'a bağımlı olmadan ----
    /** epochDay -> {yıl, ay(1-12), gün} */
    public static int[] ymd(long epochDay) {
        long zeroDay = epochDay + 719528 - 60;
        long adjust = 0;
        if (zeroDay < 0) {
            long adjustCycles = (zeroDay + 1) / 146097 - 1;
            adjust = adjustCycles * 400;
            zeroDay += -adjustCycles * 146097;
        }
        long yearEst = (400 * zeroDay + 591) / 146097;
        long doyEst = zeroDay - (365 * yearEst + yearEst / 4 - yearEst / 100 + yearEst / 400);
        if (doyEst < 0) {
            yearEst--;
            doyEst = zeroDay - (365 * yearEst + yearEst / 4 - yearEst / 100 + yearEst / 400);
        }
        yearEst += adjust;
        int marchDoy0 = (int) doyEst;
        int marchMonth0 = (marchDoy0 * 5 + 2) / 153;
        int month = (marchMonth0 + 2) % 12 + 1;
        int dom = marchDoy0 - (marchMonth0 * 306 + 5) / 10 + 1;
        yearEst += marchMonth0 / 10;
        return new int[]{(int) yearEst, month, dom};
    }

    public static int epochDay(int y, int m, int d) {
        long total = 365L * y;
        if (y >= 0) total += (y + 3) / 4 - (y + 99) / 100 + (y + 399) / 400;
        else total -= y / -4 - y / -100 + y / -400;
        total += (367 * m - 362) / 12;
        total += d - 1;
        if (m > 2) { total--; if (!leap(y)) total--; }
        return (int) (total - 719528);
    }

    public static boolean leap(int y) { return (y % 4 == 0 && y % 100 != 0) || y % 400 == 0; }

    public static String dateLong(int epochDay) {
        int[] a = ymd(epochDay);
        return a[2] + " " + MONTH[a[1] - 1] + " " + DAY_LONG[weekday(epochDay)];
    }

    public static String dateShort(int epochDay) {
        int[] a = ymd(epochDay);
        return a[2] + " " + MONTH[a[1] - 1].substring(0, 3);
    }

    public static int month(int epochDay) { return ymd(epochDay)[1]; }
}
