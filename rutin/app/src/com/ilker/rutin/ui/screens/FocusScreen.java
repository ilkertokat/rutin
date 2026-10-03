package com.ilker.rutin.ui.screens;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ilker.rutin.core.Habit;
import com.ilker.rutin.core.Insights;
import com.ilker.rutin.core.Planner;
import com.ilker.rutin.core.State;
import com.ilker.rutin.core.Task;
import com.ilker.rutin.core.Tm;
import com.ilker.rutin.sys.FocusService;
import com.ilker.rutin.ui.Art;
import com.ilker.rutin.ui.FocusOverlay;
import com.ilker.rutin.ui.MainActivity;
import com.ilker.rutin.ui.Th;
import com.ilker.rutin.ui.U;
import com.ilker.rutin.ui.Views;
import com.ilker.rutin.ui.sheets.Sheets;

import java.util.ArrayList;

/** Odak: günlük süre hedefleri, önerilen bloklar, istatistik ve odak ormanı. */
@SuppressLint("ViewConstructor")
public class FocusScreen extends Screen {
    public FocusScreen(MainActivity a) { super(a); }

    @Override
    protected void onInsets() { refresh(); }

    @Override
    public void refresh() {
        long now = now();
        int td = r.e.today(now);
        State.Profile p = r.s.p;
        col.removeAllViews();
        col.setPadding(U.dp(16), top + U.dp(14), U.dp(16), U.dp(24));
        col.addView(U.title(a, "Odak"));
        int todayMin = r.s.day(td).focusMin;
        col.addView(U.tv(a, "Bugün " + Tm.dur(todayMin) + " odaklandın" + (todayMin >= 60 ? " 🔥" : ""), 14, Th.dim, 700), U.mb(U.lpMW(), 14));

        if (p.focusOn) {
            LinearLayout live = U.card(a);
            live.setBackground(U.rrs(Art.alpha(Th.focus, 0.15f), 22, Th.focus, 1.5f));
            live.addView(U.tv(a, (p.focusPhase == 0 ? "🍅 Devam eden oturum · " : "☕ Mola · ") + (p.focusSubject.isEmpty() ? "Serbest" : p.focusSubject), 16, Th.text, 900));
            long left = FocusService.remainingMs(p);
            live.addView(U.tv(a, (left / 60000) + " dk kaldı · dokun ve devam et", 13, Th.dim, 700));
            live.setOnClickListener(v -> FocusOverlay.show(a));
            U.squish(live);
            col.addView(live, U.mb(U.lpMW(), 12));
        }
        if (p.focusRate >= 0) {
            TextView rate = U.chip(a, "⭐ Son oturumu puanla", Th.gold, 0xFF2A1E00);
            rate.setOnClickListener(v -> Sheets.maybeRateFocus(a));
            col.addView(rate, U.mb(U.lpWW(), 12));
        }

        // Süreli hedefler
        ArrayList<Task> durs = new ArrayList<>();
        for (Task t : r.e.sorted(td)) if (t.type == Habit.DURATION) durs.add(t);
        if (durs.isEmpty()) {
            LinearLayout e = U.card(a);
            e.addView(U.tv(a, "⏱️ Günlük süre hedefi ekle", 16, Th.text, 900));
            e.addView(U.tv(a, "\"Günde 4 saat ders çalışacağım\" gibi bir hedef koy; uygulama onu bloklara böler, boşluklarına yerleştirir ve zaman daraldıkça seni dürter.", 13, Th.dim, 700));
            U.Btn b = U.ghost(a, "+ Süreli hedef ekle");
            b.setTextSize(14);
            b.setOnClickListener(v -> Sheets.habitEditor(a, null, -1));
            e.addView(b, U.m(U.lpMW(), 0, 10, 0, 0));
            col.addView(e, U.mb(U.lpMW(), 12));
        }
        for (final Task t : durs) {
            LinearLayout c = U.card(a);
            LinearLayout row = U.h(a);
            Views.RingsView ring = new Views.RingsView(a);
            ring.set(new float[]{(float) t.ratio()}, new int[]{Th.focus}, Math.round(t.ratio() * 100) + "%", "");
            row.addView(ring, new LinearLayout.LayoutParams(U.dp(84), U.dp(84)));
            row.addView(U.space(a, 14));
            LinearLayout mid = U.v(a);
            mid.addView(U.tv(a, t.emoji + " " + t.title, 17, Th.text, 900));
            mid.addView(U.tv(a, Tm.durShort(t.progress) + " / " + Tm.durShort(t.full) + (t.mini > 0 ? " · mini " + Tm.durShort(t.mini) + (t.progress >= t.mini ? " ✓" : "") : ""), 13.5f, Th.dim, 700));
            int rem = Math.max(0, t.full - t.progress);
            mid.addView(U.tv(a, rem == 0 ? "✅ Bugünlük tamam!" : "Kalan: " + Tm.dur(rem) + " ≈ " + (int) Math.ceil(rem / 25.0) + " pomodoro", 13, rem == 0 ? Th.success : Th.text, 800));
            row.addView(mid, U.lpW(1));
            c.addView(row);
            if (rem > 0 && t.day == td) {
                U.Btn go = U.btn(a, "▶  BAŞLAT", Th.focus);
                go.setOnClickListener(v -> Sheets.focusStart(a, t.id));
                c.addView(go, U.m(U.lpMW(), 0, 12, 0, 0));
                final ArrayList<Integer> bl = Planner.suggestBlocks(r.e, t, now, 50, 10);
                if (!t.blocks.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (int b : t.blocks) sb.append(sb.length() > 0 ? ", " : "").append(Tm.hhmm(b));
                    c.addView(U.tv(a, "🗓️ Planlı bloklar: " + sb + " · zamanı gelince hatırlatırım", 12.5f, Th.focus, 800), U.m(U.lpMW(), 0, 10, 0, 0));
                } else if (!bl.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (int b : bl) sb.append(sb.length() > 0 ? ", " : "").append(Tm.hhmm(b));
                    TextView sug = U.chip(a, "💡 Boşlukların: " + sb + " · Plana ekle", Art.alpha(Th.focus, 0.15f), Th.focus);
                    sug.setOnClickListener(v -> { t.blocks.clear(); t.blocks.addAll(bl); r.commit(); a.toast("🗓️ Bloklar planlandı!"); });
                    c.addView(sug, U.m(U.lpMW(), 0, 10, 0, 0));
                }
            }
            col.addView(c, U.mb(U.lpMW(), 12));
        }
        U.Btn free = U.ghost(a, "🌀 Serbest odak oturumu");
        free.setOnClickListener(v -> Sheets.focusStart(a, 0));
        col.addView(free, U.mb(U.lpMW(), 8));

        // İstatistik
        col.addView(U.section(a, "ODAK İSTATİSTİĞİ"));
        LinearLayout st = U.h(a);
        int weekMin = 0, sessions = 0;
        for (State.FocusLog f : r.s.focus) if (f.day > td - 7) { weekMin += f.minutes; if (f.completed) sessions++; }
        int[] byHour = Insights.focusByHour(r.s, td - 30);
        int bh = Insights.bestHour(byHour);
        st.addView(tile("⏱️", Tm.durShort(weekMin), "bu hafta"), U.lpW(1));
        st.addView(tile("🍅", String.valueOf(sessions), "oturum (7g)"), U.lpW(1));
        st.addView(tile("🌟", bh < 0 ? "—" : String.format("%02d:00", bh), "altın saat"), U.lpW(1));
        col.addView(st);
        ArrayList<Object[]> subj = Insights.focusBySubject(r.s, td - 30);
        if (!subj.isEmpty()) {
            LinearLayout c = U.card(a);
            c.addView(U.tv(a, "Konulara göre (30 gün)", 14, Th.text, 900));
            int mx = 1;
            for (Object[] o : subj) mx = Math.max(mx, (Integer) o[1]);
            for (int i = 0; i < Math.min(6, subj.size()); i++) {
                Object[] o = subj.get(i);
                LinearLayout row = U.h(a);
                row.addView(U.tv(a, (String) o[0], 13, Th.text, 800), new LinearLayout.LayoutParams(U.dp(110), -2));
                Views.WaveBar b = new Views.WaveBar(a, Th.focus);
                b.set((Integer) o[1] / (float) mx, false);
                b.animating = false;
                row.addView(b, new LinearLayout.LayoutParams(0, U.dp(10), 1));
                row.addView(U.tv(a, "  " + Tm.durShort((Integer) o[1]), 12, Th.dim, 800));
                c.addView(row, U.m(U.lpMW(), 0, 8, 0, 0));
            }
            if (bh >= 0) c.addView(U.tv(a, "💡 En çok " + String.format("%02d:00", bh) + " civarında odaklanıyorsun. Zor işleri bu saate koy.", 12.5f, Th.dim, 700), U.m(U.lpMW(), 0, 10, 0, 0));
            col.addView(c, U.m(U.lpMW(), 10, 10, 0, 0));
        }

        // Odak ormanı
        col.addView(U.section(a, "🌳 ODAK ORMANIN"));
        ArrayList<State.FocusLog> trees = new ArrayList<>();
        for (State.FocusLog f : r.s.focus) if (f.day > td - 30) trees.add(f);
        Forest fv = new Forest(a, trees);
        LinearLayout fc = U.card(a);
        fc.setPadding(U.dp(8), U.dp(8), U.dp(8), U.dp(8));
        fc.addView(fv, new LinearLayout.LayoutParams(-1, U.dp(Math.max(120, ((trees.size() + 7) / 8) * 56 + 20))));
        int alive = 0;
        for (State.FocusLog f : trees) if (f.completed) alive++;
        fc.addView(U.tv(a, "  " + alive + " ağaç büyüttün, " + (trees.size() - alive) + " tanesi soldu (son 30 gün)", 12, Th.dim, 700));
        col.addView(fc, U.mb(U.lpMW(), 70));
    }

    View tile(String e, String v, String l) {
        LinearLayout c = U.v(a);
        c.setGravity(Gravity.CENTER);
        c.setBackground(U.rr(Th.surface, 18));
        c.setPadding(0, U.dp(12), 0, U.dp(12));
        TextView tv = U.tv(a, e + " " + v, 17, Th.text, 900);
        tv.setGravity(Gravity.CENTER);
        c.addView(tv);
        TextView tl = U.tv(a, l, 12, Th.dim, 700);
        tl.setGravity(Gravity.CENTER);
        c.addView(tl);
        LinearLayout w = U.v(a);
        w.setPadding(U.dp(3), 0, U.dp(3), 0);
        w.addView(c);
        return w;
    }

    static class Forest extends View {
        final ArrayList<State.FocusLog> trees;
        Forest(Context c, ArrayList<State.FocusLog> t) { super(c); trees = t; }
        @Override
        protected void onDraw(Canvas c) {
            if (trees.isEmpty()) {
                android.graphics.Paint p = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
                p.setColor(Th.dim); p.setTextSize(U.dp(14)); p.setTextAlign(android.graphics.Paint.Align.CENTER); p.setTypeface(U.font(700));
                c.drawText("İlk odak oturumunla ilk ağacını dik 🌱", getWidth() / 2f, getHeight() / 2f, p);
                return;
            }
            float cw = getWidth() / 8f;
            float rh = U.dp(56);
            for (int i = 0; i < trees.size(); i++) {
                State.FocusLog f = trees.get(i);
                float x = (i % 8) * cw + cw / 2, y = (i / 8) * rh + rh;
                android.graphics.Paint g = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
                g.setColor(Art.alpha(Th.success, 0.15f));
                c.drawOval(x - cw * 0.4f, y - U.dp(5), x + cw * 0.4f, y + U.dp(4), g);
                float size = U.dp(26 + Math.min(24, f.minutes / 4));
                Art.tree(c, x, y, size, f.tree >= 0 ? f.tree : 1, 1, !f.completed);
            }
        }
    }
}
