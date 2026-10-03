package com.ilker.rutin.ui.screens;

import android.annotation.SuppressLint;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ilker.rutin.core.Badges;
import com.ilker.rutin.core.Coach;
import com.ilker.rutin.core.DayRec;
import com.ilker.rutin.core.Fmt;
import com.ilker.rutin.core.Game;
import com.ilker.rutin.core.State;
import com.ilker.rutin.ui.Art;
import com.ilker.rutin.ui.MainActivity;
import com.ilker.rutin.ui.Th;
import com.ilker.rutin.ui.U;
import com.ilker.rutin.ui.Views;
import com.ilker.rutin.ui.sheets.MeSheets;
import com.ilker.rutin.ui.sheets.Sheets;

/** Ben: yoldaş + hayat adası, seviye, rozetler, mağaza, ödüller, kimlik, ayarlar. */
@SuppressLint("ViewConstructor")
public class MeScreen extends Screen {
    public MeScreen(MainActivity a) { super(a); }

    @Override
    protected void onInsets() { refresh(); }

    @Override
    public void refresh() {
        long now = now();
        int td = r.e.today(now);
        State.Profile p = r.s.p;
        col.removeAllViews();
        col.setPadding(U.dp(16), top + U.dp(10), U.dp(16), U.dp(24));

        // Ada + yoldaş
        FrameLayout hero = new FrameLayout(a);
        Views.IslandView iv = new Views.IslandView(a);
        iv.items = r.s.island;
        iv.seed = p.firstDay;
        int nd = r.e.nowDm(now);
        iv.night = nd < r.e.wakeDm() || nd > 1140;
        hero.addView(iv, new FrameLayout.LayoutParams(-1, -1));
        Views.CompanionView cv = new Views.CompanionView(a);
        cv.species = p.companion; cv.hat = p.hat; cv.acc = p.acc;
        DayRec rec = r.s.dayOrNull(td);
        int daysAway = p.lastActiveDay >= 0 ? td - p.lastActiveDay : 0;
        cv.mood = rec != null && rec.perfect ? Art.MOOD_EXCITED : rec != null && rec.met ? Art.MOOD_HAPPY : daysAway >= 2 ? Art.MOOD_MISS : nd > r.e.sleepDm() - 60 ? Art.MOOD_SLEEPY : Art.MOOD_OK;
        int good = 0;
        for (DayRec d : r.s.days) if (d.good()) good++;
        cv.stage = good < 7 ? 0 : good < 30 ? 1 : 2;
        FrameLayout.LayoutParams cl = U.flp(U.dp(130), U.dp(130), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        cl.bottomMargin = U.dp(6);
        hero.addView(cv, cl);
        TextView nm = U.chip(a, p.companionName + " · " + (cv.stage == 0 ? "Yavru" : cv.stage == 1 ? "Genç" : "Yetişkin") + " " + Art.SPECIES_EMOJI[p.companion], 0xCC000000, 0xFFFFFFFF);
        FrameLayout.LayoutParams nl = U.flp(-2, -2, Gravity.TOP | Gravity.START);
        nl.setMargins(U.dp(12), U.dp(12), 0, 0);
        hero.addView(nm, nl);
        TextView isl = U.chip(a, "🏝️ " + r.s.island.size(), 0xCC000000, 0xFFFFFFFF);
        FrameLayout.LayoutParams il = U.flp(-2, -2, Gravity.TOP | Gravity.END);
        il.setMargins(0, U.dp(12), U.dp(12), 0);
        hero.addView(isl, il);
        hero.setOnClickListener(v -> MeSheets.island(a));
        col.addView(hero, new LinearLayout.LayoutParams(-1, U.dp(260)));
        TextView bubble = U.tv(a, "💬 " + Coach.companionLine(r.e, now), 14, Th.text, 700);
        bubble.setBackground(U.rr(Th.surface2, 16));
        bubble.setPadding(U.dp(14), U.dp(10), U.dp(14), U.dp(10));
        col.addView(bubble, U.m(U.lpMW(), 0, 10, 0, 0));
        LinearLayout hb = U.h(a);
        U.Btn dress = U.ghost(a, "👕 Giydir");
        dress.setTextSize(14);
        dress.setOnClickListener(v -> MeSheets.shop(a, Game.C_HAT));
        U.Btn island = U.ghost(a, "🏝️ Adam");
        island.setTextSize(14);
        island.setOnClickListener(v -> MeSheets.island(a));
        hb.addView(dress, U.m(U.lpW(1), 0, 0, 4, 0));
        hb.addView(island, U.m(U.lpW(1), 4, 0, 0, 0));
        col.addView(hb, U.m(U.lpMW(), 0, 10, 0, 12));

        // Seviye
        LinearLayout lc = U.card(a);
        int lv = Game.level(p.xp);
        LinearLayout lr = U.h(a);
        TextView big = U.tv(a, Game.TITLE_EMOJI[Game.titleIndex(lv)], 40, Th.text, 400);
        lr.addView(big);
        lr.addView(U.space(a, 12));
        LinearLayout lcol = U.v(a);
        lcol.addView(U.tv(a, (p.name.isEmpty() ? "Sen" : p.name) + " · Seviye " + lv, 18, Th.text, 900));
        lcol.addView(U.tv(a, Game.TITLES[Game.titleIndex(lv)] + " · " + Fmt.num(p.xp) + " XP · sonraki seviyeye " + Fmt.num(Game.levelStart(lv + 1) - p.xp), 12.5f, Th.dim, 700));
        Views.WaveBar xb = new Views.WaveBar(a, Th.gold);
        xb.set((float) Game.levelProgress(p.xp), false);
        lcol.addView(xb, U.m(new LinearLayout.LayoutParams(-1, U.dp(10)), 0, 8, 0, 0));
        lr.addView(lcol, U.lpW(1));
        lc.addView(lr);
        LinearLayout chips = U.h(a);
        chips.addView(U.chip(a, "🪙 " + p.coins, Art.alpha(Th.gold, 0.15f), Th.gold));
        chips.addView(U.space(a, 6));
        chips.addView(U.chip(a, "🧊 " + p.freezes, Art.alpha(Th.water, 0.15f), Th.water));
        chips.addView(U.space(a, 6));
        if (!r.s.chests.isEmpty()) {
            TextView ch = U.chip(a, "🎁 " + r.s.chests.size() + " sandık", Th.gold, 0xFF2A1E00);
            ch.setOnClickListener(v -> MeSheets.chests(a));
            chips.addView(ch);
        }
        lc.addView(chips, U.m(U.lpMW(), 0, 12, 0, 0));
        col.addView(lc, U.mb(U.lpMW(), 12));

        // Menü
        LinearLayout menu = U.h(a);
        menu.addView(menuBtn("🛍️", "Mağaza", () -> MeSheets.shop(a, 0)), U.lpW(1));
        menu.addView(menuBtn("🍰", "Ödüllerim", () -> MeSheets.rewards(a)), U.lpW(1));
        menu.addView(menuBtn("🪞", "Kimliğim", () -> MeSheets.identity(a)), U.lpW(1));
        menu.addView(menuBtn("⚙️", "Ayarlar", () -> MeSheets.settings(a)), U.lpW(1));
        col.addView(menu, U.mb(U.lpMW(), 8));
        LinearLayout menu2 = U.h(a);
        menu2.addView(menuBtn("🃏", "Kartlar " + r.s.cards.size() + "/" + com.ilker.rutin.core.Rewards.CARDS.length, () -> MeSheets.album(a)), U.lpW(1));
        menu2.addView(menuBtn("🎁", "Sandıklar " + r.s.chests.size(), () -> MeSheets.chests(a)), U.lpW(1));
        menu2.addView(menuBtn("🎯", "Günlük hedef", () -> MeSheets.goalPicker(a)), U.lpW(1));
        menu2.addView(menuBtn("📅", "Ay özeti", () -> com.ilker.rutin.ui.sheets.DaySheets.wrapped(a)), U.lpW(1));
        col.addView(menu2, U.mb(U.lpMW(), 8));

        // Kimlik vitrini
        if (!p.identities.isEmpty() || (p.promise != null && !p.promise.isEmpty())) {
            LinearLayout idc = U.card(a);
            for (String s : p.identities) idc.addView(U.tv(a, "🪞 " + s, 15, Th.text, 800));
            if (p.promise != null && !p.promise.isEmpty()) {
                idc.addView(U.tv(a, "✍️ \"" + p.promise + "\"", 13.5f, Th.dim, 700), U.m(U.lpMW(), 0, 8, 0, 0));
                if (p.signature != null && !p.signature.isEmpty()) {
                    Views.SignatureShow sg = new Views.SignatureShow(a);
                    sg.data = p.signature;
                    idc.addView(sg, new LinearLayout.LayoutParams(-1, U.dp(70)));
                }
            }
            idc.setOnClickListener(v -> MeSheets.identity(a));
            col.addView(idc, U.mb(U.lpMW(), 12));
        }

        // Rozetler
        int have = 0;
        for (Badges.B b : Badges.ALL) if (r.s.badgeTier(b.id) > 0) have++;
        col.addView(U.section(a, "ROZETLER · " + have + "/" + Badges.ALL.length));
        LinearLayout grid = null;
        int i = 0;
        for (final Badges.B b : Badges.ALL) {
            if (i % 4 == 0) { grid = U.h(a); col.addView(grid, U.mb(U.lpMW(), 6)); }
            int tier = r.s.badgeTier(b.id);
            LinearLayout cell = U.v(a);
            cell.setGravity(Gravity.CENTER_HORIZONTAL);
            Views.MedalView m = new Views.MedalView(a);
            m.emoji = b.emoji;
            m.locked = tier == 0;
            m.color = Badges.TIER_COLOR[tier == 0 ? 0 : b.tiers.length == 1 ? 3 : tier];
            cell.addView(m, new LinearLayout.LayoutParams(U.dp(64), U.dp(64)));
            TextView t = U.tv(a, b.hidden && tier == 0 ? "???" : b.name, 11, tier > 0 ? Th.text : Th.dim, 800);
            t.setGravity(Gravity.CENTER);
            t.setMaxLines(2);
            cell.addView(t);
            cell.setOnClickListener(v -> MeSheets.badge(a, b));
            grid.addView(cell, U.lpW(1));
            i++;
        }
        while (i % 4 != 0) { grid.addView(new View(a), U.lpW(1)); i++; }
        col.addView(U.space(a, 70));
    }

    View menuBtn(String e, String l, final Runnable r) {
        LinearLayout c = U.v(a);
        c.setGravity(Gravity.CENTER);
        c.setBackground(U.rr(Th.surface, 18));
        c.setPadding(0, U.dp(12), 0, U.dp(12));
        TextView et = U.tv(a, e, 24, Th.text, 400);
        et.setGravity(Gravity.CENTER);
        c.addView(et);
        TextView lt = U.tv(a, l, 12, Th.text, 800);
        lt.setGravity(Gravity.CENTER);
        c.addView(lt);
        c.setOnClickListener(v -> { U.haptic(v); r.run(); });
        U.squish(c);
        LinearLayout w = U.v(a);
        w.setPadding(U.dp(3), 0, U.dp(3), 0);
        w.addView(c);
        return w;
    }
}
