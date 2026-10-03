package com.ilker.rutin.ui.sheets;

import android.app.NotificationManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ilker.rutin.core.Badges;
import com.ilker.rutin.core.Coach;
import com.ilker.rutin.core.Ev;
import com.ilker.rutin.core.Game;
import com.ilker.rutin.core.NotifEngine;
import com.ilker.rutin.core.State;
import com.ilker.rutin.core.Synth;
import com.ilker.rutin.core.Task;
import com.ilker.rutin.core.Tm;
import com.ilker.rutin.data.Repo;
import com.ilker.rutin.sys.Notifier;
import com.ilker.rutin.sys.Scheduler;
import com.ilker.rutin.sys.Sound;
import com.ilker.rutin.ui.Art;
import com.ilker.rutin.ui.MainActivity;
import com.ilker.rutin.ui.Sheet;
import com.ilker.rutin.ui.Th;
import com.ilker.rutin.ui.U;
import com.ilker.rutin.ui.Views;
import com.ilker.rutin.widget.StreakWidget;
import com.ilker.rutin.widget.TodayWidget;
import com.ilker.rutin.widget.WaterWidget;

import java.util.ArrayList;

/** Mağaza, sandıklar, ödüller, rozetler, kimlik, ayarlar. */
public final class MeSheets {
    private MeSheets() {}

    // ---------------- Mağaza ----------------
    public static void shop(final MainActivity a, final int cat0) {
        final Repo r = a.r;
        final Sheet s = new Sheet(a, "🛍️ Mağaza", true);
        final TextView coins = U.tv(a, "", 16, Th.gold, 900);
        s.add(coins);
        final int[] cat = {cat0 < 0 ? 0 : cat0};
        Views.Flow tabs = new Views.Flow(a);
        final ArrayList<TextView> tb = new ArrayList<>();
        final LinearLayout list = U.v(a);
        final Runnable[] render = new Runnable[1];
        for (int i = 0; i < Game.CAT_NAME.length; i++) {
            final int k = i;
            TextView t = U.pill(a, Game.CAT_NAME[i], i == cat[0]);
            t.setOnClickListener(v -> { cat[0] = k; for (TextView x : tb) U.setPill(x, x == v); render[0].run(); });
            tb.add(t);
            tabs.addView(t);
        }
        s.add(tabs, U.m(U.lpMW(), 0, 10, 0, 10));
        s.add(list);
        render[0] = () -> {
            State.Profile p = r.s.p;
            coins.setText("🪙 " + p.coins + " coin   ·   Lv " + Game.level(p.xp));
            list.removeAllViews();
            if (cat[0] == Game.C_HAT || cat[0] == Game.C_ACC) {
                Views.CompanionView cv = new Views.CompanionView(a);
                cv.species = p.companion; cv.hat = p.hat; cv.acc = p.acc; cv.mood = Art.MOOD_HAPPY;
                list.addView(cv, new LinearLayout.LayoutParams(-1, U.dp(150)));
            }
            int lv = Game.level(p.xp);
            for (final Game.Item it : Game.ITEMS) {
                if (it.cat != cat[0]) continue;
                boolean owned = it.price == 0 || r.s.owns(it.id);
                boolean equipped = (it.cat == Game.C_THEME && Th.themeOf(it.id) == p.theme)
                        || (it.cat == Game.C_SOUND && Integer.parseInt(it.id.substring(1)) == p.soundPack)
                        || (it.id.equals(p.hat)) || (it.id.equals(p.acc));
                LinearLayout row = U.h(a);
                row.setBackground(U.rrs(Th.surface2, 18, equipped ? Th.primary : Th.stroke, equipped ? 2 : 1));
                row.setPadding(U.dp(12), U.dp(12), U.dp(12), U.dp(12));
                TextView e = U.tv(a, it.emoji, 28, Th.text, 400);
                row.addView(e);
                row.addView(U.space(a, 12));
                LinearLayout c = U.v(a);
                c.addView(U.tv(a, it.name, 16, Th.text, 900));
                String sub = Game.RARITY[it.rarity] + (it.cat == Game.C_FREEZE ? " · Sahip: " + p.freezes + "/" + Game.MAX_FREEZES : "");
                c.addView(U.tv(a, sub, 12, Game.RARITY_COLOR[it.rarity], 800));
                row.addView(c, U.lpW(1));
                TextView act;
                if (lv < it.minLevel) act = U.chip(a, "🔒 Lv " + it.minLevel, Th.surface, Th.dim);
                else if (it.cat == Game.C_FREEZE) act = U.chip(a, "🪙 " + it.price, Th.gold, 0xFF2A1E00);
                else if (equipped) act = U.chip(a, it.cat == Game.C_HAT || it.cat == Game.C_ACC ? "Çıkar" : "✓ Kullanılıyor", Th.primary, 0xFFFFFFFF);
                else if (owned) act = U.chip(a, it.cat == Game.C_ISLAND ? "✓ Adada" : "Kullan", Th.success, 0xFF0B2A18);
                else act = U.chip(a, "🪙 " + it.price, Th.gold, 0xFF2A1E00);
                act.setOnClickListener(v -> {
                    State.Profile pp = r.s.p;
                    if (Game.level(pp.xp) < it.minLevel) { a.toast("Seviye " + it.minLevel + "'de açılır ⭐"); return; }
                    boolean own = it.price == 0 || r.s.owns(it.id);
                    if (!own || it.cat == Game.C_FREEZE) {
                        if (pp.coins < it.price) { a.toast("Yeterli coin yok 🪙 (" + (it.price - pp.coins) + " eksik)"); return; }
                        if (!r.e.buy(it.id, r.now())) { a.toast(it.cat == Game.C_FREEZE ? "Dondurma deposu dolu" : "Alınamadı"); return; }
                        Sound.play(a, Synth.CHEST);
                        a.confettiAt(v, 40);
                        a.toast(it.emoji + " " + it.name + " senin!");
                    }
                    equip(a, it);
                    r.commit();
                    render[0].run();
                });
                U.squish(act);
                row.addView(act);
                if (it.cat == Game.C_SOUND) row.setOnClickListener(v -> Sound.playPack(Synth.STREAK, Integer.parseInt(it.id.substring(1))));
                list.addView(row, U.mb(U.lpMW(), 8));
            }
            if (cat[0] == Game.C_THEME) list.addView(U.tv(a, "Tema değişince uygulama yeniden çizilir.", 12, Th.dim, 700));
            if (cat[0] == Game.C_SOUND) list.addView(U.tv(a, "Satıra dokun: sesi dinle 🔊", 12, Th.dim, 700));
        };
        render[0].run();
        a.show(s);
    }

    static void equip(MainActivity a, Game.Item it) {
        State.Profile p = a.r.s.p;
        switch (it.cat) {
            case Game.C_THEME:
                p.theme = Th.themeOf(it.id);
                a.r.save();
                a.closeAllSheets();
                a.getWindow().getDecorView().postDelayed(a::rebuild, 280);
                break;
            case Game.C_SOUND: p.soundPack = Integer.parseInt(it.id.substring(1)); Sound.play(a, Synth.SUCCESS); break;
            case Game.C_HAT: p.hat = it.id.equals(p.hat) ? "" : it.id; break;
            case Game.C_ACC: p.acc = it.id.equals(p.acc) ? "" : it.id; break;
        }
    }

    // ---------------- Sandıklar ----------------
    public static void chests(final MainActivity a) {
        final Repo r = a.r;
        if (r.s.chests.isEmpty()) { a.toast("Açılacak sandık yok"); return; }
        final FrameLayout f = new FrameLayout(a);
        f.setBackgroundColor(0xF20B0B12);
        f.setClickable(true);
        final LinearLayout c = U.v(a);
        c.setGravity(Gravity.CENTER_HORIZONTAL);
        c.setPadding(U.dp(24), a.topInset + U.dp(40), U.dp(24), a.bottomInset + U.dp(24));
        f.addView(c, new FrameLayout.LayoutParams(-1, -1));
        final Runnable[] next = new Runnable[1];
        next[0] = () -> {
            c.removeAllViews();
            if (r.s.chests.isEmpty()) { a.closeFull(f); return; }
            final State.Chest ch = r.s.chests.get(0);
            TextView t = U.tv(a, Game.RARITY[ch.rarity] + " sandık", 28, Game.RARITY_COLOR[ch.rarity], 900);
            t.setGravity(Gravity.CENTER);
            c.addView(t, U.lpMW());
            TextView why = U.tv(a, ch.reason + " · kalan " + r.s.chests.size(), 14, 0xAAFFFFFF, 700);
            why.setGravity(Gravity.CENTER);
            c.addView(why, U.lpMW());
            c.addView(U.fill(a), new LinearLayout.LayoutParams(1, 0, 1));
            final Views.ChestView cv = new Views.ChestView(a);
            cv.rarity = ch.rarity;
            c.addView(cv, new LinearLayout.LayoutParams(-1, U.dp(280)));
            final LinearLayout loot = U.v(a);
            loot.setGravity(Gravity.CENTER_HORIZONTAL);
            c.addView(loot, U.lpMW());
            c.addView(U.fill(a), new LinearLayout.LayoutParams(1, 0, 1));
            final U.Btn b = U.btn(a, "AÇ!", Game.RARITY_COLOR[ch.rarity]);
            c.addView(b, U.lpMW());
            b.setOnClickListener(v -> {
                if ("AÇ!".contentEquals(b.getText())) {
                    ArrayList<String> got = r.e.openChest(0, r.now());
                    android.animation.ValueAnimator an = android.animation.ValueAnimator.ofFloat(0, 1);
                    an.setDuration(700);
                    an.addUpdateListener(x -> cv.open = (float) x.getAnimatedValue());
                    an.start();
                    Sound.play(a, Synth.CHEST);
                    U.hapticStrong(v);
                    a.confetti.burst(a.rootView().getWidth() / 2f, a.rootView().getHeight() * 0.45f, 120, U.dp(700), null);
                    int delay = 500;
                    for (String g : got) {
                        final TextView gt = U.tv(a, g, 22, 0xFFFFFFFF, 900);
                        gt.setGravity(Gravity.CENTER);
                        gt.setAlpha(0);
                        loot.addView(gt, U.lpMW());
                        gt.postDelayed(() -> { U.pop(gt); Sound.play(a, Synth.POP); }, delay);
                        delay += 350;
                    }
                    r.commit();
                    b.setText(r.s.chests.isEmpty() ? "HARİKA!" : "SONRAKİ SANDIK");
                } else next[0].run();
            });
        };
        next[0].run();
        a.showFull(f);
    }

    // ---------------- Kart albümü ----------------
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

    // ---------------- Kişisel ödüller ----------------
    public static void rewards(final MainActivity a) {
        final Repo r = a.r;
        final Sheet s = new Sheet(a, "🍰 Kişisel ödüllerim");
        s.add(U.tv(a, "Uygulamadaki emeğini gerçek hayata bağla: coin biriktir, kendine söz verdiğin ödülü al.", 14, Th.dim, 700));
        final TextView coins = U.tv(a, "🪙 " + r.s.p.coins, 18, Th.gold, 900);
        s.add(coins, U.m(U.lpMW(), 0, 10, 0, 6));
        for (final State.Reward w : r.s.rewards) {
            LinearLayout row = U.h(a);
            row.setBackground(U.rr(Th.surface2, 18));
            row.setPadding(U.dp(14), U.dp(12), U.dp(12), U.dp(12));
            row.addView(U.tv(a, w.emoji, 26, Th.text, 400));
            row.addView(U.space(a, 12));
            LinearLayout c = U.v(a);
            c.addView(U.tv(a, w.name, 16, Th.text, 900));
            c.addView(U.tv(a, "🪙 " + w.cost + (w.redeemed > 0 ? " · " + w.redeemed + " kez alındı" : ""), 13, Th.dim, 700));
            row.addView(c, U.lpW(1));
            int pct = (int) Math.min(100, r.s.p.coins * 100L / Math.max(1, w.cost));
            TextView b = U.chip(a, r.s.p.coins >= w.cost ? "Al! 🎉" : "%" + pct, r.s.p.coins >= w.cost ? Th.success : Th.surface, r.s.p.coins >= w.cost ? 0xFF0B2A18 : Th.dim);
            b.setOnClickListener(v -> {
                if (r.e.redeem(w.id, r.now())) {
                    Sound.play(a, Synth.FANFARE);
                    a.confetti.rain(160);
                    a.banner("🎉 Hak ettin: " + w.emoji + " " + w.name + ". Keyfini çıkar!", Th.success, 0xFF0B2A18);
                    r.commit();
                    s.dismiss();
                } else a.toast("Biraz daha coin biriktir 💪");
            });
            b.setOnLongClickListener(v -> { r.s.rewards.remove(w); r.commit(); s.dismiss(); rewards(a); return true; });
            row.addView(b);
            s.add(row, U.mb(U.lpMW(), 8));
        }
        s.add(Sheets.label(a, "YENİ ÖDÜL"));
        final String[] emo = {"🍰"};
        LinearLayout er = U.h(a);
        final String[] opts = {"🍰", "🎮", "🍕", "🎬", "☕", "👟", "📚", "✈️", "🛍️", "🎧"};
        final ArrayList<TextView> es = new ArrayList<>();
        for (final String o : opts) {
            TextView t = U.tv(a, o, 22, Th.text, 400);
            t.setGravity(Gravity.CENTER);
            t.setBackground(o.equals(emo[0]) ? U.rr(Art.alpha(Th.primary, 0.3f), 10) : null);
            t.setOnClickListener(v -> { emo[0] = o; for (TextView x : es) x.setBackground(x == v ? U.rr(Art.alpha(Th.primary, 0.3f), 10) : null); });
            es.add(t);
            er.addView(t, U.lpW(1));
        }
        s.add(er);
        final EditText name = U.input(a, "Örn: Sevdiğim tatlıyı yerim", "", false);
        s.add(name, U.m(U.lpMW(), 0, 8, 0, 0));
        final Views.NumStepper cost = new Views.NumStepper(a, 500, 50, 50, 20000, v -> "🪙 " + v);
        s.add(cost, U.m(U.lpMW(), 0, 8, 0, 0));
        U.Btn add = U.btn(a, "ÖDÜL EKLE", Th.primary);
        add.setOnClickListener(v -> {
            String n = name.getText().toString().trim();
            if (n.isEmpty()) { name.setError("Ödülün adı"); return; }
            State.Reward w = new State.Reward();
            w.id = r.s.nextId(); w.name = n; w.emoji = emo[0]; w.cost = cost.value;
            r.s.rewards.add(w);
            r.commit();
            s.dismiss();
            rewards(a);
        });
        s.add(add, U.m(U.lpMW(), 0, 14, 0, 0));
        s.add(U.tv(a, "Silmek için ödüle uzun bas.", 12, Th.dim, 700), U.m(U.lpMW(), 0, 8, 0, 0));
        a.show(s);
    }

    // ---------------- Rozet ----------------
    public static void badge(final MainActivity a, Badges.B b) {
        int tier = a.r.s.badgeTier(b.id);
        Sheet s = new Sheet(a, null);
        Views.MedalView m = new Views.MedalView(a);
        m.emoji = b.emoji;
        m.locked = tier == 0;
        m.color = Badges.TIER_COLOR[tier == 0 ? 0 : b.tiers.length == 1 ? 3 : tier];
        m.animating = tier > 0;
        LinearLayout.LayoutParams ml = new LinearLayout.LayoutParams(U.dp(140), U.dp(140));
        ml.gravity = Gravity.CENTER_HORIZONTAL;
        s.add(m, ml);
        boolean hidden = b.hidden && tier == 0;
        s.add(Sheets.center(a, hidden ? "??? Gizli rozet" : b.name, 24, Th.text));
        s.add(Sheets.center(a, hidden ? "Nasıl kazanılacağı bir sır 🤫" : b.descFor(Math.max(1, Math.min(b.tiers.length, tier + 1))), 15, Th.dim));
        if (!hidden && b.tiers.length > 1) {
            s.add(Sheets.label(a, "KADEMELER"));
            long v = Badges.metric(a.r.s, b.metric);
            for (int i = 0; i < b.tiers.length; i++) {
                LinearLayout row = U.h(a);
                row.addView(U.tv(a, (tier > i ? "✅ " : "⬜ ") + Badges.TIER[i + 1], 15, tier > i ? Badges.TIER_COLOR[i + 1] : Th.text, 800), U.lpW(1));
                row.addView(U.tv(a, Math.min(v, b.tiers[i]) + " / " + b.tiers[i], 14, Th.dim, 800));
                s.add(row, U.m(U.lpMW(), 0, 4, 0, 4));
            }
        }
        a.show(s);
    }

    // ---------------- Kimlik ----------------
    public static void identity(final MainActivity a) {
        final Repo r = a.r;
        final State.Profile p = r.s.p;
        final Sheet s = new Sheet(a, "🪞 Kimliğim", true);
        s.add(U.tv(a, "Her tamamladığın iş, olmak istediğin kişiye verilmiş bir oydur.", 14, Th.dim, 700));
        s.add(Sheets.label(a, "BEN ... BİRİYİM"));
        for (final String id : new ArrayList<>(p.identities)) {
            LinearLayout row = U.h(a);
            row.addView(U.tv(a, "🪞 " + id, 15, Th.text, 800), U.lpW(1));
            TextView x = U.tv(a, "✕", 16, Th.dim, 900);
            x.setPadding(U.dp(10), U.dp(6), U.dp(6), U.dp(6));
            x.setOnClickListener(v -> { p.identities.remove(id); r.commit(); s.dismiss(); identity(a); });
            row.addView(x);
            s.add(row, U.m(U.lpMW(), 0, 4, 0, 4));
        }
        final EditText add = U.input(a, "Örn: Sağlığına önem veren biriyim", "", false);
        s.add(add, U.m(U.lpMW(), 0, 6, 0, 0));
        U.Btn ab = U.ghost(a, "+ Ekle");
        ab.setOnClickListener(v -> { String t = add.getText().toString().trim(); if (!t.isEmpty()) { p.identities.add(t); r.commit(); s.dismiss(); identity(a); } });
        s.add(ab, U.m(U.lpMW(), 0, 8, 0, 0));
        s.add(Sheets.label(a, "✍️ SÖZÜM"));
        final EditText prom = U.input(a, "Kendime söz veriyorum...", p.promise, true);
        s.add(prom);
        if (p.signature != null && !p.signature.isEmpty()) {
            Views.SignatureShow sg = new Views.SignatureShow(a);
            sg.data = p.signature;
            s.add(sg, new LinearLayout.LayoutParams(-1, U.dp(110)));
        }
        s.add(Sheets.label(a, "💌 GELECEKTEKİ BANA MEKTUP (30, 100 ve 365. günde açılır)"));
        final EditText letter = U.input(a, "Sevgili gelecekteki ben...", p.letter, true);
        letter.setMinLines(4);
        s.add(letter);
        U.Btn save = U.btn(a, "KAYDET", Th.primary);
        save.setOnClickListener(v -> {
            p.promise = prom.getText().toString().trim();
            p.letter = letter.getText().toString().trim();
            r.commit();
            a.toast("💾 Kaydedildi");
            s.dismiss();
        });
        s.add(save, U.m(U.lpMW(), 0, 18, 0, 0));
        a.show(s);
    }

    // ---------------- Ada tam ekran ----------------
    public static void island(final MainActivity a) {
        final FrameLayout f = new FrameLayout(a);
        f.setBackgroundColor(0xFF000000);
        f.setClickable(true);
        Views.IslandView iv = new Views.IslandView(a);
        iv.items = a.r.s.island;
        iv.seed = a.r.s.p.firstDay;
        int nd = a.r.e.nowDm(a.r.now());
        iv.night = nd < a.r.e.wakeDm() || nd > 1140;
        iv.radius = 0;
        f.addView(iv, new FrameLayout.LayoutParams(-1, -1));
        Views.CompanionView cv = new Views.CompanionView(a);
        State.Profile p = a.r.s.p;
        cv.species = p.companion; cv.hat = p.hat; cv.acc = p.acc; cv.mood = Art.MOOD_HAPPY;
        FrameLayout.LayoutParams cl = U.flp(U.dp(130), U.dp(130), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        cl.bottomMargin = a.bottomInset + U.dp(90);
        f.addView(cv, cl);
        TextView t = U.tv(a, "🏝️ Hayat Adası · " + a.r.s.island.size() + " yapı\nHer iyi gün adana bir şey ekler.", 15, 0xFFFFFFFF, 800);
        t.setShadowLayer(8, 0, 2, 0xAA000000);
        FrameLayout.LayoutParams tl = U.flp(-2, -2, Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        tl.topMargin = a.topInset + U.dp(20);
        t.setGravity(Gravity.CENTER);
        f.addView(t, tl);
        f.setOnClickListener(v -> a.closeFull(f));
        a.showFull(f);
    }

    // ---------------- Ayarlar ----------------
    public static void settings(final MainActivity a) {
        final Repo r = a.r;
        final State.Profile p = r.s.p;
        final Sheet s = new Sheet(a, "⚙️ Ayarlar", true);
        s.add(Sheets.label(a, "PROFİL"));
        final EditText name = U.input(a, "Adın", p.name, false);
        s.add(name);
        final EditText cname = U.input(a, "Yoldaşının adı", p.companionName, false);
        s.add(cname, U.m(U.lpMW(), 0, 8, 0, 0));
        Views.Flow sp = new Views.Flow(a);
        for (int i = 0; i < 4; i++) {
            final int k = i;
            TextView t = U.pill(a, Art.SPECIES_EMOJI[i] + " " + Art.SPECIES[i], p.companion == i);
            t.setOnClickListener(v -> { p.companion = k; r.commit(); s.dismiss(); settings(a); });
            sp.addView(t);
        }
        s.add(sp, U.m(U.lpMW(), 0, 8, 0, 0));
        s.add(Sheets.label(a, "GÜN DÖNGÜSÜ"));
        LinearLayout times = U.h(a);
        final Views.TimeStepper wake = new Views.TimeStepper(a, p.wakeMin, "Uyanma");
        final Views.TimeStepper sleep = new Views.TimeStepper(a, p.sleepMin, "Yatma");
        times.addView(wake, U.lpW(1));
        times.addView(sleep, U.lpW(1));
        s.add(times);
        s.add(Sheets.label(a, "DİNLENME GÜNLERİ (sadece çapa rutinler, seri bozulmaz)"));
        final Views.DaysPicker rest = new Views.DaysPicker(a, p.restMask);
        s.add(rest);
        s.add(Sheets.label(a, "KOÇ KİŞİLİĞİ"));
        for (int i = 0; i < 4; i++) {
            final int k = i;
            LinearLayout row = U.row(a, Coach.EMOJI[i], Coach.NAMES[i], Coach.SAMPLE[i], Th.primary);
            row.setBackground(U.rrs(Th.surface2, 16, p.coach == i ? Th.primary : Th.stroke, p.coach == i ? 2 : 1));
            row.setOnClickListener(v -> { p.coach = k; r.commit(); s.dismiss(); settings(a); });
            s.add(row, U.mb(U.lpMW(), 6));
        }
        s.add(Sheets.label(a, "BİLDİRİMLER"));
        final TextView prep = U.pill(a, "10 dk önce hazırlık hatırlatması: " + (p.prepReminders ? "AÇIK" : "KAPALI"), p.prepReminders);
        prep.setOnClickListener(v -> { p.prepReminders = !p.prepReminders; U.setPill(prep, p.prepReminders); prep.setText("10 dk önce hazırlık hatırlatması: " + (p.prepReminders ? "AÇIK" : "KAPALI")); });
        s.add(prep, U.lpWW());
        LinearLayout br = U.h(a);
        br.addView(U.tv(a, "Saatlik bildirim sınırı", 15, Th.text, 800), U.lpW(1));
        final Views.NumStepper budget = new Views.NumStepper(a, p.budget, 1, 1, 12, v -> v + " / saat");
        br.addView(budget);
        s.add(br, U.m(U.lpMW(), 0, 8, 0, 0));
        U.Btn test = U.ghost(a, "🔔 Test bildirimi gönder");
        test.setOnClickListener(v -> {
            ArrayList<Task> ts = r.s.tasksOf(r.e.today(r.now()));
            if (ts.isEmpty()) { a.toast("Bugün görev yok"); return; }
            boolean fg = r.foreground;
            Notifier.postTask(a, ts.get(0), NotifEngine.L_ON, r.now());
            a.toast("Bildirim gönderildi");
        });
        s.add(test, U.m(U.lpMW(), 0, 10, 0, 0));
        s.add(Sheets.label(a, "SES VE TİTREŞİM"));
        final TextView snd = U.pill(a, "🔊 Sesler: " + (p.soundOn ? "AÇIK" : "KAPALI"), p.soundOn);
        snd.setOnClickListener(v -> { p.soundOn = !p.soundOn; U.setPill(snd, p.soundOn); snd.setText("🔊 Sesler: " + (p.soundOn ? "AÇIK" : "KAPALI")); });
        final TextView hap = U.pill(a, "📳 Titreşim: " + (p.hapticOn ? "AÇIK" : "KAPALI"), p.hapticOn);
        hap.setOnClickListener(v -> { p.hapticOn = !p.hapticOn; U.setPill(hap, p.hapticOn); hap.setText("📳 Titreşim: " + (p.hapticOn ? "AÇIK" : "KAPALI")); });
        Views.Flow sf = new Views.Flow(a);
        sf.addView(snd);
        sf.addView(hap);
        s.add(sf);
        s.add(Sheets.label(a, "MODLAR"));
        LinearLayout vac = U.h(a);
        vac.addView(U.tv(a, "🏖️ Tatil modu (seri durur, bildirim yok)", 14, Th.text, 800), U.lpW(1));
        final Views.NumStepper vd = new Views.NumStepper(a, 3, 1, 1, 30, v -> v + " gün");
        vac.addView(vd);
        s.add(vac);
        int td = r.e.today(r.now());
        boolean vacOn = r.e.isVacation(td);
        U.Btn vb = U.ghost(a, vacOn ? "Tatil modunu kapat (bitiş: " + Tm.dateShort(p.vacationUntil) + ")" : "Tatil modunu başlat");
        vb.setOnClickListener(v -> {
            if (r.e.isVacation(td)) p.vacationUntil = td - 1; else p.vacationUntil = td + vd.value - 1;
            r.e.resyncFrom(td, r.now());
            r.commit();
            s.dismiss();
            a.toast(r.e.isVacation(td) ? "🏖️ İyi tatiller! Serin güvende." : "Tatil modu kapandı");
        });
        s.add(vb, U.m(U.lpMW(), 0, 6, 0, 0));
        s.add(Sheets.label(a, "İZİNLER VE GÜVENİLİRLİK"));
        s.add(perm(a, "🔔 Bildirim izni", a.notifGranted(), () -> {
            if (Build.VERSION.SDK_INT >= 33) a.requestNotifPermission();
            else openAppSettings(a);
        }));
        s.add(perm(a, "⏰ Tam zamanlı alarm", Scheduler.exactAllowed(a), () -> {
            if (Build.VERSION.SDK_INT >= 31) try { a.startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + a.getPackageName()))); } catch (Exception e) { openAppSettings(a); }
        }));
        PowerManager pm = (PowerManager) a.getSystemService(Context.POWER_SERVICE);
        s.add(perm(a, "🔋 Pil optimizasyonu kapalı", pm.isIgnoringBatteryOptimizations(a.getPackageName()), () -> {
            try { a.startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + a.getPackageName()))); } catch (Exception e) { try { a.startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)); } catch (Exception ignored) {} }
        }));
        NotificationManager nm = (NotificationManager) a.getSystemService(Context.NOTIFICATION_SERVICE);
        s.add(perm(a, "🔕 Rahatsız Etme erişimi (odak için)", nm.isNotificationPolicyAccessGranted(), () -> {
            try { a.startActivity(new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)); } catch (Exception ignored) {}
        }));
        s.add(U.tv(a, "Xiaomi / Huawei / Samsung gibi telefonlarda: Ayarlar → Uygulamalar → Rutin → Pil → \"Kısıtlama yok\" ve \"Otomatik başlatma\"yı aç. Böylece bildirimler kesin zamanında gelir.", 12, Th.dim, 700), U.m(U.lpMW(), 0, 8, 0, 0));
        s.add(Sheets.label(a, "WIDGET'LAR"));
        Views.Flow wf = new Views.Flow(a);
        wf.addView(widgetPin(a, "🔥 Seri", StreakWidget.class));
        wf.addView(widgetPin(a, "💧 Su", WaterWidget.class));
        wf.addView(widgetPin(a, "📋 Bugün", TodayWidget.class));
        s.add(wf);
        s.add(Sheets.label(a, "YEDEKLEME"));
        LinearLayout bk = U.h(a);
        U.Btn ex = U.ghost(a, "💾 Dışa aktar");
        ex.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("application/json");
            i.putExtra(Intent.EXTRA_TITLE, "rutin-yedek-" + Tm.dateShort(td).replace(' ', '-') + ".json");
            a.startActivityForResult(i, MainActivity.REQ_EXPORT);
        });
        U.Btn im = U.ghost(a, "📂 İçe aktar");
        im.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            a.startActivityForResult(i, MainActivity.REQ_IMPORT);
        });
        bk.addView(ex, U.m(U.lpW(1), 0, 0, 4, 0));
        bk.addView(im, U.m(U.lpW(1), 4, 0, 0, 0));
        s.add(bk);
        U.Btn save = U.btn(a, "KAYDET", Th.primary);
        save.setOnClickListener(v -> {
            p.name = name.getText().toString().trim();
            p.companionName = cname.getText().toString().trim().isEmpty() ? "Pofuduk" : cname.getText().toString().trim();
            boolean timesChanged = p.wakeMin != wake.minute || p.sleepMin != sleep.minute;
            p.wakeMin = wake.minute;
            p.sleepMin = sleep.minute;
            p.restMask = rest.mask;
            p.budget = budget.value;
            if (timesChanged || true) r.e.resyncFrom(r.e.today(r.now()) + 1, r.now());
            r.commit();
            a.toast("💾 Ayarlar kaydedildi");
            s.dismiss();
        });
        s.add(save, U.m(U.lpMW(), 0, 20, 0, 0));
        final TextView ver = U.tv(a, "Rutin 1.0 · verilerin sadece bu telefonda", 12, Th.dim, 700);
        ver.setGravity(Gravity.CENTER);
        final int[] taps = {0};
        ver.setOnClickListener(v -> { if (++taps[0] >= 7) { taps[0] = 0; s.dismiss(); debug(a); } });
        s.add(ver, U.m(U.lpMW(), 0, 16, 0, 0));
        a.show(s);
    }

    static View perm(MainActivity a, String name, boolean ok, final Runnable fix) {
        LinearLayout row = U.h(a);
        row.setPadding(0, U.dp(6), 0, U.dp(6));
        row.addView(U.tv(a, name, 14, Th.text, 800), U.lpW(1));
        TextView b = U.chip(a, ok ? "✓ Tamam" : "Ayarla", ok ? Art.alpha(Th.success, 0.2f) : Th.warn, ok ? Th.success : 0xFF2A1E00);
        if (!ok) b.setOnClickListener(v -> fix.run());
        row.addView(b);
        return row;
    }

    static void openAppSettings(MainActivity a) {
        try { a.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + a.getPackageName()))); } catch (Exception ignored) {}
    }

    public static TextView widgetPin(final MainActivity a, String label, final Class<?> cls) {
        TextView t = U.pill(a, "+ " + label, false);
        t.setOnClickListener(v -> {
            AppWidgetManager m = AppWidgetManager.getInstance(a);
            if (m.isRequestPinAppWidgetSupported()) m.requestPinAppWidget(new ComponentName(a, cls), null, null);
            else a.toast("Ana ekrana uzun bas → Widget'lar → Rutin");
        });
        return t;
    }

    // ---------------- Hata ayıklama (gizli) ----------------
    static void debug(final MainActivity a) {
        final Repo r = a.r;
        final Sheet s = new Sheet(a, "🛠️ Test menüsü");
        s.add(U.tv(a, "Şu an (uygulama saati): " + Tm.hhmm((int) (r.now() % 1440)) + " · " + Tm.dateLong(r.e.today(r.now())) + " · ofset " + r.s.p.debugOffsetMin + " dk", 13, Th.dim, 700));
        String[] n = {"+30 dk", "+1 saat", "+3 saat", "+1 gün", "Sıfırla"};
        final int[] m = {30, 60, 180, 1440, 0};
        Views.Flow f = new Views.Flow(a);
        for (int i = 0; i < n.length; i++) {
            final int k = i;
            TextView t = U.pill(a, n[i], false);
            t.setOnClickListener(v -> {
                if (m[k] == 0) r.s.p.debugOffsetMin = 0; else r.s.p.debugOffsetMin += m[k];
                Ev.List ev = r.e.onOpen(r.now());
                r.commit();
                a.celebrate(ev, null);
                s.dismiss();
                debug(a);
            });
            f.addView(t);
        }
        s.add(f);
        U.Btn notif = U.ghost(a, "Zamanlayıcıyı şimdi çalıştır");
        notif.setOnClickListener(v -> { boolean fg = r.foreground; r.foreground = false; Scheduler.reschedule(a, true); r.foreground = fg; a.toast("Çalıştı"); });
        s.add(notif, U.m(U.lpMW(), 0, 10, 0, 0));
        U.Btn coins = U.ghost(a, "+1000 coin, +500 XP, sandık");
        coins.setOnClickListener(v -> { r.s.p.coins += 1000; Ev.List ev = new Ev.List(); r.e.gainXp(500, "test", r.e.today(r.now()), r.now(), ev); r.e.addChest(2, r.e.today(r.now()), "Test", ev); a.celebrate(ev, v); r.commit(); });
        s.add(coins, U.m(U.lpMW(), 0, 10, 0, 0));
        U.Btn wr = U.ghost(a, "Ay özetini göster");
        wr.setOnClickListener(v -> { s.dismiss(); DaySheets.wrapped(a); });
        s.add(wr, U.m(U.lpMW(), 0, 10, 0, 0));
        U.Btn reset = U.ghost(a, "⚠️ Tüm verileri sil (onboarding'e dön)");
        reset.setOnLongClickListener(v -> { r.importJson(com.ilker.rutin.core.Json.write(new State())); a.rebuild(); return true; });
        s.add(reset, U.m(U.lpMW(), 0, 10, 0, 0));
        s.add(U.tv(a, "Silmek için uzun bas.", 12, Th.dim, 700));
        a.show(s);
    }
}
