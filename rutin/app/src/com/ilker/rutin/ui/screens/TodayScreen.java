package com.ilker.rutin.ui.screens;

import android.annotation.SuppressLint;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ilker.rutin.core.Area;
import com.ilker.rutin.core.Coach;
import com.ilker.rutin.core.DayRec;
import com.ilker.rutin.core.Game;
import com.ilker.rutin.core.Habit;
import com.ilker.rutin.core.Insights;
import com.ilker.rutin.core.Quests;
import com.ilker.rutin.core.State;
import com.ilker.rutin.core.Task;
import com.ilker.rutin.core.Tm;
import com.ilker.rutin.ui.Art;
import com.ilker.rutin.ui.MainActivity;
import com.ilker.rutin.ui.TaskRow;
import com.ilker.rutin.ui.Th;
import com.ilker.rutin.ui.U;
import com.ilker.rutin.ui.Views;
import com.ilker.rutin.ui.sheets.Sheets;

import java.util.ArrayList;

/** Ana ekran: günün halkası, yoldaş, görevler ve zaman çizelgesi. */
@SuppressLint("ViewConstructor")
public class TodayScreen extends Screen {
    final LinearLayout header;
    final TextView fab;
    boolean doneExpanded;

    public TodayScreen(MainActivity a) {
        super(a);
        header = U.v(a);
        fab = U.tv(a, "+", 30, 0xFFFFFFFF, 900);
        fab.setGravity(Gravity.CENTER);
        fab.setBackground(U.rr(Th.primary, 50));
        fab.setElevation(U.dp(8));
        fab.setOnClickListener(v -> { U.haptic(v); Sheets.quickAdd(a, r.e.today(now())); });
        U.squish(fab);
        addView(fab, U.flp(U.dp(60), U.dp(60), Gravity.BOTTOM | Gravity.END));
        scroll.setOnScrollChangeListener((v, x, y, ox, oy) -> {
            if (y > oy + 8) fab.animate().translationY(U.dp(120)).setDuration(200).start();
            else if (y < oy - 8) fab.animate().translationY(0).setDuration(200).start();
        });
    }

    @Override
    protected void onInsets() {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) fab.getLayoutParams();
        lp.setMargins(0, 0, U.dp(18), bottom + U.dp(16));
        fab.setLayoutParams(lp);
        refresh();
    }

    @Override
    public void refresh() {
        long now = now();
        int td = r.e.today(now);
        r.e.ensureDay(td, now);
        State.Profile p = r.s.p;
        col.removeAllViews();
        int nd = r.e.nowDm(now);
        // ---- Başlık (günün saatine göre gökyüzü) ----
        int[] sky = Th.skyFor(nd);
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, sky);
        LinearLayout head = U.v(a);
        head.setPadding(U.dp(16), top + U.dp(14), U.dp(16), U.dp(10));
        head.setBackground(g);
        LinearLayout hr = U.h(a);
        LinearLayout hl = U.v(a);
        String greet = nd < 720 ? "Günaydın" : nd < 1080 ? "İyi günler" : nd < 1320 ? "İyi akşamlar" : "İyi geceler";
        String emo = nd < 720 ? "☀️" : nd < 1080 ? "🌤️" : nd < 1320 ? "🌆" : "🌙";
        hl.addView(U.tv(a, greet + (p.name.isEmpty() ? "" : ", " + p.name) + " " + emo, 22, Th.text, 900));
        hl.addView(U.tv(a, Tm.dateLong(td) + " · " + Area.EMOJI[Area.monthArea(Tm.month(td))] + " " + Area.MONTH_THEME[Tm.month(td) - 1], 13, Th.dim, 700));
        hr.addView(hl, U.lpW(1));
        // alev çipi
        int ds = r.e.displayStreak(now);
        boolean met = r.e.todayMet(now);
        LinearLayout fchip = U.h(a);
        fchip.setBackground(U.rr(Art.alpha(Th.surface, 0.85f), 50));
        fchip.setPadding(U.dp(8), U.dp(4), U.dp(12), U.dp(4));
        Views.FlameView fv = new Views.FlameView(a);
        fv.set(Game.flameStage(Math.max(ds, 1)), met);
        fchip.addView(fv, new LinearLayout.LayoutParams(U.dp(26), U.dp(30)));
        fchip.addView(U.tv(a, String.valueOf(ds), 18, met ? Art.flameColor(Game.flameStage(ds)) : Th.dim, 900));
        fchip.setOnClickListener(v -> Sheets.streakInfo(a));
        U.squish(fchip);
        hr.addView(fchip);
        hr.addView(U.space(a, 6));
        TextView coin = U.chip(a, "🪙 " + p.coins, Art.alpha(Th.surface, 0.85f), Th.gold);
        coin.setOnClickListener(v -> Sheets.shop(a, -1));
        U.squish(coin);
        hr.addView(coin);
        head.addView(hr);
        col.addView(head, U.m(new LinearLayout.LayoutParams(-1, -2), -16, 0, -16, 0));

        ArrayList<Task> all = r.e.sorted(td);
        DayRec rec = r.s.day(td);

        // ---- Kahraman kart: halkalar ----
        LinearLayout hero = U.card(a);
        LinearLayout heroRow = U.h(a);
        Views.RingsView rings = new Views.RingsView(a);
        int reqTotal = 0, reqDone = 0; int wFull = 0, wProg = 0; int dFull = 0, dProg = 0;
        String wUnit = "ml";
        for (Task t : r.e.required(td)) { reqTotal++; if (r.e.satisfied(t)) reqDone++; }
        for (Task t : all) {
            if (t.type == Habit.COUNTER && "ml".equals(t.unit)) { wFull += t.full; wProg += Math.min(t.progress, t.full); }
            if (t.type == Habit.DURATION) { dFull += t.full; dProg += Math.min(t.progress, t.full); }
        }
        ArrayList<Float> vals = new ArrayList<>();
        ArrayList<Integer> cols = new ArrayList<>();
        vals.add(reqTotal == 0 ? (met ? 1f : 0f) : reqDone / (float) reqTotal); cols.add(Th.primary);
        if (wFull > 0) { vals.add(wProg / (float) wFull); cols.add(Th.water); }
        if (dFull > 0) { vals.add(dProg / (float) dFull); cols.add(Th.focus); }
        float[] va = new float[vals.size()]; int[] ca = new int[vals.size()];
        for (int i = 0; i < va.length; i++) { va[i] = vals.get(i); ca[i] = cols.get(i); }
        int pct = (int) Math.round(rec.ratio * 100);
        rings.set(va, ca, pct + "%", "bugün");
        heroRow.addView(rings, new LinearLayout.LayoutParams(U.dp(128), U.dp(128)));
        heroRow.addView(U.space(a, 14));
        LinearLayout legend = U.v(a);
        legend.addView(legendRow("🔒", "Zorunlu", reqDone + " / " + reqTotal, Th.primary));
        if (wFull > 0) legend.addView(legendRow("💧", "Su", com.ilker.rutin.core.Fmt.amount(wProg, wUnit, Habit.COUNTER) + " / " + com.ilker.rutin.core.Fmt.amount(wFull, wUnit, Habit.COUNTER), Th.water));
        if (dFull > 0) legend.addView(legendRow("⏱️", "Odak", Tm.durShort(dProg) + " / " + Tm.durShort(dFull), Th.focus));
        int lv = Game.level(p.xp);
        TextView lvT = U.tv(a, "Lv " + lv + " · " + Game.title(lv), 13, Th.text, 800);
        lvT.setPadding(0, U.dp(8), 0, U.dp(4));
        legend.addView(lvT);
        Views.WaveBar xpb = new Views.WaveBar(a, Th.gold);
        xpb.set((float) Game.levelProgress(p.xp), false);
        xpb.animating = false;
        legend.addView(xpb, new LinearLayout.LayoutParams(-1, U.dp(8)));
        TextView goalT = U.tv(a, (rec.goalHit ? "✅" : "🎯") + " Günlük hedef " + Math.min(rec.xp, p.dailyGoal) + "/" + p.dailyGoal + " XP", 12.5f, rec.goalHit ? Th.success : Th.dim, 800);
        goalT.setPadding(0, U.dp(8), 0, U.dp(3));
        goalT.setOnClickListener(v -> com.ilker.rutin.ui.sheets.MeSheets.goalPicker(a));
        legend.addView(goalT);
        Views.WaveBar gb = new Views.WaveBar(a, rec.goalHit ? Th.success : Th.primary);
        gb.set(Math.min(1f, rec.xp / (float) Math.max(1, p.dailyGoal)), false);
        gb.animating = false;
        legend.addView(gb, new LinearLayout.LayoutParams(-1, U.dp(6)));
        heroRow.addView(legend, U.lpW(1));
        hero.addView(heroRow);
        // durum satırı
        String status;
        int statusColor;
        if (rec.perfect) { status = "💎 Mükemmel gün! Her şey tam."; statusColor = Th.gold; }
        else if (met) { status = "🔥 Serin güvende! Gerisi bonus."; statusColor = Th.success; }
        else {
            int left = 0;
            for (Task t : r.e.required(td)) if (!r.e.satisfied(t)) left++;
            status = left == 0 ? "İlk adımı at, alev yansın 🔥" : "Seriyi uzatmak için " + left + " iş kaldı · mini versiyonlar da sayılır";
            statusColor = Th.dim;
        }
        TextView st = U.tv(a, status, 13.5f, statusColor, 800);
        st.setPadding(0, U.dp(10), 0, 0);
        hero.addView(st);
        if (p.comebackDay == td) {
            TextView easy = U.chip(a, "🌱 Kolay Başlangıç Günü · sadece çapa rutinler · 2x XP", Art.alpha(Th.success, 0.18f), Th.success);
            hero.addView(easy, U.m(U.lpWW(), 0, 8, 0, 0));
        }
        if (p.boostUntil > now) hero.addView(U.chip(a, "⚗️ XP iksiri aktif · " + (p.boostUntil - now) + " dk", Art.alpha(Th.focus, 0.2f), Th.focus), U.m(U.lpWW(), 0, 8, 0, 0));
        col.addView(hero, U.m(U.lpMW(), 0, 4, 0, 12));

        // ---- Yoldaş balonu ----
        LinearLayout comp = U.h(a);
        Views.CompanionView cv = new Views.CompanionView(a);
        cv.species = p.companion; cv.hat = p.hat; cv.acc = p.acc;
        cv.mood = rec.perfect ? Art.MOOD_EXCITED : met ? Art.MOOD_HAPPY : nd > r.e.sleepDm() - 60 ? Art.MOOD_SLEEPY : p.comebackDay == td ? Art.MOOD_EXCITED : Art.MOOD_OK;
        comp.addView(cv, new LinearLayout.LayoutParams(U.dp(64), U.dp(64)));
        TextView bubble = U.tv(a, Coach.companionLine(r.e, now), 14, Th.text, 700);
        bubble.setBackground(U.rr(Th.surface2, 18));
        bubble.setPadding(U.dp(14), U.dp(10), U.dp(14), U.dp(10));
        comp.addView(bubble, U.lpW(1));
        comp.setOnClickListener(v -> a.go(4));
        col.addView(comp, U.mb(U.lpMW(), 10));

        // ---- Günaydın / Gün sonu kartları ----
        if (!rec.started && nd < r.e.wakeDm() + 300) {
            U.Btn b = U.btn(a, "☀️  GÜNE BAŞLA", Th.primary);
            b.setOnClickListener(v -> Sheets.morning(a));
            col.addView(b, U.mb(U.lpMW(), 12));
        }
        if (!rec.summaryDone && nd >= r.e.sleepDm() - 120) {
            U.Btn b = U.btn(a, "🌙  GÜNÜ KAPAT · ÖZET", Th.focus);
            b.setOnClickListener(v -> Sheets.summary(a));
            col.addView(b, U.mb(U.lpMW(), 12));
        }

        // ---- Uyarılar ve içgörüler ----
        if (p.repairUntil >= td) {
            col.addView(infoCard("🦅", "Seri onarma görevi", p.repairOld + " günlük serini geri kazan: " + p.repairProgress + "/2 günü tamamla (son gün: " + Tm.dateShort(p.repairUntil) + ")", Th.primary), U.mb(U.lpMW(), 10));
        }
        int load = r.e.estimatedLoad(td);
        int avail = Math.max(0, r.e.sleepDm() - Math.max(nd, r.e.wakeDm()));
        if (load > avail * 0.9 && load > 120 && nd >= r.e.wakeDm() && nd < r.e.sleepDm() - 60) {
            LinearLayout w = infoCard("⚠️", "Bugün çok yüklü", "Kalan işler ~" + Tm.dur(load) + ", kalan zaman " + Tm.dur(avail) + ". Bazılarını taşımak veya mini versiyona geçmek tükenmişliği önler.", Th.warn);
            w.setOnClickListener(v -> Sheets.disruption(a));
            col.addView(w, U.mb(U.lpMW(), 10));
        }
        for (State.Insight in : Insights.active(r.s)) col.addView(insight(in), U.mb(U.lpMW(), 10));
        if (com.ilker.rutin.core.Rewards.canSpin(r.e, now)) {
            LinearLayout sp = infoCard("🎡", "Şans çarkın hazır!", "Serini güvenceye aldın. Çevir ve ödülünü kap!", Th.primary);
            sp.setOnClickListener(v -> com.ilker.rutin.ui.SpinOverlay.show(a));
            U.squish(sp);
            col.addView(sp, U.mb(U.lpMW(), 10));
            sp.postDelayed(() -> U.bounce(sp), 400);
        }
        if (!r.s.chests.isEmpty()) {
            LinearLayout ch = infoCard("🎁", r.s.chests.size() + " sandık seni bekliyor!", "Aç ve ödüllerini topla", Th.gold);
            ch.setOnClickListener(v -> Sheets.chests(a));
            U.squish(ch);
            col.addView(ch, U.mb(U.lpMW(), 10));
        }

        // ---- Günlük görevler ----
        LinearLayout q = U.card(a);
        LinearLayout qh = U.h(a);
        qh.addView(U.tv(a, "🎯 Günlük Görevler", 16, Th.text, 900), U.lpW(1));
        int qd = 0;
        for (State.Quest x : r.s.quests) if (x.done) qd++;
        qh.addView(U.tv(a, qd + "/3 · 🎁", 13, qd == 3 ? Th.gold : Th.dim, 800));
        q.addView(qh);
        for (State.Quest x : r.s.quests) q.addView(questRow(x), U.m(U.lpMW(), 0, 8, 0, 0));
        if (p.weekly != null) {
            View div = new View(a);
            div.setBackgroundColor(Th.stroke);
            q.addView(div, U.m(new LinearLayout.LayoutParams(-1, U.dp(1)), 0, 12, 0, 4));
            TextView wt = U.tv(a, "🏅 Haftalık meydan okuma", 13, Th.dim, 800);
            q.addView(wt, U.m(U.lpMW(), 0, 6, 0, 0));
            q.addView(questRow(p.weekly), U.m(U.lpMW(), 0, 6, 0, 0));
        }
        col.addView(q, U.mb(U.lpMW(), 6));

        // ---- Kendinle yarış ----
        int[] gh = com.ilker.rutin.core.Rewards.ghost(r.s, td);
        if (gh[1] > 0 || gh[0] > 0) {
            LinearLayout gc = U.card(a);
            gc.addView(U.tv(a, "🏁 Geçen haftaki senle yarış", 15, Th.text, 900));
            int mx = Math.max(1, Math.max(gh[0], gh[1]));
            gc.addView(raceRow("Sen", gh[0], mx, Th.primary), U.m(U.lpMW(), 0, 8, 0, 0));
            gc.addView(raceRow("👻 Geçen hafta", gh[1], mx, Th.dim), U.m(U.lpMW(), 0, 6, 0, 0));
            int diff = gh[0] - gh[1];
            gc.addView(U.tv(a, diff >= 0 ? "🚀 Geçen haftaki seni " + diff + " XP geçiyorsun!" : "💪 Geçen haftaki senin " + (-diff) + " XP gerisindesin. Yakala!", 13, diff >= 0 ? Th.success : Th.warn, 800), U.m(U.lpMW(), 0, 8, 0, 0));
            col.addView(gc, U.m(U.lpMW(), 0, 10, 0, 0));
        }

        // ---- Zaman çizelgesi ----
        ArrayList<Task> nowL = new ArrayList<>(), next = new ArrayList<>(), allDay = new ArrayList<>(), flex = new ArrayList<>(), done = new ArrayList<>();
        for (Task t : all) {
            if (!t.isOpen()) { done.add(t); continue; }
            if (t.type == Habit.FLEX) { flex.add(t); continue; }
            int tg = r.e.targetDm(t);
            if (tg < 0) allDay.add(t);
            else if (tg <= nd + 20) nowL.add(t);
            else next.add(t);
        }
        if (all.isEmpty()) {
            LinearLayout e = infoCard("🌱", "Bugün için plan yok", "+ butonuyla alışkanlık ya da tek seferlik iş ekle. Küçük başla!", Th.success);
            col.addView(e, U.m(U.lpMW(), 0, 12, 0, 0));
        }
        section("ŞİMDİ", nowL);
        section("GÜN BOYU", allDay);
        section("SIRADA", next);
        section("ESNEK · BOŞ ZAMANINDA", flex);
        if (!done.isEmpty()) {
            TextView dh = U.section(a, "TAMAMLANDI (" + done.size() + ")  " + (doneExpanded ? "▾" : "▸"));
            dh.setOnClickListener(v -> { doneExpanded = !doneExpanded; refresh(); });
            col.addView(dh);
            if (doneExpanded) for (Task t : done) col.addView(new TaskRow(a, t, true), U.mb(U.lpMW(), 8));
        }
        // ---- Plan dışı olay ----
        LinearLayout bottomRow = U.h(a);
        TextView shield = U.chip(a, "🛡️ Plan dışı olay", Th.surface2, Th.text);
        shield.setPadding(U.dp(16), U.dp(12), U.dp(16), U.dp(12));
        shield.setOnClickListener(v -> Sheets.disruption(a));
        U.squish(shield);
        bottomRow.addView(shield);
        bottomRow.addView(U.space(a, 8));
        if (p.shieldUntil > now) bottomRow.addView(U.tv(a, "🔕 " + Tm.hhmm((int) (p.shieldUntil % 1440)) + "'e kadar sessiz", 12, Th.dim, 700));
        bottomRow.setGravity(Gravity.CENTER);
        col.addView(bottomRow, U.m(U.lpMW(), 0, 18, 0, 8));
        TextView quote = U.tv(a, "“" + Coach.quote(td) + "”", 13, Th.dim, 600);
        quote.setGravity(Gravity.CENTER);
        quote.setPadding(U.dp(20), U.dp(8), U.dp(20), U.dp(70));
        col.addView(quote);
    }

    void section(String title, ArrayList<Task> list) {
        if (list.isEmpty()) return;
        col.addView(U.section(a, title));
        for (Task t : list) col.addView(new TaskRow(a, t, true), U.mb(U.lpMW(), 8));
    }

    View raceRow(String name, int v, int mx, int color) {
        LinearLayout l = U.h(a);
        l.addView(U.tv(a, name, 12.5f, Th.dim, 800), new LinearLayout.LayoutParams(U.dp(96), -2));
        Views.WaveBar b = new Views.WaveBar(a, color);
        b.set(v / (float) mx, false);
        b.animating = false;
        l.addView(b, new LinearLayout.LayoutParams(0, U.dp(10), 1));
        l.addView(U.tv(a, "  " + v + " XP", 12.5f, Th.text, 900));
        return l;
    }

    LinearLayout legendRow(String emoji, String label, String val, int color) {
        LinearLayout l = U.h(a);
        View dot = new View(a);
        dot.setBackground(U.rr(color, 5));
        l.addView(dot, new LinearLayout.LayoutParams(U.dp(10), U.dp(10)));
        l.addView(U.space(a, 8));
        l.addView(U.tv(a, emoji + " " + label, 13.5f, Th.dim, 700), U.lpW(1));
        l.addView(U.tv(a, val, 14, Th.text, 900));
        l.setPadding(0, U.dp(3), 0, U.dp(3));
        return l;
    }

    LinearLayout infoCard(String emoji, String title, String text, int color) {
        LinearLayout c = U.h(a);
        c.setBackground(U.rrs(Art.alpha(color, 0.1f), 20, Art.alpha(color, 0.35f), 1));
        c.setPadding(U.dp(14), U.dp(12), U.dp(14), U.dp(12));
        TextView e = U.tv(a, emoji, 26, Th.text, 400);
        c.addView(e);
        c.addView(U.space(a, 12));
        LinearLayout v = U.v(a);
        v.addView(U.tv(a, title, 15, Th.text, 900));
        v.addView(U.tv(a, text, 13, Th.dim, 700));
        c.addView(v, U.lpW(1));
        return c;
    }

    View insight(final State.Insight in) {
        LinearLayout c = U.card(a);
        c.setBackground(U.rrs(Art.alpha(Th.focus, 0.1f), 20, Art.alpha(Th.focus, 0.35f), 1));
        c.addView(U.tv(a, "💡 " + in.text, 14, Th.text, 700));
        LinearLayout btns = U.h(a);
        btns.setGravity(Gravity.END);
        TextView no = U.chip(a, "Şimdilik hayır", Th.surface2, Th.dim);
        no.setOnClickListener(v -> { in.dismissed = true; r.commit(); });
        TextView yes = U.chip(a, in.kind == Insights.K_MINI_SUGGEST ? "Düzenle" : "Evet, uygula", Th.focus, 0xFFFFFFFF);
        yes.setOnClickListener(v -> {
            if (in.kind == Insights.K_MINI_SUGGEST) {
                in.dismissed = true;
                Habit h = r.s.habit(in.habitId);
                if (h != null) Sheets.habitEditor(a, h, -1);
                r.commit();
            } else {
                Insights.apply(r.e, in, now());
                a.toast("✅ Uygulandı");
                r.commit();
            }
        });
        btns.addView(no);
        btns.addView(U.space(a, 8));
        btns.addView(yes);
        c.addView(btns, U.m(U.lpMW(), 0, 10, 0, 0));
        return c;
    }

    View questRow(State.Quest x) {
        LinearLayout l = U.h(a);
        TextView e = U.tv(a, x.done ? "✅" : Quests.emoji(x.kind), 18, Th.text, 400);
        l.addView(e, new LinearLayout.LayoutParams(U.dp(30), -2));
        LinearLayout v = U.v(a);
        TextView t = U.tv(a, Quests.text(x), 14, x.done ? Th.dim : Th.text, 800);
        v.addView(t);
        Views.WaveBar b = new Views.WaveBar(a, x.done ? Th.success : Th.gold);
        b.set(x.target == 0 ? 0 : x.progress / (float) x.target, false);
        b.animating = false;
        v.addView(b, U.m(new LinearLayout.LayoutParams(-1, U.dp(6)), 0, 4, 0, 0));
        l.addView(v, U.lpW(1));
        l.addView(U.space(a, 10));
        String prog = x.kind == Quests.Q_WATER_NOON ? com.ilker.rutin.core.Fmt.amount(x.progress, "ml", Habit.COUNTER) : x.kind == Quests.W_FOCUS || x.kind == Quests.Q_FOCUS45 ? Tm.durShort(x.progress) : String.valueOf(x.progress);
        String tgt = x.kind == Quests.Q_WATER_NOON ? com.ilker.rutin.core.Fmt.amount(x.target, "ml", Habit.COUNTER) : x.kind == Quests.W_FOCUS || x.kind == Quests.Q_FOCUS45 ? Tm.durShort(x.target) : String.valueOf(x.target);
        l.addView(U.tv(a, prog + "/" + tgt, 12, Th.dim, 800));
        return l;
    }
}
