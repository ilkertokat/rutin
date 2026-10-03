import os
os.chdir(os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'app', 'src', 'com', 'ilker', 'rutin'))


def rep(p, a, b):
    s = open(p, encoding='utf-8').read()
    assert a in s, (p, a[:60])
    s = s.replace(a, b, 1)
    open(p, 'w', encoding='utf-8').write(s)


T = 'ui/screens/TodayScreen.java'
rep(T, """        legend.addView(xpb, new LinearLayout.LayoutParams(-1, U.dp(8)));""", """        legend.addView(xpb, new LinearLayout.LayoutParams(-1, U.dp(8)));
        TextView goalT = U.tv(a, (rec.goalHit ? "✅" : "🎯") + " Günlük hedef " + Math.min(rec.xp, p.dailyGoal) + "/" + p.dailyGoal + " XP", 12.5f, rec.goalHit ? Th.success : Th.dim, 800);
        goalT.setPadding(0, U.dp(8), 0, U.dp(3));
        goalT.setOnClickListener(v -> com.ilker.rutin.ui.sheets.MeSheets.goalPicker(a));
        legend.addView(goalT);
        Views.WaveBar gb = new Views.WaveBar(a, rec.goalHit ? Th.success : Th.primary);
        gb.set(Math.min(1f, rec.xp / (float) Math.max(1, p.dailyGoal)), false);
        gb.animating = false;
        legend.addView(gb, new LinearLayout.LayoutParams(-1, U.dp(6)));""")
rep(T, """        if (!r.s.chests.isEmpty()) {""", """        if (com.ilker.rutin.core.Rewards.canSpin(r.e, now)) {
            LinearLayout sp = infoCard("🎡", "Şans çarkın hazır!", "Serini güvenceye aldın. Çevir ve ödülünü kap!", Th.primary);
            sp.setOnClickListener(v -> com.ilker.rutin.ui.SpinOverlay.show(a));
            U.squish(sp);
            col.addView(sp, U.mb(U.lpMW(), 10));
            sp.postDelayed(() -> U.bounce(sp), 400);
        }
        if (!r.s.chests.isEmpty()) {""")
rep(T, """        // ---- Zaman çizelgesi ----""", """        // ---- Kendinle yarış ----
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

        // ---- Zaman çizelgesi ----""")
rep(T, """    LinearLayout legendRow(""", """    View raceRow(String name, int v, int mx, int color) {
        LinearLayout l = U.h(a);
        l.addView(U.tv(a, name, 12.5f, Th.dim, 800), new LinearLayout.LayoutParams(U.dp(96), -2));
        Views.WaveBar b = new Views.WaveBar(a, color);
        b.set(v / (float) mx, false);
        b.animating = false;
        l.addView(b, new LinearLayout.LayoutParams(0, U.dp(10), 1));
        l.addView(U.tv(a, "  " + v + " XP", 12.5f, Th.text, 900));
        return l;
    }

    LinearLayout legendRow(""")
C = 'ui/Celebrate.java'
rep(C, """                case Ev.SLEEP_STREAK:""", """                case Ev.GOAL: a.confetti.rain(100); a.banner("🎯 Günlük hedef tamam! +20 🪙 + 🎁", Th.success, 0xFF0B2A18); break;
                case Ev.HABIT_LEVEL: a.banner("🌳 Alışkanlık seviye atladı: " + e.text, Th.surface2, Th.text); break;
                case Ev.FREEZE_EARNED: a.banner("🧊 " + e.text, Th.water, 0xFF06223A); break;
                case Ev.SLEEP_STREAK:""")
rep('sys/Sound.java', """                case Ev.COUNTER_FULL: s = Synth.SUCCESS; rk = 5; break;""", """                case Ev.COUNTER_FULL: case Ev.GOAL: s = Synth.SUCCESS; rk = 5; break;
                case Ev.HABIT_LEVEL: s = Synth.BADGE; rk = 5; break;""")
S = 'ui/sheets/Sheets.java'
rep(S, """            if (h.graduated) { stats.addView(U.space(a, 6)); stats.addView(U.chip(a, "🎓", Th.surface2, Th.gold)); }
            s.add(stats, U.m(U.lpMW(), 0, 8, 0, 4));""", """            if (h.graduated) { stats.addView(U.space(a, 6)); stats.addView(U.chip(a, "🎓", Th.surface2, Th.gold)); }
            s.add(stats, U.m(U.lpMW(), 0, 8, 0, 4));
            int nx = com.ilker.rutin.core.Rewards.habitToNext(h.totalDone);
            s.add(U.chip(a, com.ilker.rutin.core.Rewards.habitLevelName(h.totalDone) + (nx > 0 ? " · sonraki seviyeye " + nx + " tekrar" : " · en üst seviye!"), Art.alpha(Th.success, 0.15f), Th.success), U.m(U.lpWW(), 0, 6, 0, 4));""")
rep('ui/screens/ProgressScreen.java', """            if (hs.h.graduated) info += " · 🎓 otomatik";""", """            if (hs.h.graduated) info += " · 🎓 otomatik";
            info += " · " + com.ilker.rutin.core.Rewards.habitLevelName(hs.h.totalDone);""")
M = 'ui/screens/MeScreen.java'
rep(M, """        col.addView(menu, U.mb(U.lpMW(), 8));""", """        col.addView(menu, U.mb(U.lpMW(), 8));
        LinearLayout menu2 = U.h(a);
        menu2.addView(menuBtn("🃏", "Kartlar " + r.s.cards.size() + "/" + com.ilker.rutin.core.Rewards.CARDS.length, () -> MeSheets.album(a)), U.lpW(1));
        menu2.addView(menuBtn("🎁", "Sandıklar " + r.s.chests.size(), () -> MeSheets.chests(a)), U.lpW(1));
        menu2.addView(menuBtn("🎯", "Günlük hedef", () -> MeSheets.goalPicker(a)), U.lpW(1));
        menu2.addView(menuBtn("📅", "Ay özeti", () -> com.ilker.rutin.ui.sheets.DaySheets.wrapped(a)), U.lpW(1));
        col.addView(menu2, U.mb(U.lpMW(), 8));""")
MS = 'ui/sheets/MeSheets.java'
rep(MS, """    // ---------------- Kişisel ödüller ----------------""", """    // ---------------- Kart albümü ----------------
    public static void album(final MainActivity a) {
        final Repo r = a.r;
        Sheet s = new Sheet(a, "🃏 Kart Albümü · " + r.s.cards.size() + "/" + com.ilker.rutin.core.Rewards.CARDS.length, true);
        s.add(U.tv(a, "Sandıklardan ve şans çarkından kartlar çıkar. Hepsini topla! Tekrar gelen kartlar coin'e dönüşür.", 13, Th.dim, 700));
        LinearLayout grid = null;
        for (int i = 0; i < com.ilker.rutin.core.Rewards.CARDS.length; i++) {
            final com.ilker.rutin.core.Rewards.Card c = com.ilker.rutin.core.Rewards.CARDS[i];
            final boolean have = r.s.cards.contains(i);
            if (i % 3 == 0) { grid = U.h(a); s.add(grid, U.m(U.lpMW(), 0, 8, 0, 0)); }
            LinearLayout cell = U.v(a);
            cell.setGravity(Gravity.CENTER);
            int rc = com.ilker.rutin.core.Game.RARITY_COLOR[c.rarity];
            cell.setBackground(have ? U.rrs(Art.alpha(rc, 0.14f), 16, rc, 1.5f) : U.rr(Th.surface2, 16));
            cell.setPadding(U.dp(6), U.dp(12), U.dp(6), U.dp(12));
            TextView e = U.tv(a, have ? c.emoji : "❔", 30, Th.text, 400);
            e.setGravity(Gravity.CENTER);
            cell.addView(e);
            TextView n = U.tv(a, have ? c.title : com.ilker.rutin.core.Rewards.CARD_RARITY[c.rarity], 12, have ? Th.text : Th.dim, 800);
            n.setGravity(Gravity.CENTER);
            cell.addView(n);
            cell.setOnClickListener(v -> { if (have) a.banner(c.emoji + " " + c.title + ": " + c.text, Th.surface2, Th.text); else a.toast("Bu kart henüz kilitli 🔒"); });
            LinearLayout w = U.v(a);
            w.setPadding(U.dp(3), 0, U.dp(3), 0);
            w.addView(cell);
            grid.addView(w, U.lpW(1));
        }
        a.show(s);
    }

    // ---------------- Günlük XP hedefi ----------------
    public static void goalPicker(final MainActivity a) {
        final Repo r = a.r;
        final Sheet s = new Sheet(a, "🎯 Günlük XP hedefi");
        s.add(U.tv(a, "Her gün ulaşmak istediğin XP. Hedefe ulaşınca 🪙 20 coin ve bir sandık kazanırsın.", 14, Th.dim, 700));
        for (int i = 0; i < com.ilker.rutin.core.Rewards.GOALS.length; i++) {
            final int g = com.ilker.rutin.core.Rewards.GOALS[i];
            String sub = g <= 30 ? "Yoğun günler için" : g <= 50 ? "Çoğu gün için ideal" : g <= 100 ? "Tüm rutinleri tamamlayan biri için" : "Odak oturumları dahil her şey";
            LinearLayout row = U.row(a, null, com.ilker.rutin.core.Rewards.GOAL_NAMES[i] + " · " + g + " XP", sub, Th.primary);
            row.setBackground(U.rrs(Th.surface2, 16, r.s.p.dailyGoal == g ? Th.primary : Th.stroke, r.s.p.dailyGoal == g ? 2 : 1));
            row.setPadding(U.dp(14), U.dp(12), U.dp(14), U.dp(12));
            row.setOnClickListener(v -> { r.s.p.dailyGoal = g; r.commit(); a.toast("🎯 Günlük hedef: " + g + " XP"); s.dismiss(); });
            s.add(row, U.m(U.lpMW(), 0, 8, 0, 0));
        }
        a.show(s);
    }

    // ---------------- Kişisel ödüller ----------------""")
print("ok")
