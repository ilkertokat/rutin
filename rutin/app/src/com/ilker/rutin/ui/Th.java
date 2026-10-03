package com.ilker.rutin.ui;

/** Tema renkleri (mağazadan açılan temalar). */
public final class Th {
    private Th() {}

    public static int bg, surface, surface2, stroke, text, dim, faint, primary, primary2, success, water, focus, warn, crit, gold;
    public static boolean light;
    public static int id = -1;

    static final int[][] T = {
            // bg, surface, surface2, text, dim, primary, primary2(accent)
            {0xFF0F0F17, 0xFF1A1A26, 0xFF242434, 0xFFF1F1F8, 0xFF9A9AB2, 0xFFFF7A45, 0xFFFFB35C},  // Gece
            {0xFF07161F, 0xFF0F2531, 0xFF173442, 0xFFEAF8FF, 0xFF8FB5C7, 0xFF2EC4E6, 0xFF7BE3F7},  // Okyanus
            {0xFF0C1510, 0xFF15231B, 0xFF1F3226, 0xFFEEF8F0, 0xFF93B39B, 0xFF4ADE80, 0xFFB5F28F},  // Orman
            {0xFF1A0F14, 0xFF28171F, 0xFF36212B, 0xFFFFF0EA, 0xFFC2A1A8, 0xFFFF8A5C, 0xFFFFC36B},  // Gün batımı
            {0xFF1B1117, 0xFF2A1A24, 0xFF382431, 0xFFFFF0F6, 0xFFC7A4B6, 0xFFFF8FB8, 0xFFFFC2D8},  // Sakura
            {0xFF0A0714, 0xFF150F26, 0xFF201737, 0xFFF3EEFF, 0xFFA59CC4, 0xFFC77DFF, 0xFF4DEEEA},  // Neon
            {0xFFF7F4EE, 0xFFFFFFFF, 0xFFF0EBE3, 0xFF1F1F29, 0xFF6E6E7E, 0xFFFF6B35, 0xFFFF9F45},  // Aydınlık
            {0xFF1D1B2E, 0xFF2A2744, 0xFF363256, 0xFFFFF8E1, 0xFFB3AED0, 0xFFFFD23F, 0xFFFF6B6B},  // Retro
    };

    public static void apply(int theme) {
        theme = Math.max(0, Math.min(T.length - 1, theme));
        id = theme;
        int[] t = T[theme];
        bg = t[0]; surface = t[1]; surface2 = t[2]; text = t[3]; dim = t[4]; primary = t[5]; primary2 = t[6];
        light = theme == 6;
        faint = Art.alpha(text, 0.08f);
        stroke = Art.alpha(text, light ? 0.1f : 0.07f);
        success = light ? 0xFF22B45E : 0xFF3DDC84;
        water = light ? 0xFF2E90E6 : 0xFF4FB3FF;
        focus = light ? 0xFF7C5CF0 : 0xFFA78BFA;
        warn = light ? 0xFFE8930C : 0xFFFFB84D;
        crit = light ? 0xFFE5484D : 0xFFF87171;
        gold = 0xFFFFC53D;
    }

    public static int themeOf(String itemId) { return Integer.parseInt(itemId.substring(1)); }

    /** Günün saatine göre üst gradyan (sabah pembe-turuncu, öğlen mavi, akşam mor, gece koyu). */
    public static int[] skyFor(int minuteOfDay) {
        int m = ((minuteOfDay % 1440) + 1440) % 1440;
        if (light) {
            if (m < 360) return new int[]{0xFFDCD6F7, bg};
            if (m < 660) return new int[]{0xFFFFE1C9, bg};
            if (m < 1020) return new int[]{0xFFD6ECFF, bg};
            if (m < 1260) return new int[]{0xFFF4D3E6, bg};
            return new int[]{0xFFD9D6F0, bg};
        }
        if (m < 330) return new int[]{Art.mix(0xFF101433, bg, 0.2f), bg};
        if (m < 660) return new int[]{Art.mix(0xFF5A2E3E, bg, 0.25f), bg};
        if (m < 1020) return new int[]{Art.mix(0xFF1E3A5C, bg, 0.25f), bg};
        if (m < 1260) return new int[]{Art.mix(0xFF3E2560, bg, 0.2f), bg};
        return new int[]{Art.mix(0xFF141838, bg, 0.2f), bg};
    }
}
