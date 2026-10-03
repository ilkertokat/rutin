package com.ilker.rutin.ui.screens;

import android.annotation.SuppressLint;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ilker.rutin.core.Area;
import com.ilker.rutin.core.DayRec;
import com.ilker.rutin.core.Fmt;
import com.ilker.rutin.core.Insights;
import com.ilker.rutin.core.State;
import com.ilker.rutin.core.Tm;
import com.ilker.rutin.ui.Art;
import com.ilker.rutin.ui.MainActivity;
import com.ilker.rutin.ui.Th;
import com.ilker.rutin.ui.U;
import com.ilker.rutin.ui.Views;
import com.ilker.rutin.ui.sheets.DaySheets;

import java.util.ArrayList;

/** İlerleme: ısı haritası, haftalık grafik, alışkanlık istatistikleri, yaşam alanları, içgörüler, rekorlar. */
@SuppressLint("ViewConstructor")
public class ProgressScreen extends Screen {
    public ProgressScreen(MainActivity a) { super(a); }

    @Override
    protected void onInsets() { refresh(); }

    @Override
    public void refresh() {
        long now = now();
        int td = r.e.today(now);
        State.Profile p = r.s.p;
        col.removeAllViews();
        col.setPadding(U.dp(16), top + U.dp(14), U.dp(16), U.dp(24));
        col.addView(U.title(a, "İlerleme"));
        col.addView(U.tv(a, "Küçük kazanımların görünür hali 📈", 14, Th.dim, 700), U.mb(U.lpMW(), 14));
        LinearLayout tiles = U.h(a);
        tiles.addView(tile("🔥", String.valueOf(r.e.displayStreak(now)), "seri"), U.lpW(1));
        tiles.addView(tile("🏆", String.valueOf(p.bestStreak), "en uzun"), U.lpW(1));
        tiles.addView(tile("💎", String.valueOf(p.perfectDays), "mükemmel"), U.lpW(1));
        tiles.addView(tile("✅", Fmt.num(p.totalDone), "görev"), U.lpW(1));
        col.addView(tiles);

        // Isı haritası
        LinearLayout hc = U.card(a);
        hc.addView(U.tv(a, "🗓️ Son 20 hafta", 15, Th.text, 900));
        Views.HeatmapView hm = new Views.HeatmapView(a);
        hm.data = Insights.ratios(r.s, td, 140);
        hm.endWeekday = Tm.weekday(td);
        hc.addView(hm, U.m(U.lpMW(), 0, 10, 0, 6));
        LinearLayout leg = U.h(a);
        leg.addView(U.tv(a, "az ", 11, Th.dim, 700));
        for (float f : new float[]{0.1f, 0.4f, 0.7f, 1f}) {
            View v = new View(a);
            v.setBackground(U.rr(Art.mix(Art.alpha(Th.success, 0.25f), Th.success, f), 3));
            leg.addView(v, U.m(new LinearLayout.LayoutParams(U.dp(12), U.dp(12)), 2, 0, 2, 0));
        }
        leg.addView(U.tv(a, " çok   🧊 donduruldu", 11, Th.dim, 700));
        hc.addView(leg);
        col.addView(hc, U.m(U.lpMW(), 0, 12, 0, 12));

        // Son 7 gün
        LinearLayout wc = U.card(a);
        LinearLayout wh = U.h(a);
        wh.addView(U.tv(a, "📊 Son 7 gün", 15, Th.text, 900), U.lpW(1));
        TextView rep = U.chip(a, "Haftalık rapor ›", Th.surface2, Th.text);
        rep.setOnClickListener(v -> DaySheets.weeklyReport(a, Tm.weekStart(td)));
        wh.addView(rep);
        wc.addView(wh);
        Views.BarsView bars = new Views.BarsView(a);
        double[] v7 = new double[7];
        String[] l7 = new String[7];
        for (int i = 0; i < 7; i++) {
            int d = td - 6 + i;
            DayRec x = r.s.dayOrNull(d);
            v7[i] = x == null ? 0 : x.ratio;
            l7[i] = Tm.DAY_SHORT[Tm.weekday(d)];
        }
        bars.highlight = 6;
        bars.set(v7, l7, Th.success);
        wc.addView(bars, U.m(new LinearLayout.LayoutParams(-1, U.dp(140)), 0, 8, 0, 0));
        col.addView(wc, U.mb(U.lpMW(), 12));

        // Alışkanlıklar
        col.addView(U.section(a, "ALIŞKANLIKLAR · SON 30 GÜN"));
        for (Insights.HabitStat hs : Insights.habitStats(r.e, td, 30)) {
            LinearLayout row = U.card(a);
            row.setPadding(U.dp(14), U.dp(12), U.dp(14), U.dp(12));
            LinearLayout top1 = U.h(a);
            top1.addView(U.tv(a, hs.h.emoji + " " + hs.h.title, 15, Th.text, 900), U.lpW(1));
            top1.addView(U.tv(a, "🔥" + hs.h.curStreak + "  " + Math.round(hs.rate * 100) + "%", 14, hs.rate >= 0.8 ? Th.success : hs.rate >= 0.5 ? Th.warn : Th.crit, 900));
            row.addView(top1);
            Views.WaveBar b = new Views.WaveBar(a, hs.rate >= 0.8 ? Th.success : hs.rate >= 0.5 ? Th.warn : Th.crit);
            b.set((float) hs.rate, false);
            b.animating = false;
            row.addView(b, U.m(new LinearLayout.LayoutParams(-1, U.dp(8)), 0, 8, 0, 6));
            String info = hs.done + " tam · " + hs.mini + " mini · " + hs.scheduled + " planlı";
            if (hs.hasDelay) info += hs.avgDelay > 10 ? " · ortalama " + hs.avgDelay + " dk geç" : hs.avgDelay < -10 ? " · ortalama " + (-hs.avgDelay) + " dk erken 🐦" : " · zamanında ⏰";
            if (hs.h.graduated) info += " · 🎓 otomatik";
            info += " · " + com.ilker.rutin.core.Rewards.habitLevelName(hs.h.totalDone);
            row.addView(U.tv(a, info, 12, Th.dim, 700));
            col.addView(row, U.mb(U.lpMW(), 8));
        }

        // Yaşam alanları
        LinearLayout ac = U.card(a);
        ac.addView(U.tv(a, "🧬 Yaşam alanların", 15, Th.text, 900));
        Views.RadarView rv = new Views.RadarView(a);
        double[] sc = Insights.areaScores(r.s, td);
        int[] cnt = Insights.areaCounts(r.s);
        for (int i = 0; i < 6; i++) { rv.vals[i] = sc[i]; rv.labels[i] = Area.EMOJI[i] + " " + Area.NAME[i]; rv.colors[i] = Area.COLOR[i]; }
        ac.addView(rv, new LinearLayout.LayoutParams(-1, U.dp(250)));
        Views.Flow lv = new Views.Flow(a);
        for (int i = 0; i < 6; i++) lv.addView(U.chip(a, Area.EMOJI[i] + " Lv " + (1 + (int) Math.sqrt(cnt[i] / 3.0)), Art.alpha(Area.COLOR[i], 0.15f), Area.COLOR[i]));
        ac.addView(lv);
        int low = 0; double lowV = 9;
        for (int i = 0; i < 6; i++) if (sc[i] < lowV) { lowV = sc[i]; low = i; }
        if (p.totalDone > 10) ac.addView(U.tv(a, "💡 " + Area.EMOJI[low] + " " + Area.NAME[low] + " alanın bu ay düşük. Ona küçük bir alışkanlık eklemek dengeyi getirir.", 12.5f, Th.dim, 700), U.m(U.lpMW(), 0, 10, 0, 0));
        col.addView(ac, U.m(U.lpMW(), 4, 12, 0, 12));

        // İçgörüler
        ArrayList<String> corr = Insights.moodCorrelations(r.e, td);
        LinearLayout ic = U.card(a);
        ic.addView(U.tv(a, "💡 Sana özel içgörüler", 15, Th.text, 900));
        if (corr.isEmpty()) ic.addView(U.tv(a, "Birkaç gün boyunca gün sonu ruh halini girdikçe, hangi alışkanlıkların seni daha mutlu ettiğini burada göreceksin.", 13, Th.dim, 700), U.m(U.lpMW(), 0, 6, 0, 0));
        for (String s : corr) ic.addView(U.tv(a, "• " + s, 14, Th.text, 700), U.m(U.lpMW(), 0, 6, 0, 0));
        col.addView(ic, U.mb(U.lpMW(), 12));

        // Rekorlar
        LinearLayout rc = U.card(a);
        rc.addView(U.tv(a, "🏅 Kişisel rekorlar", 15, Th.text, 900));
        rc.addView(rec("🔥 En uzun seri", p.bestStreak + " gün"));
        rc.addView(rec("⏱️ Bir günde en çok odak", Tm.dur(p.maxFocusDay)));
        rc.addView(rec("🤿 En uzun kesintisiz oturum", Tm.dur(p.longestSession)));
        rc.addView(rec("🎯 En büyük kombo", p.bestCombo + "'li"));
        rc.addView(rec("😴 En uzun uyku serisi", p.bestSleepStreak + " gece"));
        rc.addView(rec("💧 Toplam su", Fmt.amount((int) Math.min(Integer.MAX_VALUE, p.waterTotal), "ml", 2)));
        rc.addView(rec("⏰ Dakiklik serisi", p.bestPunctual + " gün"));
        col.addView(rc, U.mb(U.lpMW(), 12));

        U.Btn wr = U.btn(a, "📅  AY ÖZETİMİ GÖSTER", Th.focus);
        wr.setOnClickListener(v -> DaySheets.wrapped(a));
        col.addView(wr, U.mb(U.lpMW(), 70));
    }

    View rec(String l, String v) {
        LinearLayout row = U.h(a);
        row.setPadding(0, U.dp(8), 0, 0);
        row.addView(U.tv(a, l, 14, Th.dim, 700), U.lpW(1));
        row.addView(U.tv(a, v, 14, Th.text, 900));
        return row;
    }

    View tile(String e, String v, String l) {
        LinearLayout c = U.v(a);
        c.setGravity(Gravity.CENTER);
        c.setBackground(U.rr(Th.surface, 18));
        c.setPadding(0, U.dp(12), 0, U.dp(12));
        TextView tv = U.tv(a, e + " " + v, 16, Th.text, 900);
        tv.setGravity(Gravity.CENTER);
        c.addView(tv);
        TextView tl = U.tv(a, l, 11.5f, Th.dim, 700);
        tl.setGravity(Gravity.CENTER);
        c.addView(tl);
        LinearLayout w = U.v(a);
        w.setPadding(U.dp(3), 0, U.dp(3), 0);
        w.addView(c);
        return w;
    }
}
