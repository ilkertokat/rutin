package com.ilker.rutin.ui.screens;

import android.annotation.SuppressLint;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.ilker.rutin.data.Repo;
import com.ilker.rutin.ui.MainActivity;
import com.ilker.rutin.ui.U;

/** Kaydırılabilir ekran tabanı. */
@SuppressLint("ViewConstructor")
public abstract class Screen extends FrameLayout {
    protected final MainActivity a;
    protected final Repo r;
    protected final ScrollView scroll;
    protected final LinearLayout col;
    protected int top, bottom;

    public Screen(MainActivity a) {
        super(a);
        this.a = a;
        this.r = a.r;
        scroll = new ScrollView(a);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setOverScrollMode(OVER_SCROLL_NEVER);
        scroll.setClipToPadding(false);
        col = U.v(a);
        col.setPadding(U.dp(16), 0, U.dp(16), U.dp(24));
        scroll.addView(col, new ScrollView.LayoutParams(-1, -2));
        addView(scroll, new FrameLayout.LayoutParams(-1, -1));
        scrim = new android.view.View(a);
        scrim.setBackgroundColor(com.ilker.rutin.ui.Art.alpha(com.ilker.rutin.ui.Th.bg, 0.94f));
        addView(scrim, new FrameLayout.LayoutParams(-1, 0));
    }

    protected final android.view.View scrim;

    public void insets(int top, int bottom) {
        if (top == this.top && bottom == this.bottom && top != 0) return;
        this.top = top;
        this.bottom = bottom;
        scroll.setPadding(0, 0, 0, bottom);
        scrim.getLayoutParams().height = top;
        scrim.requestLayout();
        onInsets();
    }

    protected void onInsets() {}

    public abstract void refresh();

    protected long now() { return r.now(); }
}
