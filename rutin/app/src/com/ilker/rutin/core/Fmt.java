package com.ilker.rutin.core;

/** Metin biçimlendirme yardımcıları. */
public final class Fmt {
    private Fmt() {}

    public static String amount(int v, String unit, int type) {
        if (type == Habit.DURATION) return Tm.dur(v);
        if ("ml".equals(unit)) {
            if (v >= 1000) {
                double l = v / 1000.0;
                String s = (l == Math.floor(l)) ? String.valueOf((int) l) : String.format(java.util.Locale.US, v % 100 == 0 ? "%.1f" : "%.2f", l).replace('.', ',');
                if (s.endsWith(",0")) s = s.substring(0, s.length() - 2);
                return s + " L";
            }
            return v + " ml";
        }
        if (unit == null || unit.isEmpty()) return String.valueOf(v);
        return v + " " + unit;
    }

    public static String num(long n) {
        String s = String.valueOf(Math.abs(n));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (i > 0 && (s.length() - i) % 3 == 0) sb.append('.');
            sb.append(s.charAt(i));
        }
        return (n < 0 ? "-" : "") + sb;
    }

    public static String pct(double r) { return Math.round(r * 100) + "%"; }

    /** Büyük harf (Türkçe i/İ kurallarıyla). */
    public static String upper(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            if (c == 'i') sb.append('İ');
            else if (c == 'ı') sb.append('I');
            else sb.append(Character.toUpperCase(c));
        }
        return sb.toString();
    }
}
