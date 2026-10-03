package com.ilker.rutin.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;

import com.ilker.rutin.core.Game;

import java.util.List;
import java.util.Random;

/** Tüm özel çizimler: alev, halka, şişe, yoldaş, ada, ağaç, madalya, sandık. Hem görünümler hem widget'lar kullanır. */
public final class Art {
    private Art() {}

    static final Paint P = new Paint(Paint.ANTI_ALIAS_FLAG);
    static final Paint S = new Paint(Paint.ANTI_ALIAS_FLAG);
    static final Path PATH = new Path();
    static final RectF RF = new RectF();

    static {
        S.setStyle(Paint.Style.STROKE);
        S.setStrokeCap(Paint.Cap.ROUND);
        S.setStrokeJoin(Paint.Join.ROUND);
    }

    static void shader(Shader sh) { P.setColor(0xFFFFFFFF); P.setShader(sh); }

    public static int alpha(int c, float a) { return (Math.round(Color.alpha(c) * a) << 24) | (c & 0x00FFFFFF); }

    public static int mix(int a, int b, float t) {
        t = Math.max(0, Math.min(1, t));
        return Color.argb(
                (int) (Color.alpha(a) + (Color.alpha(b) - Color.alpha(a)) * t),
                (int) (Color.red(a) + (Color.red(b) - Color.red(a)) * t),
                (int) (Color.green(a) + (Color.green(b) - Color.green(a)) * t),
                (int) (Color.blue(a) + (Color.blue(b) - Color.blue(a)) * t));
    }

    // ---------------- Alev ----------------
    static final int[][] FLAME = {
            {0xFFFF4D1A, 0xFFFF9A3C, 0xFFFFC56B, 0xFFFFF1A8},
            {0xFFD91A2A, 0xFFFF4D3C, 0xFFFF9166, 0xFFFFE0B8},
            {0xFF6B21E8, 0xFFA855F7, 0xFFD6A2FF, 0xFFF7EBFF},
            {0xFF1D4ED8, 0xFF3B9BFF, 0xFF8FD3FF, 0xFFEAF8FF},
            {0xFFFF9F00, 0xFFFFD000, 0xFFFFF08A, 0xFFFFFFFF},
    };
    static final int[] FLAME_OFF = {0xFF4A4A5E, 0xFF6E6E86, 0xFF9090A8, 0xFFB8B8CC};

    public static int flameColor(int stage) { return FLAME[Math.max(0, Math.min(4, stage))][1]; }

    static void flamePath(Path p, float cx, float by, float w, float h, float sway) {
        p.reset();
        float tx = cx + sway;
        p.moveTo(tx, by - h);
        p.cubicTo(cx + w * 0.12f + sway * 0.6f, by - h * 0.72f, cx + w * 0.56f, by - h * 0.52f, cx + w * 0.5f, by - h * 0.27f);
        p.cubicTo(cx + w * 0.46f, by - h * 0.06f, cx + w * 0.26f, by, cx, by);
        p.cubicTo(cx - w * 0.26f, by, cx - w * 0.46f, by - h * 0.06f, cx - w * 0.5f, by - h * 0.27f);
        p.cubicTo(cx - w * 0.56f, by - h * 0.52f, cx - w * 0.12f + sway * 0.6f, by - h * 0.72f, tx, by - h);
        p.close();
    }

    /** Alev çizer. (cx, by) = alt orta nokta, h = yükseklik, t = saniye cinsinden zaman (titreşim için). */
    public static void flame(Canvas c, float cx, float by, float h, int stage, boolean lit, float t) {
        int[] col = lit ? FLAME[Math.max(0, Math.min(4, stage))] : FLAME_OFF;
        float fl = lit ? 1f : 0f;
        float sway = fl * (float) (Math.sin(t * 5.3) * h * 0.035 + Math.sin(t * 8.9) * h * 0.02);
        float hs = h * (1 + fl * (float) (Math.sin(t * 7.1) * 0.025 + Math.sin(t * 12.3) * 0.015));
        if (lit) {
            float gr = Math.min(h * 0.75f, Math.min(cx, c.getWidth() - cx) * 0.98f);
            shader(new RadialGradient(cx, by - h * 0.42f, gr, alpha(col[1], 0.45f), alpha(col[1], 0f), Shader.TileMode.CLAMP));
            c.drawCircle(cx, by - h * 0.42f, gr, P);
        }
        float w = hs * 0.74f;
        flamePath(PATH, cx, by, w, hs, sway);
        shader(new LinearGradient(cx, by - hs, cx, by, col[1], col[0], Shader.TileMode.CLAMP));
        c.drawPath(PATH, P);
        float h2 = hs * 0.7f;
        flamePath(PATH, cx, by - hs * 0.02f, w * 0.68f, h2, sway * 0.7f);
        shader(new LinearGradient(cx, by - h2, cx, by, col[2], col[1], Shader.TileMode.CLAMP));
        c.drawPath(PATH, P);
        float h3 = hs * 0.42f;
        flamePath(PATH, cx, by - hs * 0.04f, w * 0.42f, h3, sway * 0.4f);
        shader(new LinearGradient(cx, by - h3, cx, by, col[3], col[2], Shader.TileMode.CLAMP));
        c.drawPath(PATH, P);
        P.setShader(null);
        if (lit && stage >= 4) {
            P.setColor(0xFFFFFFFF);
            for (int i = 0; i < 6; i++) {
                double a = t * 1.3 + i * Math.PI / 3;
                float rr = h * 0.62f;
                float sx = cx + (float) Math.cos(a) * rr, sy = by - h * 0.45f + (float) Math.sin(a) * rr * 0.8f;
                star(c, sx, sy, h * 0.05f * (1 + 0.4f * (float) Math.sin(t * 4 + i)), 0xFFFFF6C2);
            }
        }
    }

    public static void star(Canvas c, float x, float y, float r, int color) {
        PATH.reset();
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4 - Math.PI / 2;
            float rr = i % 2 == 0 ? r : r * 0.35f;
            float px = x + (float) Math.cos(a) * rr, py = y + (float) Math.sin(a) * rr;
            if (i == 0) PATH.moveTo(px, py); else PATH.lineTo(px, py);
        }
        PATH.close();
        P.setShader(null);
        P.setColor(color);
        c.drawPath(PATH, P);
    }

    // ---------------- Halka ----------------
    public static void ring(Canvas c, float cx, float cy, float r, float stroke, float frac, int color, int track) {
        S.setShader(null);
        S.setStrokeWidth(stroke);
        S.setColor(track);
        c.drawCircle(cx, cy, r, S);
        if (frac <= 0) return;
        RF.set(cx - r, cy - r, cx + r, cy + r);
        S.setColor(color);
        float sweep = Math.min(1f, frac) * 360f;
        c.drawArc(RF, -90, sweep, false, S);
        if (frac > 1f) {
            S.setColor(mix(color, 0xFFFFFFFF, 0.35f));
            c.drawArc(RF, -90, Math.min(1f, frac - 1f) * 360f, false, S);
        }
    }

    // ---------------- Su şişesi ----------------
    public static void bottle(Canvas c, RectF r, float frac, int color, int glass, float t) {
        float w = r.width(), h = r.height();
        float neckW = w * 0.38f, neckH = h * 0.12f;
        Path body = new Path();
        float top = r.top + neckH + h * 0.04f;
        body.addRoundRect(new RectF(r.left, top, r.right, r.bottom), w * 0.28f, w * 0.28f, Path.Direction.CW);
        // cam
        P.setShader(null);
        P.setColor(glass);
        c.drawPath(body, P);
        RectF cap = new RectF(r.centerX() - neckW / 2, r.top, r.centerX() + neckW / 2, r.top + neckH);
        P.setColor(mix(glass, 0xFFFFFFFF, 0.15f));
        c.drawRoundRect(cap, neckW * 0.25f, neckW * 0.25f, P);
        // su
        frac = Math.max(0, Math.min(1, frac));
        if (frac > 0) {
            c.save();
            c.clipPath(body);
            float level = r.bottom - (r.bottom - top) * frac;
            Path wave = new Path();
            wave.moveTo(r.left - 5, r.bottom + 5);
            wave.lineTo(r.left - 5, level);
            float amp = frac >= 1 ? 0 : h * 0.018f;
            for (float x = r.left - 5; x <= r.right + 5; x += 4) {
                wave.lineTo(x, level + (float) Math.sin((x - r.left) / w * 6.28f * 1.2f + t * 3) * amp);
            }
            wave.lineTo(r.right + 5, r.bottom + 5);
            wave.close();
            shader(new LinearGradient(0, level, 0, r.bottom, mix(color, 0xFFFFFFFF, 0.25f), color, Shader.TileMode.CLAMP));
            c.drawPath(wave, P);
            P.setShader(null);
            // kabarcıklar
            P.setColor(0x55FFFFFF);
            for (int i = 0; i < 5; i++) {
                float bx = r.left + w * (0.2f + 0.15f * i);
                float ph = (t * 0.4f + i * 0.23f) % 1f;
                float by = r.bottom - (r.bottom - level) * ph;
                if (by > level + 4) c.drawCircle(bx, by, w * 0.025f, P);
            }
            c.restore();
        }
        // parlama
        P.setColor(0x22FFFFFF);
        c.drawRoundRect(new RectF(r.left + w * 0.12f, top + h * 0.08f, r.left + w * 0.22f, r.bottom - h * 0.12f), w * 0.05f, w * 0.05f, P);
    }

    // ---------------- Yoldaş ----------------
    public static final String[] SPECIES = {"Penguen", "Kedi", "Tilki", "Ejder"};
    public static final String[] SPECIES_EMOJI = {"🐧", "🐱", "🦊", "🐲"};
    static final int[][] SPC = {
            {0xFF2B3550, 0xFFF3F4F8, 0xFFFFA63D},   // gövde, göbek, gaga
            {0xFF8A8FA8, 0xFFF1E9E4, 0xFFFF8FA3},
            {0xFFF2711C, 0xFFFFF4E8, 0xFF2B2B2B},
            {0xFF3CB371, 0xFFE2F7C7, 0xFFFFD166},
    };
    public static final int MOOD_HAPPY = 0, MOOD_OK = 1, MOOD_SLEEPY = 2, MOOD_MISS = 3, MOOD_EXCITED = 4;

    /** size = gövde yüksekliği. stage 0 yavru, 1 genç, 2 yetişkin (gözler/oranlar değişir). */
    public static void companion(Canvas c, float cx, float cy, float size, int sp, int mood, String hat, String acc, float t, int stage) {
        sp = Math.max(0, Math.min(3, sp));
        int[] col = SPC[sp];
        float bounce = (float) Math.sin(t * (mood == MOOD_EXCITED ? 7 : 2.4)) * size * (mood == MOOD_EXCITED ? 0.035f : 0.012f);
        float breathe = 1 + (float) Math.sin(t * 2.4) * 0.012f;
        float h = size * breathe, w = size * (stage == 0 ? 0.9f : 0.82f);
        cy += bounce;
        float top = cy - h / 2, bot = cy + h / 2;
        // gölge
        P.setShader(null);
        P.setColor(0x33000000);
        c.drawOval(new RectF(cx - w * 0.45f, bot - h * 0.03f, cx + w * 0.45f, bot + h * 0.07f), P);
        // kanat / kuyruk
        if (sp == 3) { // ejder kanatları
            P.setColor(mix(col[0], 0xFF000000, 0.15f));
            float fl = (float) Math.sin(t * 3) * w * 0.05f;
            Path wg = new Path();
            wg.moveTo(cx - w * 0.35f, cy - h * 0.05f); wg.lineTo(cx - w * 0.85f, cy - h * 0.35f - fl); wg.lineTo(cx - w * 0.7f, cy + h * 0.05f); wg.close();
            wg.moveTo(cx + w * 0.35f, cy - h * 0.05f); wg.lineTo(cx + w * 0.85f, cy - h * 0.35f - fl); wg.lineTo(cx + w * 0.7f, cy + h * 0.05f); wg.close();
            c.drawPath(wg, P);
        }
        if (sp == 2 || sp == 1) { // kuyruk
            P.setColor(col[0]);
            float sw = (float) Math.sin(t * 2) * w * 0.06f;
            c.drawOval(new RectF(cx + w * 0.3f, cy + h * 0.05f + sw, cx + w * 0.75f, cy + h * 0.35f + sw), P);
            if (sp == 2) { P.setColor(col[1]); c.drawCircle(cx + w * 0.7f, cy + h * 0.2f + sw, w * 0.09f, P); }
        }
        // kulaklar
        if (sp == 1 || sp == 2) {
            P.setColor(col[0]);
            Path e = new Path();
            float ew = w * 0.22f, eh = h * (sp == 2 ? 0.3f : 0.22f);
            e.moveTo(cx - w * 0.38f, top + h * 0.16f); e.lineTo(cx - w * 0.3f, top - eh * 0.55f); e.lineTo(cx - w * 0.12f, top + h * 0.06f); e.close();
            e.moveTo(cx + w * 0.38f, top + h * 0.16f); e.lineTo(cx + w * 0.3f, top - eh * 0.55f); e.lineTo(cx + w * 0.12f, top + h * 0.06f); e.close();
            c.drawPath(e, P);
            P.setColor(sp == 2 ? 0xFF2B2B2B : 0xFFFFB3C1);
            Path ei = new Path();
            ei.moveTo(cx - w * 0.33f, top + h * 0.12f); ei.lineTo(cx - w * 0.29f, top - eh * 0.3f); ei.lineTo(cx - w * 0.19f, top + h * 0.07f); ei.close();
            ei.moveTo(cx + w * 0.33f, top + h * 0.12f); ei.lineTo(cx + w * 0.29f, top - eh * 0.3f); ei.lineTo(cx + w * 0.19f, top + h * 0.07f); ei.close();
            c.drawPath(ei, P);
            if (ew < 0) return;
        }
        if (sp == 3) { // boynuzlar
            P.setColor(0xFFFFE9A8);
            Path hn = new Path();
            hn.moveTo(cx - w * 0.25f, top + h * 0.08f); hn.lineTo(cx - w * 0.3f, top - h * 0.12f); hn.lineTo(cx - w * 0.14f, top + h * 0.04f); hn.close();
            hn.moveTo(cx + w * 0.25f, top + h * 0.08f); hn.lineTo(cx + w * 0.3f, top - h * 0.12f); hn.lineTo(cx + w * 0.14f, top + h * 0.04f); hn.close();
            c.drawPath(hn, P);
        }
        // gövde
        RectF body = new RectF(cx - w / 2, top, cx + w / 2, bot);
        shader(new LinearGradient(0, top, 0, bot, mix(col[0], 0xFFFFFFFF, 0.12f), mix(col[0], 0xFF000000, 0.12f), Shader.TileMode.CLAMP));
        c.drawRoundRect(body, w * 0.48f, h * 0.5f, P);
        P.setShader(null);
        // penguen kanatları
        if (sp == 0) {
            P.setColor(mix(col[0], 0xFF000000, 0.2f));
            float fl = mood == MOOD_EXCITED ? (float) Math.sin(t * 10) * 12 : (float) Math.sin(t * 2) * 3;
            c.save(); c.rotate(20 + fl, cx - w * 0.48f, cy - h * 0.05f);
            c.drawOval(new RectF(cx - w * 0.58f, cy - h * 0.12f, cx - w * 0.38f, cy + h * 0.28f), P);
            c.restore();
            c.save(); c.rotate(-20 - fl, cx + w * 0.48f, cy - h * 0.05f);
            c.drawOval(new RectF(cx + w * 0.38f, cy - h * 0.12f, cx + w * 0.58f, cy + h * 0.28f), P);
            c.restore();
        }
        // göbek / yüz maskesi
        P.setColor(col[1]);
        if (sp == 0) c.drawOval(new RectF(cx - w * 0.34f, top + h * 0.2f, cx + w * 0.34f, bot - h * 0.04f), P);
        else if (sp == 2) {
            c.drawOval(new RectF(cx - w * 0.3f, cy + h * 0.02f, cx + w * 0.3f, bot - h * 0.04f), P);
            c.drawOval(new RectF(cx - w * 0.42f, top + h * 0.28f, cx - w * 0.02f, top + h * 0.55f), P);
            c.drawOval(new RectF(cx + w * 0.02f, top + h * 0.28f, cx + w * 0.42f, top + h * 0.55f), P);
        } else c.drawOval(new RectF(cx - w * 0.28f, cy + h * 0.02f, cx + w * 0.28f, bot - h * 0.05f), P);
        // gözler
        float ey = top + h * (stage == 0 ? 0.36f : 0.33f);
        float ex = w * 0.17f;
        float er = w * (stage == 0 ? 0.095f : 0.08f);
        boolean blink = (t % 4.2f) < 0.12f;
        P.setColor(0xFF1A1A22);
        S.setShader(null);
        S.setColor(0xFF1A1A22);
        S.setStrokeWidth(er * 0.45f);
        if (mood == MOOD_SLEEPY || blink) {
            c.drawLine(cx - ex - er, ey, cx - ex + er, ey, S);
            c.drawLine(cx + ex - er, ey, cx + ex + er, ey, S);
        } else if (mood == MOOD_HAPPY || mood == MOOD_EXCITED) {
            RF.set(cx - ex - er, ey - er, cx - ex + er, ey + er * 0.9f);
            c.drawArc(RF, 200, 140, false, S);
            RF.set(cx + ex - er, ey - er, cx + ex + er, ey + er * 0.9f);
            c.drawArc(RF, 200, 140, false, S);
        } else {
            c.drawCircle(cx - ex, ey, er, P);
            c.drawCircle(cx + ex, ey, er, P);
            P.setColor(0xFFFFFFFF);
            c.drawCircle(cx - ex + er * 0.35f, ey - er * 0.35f, er * 0.35f, P);
            c.drawCircle(cx + ex + er * 0.35f, ey - er * 0.35f, er * 0.35f, P);
            if (mood == MOOD_MISS) { // gözyaşı
                P.setColor(0xFF7FC8FF);
                float ty = ey + er * 1.4f + (t % 1.5f) * er * 2;
                c.drawCircle(cx + ex + er * 0.4f, ty, er * 0.35f, P);
            }
        }
        // yanaklar
        P.setColor(0x55FF6B8A);
        c.drawCircle(cx - ex - er * 1.3f, ey + er * 1.6f, er * 0.9f, P);
        c.drawCircle(cx + ex + er * 1.3f, ey + er * 1.6f, er * 0.9f, P);
        // ağız / gaga
        float my = ey + er * 2.2f;
        if (sp == 0) {
            P.setColor(col[2]);
            Path bk = new Path();
            bk.moveTo(cx - w * 0.08f, my - er * 0.5f); bk.lineTo(cx + w * 0.08f, my - er * 0.5f); bk.lineTo(cx, my + er * 0.8f); bk.close();
            c.drawPath(bk, P);
        } else {
            if (sp == 1 || sp == 2) { P.setColor(col[2]); c.drawOval(new RectF(cx - er * 0.6f, my - er * 0.9f, cx + er * 0.6f, my - er * 0.2f), P); }
            S.setColor(0xFF1A1A22);
            S.setStrokeWidth(er * 0.35f);
            RF.set(cx - er, my - er * 0.6f, cx + er, my + er * 0.8f);
            if (mood == MOOD_MISS) { RF.offset(0, er * 0.8f); c.drawArc(RF, 200, 140, false, S); }
            else c.drawArc(RF, 20, 140, false, S);
            if (sp == 1) { // bıyık
                S.setStrokeWidth(er * 0.15f);
                S.setColor(0x991A1A22);
                c.drawLine(cx - w * 0.2f, my - er * 0.2f, cx - w * 0.42f, my - er * 0.6f, S);
                c.drawLine(cx - w * 0.2f, my + er * 0.1f, cx - w * 0.42f, my + er * 0.2f, S);
                c.drawLine(cx + w * 0.2f, my - er * 0.2f, cx + w * 0.42f, my - er * 0.6f, S);
                c.drawLine(cx + w * 0.2f, my + er * 0.1f, cx + w * 0.42f, my + er * 0.2f, S);
            }
        }
        // ayaklar
        P.setColor(sp == 0 ? col[2] : mix(col[0], 0xFF000000, 0.2f));
        c.drawOval(new RectF(cx - w * 0.3f, bot - h * 0.06f, cx - w * 0.06f, bot + h * 0.03f), P);
        c.drawOval(new RectF(cx + w * 0.06f, bot - h * 0.06f, cx + w * 0.3f, bot + h * 0.03f), P);
        // aksesuar
        if (acc != null && !acc.isEmpty()) accessory(c, acc, cx, top, w, h, ey, er, my);
        if (hat != null && !hat.isEmpty()) hat(c, hat, cx, top, w, h, t);
        if (mood == MOOD_SLEEPY) {
            P.setColor(0xCCFFFFFF);
            P.setTextSize(h * 0.14f);
            P.setTypeface(Typeface.DEFAULT_BOLD);
            float zz = (t % 2f) / 2f;
            c.drawText("z", cx + w * 0.45f, top - h * 0.05f - zz * h * 0.15f, P);
            P.setTextSize(h * 0.1f);
            c.drawText("z", cx + w * 0.6f, top - h * 0.18f - zz * h * 0.12f, P);
        }
    }

    static void hat(Canvas c, String id, float cx, float top, float w, float h, float t) {
        P.setShader(null);
        switch (id) {
            case "h_beanie": {
                P.setColor(0xFFE84A5F);
                RF.set(cx - w * 0.36f, top - h * 0.12f, cx + w * 0.36f, top + h * 0.2f);
                c.drawArc(RF, 180, 180, true, P);
                P.setColor(0xFFFFFFFF);
                c.drawRoundRect(new RectF(cx - w * 0.4f, top + h * 0.02f, cx + w * 0.4f, top + h * 0.09f), 10, 10, P);
                c.drawCircle(cx, top - h * 0.13f, w * 0.07f, P);
                break;
            }
            case "h_party": {
                Path p = new Path();
                p.moveTo(cx - w * 0.2f, top + h * 0.05f); p.lineTo(cx + w * 0.2f, top + h * 0.05f); p.lineTo(cx + w * 0.02f, top - h * 0.32f); p.close();
                shader(new LinearGradient(cx - w * 0.2f, 0, cx + w * 0.2f, 0, 0xFF7C4DFF, 0xFFFF5FA2, Shader.TileMode.CLAMP));
                c.drawPath(p, P);
                P.setShader(null);
                P.setColor(0xFFFFD84D);
                c.drawCircle(cx + w * 0.02f, top - h * 0.32f, w * 0.06f, P);
                break;
            }
            case "h_flower": {
                int[] cols = {0xFFFFD84D, 0xFFFF7AA2, 0xFF7FD3FF, 0xFFFFA24D, 0xFFB57BFF};
                for (int i = 0; i < 5; i++) {
                    float x = cx - w * 0.3f + i * w * 0.15f;
                    float y = top + h * 0.03f - (float) Math.sin(i / 4f * Math.PI) * h * 0.06f;
                    P.setColor(cols[i]);
                    for (int k = 0; k < 5; k++) {
                        double a = k * Math.PI * 2 / 5;
                        c.drawCircle(x + (float) Math.cos(a) * w * 0.035f, y + (float) Math.sin(a) * w * 0.035f, w * 0.03f, P);
                    }
                    P.setColor(0xFFFFFFFF);
                    c.drawCircle(x, y, w * 0.022f, P);
                }
                break;
            }
            case "h_grad": {
                P.setColor(0xFF1F2233);
                c.drawRect(cx - w * 0.22f, top - h * 0.06f, cx + w * 0.22f, top + h * 0.06f, P);
                Path p = new Path();
                p.moveTo(cx - w * 0.48f, top - h * 0.06f); p.lineTo(cx, top - h * 0.2f); p.lineTo(cx + w * 0.48f, top - h * 0.06f); p.lineTo(cx, top + h * 0.06f); p.close();
                c.drawPath(p, P);
                S.setColor(0xFFFFC53D); S.setStrokeWidth(w * 0.025f);
                float sw = (float) Math.sin(t * 2) * w * 0.03f;
                c.drawLine(cx, top - h * 0.12f, cx + w * 0.38f + sw, top + h * 0.02f, S);
                c.drawLine(cx + w * 0.38f + sw, top + h * 0.02f, cx + w * 0.38f + sw, top + h * 0.14f, S);
                break;
            }
            case "h_crown": {
                Path p = new Path();
                float b = top + h * 0.06f, tp = top - h * 0.2f;
                p.moveTo(cx - w * 0.28f, b); p.lineTo(cx - w * 0.3f, tp); p.lineTo(cx - w * 0.15f, top - h * 0.06f); p.lineTo(cx, tp - h * 0.04f);
                p.lineTo(cx + w * 0.15f, top - h * 0.06f); p.lineTo(cx + w * 0.3f, tp); p.lineTo(cx + w * 0.28f, b); p.close();
                shader(new LinearGradient(0, tp, 0, b, 0xFFFFE680, 0xFFFFB300, Shader.TileMode.CLAMP));
                c.drawPath(p, P);
                P.setShader(null);
                P.setColor(0xFFE0245E); c.drawCircle(cx, top - h * 0.02f, w * 0.035f, P);
                P.setColor(0xFF2ECC71); c.drawCircle(cx - w * 0.17f, top, w * 0.028f, P);
                P.setColor(0xFF4FB3FF); c.drawCircle(cx + w * 0.17f, top, w * 0.028f, P);
                break;
            }
        }
    }

    static void accessory(Canvas c, String id, float cx, float top, float w, float h, float ey, float er, float my) {
        P.setShader(null);
        switch (id) {
            case "a_scarf": {
                P.setColor(0xFFE84A5F);
                float y = my + er * 1.6f;
                c.drawRoundRect(new RectF(cx - w * 0.42f, y, cx + w * 0.42f, y + h * 0.09f), h * 0.04f, h * 0.04f, P);
                c.drawRoundRect(new RectF(cx + w * 0.12f, y, cx + w * 0.26f, y + h * 0.25f), h * 0.03f, h * 0.03f, P);
                P.setColor(0xFFFFFFFF);
                for (int i = 0; i < 4; i++) c.drawRect(cx - w * 0.36f + i * w * 0.18f, y + h * 0.02f, cx - w * 0.3f + i * w * 0.18f, y + h * 0.07f, P);
                break;
            }
            case "a_bow": {
                P.setColor(0xFFFF4F8B);
                float y = my + er * 2f;
                Path p = new Path();
                p.moveTo(cx, y); p.lineTo(cx - w * 0.16f, y - h * 0.06f); p.lineTo(cx - w * 0.16f, y + h * 0.06f); p.close();
                p.moveTo(cx, y); p.lineTo(cx + w * 0.16f, y - h * 0.06f); p.lineTo(cx + w * 0.16f, y + h * 0.06f); p.close();
                c.drawPath(p, P);
                c.drawCircle(cx, y, w * 0.035f, P);
                break;
            }
            case "a_glasses": {
                S.setColor(0xFF1A1A22); S.setStrokeWidth(er * 0.35f);
                c.drawCircle(cx - w * 0.17f, ey, er * 1.7f, S);
                c.drawCircle(cx + w * 0.17f, ey, er * 1.7f, S);
                c.drawLine(cx - w * 0.17f + er * 1.7f, ey, cx + w * 0.17f - er * 1.7f, ey, S);
                P.setColor(0x2299DDFF);
                c.drawCircle(cx - w * 0.17f, ey, er * 1.6f, P);
                c.drawCircle(cx + w * 0.17f, ey, er * 1.6f, P);
                break;
            }
            case "a_headph": {
                S.setColor(0xFF2B2B3A); S.setStrokeWidth(w * 0.06f);
                RF.set(cx - w * 0.5f, top - h * 0.08f, cx + w * 0.5f, top + h * 0.7f);
                c.drawArc(RF, 190, 160, false, S);
                P.setColor(0xFFA78BFA);
                c.drawRoundRect(new RectF(cx - w * 0.58f, top + h * 0.22f, cx - w * 0.42f, top + h * 0.45f), w * 0.05f, w * 0.05f, P);
                c.drawRoundRect(new RectF(cx + w * 0.42f, top + h * 0.22f, cx + w * 0.58f, top + h * 0.45f), w * 0.05f, w * 0.05f, P);
                break;
            }
        }
    }

    // ---------------- Ağaç (odak ormanı / bitki) ----------------
    /** type 0..4, growth 0..1, withered = soldu. */
    public static void tree(Canvas c, float x, float by, float size, int type, float growth, boolean withered) {
        growth = Math.max(0.05f, Math.min(1, growth));
        float h = size * Math.max(growth, 0.55f);
        int trunk = withered ? 0xFF6B5B4B : 0xFF8B5A2B;
        int[] leaves = {0xFF34C759, 0xFF2EAD6B, 0xFF7ED957, 0xFFFF8FB8, 0xFFFFB84D};
        int leaf = withered ? 0xFF8A7F5A : leaves[Math.abs(type) % leaves.length];
        P.setShader(null);
        P.setColor(trunk);
        c.drawRoundRect(new RectF(x - h * 0.05f, by - h * 0.45f, x + h * 0.05f, by), h * 0.03f, h * 0.03f, P);
        P.setColor(leaf);
        if (growth < 0.3f) { // filiz
            c.drawOval(new RectF(x - h * 0.3f, by - h * 0.7f, x, by - h * 0.45f), P);
            c.drawOval(new RectF(x, by - h * 0.75f, x + h * 0.3f, by - h * 0.5f), P);
            return;
        }
        switch (Math.abs(type) % 5) {
            case 0: case 1: {
                Path p = new Path();
                for (int i = 0; i < 3; i++) {
                    float ty = by - h * (0.35f + i * 0.2f), ww = h * (0.42f - i * 0.1f);
                    p.reset(); p.moveTo(x - ww, ty); p.lineTo(x + ww, ty); p.lineTo(x, ty - h * 0.35f); p.close();
                    P.setColor(i == 1 ? mix(leaf, 0xFFFFFFFF, 0.1f) : leaf);
                    c.drawPath(p, P);
                }
                break;
            }
            default: {
                c.drawCircle(x, by - h * 0.62f, h * 0.3f, P);
                c.drawCircle(x - h * 0.2f, by - h * 0.5f, h * 0.2f, P);
                c.drawCircle(x + h * 0.2f, by - h * 0.5f, h * 0.2f, P);
                P.setColor(mix(leaf, 0xFFFFFFFF, 0.18f));
                c.drawCircle(x - h * 0.08f, by - h * 0.72f, h * 0.1f, P);
                if (Math.abs(type) % 5 >= 3 && !withered) {
                    P.setColor(0xFFFFFFFF);
                    c.drawCircle(x + h * 0.12f, by - h * 0.6f, h * 0.035f, P);
                    c.drawCircle(x - h * 0.15f, by - h * 0.45f, h * 0.035f, P);
                }
            }
        }
    }

    // ---------------- Ada ----------------
    public static void island(Canvas c, RectF area, List<Integer> items, float t, boolean night, long seed) {
        float w = area.width(), hh = area.height();
        // gök
        shader(new LinearGradient(0, area.top, 0, area.bottom, night ? 0xFF141A3A : 0xFF7EC8F5, night ? 0xFF2A2F5C : 0xFFBEE6FF, Shader.TileMode.CLAMP));
        c.drawRect(area, P);
        P.setShader(null);
        if (night) {
            Random r = new Random(5);
            P.setColor(0xCCFFFFFF);
            for (int i = 0; i < 30; i++) c.drawCircle(area.left + r.nextFloat() * w, area.top + r.nextFloat() * hh * 0.5f, 1.5f + r.nextFloat() * 1.5f * (float) (0.6 + 0.4 * Math.sin(t * 2 + i)), P);
            P.setColor(0xFFFFF4C2);
            c.drawCircle(area.left + w * 0.82f, area.top + hh * 0.16f, hh * 0.07f, P);
        } else {
            P.setColor(0xFFFFE27A);
            c.drawCircle(area.left + w * 0.84f, area.top + hh * 0.15f, hh * 0.07f, P);
            P.setColor(0xDDFFFFFF);
            float cxo = (t * 8) % (w + 200) - 100;
            cloud(c, area.left + cxo, area.top + hh * 0.14f, hh * 0.05f);
            cloud(c, area.left + (cxo + w * 0.55f) % (w + 200) - 100, area.top + hh * 0.26f, hh * 0.04f);
        }
        // deniz
        float seaTop = area.top + hh * 0.55f;
        shader(new LinearGradient(0, seaTop, 0, area.bottom, night ? 0xFF1B3A6B : 0xFF2E9BD6, night ? 0xFF0E1F3F : 0xFF1B6FB0, Shader.TileMode.CLAMP));
        c.drawRect(area.left, seaTop, area.right, area.bottom, P);
        P.setShader(null);
        S.setColor(0x33FFFFFF); S.setStrokeWidth(2);
        for (int i = 0; i < 6; i++) {
            float y = seaTop + (hh * 0.45f) * (i + 1) / 7f;
            float off = (float) Math.sin(t + i) * 12;
            c.drawLine(area.left + w * 0.05f + off + i * 17 % 60, y, area.left + w * 0.2f + off + i * 17 % 60, y, S);
            c.drawLine(area.right - w * 0.25f - off, y + 6, area.right - w * 0.1f - off, y + 6, S);
        }
        int n = items == null ? 0 : items.size();
        float grow = Math.min(1f, 0.45f + (float) Math.sqrt(n) * 0.06f);
        float iw = w * 0.82f * grow, ih = hh * 0.2f * (0.8f + grow * 0.2f);
        float icx = area.centerX(), icy = seaTop + hh * 0.05f;
        // kum ve çimen
        P.setColor(night ? 0xFFB39B6B : 0xFFF2D59B);
        c.drawOval(new RectF(icx - iw / 2 - 10, icy - ih / 2, icx + iw / 2 + 10, icy + ih / 2 + 12), P);
        shader(new LinearGradient(0, icy - ih / 2, 0, icy + ih / 2, night ? 0xFF3B7A4A : 0xFF6BD36B, night ? 0xFF2C5E39 : 0xFF3FAF5A, Shader.TileMode.CLAMP));
        c.drawOval(new RectF(icx - iw / 2, icy - ih / 2 - 4, icx + iw / 2, icy + ih / 2 - 4), P);
        P.setShader(null);
        if (n == 0) return;
        // yapıları yerleştir: arkadan öne
        Random r = new Random(seed * 31 + 7);
        float[][] pos = new float[n][];
        for (int i = 0; i < n; i++) {
            double a = r.nextDouble() * Math.PI * 2;
            double rr = Math.sqrt(r.nextDouble()) * 0.9;
            float px = icx + (float) (Math.cos(a) * rr) * iw / 2 * 0.85f;
            float py = icy - 4 + (float) (Math.sin(a) * rr) * ih / 2 * 0.75f;
            pos[i] = new float[]{px, py, i};
        }
        java.util.Arrays.sort(pos, (x, y) -> Float.compare(x[1], y[1]));
        float unit = Math.max(10, hh * 0.12f * (1.1f - grow * 0.3f));
        for (float[] ps : pos) {
            int code = items.get((int) ps[2]);
            islandItem(c, code, ps[0], ps[1], unit, t, area, seaTop, night);
        }
    }

    static void cloud(Canvas c, float x, float y, float r) {
        c.drawCircle(x, y, r, P);
        c.drawCircle(x + r, y - r * 0.4f, r * 1.2f, P);
        c.drawCircle(x + r * 2.1f, y, r * 0.9f, P);
        c.drawRect(x, y - r * 0.2f, x + r * 2.1f, y + r * 0.9f, P);
    }

    static void islandItem(Canvas c, int code, float x, float y, float u, float t, RectF area, float seaTop, boolean night) {
        P.setShader(null);
        switch (code) {
            case Game.I_PINE: tree(c, x, y, u * 1.4f, 0, 1, false); break;
            case Game.I_TREE: tree(c, x, y, u * 1.3f, 2, 1, false); break;
            case Game.I_BUSH: P.setColor(0xFF2E9E52); c.drawCircle(x - u * 0.2f, y - u * 0.15f, u * 0.25f, P); c.drawCircle(x + u * 0.15f, y - u * 0.18f, u * 0.3f, P); break;
            case Game.I_FLOWER: {
                int[] cols = {0xFFFF7AA2, 0xFFFFD84D, 0xFFB57BFF};
                for (int k = 0; k < 3; k++) {
                    float fx = x + (k - 1) * u * 0.25f;
                    S.setColor(0xFF2E9E52); S.setStrokeWidth(u * 0.05f); c.drawLine(fx, y, fx, y - u * 0.4f, S);
                    P.setColor(cols[k]); c.drawCircle(fx, y - u * 0.45f, u * 0.12f, P);
                    P.setColor(0xFFFFFFFF); c.drawCircle(fx, y - u * 0.45f, u * 0.05f, P);
                }
                break;
            }
            case Game.I_ROCK: P.setColor(0xFF8E96A6); c.drawOval(new RectF(x - u * 0.3f, y - u * 0.3f, x + u * 0.3f, y + u * 0.02f), P); break;
            case Game.I_HOUSE: case Game.I_BIGHOUSE: {
                float s = code == Game.I_BIGHOUSE ? 1.5f : 1f;
                float hw = u * 0.45f * s, hh = u * 0.55f * s;
                P.setColor(0xFFFFF1DC); c.drawRect(x - hw, y - hh, x + hw, y, P);
                P.setColor(0xFFE0584B);
                Path p = new Path(); p.moveTo(x - hw * 1.2f, y - hh); p.lineTo(x, y - hh * 1.9f); p.lineTo(x + hw * 1.2f, y - hh); p.close(); c.drawPath(p, P);
                P.setColor(0xFF8B5A2B); c.drawRect(x - hw * 0.25f, y - hh * 0.55f, x + hw * 0.25f, y, P);
                P.setColor(night ? 0xFFFFD86B : 0xFF7EC8F5); c.drawRect(x + hw * 0.45f, y - hh * 0.75f, x + hw * 0.8f, y - hh * 0.45f, P);
                break;
            }
            case Game.I_BOAT: {
                float bx = area.left + area.width() * 0.15f + (float) Math.sin(t * 0.3) * 20, by = seaTop + area.height() * 0.3f + (float) Math.sin(t * 1.5) * 3;
                P.setColor(0xFF8B5A2B);
                Path p = new Path(); p.moveTo(bx - u * 0.6f, by); p.lineTo(bx + u * 0.6f, by); p.lineTo(bx + u * 0.4f, by + u * 0.3f); p.lineTo(bx - u * 0.4f, by + u * 0.3f); p.close(); c.drawPath(p, P);
                P.setColor(0xFFFFFFFF);
                p.reset(); p.moveTo(bx, by - u); p.lineTo(bx, by - u * 0.05f); p.lineTo(bx + u * 0.5f, by - u * 0.1f); p.close(); c.drawPath(p, P);
                break;
            }
            case Game.I_BRIDGE: {
                S.setColor(0xFF8B5A2B); S.setStrokeWidth(u * 0.12f);
                float bx = area.right - area.width() * 0.2f;
                RectF rr = new RectF(bx - u, seaTop - u * 0.2f, bx + u, seaTop + u * 0.6f);
                c.drawArc(rr, 200, 140, false, S);
                break;
            }
            case Game.I_BALLOON: {
                float bx = area.left + area.width() * 0.25f, by = area.top + area.height() * 0.2f + (float) Math.sin(t * 0.8) * 8;
                P.setColor(0xFFFF5F5F); c.drawCircle(bx, by, u * 0.5f, P);
                P.setColor(0xFFFFD84D); c.drawRect(bx - u * 0.08f, by - u * 0.5f, bx + u * 0.08f, by + u * 0.48f, P);
                S.setColor(0xFF8B5A2B); S.setStrokeWidth(1.5f);
                c.drawLine(bx - u * 0.3f, by + u * 0.35f, bx - u * 0.12f, by + u * 0.8f, S);
                c.drawLine(bx + u * 0.3f, by + u * 0.35f, bx + u * 0.12f, by + u * 0.8f, S);
                P.setColor(0xFF8B5A2B); c.drawRect(bx - u * 0.14f, by + u * 0.8f, bx + u * 0.14f, by + u, P);
                break;
            }
            case Game.I_WINDMILL: {
                P.setColor(0xFFF5F0E6);
                Path p = new Path(); p.moveTo(x - u * 0.25f, y); p.lineTo(x - u * 0.15f, y - u * 1.1f); p.lineTo(x + u * 0.15f, y - u * 1.1f); p.lineTo(x + u * 0.25f, y); p.close(); c.drawPath(p, P);
                S.setColor(0xFF8B5A2B); S.setStrokeWidth(u * 0.08f);
                for (int k = 0; k < 4; k++) {
                    double a = t * 1.5 + k * Math.PI / 2;
                    c.drawLine(x, y - u * 1.1f, x + (float) Math.cos(a) * u * 0.7f, y - u * 1.1f + (float) Math.sin(a) * u * 0.7f, S);
                }
                break;
            }
            case Game.I_LIGHTHOUSE: {
                float lx = area.right - area.width() * 0.12f, ly = seaTop + 6;
                P.setColor(0xFFFFFFFF); c.drawRect(lx - u * 0.18f, ly - u * 1.6f, lx + u * 0.18f, ly, P);
                P.setColor(0xFFE0584B);
                c.drawRect(lx - u * 0.18f, ly - u * 1.2f, lx + u * 0.18f, ly - u * 0.9f, P);
                c.drawRect(lx - u * 0.18f, ly - u * 0.6f, lx + u * 0.18f, ly - u * 0.3f, P);
                P.setColor(0xFFFFE27A); c.drawCircle(lx, ly - u * 1.75f, u * 0.16f, P);
                if (night) {
                    P.setColor(0x33FFE27A);
                    double a = t * 1.2;
                    Path bm = new Path(); bm.moveTo(lx, ly - u * 1.75f);
                    bm.lineTo(lx + (float) Math.cos(a) * u * 5, ly - u * 1.75f + (float) Math.sin(a) * u);
                    bm.lineTo(lx + (float) Math.cos(a + 0.2) * u * 5, ly - u * 1.75f + (float) Math.sin(a + 0.2) * u);
                    bm.close(); c.drawPath(bm, P);
                }
                break;
            }
            case Game.I_RAINBOW: {
                int[] rc = {0x88FF5F5F, 0x88FFA24D, 0x88FFE14D, 0x8866D96B, 0x884FB3FF, 0x88B57BFF};
                for (int k = 0; k < rc.length; k++) {
                    S.setColor(rc[k]); S.setStrokeWidth(u * 0.1f);
                    float rr = area.width() * 0.35f - k * u * 0.1f;
                    c.drawArc(new RectF(area.centerX() - rr, seaTop - rr * 0.6f, area.centerX() + rr, seaTop + rr * 1.1f), 200, 140, false, S);
                }
                break;
            }
            case Game.I_CASTLE: {
                float s = 1.4f;
                P.setColor(0xFFB8BCC8);
                c.drawRect(x - u * 0.6f * s, y - u * 0.8f * s, x + u * 0.6f * s, y, P);
                c.drawRect(x - u * 0.75f * s, y - u * 1.2f * s, x - u * 0.45f * s, y, P);
                c.drawRect(x + u * 0.45f * s, y - u * 1.2f * s, x + u * 0.75f * s, y, P);
                P.setColor(0xFF4F7BD9);
                Path p = new Path();
                p.moveTo(x - u * 0.8f * s, y - u * 1.2f * s); p.lineTo(x - u * 0.6f * s, y - u * 1.6f * s); p.lineTo(x - u * 0.4f * s, y - u * 1.2f * s); p.close();
                p.moveTo(x + u * 0.4f * s, y - u * 1.2f * s); p.lineTo(x + u * 0.6f * s, y - u * 1.6f * s); p.lineTo(x + u * 0.8f * s, y - u * 1.2f * s); p.close();
                c.drawPath(p, P);
                P.setColor(0xFF5A4636); c.drawRect(x - u * 0.15f * s, y - u * 0.4f * s, x + u * 0.15f * s, y, P);
                break;
            }
            case Game.I_GOLDTREE: {
                P.setColor(0xFF8B5A2B); c.drawRect(x - u * 0.07f, y - u * 0.6f, x + u * 0.07f, y, P);
                shader(new RadialGradient(x, y - u * 0.9f, u * 0.6f, 0xFFFFF1A0, 0xFFFFB300, Shader.TileMode.CLAMP));
                c.drawCircle(x, y - u * 0.9f, u * 0.5f, P);
                P.setShader(null);
                star(c, x + u * 0.3f, y - u * 1.3f, u * 0.12f * (1 + 0.3f * (float) Math.sin(t * 3)), 0xFFFFFFFF);
                break;
            }
            case Game.I_FOUNTAIN: {
                P.setColor(0xFFCBD2DC); c.drawOval(new RectF(x - u * 0.5f, y - u * 0.25f, x + u * 0.5f, y + u * 0.05f), P);
                P.setColor(0xFF7FC8FF);
                for (int k = 0; k < 3; k++) {
                    float ph = (t * 1.5f + k * 0.33f) % 1f;
                    c.drawCircle(x + (k - 1) * u * 0.15f * ph * 2, y - u * 0.2f - (float) Math.sin(ph * Math.PI) * u * 0.6f, u * 0.05f, P);
                }
                break;
            }
            case Game.I_STATUE: {
                P.setColor(0xFFB0B7C3);
                c.drawRect(x - u * 0.25f, y - u * 0.3f, x + u * 0.25f, y, P);
                c.drawOval(new RectF(x - u * 0.18f, y - u * 1.0f, x + u * 0.18f, y - u * 0.3f), P);
                c.drawCircle(x, y - u * 1.1f, u * 0.14f, P);
                break;
            }
        }
    }

    // ---------------- Madalya ----------------
    public static void medal(Canvas c, float cx, float cy, float r, int tierColor, String emoji, boolean locked, float shine) {
        P.setShader(null);
        if (!locked) {
            P.setColor(alpha(tierColor, 0.25f));
            c.drawCircle(cx, cy, r * 1.12f, P);
        }
        int base = locked ? 0xFF2A2A38 : tierColor;
        shader(new LinearGradient(cx - r, cy - r, cx + r, cy + r, mix(base, 0xFFFFFFFF, locked ? 0.05f : 0.35f), mix(base, 0xFF000000, 0.25f), Shader.TileMode.CLAMP));
        c.drawCircle(cx, cy, r, P);
        P.setShader(null);
        P.setColor(locked ? 0xFF1C1C28 : mix(base, 0xFF000000, 0.4f));
        c.drawCircle(cx, cy, r * 0.78f, P);
        P.setTextAlign(Paint.Align.CENTER);
        P.setTextSize(r * 0.9f);
        P.setColor(0xFFFFFFFF);
        P.setAlpha(locked ? 70 : 255);
        c.drawText(locked ? "🔒" : emoji, cx, cy + r * 0.32f, P);
        P.setAlpha(255);
        P.setTextAlign(Paint.Align.LEFT);
        if (!locked && shine > 0) {
            P.setColor(alpha(0xFFFFFFFF, 0.35f * shine));
            c.save();
            Path clip = new Path(); clip.addCircle(cx, cy, r, Path.Direction.CW);
            c.clipPath(clip);
            float sx = cx - r * 2 + shine * r * 4;
            Path band = new Path(); band.moveTo(sx, cy - r); band.lineTo(sx + r * 0.4f, cy - r); band.lineTo(sx - r * 0.2f, cy + r); band.lineTo(sx - r * 0.6f, cy + r); band.close();
            c.drawPath(band, P);
            c.restore();
        }
    }

    // ---------------- Sandık ----------------
    public static void chest(Canvas c, float cx, float cy, float s, int rarity, float open, float t) {
        int col = Game.RARITY_COLOR[Math.max(0, Math.min(3, rarity))];
        float w = s, h = s * 0.62f;
        float shake = open <= 0 ? (float) Math.sin(t * 30) * s * 0.012f * (float) Math.max(0, Math.sin(t * 2)) : 0;
        cx += shake;
        if (open > 0) {
            shader(new RadialGradient(cx, cy - h * 0.2f, s * 1.1f * open, alpha(col, 0.7f), alpha(col, 0f), Shader.TileMode.CLAMP));
            c.drawCircle(cx, cy - h * 0.2f, s * 1.1f * open, P);
            P.setShader(null);
            for (int i = 0; i < 10; i++) {
                double a = -Math.PI / 2 + (i - 4.5) * 0.28;
                float d = s * (0.3f + open * 0.8f);
                star(c, cx + (float) Math.cos(a) * d, cy - h * 0.2f + (float) Math.sin(a) * d, s * 0.04f * (1 + (float) Math.sin(t * 5 + i)), 0xFFFFFFFF);
            }
        }
        RectF box = new RectF(cx - w / 2, cy - h * 0.1f, cx + w / 2, cy + h * 0.6f);
        shader(new LinearGradient(0, box.top, 0, box.bottom, 0xFF9C5B2E, 0xFF6B3A1C, Shader.TileMode.CLAMP));
        c.drawRoundRect(box, s * 0.06f, s * 0.06f, P);
        P.setShader(null);
        P.setColor(col);
        c.drawRect(box.left, box.top, box.left + w * 0.1f, box.bottom, P);
        c.drawRect(box.right - w * 0.1f, box.top, box.right, box.bottom, P);
        c.save();
        c.rotate(-open * 35, box.left, box.top);
        RectF lid = new RectF(cx - w / 2, cy - h * 0.45f, cx + w / 2, cy - h * 0.05f);
        shader(new LinearGradient(0, lid.top, 0, lid.bottom, 0xFFB86B36, 0xFF8C4F24, Shader.TileMode.CLAMP));
        c.drawRoundRect(lid, s * 0.12f, s * 0.12f, P);
        P.setShader(null);
        P.setColor(col);
        c.drawRect(lid.left, lid.top + s * 0.02f, lid.left + w * 0.1f, lid.bottom, P);
        c.drawRect(lid.right - w * 0.1f, lid.top + s * 0.02f, lid.right, lid.bottom, P);
        P.setColor(0xFFFFD84D);
        c.drawRoundRect(new RectF(cx - w * 0.08f, lid.bottom - h * 0.1f, cx + w * 0.08f, lid.bottom + h * 0.14f), s * 0.02f, s * 0.02f, P);
        c.restore();
    }
}
