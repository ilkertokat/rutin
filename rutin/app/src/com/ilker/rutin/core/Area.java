package com.ilker.rutin.core;

/** Yaşam alanları (RPG istatistikleri). */
public final class Area {
    private Area() {}
    public static final int HEALTH = 0, MIND = 1, CAREER = 2, HOME = 3, SOCIAL = 4, SOUL = 5;
    public static final String[] NAME = {"Sağlık", "Zihin", "Kariyer", "Ev", "Sosyal", "Ruh"};
    public static final String[] EMOJI = {"❤️", "🧠", "💼", "🏠", "👥", "🕊️"};
    public static final int[] COLOR = {0xFFFF6B8A, 0xFFA78BFA, 0xFF4FB3FF, 0xFFFFA24D, 0xFFFFD84D, 0xFF3DD9C9};

    /** Ay teması: ayın bonuslu yaşam alanı (+%25 XP). */
    public static int monthArea(int month) {
        int[] m = {CAREER, SOCIAL, HEALTH, MIND, HEALTH, SOUL, SOCIAL, HOME, CAREER, MIND, HOME, SOUL};
        return m[(month - 1) % 12];
    }

    public static final String[] MONTH_THEME = {"Kariyer Ayı", "Bağ Kurma Ayı", "Bahar Sağlık Ayı", "Okuma ve Öğrenme Ayı",
            "Hareket Ayı", "İç Huzur Ayı", "Dostluk Ayı", "Düzen Ayı", "Yeni Dönem Ayı", "Okuma Ayı", "Ev ve Yuva Ayı", "Yıl Sonu Huzur Ayı"};
}
