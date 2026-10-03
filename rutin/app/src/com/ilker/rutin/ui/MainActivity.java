package com.ilker.rutin.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ilker.rutin.core.Ev;
import com.ilker.rutin.core.Task;
import com.ilker.rutin.data.Repo;
import com.ilker.rutin.sys.Notifier;
import com.ilker.rutin.sys.Scheduler;
import com.ilker.rutin.sys.Sound;
import com.ilker.rutin.ui.screens.FocusScreen;
import com.ilker.rutin.ui.screens.MeScreen;
import com.ilker.rutin.ui.screens.ProgressScreen;
import com.ilker.rutin.ui.screens.Screen;
import com.ilker.rutin.ui.screens.TodayScreen;
import com.ilker.rutin.ui.screens.WeekScreen;
import com.ilker.rutin.ui.sheets.Sheets;
import com.ilker.rutin.widget.Widgets;

import java.util.ArrayList;

/** Tek aktivite: ekranlar, alt gezinme, paneller, kutlamalar. */
public class MainActivity extends Activity {
    public static MainActivity I;

    public Repo r;
    FrameLayout root, content, overlay;
    public Views.ConfettiView confetti;
    LinearLayout nav;
    final Screen[] screens = new Screen[5];
    final TextView[] navItems = new TextView[5];
    int tab = 0;
    public int topInset, bottomInset;
    public Celebrate celebrate;
    final ArrayList<View> fulls = new ArrayList<>();
    Onboarding onboarding;
    final Runnable onChange = this::refresh;
    final Repo.EvListener onEv = ev -> celebrate(ev, null);
    public static final int REQ_NOTIF = 11, REQ_EXPORT = 21, REQ_IMPORT = 22;
    static final String[] NAV_E = {"🏠", "📅", "⏱️", "📊", "🐧"};
    static final String[] NAV_T = {"Bugün", "Hafta", "Odak", "İlerleme", "Ben"};

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        I = this;
        U.init(this);
        r = Repo.get(this);
        Th.apply(r.s.p.theme);
        Notifier.channels(this);
        Sound.prewarm(this);
        Window w = getWindow();
        if (Build.VERSION.SDK_INT >= 30) w.setDecorFitsSystemWindows(false);
        else w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        w.setStatusBarColor(Color.TRANSPARENT);
        w.setNavigationBarColor(Color.TRANSPARENT);
        build();
        r.listen(onChange);
        r.listenEv(onEv);
        handleIntent(getIntent());
    }

    void build() {
        Th.apply(r.s.p.theme);
        root = new FrameLayout(this);
        root.setBackgroundColor(Th.bg);
        content = new FrameLayout(this);
        root.addView(content, new FrameLayout.LayoutParams(-1, -1));
        nav = U.h(this);
        nav.setGravity(Gravity.CENTER);
        nav.setBackground(U.rr(Th.light ? 0xFFFFFFFF : Th.surface, 0));
        nav.setElevation(U.dp(12));
        for (int i = 0; i < 5; i++) {
            final int k = i;
            TextView t = U.tv(this, NAV_E[i] + "\n" + NAV_T[i], 11, Th.dim, 800);
            t.setGravity(Gravity.CENTER);
            t.setLineSpacing(U.dp(2), 1f);
            t.setPadding(0, U.dp(8), 0, U.dp(8));
            t.setOnClickListener(v -> { U.haptic(v); go(k); });
            navItems[i] = t;
            nav.addView(t, U.lpW(1));
        }
        root.addView(nav, U.flp(-1, -2, Gravity.BOTTOM));
        overlay = new FrameLayout(this);
        overlay.setElevation(U.dp(20));
        root.addView(overlay, new FrameLayout.LayoutParams(-1, -1));
        confetti = new Views.ConfettiView(this);
        confetti.setElevation(U.dp(40));
        root.addView(confetti, new FrameLayout.LayoutParams(-1, -1));
        celebrate = new Celebrate(this);
        root.setOnApplyWindowInsetsListener((v, ins) -> {
            int top, bot;
            if (Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets bars = ins.getInsets(WindowInsets.Type.systemBars());
                android.graphics.Insets ime = ins.getInsets(WindowInsets.Type.ime());
                top = bars.top; bot = Math.max(bars.bottom, 0);
                int imeX = ime.bottom > bot ? ime.bottom - bot : 0;
                overlay.setPadding(0, 0, 0, imeX);
                content.setPadding(0, 0, 0, imeX);
            } else {
                top = ins.getSystemWindowInsetTop(); bot = ins.getSystemWindowInsetBottom();
            }
            topInset = top; bottomInset = bot;
            nav.setPadding(0, 0, 0, bot);
            for (Screen s : screens) if (s != null) s.insets(top, navHeight());
            return ins;
        });
        setContentView(root);
        if (Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController c = getWindow().getInsetsController();
            if (c != null) c.setSystemBarsAppearance(Th.light ? WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS : 0,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS);
        }
        if (!r.s.p.onboarded) {
            nav.setVisibility(View.GONE);
            onboarding = new Onboarding(this);
            content.addView(onboarding, new FrameLayout.LayoutParams(-1, -1));
        } else {
            go(tab);
        }
    }

    /** Tema değişince tüm arayüzü yeniden kur. */
    public void rebuild() {
        for (int i = 0; i < 5; i++) screens[i] = null;
        build();
        root.requestApplyInsets();
    }

    public int navHeight() { return nav.getVisibility() == View.GONE ? bottomInset : U.dp(62) + bottomInset; }

    public void onboardingDone() {
        content.removeView(onboarding);
        onboarding = null;
        nav.setVisibility(View.VISIBLE);
        go(0);
        root.requestApplyInsets();
        r.commit();
    }

    public void go(int k) {
        tab = k;
        if (screens[k] == null) {
            switch (k) {
                case 0: screens[k] = new TodayScreen(this); break;
                case 1: screens[k] = new WeekScreen(this); break;
                case 2: screens[k] = new FocusScreen(this); break;
                case 3: screens[k] = new ProgressScreen(this); break;
                default: screens[k] = new MeScreen(this);
            }
            screens[k].insets(topInset, navHeight());
        }
        content.removeAllViews();
        content.addView(screens[k], new FrameLayout.LayoutParams(-1, -1));
        screens[k].refresh();
        screens[k].setAlpha(0f);
        screens[k].setTranslationY(U.dp(12));
        screens[k].animate().alpha(1).translationY(0).setDuration(220).start();
        for (int i = 0; i < 5; i++) {
            boolean on = i == k;
            navItems[i].setTextColor(on ? Th.primary : Th.dim);
            navItems[i].setBackground(on ? U.rr(Art.alpha(Th.primary, 0.12f), 18) : null);
            if (on) U.bounce(navItems[i]);
        }
    }

    public void refresh() {
        if (onboarding != null) return;
        Screen s = screens[tab];
        if (s != null) s.refresh();
    }

    // ---------------- Paneller ----------------
    public Sheet sheet(String title) { return new Sheet(this, title); }

    public void show(Sheet s) {
        s.setBottomInset(bottomInset);
        s.setTopInset(topInset);
        overlay.addView(s, new FrameLayout.LayoutParams(-1, -1));
        s.appear();
    }

    public void showFull(View v) {
        fulls.add(v);
        overlay.addView(v, new FrameLayout.LayoutParams(-1, -1));
        v.setAlpha(0);
        v.animate().alpha(1).setDuration(250).start();
    }

    public void closeFull(View v) {
        fulls.remove(v);
        v.animate().alpha(0).setDuration(200).withEndAction(() -> overlay.removeView(v)).start();
    }

    public void closeAllSheets() {
        for (int i = overlay.getChildCount() - 1; i >= 0; i--) {
            View v = overlay.getChildAt(i);
            if (v instanceof Sheet) ((Sheet) v).dismiss();
        }
    }

    /** Üstte kayan kısa bildirim bandı. */
    public void toast(String msg) { banner(msg, Th.surface2, Th.text); }

    public void banner(String msg, int bg, int fg) {
        final TextView t = U.tv(this, msg, 15, fg, 800);
        t.setBackground(U.rr(bg, 18));
        t.setElevation(U.dp(30));
        t.setPadding(U.dp(18), U.dp(14), U.dp(18), U.dp(14));
        t.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams lp = U.flp(-1, -2, Gravity.TOP);
        lp.setMargins(U.dp(16), topInset + U.dp(10), U.dp(16), 0);
        root.addView(t, lp);
        t.setTranslationY(-U.dp(120));
        t.animate().translationY(0).setDuration(380).setInterpolator(new OvershootInterpolator(1.4f)).start();
        t.postDelayed(() -> t.animate().translationY(-U.dp(160)).alpha(0).setDuration(300).withEndAction(() -> root.removeView(t)).start(), 2600);
    }

    /** Bir görünümün üstünde yükselen "+15 XP" yazısı. */
    public void floatText(View anchor, String text, int color) {
        if (anchor == null) return;
        int[] a = new int[2], rr = new int[2];
        anchor.getLocationInWindow(a);
        root.getLocationInWindow(rr);
        final TextView t = U.tv(this, text, 18, color, 900);
        t.setShadowLayer(U.dp(4), 0, U.dp(1), 0x88000000);
        t.setElevation(U.dp(35));
        FrameLayout.LayoutParams lp = U.flp(-2, -2, Gravity.TOP | Gravity.START);
        lp.leftMargin = a[0] - rr[0] - U.dp(10);
        lp.topMargin = a[1] - rr[1] - U.dp(6);
        root.addView(t, lp);
        t.animate().translationY(-U.dp(70)).alpha(0).setDuration(1200).withEndAction(() -> root.removeView(t)).start();
    }

    public void confettiAt(View anchor, int count) {
        if (anchor == null) { confetti.rain(count); return; }
        int[] a = new int[2], rr = new int[2];
        anchor.getLocationInWindow(a);
        root.getLocationInWindow(rr);
        confetti.burst(a[0] - rr[0] + anchor.getWidth() / 2f, a[1] - rr[1] + anchor.getHeight() / 2f, count, U.dp(420), null);
    }

    /** Motor olaylarını ses, animasyon ve tam ekran kutlamalara çevirir. */
    public void celebrate(Ev.List ev, View anchor) {
        if (ev == null || ev.isEmpty()) return;
        Sound.forEvents(this, ev);
        celebrate.handle(ev, anchor);
    }

    // ---------------- Yaşam döngüsü ----------------
    @Override
    protected void onResume() {
        super.onResume();
        r.foreground = true;
        Th.apply(r.s.p.theme);
        if (r.s.p.onboarded) {
            Ev.List ev = r.e.onOpen(r.now());
            r.commit();
            celebrate(ev, null);
            showNotices();
            maybeMorning();
            Sheets.maybeRateFocus(this);
        }
        if (onboarding != null) onboarding.refreshPerms();
        Notifier.cancel(this, Notifier.ID_GROUP);
        for (int k = 1; k <= 5; k++) Notifier.cancel(this, Notifier.ID_SYS + k);
        if (r.s.p.focusOn) com.ilker.rutin.sys.FocusService.send(this, com.ilker.rutin.sys.FocusService.SYNC);
        refresh();
    }

    @Override
    protected void onPause() {
        super.onPause();
        r.foreground = false;
        r.save();
        Scheduler.reschedule(this, false);
        Widgets.updateAll(this);
    }

    @Override
    protected void onDestroy() {
        r.unlisten(onChange);
        r.unlistenEv(onEv);
        if (I == this) I = null;
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    void handleIntent(Intent in) {
        if (in == null || !r.s.p.onboarded) return;
        final String open = in.getStringExtra("open");
        final long task = in.getLongExtra("task", 0);
        in.removeExtra("open");
        if (open == null) return;
        root.postDelayed(() -> {
            switch (open) {
                case "task": {
                    go(0);
                    Task t = r.s.task(task);
                    if (t != null) Sheets.task(this, t, null);
                    Notifier.cancelTask(this, task);
                    break;
                }
                case "focus":
                    go(2);
                    if (task != 0 && !r.s.p.focusOn) Sheets.focusStart(this, task);
                    else if (r.s.p.focusOn) FocusOverlay.show(this);
                    break;
                case "summary": go(0); Sheets.summary(this); break;
                case "morning": go(0); Sheets.morning(this); break;
                case "plan": go(1); Sheets.planWeek(this); break;
                default: go(0);
            }
        }, 350);
    }

    void maybeMorning() {
        int td = r.e.today(r.now());
        int nd = r.e.nowDm(r.now());
        if (r.s.p.lastMorningShownDay != td && !r.s.day(td).started && nd < r.e.wakeDm() + 240 && nd >= r.e.wakeDm() - 90) {
            r.s.p.lastMorningShownDay = td;
            r.save();
            root.postDelayed(() -> Sheets.morning(this), 500);
        }
    }

    void showNotices() {
        if (r.s.notices.isEmpty()) return;
        final ArrayList<String> list = new ArrayList<>(r.s.notices);
        r.s.notices.clear();
        r.save();
        root.postDelayed(() -> Sheets.notices(this, list), 700);
    }

    @Override
    public void onBackPressed() {
        for (int i = overlay.getChildCount() - 1; i >= 0; i--) {
            View v = overlay.getChildAt(i);
            if (v instanceof Sheet) { ((Sheet) v).dismiss(); return; }
            if (fulls.contains(v)) { if (v instanceof FocusOverlay) ((FocusOverlay) v).close(); else closeFull(v); return; }
        }
        if (onboarding != null) { if (onboarding.back()) return; }
        else if (tab != 0) { go(0); return; }
        super.onBackPressed();
    }

    public void requestNotifPermission() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission("android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, REQ_NOTIF);
    }

    public boolean notifGranted() {
        return Build.VERSION.SDK_INT < 33 || checkSelfPermission("android.permission.POST_NOTIFICATIONS") == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (onboarding != null) onboarding.refreshPerms();
        Notifier.cancel(this, Notifier.ID_GROUP);
        for (int k = 1; k <= 5; k++) Notifier.cancel(this, Notifier.ID_SYS + k);
        if (r.s.p.focusOn) com.ilker.rutin.sys.FocusService.send(this, com.ilker.rutin.sys.FocusService.SYNC);
        refresh();
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (res != RESULT_OK || data == null || data.getData() == null) return;
        try {
            if (req == REQ_EXPORT) {
                try (java.io.OutputStream o = getContentResolver().openOutputStream(data.getData())) {
                    o.write(r.exportJson().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                }
                toast("💾 Yedek kaydedildi");
            } else if (req == REQ_IMPORT) {
                java.io.InputStream in = getContentResolver().openInputStream(data.getData());
                java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) > 0) bo.write(buf, 0, n);
                in.close();
                if (r.importJson(bo.toString("UTF-8"))) { toast("✅ Yedek geri yüklendi"); rebuild(); }
                else toast("⚠️ Dosya okunamadı");
            }
        } catch (Exception e) {
            toast("⚠️ Hata: " + e.getMessage());
        }
    }

    public ViewGroup rootView() { return root; }
}
