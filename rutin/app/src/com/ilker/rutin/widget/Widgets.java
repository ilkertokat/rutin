package com.ilker.rutin.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.widget.RemoteViews;

import com.ilker.rutin.R;
import com.ilker.rutin.core.DayRec;
import com.ilker.rutin.core.Game;
import com.ilker.rutin.core.Habit;
import com.ilker.rutin.core.Task;
import com.ilker.rutin.core.Tm;
import com.ilker.rutin.data.Repo;
import com.ilker.rutin.sys.ActionReceiver;
import com.ilker.rutin.sys.Notifier;
import com.ilker.rutin.ui.Art;

import java.util.ArrayList;

/** Widget çizimi ve güncelleme. */
public final class Widgets {
    private Widgets() {}

    public static void updateAll(Context c) {
        AppWidgetManager m = AppWidgetManager.getInstance(c);
        if (m == null) return;
        update(c, m, StreakWidget.class);
        update(c, m, WaterWidget.class);
        update(c, m, TodayWidget.class);
    }

    static void update(Context c, AppWidgetManager m, Class<? extends AppWidgetProvider> cls) {
        int[] ids = m.getAppWidgetIds(new ComponentName(c, cls));
        if (ids == null || ids.length == 0) return;
        for (int id : ids) {
            RemoteViews rv;
            if (cls == StreakWidget.class) rv = streak(c);
            else if (cls == WaterWidget.class) rv = water(c);
            else rv = today(c);
            try { m.updateAppWidget(id, rv); } catch (Throwable ignored) {}
        }
    }

    static Typeface font(Context c) {
        try { return new Typeface.Builder(c.getAssets(), "Nunito.ttf").setFontVariationSettings("'wght' 900").build(); }
        catch (Throwable t) { return Typeface.DEFAULT_BOLD; }
    }

    // ---------------- Seri ----------------
    static RemoteViews streak(Context c) {
        Repo r = Repo.get(c);
        long now = r.now();
        r.e.ensureDay(r.e.today(now), now);
        int ds = r.e.displayStreak(now);
        boolean met = r.e.todayMet(now);
        DayRec rec = r.s.dayOrNull(r.e.today(now));
        double ratio = rec == null ? 0 : rec.ratio;
        int S = 360;
        Bitmap b = Bitmap.createBitmap(S, S, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(b);
        int stage = Game.flameStage(Math.max(ds, 1));
        Art.ring(cv, S / 2f, S / 2f, S * 0.43f, S * 0.05f, (float) ratio, met ? Art.flameColor(stage) : 0xFF6A6A80, 0x22FFFFFF);
        Art.flame(cv, S / 2f, S * 0.6f, S * 0.36f, stage, met, 0.4f);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setTypeface(font(c));
        p.setTextAlign(Paint.Align.CENTER);
        p.setColor(0xFFFFFFFF);
        p.setTextSize(S * 0.2f);
        p.setShadowLayer(8, 0, 2, 0x88000000);
        cv.drawText(String.valueOf(ds), S / 2f, S * 0.8f, p);
        p.setTextSize(S * 0.065f);
        p.setColor(met ? 0xFFFFD27A : 0xFFB0B0C0);
        cv.drawText(met ? "gün seri 🔥" : "bugünü tamamla", S / 2f, S * 0.89f, p);
        RemoteViews rv = new RemoteViews(c.getPackageName(), R.layout.widget_streak);
        rv.setImageViewBitmap(R.id.img, b);
        rv.setOnClickPendingIntent(R.id.root, Notifier.openApp(c, "today", 0, 960));
        return rv;
    }

    // ---------------- Su ----------------
    static RemoteViews water(Context c) {
        Repo r = Repo.get(c);
        long now = r.now();
        r.e.ensureDay(r.e.today(now), now);
        Task w = null;
        for (Task t : r.s.tasksOf(r.e.today(now))) if (t.type == Habit.COUNTER && "ml".equals(t.unit)) { w = t; break; }
        if (w == null) for (Task t : r.s.tasksOf(r.e.today(now))) if (t.type == Habit.COUNTER) { w = t; break; }
        int W = 300, H = 300;
        Bitmap b = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setTypeface(font(c));
        RemoteViews rv = new RemoteViews(c.getPackageName(), R.layout.widget_water);
        if (w == null) {
            p.setColor(0xFFB0B0C0); p.setTextSize(28); p.setTextAlign(Paint.Align.CENTER);
            cv.drawText("Sayaçlı hedef yok", W / 2f, H / 2f, p);
            rv.setTextViewText(R.id.btn1, "Aç");
            rv.setTextViewText(R.id.btn2, "");
        } else {
            float frac = (float) w.ratio();
            Art.bottle(cv, new RectF(W * 0.08f, H * 0.06f, W * 0.42f, H * 0.94f), frac, 0xFF4FB3FF, 0x334FB3FF, 0.5f);
            p.setColor(0xFFFFFFFF);
            p.setTextSize(W * 0.14f);
            cv.drawText(w.amount(w.progress), W * 0.48f, H * 0.42f, p);
            p.setTextSize(W * 0.075f);
            p.setColor(0xFF9FD4FF);
            cv.drawText("/ " + w.amount(w.full), W * 0.48f, H * 0.55f, p);
            p.setTextSize(W * 0.07f);
            p.setColor(w.progress >= w.full ? 0xFF3DDC84 : w.mini > 0 && w.progress >= w.mini ? 0xFFFFB84D : 0xFFB0B0C0);
            cv.drawText(w.progress >= w.full ? "tamam 🎉" : w.mini > 0 && w.progress >= w.mini ? "seri güvende" : Math.round(frac * 100) + "%", W * 0.48f, H * 0.7f, p);
            rv.setTextViewText(R.id.btn1, "+" + w.amount(w.step));
            rv.setTextViewText(R.id.btn2, "+" + w.amount(w.step * 2));
            rv.setOnClickPendingIntent(R.id.btn1, Notifier.action(c, ActionReceiver.WATER, 0, w.step, 961));
            rv.setOnClickPendingIntent(R.id.btn2, Notifier.action(c, ActionReceiver.WATER, 0, w.step * 2, 962));
        }
        rv.setImageViewBitmap(R.id.img, b);
        rv.setOnClickPendingIntent(R.id.img, Notifier.openApp(c, "today", 0, 963));
        return rv;
    }

    // ---------------- Bugün ----------------
    static final int[] ROW = {R.id.row0, R.id.row1, R.id.row2};
    static final int[] TXT = {R.id.txt0, R.id.txt1, R.id.txt2};
    static final int[] CHK = {R.id.chk0, R.id.chk1, R.id.chk2};

    static RemoteViews today(Context c) {
        Repo r = Repo.get(c);
        long now = r.now();
        int td = r.e.today(now);
        r.e.ensureDay(td, now);
        RemoteViews rv = new RemoteViews(c.getPackageName(), R.layout.widget_today);
        int ds = r.e.displayStreak(now);
        rv.setTextViewText(R.id.title, "Bugün · " + Tm.DAY_LONG[Tm.weekday(td)]);
        rv.setTextViewText(R.id.streak, (r.e.todayMet(now) ? "🔥 " : "🕯️ ") + ds);
        ArrayList<Task> open = new ArrayList<>();
        int total = 0, done = 0;
        for (Task t : r.e.sorted(td)) {
            if (t.streak) { total++; if (r.e.satisfied(t)) done++; }
            if (t.isOpen() && t.type != Habit.DURATION) open.add(t);
        }
        rv.setTextViewText(R.id.sub, open.isEmpty() ? "Hepsi tamam! 🎉" : done + "/" + total + " zorunlu tamam · sıradakiler:");
        rv.setOnClickPendingIntent(R.id.root, Notifier.openApp(c, "today", 0, 964));
        for (int i = 0; i < 3; i++) {
            if (i < open.size()) {
                Task t = open.get(i);
                String tm = t.targetMin >= 0 ? Tm.hhmm(t.targetMin) + "  " : "";
                String extra = t.type == Habit.COUNTER ? "  " + t.amount(t.progress) + "/" + t.amount(t.full) : "";
                rv.setViewVisibility(ROW[i], android.view.View.VISIBLE);
                rv.setTextViewText(TXT[i], tm + t.emoji + " " + t.title + extra);
                rv.setTextViewText(CHK[i], t.type == Habit.COUNTER ? "＋" : "○");
                rv.setOnClickPendingIntent(CHK[i], Notifier.action(c, ActionReceiver.WTOGGLE, t.id, 0, 970 + i));
                rv.setOnClickPendingIntent(TXT[i], Notifier.openApp(c, "task", t.id, 975 + i));
            } else rv.setViewVisibility(ROW[i], android.view.View.GONE);
        }
        return rv;
    }
}
