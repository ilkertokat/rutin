package com.ilker.rutin.ui;

import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ilker.rutin.core.Badges;
import com.ilker.rutin.core.DayRec;
import com.ilker.rutin.core.Ev;
import com.ilker.rutin.core.Game;
import com.ilker.rutin.core.Tm;
import com.ilker.rutin.data.Repo;
import com.ilker.rutin.sys.Sound;
import com.ilker.rutin.core.Synth;
import com.ilker.rutin.ui.sheets.Sheets;

import java.util.ArrayDeque;

/** Kutlama kuyruğu: küçük etkiler anında, büyük tam ekran kutlamalar sırayla. */
public final class Celebrate {
    final MainActivity a;
    final ArrayDeque<Runnable> queue = new ArrayDeque<>();
    boolean showing;

    Celebrate(MainActivity a) { this.a = a; }

    public void handle(Ev.List ev, View anchor) {
        int xp = 0;
        boolean streak = false, milestone = false;
        int streakVal = 0;
        for (Ev e : ev) {
            switch (e.type) {
                case Ev.XP: xp += e.value; break;
                case Ev.STREAK_UP: streak = true; streakVal = e.value; break;
                case Ev.MILESTONE: milestone = true; break;
            }
        }
        if (anchor != null && xp > 0) a.floatText(anchor, "+" + xp + " XP", Th.gold);
        for (Ev e : ev) {
            switch (e.type) {
                case Ev.TASK_DONE: case Ev.MINI_DONE:
                    if (anchor != null) a.confettiAt(anchor, e.type == Ev.TASK_DONE ? 28 : 14);
                    break;
                case Ev.EARLY: a.banner("🐦 Erkenci bonusu! +%20 XP", Th.success, 0xFF0B2A18); break;
                case Ev.HALFWAY: a.banner("💪 Yarıyı geçtin! " + e.text, Th.water, 0xFF06223A); break;
                case Ev.COUNTER_FULL: a.confetti.rain(140); a.banner("🎉 " + e.text + " hedefi tamam!", Th.success, 0xFF0B2A18); break;
                case Ev.COMBO: a.banner("🎯 KOMBO x" + e.value + "!  XP x1.5", Th.primary, 0xFFFFFFFF); break;
                case Ev.QUEST: a.banner("🎯 Görev tamam: " + e.text, Th.surface2, Th.text); break;
                case Ev.QUESTS_ALL: a.banner("🎁 3 görevin hepsi tamam! Sandık kazandın", Th.gold, 0xFF2A1E00); break;
                case Ev.GOAL: a.confetti.rain(100); a.banner("🎯 Günlük hedef tamam! +20 🪙 + 🎁", Th.success, 0xFF0B2A18); break;
                case Ev.HABIT_LEVEL: a.banner("🌳 Alışkanlık seviye atladı: " + e.text, Th.surface2, Th.text); break;
                case Ev.FREEZE_EARNED: a.banner("🧊 " + e.text, Th.water, 0xFF06223A); break;
                case Ev.SLEEP_STREAK: a.banner("😴 Uyku serisi: " + e.value + " gece", Th.focus, 0xFFFFFFFF); break;
                case Ev.GRADUATED: enqueue(() -> simple("🎓", "Mezuniyet!", e.text, "Bu alışkanlık artık hayatının doğal bir parçası. Bildirimleri azaltıyorum; sen zaten yapıyorsun!", Th.gold)); break;
            }
        }
        if (streak) { final int v = streakVal; final boolean m = milestone; enqueue(() -> streak(v, m)); }
        for (Ev e : ev) {
            switch (e.type) {
                case Ev.PERFECT: enqueue(this::perfect); break;
                case Ev.LEVEL_UP: enqueue(() -> level(e.value)); break;
                case Ev.BADGE: enqueue(() -> badge(e.extra, e.value)); break;
                case Ev.WEEKLY_DONE: enqueue(() -> simple("🏅", "Haftalık meydan okuma tamam!", e.text, "+100 XP ve 🎁 Epik sandık kazandın!", Th.gold)); break;
                case Ev.COMEBACK: enqueue(this::comeback); break;
                case Ev.LETTER: enqueue(() -> letter(e.value, e.text)); break;
                case Ev.REPAIRED: enqueue(() -> simple("🦅", "Serin geri geldi!", e.value + " gün", "Anka kuşu gibi küllerinden doğdun. İşte gerçek disiplin bu!", Th.primary)); break;
            }
        }
    }

    void enqueue(Runnable r) {
        queue.add(r);
        if (!showing) next();
    }

    void next() {
        Runnable r = queue.poll();
        if (r == null) { showing = false; return; }
        showing = true;
        r.run();
    }

    FrameLayout frame(int c1, int c2) {
        FrameLayout f = new FrameLayout(a);
        f.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{c1, c2}));
        f.setClickable(true);
        return f;
    }

    LinearLayout col(FrameLayout f) {
        LinearLayout col = U.v(a);
        col.setGravity(Gravity.CENTER_HORIZONTAL);
        col.setPadding(U.dp(28), a.topInset + U.dp(40), U.dp(28), a.bottomInset + U.dp(28));
        f.addView(col, new FrameLayout.LayoutParams(-1, -1));
        return col;
    }

    void finish(FrameLayout f) {
        a.closeFull(f);
        f.postDelayed(this::next, 250);
    }

    TextView center(String s, float sp, int color, int w) {
        TextView t = U.tv(a, s, sp, color, w);
        t.setGravity(Gravity.CENTER);
        return t;
    }

    View cont(final FrameLayout f, String label) {
        U.Btn b = U.btn(a, label, Th.primary);
        b.setOnClickListener(v -> finish(f));
        return b;
    }

    void streak(int n, boolean milestone) {
        Repo r = a.r;
        int stage = Game.flameStage(n);
        int fc = Art.flameColor(stage);
        FrameLayout f = frame(Art.mix(0xFF000000, fc, 0.25f), 0xFF0B0B12);
        LinearLayout c = col(f);
        c.addView(U.fill(a), new LinearLayout.LayoutParams(1, 0, 1));
        Views.FlameView fv = new Views.FlameView(a);
        fv.set(stage, true);
        c.addView(fv, new LinearLayout.LayoutParams(U.dp(190), U.dp(210)));
        final TextView num = center(String.valueOf(Math.max(0, n - 1)), 76, 0xFFFFFFFF, 900);
        c.addView(num);
        c.addView(center("günlük seri!", 22, fc, 900));
        c.addView(U.space(a, 18));
        // hafta noktaları
        LinearLayout week = U.h(a);
        week.setGravity(Gravity.CENTER);
        int td = r.e.today(r.now());
        int ws = Tm.weekStart(td);
        for (int d = ws; d < ws + 7; d++) {
            LinearLayout cell = U.v(a);
            cell.setGravity(Gravity.CENTER);
            DayRec dr = r.s.dayOrNull(d);
            boolean good = d == td || (dr != null && (dr.good() || dr.met));
            boolean frozen = dr != null && dr.state == DayRec.FROZEN;
            TextView dot = center(good ? "🔥" : frozen ? "🧊" : d < td ? "·" : "", 18, 0xFFFFFFFF, 800);
            dot.setBackground(U.rr(good ? Art.alpha(fc, 0.3f) : 0x22FFFFFF, 50));
            cell.addView(dot, new LinearLayout.LayoutParams(U.dp(38), U.dp(38)));
            TextView l = center(Tm.DAY_LETTER[d - ws], 12, d == td ? 0xFFFFFFFF : 0x99FFFFFF, 800);
            cell.addView(l);
            week.addView(cell, U.m(U.lpWW(), 3, 0, 3, 0));
            if (d == td) dot.postDelayed(() -> U.bounce(dot), 900);
        }
        c.addView(week);
        c.addView(U.space(a, 18));
        String msg;
        if (milestone) msg = "🏆 " + n + " günlük kilometre taşı! " + (n == 7 || n == 30 || n == 100 || n == 365 ? "Alevin evrildi: " + Game.FLAME_NAME[stage] + "!" : "Sen artık bir alışkanlık makinesisin.");
        else if (n == 1) msg = "İlk alev yandı! Yarın da gel, 2 olsun. 🔥";
        else msg = "Bugünün gereksinimleri tamam. Yarın da gel, seri " + (n + 1) + " olsun!";
        c.addView(center(msg, 16, 0xDDFFFFFF, 700));
        c.addView(U.fill(a), new LinearLayout.LayoutParams(1, 0, 1));
        if (milestone) {
            U.Btn share = U.ghost(a, "📤 Paylaş");
            share.setOnClickListener(v -> Sheets.shareStreak(a, n));
            c.addView(share, U.mb(U.lpMW(), 10));
        }
        c.addView(cont(f, "DEVAM"), U.lpMW());
        a.showFull(f);
        num.postDelayed(() -> { U.countUp(num, Math.max(0, n - 1), n, "", "", 700); U.bounce(num); a.confetti.rain(milestone ? 220 : 90); }, 450);
    }

    void perfect() {
        FrameLayout f = frame(0xFF3A2A00, 0xFF0B0B12);
        LinearLayout c = col(f);
        c.addView(U.fill(a), new LinearLayout.LayoutParams(1, 0, 1));
        TextView g = center("💎", 110, 0xFFFFFFFF, 400);
        c.addView(g);
        c.addView(center("Mükemmel Gün!", 34, Th.gold, 900));
        c.addView(U.space(a, 10));
        c.addView(center("Bugün her şeyi tam versiyonuyla yaptın. Bu nadir ve değerli bir şey.", 16, 0xDDFFFFFF, 700));
        c.addView(U.space(a, 16));
        c.addView(center("+50 XP   🎁 Nadir sandık", 18, 0xFFFFFFFF, 900));
        c.addView(U.fill(a), new LinearLayout.LayoutParams(1, 0, 1));
        c.addView(cont(f, "HARİKA!"), U.lpMW());
        a.showFull(f);
        U.pop(g);
        g.postDelayed(() -> a.confetti.rain(200), 300);
    }

    void level(int lv) {
        FrameLayout f = frame(Art.mix(0xFF000000, Th.focus, 0.3f), 0xFF0B0B12);
        LinearLayout c = col(f);
        c.addView(U.fill(a), new LinearLayout.LayoutParams(1, 0, 1));
        TextView star = center("⭐", 96, 0xFFFFFFFF, 400);
        c.addView(star);
        c.addView(center("Seviye " + lv + "!", 40, 0xFFFFFFFF, 900));
        c.addView(center(Game.title(lv), 20, Th.gold, 900));
        c.addView(U.space(a, 16));
        StringBuilder sb = new StringBuilder();
        for (Game.Item it : Game.ITEMS) if (it.minLevel == lv) sb.append(it.emoji).append(" ").append(it.name).append("   ");
        if (sb.length() > 0) {
            c.addView(center("Mağazada açıldı:", 14, 0xAAFFFFFF, 800));
            c.addView(center(sb.toString().trim(), 16, 0xFFFFFFFF, 800));
            c.addView(U.space(a, 10));
        }
        c.addView(center("🎁 Sandık kazandın!", 18, 0xFFFFFFFF, 900));
        c.addView(U.fill(a), new LinearLayout.LayoutParams(1, 0, 1));
        c.addView(cont(f, "DEVAM"), U.lpMW());
        a.showFull(f);
        U.pop(star);
        star.postDelayed(() -> a.confetti.rain(150), 250);
    }

    void badge(String id, int tier) {
        Badges.B b = Badges.get(id);
        if (b == null) { next(); return; }
        int col = Badges.TIER_COLOR[b.tiers.length == 1 ? 3 : Math.max(1, Math.min(4, tier))];
        FrameLayout f = frame(Art.mix(0xFF000000, col, 0.28f), 0xFF0B0B12);
        LinearLayout c = col(f);
        c.addView(U.fill(a), new LinearLayout.LayoutParams(1, 0, 1));
        c.addView(center("YENİ ROZET", 14, 0xAAFFFFFF, 900));
        Views.MedalView m = new Views.MedalView(a);
        m.color = col; m.emoji = b.emoji; m.animating = true;
        c.addView(m, new LinearLayout.LayoutParams(U.dp(190), U.dp(190)));
        c.addView(center(b.name, 30, 0xFFFFFFFF, 900));
        if (b.tiers.length > 1) c.addView(center(Badges.TIER[Math.max(1, Math.min(4, tier))] + " kademe", 16, col, 900));
        c.addView(U.space(a, 8));
        c.addView(center(b.descFor(tier), 16, 0xDDFFFFFF, 700));
        c.addView(center("+" + (25 * Math.max(1, tier)) + " 🪙", 18, Th.gold, 900));
        c.addView(U.fill(a), new LinearLayout.LayoutParams(1, 0, 1));
        c.addView(cont(f, "HARİKA!"), U.lpMW());
        a.showFull(f);
        U.pop(m);
        m.postDelayed(() -> a.confetti.burst(a.rootView().getWidth() / 2f, a.rootView().getHeight() * 0.35f, 90, U.dp(600), null), 200);
    }

    void comeback() {
        FrameLayout f = frame(Art.mix(0xFF000000, Th.water, 0.25f), 0xFF0B0B12);
        LinearLayout c = col(f);
        c.addView(U.fill(a), new LinearLayout.LayoutParams(1, 0, 1));
        Views.CompanionView cv = new Views.CompanionView(a);
        Repo r = a.r;
        cv.species = r.s.p.companion; cv.mood = Art.MOOD_EXCITED; cv.hat = r.s.p.hat; cv.acc = r.s.p.acc;
        c.addView(cv, new LinearLayout.LayoutParams(U.dp(200), U.dp(200)));
        c.addView(center("Tekrar hoş geldin! 👋", 30, 0xFFFFFFFF, 900));
        c.addView(U.space(a, 10));
        c.addView(center(r.s.p.companionName + " seni çok özledi. En zor adım geri dönmekti ve onu yaptın.", 16, 0xDDFFFFFF, 700));
        c.addView(U.space(a, 14));
        TextView box = center("🌱 Bugün Kolay Başlangıç Günü\nSadece çapa rutinlerin yeter · 2x XP", 16, 0xFFFFFFFF, 800);
        box.setBackground(U.rr(0x22FFFFFF, 18));
        box.setPadding(U.dp(16), U.dp(14), U.dp(16), U.dp(14));
        c.addView(box, U.lpMW());
        c.addView(U.fill(a), new LinearLayout.LayoutParams(1, 0, 1));
        c.addView(cont(f, "HADİ BAŞLAYALIM"), U.lpMW());
        a.showFull(f);
        Sound.play(a, Synth.SUCCESS);
    }

    void letter(int n, String text) {
        FrameLayout f = frame(0xFF2A1A30, 0xFF0B0B12);
        LinearLayout c = col(f);
        c.addView(U.fill(a), new LinearLayout.LayoutParams(1, 0, 1));
        c.addView(center("💌", 80, 0xFFFFFFFF, 400));
        c.addView(center(n + ". gün: Geçmişteki senden bir mektup", 20, 0xFFFFFFFF, 900));
        c.addView(U.space(a, 16));
        TextView body = U.tv(a, text, 17, 0xFFF3E9FF, 700);
        body.setBackground(U.rr(0x22FFFFFF, 20));
        body.setPadding(U.dp(20), U.dp(18), U.dp(20), U.dp(18));
        c.addView(body, U.lpMW());
        c.addView(U.fill(a), new LinearLayout.LayoutParams(1, 0, 1));
        c.addView(cont(f, "TEŞEKKÜRLER, GEÇMİŞTEKİ BEN"), U.lpMW());
        a.showFull(f);
        Sound.play(a, Synth.BADGE);
    }

    void simple(String emoji, String title, String sub, String text, int color) {
        FrameLayout f = frame(Art.mix(0xFF000000, color, 0.25f), 0xFF0B0B12);
        LinearLayout c = col(f);
        c.addView(U.fill(a), new LinearLayout.LayoutParams(1, 0, 1));
        TextView e = center(emoji, 96, 0xFFFFFFFF, 400);
        c.addView(e);
        c.addView(center(title, 30, 0xFFFFFFFF, 900));
        if (sub != null && !sub.isEmpty()) c.addView(center(sub, 18, color, 900));
        c.addView(U.space(a, 10));
        c.addView(center(text, 16, 0xDDFFFFFF, 700));
        c.addView(U.fill(a), new LinearLayout.LayoutParams(1, 0, 1));
        c.addView(cont(f, "DEVAM"), U.lpMW());
        a.showFull(f);
        U.pop(e);
        e.postDelayed(() -> a.confetti.rain(120), 250);
    }
}
