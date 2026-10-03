package com.ilker.rutin.ui.sheets;

import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ilker.rutin.core.Coach;
import com.ilker.rutin.core.DayRec;
import com.ilker.rutin.core.Ev;
import com.ilker.rutin.core.Fmt;
import com.ilker.rutin.core.Habit;
import com.ilker.rutin.core.Insights;
import com.ilker.rutin.core.Quests;
import com.ilker.rutin.core.State;
import com.ilker.rutin.core.Task;
import com.ilker.rutin.core.Tm;
import com.ilker.rutin.data.Repo;
import com.ilker.rutin.ui.Art;
import com.ilker.rutin.ui.MainActivity;
import com.ilker.rutin.ui.Sheet;
import com.ilker.rutin.ui.Th;
import com.ilker.rutin.ui.U;
import com.ilker.rutin.ui.Views;

import java.util.ArrayList;

/** Günaydın, gün sonu özeti, haftalık planlama, haftalık rapor, ay özeti. */
public final class DaySheets {
    private DaySheets() {}

    // ---------------- Günaydın ----------------
    public static void morning(final MainActivity a) {
        final Repo r = a.r;
        final long now = r.now();
        final int td = r.e.today(now);
        final DayRec rec = r.s.day(td);
        final Sheet s = new Sheet(a, null, true);
        State.Profile p = r.s.p;
        TextView hi = U.tv(a, "Günaydın" + (p.name.isEmpty() ? "" : " " + p.name) + " ☀️", 28, Th.text, 900);
        s.add(hi, U.m(U.lpMW(), 0, 10, 0, 0));
        s.add(U.tv(a, Tm.dateLong(td) + " · Yeni bir gün, yeni bir sayfa.", 14, Th.dim, 700));
        LinearLayout comp = U.h(a);
        Views.CompanionView cv = new Views.CompanionView(a);
        cv.species = p.companion; cv.mood = Art.MOOD_HAPPY; cv.hat = p.hat; cv.acc = p.acc;
        comp.addView(cv, new LinearLayout.LayoutParams(U.dp(90), U.dp(90)));
        TextView q = U.tv(a, "“" + Coach.quote(td) + "”", 16, Th.text, 800);
        q.setBackground(U.rr(Th.surface2, 18));
        q.setPadding(U.dp(14), U.dp(12), U.dp(14), U.dp(12));
        comp.addView(q, U.lpW(1));
        s.add(comp, U.m(U.lpMW(), 0, 14, 0, 0));
        // seri
        int ds = r.e.displayStreak(now);
        s.add(Sheets.note(a, "🔥 Serin: " + ds + " gün", r.e.todayMet(now) ? "Bugün zaten güvende!" : "Bugünün gereksinimlerini tamamla, " + (r.s.p.streak + 1) + ". güne ulaş.", Th.primary), U.m(U.lpMW(), 0, 12, 0, 0));
        // dünkü niyet
        DayRec prev = r.s.dayOrNull(td - 1);
        if (prev != null && prev.intention != null && !prev.intention.isEmpty())
            s.add(Sheets.note(a, "🎯 Dün gece kendine söz verdin:", prev.intention, Th.success), U.m(U.lpMW(), 0, 8, 0, 0));
        // kimlik
        if (!p.identities.isEmpty())
            s.add(Sheets.note(a, "🪞 Kimliğin", p.identities.get(Math.floorMod(td, p.identities.size())), Th.focus), U.m(U.lpMW(), 0, 8, 0, 0));
        // 3 öncelik
        s.add(Sheets.label(a, "BUGÜNÜN 3 ÖNCELİĞİ"));
        ArrayList<Task> pri = new ArrayList<>();
        for (Task t : r.e.sorted(td)) if (t.isOpen() && t.streak && (t.priority == Habit.P_CRITICAL)) pri.add(t);
        for (Task t : r.e.sorted(td)) if (pri.size() < 3 && t.isOpen() && t.streak && !pri.contains(t)) pri.add(t);
        if (pri.isEmpty()) s.add(U.tv(a, "Bugün için zorunlu iş yok. Keyfini çıkar 🌿", 14, Th.dim, 700));
        for (Task t : pri) {
            LinearLayout row = U.row(a, t.emoji, t.title, (t.targetMin >= 0 ? Tm.hhmm(t.targetMin) + " · " : "") + (t.isQuantity() ? t.amount(t.full) : t.hasMini() ? "mini: " + t.miniText : Habit.TYPE_NAME[t.type]), Th.primary);
            row.setBackground(U.rr(Th.surface2, 16));
            s.add(row, U.mb(U.lpMW(), 6));
        }
        // görevler
        s.add(Sheets.label(a, "🎯 GÜNÜN GÖREVLERİ"));
        for (State.Quest x : r.s.quests) s.add(U.tv(a, Quests.emoji(x.kind) + "  " + Quests.text(x), 14, Th.text, 700), U.mb(U.lpMW(), 4));
        U.Btn go = U.btn(a, rec.started ? "HADİ BAŞLAYALIM" : "☀️  GÜNE BAŞLA", Th.primary);
        go.setOnClickListener(v -> {
            Ev.List ev = r.e.startDay(r.now());
            a.celebrate(ev, v);
            if (!ev.isEmpty()) a.confettiAt(v, 40);
            r.commit();
            s.dismiss();
        });
        s.add(go, U.m(U.lpMW(), 0, 22, 0, 0));
        a.show(s);
    }

    // ---------------- Gün sonu ----------------
    public static void summary(final MainActivity a) {
        final Repo r = a.r;
        final long now = r.now();
        final int td = r.e.today(now);
        final DayRec rec = r.s.day(td);
        r.e.refreshDay(td, now, null);
        final Sheet s = new Sheet(a, null, true);
        s.add(U.tv(a, "✨ Günün özeti", 26, Th.text, 900), U.m(U.lpMW(), 0, 10, 0, 0));
        s.add(U.tv(a, Tm.dateLong(td), 14, Th.dim, 700));
        // halka
        Views.RingsView rv = new Views.RingsView(a);
        rv.set(new float[]{(float) rec.ratio}, new int[]{rec.perfect ? Th.gold : Th.primary}, Math.round(rec.ratio * 100) + "%", rec.perfect ? "mükemmel" : rec.met ? "seri güvende" : "tamamlandı");
        LinearLayout.LayoutParams rl = new LinearLayout.LayoutParams(U.dp(170), U.dp(170));
        rl.gravity = Gravity.CENTER_HORIZONTAL;
        s.add(rv, U.m(rl, 0, 14, 0, 8));
        // en iyi an (zirve-son kuralı)
        String best = bestMoment(r, td);
        s.add(Sheets.note(a, "🌟 Günün en iyi anı", best, Th.gold), U.m(U.lpMW(), 0, 10, 0, 0));
        LinearLayout stats = U.h(a);
        stats.addView(Sheets.stat(a, "⭐", "+" + rec.xp, "XP"), U.lpW(1));
        stats.addView(Sheets.stat(a, "✅", String.valueOf(rec.done + rec.mini), "görev"), U.lpW(1));
        stats.addView(Sheets.stat(a, "⏱️", Tm.durShort(rec.focusMin), "odak"), U.lpW(1));
        stats.addView(Sheets.stat(a, "🔥", String.valueOf(r.e.displayStreak(now)), "seri"), U.lpW(1));
        s.add(stats, U.m(U.lpMW(), 0, 12, 0, 0));
        // yapılmayanlar
        ArrayList<Task> open = new ArrayList<>();
        for (Task t : r.s.tasksOf(td)) if (t.isOpen() && t.type != Habit.FLEX) open.add(t);
        if (!open.isEmpty()) {
            s.add(Sheets.label(a, "KALANLAR · suçluluk yok, sadece karar"));
            for (final Task t : open) {
                LinearLayout row = U.h(a);
                row.addView(U.tv(a, t.emoji + " " + t.title, 14, Th.text, 700), U.lpW(1));
                if (t.hasMini() && !t.isQuantity()) {
                    TextView m = U.chip(a, "🟡 Mini yaptım", Art.alpha(Th.warn, 0.2f), Th.warn);
                    m.setOnClickListener(v -> { Ev.List ev = r.e.complete(t.id, true, r.now()); a.celebrate(ev, v); r.commit(); s.dismiss(); summary(a); });
                    row.addView(m);
                    row.addView(U.space(a, 6));
                }
                if (!t.isQuantity() && t.habitId == 0 || t.type == Habit.ONCE) {
                    TextView mv = U.chip(a, "Yarına taşı", Th.surface2, Th.dim);
                    mv.setOnClickListener(v -> { r.e.move(t.id, td + 1, t.targetMin, r.now()); r.commit(); v.setAlpha(0.4f); v.setEnabled(false); });
                    row.addView(mv);
                } else if (t.isQuantity()) row.addView(U.tv(a, t.amount(t.progress) + " / " + t.amount(t.full), 12, Th.dim, 800));
                s.add(row, U.m(U.lpMW(), 0, 4, 0, 4));
            }
        }
        // ruh hali
        s.add(Sheets.label(a, "BUGÜN NASIL HİSSETTİN?"));
        final int[] mood = {rec.mood};
        LinearLayout moods = U.h(a);
        moods.setGravity(Gravity.CENTER);
        final String[] me = {"😞", "😐", "🙂", "😄", "🤩"};
        final ArrayList<TextView> mv = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final int k = i + 1;
            TextView t = U.tv(a, me[i], 34, Th.text, 400);
            t.setGravity(Gravity.CENTER);
            t.setBackground(k == mood[0] ? U.rr(Art.alpha(Th.primary, 0.3f), 50) : null);
            t.setOnClickListener(v -> { mood[0] = k; for (TextView x : mv) x.setBackground(x == v ? U.rr(Art.alpha(Th.primary, 0.3f), 50) : null); U.bounce(v); U.haptic(v); });
            mv.add(t);
            moods.addView(t, new LinearLayout.LayoutParams(U.dp(58), U.dp(58)));
        }
        s.add(moods);
        final EditText note = U.input(a, "Bugün ne öğrendin / neye minnettarsın? (isteğe bağlı)", rec.reflection, true);
        s.add(note, U.m(U.lpMW(), 0, 12, 0, 0));
        s.add(Sheets.label(a, "🎯 YARIN İLK İŞİM..."));
        final EditText intent = U.input(a, "Örn: Kalkınca 1 bardak su ve 25 dk ders", rec.intention, false);
        s.add(intent);
        U.Btn gn = U.btn(a, "🌙  İYİ GECELER", Th.focus);
        gn.setOnClickListener(v -> {
            if (mood[0] == 0) { a.toast("Bir ruh hali seç 🙂"); return; }
            Ev.List ev = r.e.reflect(mood[0], note.getText().toString().trim(), intent.getText().toString().trim(), r.now());
            ev.addAll(r.e.goodnight(r.now()));
            a.celebrate(ev, v);
            r.commit();
            s.dismiss();
            a.banner("🌙 İyi geceler" + (r.s.p.name.isEmpty() ? "" : " " + r.s.p.name) + "! Yarın görüşürüz 💫", Th.focus, 0xFFFFFFFF);
        });
        s.add(gn, U.m(U.lpMW(), 0, 20, 0, 0));
        a.show(s);
    }

    static String bestMoment(Repo r, int td) {
        DayRec rec = r.s.day(td);
        if (rec.perfect) return "Mükemmel Gün yaşadın: her şey tam versiyonuyla! 💎";
        Task early = null, big = null;
        for (Task t : r.s.tasksOf(td)) {
            if (t.early && early == null) early = t;
            if (t.status == Task.DONE && t.isQuantity() && big == null) big = t;
        }
        if (rec.combo >= 3) return rec.combo + "'li kombo yaptın, üst üste işleri devirdin 🎯";
        if (big != null) return big.emoji + " " + big.title + " hedefini tamamladın (" + big.amount(big.full) + ")!";
        if (early != null) return early.emoji + " " + early.title + " işini erkenden bitirdin 🐦";
        if (rec.focusMin >= 25) return Tm.dur(rec.focusMin) + " odaklandın, zihnin çalıştı 🧠";
        if (rec.met) return "Serini bir gün daha uzattın 🔥";
        if (rec.done + rec.mini > 0) return (rec.done + rec.mini) + " iş tamamladın. Her adım sayılır 🌱";
        return "Bugün zor bir gündü ama buradasın ve günü değerlendiriyorsun. Bu da bir adım 💛";
    }

    // ---------------- Haftalık planlama ----------------
    public static void planWeek(final MainActivity a) {
        final Repo r = a.r;
        final long now = r.now();
        final int td = r.e.today(now);
        final int nextWs = Tm.weekday(td) >= 4 ? Tm.weekStart(td) + 7 : Tm.weekStart(td);
        final Sheet s = new Sheet(a, "🗺️ Haftayı planla", true);
        Insights.Week w = Insights.week(r.e, Tm.weekStart(td) - (nextWs == Tm.weekStart(td) ? 7 : 0));
        s.add(Sheets.label(a, "1 · GEÇEN HAFTAYA BAKIŞ"));
        s.add(weekCard(a, w));
        s.add(Sheets.label(a, "2 · ŞABLON SEÇ"));
        final long[] tpl = {r.s.p.activeTemplate};
        Views.Flow tf = new Views.Flow(a);
        final ArrayList<TextView> ts = new ArrayList<>();
        TextView cur = U.pill(a, "📋 Mevcut plan", tpl[0] == 0);
        cur.setOnClickListener(v -> { tpl[0] = 0; for (TextView x : ts) U.setPill(x, x == v); });
        ts.add(cur);
        tf.addView(cur);
        for (final State.Template t : r.s.templates) {
            TextView p = U.pill(a, t.emoji + " " + t.name, t.id == tpl[0]);
            p.setOnClickListener(v -> { tpl[0] = t.id; for (TextView x : ts) U.setPill(x, x == v); });
            ts.add(p);
            tf.addView(p);
        }
        s.add(tf);
        s.add(Sheets.label(a, "3 · HAFTANIN NİYETİ"));
        final EditText intent = U.input(a, "Örn: Bu hafta her gün en az 2 saat ders", "", false);
        s.add(intent);
        s.add(Sheets.label(a, "4 · HAFTALIK MEYDAN OKUMA"));
        if (r.s.p.weekly != null) s.add(Sheets.note(a, Quests.emoji(r.s.p.weekly.kind) + " Meydan okuman", Quests.text(r.s.p.weekly) + "  ·  Ödül: 100 XP + 🎁 Epik sandık", Th.gold));
        U.Btn ok = U.btn(a, "✍️  PLANI ONAYLA", Th.primary);
        ok.setOnClickListener(v -> {
            if (tpl[0] != 0) {
                State.Template t = r.e.template(tpl[0]);
                if (t != null) r.e.applyTemplate(t, nextWs, r.now());
            }
            if (r.s.p.lastPlanWeek != nextWs) {
                r.s.p.plans++;
                Ev.List ev = new Ev.List();
                r.e.gainXp(20, "Haftalık planlama", td, r.now(), ev);
                com.ilker.rutin.core.Badges.check(r.s, td, ev);
                a.celebrate(ev, v);
            }
            r.s.p.lastPlanWeek = nextWs;
            String it = intent.getText().toString().trim();
            if (!it.isEmpty()) { r.s.day(nextWs).intention = it; }
            r.commit();
            a.toast("🗺️ Hafta planlandı! Taahhüdün kaydedildi.");
            s.dismiss();
        });
        s.add(ok, U.m(U.lpMW(), 0, 22, 0, 0));
        a.show(s);
    }

    static View weekCard(MainActivity a, Insights.Week w) {
        LinearLayout c = U.card(a);
        c.setBackground(U.rr(Th.surface2, 20));
        LinearLayout row = U.h(a);
        row.addView(Sheets.stat(a, "📈", Math.round(w.avgRatio * 100) + "%", "tamamlama"), U.lpW(1));
        row.addView(Sheets.stat(a, "🔥", String.valueOf(w.goodDays), "iyi gün"), U.lpW(1));
        row.addView(Sheets.stat(a, "⏱️", Tm.durShort(w.focusMin), "odak"), U.lpW(1));
        c.addView(row);
        if (w.prevAvgRatio >= 0) {
            int diff = (int) Math.round((w.avgRatio - w.prevAvgRatio) * 100);
            c.addView(U.tv(a, diff >= 0 ? "📈 Önceki haftaya göre +" + diff + " puan" : "📉 Önceki haftaya göre " + diff + " puan", 14, diff >= 0 ? Th.success : Th.warn, 800), U.m(U.lpMW(), 0, 10, 0, 0));
        }
        if (!w.bestDay.isEmpty()) c.addView(U.tv(a, "🏆 En verimli gün: " + w.bestDay, 14, Th.text, 700), U.m(U.lpMW(), 0, 6, 0, 0));
        if (!w.worstHabit.isEmpty()) c.addView(U.tv(a, "🧗 En çok aksayan: " + w.worstHabit, 14, Th.text, 700), U.m(U.lpMW(), 0, 4, 0, 0));
        if (w.moodN > 0) c.addView(U.tv(a, "🙂 Ortalama ruh hali: " + String.format(java.util.Locale.US, "%.1f", w.avgMood).replace('.', ',') + " / 5", 14, Th.text, 700), U.m(U.lpMW(), 0, 4, 0, 0));
        c.addView(Sheets.note(a, "💡 Gelecek hafta için tek öneri", w.tip, Th.focus), U.m(U.lpMW(), 0, 10, 0, 0));
        return c;
    }

    public static void weeklyReport(final MainActivity a, int weekStart) {
        Sheet s = new Sheet(a, "📊 Haftalık rapor · " + Tm.dateShort(weekStart) + " – " + Tm.dateShort(weekStart + 6));
        s.add(weekCard(a, Insights.week(a.r.e, weekStart)));
        a.show(s);
    }

    // ---------------- Ay özeti (Wrapped) ----------------
    public static void wrapped(final MainActivity a) {
        final Repo r = a.r;
        long now = r.now();
        int td = r.e.today(now);
        int[] ymd = Tm.ymd(td);
        int from = Tm.epochDay(ymd[0], ymd[1], 1);
        int done = 0, perfect = 0, good = 0, focus = 0, moodN = 0; double moodS = 0;
        long water = 0;
        for (int d = from; d <= td; d++) {
            DayRec x = r.s.dayOrNull(d);
            if (x == null) continue;
            done += x.done + x.mini; if (x.perfect) perfect++; if (x.met || x.good()) good++; focus += x.focusMin;
            if (x.mood > 0) { moodN++; moodS += x.mood; }
        }
        for (Task t : r.s.tasks) if (t.day >= from && t.day <= td && t.type == Habit.COUNTER && "ml".equals(t.unit)) water += t.progress;
        int badges = 0;
        for (State.BadgeEarn b : r.s.badges) if (b.day >= from) badges++;
        final String[][] slides = {
                {"📅", Tm.MONTH[ymd[1] - 1] + " özetin", "Bu ay neler başardığına bir bakalım…"},
                {"✅", Fmt.num(done) + " görev", "Her biri, olmak istediğin kişiye verilmiş bir oy."},
                {"🔥", good + " iyi gün", "Seri en uzun: " + r.s.p.bestStreak + " gün"},
                {"💧", Fmt.amount((int) Math.min(Integer.MAX_VALUE, water), "ml", Habit.COUNTER), "su içtin. Hücrelerin teşekkür ediyor!"},
                {"⏱️", Tm.dur(focus), "odaklandın. Bu, derin bir emek demek."},
                {"💎", perfect + " Mükemmel Gün", badges + " yeni rozet kazandın"},
                {moodN > 0 ? "🙂" : "🌱", moodN > 0 ? "Ruh halin: " + String.format(java.util.Locale.US, "%.1f", moodS / moodN).replace('.', ',') + "/5" : "Büyümeye devam", "Gelecek ay daha da iyisi seni bekliyor. 💫"},
        };
        final FrameLayout f = new FrameLayout(a);
        final int[] idx = {0};
        final int[][] grads = {{0xFF3A1C71, 0xFFD76D77}, {0xFF0F9B8E, 0xFF1B3A4B}, {0xFFFF512F, 0xFFDD2476}, {0xFF1FA2FF, 0xFF12D8FA},
                {0xFF7F00FF, 0xFFE100FF}, {0xFFF7971E, 0xFFFFD200}, {0xFF11998E, 0xFF38EF7D}};
        final LinearLayout c = U.v(a);
        c.setGravity(Gravity.CENTER);
        c.setPadding(U.dp(30), a.topInset + U.dp(30), U.dp(30), a.bottomInset + U.dp(30));
        f.addView(c, new FrameLayout.LayoutParams(-1, -1));
        final Runnable[] show = new Runnable[1];
        show[0] = () -> {
            String[] sl = slides[idx[0]];
            f.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR, grads[idx[0] % grads.length]));
            c.removeAllViews();
            LinearLayout dots = U.h(a);
            for (int i = 0; i < slides.length; i++) {
                View d = new View(a);
                d.setBackground(U.rr(i <= idx[0] ? 0xFFFFFFFF : 0x55FFFFFF, 3));
                dots.addView(d, U.m(new LinearLayout.LayoutParams(0, U.dp(4), 1), 2, 0, 2, 0));
            }
            c.addView(dots, U.lpMW());
            c.addView(U.fill(a), new LinearLayout.LayoutParams(1, 0, 1));
            TextView e = U.tv(a, sl[0], 90, 0xFFFFFFFF, 400); e.setGravity(Gravity.CENTER);
            c.addView(e);
            TextView t = U.tv(a, sl[1], 36, 0xFFFFFFFF, 900); t.setGravity(Gravity.CENTER);
            c.addView(t);
            TextView sb = U.tv(a, sl[2], 18, 0xEEFFFFFF, 700); sb.setGravity(Gravity.CENTER);
            c.addView(sb, U.m(U.lpMW(), 0, 12, 0, 0));
            c.addView(U.fill(a), new LinearLayout.LayoutParams(1, 0, 1));
            c.addView(U.tv(a, idx[0] < slides.length - 1 ? "Devam için dokun ›" : "Kapatmak için dokun", 13, 0xAAFFFFFF, 800));
            U.pop(e);
            U.pop(t);
        };
        f.setClickable(true);
        f.setOnClickListener(v -> {
            if (idx[0] < slides.length - 1) { idx[0]++; show[0].run(); U.haptic(v); }
            else a.closeFull(f);
        });
        show[0].run();
        a.showFull(f);
        a.confetti.rain(60);
    }
}
