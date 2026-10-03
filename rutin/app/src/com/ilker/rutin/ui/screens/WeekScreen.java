package com.ilker.rutin.ui.screens;

import android.annotation.SuppressLint;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ilker.rutin.core.Area;
import com.ilker.rutin.core.DayRec;
import com.ilker.rutin.core.Habit;
import com.ilker.rutin.core.Quests;
import com.ilker.rutin.core.State;
import com.ilker.rutin.core.Task;
import com.ilker.rutin.core.Tm;
import com.ilker.rutin.ui.Art;
import com.ilker.rutin.ui.MainActivity;
import com.ilker.rutin.ui.Sheet;
import com.ilker.rutin.ui.TaskRow;
import com.ilker.rutin.ui.Th;
import com.ilker.rutin.ui.U;
import com.ilker.rutin.ui.Views;
import com.ilker.rutin.ui.sheets.DaySheets;
import com.ilker.rutin.ui.sheets.Sheets;

import java.util.ArrayList;

/** Haftalık plan: gün şeridi, günün işleri, alışkanlık planı ve şablonlar. */
@SuppressLint("ViewConstructor")
public class WeekScreen extends Screen {
    int sel = Integer.MIN_VALUE;
    int weekOffset = 0;

    public WeekScreen(MainActivity a) { super(a); }

    @Override
    protected void onInsets() { refresh(); }

    @Override
    public void refresh() {
        long now = now();
        int td = r.e.today(now);
        if (sel == Integer.MIN_VALUE) sel = td;
        int ws = Tm.weekStart(td) + weekOffset * 7;
        col.removeAllViews();
        col.setPadding(U.dp(16), top + U.dp(14), U.dp(16), U.dp(24));
        LinearLayout head = U.h(a);
        TextView prev = U.tv(a, "‹", 28, Th.text, 900);
        prev.setPadding(U.dp(4), 0, U.dp(14), 0);
        prev.setOnClickListener(v -> { weekOffset--; sel = Tm.weekStart(td) + weekOffset * 7; refresh(); });
        head.addView(prev);
        LinearLayout ht = U.v(a);
        ht.addView(U.title(a, weekOffset == 0 ? "Bu Hafta" : weekOffset == 1 ? "Gelecek Hafta" : weekOffset == -1 ? "Geçen Hafta" : "Hafta"));
        ht.addView(U.tv(a, Tm.dateShort(ws) + " – " + Tm.dateShort(ws + 6), 13, Th.dim, 700));
        head.addView(ht, U.lpW(1));
        TextView next = U.tv(a, "›", 28, Th.text, 900);
        next.setPadding(U.dp(14), 0, U.dp(4), 0);
        next.setOnClickListener(v -> { if (weekOffset < 2) { weekOffset++; sel = Tm.weekStart(td) + weekOffset * 7; refresh(); } });
        head.addView(next);
        col.addView(head, U.mb(U.lpMW(), 12));

        // Gün şeridi
        LinearLayout strip = U.h(a);
        for (int d = ws; d < ws + 7; d++) {
            final int dd = d;
            if (d >= td && d <= td + 21) r.e.ensureDay(d, now);
            LinearLayout cell = U.v(a);
            cell.setGravity(Gravity.CENTER);
            cell.setPadding(0, U.dp(8), 0, U.dp(8));
            boolean isSel = d == sel;
            cell.setBackground(isSel ? U.rr(Th.primary, 16) : d == td ? U.rrs(Th.surface, 16, Th.primary, 1.5f) : U.rr(Th.surface, 16));
            int fg = isSel ? U.Btn.contrastPublic(Th.primary) : Th.text;
            cell.addView(center(Tm.DAY_SHORT[Tm.weekday(d)], 12, isSel ? fg : Th.dim));
            cell.addView(center(String.valueOf(Tm.ymd(d)[2]), 18, fg));
            DayRec rec = r.s.dayOrNull(d);
            String mark;
            if (d > td) { int n = r.s.tasksOf(d).size(); mark = n > 0 ? String.valueOf(n) : "·"; }
            else if (d == td) mark = rec != null && rec.met ? "🔥" : "⏳";
            else if (rec == null) mark = "·";
            else mark = rec.perfect ? "💎" : rec.good() ? "🔥" : rec.state == DayRec.FROZEN ? "🧊" : rec.state == DayRec.BROKEN ? "💔" : "·";
            cell.addView(center(mark, 12, fg));
            cell.setOnClickListener(v -> { sel = dd; U.haptic(v); refresh(); });
            strip.addView(cell, U.m(U.lpW(1), 2, 0, 2, 0));
        }
        col.addView(strip, U.mb(U.lpMW(), 8));

        // Seçili gün
        LinearLayout dh = U.h(a);
        dh.addView(U.section(a, Fmt_up(Tm.dateLong(sel))), U.lpW(1));
        TextView add = U.chip(a, "+ Ekle", Art.alpha(Th.primary, 0.15f), Th.primary);
        add.setOnClickListener(v -> Sheets.once(a, sel));
        if (sel >= td) dh.addView(add);
        col.addView(dh);
        ArrayList<Task> list = r.e.sorted(sel);
        if (list.isEmpty()) col.addView(U.tv(a, sel > td + 21 ? "Bu gün henüz planlanmadı." : "Bu gün için iş yok 🌿", 14, Th.dim, 700), U.mb(U.lpMW(), 8));
        for (Task t : list) col.addView(new TaskRow(a, t, true), U.mb(U.lpMW(), 8));

        // Haftalık meydan okuma
        if (weekOffset == 0 && r.s.p.weekly != null) {
            State.Quest w = r.s.p.weekly;
            LinearLayout c = U.card(a);
            c.addView(U.tv(a, "🏅 Haftalık meydan okuma", 13, Th.dim, 800));
            c.addView(U.tv(a, Quests.emoji(w.kind) + " " + Quests.text(w), 16, Th.text, 900));
            Views.WaveBar b = new Views.WaveBar(a, w.done ? Th.success : Th.gold);
            b.set(w.target == 0 ? 0 : w.progress / (float) w.target, false);
            b.animating = false;
            c.addView(b, U.m(new LinearLayout.LayoutParams(-1, U.dp(10)), 0, 8, 0, 0));
            c.addView(U.tv(a, (w.done ? "✅ Tamamlandı!" : w.progress + " / " + w.target) + "  ·  Ödül: 100 XP + 🎁 Epik sandık", 12, Th.dim, 700), U.m(U.lpMW(), 0, 6, 0, 0));
            col.addView(c, U.m(U.lpMW(), 0, 14, 0, 0));
        }

        // Planlama
        LinearLayout plan = U.h(a);
        U.Btn pb = U.btn(a, "🗺️ Haftayı planla", Th.primary);
        pb.setTextSize(14);
        pb.setOnClickListener(v -> DaySheets.planWeek(a));
        plan.addView(pb, U.m(U.lpW(1), 0, 0, 4, 0));
        U.Btn rp = U.ghost(a, "📊 Rapor");
        rp.setTextSize(14);
        rp.setOnClickListener(v -> DaySheets.weeklyReport(a, Tm.weekStart(td) - (Tm.weekday(td) < 5 ? 7 : 0)));
        plan.addView(rp, U.m(U.lpW(1), 4, 0, 0, 0));
        col.addView(plan, U.m(U.lpMW(), 0, 16, 0, 0));

        // Alışkanlıklar = şablon hafta
        LinearLayout hh = U.h(a);
        hh.addView(U.section(a, "ALIŞKANLIKLARIM · HAFTALIK PLAN"), U.lpW(1));
        TextView newH = U.chip(a, "+ Yeni", Art.alpha(Th.primary, 0.15f), Th.primary);
        newH.setOnClickListener(v -> Sheets.habitEditor(a, null, -1));
        hh.addView(newH);
        col.addView(hh);
        for (final Habit h : r.s.activeHabits()) {
            LinearLayout row = U.h(a);
            row.setBackground(U.rr(Th.surface, 18));
            row.setPadding(U.dp(12), U.dp(10), U.dp(12), U.dp(10));
            row.addView(U.emojiBox(a, h.emoji, Area.COLOR[h.area], 38));
            row.addView(U.space(a, 10));
            LinearLayout c = U.v(a);
            c.addView(U.tv(a, h.title, 15, Th.text, 800));
            String info = Habit.TYPE_NAME[h.type] + (h.targetMin >= 0 ? " · " + Tm.hhmm(h.targetMin) : "") + (h.isQuantity() ? " · " + h.fmtAmount(h.full) : "") + (h.curStreak > 0 ? " · 🔥" + h.curStreak : "") + (h.graduated ? " · 🎓" : "");
            c.addView(U.tv(a, info, 12, Th.dim, 700));
            LinearLayout dots = U.h(a);
            for (int i = 0; i < 7; i++) {
                boolean on = (h.daysMask & (1 << i)) != 0;
                TextView d = U.tv(a, Tm.DAY_LETTER[i], 10, on ? U.Btn.contrastPublic(Th.primary) : Th.dim, 900);
                d.setGravity(Gravity.CENTER);
                d.setBackground(on ? U.rr(Th.primary, 10) : U.rr(Th.surface2, 10));
                dots.addView(d, U.m(new LinearLayout.LayoutParams(U.dp(20), U.dp(20)), 0, 4, 3, 0));
            }
            c.addView(dots);
            row.addView(c, U.lpW(1));
            row.setOnClickListener(v -> Sheets.habitEditor(a, h, -1));
            U.squish(row);
            col.addView(row, U.mb(U.lpMW(), 8));
        }

        // Şablonlar
        col.addView(U.section(a, "📋 ŞABLON HAFTALAR"));
        LinearLayout tc = U.card(a);
        tc.addView(U.tv(a, "İdeal haftanı şablon olarak kaydet. Okul, iş, sınav, tatil gibi farklı haftalar için ayrı şablonların olsun; her pazartesi aktif şablon otomatik yüklenir.", 13, Th.dim, 700));
        for (final State.Template t : r.s.templates) {
            LinearLayout row = U.h(a);
            row.setPadding(0, U.dp(8), 0, 0);
            boolean active = t.id == r.s.p.activeTemplate;
            row.addView(U.tv(a, t.emoji + " " + t.name + (active ? "  ✓ aktif" : ""), 15, active ? Th.primary : Th.text, 800), U.lpW(1));
            TextView ap = U.chip(a, "Uygula", Th.surface2, Th.text);
            ap.setOnClickListener(v -> { r.e.applyTemplate(t, r.e.today(now()), now()); r.commit(); a.toast(t.emoji + " " + t.name + " uygulandı"); });
            row.addView(ap);
            row.addView(U.space(a, 6));
            TextView del = U.chip(a, "✕", Th.surface2, Th.dim);
            del.setOnLongClickListener(v -> { r.s.templates.remove(t); if (r.s.p.activeTemplate == t.id) r.s.p.activeTemplate = 0; r.commit(); return true; });
            del.setOnClickListener(v -> a.toast("Silmek için uzun bas"));
            row.addView(del);
            tc.addView(row);
        }
        final TextView auto = U.pill(a, "Pazartesi otomatik yükle: " + (r.s.p.autoTemplate ? "AÇIK" : "KAPALI"), r.s.p.autoTemplate);
        auto.setOnClickListener(v -> { r.s.p.autoTemplate = !r.s.p.autoTemplate; r.commit(); });
        tc.addView(auto, U.m(U.lpWW(), 0, 12, 0, 0));
        U.Btn save = U.ghost(a, "💾 Bu planı şablon olarak kaydet");
        save.setTextSize(14);
        save.setOnClickListener(v -> saveTemplate());
        tc.addView(save, U.m(U.lpMW(), 0, 12, 0, 0));
        col.addView(tc, U.mb(U.lpMW(), 70));
    }

    void saveTemplate() {
        final Sheet s = new Sheet(a, "💾 Şablon kaydet");
        final EditText name = U.input(a, "Örn: Okul Haftası", "", false);
        s.add(name);
        final String[] em = {"🎓"};
        final String[] opts = {"🎓", "💼", "📚", "🏖️", "🤒", "🏋️", "🌙", "📋"};
        LinearLayout er = U.h(a);
        final ArrayList<TextView> es = new ArrayList<>();
        for (final String o : opts) {
            TextView t = U.tv(a, o, 24, Th.text, 400);
            t.setGravity(Gravity.CENTER);
            t.setBackground(o.equals(em[0]) ? U.rr(Art.alpha(Th.primary, 0.3f), 10) : null);
            t.setOnClickListener(v -> { em[0] = o; for (TextView x : es) x.setBackground(x == v ? U.rr(Art.alpha(Th.primary, 0.3f), 10) : null); });
            es.add(t);
            er.addView(t, U.lpW(1));
        }
        s.add(er, U.m(U.lpMW(), 0, 10, 0, 0));
        U.Btn ok = U.btn(a, "KAYDET", Th.primary);
        ok.setOnClickListener(v -> {
            String n = name.getText().toString().trim();
            if (n.isEmpty()) { name.setError("Bir ad ver"); return; }
            r.e.saveTemplate(n, em[0]);
            r.commit();
            a.toast("📋 " + n + " kaydedildi ve aktif");
            s.dismiss();
        });
        s.add(ok, U.m(U.lpMW(), 0, 16, 0, 0));
        a.show(s);
    }

    static String Fmt_up(String s) { return com.ilker.rutin.core.Fmt.upper(s); }

    TextView center(String s, float sp, int c) {
        TextView t = U.tv(a, s, sp, c, 800);
        t.setGravity(Gravity.CENTER);
        return t;
    }
}
