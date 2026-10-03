package com.ilker.rutin.ui;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ilker.rutin.data.Repo;

import java.util.HashMap;

/** Arayüz yardımcıları: ölçü, yazı tipi, kart, buton, giriş alanı, animasyon. */
public final class U {
    private U() {}

    public static float density = 3;
    public static Context app;
    static final HashMap<Integer, Typeface> fonts = new HashMap<>();

    public static void init(Context c) {
        app = c.getApplicationContext();
        density = c.getResources().getDisplayMetrics().density;
    }

    public static int dp(float v) { return Math.round(v * density); }

    static java.io.File fontFile;

    public static Typeface font(int weight) {
        Typeface t = fonts.get(weight);
        if (t != null) return t;
        try {
            t = new Typeface.Builder(app.getAssets(), "Nunito.ttf").setFontVariationSettings("'wght' " + weight).build();
        } catch (Throwable e) {
            t = null;
        }
        if (t == null) {
            try {
                if (fontFile == null) {
                    fontFile = new java.io.File(app.getFilesDir(), "Nunito.ttf");
                    if (!fontFile.exists()) {
                        try (java.io.InputStream in = app.getAssets().open("Nunito.ttf");
                             java.io.FileOutputStream o = new java.io.FileOutputStream(fontFile)) {
                            byte[] b = new byte[16384]; int n;
                            while ((n = in.read(b)) > 0) o.write(b, 0, n);
                        }
                    }
                }
                t = new Typeface.Builder(fontFile).setFontVariationSettings("'wght' " + weight).build();
            } catch (Throwable e) {
                t = null;
            }
        }
        if (t == null) t = weight >= 700 ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT;
        fonts.put(weight, t);
        return t;
    }

    public static TextView tv(Context c, String s, float sp, int color, int weight) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setTextColor(color);
        t.setTypeface(font(weight));
        t.setIncludeFontPadding(true);
        t.setLineSpacing(0, 1.08f);
        return t;
    }

    public static TextView title(Context c, String s) { return tv(c, s, 22, Th.text, 900); }

    public static TextView section(Context c, String s) {
        TextView t = tv(c, s, 13, Th.dim, 800);
        t.setLetterSpacing(0.06f);
        t.setPadding(dp(4), dp(18), dp(4), dp(8));
        return t;
    }

    public static GradientDrawable rr(int color, float radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radiusDp));
        return g;
    }

    public static GradientDrawable rrs(int color, float radiusDp, int strokeColor, float strokeDp) {
        GradientDrawable g = rr(color, radiusDp);
        g.setStroke(dp(strokeDp), strokeColor);
        return g;
    }

    public static GradientDrawable grad(int a, int b, float radiusDp, GradientDrawable.Orientation o) {
        GradientDrawable g = new GradientDrawable(o, new int[]{a, b});
        g.setCornerRadius(dp(radiusDp));
        return g;
    }

    public static RippleDrawable ripple(int color, float radiusDp) {
        return new RippleDrawable(ColorStateList.valueOf(Art.alpha(Th.text, 0.12f)), rr(color, radiusDp), rr(0xFFFFFFFF, radiusDp));
    }

    public static LinearLayout v(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    public static LinearLayout h(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    public static LinearLayout card(Context c) {
        LinearLayout l = v(c);
        l.setBackground(rr(Th.surface, 24));
        l.setPadding(dp(16), dp(16), dp(16), dp(16));
        return l;
    }

    public static LinearLayout.LayoutParams lp(int w, int h) { return new LinearLayout.LayoutParams(w, h); }
    public static LinearLayout.LayoutParams lpMW() { return new LinearLayout.LayoutParams(-1, -2); }
    public static LinearLayout.LayoutParams lpWW() { return new LinearLayout.LayoutParams(-2, -2); }

    public static LinearLayout.LayoutParams lpW(float weight) {
        LinearLayout.LayoutParams l = new LinearLayout.LayoutParams(0, -2);
        l.weight = weight;
        return l;
    }

    public static LinearLayout.LayoutParams mb(LinearLayout.LayoutParams l, int bottomDp) { l.bottomMargin = dp(bottomDp); return l; }

    public static LinearLayout.LayoutParams m(LinearLayout.LayoutParams l, int left, int top, int right, int bottom) {
        l.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return l;
    }

    public static View space(Context c, int dp) {
        View v = new View(c);
        v.setLayoutParams(new LinearLayout.LayoutParams(dp(dp), dp(dp)));
        return v;
    }

    public static View fill(Context c) {
        View v = new View(c);
        v.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 1));
        return v;
    }

    public static FrameLayout.LayoutParams flp(int w, int h, int gravity) {
        FrameLayout.LayoutParams l = new FrameLayout.LayoutParams(w, h);
        l.gravity = gravity;
        return l;
    }

    public static TextView chip(Context c, String s, int bg, int fg) {
        TextView t = tv(c, s, 13, fg, 800);
        t.setBackground(rr(bg, 50));
        t.setPadding(dp(12), dp(6), dp(12), dp(6));
        t.setGravity(Gravity.CENTER);
        return t;
    }

    public static TextView emojiBox(Context c, String emoji, int color, int sizeDp) {
        TextView t = new TextView(c);
        t.setText(emoji);
        t.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp(sizeDp) * 0.5f);
        t.setGravity(Gravity.CENTER);
        t.setBackground(rr(Art.alpha(color, 0.18f), sizeDp * 0.34f));
        t.setLayoutParams(new LinearLayout.LayoutParams(dp(sizeDp), dp(sizeDp)));
        return t;
    }

    public static EditText input(Context c, String hint, String text, boolean multiline) {
        EditText e = new EditText(c);
        e.setHint(hint);
        e.setText(text == null ? "" : text);
        e.setTextColor(Th.text);
        e.setHintTextColor(Art.alpha(Th.dim, 0.7f));
        e.setTypeface(font(700));
        e.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        e.setBackground(rrs(Th.surface2, 16, Th.stroke, 1));
        e.setPadding(dp(14), dp(12), dp(14), dp(12));
        if (multiline) {
            e.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            e.setMinLines(2);
            e.setGravity(Gravity.TOP | Gravity.START);
        } else {
            e.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            e.setSingleLine(true);
        }
        return e;
    }

    public static EditText number(Context c, String hint, int value) {
        EditText e = input(c, hint, value > 0 ? String.valueOf(value) : "", false);
        e.setInputType(InputType.TYPE_CLASS_NUMBER);
        return e;
    }

    public static int parse(EditText e, int def) {
        try { return Integer.parseInt(e.getText().toString().trim()); } catch (Exception x) { return def; }
    }

    // ---------------- Dokunma geri bildirimi ----------------
    public static void haptic(View v) {
        if (Repo.get(v.getContext()).s.p.hapticOn) v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    }

    public static void hapticStrong(View v) {
        if (Repo.get(v.getContext()).s.p.hapticOn) v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
    }

    /** Basınca hafif küçülüp geri gelen (squish) his. */
    @SuppressLint("ClickableViewAccessibility")
    public static <T extends View> T squish(final T v) {
        v.setOnTouchListener((view, ev) -> {
            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    view.animate().scaleX(0.95f).scaleY(0.95f).setDuration(90).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    view.animate().scaleX(1f).scaleY(1f).setDuration(260).setInterpolator(new OvershootInterpolator(3f)).start();
                    break;
            }
            return false;
        });
        return v;
    }

    public static void pop(View v) {
        v.setScaleX(0.6f); v.setScaleY(0.6f); v.setAlpha(0f);
        v.animate().scaleX(1).scaleY(1).alpha(1).setDuration(380).setInterpolator(new OvershootInterpolator(2.2f)).start();
    }

    public static void bounce(View v) {
        v.animate().scaleX(1.18f).scaleY(1.18f).setDuration(120).withEndAction(() ->
                v.animate().scaleX(1f).scaleY(1f).setDuration(320).setInterpolator(new OvershootInterpolator(3f)).start()).start();
    }

    public static void countUp(final TextView t, final int from, final int to, final String prefix, final String suffix, long ms) {
        ValueAnimator a = ValueAnimator.ofInt(from, to);
        a.setDuration(ms);
        a.addUpdateListener(an -> t.setText(prefix + an.getAnimatedValue() + suffix));
        a.start();
    }

    public static void clear(ViewGroup g) { g.removeAllViews(); }

    // ---------------- 3D buton ----------------
    /** Duolingo tarzı alt gölgeli, basınca çöken buton. */
    public static class Btn extends TextView {
        int color, shadow;
        boolean down;
        final RectF r = new RectF();
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        final int depth = dp(4);
        float radius = dp(16);

        public Btn(Context c, String text, int color) {
            super(c);
            this.color = color;
            this.shadow = Art.mix(color, 0xFF000000, 0.32f);
            setText(text);
            setTextColor(Art.mix(color, 0xFF000000, 0) == Th.surface2 ? Th.text : contrast(color));
            setTypeface(font(900));
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            setGravity(Gravity.CENTER);
            setPadding(dp(18), dp(13), dp(18), dp(13) + depth);
            setClickable(true);
            setLetterSpacing(0.02f);
        }

        public Btn radius(float dpv) { radius = dp(dpv); invalidate(); return this; }

        public Btn colors(int c, int fg) { color = c; shadow = Art.mix(c, 0xFF000000, 0.32f); setTextColor(fg); invalidate(); return this; }

        public static int contrastPublic(int c) { return contrast(c); }

        public static int contrast(int c) {
            double l = (0.299 * ((c >> 16) & 255) + 0.587 * ((c >> 8) & 255) + 0.114 * (c & 255)) / 255;
            return l > 0.62 ? 0xFF1A1A22 : 0xFFFFFFFF;
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouchEvent(MotionEvent e) {
            if (!isEnabled()) return false;
            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN: down = true; invalidate(); haptic(this); break;
                case MotionEvent.ACTION_UP: case MotionEvent.ACTION_CANCEL: down = false; invalidate(); break;
            }
            return super.onTouchEvent(e);
        }

        @Override
        protected void onDraw(Canvas c) {
            int w = getWidth(), h = getHeight();
            float off = down ? depth : 0;
            p.setColor(isEnabled() ? shadow : Art.alpha(shadow, 0.4f));
            r.set(0, depth, w, h);
            c.drawRoundRect(r, radius, radius, p);
            p.setColor(isEnabled() ? color : Art.alpha(color, 0.4f));
            r.set(0, off, w, h - depth + off);
            c.drawRoundRect(r, radius, radius, p);
            p.setColor(0x22FFFFFF);
            r.set(dp(6), off + dp(3), w - dp(6), off + dp(7));
            c.drawRoundRect(r, dp(4), dp(4), p);
            c.save();
            c.translate(0, off);
            super.onDraw(c);
            c.restore();
        }
    }

    public static Btn btn(Context c, String text, int color) { return new Btn(c, text, color); }

    public static Btn ghost(Context c, String text) {
        Btn b = new Btn(c, text, Th.surface2);
        b.setTextColor(Th.text);
        return b;
    }

    /** Başlık + alt metin satırı. */
    public static LinearLayout row(Context c, String emoji, String title, String sub, int color) {
        LinearLayout r = h(c);
        r.setPadding(dp(4), dp(8), dp(4), dp(8));
        if (emoji != null) {
            TextView e = emojiBox(c, emoji, color, 40);
            r.addView(e);
            r.addView(space(c, 12));
        }
        LinearLayout col = v(c);
        col.addView(tv(c, title, 15, Th.text, 800));
        if (sub != null && !sub.isEmpty()) col.addView(tv(c, sub, 13, Th.dim, 600));
        r.addView(col, lpW(1));
        return r;
    }

    /** Seçilebilir hap (chip) grubu için hap. */
    public static TextView pill(Context c, String text, boolean on) {
        TextView t = tv(c, text, 14, on ? Btn.contrast(Th.primary) : Th.text, 800);
        t.setBackground(on ? rr(Th.primary, 50) : rrs(Th.surface2, 50, Th.stroke, 1));
        t.setPadding(dp(14), dp(9), dp(14), dp(9));
        t.setGravity(Gravity.CENTER);
        return t;
    }

    public static void setPill(TextView t, boolean on) {
        t.setTextColor(on ? Btn.contrast(Th.primary) : Th.text);
        t.setBackground(on ? rr(Th.primary, 50) : rrs(Th.surface2, 50, Th.stroke, 1));
    }
}
