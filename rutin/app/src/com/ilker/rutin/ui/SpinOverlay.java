package com.ilker.rutin.ui;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ilker.rutin.core.Ev;
import com.ilker.rutin.core.Rewards;
import com.ilker.rutin.core.Synth;
import com.ilker.rutin.data.Repo;
import com.ilker.rutin.sys.Sound;

import java.util.Random;

/** Günlük şans çarkı (değişken ödül). */
@SuppressLint("ViewConstructor")
public class SpinOverlay extends FrameLayout {
    final MainActivity a;
    final Repo r;
    final Wheel wheel;
    final TextView result;
    final U.Btn btn;
    boolean spinning, done;

    public static void show(MainActivity a) { a.showFull(new SpinOverlay(a)); }

    public SpinOverlay(MainActivity a) {
        super(a);
        this.a = a;
        this.r = a.r;
        setClickable(true);
        setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{0xFF2A1450, 0xFF0B0B12}));
        LinearLayout c = U.v(a);
        c.setGravity(Gravity.CENTER_HORIZONTAL);
        c.setPadding(U.dp(24), a.topInset + U.dp(30), U.dp(24), a.bottomInset + U.dp(24));
        addView(c, new LayoutParams(-1, -1));
        TextView t = U.tv(a, "🎡 Şans Çarkı", 30, 0xFFFFFFFF, 900);
        t.setGravity(Gravity.CENTER);
        c.addView(t, U.lpMW());
        TextView s = U.tv(a, "Bugünün serisini güvenceye aldın. Ödülünü çevir!", 15, 0xCCFFFFFF, 700);
        s.setGravity(Gravity.CENTER);
        c.addView(s, U.m(U.lpMW(), 0, 6, 0, 0));
        c.addView(U.fill(a), new LinearLayout.LayoutParams(1, 0, 1));
        wheel = new Wheel(a);
        c.addView(wheel, new LinearLayout.LayoutParams(-1, U.dp(330)));
        result = U.tv(a, "", 24, Th.gold, 900);
        result.setGravity(Gravity.CENTER);
        c.addView(result, U.m(U.lpMW(), 0, 16, 0, 0));
        c.addView(U.fill(a), new LinearLayout.LayoutParams(1, 0, 1));
        btn = U.btn(a, "ÇEVİR!", Th.primary);
        btn.setOnClickListener(v -> { if (done) a.closeFull(this); else spin(); });
        c.addView(btn, U.lpMW());
    }

    void spin() {
        if (spinning) return;
        spinning = true;
        U.hapticStrong(this);
        final Ev.List ev = new Ev.List();
        final int idx = Rewards.spin(r.e, r.now(), new Random(System.nanoTime()), ev);
        if (idx < 0) { a.closeFull(this); return; }
        int n = Rewards.SPIN.length;
        float seg = 360f / n;
        // İşaretçi üstte (-90°). idx diliminin ortası işaretçiye gelmeli.
        float target = 360 * 6 + (360 - (idx * seg + seg / 2)) + (float) (Math.random() - 0.5) * seg * 0.6f;
        final float start = wheel.angle;
        ValueAnimator an = ValueAnimator.ofFloat(0, 1);
        an.setDuration(4200);
        an.setInterpolator(new DecelerateInterpolator(2.6f));
        final int[] lastSeg = {-1};
        an.addUpdateListener(x -> {
            float f = (float) x.getAnimatedValue();
            wheel.angle = start + (target - start % 360) * f;
            int sg = (int) (wheel.angle / seg);
            if (sg != lastSeg[0]) { lastSeg[0] = sg; Sound.play(a, Synth.TICK); }
            wheel.invalidate();
        });
        an.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(android.animation.Animator animation) {
                spinning = false;
                done = true;
                String txt = "";
                for (Ev e : ev) if (e.type == Ev.SPIN) txt = e.text;
                result.setText(txt);
                U.pop(result);
                Sound.play(a, idx == 7 ? Synth.FANFARE : Synth.CHEST);
                a.confetti.rain(idx == 7 ? 260 : 140);
                btn.setText("HARİKA!");
                r.commit();
                postDelayed(() -> a.celebrate(ev, null), 1600);
            }
        });
        an.start();
    }

    static class Wheel extends View {
        float angle;
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        Wheel(Context c) { super(c); }

        @Override
        protected void onDraw(Canvas c) {
            float cx = getWidth() / 2f, cy = getHeight() / 2f + U.dp(8);
            float rad = Math.min(cx, getHeight() / 2f) - U.dp(28);
            int n = Rewards.SPIN.length;
            float seg = 360f / n;
            p.setShader(null);
            p.setColor(0x55000000);
            c.drawCircle(cx, cy + U.dp(6), rad + U.dp(8), p);
            p.setColor(0xFFFFD27A);
            c.drawCircle(cx, cy, rad + U.dp(8), p);
            RectF o = new RectF(cx - rad, cy - rad, cx + rad, cy + rad);
            for (int i = 0; i < n; i++) {
                float st = -90 + angle + i * seg;
                p.setColor(Rewards.SPIN_COLORS[i]);
                c.drawArc(o, st, seg, true, p);
                p.setColor(0x22000000);
                if (i % 2 == 0) c.drawArc(o, st, seg, true, p);
                c.save();
                c.rotate(st + seg / 2, cx, cy);
                p.setColor(0xFF1A1A22);
                p.setTypeface(U.font(900));
                p.setTextSize(U.dp(14));
                p.setTextAlign(Paint.Align.RIGHT);
                c.drawText(Rewards.SPIN[i], cx + rad - U.dp(14), cy + U.dp(5), p);
                c.restore();
            }
            // lambalar
            for (int i = 0; i < 16; i++) {
                double aa = Math.toRadians(i * 22.5 + angle * 0.2);
                p.setColor(i % 2 == 0 ? 0xFFFFFFFF : 0xFFFF7A45);
                c.drawCircle(cx + (float) Math.cos(aa) * (rad + U.dp(4)), cy + (float) Math.sin(aa) * (rad + U.dp(4)), U.dp(3), p);
            }
            p.setColor(0xFF1A1A22);
            c.drawCircle(cx, cy, U.dp(26), p);
            p.setColor(0xFFFFD27A);
            c.drawCircle(cx, cy, U.dp(20), p);
            p.setTextAlign(Paint.Align.CENTER);
            p.setTextSize(U.dp(20));
            c.drawText("⭐", cx, cy + U.dp(7), p);
            // işaretçi
            Path ptr = new Path();
            ptr.moveTo(cx - U.dp(16), cy - rad - U.dp(18));
            ptr.lineTo(cx + U.dp(16), cy - rad - U.dp(18));
            ptr.lineTo(cx, cy - rad + U.dp(14));
            ptr.close();
            p.setColor(0xFFFFFFFF);
            c.drawPath(ptr, p);
            p.setColor(0xFFE0245E);
            c.drawCircle(cx, cy - rad - U.dp(14), U.dp(6), p);
        }
    }
}
