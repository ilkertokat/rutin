package com.ilker.rutin.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Alttan kayan panel. İçeriği kaydırılabilir; arka plana dokununca veya aşağı sürükleyince kapanır. */
@SuppressLint("ViewConstructor")
public class Sheet extends FrameLayout {
    public final LinearLayout body;
    final LinearLayout panel;
    final View dim;
    public Runnable onClose;
    boolean closing;
    public boolean full;

    public Sheet(Context c, String title) { this(c, title, false); }

    @SuppressLint("ClickableViewAccessibility")
    public Sheet(Context c, String title, boolean full) {
        super(c);
        this.full = full;
        dim = new View(c);
        dim.setBackgroundColor(0x99000000);
        dim.setOnClickListener(v -> dismiss());
        addView(dim, new LayoutParams(-1, -1));
        panel = U.v(c);
        panel.setBackground(Th.light ? U.rr(Th.bg, 28) : U.rr(Th.surface, 28));
        panel.setClickable(true);
        LayoutParams plp = new LayoutParams(-1, full ? -1 : -2, Gravity.BOTTOM);
        plp.topMargin = U.dp(full ? 24 : 70);
        addView(panel, plp);
        View handle = new View(c);
        handle.setBackground(U.rr(Art.alpha(Th.text, 0.25f), 4));
        LinearLayout.LayoutParams hl = new LinearLayout.LayoutParams(U.dp(44), U.dp(5));
        hl.gravity = Gravity.CENTER_HORIZONTAL;
        hl.topMargin = U.dp(10);
        panel.addView(handle, hl);
        // aşağı sürükleyerek kapat
        final float[] y0 = new float[1];
        handle.setOnTouchListener((v, e) -> dragClose(e, y0));
        if (title != null) {
            TextView t = U.tv(c, title, 20, Th.text, 900);
            t.setPadding(U.dp(22), U.dp(12), U.dp(22), U.dp(4));
            t.setOnTouchListener((v, e) -> dragClose(e, y0));
            panel.addView(t);
        }
        ScrollView sv = new ScrollView(c);
        sv.setOverScrollMode(OVER_SCROLL_NEVER);
        sv.setVerticalScrollBarEnabled(false);
        body = U.v(c);
        body.setPadding(U.dp(20), U.dp(8), U.dp(20), U.dp(28));
        sv.addView(body, new ScrollView.LayoutParams(-1, -2));
        panel.addView(sv, new LinearLayout.LayoutParams(-1, full ? 0 : -2, full ? 1 : 0));
    }

    boolean dragClose(MotionEvent e, float[] y0) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: y0[0] = e.getRawY(); return true;
            case MotionEvent.ACTION_MOVE: panel.setTranslationY(Math.max(0, e.getRawY() - y0[0])); return true;
            case MotionEvent.ACTION_UP: case MotionEvent.ACTION_CANCEL:
                if (panel.getTranslationY() > U.dp(90)) dismiss();
                else panel.animate().translationY(0).setDuration(200).start();
                return true;
        }
        return false;
    }

    public Sheet add(View v) { body.addView(v); return this; }

    public Sheet add(View v, LinearLayout.LayoutParams lp) { body.addView(v, lp); return this; }

    public void setTopInset(int px) {
        LayoutParams lp = (LayoutParams) panel.getLayoutParams();
        lp.topMargin = px + U.dp(full ? 10 : 70);
        panel.setLayoutParams(lp);
    }

    public void setBottomInset(int px) { body.setPadding(body.getPaddingLeft(), body.getPaddingTop(), body.getPaddingRight(), U.dp(28) + px); }

    public void appear() {
        dim.setAlpha(0);
        dim.animate().alpha(1).setDuration(220).start();
        panel.setTranslationY(U.dp(600));
        panel.post(() -> {
            panel.setTranslationY(panel.getHeight());
            panel.animate().translationY(0).setDuration(320).setInterpolator(new DecelerateInterpolator(2.2f)).start();
        });
    }

    public void dismiss() {
        if (closing) return;
        closing = true;
        dim.animate().alpha(0).setDuration(200).start();
        panel.animate().translationY(panel.getHeight() + U.dp(40)).setDuration(220).withEndAction(() -> {
            ViewGroup p = (ViewGroup) getParent();
            if (p != null) p.removeView(this);
            if (onClose != null) onClose.run();
        }).start();
    }
}
