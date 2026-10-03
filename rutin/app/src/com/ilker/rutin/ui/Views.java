package com.ilker.rutin.ui;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ilker.rutin.core.Tm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Özel çizim görünümleri. */
public final class Views {
    private Views() {}

    /** Sürekli animasyonlu görünüm tabanı. */
    public abstract static class Anim extends View {
        final long start = SystemClock.uptimeMillis();
        public boolean animating = true;
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        Anim(Context c) { super(c); }

        float t() { return (SystemClock.uptimeMillis() - start) / 1000f; }

        @Override
        protected void onDraw(Canvas c) {
            draw(c, t());
            if (animating && isShown()) postInvalidateOnAnimation();
        }

        abstract void draw(Canvas c, float t);
    }

    // ---------------- Alev ----------------
    public static class FlameView extends Anim {
        public int stage; public boolean lit = true;
        public FlameView(Context c) { super(c); }
        public void set(int stage, boolean lit) { this.stage = stage; this.lit = lit; invalidate(); }
        @Override void draw(Canvas c, float t) {
            float h = Math.min(getHeight() * 0.92f, getWidth() * 1.1f);
            Art.flame(c, getWidth() / 2f, getHeight() * 0.96f, h, stage, lit, t);
        }
    }

    // ---------------- Üçlü halka ----------------
    public static class RingsView extends View {
        final float[] val = new float[3], shown = new float[3];
        final int[] col = new int[3];
        int n = 0;
        public String center = "", centerSub = "";
        final Paint tp = new Paint(Paint.ANTI_ALIAS_FLAG);
        public RingsView(Context c) { super(c); }

        public void set(float[] values, int[] colors, String center, String sub) {
            n = values.length;
            for (int i = 0; i < n; i++) { val[i] = values[i]; col[i] = colors[i]; }
            this.center = center; this.centerSub = sub;
            ValueAnimator a = ValueAnimator.ofFloat(0, 1);
            final float[] from = shown.clone();
            a.setDuration(900);
            a.setInterpolator(new DecelerateInterpolator(2f));
            a.addUpdateListener(an -> {
                float f = (float) an.getAnimatedValue();
                for (int i = 0; i < n; i++) shown[i] = from[i] + (val[i] - from[i]) * f;
                invalidate();
            });
            a.start();
        }

        @Override
        protected void onDraw(Canvas c) {
            float s = Math.min(getWidth(), getHeight());
            float cx = getWidth() / 2f, cy = getHeight() / 2f;
            float stroke = s * 0.085f;
            for (int i = 0; i < n; i++) {
                float r = s / 2 - stroke / 2 - i * (stroke * 1.25f);
                Art.ring(c, cx, cy, r, stroke, shown[i], col[i], Art.alpha(col[i], 0.16f));
            }
            tp.setTextAlign(Paint.Align.CENTER);
            tp.setColor(Th.text);
            tp.setTypeface(U.font(900));
            tp.setTextSize(s * 0.15f);
            c.drawText(center, cx, cy + s * 0.04f, tp);
            tp.setTypeface(U.font(700));
            tp.setTextSize(s * 0.075f);
            tp.setColor(Th.dim);
            c.drawText(centerSub, cx, cy + s * 0.14f, tp);
        }
    }

    // ---------------- Dalgalı ilerleme çubuğu ----------------
    public static class WaveBar extends Anim {
        public float frac, target; public int color; public float mini = -1;
        public WaveBar(Context c, int color) { super(c); this.color = color; }
        long animUntil;
        public void set(float f, boolean animate) {
            target = Math.max(0, Math.min(1, f));
            if (!animate) frac = target;
            animUntil = SystemClock.uptimeMillis() + 2500;
            if (animating) invalidate(); else { animating = true; invalidate(); animating = false; }
        }
        @Override void draw(Canvas c, float t) {
            if (SystemClock.uptimeMillis() > animUntil && Math.abs(target - frac) < 0.002f) animating = false;
            else if (SystemClock.uptimeMillis() <= animUntil) animating = true;
            frac += (target - frac) * 0.15f;
            float w = getWidth(), h = getHeight(), r = h / 2;
            p.setShader(null);
            p.setColor(Art.alpha(color, 0.16f));
            c.drawRoundRect(new RectF(0, 0, w, h), r, r, p);
            if (frac > 0.002f) {
                c.save();
                Path clip = new Path(); clip.addRoundRect(new RectF(0, 0, w, h), r, r, Path.Direction.CW);
                c.clipPath(clip);
                float fw = w * frac;
                Path wave = new Path();
                wave.moveTo(0, 0); wave.lineTo(fw, 0);
                for (float y = 0; y <= h; y += 2) wave.lineTo(fw + (float) Math.sin(y / h * 6.28f + t * 4) * (frac < 1 ? h * 0.12f : 0), y);
                wave.lineTo(0, h); wave.close();
                p.setColor(0xFFFFFFFF); p.setShader(new LinearGradient(0, 0, fw, 0, Art.mix(color, 0xFFFFFFFF, 0.1f), color, Shader.TileMode.CLAMP));
                c.drawPath(wave, p);
                p.setShader(null);
                p.setColor(0x33FFFFFF);
                c.drawRect(0, h * 0.18f, fw, h * 0.34f, p);
                c.restore();
            }
            if (mini > 0 && mini < 1) {
                p.setColor(Art.alpha(Th.text, 0.5f));
                float x = w * mini;
                c.drawRect(x - 1.5f, h * 0.15f, x + 1.5f, h * 0.85f, p);
            }
        }
    }

    // ---------------- Onay kutusu ----------------
    public static class CheckView extends View {
        public float prog;   // 0 boş, 1 dolu
        public int color = 0xFF3DDC84;
        public boolean mini;
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        public CheckView(Context c) { super(c); }
        public void set(boolean done, boolean mini, boolean animate) {
            this.mini = mini;
            float to = done ? 1 : 0;
            if (!animate) { prog = to; invalidate(); return; }
            ValueAnimator a = ValueAnimator.ofFloat(prog, to);
            a.setDuration(420);
            a.setInterpolator(new android.view.animation.OvershootInterpolator(2f));
            a.addUpdateListener(an -> { prog = (float) an.getAnimatedValue(); invalidate(); });
            a.start();
        }
        @Override
        protected void onDraw(Canvas c) {
            float s = Math.min(getWidth(), getHeight());
            float cx = getWidth() / 2f, cy = getHeight() / 2f, r = s * 0.42f;
            int col = mini ? Th.warn : color;
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(s * 0.07f);
            p.setColor(Art.mix(Art.alpha(Th.text, 0.25f), col, prog));
            c.drawCircle(cx, cy, r, p);
            if (prog > 0) {
                p.setStyle(Paint.Style.FILL);
                p.setColor(col);
                c.drawCircle(cx, cy, r * Math.min(1.1f, prog), p);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeCap(Paint.Cap.ROUND);
                p.setStrokeJoin(Paint.Join.ROUND);
                p.setStrokeWidth(s * 0.09f);
                p.setColor(0xFFFFFFFF);
                Path ck = new Path();
                float k = Math.min(1, prog);
                ck.moveTo(cx - r * 0.45f, cy + r * 0.02f);
                float mx = cx - r * 0.12f, my = cy + r * 0.34f;
                if (k < 0.5f) ck.lineTo(cx - r * 0.45f + (mx - (cx - r * 0.45f)) * k * 2, cy + r * 0.02f + (my - cy - r * 0.02f) * k * 2);
                else { ck.lineTo(mx, my); float k2 = (k - 0.5f) * 2; ck.lineTo(mx + (r * 0.58f) * k2, my - (r * 0.66f) * k2); }
                c.drawPath(ck, p);
                p.setStyle(Paint.Style.FILL);
            }
        }
    }

    // ---------------- Yoldaş ----------------
    public static class CompanionView extends Anim {
        public int species, mood, stage = 1; public String hat = "", acc = "";
        public CompanionView(Context c) { super(c); }
        @Override void draw(Canvas c, float t) {
            float s = Math.min(getWidth(), getHeight() * 0.95f) * 0.62f;
            Art.companion(c, getWidth() / 2f, getHeight() * 0.58f, s, species, mood, hat, acc, t, stage);
        }
    }

    // ---------------- Ada ----------------
    public static class IslandView extends Anim {
        public List<Integer> items = new ArrayList<>(); public boolean night; public long seed = 1;
        public float radius = U.dp(24);
        public IslandView(Context c) { super(c); }
        @Override void draw(Canvas c, float t) {
            c.save();
            Path clip = new Path();
            clip.addRoundRect(new RectF(0, 0, getWidth(), getHeight()), radius, radius, Path.Direction.CW);
            c.clipPath(clip);
            Art.island(c, new RectF(0, 0, getWidth(), getHeight()), items, t, night, seed);
            c.restore();
        }
    }

    // ---------------- Isı haritası ----------------
    public static class HeatmapView extends View {
        public double[] data = new double[0];   // eskiden yeniye
        public int endWeekday;                   // son günün haftanın günü (0=Pzt)
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        public HeatmapView(Context c) { super(c); }
        @Override
        protected void onMeasure(int wm, int hm) {
            int w = MeasureSpec.getSize(wm);
            int weeks = (data.length + 6) / 7 + 1;
            float cell = Math.min(U.dp(18), (w - U.dp(20)) / (float) weeks);
            setMeasuredDimension(w, (int) (cell * 7 + U.dp(4)));
        }
        @Override
        protected void onDraw(Canvas c) {
            int n = data.length;
            if (n == 0) return;
            int weeks = (n + endWeekday + 7) / 7 + 1;
            float cell = Math.min(U.dp(18), (getWidth() - U.dp(20)) / (float) weeks);
            float gap = cell * 0.18f;
            float x0 = getWidth() - weeks * cell;
            p.setTypeface(U.font(700));
            p.setTextSize(cell * 0.55f);
            p.setColor(Th.dim);
            for (int d = 0; d < 7; d += 2) c.drawText(Tm.DAY_LETTER[d], x0 - cell * 0.9f, d * cell + cell * 0.72f, p);
            for (int i = 0; i < n; i++) {
                int fromEnd = n - 1 - i;
                int wd = Math.floorMod(endWeekday - fromEnd, 7);
                int col = weeks - 1 - (fromEnd + (6 - endWeekday)) / 7;
                float x = x0 + col * cell, y = wd * cell;
                double v = data[i];
                int color;
                if (v == -1) color = Th.faint;
                else if (v == -2) color = 0xFF5AB8FF;
                else if (v < 0.01) color = Art.alpha(Th.crit, 0.35f);
                else color = Art.mix(Art.alpha(Th.success, 0.25f), Th.success, (float) v);
                p.setColor(color);
                c.drawRoundRect(new RectF(x + gap / 2, y + gap / 2, x + cell - gap / 2, y + cell - gap / 2), cell * 0.22f, cell * 0.22f, p);
                if (i == n - 1) {
                    p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(U.dp(1.5f)); p.setColor(Th.text);
                    c.drawRoundRect(new RectF(x + gap / 2, y + gap / 2, x + cell - gap / 2, y + cell - gap / 2), cell * 0.22f, cell * 0.22f, p);
                    p.setStyle(Paint.Style.FILL);
                }
            }
        }
    }

    // ---------------- Çubuk grafik ----------------
    public static class BarsView extends View {
        public double[] vals = new double[0]; public String[] labels = new String[0]; public int color = 0xFF3DDC84;
        public int highlight = -1; public boolean pct = true;
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        float anim = 0;
        public BarsView(Context c) { super(c); }
        public void set(double[] v, String[] l, int color) {
            vals = v; labels = l; this.color = color;
            ValueAnimator a = ValueAnimator.ofFloat(0, 1);
            a.setDuration(800); a.setInterpolator(new DecelerateInterpolator());
            a.addUpdateListener(an -> { anim = (float) an.getAnimatedValue(); invalidate(); });
            a.start();
        }
        @Override
        protected void onDraw(Canvas c) {
            int n = vals.length;
            if (n == 0) return;
            float w = getWidth(), h = getHeight();
            float lab = U.dp(18);
            float bw = w / n;
            double mx = 0.0001;
            for (double v : vals) mx = Math.max(mx, v);
            if (pct) mx = Math.max(mx, 1);
            p.setTextAlign(Paint.Align.CENTER);
            p.setTypeface(U.font(800));
            for (int i = 0; i < n; i++) {
                float x = i * bw + bw * 0.2f, x2 = (i + 1) * bw - bw * 0.2f;
                float bh = (float) ((h - lab - U.dp(16)) * Math.max(0, vals[i]) / mx) * anim;
                p.setColor(Th.faint);
                c.drawRoundRect(new RectF(x, U.dp(12), x2, h - lab), U.dp(6), U.dp(6), p);
                p.setColor(i == highlight ? Th.primary : color);
                if (vals[i] >= 0) c.drawRoundRect(new RectF(x, h - lab - bh, x2, h - lab), U.dp(6), U.dp(6), p);
                p.setColor(i == highlight ? Th.text : Th.dim);
                p.setTextSize(U.dp(11));
                c.drawText(labels.length > i ? labels[i] : "", (x + x2) / 2, h - U.dp(3), p);
            }
        }
    }

    // ---------------- Radar ----------------
    public static class RadarView extends View {
        public double[] vals = new double[6]; public String[] labels = new String[6]; public int[] colors = new int[6];
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        public RadarView(Context c) { super(c); }
        @Override
        protected void onDraw(Canvas c) {
            float cx = getWidth() / 2f, cy = getHeight() / 2f, r = Math.min(cx, cy) * 0.68f;
            int n = vals.length;
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(U.dp(1));
            p.setColor(Th.stroke);
            for (int k = 1; k <= 4; k++) {
                Path g = new Path();
                for (int i = 0; i < n; i++) {
                    double a = -Math.PI / 2 + i * 2 * Math.PI / n;
                    float x = cx + (float) Math.cos(a) * r * k / 4, y = cy + (float) Math.sin(a) * r * k / 4;
                    if (i == 0) g.moveTo(x, y); else g.lineTo(x, y);
                }
                g.close();
                c.drawPath(g, p);
            }
            Path d = new Path();
            for (int i = 0; i < n; i++) {
                double a = -Math.PI / 2 + i * 2 * Math.PI / n;
                float v = (float) Math.max(0.06, vals[i]);
                float x = cx + (float) Math.cos(a) * r * v, y = cy + (float) Math.sin(a) * r * v;
                if (i == 0) d.moveTo(x, y); else d.lineTo(x, y);
            }
            d.close();
            p.setStyle(Paint.Style.FILL);
            p.setColor(Art.alpha(Th.primary, 0.3f));
            c.drawPath(d, p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(U.dp(2.5f));
            p.setColor(Th.primary);
            c.drawPath(d, p);
            p.setStyle(Paint.Style.FILL);
            p.setTextAlign(Paint.Align.CENTER);
            p.setTypeface(U.font(800));
            p.setTextSize(U.dp(12));
            for (int i = 0; i < n; i++) {
                double a = -Math.PI / 2 + i * 2 * Math.PI / n;
                float x = cx + (float) Math.cos(a) * (r + U.dp(24)), y = cy + (float) Math.sin(a) * (r + U.dp(18)) + U.dp(4);
                p.setColor(colors[i] != 0 ? colors[i] : Th.text);
                c.drawText(labels[i] == null ? "" : labels[i], x, y, p);
            }
        }
    }

    // ---------------- Odak halkası + büyüyen bitki ----------------
    public static class FocusRing extends Anim {
        public float frac; public int color = 0xFFA78BFA; public String big = "", small = ""; public boolean breakMode, paused;
        public int treeType;
        public FocusRing(Context c) { super(c); }
        @Override void draw(Canvas c, float t) {
            float s = Math.min(getWidth(), getHeight());
            float cx = getWidth() / 2f, cy = getHeight() / 2f;
            float stroke = s * 0.045f;
            float r = s / 2 - stroke;
            // parıltı
            p.setColor(0xFFFFFFFF); p.setShader(new android.graphics.RadialGradient(cx, cy, r * 1.05f, Art.alpha(color, 0.18f), Art.alpha(color, 0f), Shader.TileMode.CLAMP));
            c.drawCircle(cx, cy, r * 1.05f, p);
            p.setShader(null);
            Art.ring(c, cx, cy, r, stroke, frac, color, Art.alpha(color, 0.15f));
            // uç noktası
            double a = -Math.PI / 2 + frac * 2 * Math.PI;
            p.setColor(0xFFFFFFFF);
            c.drawCircle(cx + (float) Math.cos(a) * r, cy + (float) Math.sin(a) * r, stroke * 0.7f * (1 + (paused ? 0 : 0.15f * (float) Math.sin(t * 4))), p);
            // bitki
            if (!breakMode) Art.tree(c, cx, cy + r * 0.12f, r * 0.62f, treeType, 0.15f + frac * 0.85f, false);
            else {
                p.setTextAlign(Paint.Align.CENTER);
                p.setTextSize(r * 0.36f);
                c.drawText("☕", cx, cy - r * 0.08f, p);
            }
            p.setTextAlign(Paint.Align.CENTER);
            p.setColor(Th.text);
            p.setTypeface(U.font(900));
            p.setTextSize(s * 0.13f);
            c.drawText(big, cx, cy + r * 0.45f, p);
            p.setTypeface(U.font(700));
            p.setTextSize(s * 0.045f);
            p.setColor(Th.dim);
            c.drawText(small, cx, cy + r * 0.62f, p);
        }
    }

    // ---------------- İmza ----------------
    public static class SignatureView extends View {
        final Path path = new Path();
        final StringBuilder data = new StringBuilder();
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        public boolean empty = true;
        public Runnable onSigned;
        public SignatureView(Context c) {
            super(c);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(U.dp(3.5f));
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeJoin(Paint.Join.ROUND);
            setBackground(U.rrs(Th.surface2, 20, Th.stroke, 1));
        }
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouchEvent(MotionEvent e) {
            getParent().requestDisallowInterceptTouchEvent(true);
            float x = e.getX(), y = e.getY();
            String pt = String.format(java.util.Locale.US, "%.3f,%.3f", x / getWidth(), y / getHeight());
            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN: path.moveTo(x, y); data.append(data.length() > 0 ? "|" : "").append(pt); break;
                case MotionEvent.ACTION_MOVE: path.lineTo(x, y); data.append(";").append(pt); empty = false; break;
                case MotionEvent.ACTION_UP: if (onSigned != null && !empty) onSigned.run(); break;
            }
            invalidate();
            return true;
        }
        public void clear() { path.reset(); data.setLength(0); empty = true; invalidate(); }
        public String data() { return data.toString(); }
        @Override
        protected void onDraw(Canvas c) {
            p.setColor(Th.text);
            c.drawPath(path, p);
            if (empty) {
                p.setStyle(Paint.Style.FILL);
                p.setTextAlign(Paint.Align.CENTER);
                p.setTextSize(U.dp(14));
                p.setTypeface(U.font(700));
                p.setColor(Th.dim);
                c.drawText("✍️ Parmağınla buraya imzala", getWidth() / 2f, getHeight() / 2f, p);
                p.setStyle(Paint.Style.STROKE);
            }
        }
    }

    /** Kayıtlı imzayı çizer. */
    public static class SignatureShow extends View {
        public String data = "";
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        public SignatureShow(Context c) {
            super(c);
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(U.dp(2.5f)); p.setStrokeCap(Paint.Cap.ROUND); p.setStrokeJoin(Paint.Join.ROUND);
        }
        @Override
        protected void onDraw(Canvas c) {
            if (data == null || data.isEmpty()) return;
            p.setColor(Th.text);
            Path path = new Path();
            for (String stroke : data.split("\\|")) {
                String[] pts = stroke.split(";");
                for (int i = 0; i < pts.length; i++) {
                    String[] xy = pts[i].split(",");
                    if (xy.length < 2) continue;
                    float x = Float.parseFloat(xy[0]) * getWidth(), y = Float.parseFloat(xy[1]) * getHeight();
                    if (i == 0) path.moveTo(x, y); else path.lineTo(x, y);
                }
            }
            c.drawPath(path, p);
        }
    }

    // ---------------- Madalya ----------------
    public static class MedalView extends Anim {
        public int color = 0xFFFFC53D; public String emoji = "🏅"; public boolean locked;
        public MedalView(Context c) { super(c); animating = false; }
        @Override void draw(Canvas c, float t) {
            float s = Math.min(getWidth(), getHeight());
            float sh = animating ? (t % 2.5f) / 1.2f : 0;
            Art.medal(c, getWidth() / 2f, getHeight() / 2f, s * 0.42f, color, emoji, locked, sh > 1 ? 0 : sh);
        }
    }

    // ---------------- Sandık ----------------
    public static class ChestView extends Anim {
        public int rarity; public float open;
        public ChestView(Context c) { super(c); }
        @Override void draw(Canvas c, float t) {
            float s = Math.min(getWidth(), getHeight()) * 0.55f;
            Art.chest(c, getWidth() / 2f, getHeight() * 0.58f, s, rarity, open, t);
        }
    }

    // ---------------- Konfeti ----------------
    public static class ConfettiView extends View {
        static final class Pc { float x, y, vx, vy, rot, vr, size; int color; int shape; float life; }
        final ArrayList<Pc> ps = new ArrayList<>();
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Random r = new Random();
        long last;
        static final int[] COLS = {0xFFFF7A45, 0xFFFFD84D, 0xFF3DDC84, 0xFF4FB3FF, 0xFFA78BFA, 0xFFFF6B8A, 0xFFFFFFFF};

        public ConfettiView(Context c) { super(c); setClickable(false); }

        public void burst(float x, float y, int count, float power, int[] colors) {
            int[] cols = colors == null ? COLS : colors;
            for (int i = 0; i < count; i++) {
                Pc q = new Pc();
                q.x = x; q.y = y;
                double a = r.nextDouble() * Math.PI * 2;
                float sp = (0.3f + r.nextFloat()) * power;
                q.vx = (float) Math.cos(a) * sp; q.vy = (float) Math.sin(a) * sp - power * 0.6f;
                q.rot = r.nextFloat() * 360; q.vr = (r.nextFloat() - 0.5f) * 720;
                q.size = U.dp(4 + r.nextInt(5));
                q.color = cols[r.nextInt(cols.length)];
                q.shape = r.nextInt(3);
                q.life = 1.6f + r.nextFloat();
                ps.add(q);
            }
            last = SystemClock.uptimeMillis();
            invalidate();
        }

        /** Ekranın üstünden yağan konfeti. */
        public void rain(int count) {
            for (int i = 0; i < count; i++) {
                Pc q = new Pc();
                q.x = r.nextFloat() * getWidth(); q.y = -r.nextFloat() * getHeight() * 0.5f;
                q.vx = (r.nextFloat() - 0.5f) * U.dp(60); q.vy = U.dp(120 + r.nextInt(160));
                q.rot = r.nextFloat() * 360; q.vr = (r.nextFloat() - 0.5f) * 540;
                q.size = U.dp(5 + r.nextInt(5)); q.color = COLS[r.nextInt(COLS.length)]; q.shape = r.nextInt(3);
                q.life = 4;
                ps.add(q);
            }
            last = SystemClock.uptimeMillis();
            invalidate();
        }

        @Override
        protected void onDraw(Canvas c) {
            if (ps.isEmpty()) return;
            long now = SystemClock.uptimeMillis();
            float dt = Math.min(0.05f, (now - last) / 1000f);
            last = now;
            float g = U.dp(900);
            for (int i = ps.size() - 1; i >= 0; i--) {
                Pc q = ps.get(i);
                q.vy += g * dt * 0.5f; q.vx *= 0.99f;
                q.x += q.vx * dt; q.y += q.vy * dt; q.rot += q.vr * dt; q.life -= dt;
                if (q.life <= 0 || q.y > getHeight() + 50) { ps.remove(i); continue; }
                p.setColor(q.color);
                p.setAlpha((int) (255 * Math.min(1, q.life)));
                c.save();
                c.translate(q.x, q.y);
                c.rotate(q.rot);
                float sx = (float) Math.abs(Math.cos(q.rot * 0.05));
                if (q.shape == 0) c.drawRect(-q.size, -q.size * 0.5f * sx, q.size, q.size * 0.5f * sx, p);
                else if (q.shape == 1) c.drawCircle(0, 0, q.size * 0.55f, p);
                else { Path t = new Path(); t.moveTo(0, -q.size); t.lineTo(q.size, q.size); t.lineTo(-q.size, q.size); t.close(); c.drawPath(t, p); }
                c.restore();
            }
            if (!ps.isEmpty()) postInvalidateOnAnimation();
        }
    }

    // ---------------- Akış düzeni (hapları sarar) ----------------
    public static class Flow extends ViewGroup {
        public int gap = U.dp(8);
        public Flow(Context c) { super(c); }
        @Override
        protected void onMeasure(int wm, int hm) {
            int maxW = MeasureSpec.getSize(wm) - getPaddingLeft() - getPaddingRight();
            int x = 0, y = 0, rowH = 0;
            for (int i = 0; i < getChildCount(); i++) {
                View ch = getChildAt(i);
                if (ch.getVisibility() == GONE) continue;
                ch.measure(MeasureSpec.makeMeasureSpec(maxW, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                if (x + ch.getMeasuredWidth() > maxW && x > 0) { x = 0; y += rowH + gap; rowH = 0; }
                x += ch.getMeasuredWidth() + gap;
                rowH = Math.max(rowH, ch.getMeasuredHeight());
            }
            setMeasuredDimension(MeasureSpec.getSize(wm), y + rowH + getPaddingTop() + getPaddingBottom());
        }
        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            int maxW = r - l - getPaddingLeft() - getPaddingRight();
            int x = 0, y = 0, rowH = 0;
            for (int i = 0; i < getChildCount(); i++) {
                View ch = getChildAt(i);
                if (ch.getVisibility() == GONE) continue;
                if (x + ch.getMeasuredWidth() > maxW && x > 0) { x = 0; y += rowH + gap; rowH = 0; }
                ch.layout(getPaddingLeft() + x, getPaddingTop() + y, getPaddingLeft() + x + ch.getMeasuredWidth(), getPaddingTop() + y + ch.getMeasuredHeight());
                x += ch.getMeasuredWidth() + gap;
                rowH = Math.max(rowH, ch.getMeasuredHeight());
            }
        }
    }

    // ---------------- Saat seçici ----------------
    public static class TimeStepper extends LinearLayout {
        public int minute;
        public int step = 15;
        final TextView val;
        public Runnable onChange;
        public TimeStepper(Context c, int minute, String label) {
            super(c);
            this.minute = minute;
            setOrientation(VERTICAL);
            setGravity(Gravity.CENTER);
            if (label != null) {
                TextView l = U.tv(c, label, 13, Th.dim, 800);
                l.setGravity(Gravity.CENTER);
                addView(l);
            }
            LinearLayout row = U.h(c);
            row.setGravity(Gravity.CENTER);
            TextView minus = stepBtn(c, "−", -1);
            val = U.tv(c, Tm.hhmm(minute), 34, Th.text, 900);
            val.setGravity(Gravity.CENTER);
            val.setMinWidth(U.dp(120));
            TextView plus = stepBtn(c, "+", 1);
            row.addView(minus);
            row.addView(val);
            row.addView(plus);
            addView(row);
        }
        TextView stepBtn(Context c, String s, final int dir) {
            final TextView b = U.tv(c, s, 26, Th.text, 900);
            b.setGravity(Gravity.CENTER);
            b.setBackground(U.rr(Th.surface2, 50));
            b.setLayoutParams(new LinearLayout.LayoutParams(U.dp(52), U.dp(52)));
            final Runnable[] rep = new Runnable[1];
            rep[0] = () -> { change(dir); b.postDelayed(rep[0], 90); };
            b.setOnTouchListener((v, e) -> {
                if (e.getActionMasked() == MotionEvent.ACTION_DOWN) { change(dir); U.haptic(v); v.postDelayed(rep[0], 450); v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(80).start(); }
                if (e.getActionMasked() == MotionEvent.ACTION_UP || e.getActionMasked() == MotionEvent.ACTION_CANCEL) { v.removeCallbacks(rep[0]); v.animate().scaleX(1).scaleY(1).setDuration(150).start(); }
                return true;
            });
            return b;
        }
        void change(int dir) {
            minute = Tm.norm(minute + dir * step);
            val.setText(Tm.hhmm(minute));
            if (onChange != null) onChange.run();
        }
        public void set(int m) { minute = Tm.norm(m); val.setText(Tm.hhmm(minute)); }
    }

    /** Sayı seçici (miktar, dakika). */
    public static class NumStepper extends LinearLayout {
        public int value, step, min, max;
        final TextView val;
        public java.util.function.IntFunction<String> fmt = String::valueOf;
        public Runnable onChange;
        public NumStepper(Context c, int value, int step, int min, int max, java.util.function.IntFunction<String> fmt) {
            super(c);
            this.value = value; this.step = step; this.min = min; this.max = max;
            if (fmt != null) this.fmt = fmt;
            setOrientation(HORIZONTAL);
            setGravity(Gravity.CENTER_VERTICAL);
            TextView minus = btn(c, "−", -1);
            val = U.tv(c, this.fmt.apply(value), 20, Th.text, 900);
            val.setGravity(Gravity.CENTER);
            val.setMinWidth(U.dp(96));
            TextView plus = btn(c, "+", 1);
            addView(minus); addView(val); addView(plus);
        }
        TextView btn(Context c, String s, final int dir) {
            final TextView b = U.tv(c, s, 22, Th.text, 900);
            b.setGravity(Gravity.CENTER);
            b.setBackground(U.rr(Th.surface2, 50));
            b.setLayoutParams(new LinearLayout.LayoutParams(U.dp(44), U.dp(44)));
            final Runnable[] rep = new Runnable[1];
            rep[0] = () -> { change(dir); b.postDelayed(rep[0], 80); };
            b.setOnTouchListener((v, e) -> {
                if (e.getActionMasked() == MotionEvent.ACTION_DOWN) { change(dir); U.haptic(v); v.postDelayed(rep[0], 450); }
                if (e.getActionMasked() == MotionEvent.ACTION_UP || e.getActionMasked() == MotionEvent.ACTION_CANCEL) v.removeCallbacks(rep[0]);
                return true;
            });
            return b;
        }
        void change(int dir) {
            value = Math.max(min, Math.min(max, value + dir * step));
            val.setText(fmt.apply(value));
            if (onChange != null) onChange.run();
        }
    }

    /** Gün seçici: P S Ç P C C P */
    public static class DaysPicker extends LinearLayout {
        public int mask;
        final TextView[] ds = new TextView[7];
        public Runnable onChange;
        public DaysPicker(Context c, int mask) {
            super(c);
            this.mask = mask;
            setOrientation(HORIZONTAL);
            for (int i = 0; i < 7; i++) {
                final int d = i;
                TextView t = U.tv(c, Tm.DAY_SHORT[i], 13, Th.text, 800);
                t.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, U.dp(40));
                lp.weight = 1; lp.setMargins(U.dp(2), 0, U.dp(2), 0);
                t.setOnClickListener(v -> { this.mask ^= (1 << d); upd(); U.haptic(v); if (onChange != null) onChange.run(); });
                ds[i] = t;
                addView(t, lp);
            }
            upd();
        }
        void upd() {
            for (int i = 0; i < 7; i++) {
                boolean on = (mask & (1 << i)) != 0;
                ds[i].setBackground(on ? U.rr(Th.primary, 12) : U.rrs(Th.surface2, 12, Th.stroke, 1));
                ds[i].setTextColor(on ? U.Btn.contrast(Th.primary) : Th.dim);
            }
        }
    }

    public static int textSizePx(float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, U.app.getResources().getDisplayMetrics());
    }

    public static int darker(int c) { return Color.rgb((int) (Color.red(c) * 0.8), (int) (Color.green(c) * 0.8), (int) (Color.blue(c) * 0.8)); }
}
