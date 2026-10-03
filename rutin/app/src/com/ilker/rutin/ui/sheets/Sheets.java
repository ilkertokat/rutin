package com.ilker.rutin.ui.sheets;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ilker.rutin.core.Area;
import com.ilker.rutin.core.DayRec;
import com.ilker.rutin.core.Ev;
import com.ilker.rutin.core.Game;
import com.ilker.rutin.core.Habit;
import com.ilker.rutin.core.Json;
import com.ilker.rutin.core.Planner;
import com.ilker.rutin.core.Task;
import com.ilker.rutin.core.Tm;
import com.ilker.rutin.data.Repo;
import com.ilker.rutin.ui.Art;
import com.ilker.rutin.ui.MainActivity;
import com.ilker.rutin.ui.Sheet;
import com.ilker.rutin.ui.Th;
import com.ilker.rutin.ui.U;
import com.ilker.rutin.ui.Views;

import java.io.OutputStream;
import java.util.ArrayList;

/** Görev, alışkanlık, plan dışı olay ve seri panelleri (+ diğer panellere kısayollar). */
public final class Sheets {
    private Sheets() {}

    // Kısayollar
    public static void focusStart(MainActivity a, long taskId) { FocusSheets.start(a, taskId); }
    public static void maybeRateFocus(MainActivity a) { FocusSheets.maybeRate(a); }
    public static void summary(MainActivity a) { DaySheets.summary(a); }
    public static void morning(MainActivity a) { DaySheets.morning(a); }
    public static void planWeek(MainActivity a) { DaySheets.planWeek(a); }
    public static void shop(MainActivity a, int cat) { MeSheets.shop(a, cat); }
    public static void chests(MainActivity a) { MeSheets.chests(a); }

    static TextView label(MainActivity a, String s) {
        TextView t = U.tv(a, s, 13, Th.dim, 800);
        t.setPadding(U.dp(2), U.dp(14), 0, U.dp(6));
        return t;
    }

    static void done(MainActivity a, Sheet s, Ev.List ev, View anchor) {
        a.celebrate(ev, anchor);
        a.r.commit();
        s.dismiss();
    }

    // ---------------- Görev ayrıntısı ----------------
    public static void task(final MainActivity a, final Task t, View anchor) {
        final Repo r = a.r;
        final long now = r.now();
        final boolean today = t.day == r.e.today(now);
        final boolean past = t.day < r.e.today(now);
        final Sheet s = new Sheet(a, null);
        LinearLayout top = U.h(a);
        top.addView(U.emojiBox(a, t.emoji, Area.COLOR[Math.max(0, Math.min(5, t.area))], 60));
        top.addView(U.space(a, 14));
        LinearLayout tc = U.v(a);
        tc.addView(U.tv(a, t.title, 21, Th.text, 900));
        String info = Habit.TYPE_EMOJI[t.type] + " " + Habit.TYPE_NAME[t.type];
        if (t.targetMin >= 0) info += " · " + Tm.hhmm(t.targetMin);
        if (t.type != Habit.FLEX && !t.isQuantity()) info += " · son " + Tm.hhmm(Tm.norm(r.e.deadlineDm(t)));
        if (!today) info += " · " + Tm.dateLong(t.day);
        tc.addView(U.tv(a, info, 13, Th.dim, 700));
        top.addView(tc, U.lpW(1));
        s.add(top, U.m(U.lpMW(), 0, 12, 0, 6));
        Habit h = r.s.habit(t.habitId);
        if (h != null) {
            LinearLayout stats = U.h(a);
            stats.addView(U.chip(a, "🔥 " + h.curStreak + " gün", Th.surface2, Th.text));
            stats.addView(U.space(a, 6));
            stats.addView(U.chip(a, "🏆 en iyi " + h.bestStreak, Th.surface2, Th.text));
            stats.addView(U.space(a, 6));
            stats.addView(U.chip(a, "✅ " + h.totalDone + " kez", Th.surface2, Th.text));
            if (h.graduated) { stats.addView(U.space(a, 6)); stats.addView(U.chip(a, "🎓", Th.surface2, Th.gold)); }
            s.add(stats, U.m(U.lpMW(), 0, 8, 0, 4));
            int nx = com.ilker.rutin.core.Rewards.habitToNext(h.totalDone);
            s.add(U.chip(a, com.ilker.rutin.core.Rewards.habitLevelName(h.totalDone) + (nx > 0 ? " · sonraki seviyeye " + nx + " tekrar" : " · en üst seviye!"), Art.alpha(Th.success, 0.15f), Th.success), U.m(U.lpWW(), 0, 6, 0, 4));
            if (h.why != null && !h.why.isEmpty()) s.add(note(a, "💭 Neden yapıyorum?", h.why, Th.focus), U.m(U.lpMW(), 0, 10, 0, 0));
            if (h.ifThen != null && !h.ifThen.isEmpty()) s.add(note(a, "🔀 Eğer-ise planım", h.ifThen, Th.water), U.m(U.lpMW(), 0, 8, 0, 0));
            if (h.afterId != 0) {
                Habit prev = r.s.habit(h.afterId);
                if (prev != null) s.add(note(a, "🔗 Alışkanlık zinciri", prev.emoji + " " + prev.title + " bittikten sonra", Th.success), U.m(U.lpMW(), 0, 8, 0, 0));
            }
        }
        if (t.hasMini() && !t.isQuantity() && t.isOpen()) s.add(note(a, "🟡 Mini versiyon (2 dakika kuralı)", t.miniText, Th.warn), U.m(U.lpMW(), 0, 8, 0, 0));

        if (past) {
            s.add(U.tv(a, t.isComplete() ? "Bu iş tamamlanmıştı ✅" : "Bu gün kapandı.", 15, Th.dim, 700), U.m(U.lpMW(), 0, 16, 0, 0));
            a.show(s);
            return;
        }
        // ---- Miktar ----
        if (t.isQuantity()) {
            final TextView amt = U.tv(a, t.amount(t.progress) + " / " + t.amount(t.full), 30, Th.text, 900);
            amt.setGravity(Gravity.CENTER);
            s.add(amt, U.m(U.lpMW(), 0, 16, 0, 6));
            final Views.WaveBar bar = new Views.WaveBar(a, t.type == Habit.COUNTER ? Th.water : Th.focus);
            bar.set((float) t.ratio(), false);
            if (t.mini > 0 && t.mini < t.full) bar.mini = t.mini / (float) t.full;
            s.add(bar, new LinearLayout.LayoutParams(-1, U.dp(18)));
            if (t.mini > 0) s.add(center(a, "Mini hedef: " + t.amount(t.mini) + (t.progress >= t.mini ? "  ✓ seri güvende" : ""), 13, Th.dim), U.m(U.lpMW(), 0, 6, 0, 0));
            if (today) {
                LinearLayout btns = U.h(a);
                int st = t.type == Habit.COUNTER ? t.step : 15;
                int[] amts = t.type == Habit.COUNTER ? new int[]{-st, st, st * 2, st * 4} : new int[]{-15, 15, 30, 60};
                for (final int am : amts) {
                    TextView b = U.tv(a, (am > 0 ? "+" : "−") + t.amount(Math.abs(am)), 14, am > 0 ? 0xFF06223A : Th.text, 900);
                    b.setGravity(Gravity.CENTER);
                    b.setBackground(U.rr(am > 0 ? (t.type == Habit.COUNTER ? Th.water : Th.focus) : Th.surface2, 14));
                    b.setPadding(0, U.dp(12), 0, U.dp(12));
                    b.setOnClickListener(v -> {
                        Ev.List ev = r.e.addProgress(t.id, am, r.now());
                        bar.set((float) t.ratio(), true);
                        amt.setText(t.amount(t.progress) + " / " + t.amount(t.full));
                        U.haptic(v);
                        a.celebrate(ev, v);
                        r.commit();
                    });
                    U.squish(b);
                    btns.addView(b, U.m(U.lpW(1), 3, 0, 3, 0));
                }
                s.add(btns, U.m(U.lpMW(), 0, 14, 0, 0));
                if (t.type == Habit.DURATION && t.isOpen()) {
                    U.Btn f = U.btn(a, "▶  ODAK BAŞLAT", Th.focus);
                    f.setOnClickListener(v -> { s.dismiss(); focusStart(a, t.id); });
                    s.add(f, U.m(U.lpMW(), 0, 14, 0, 0));
                    final ArrayList<Integer> bl = Planner.suggestBlocks(r.e, t, now, a.r.s.p.focusMin + 25 > 60 ? 50 : 50, 10);
                    if (!bl.isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < bl.size(); i++) sb.append(i > 0 ? ", " : "").append(Tm.hhmm(bl.get(i)));
                        TextView sug = U.chip(a, "🗓️ Önerilen bloklar: " + sb + "  ·  Plana ekle", Art.alpha(Th.focus, 0.15f), Th.focus);
                        sug.setOnClickListener(v -> { t.blocks.clear(); t.blocks.addAll(bl); r.commit(); a.toast("🗓️ Bloklar planlandı, zamanı gelince hatırlatacağım"); s.dismiss(); });
                        s.add(sug, U.m(U.lpMW(), 0, 10, 0, 0));
                    }
                }
            }
        }
        // ---- Eylemler ----
        if (today && t.isOpen() && !t.isQuantity()) {
            U.Btn ok = U.btn(a, "✅  TAMAMLADIM", Th.success);
            ok.setOnClickListener(v -> done(a, s, r.e.complete(t.id, false, r.now()), anchor != null ? anchor : v));
            s.add(ok, U.m(U.lpMW(), 0, 16, 0, 0));
            if (t.hasMini()) {
                U.Btn mini = U.btn(a, "🟡  MİNİ VERSİYONU YAPTIM", Th.warn);
                mini.setOnClickListener(v -> done(a, s, r.e.complete(t.id, true, r.now()), v));
                s.add(mini, U.m(U.lpMW(), 0, 10, 0, 0));
            }
        }
        if (today && t.isQuantity() && t.isOpen() && t.mini > 0 && t.progress < t.mini) {
            U.Btn mini = U.ghost(a, "🟡  Mini hedefi tamamla (" + t.amount(t.mini) + ")");
            mini.setOnClickListener(v -> done(a, s, r.e.complete(t.id, true, r.now()), v));
            s.add(mini, U.m(U.lpMW(), 0, 10, 0, 0));
        }
        if (t.isComplete() || t.status == Task.SKIPPED) {
            U.Btn undo = U.ghost(a, "↩️  Geri al");
            undo.setOnClickListener(v -> { r.e.undo(t.id, r.now()); r.commit(); a.toast("Geri alındı"); s.dismiss(); });
            s.add(undo, U.m(U.lpMW(), 0, 14, 0, 0));
        }
        if (t.isOpen()) {
            if (today) {
                s.add(label(a, "⏰ ERTELE"));
                LinearLayout sn = U.h(a);
                for (final int m : new int[]{15, 30, 60, 120}) {
                    TextView c = U.pill(a, m < 60 ? m + " dk" : (m / 60) + " sa", false);
                    c.setOnClickListener(v -> { r.e.snooze(t.id, m, r.now()); r.commit(); a.toast("💤 " + Tm.dur(m) + " sonra hatırlatacağım"); s.dismiss(); });
                    sn.addView(c, U.m(U.lpW(1), 3, 0, 3, 0));
                }
                s.add(sn);
            }
            if (t.type != Habit.COUNTER) {
            s.add(label(a, "🕐 SAATİ DEĞİŞTİR"));
            final Views.TimeStepper ts = new Views.TimeStepper(a, t.targetMin >= 0 ? t.targetMin : Tm.norm((int) (now % 1440) / 15 * 15 + 30), null);
            s.add(ts);
            U.Btn setT = U.ghost(a, "Saati kaydet");
            setT.setOnClickListener(v -> { r.e.move(t.id, t.day, ts.minute, r.now()); r.commit(); a.toast("🕐 " + Tm.hhmm(ts.minute) + " olarak ayarlandı"); s.dismiss(); });
            s.add(setT, U.m(U.lpMW(), 0, 8, 0, 0));
            s.add(label(a, "📅 BAŞKA GÜNE TAŞI"));
            Views.Flow days = new Views.Flow(a);
            int td = r.e.today(now);
            for (int d = td; d < td + 8; d++) {
                if (d == t.day) continue;
                final int dd = d;
                TextView c = U.pill(a, d == td ? "Bugün" : d == td + 1 ? "Yarın" : Tm.DAY_SHORT[Tm.weekday(d)] + " " + Tm.ymd(d)[2], false);
                c.setOnClickListener(v -> { r.e.move(t.id, dd, t.targetMin, r.now()); r.commit(); a.toast("📅 " + Tm.dateLong(dd) + " gününe taşındı"); s.dismiss(); });
                days.addView(c);
            }
            s.add(days);
            }
            if (t.type == Habit.FLEX || t.type == Habit.ONCE || !t.streak) {
                U.Btn sk = U.ghost(a, "⏭️  Bugün atla");
                sk.setOnClickListener(v -> { r.e.skip(t.id); r.commit(); s.dismiss(); });
                s.add(sk, U.m(U.lpMW(), 0, 16, 0, 0));
            }
        }
        if (h != null) {
            U.Btn ed = U.ghost(a, "✏️  Alışkanlığı düzenle");
            ed.setOnClickListener(v -> { s.dismiss(); habitEditor(a, h, -1); });
            s.add(ed, U.m(U.lpMW(), 0, 16, 0, 0));
        } else if (t.habitId == 0) {
            U.Btn del = U.ghost(a, "🗑️  Bu işi sil");
            del.setOnClickListener(v -> { r.s.tasks.remove(t); r.commit(); s.dismiss(); });
            s.add(del, U.m(U.lpMW(), 0, 16, 0, 0));
        }
        a.show(s);
    }

    static View note(MainActivity a, String title, String text, int color) {
        LinearLayout c = U.v(a);
        c.setBackground(U.rrs(Art.alpha(color, 0.1f), 16, Art.alpha(color, 0.3f), 1));
        c.setPadding(U.dp(14), U.dp(10), U.dp(14), U.dp(10));
        c.addView(U.tv(a, title, 12, color, 900));
        c.addView(U.tv(a, text, 15, Th.text, 700));
        return c;
    }

    static TextView center(MainActivity a, String s, float sp, int color) {
        TextView t = U.tv(a, s, sp, color, 700);
        t.setGravity(Gravity.CENTER);
        return t;
    }

    // ---------------- Hızlı ekle ----------------
    public static void quickAdd(final MainActivity a, final int day) {
        final Sheet s = new Sheet(a, "Ne eklemek istersin?");
        LinearLayout h1 = U.row(a, "🔁", "Yeni alışkanlık / rutin", "Her gün ya da belirli günlerde tekrar eden iş", Th.primary);
        h1.setBackground(U.rr(Th.surface2, 18));
        h1.setOnClickListener(v -> { s.dismiss(); habitEditor(a, null, -1); });
        U.squish(h1);
        s.add(h1, U.m(U.lpMW(), 0, 10, 0, 0));
        LinearLayout h2 = U.row(a, "📌", "Tek seferlik iş", "Belirli bir güne ait (randevu, ödeme, teslim)", Th.water);
        h2.setBackground(U.rr(Th.surface2, 18));
        h2.setOnClickListener(v -> { s.dismiss(); once(a, day); });
        U.squish(h2);
        s.add(h2, U.m(U.lpMW(), 0, 10, 0, 0));
        a.show(s);
    }

    public static void once(final MainActivity a, int day0) {
        final Repo r = a.r;
        final Sheet s = new Sheet(a, "📌 Tek seferlik iş");
        final EditText title = U.input(a, "Örn: Dişçi randevusu", "", false);
        s.add(title);
        final String[] emoji = {"📌"};
        final Views.Flow ef = emojiPicker(a, emoji);
        s.add(label(a, "SİMGE"));
        s.add(ef);
        s.add(label(a, "GÜN"));
        final int[] day = {day0};
        Views.Flow days = new Views.Flow(a);
        final ArrayList<TextView> dps = new ArrayList<>();
        int td = r.e.today(r.now());
        for (int d = td; d < td + 14; d++) {
            final int dd = d;
            TextView c = U.pill(a, d == td ? "Bugün" : d == td + 1 ? "Yarın" : Tm.DAY_SHORT[Tm.weekday(d)] + " " + Tm.ymd(d)[2], d == day[0]);
            c.setOnClickListener(v -> { day[0] = dd; for (TextView x : dps) U.setPill(x, x == v); });
            dps.add(c);
            days.addView(c);
        }
        s.add(days);
        s.add(label(a, "SAAT (isteğe bağlı)"));
        final boolean[] timed = {true};
        final Views.TimeStepper ts = new Views.TimeStepper(a, Tm.norm((int) (r.now() % 1440) / 15 * 15 + 60), null);
        final TextView tog = U.pill(a, "⏰ Belirli saat", true);
        tog.setOnClickListener(v -> { timed[0] = !timed[0]; U.setPill(tog, timed[0]); ts.setVisibility(timed[0] ? View.VISIBLE : View.GONE); });
        s.add(tog, U.lpWW());
        s.add(ts);
        s.add(label(a, "ÖNCELİK"));
        final int[] pr = {Habit.P_NORMAL};
        s.add(pills(a, new String[]{"Düşük", "Normal", "Kritik 🔴"}, pr));
        U.Btn save = U.btn(a, "EKLE", Th.primary);
        save.setOnClickListener(v -> {
            String tt = title.getText().toString().trim();
            if (tt.isEmpty()) { title.setError("Bir başlık yaz"); return; }
            r.e.addOnce(tt, emoji[0], day[0], timed[0] ? ts.minute : -1, -1, Area.CAREER, pr[0], r.now());
            r.commit();
            a.toast("📌 Eklendi: " + tt);
            s.dismiss();
        });
        s.add(save, U.m(U.lpMW(), 0, 20, 0, 0));
        a.show(s);
    }

    static final String[] EMOJIS = {"💧", "📖", "🏃", "🚶", "🏋️", "🧘", "🛏️", "🦷", "💊", "🥗", "🍎", "☕", "📝", "🗣️", "💻", "📚",
            "🎯", "🧹", "🧺", "🛒", "📞", "❤️", "🐶", "🌱", "🎸", "🎨", "✍️", "🧠", "💼", "📧", "🙏", "😴", "📵", "🚭", "🌅", "🌙",
            "⏱️", "🚿", "💰", "📊", "🎧", "🚴", "🏊", "⚽", "🍳", "🧃", "🕯️", "📌", "✅", "⭐"};

    static Views.Flow emojiPicker(MainActivity a, final String[] sel) {
        final Views.Flow f = new Views.Flow(a);
        f.gap = U.dp(6);
        final ArrayList<TextView> all = new ArrayList<>();
        for (final String e : EMOJIS) {
            TextView t = U.tv(a, e, 22, Th.text, 400);
            t.setGravity(Gravity.CENTER);
            t.setLayoutParams(new LinearLayout.LayoutParams(U.dp(44), U.dp(44)));
            t.setBackground(e.equals(sel[0]) ? U.rr(Art.alpha(Th.primary, 0.3f), 12) : U.rr(Th.surface2, 12));
            t.setOnClickListener(v -> { sel[0] = e; for (TextView x : all) x.setBackground(x == v ? U.rr(Art.alpha(Th.primary, 0.3f), 12) : U.rr(Th.surface2, 12)); U.haptic(v); });
            all.add(t);
            f.addView(t);
        }
        return f;
    }

    static Views.Flow pills(MainActivity a, String[] names, final int[] sel) {
        final Views.Flow f = new Views.Flow(a);
        final ArrayList<TextView> all = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            final int k = i;
            TextView t = U.pill(a, names[i], i == sel[0]);
            t.setOnClickListener(v -> { sel[0] = k; for (TextView x : all) U.setPill(x, x == v); U.haptic(v); });
            all.add(t);
            f.addView(t);
        }
        return f;
    }

    // ---------------- Alışkanlık düzenleyici ----------------
    public static void habitEditor(final MainActivity a, final Habit orig, int dayHint) {
        final Repo r = a.r;
        final boolean isNew = orig == null;
        final Habit h = isNew ? new Habit() : Json.decode(Json.parse(Json.write(orig)), Habit.class);
        if (isNew) { h.emoji = "✅"; h.type = Habit.REQUIRED; h.targetMin = Tm.norm((int) (r.now() % 1440) / 30 * 30 + 60); }
        final Sheet s = new Sheet(a, isNew ? "🔁 Yeni alışkanlık" : "✏️ Alışkanlığı düzenle", true);
        final LinearLayout form = U.v(a);
        s.add(form);
        final Runnable[] render = new Runnable[1];
        render[0] = () -> {
            form.removeAllViews();
            final EditText title = U.input(a, "Örn: 20 sayfa kitap oku", h.title, false);
            title.addTextChangedListener(new SimpleWatcher(() -> h.title = title.getText().toString()));
            form.addView(title);
            form.addView(label(a, "SİMGE"));
            final String[] em = {h.emoji};
            Views.Flow ep = emojiPicker(a, em);
            for (int i = 0; i < ep.getChildCount(); i++) {
                final View ch = ep.getChildAt(i);
                final View.OnClickListener prev = null;
                ch.setOnClickListener(v -> { h.emoji = ((TextView) v).getText().toString(); for (int k = 0; k < ep.getChildCount(); k++) ep.getChildAt(k).setBackground(ep.getChildAt(k) == v ? U.rr(Art.alpha(Th.primary, 0.3f), 12) : U.rr(Th.surface2, 12)); });
            }
            form.addView(ep);
            form.addView(label(a, "TÜR"));
            Views.Flow types = new Views.Flow(a);
            for (int i = 0; i < 5; i++) {
                final int k = i;
                TextView t = U.pill(a, Habit.TYPE_EMOJI[i] + " " + Habit.TYPE_NAME[i], h.type == i);
                t.setOnClickListener(v -> {
                    h.type = k;
                    h.streak = Habit.defaultStreak(k);
                    if (k == Habit.COUNTER && h.full <= 1) { h.full = 8; h.mini = 4; h.step = 1; h.unit = "bardak"; }
                    if (k == Habit.DURATION && h.full <= 1) { h.full = 60; h.mini = 15; h.step = 1; h.unit = ""; }
                    if (k != Habit.COUNTER && k != Habit.DURATION) { h.full = 1; h.mini = 0; }
                    render[0].run();
                });
                types.addView(t);
            }
            form.addView(types);
            form.addView(U.tv(a, Habit.TYPE_DESC[h.type], 13, Th.dim, 700), U.m(U.lpMW(), 2, 6, 0, 0));
            // Miktar
            if (h.type == Habit.COUNTER) {
                form.addView(label(a, "BİRİM"));
                final String[] units = {"ml", "bardak", "sayfa", "adım", "tekrar", "kez"};
                Views.Flow uf = new Views.Flow(a);
                for (final String u : units) {
                    TextView t = U.pill(a, u, u.equals(h.unit));
                    t.setOnClickListener(v -> {
                        h.unit = u;
                        if (u.equals("ml")) { h.full = 4000; h.mini = 2000; h.step = 250; }
                        else if (u.equals("sayfa")) { h.full = 20; h.mini = 5; h.step = 5; }
                        else if (u.equals("adım")) { h.full = 8000; h.mini = 4000; h.step = 1000; }
                        else { h.full = Math.max(h.full, 2); h.step = 1; if (h.mini >= h.full) h.mini = h.full / 2; }
                        render[0].run();
                    });
                    uf.addView(t);
                }
                form.addView(uf);
                form.addView(stepperRow(a, "Hedef", h.full, h.step, h.step, 100000, v -> Habit_f(h, v, 0), h));
                form.addView(stepperRow(a, "Mini hedef", h.mini, h.step, 0, 100000, v -> Habit_f(h, v, 1), h));
                form.addView(stepperRow(a, "Her dokunuşta", h.step, h.unit.equals("ml") ? 50 : 1, 1, 5000, v -> Habit_f(h, v, 2), h));
            } else if (h.type == Habit.DURATION) {
                form.addView(stepperRow(a, "Günlük hedef", h.full, 15, 15, 720, v -> h.full = v, h));
                form.addView(stepperRow(a, "Mini hedef", h.mini, 5, 0, 720, v -> h.mini = v, h));
            } else {
                form.addView(label(a, "🟡 MİNİ VERSİYON (2 dakika kuralı)"));
                final EditText mini = U.input(a, "Örn: Sadece 5 sayfa / spor kıyafetini giy", h.miniText, false);
                mini.addTextChangedListener(new SimpleWatcher(() -> h.miniText = mini.getText().toString().trim()));
                form.addView(mini);
                form.addView(U.tv(a, "Kötü bir günde mini versiyonu yapmak da seriyi korur.", 12, Th.dim, 700), U.m(U.lpMW(), 2, 4, 0, 0));
            }
            // Günler
            form.addView(label(a, "GÜNLER"));
            final Views.DaysPicker dp = new Views.DaysPicker(a, h.daysMask);
            dp.onChange = () -> h.daysMask = dp.mask;
            form.addView(dp);
            Views.Flow quick = new Views.Flow(a);
            String[] qn = {"Her gün", "Hafta içi", "Hafta sonu", "Pzt-Çar-Cum"};
            final int[] qm = {127, 31, 96, 21};
            for (int i = 0; i < qn.length; i++) {
                final int k = i;
                TextView t = U.chip(a, qn[i], Th.surface2, Th.dim);
                t.setOnClickListener(v -> { h.daysMask = qm[k]; render[0].run(); });
                quick.addView(t);
            }
            form.addView(quick, U.m(U.lpMW(), 0, 8, 0, 0));
            // Saat
            if (h.type != Habit.COUNTER) {
                form.addView(label(a, h.type == Habit.DURATION ? "BAŞLANGIÇ SAATİ (isteğe bağlı)" : "HEDEF SAAT (genelde bu saatte)"));
                final TextView tog = U.pill(a, h.targetMin >= 0 ? "⏰ Saat var" : "Saat yok (gün içinde)", h.targetMin >= 0);
                tog.setOnClickListener(v -> { h.targetMin = h.targetMin >= 0 ? -1 : 9 * 60; render[0].run(); });
                form.addView(tog, U.lpWW());
                if (h.targetMin >= 0) {
                    final Views.TimeStepper ts = new Views.TimeStepper(a, h.targetMin, null);
                    ts.onChange = () -> h.targetMin = ts.minute;
                    form.addView(ts);
                    form.addView(stepperRow(a, "Tolerans", h.graceMin, 5, 0, 120, v -> h.graceMin = v, null));
                }
            }
            if (h.type != Habit.FLEX) {
                form.addView(label(a, "SON SAAT (bu saatten sonra kaçırıldı sayılır)"));
                final TextView dt = U.pill(a, h.deadlineMin >= 0 ? "Son saat: " + Tm.hhmm(h.deadlineMin) : "Otomatik (uyku saatine göre)", h.deadlineMin >= 0);
                dt.setOnClickListener(v -> { h.deadlineMin = h.deadlineMin >= 0 ? -1 : Tm.norm(r.s.p.sleepMin - 60); render[0].run(); });
                form.addView(dt, U.lpWW());
                if (h.deadlineMin >= 0) {
                    final Views.TimeStepper ds = new Views.TimeStepper(a, h.deadlineMin, null);
                    ds.onChange = () -> h.deadlineMin = ds.minute;
                    form.addView(ds);
                }
            }
            // Alan ve öncelik
            form.addView(label(a, "YAŞAM ALANI"));
            Views.Flow af = new Views.Flow(a);
            for (int i = 0; i < 6; i++) {
                final int k = i;
                TextView t = U.pill(a, Area.EMOJI[i] + " " + Area.NAME[i], h.area == i);
                t.setOnClickListener(v -> { h.area = k; render[0].run(); });
                af.addView(t);
            }
            form.addView(af);
            form.addView(label(a, "ÖNCELİK (bildirim ısrarı)"));
            final int[] pr = {h.priority};
            Views.Flow pf = pills(a, new String[]{"Düşük", "Normal", "Kritik 🔴"}, pr);
            for (int i = 0; i < pf.getChildCount(); i++) {
                final int k = i;
                pf.getChildAt(i).setOnClickListener(v -> { h.priority = k; render[0].run(); });
            }
            form.addView(pf);
            if (h.type != Habit.FLEX) {
                final TextView sk = U.pill(a, h.streak ? "🔥 Seriyi etkiler" : "Seriyi etkilemez", h.streak);
                sk.setOnClickListener(v -> { h.streak = !h.streak; render[0].run(); });
                form.addView(sk, U.m(U.lpWW(), 0, 12, 0, 0));
            }
            // Psikoloji
            form.addView(label(a, "💭 NEDEN? (motivasyon düştüğünde gösterilir)"));
            final EditText why = U.input(a, "Örn: Enerjim ve odağım için", h.why, true);
            why.addTextChangedListener(new SimpleWatcher(() -> h.why = why.getText().toString().trim()));
            form.addView(why);
            form.addView(label(a, "🔀 EĞER-İSE PLANI"));
            final EditText ift = U.input(a, "Örn: Eğer yağmur yağarsa evde 20 dk esneme yaparım", h.ifThen, true);
            ift.addTextChangedListener(new SimpleWatcher(() -> h.ifThen = ift.getText().toString().trim()));
            form.addView(ift);
            ArrayList<Habit> others = new ArrayList<>();
            for (Habit o : r.s.activeHabits()) if (o.id != h.id) others.add(o);
            if (!others.isEmpty()) {
                form.addView(label(a, "🔗 ALIŞKANLIK ZİNCİRİ: şundan sonra yap"));
                Views.Flow ch = new Views.Flow(a);
                TextView none = U.pill(a, "Yok", h.afterId == 0);
                none.setOnClickListener(v -> { h.afterId = 0; render[0].run(); });
                ch.addView(none);
                for (final Habit o : others) {
                    TextView t = U.pill(a, o.emoji + " " + o.title, h.afterId == o.id);
                    t.setOnClickListener(v -> { h.afterId = o.id; if (o.targetMin >= 0 && h.type != Habit.COUNTER) h.targetMin = Tm.norm(o.targetMin + 15); render[0].run(); });
                    ch.addView(t);
                }
                form.addView(ch);
            }
            U.Btn save = U.btn(a, isNew ? "EKLE" : "KAYDET", Th.primary);
            save.setOnClickListener(v -> {
                if (h.title == null || h.title.trim().isEmpty()) { a.toast("Bir başlık yaz ✍️"); return; }
                if (h.daysMask == 0) { a.toast("En az bir gün seç 📅"); return; }
                h.title = h.title.trim();
                long now = r.now();
                if (isNew) {
                    h.createdDay = r.e.today(now);
                    r.e.addHabit(h, now);
                    a.toast(h.emoji + " " + h.title + " eklendi!");
                } else {
                    copyInto(h, orig);
                    r.e.updateHabit(orig, now);
                    a.toast("✅ Kaydedildi");
                }
                r.commit();
                s.dismiss();
            });
            form.addView(save, U.m(U.lpMW(), 0, 22, 0, 0));
            if (!isNew) {
                U.Btn del = U.ghost(a, "🗑️  Alışkanlığı arşivle");
                del.setOnClickListener(v -> { r.e.archiveHabit(orig, r.now()); r.commit(); a.toast("Arşivlendi"); s.dismiss(); });
                form.addView(del, U.m(U.lpMW(), 0, 10, 0, 0));
            }
        };
        render[0].run();
        a.show(s);
    }

    static void Habit_f(Habit h, int v, int which) {
        if (which == 0) { h.full = v; if (h.mini >= h.full) h.mini = h.full / 2; }
        else if (which == 1) h.mini = Math.min(v, Math.max(0, h.full - 1));
        else h.step = Math.max(1, v);
    }

    static void copyInto(Habit src, Habit dst) {
        dst.title = src.title; dst.emoji = src.emoji; dst.type = src.type; dst.area = src.area; dst.daysMask = src.daysMask;
        dst.targetMin = src.targetMin; dst.deadlineMin = src.deadlineMin; dst.graceMin = src.graceMin; dst.full = src.full; dst.mini = src.mini;
        dst.unit = src.unit; dst.step = src.step; dst.miniText = src.miniText; dst.why = src.why; dst.ifThen = src.ifThen; dst.afterId = src.afterId;
        dst.priority = src.priority; dst.streak = src.streak;
    }

    interface IntSet { void set(int v); }

    static View stepperRow(MainActivity a, String label, int value, int step, int min, int max, final IntSet set, final Habit h) {
        LinearLayout row = U.h(a);
        row.setPadding(0, U.dp(8), 0, 0);
        row.addView(U.tv(a, label, 15, Th.text, 800), U.lpW(1));
        final Views.NumStepper ns = new Views.NumStepper(a, value, step, min, max,
                h == null ? (v -> v + " dk") : h.type == Habit.DURATION ? Tm::dur : (v -> Habit_fmt(h, v)));
        ns.onChange = () -> set.set(ns.value);
        row.addView(ns);
        return row;
    }

    static String Habit_fmt(Habit h, int v) { return com.ilker.rutin.core.Fmt.amount(v, h.unit, h.type); }

    public static class SimpleWatcher implements android.text.TextWatcher {
        final Runnable r;
        public SimpleWatcher(Runnable r) { this.r = r; }
        public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
        public void onTextChanged(CharSequence s, int a, int b, int c) {}
        public void afterTextChanged(android.text.Editable e) { r.run(); }
    }

    // ---------------- Plan dışı olay ----------------
    public static void disruption(final MainActivity a) {
        final Repo r = a.r;
        final Sheet s = new Sheet(a, "🛡️ Plan dışı olay");
        s.add(U.tv(a, "Hayat plana uymaz, sorun değil. Ne olduğunu söyle, günü birlikte yeniden düzenleyelim.", 14, Th.dim, 700));
        s.add(label(a, "NE OLDU?"));
        final int[] type = {-1};
        final int[] dur = {60};
        Views.Flow tf = new Views.Flow(a);
        final ArrayList<TextView> tps = new ArrayList<>();
        final LinearLayout preview = U.v(a);
        final Runnable[] upd = new Runnable[1];
        for (int i = 0; i < Planner.DIS_NAME.length; i++) {
            final int k = i;
            TextView t = U.pill(a, Planner.DIS_EMOJI[i] + " " + Planner.DIS_NAME[i], false);
            t.setOnClickListener(v -> { type[0] = k; for (TextView x : tps) U.setPill(x, x == v); upd[0].run(); });
            tps.add(t);
            tf.addView(t);
        }
        s.add(tf);
        s.add(label(a, "NE KADAR SÜRECEK?"));
        Views.Flow df = new Views.Flow(a);
        final ArrayList<TextView> dps = new ArrayList<>();
        final int[] durs = {30, 60, 120, 180, Planner.REST_OF_DAY, -2};
        final String[] dn = {"30 dk", "1 saat", "2 saat", "3 saat", "Günün geri kalanı", "Birkaç gün (hasta)"};
        for (int i = 0; i < durs.length; i++) {
            final int k = i;
            TextView t = U.pill(a, dn[i], durs[i] == dur[0]);
            t.setOnClickListener(v -> { dur[0] = durs[k]; for (TextView x : dps) U.setPill(x, x == v); upd[0].run(); });
            dps.add(t);
            df.addView(t);
        }
        s.add(df);
        s.add(preview, U.m(U.lpMW(), 0, 12, 0, 0));
        final int[] sickDays = {3};
        upd[0] = () -> {
            preview.removeAllViews();
            if (type[0] < 0) return;
            long now = r.now();
            if (dur[0] == -2) {
                preview.addView(label(a, "KAÇ GÜN?"));
                final Views.NumStepper ns = new Views.NumStepper(a, sickDays[0], 1, 1, 14, v -> v + " gün");
                ns.onChange = () -> sickDays[0] = ns.value;
                preview.addView(ns);
                preview.addView(note(a, "🤒 Hastalık modu", "Hedefler iyileşme versiyonuna (mini) düşer, bildirimler yumuşar, serin dondurma hakkı harcanmadan korunur. Geçmiş olsun 💛", Th.water), U.m(U.lpMW(), 0, 10, 0, 0));
                U.Btn ok = U.btn(a, "HASTALIK MODUNU AÇ", Th.water);
                ok.setOnClickListener(v -> { Planner.sickDays(r.e, sickDays[0], r.now()); r.commit(); a.toast("💛 Geçmiş olsun. Serin güvende."); s.dismiss(); });
                preview.addView(ok, U.m(U.lpMW(), 0, 14, 0, 0));
                return;
            }
            final ArrayList<Planner.Change> ch = Planner.preview(r.e, now, dur[0], type[0]);
            preview.addView(label(a, "ŞÖYLE DÜZENLEDİM:"));
            for (Planner.Change c : ch) {
                TextView t = U.tv(a, "• " + c.text, 14, c.kind == Planner.Change.INFO ? Th.dim : Th.text, 700);
                t.setPadding(0, U.dp(3), 0, U.dp(3));
                preview.addView(t);
            }
            U.Btn ok = U.btn(a, "ONAYLA", Th.primary);
            ok.setOnClickListener(v -> {
                Planner.apply(r.e, ch, r.now(), dur[0], type[0]);
                r.commit();
                a.toast("🛡️ Plan güncellendi. Rutinin devam ediyor!");
                s.dismiss();
            });
            preview.addView(ok, U.m(U.lpMW(), 0, 16, 0, 0));
            preview.addView(U.tv(a, "Onayladıktan sonra her işi tek tek sürükleyip düzenleyebilirsin.", 12, Th.dim, 700), U.m(U.lpMW(), 0, 8, 0, 0));
        };
        a.show(s);
    }

    // ---------------- Seri bilgisi ----------------
    public static void streakInfo(final MainActivity a) {
        final Repo r = a.r;
        long now = r.now();
        int ds = r.e.displayStreak(now);
        boolean met = r.e.todayMet(now);
        Sheet s = new Sheet(a, null);
        Views.FlameView fv = new Views.FlameView(a);
        fv.set(Game.flameStage(Math.max(1, ds)), met);
        LinearLayout.LayoutParams fl = new LinearLayout.LayoutParams(U.dp(120), U.dp(130));
        fl.gravity = Gravity.CENTER_HORIZONTAL;
        s.add(fv, fl);
        s.add(center(a, ds + " günlük seri", 28, Th.text));
        s.add(center(a, met ? "Bugün tamam, alev yanıyor 🔥" : "Bugünün gereksinimlerini tamamla, alev yansın", 14, Th.dim));
        s.add(center(a, Game.FLAME_NAME[Game.flameStage(Math.max(1, ds))] + " · sonraki kilometre taşı: " + Game.nextMilestone(ds) + " gün", 13, Art.flameColor(Game.flameStage(Math.max(1, ds)))), U.m(U.lpMW(), 0, 6, 0, 0));
        LinearLayout stats = U.h(a);
        stats.addView(stat(a, "🏆", String.valueOf(r.s.p.bestStreak), "En uzun"), U.lpW(1));
        stats.addView(stat(a, "🧊", r.s.p.freezes + "/" + Game.MAX_FREEZES, "Dondurma"), U.lpW(1));
        stats.addView(stat(a, "😴", String.valueOf(r.s.p.sleepStreak), "Uyku serisi"), U.lpW(1));
        s.add(stats, U.m(U.lpMW(), 0, 16, 0, 0));
        // son 14 gün
        s.add(label(a, "SON 14 GÜN"));
        LinearLayout days = U.h(a);
        int td = r.e.today(now);
        for (int d = td - 13; d <= td; d++) {
            DayRec dr = r.s.dayOrNull(d);
            String e = d == td ? (met ? "🔥" : "⏳") : dr == null ? "·" : dr.good() ? "🔥" : dr.state == DayRec.FROZEN ? "🧊" : dr.state == DayRec.BROKEN ? "💔" : "·";
            TextView t = U.tv(a, e, 14, Th.text, 400);
            t.setGravity(Gravity.CENTER);
            days.addView(t, U.lpW(1));
        }
        s.add(days);
        s.add(note(a, "🧊 Seri Dondurma", "Bir günü kaçırırsan otomatik kullanılır ve serin kırılmaz. Her hafta 1 ücretsiz hak yenilenir; mağazadan en fazla " + Game.MAX_FREEZES + " tane biriktirebilirsin.", Th.water), U.m(U.lpMW(), 0, 14, 0, 0));
        s.add(note(a, "🦅 Seri Onarma", "Seri kırılırsa 48 saatin var: sonraki 2 günü tamamlarsan eski serin geri gelir.", Th.primary), U.m(U.lpMW(), 0, 8, 0, 0));
        s.add(note(a, "🟡 Mini versiyonlar", "Kötü bir günde mini versiyonu yapmak da seriyi uzatır. Asla iki kez kaçırma!", Th.warn), U.m(U.lpMW(), 0, 8, 0, 0));
        U.Btn buy = U.ghost(a, "🧊 Dondurma satın al (200 🪙)");
        buy.setOnClickListener(v -> {
            if (r.e.buy("freeze", r.now())) { r.commit(); a.toast("🧊 Seri dondurma eklendi"); s.dismiss(); }
            else a.toast(r.s.p.freezes >= Game.MAX_FREEZES ? "Depo dolu (en fazla " + Game.MAX_FREEZES + ")" : "Yeterli coin yok 🪙");
        });
        s.add(buy, U.m(U.lpMW(), 0, 14, 0, 0));
        if (ds > 0) {
            U.Btn share = U.ghost(a, "📤 Serimi paylaş");
            share.setOnClickListener(v -> shareStreak(a, ds));
            s.add(share, U.m(U.lpMW(), 0, 10, 0, 0));
        }
        a.show(s);
    }

    static View stat(MainActivity a, String emoji, String val, String lbl) {
        LinearLayout c = U.v(a);
        c.setGravity(Gravity.CENTER);
        c.setBackground(U.rr(Th.surface2, 16));
        c.setPadding(0, U.dp(10), 0, U.dp(10));
        c.addView(center(a, emoji + " " + val, 18, Th.text));
        c.addView(center(a, lbl, 12, Th.dim));
        LinearLayout wrap = U.v(a);
        wrap.setPadding(U.dp(3), 0, U.dp(3), 0);
        wrap.addView(c);
        return wrap;
    }

    // ---------------- Bekleyen bildirimler ----------------
    public static void notices(final MainActivity a, ArrayList<String> list) {
        StringBuilder sb = new StringBuilder();
        String title = "Sen yokken olanlar";
        boolean any = false;
        for (String n : list) {
            String[] p = n.split("\\|", 3);
            if (p.length < 3) continue;
            int type = Integer.parseInt(p[0]);
            if (type == Ev.BADGE) { sb.append("🏅 Yeni rozet: ").append(p[2]).append("\n\n"); any = true; }
            else if (type == Ev.LEVEL_UP) { sb.append("⭐ Seviye ").append(p[1]).append(": ").append(p[2]).append("\n\n"); any = true; }
            else if (type == Ev.PERFECT) { sb.append("💎 Mükemmel Gün yaşadın!\n\n"); any = true; }
            else if (type == Ev.QUESTS_ALL) { sb.append("🎁 Günlük görevlerin hepsi tamam, sandık kazandın!\n\n"); any = true; }
            else if (type == Ev.MILESTONE) { sb.append("🏆 ").append(p[2]).append("\n\n"); any = true; }
            else { sb.append(p[2]).append("\n\n"); any = true; }
        }
        if (!any) return;
        Sheet s = new Sheet(a, "📬 " + title);
        s.add(U.tv(a, sb.toString().trim(), 15, Th.text, 700));
        U.Btn ok = U.btn(a, "TAMAM", Th.primary);
        ok.setOnClickListener(v -> s.dismiss());
        s.add(ok, U.m(U.lpMW(), 0, 18, 0, 0));
        a.show(s);
    }

    // ---------------- Paylaşım kartı ----------------
    public static void shareStreak(MainActivity a, int n) {
        try {
            int w = 1080, h = 1920;
            Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            int stage = Game.flameStage(n);
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setShader(new LinearGradient(0, 0, 0, h, Art.mix(0xFF000000, Art.flameColor(stage), 0.35f), 0xFF0B0B12, Shader.TileMode.CLAMP));
            c.drawRect(0, 0, w, h, p);
            p.setShader(null);
            Art.flame(c, w / 2f, h * 0.55f, h * 0.32f, stage, true, 0.3f);
            p.setTextAlign(Paint.Align.CENTER);
            p.setColor(0xFFFFFFFF);
            p.setTypeface(U.font(900));
            p.setTextSize(260);
            c.drawText(String.valueOf(n), w / 2f, h * 0.72f, p);
            p.setTextSize(72);
            p.setColor(Art.flameColor(stage));
            c.drawText("günlük seri 🔥", w / 2f, h * 0.78f, p);
            p.setTextSize(44);
            p.setColor(0xCCFFFFFF);
            p.setTypeface(U.font(700));
            c.drawText("Her gün küçük bir adım. — Rutin", w / 2f, h * 0.9f, p);
            if (Build.VERSION.SDK_INT >= 29) {
                ContentValues cv = new ContentValues();
                cv.put(MediaStore.Images.Media.DISPLAY_NAME, "rutin_seri_" + n + "_" + System.currentTimeMillis() + ".png");
                cv.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                cv.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Rutin");
                Uri uri = a.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
                if (uri == null) { a.toast("Kaydedilemedi"); return; }
                try (OutputStream o = a.getContentResolver().openOutputStream(uri)) { b.compress(Bitmap.CompressFormat.PNG, 100, o); }
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("image/png");
                i.putExtra(Intent.EXTRA_STREAM, uri);
                i.putExtra(Intent.EXTRA_TEXT, n + " günlük serim var! 🔥");
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                a.startActivity(Intent.createChooser(i, "Serini paylaş"));
            } else a.toast("Paylaşım Android 10+ gerektirir");
        } catch (Exception e) {
            a.toast("Paylaşılamadı: " + e.getMessage());
        }
    }
}
