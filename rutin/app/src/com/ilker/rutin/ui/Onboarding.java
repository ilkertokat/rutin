package com.ilker.rutin.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ilker.rutin.core.Badges;
import com.ilker.rutin.core.Coach;
import com.ilker.rutin.core.Ev;
import com.ilker.rutin.core.Habit;
import com.ilker.rutin.core.Planner;
import com.ilker.rutin.core.Quests;
import com.ilker.rutin.core.State;
import com.ilker.rutin.core.Synth;
import com.ilker.rutin.core.Task;
import com.ilker.rutin.core.Tm;
import com.ilker.rutin.data.Repo;
import com.ilker.rutin.sys.Scheduler;
import com.ilker.rutin.sys.Sound;
import com.ilker.rutin.ui.sheets.MeSheets;
import com.ilker.rutin.widget.StreakWidget;
import com.ilker.rutin.widget.TodayWidget;
import com.ilker.rutin.widget.WaterWidget;

import java.util.ArrayList;

/** İlk açılış: 3 dakikada ilk gün hazır + duygusal bağ. */
@SuppressLint("ViewConstructor")
public class Onboarding extends FrameLayout {
    final MainActivity a;
    final Repo r;
    int page = 0;
    static final int PAGES = 12;
    final LinearLayout dots;
    final FrameLayout holder;
    // seçimler
    String name = "";
    int reasons = 0;
    int wake = 7 * 60 + 30, sleep = 23 * 60 + 30;
    ArrayList<Planner.Preset> presets;
    boolean[] picks;
    final ArrayList<String> identities = new ArrayList<>();
    int species = 0;
    String compName = "Pofuduk";
    int coach = 0;
    String promise = "Kendime söz veriyorum: her gün küçük de olsa bir adım atacağım ve iki gün üst üste kaçırmayacağım.";
    String signature = "";
    Runnable permRefresh;

    public Onboarding(MainActivity a) {
        super(a);
        this.a = a;
        this.r = a.r;
        setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Art.mix(Th.bg, 0xFF3A1C71, 0.35f), Th.bg}));
        LinearLayout c = U.v(a);
        addView(c, new LayoutParams(-1, -1));
        dots = U.h(a);
        dots.setGravity(Gravity.CENTER);
        dots.setPadding(U.dp(20), a.topInset + U.dp(16), U.dp(20), U.dp(6));
        c.addView(dots, U.lpMW());
        holder = new FrameLayout(a);
        c.addView(holder, new LinearLayout.LayoutParams(-1, 0, 1));
        post(() -> { dots.setPadding(U.dp(20), a.topInset + U.dp(16), U.dp(20), U.dp(6)); show(0, true); });
    }

    public boolean back() {
        if (page > 0) { show(page - 1, false); return true; }
        return false;
    }

    public void refreshPerms() { if (permRefresh != null) permRefresh.run(); }

    void updateDots() {
        dots.removeAllViews();
        for (int i = 0; i < PAGES; i++) {
            View d = new View(a);
            d.setBackground(U.rr(i <= page ? Th.primary : Art.alpha(Th.text, 0.15f), 3));
            dots.addView(d, U.m(new LinearLayout.LayoutParams(0, U.dp(5), 1), 2, 0, 2, 0));
        }
        dots.setVisibility(page == 0 ? INVISIBLE : VISIBLE);
    }

    void show(int p, boolean fwd) {
        page = p;
        updateDots();
        final View old = holder.getChildCount() > 0 ? holder.getChildAt(0) : null;
        View nv = build(p);
        holder.addView(nv, new LayoutParams(-1, -1));
        nv.setTranslationX(fwd ? U.dp(60) : -U.dp(60));
        nv.setAlpha(0);
        nv.animate().translationX(0).alpha(1).setDuration(300).start();
        if (old != null) old.animate().alpha(0).translationX(fwd ? -U.dp(60) : U.dp(60)).setDuration(200).withEndAction(() -> holder.removeView(old)).start();
    }

    void next() { U.haptic(this); show(page + 1, true); }

    /** Sayfa iskeleti: başlık, alt başlık, içerik, alt buton. */
    LinearLayout frame(String title, String sub, LinearLayout[] body, String btn, final Runnable onNext) {
        LinearLayout root = U.v(a);
        ScrollView sv = new ScrollView(a);
        sv.setVerticalScrollBarEnabled(false);
        LinearLayout c = U.v(a);
        c.setPadding(U.dp(24), U.dp(20), U.dp(24), U.dp(20));
        if (title != null) c.addView(U.tv(a, title, 28, Th.text, 900));
        if (sub != null) c.addView(U.tv(a, sub, 15, Th.dim, 700), U.m(U.lpMW(), 0, 8, 0, 14));
        LinearLayout b = U.v(a);
        c.addView(b, U.lpMW());
        body[0] = b;
        sv.addView(c);
        root.addView(sv, new LinearLayout.LayoutParams(-1, 0, 1));
        if (btn != null) {
            U.Btn nb = U.btn(a, btn, Th.primary);
            nb.setOnClickListener(v -> onNext.run());
            LinearLayout bw = U.v(a);
            bw.setPadding(U.dp(24), U.dp(8), U.dp(24), a.bottomInset + U.dp(20));
            bw.addView(nb, U.lpMW());
            root.addView(bw, U.lpMW());
        }
        return root;
    }

    View build(int p) {
        final LinearLayout[] b = new LinearLayout[1];
        switch (p) {
            case 0: {
                LinearLayout root = U.v(a);
                root.setGravity(Gravity.CENTER_HORIZONTAL);
                root.setPadding(U.dp(28), a.topInset + U.dp(40), U.dp(28), a.bottomInset + U.dp(24));
                root.addView(U.fill(a), new LinearLayout.LayoutParams(1, 0, 1));
                Views.FlameView fv = new Views.FlameView(a);
                fv.set(0, true);
                root.addView(fv, new LinearLayout.LayoutParams(U.dp(170), U.dp(190)));
                TextView t = U.tv(a, "Hayatını düzene sokmaya hazır mısın?", 30, Th.text, 900);
                t.setGravity(Gravity.CENTER);
                root.addView(t, U.m(U.lpMW(), 0, 20, 0, 0));
                TextView s = U.tv(a, "Rutin; hatırlatan, motive eden, affeden ama bırakmayan yol arkadaşın. Birlikte her gün küçük bir adım atacağız. 🔥", 16, Th.dim, 700);
                s.setGravity(Gravity.CENTER);
                root.addView(s, U.m(U.lpMW(), 0, 12, 0, 0));
                root.addView(U.fill(a), new LinearLayout.LayoutParams(1, 0, 1));
                U.Btn go = U.btn(a, "BAŞLAYALIM", Th.primary);
                go.setOnClickListener(v -> { Sound.play(a, Synth.POP); next(); });
                root.addView(go, U.lpMW());
                return root;
            }
            case 1: {
                View v = frame("Sana nasıl hitap edelim? 👋", "Koçun ve yoldaşın seni adınla çağıracak.", b, "DEVAM", () -> next());
                final EditText e = U.input(a, "Adın", name, false);
                e.addTextChangedListener(new com.ilker.rutin.ui.sheets.Sheets.SimpleWatcher(() -> name = e.getText().toString().trim()));
                b[0].addView(e);
                return v;
            }
            case 2: {
                View v = frame("Neden buradasın?", "Birden fazla seçebilirsin. Önerilerimi buna göre hazırlayacağım.", b, "DEVAM", () -> {
                    if (reasons == 0) { a.toast("En az bir tane seç 🙂"); return; }
                    next();
                });
                for (int i = 0; i < Planner.REASONS.length; i++) {
                    final int k = i;
                    final LinearLayout row = U.row(a, Planner.REASON_EMOJI[i], Planner.REASONS[i], null, Th.primary);
                    row.setBackground(U.rrs(Th.surface, 18, (reasons & (1 << i)) != 0 ? Th.primary : Th.stroke, 2));
                    row.setOnClickListener(x -> { reasons ^= (1 << k); row.setBackground(U.rrs(Th.surface, 18, (reasons & (1 << k)) != 0 ? Th.primary : Th.stroke, 2)); U.haptic(x); });
                    U.squish(row);
                    b[0].addView(row, U.mb(U.lpMW(), 10));
                }
                return v;
            }
            case 3: {
                View v = frame("Günün iskeleti ⏰", "Bildirimler sadece bu saatler arasında gelir. Gün, uyku saatinde biter; gece yarısından sonra yaptıkların da o güne sayılır.", b, "DEVAM", () -> next());
                final Views.TimeStepper w = new Views.TimeStepper(a, wake, "☀️ Uyanma saati");
                w.onChange = () -> wake = w.minute;
                b[0].addView(w, U.mb(U.lpMW(), 24));
                final Views.TimeStepper s = new Views.TimeStepper(a, sleep, "🌙 Yatma saati");
                s.onChange = () -> sleep = s.minute;
                b[0].addView(s);
                b[0].addView(U.tv(a, "💡 Tutarlı uyku saati, rutinin temelidir. Zamanında yatmak ayrı bir seri olarak da sayılır.", 13, Th.dim, 700), U.m(U.lpMW(), 0, 24, 0, 0));
                return v;
            }
            case 4: {
                if (presets == null) { presets = Planner.presets(wake, sleep); picks = Planner.defaultPicks(presets, reasons); }
                final TextView[] counter = new TextView[1];
                View v = frame("Başlangıç paketin 🎒", "Senin için seçtim. Açıp kapatabilirsin. Az ama düzenli: 5 veya daha az ile başlamak en iyisi.", b, "DEVAM", () -> {
                    int n = 0; for (boolean x : picks) if (x) n++;
                    if (n == 0) { a.toast("En az bir alışkanlık seç"); return; }
                    next();
                });
                counter[0] = U.tv(a, "", 14, Th.primary, 900);
                b[0].addView(counter[0], U.mb(U.lpMW(), 10));
                final Runnable upd = () -> {
                    int n = 0; for (boolean x : picks) if (x) n++;
                    counter[0].setText(n + " seçili" + (n > 5 ? "  ·  ⚠️ Fazlası tükenmişlik getirebilir" : "  ·  👍 Harika bir başlangıç"));
                    counter[0].setTextColor(n > 5 ? Th.warn : Th.success);
                };
                upd.run();
                for (int i = 0; i < presets.size(); i++) {
                    final int k = i;
                    Planner.Preset ps = presets.get(i);
                    final LinearLayout row = U.row(a, ps.h.emoji, ps.h.title, Habit.TYPE_NAME[ps.h.type] + " · " + ps.blurb, Th.primary);
                    final TextView chk = U.tv(a, picks[i] ? "✅" : "⬜", 20, Th.text, 400);
                    row.addView(chk);
                    row.setBackground(U.rrs(Th.surface, 18, picks[i] ? Th.primary : Th.stroke, picks[i] ? 2 : 1));
                    row.setOnClickListener(x -> {
                        picks[k] = !picks[k];
                        chk.setText(picks[k] ? "✅" : "⬜");
                        row.setBackground(U.rrs(Th.surface, 18, picks[k] ? Th.primary : Th.stroke, picks[k] ? 2 : 1));
                        U.haptic(x);
                        upd.run();
                    });
                    b[0].addView(row, U.mb(U.lpMW(), 8));
                }
                b[0].addView(U.tv(a, "Saatleri ve hedefleri sonra istediğin gibi değiştirebilirsin.", 12, Th.dim, 700), U.m(U.lpMW(), 0, 6, 0, 0));
                return v;
            }
            case 5: {
                View v = frame("Kim olmak istiyorsun? 🪞", "\"Ben ___ biriyim.\" Her tamamladığın iş, bu kimliğe verilmiş bir oy olacak.", b, "DEVAM", () -> next());
                final String[] sug = {"Düzenli yaşayan", "Her gün çalışan", "Sağlığına önem veren", "Sözünü tutan", "Erken kalkan", "Kitap okuyan", "Sakin ve dengeli", "Hedeflerine ulaşan"};
                Views.Flow f = new Views.Flow(a);
                for (final String s : sug) {
                    final String full = s + " biriyim";
                    final TextView t = U.pill(a, full, identities.contains(full));
                    t.setOnClickListener(x -> { if (identities.contains(full)) identities.remove(full); else identities.add(full); U.setPill(t, identities.contains(full)); U.haptic(x); });
                    f.addView(t);
                }
                b[0].addView(f);
                final EditText e = U.input(a, "Kendi cümleni yaz (isteğe bağlı)", "", false);
                b[0].addView(e, U.m(U.lpMW(), 0, 14, 0, 0));
                U.Btn add = U.ghost(a, "+ Ekle");
                add.setOnClickListener(x -> { String s = e.getText().toString().trim(); if (!s.isEmpty()) { identities.add(s); e.setText(""); a.toast("🪞 " + s); } });
                b[0].addView(add, U.m(U.lpMW(), 0, 8, 0, 0));
                return v;
            }
            case 6: {
                View v = frame("Yol arkadaşını seç 🐾", "Seninle birlikte büyüyecek. Sen iyi oldukça o da mutlu olacak (ve asla cezalandırılmayacak).", b, "DEVAM", () -> next());
                final Views.CompanionView big = new Views.CompanionView(a);
                big.species = species; big.mood = Art.MOOD_HAPPY;
                b[0].addView(big, new LinearLayout.LayoutParams(-1, U.dp(180)));
                LinearLayout row = U.h(a);
                final ArrayList<View> cells = new ArrayList<>();
                for (int i = 0; i < 4; i++) {
                    final int k = i;
                    LinearLayout cell = U.v(a);
                    cell.setGravity(Gravity.CENTER);
                    cell.setPadding(0, U.dp(10), 0, U.dp(10));
                    cell.setBackground(U.rrs(Th.surface, 16, species == i ? Th.primary : Th.stroke, 2));
                    TextView e = U.tv(a, Art.SPECIES_EMOJI[i], 28, Th.text, 400);
                    e.setGravity(Gravity.CENTER);
                    cell.addView(e);
                    TextView l = U.tv(a, Art.SPECIES[i], 12, Th.text, 800);
                    l.setGravity(Gravity.CENTER);
                    cell.addView(l);
                    cell.setOnClickListener(x -> {
                        species = k; big.species = k; big.mood = Art.MOOD_EXCITED; U.bounce(big); U.haptic(x);
                        for (int j = 0; j < cells.size(); j++) cells.get(j).setBackground(U.rrs(Th.surface, 16, j == k ? Th.primary : Th.stroke, 2));
                        big.postDelayed(() -> big.mood = Art.MOOD_HAPPY, 1500);
                    });
                    cells.add(cell);
                    row.addView(cell, U.m(U.lpW(1), 3, 0, 3, 0));
                }
                b[0].addView(row, U.m(U.lpMW(), 0, 10, 0, 0));
                final EditText e = U.input(a, "Adını koy", compName, false);
                e.addTextChangedListener(new com.ilker.rutin.ui.sheets.Sheets.SimpleWatcher(() -> compName = e.getText().toString().trim()));
                b[0].addView(e, U.m(U.lpMW(), 0, 14, 0, 0));
                return v;
            }
            case 7: {
                View v = frame("Koçunu seç 🎙️", "Bildirimler bu kişilikle konuşacak. Sonra ayarlardan değiştirebilirsin.", b, "DEVAM", () -> next());
                final ArrayList<View> rows = new ArrayList<>();
                for (int i = 0; i < 4; i++) {
                    final int k = i;
                    LinearLayout row = U.row(a, Coach.EMOJI[i], Coach.NAMES[i], "“" + Coach.SAMPLE[i] + "”", Th.primary);
                    row.setBackground(U.rrs(Th.surface, 18, coach == i ? Th.primary : Th.stroke, 2));
                    row.setOnClickListener(x -> { coach = k; for (int j = 0; j < rows.size(); j++) rows.get(j).setBackground(U.rrs(Th.surface, 18, j == k ? Th.primary : Th.stroke, 2)); U.haptic(x); });
                    U.squish(row);
                    rows.add(row);
                    b[0].addView(row, U.mb(U.lpMW(), 10));
                }
                return v;
            }
            case 8: {
                View v = frame("Kendine bir söz ver ✍️", "Kendine verdiğin söz, zor anlarda sana hatırlatılacak.", b, "SÖZ VERİYORUM", () -> {
                    if (signature.isEmpty()) { a.toast("Parmağınla imzala ✍️"); return; }
                    Sound.play(a, Synth.BADGE);
                    next();
                });
                final EditText e = U.input(a, "Sözün", promise, true);
                e.addTextChangedListener(new com.ilker.rutin.ui.sheets.Sheets.SimpleWatcher(() -> promise = e.getText().toString().trim()));
                b[0].addView(e);
                final Views.SignatureView sv = new Views.SignatureView(a);
                sv.onSigned = () -> signature = sv.data();
                b[0].addView(sv, U.m(new LinearLayout.LayoutParams(-1, U.dp(180)), 0, 14, 0, 0));
                TextView clr = U.tv(a, "Temizle", 13, Th.dim, 800);
                clr.setPadding(0, U.dp(8), 0, 0);
                clr.setOnClickListener(x -> { sv.clear(); signature = ""; });
                b[0].addView(clr);
                return v;
            }
            case 9: {
                View v = frame("İzinler 🔔", "Rutin'in seni zamanında dürtebilmesi için birkaç izin gerekiyor. Hepsi sadece bu telefonda kalır.", b, "DEVAM", () -> next());
                final LinearLayout list = U.v(a);
                b[0].addView(list);
                permRefresh = () -> {
                    list.removeAllViews();
                    list.addView(permRow("🔔", "Bildirimler", "Hatırlatıcılar, seri uyarıları ve kutlamalar için şart.", a.notifGranted(), () -> a.requestNotifPermission()));
                    list.addView(permRow("⏰", "Tam zamanlı alarm", "Hatırlatmaların dakikası dakikasına gelmesi için.", Scheduler.exactAllowed(a), () -> {
                        if (Build.VERSION.SDK_INT >= 31) try { a.startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + a.getPackageName()))); } catch (Exception ignored) {}
                    }));
                    PowerManager pm = (PowerManager) a.getSystemService(Context.POWER_SERVICE);
                    list.addView(permRow("🔋", "Pil optimizasyonundan muaf", "Telefon uykudayken de hatırlatabilmem için (özellikle Xiaomi/Samsung/Huawei).", pm.isIgnoringBatteryOptimizations(a.getPackageName()), () -> {
                        try { a.startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + a.getPackageName()))); } catch (Exception e) { try { a.startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)); } catch (Exception ignored) {} }
                    }));
                };
                permRefresh.run();
                if (!a.notifGranted()) a.requestNotifPermission();
                return v;
            }
            case 10: {
                View v = frame("Ana ekranına ekle 📱", "Serini ve suyunu uygulamayı açmadan gör, tek dokunuşla işaretle.", b, "DEVAM", () -> next());
                b[0].addView(MeSheets.widgetPin(a, "🔥 Seri widget'ı", StreakWidget.class), U.mb(U.lpWW(), 10));
                b[0].addView(MeSheets.widgetPin(a, "💧 Su widget'ı (+250 ml butonlu)", WaterWidget.class), U.mb(U.lpWW(), 10));
                b[0].addView(MeSheets.widgetPin(a, "📋 Bugün widget'ı (işaretleme butonlu)", TodayWidget.class), U.mb(U.lpWW(), 10));
                b[0].addView(U.tv(a, "Buton çalışmazsa: ana ekrana uzun bas → Widget'lar → Rutin.", 13, Th.dim, 700), U.m(U.lpMW(), 0, 10, 0, 0));
                return v;
            }
            default: return gift();
        }
    }

    View permRow(String e, String t, String d, boolean ok, final Runnable fix) {
        LinearLayout row = U.row(a, e, t, d, ok ? Th.success : Th.warn);
        row.setBackground(U.rrs(Th.surface, 18, ok ? Art.alpha(Th.success, 0.5f) : Th.stroke, 1.5f));
        TextView s = U.chip(a, ok ? "✓" : "İzin ver", ok ? Art.alpha(Th.success, 0.2f) : Th.primary, ok ? Th.success : 0xFFFFFFFF);
        if (!ok) s.setOnClickListener(v -> fix.run());
        row.addView(s);
        LinearLayout w = U.v(a);
        w.addView(row, U.mb(U.lpMW(), 10));
        return w;
    }

    /** Son sayfa: kurulum + ilk hediye + ilk görev (ilk başarı ilk dakikada). */
    View gift() {
        finishSetup();
        final LinearLayout root = U.v(a);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(U.dp(28), U.dp(20), U.dp(28), a.bottomInset + U.dp(24));
        root.addView(U.fill(a), new LinearLayout.LayoutParams(1, 0, 1));
        Views.MedalView m = new Views.MedalView(a);
        m.emoji = "🌱"; m.color = 0xFFFFC53D; m.animating = true;
        root.addView(m, new LinearLayout.LayoutParams(U.dp(150), U.dp(150)));
        TextView t = U.tv(a, "Hoş geldin" + (name.isEmpty() ? "" : ", " + name) + "! 🎉", 28, Th.text, 900);
        t.setGravity(Gravity.CENTER);
        root.addView(t);
        TextView s = U.tv(a, "🌱 Başlangıç rozeti  ·  🪙 100 coin hediye", 16, Th.gold, 900);
        s.setGravity(Gravity.CENTER);
        root.addView(s, U.m(U.lpMW(), 0, 8, 0, 0));
        final Views.FlameView fv = new Views.FlameView(a);
        fv.set(0, false);
        root.addView(fv, U.m(new LinearLayout.LayoutParams(U.dp(90), U.dp(100)), 0, 20, 0, 0));
        final TextView q = U.tv(a, "İlk görevin: şimdi 1 bardak su iç 💧\nAlevini yak!", 18, Th.text, 800);
        q.setGravity(Gravity.CENTER);
        root.addView(q, U.m(U.lpMW(), 0, 6, 0, 0));
        root.addView(U.fill(a), new LinearLayout.LayoutParams(1, 0, 1));
        final U.Btn drink = U.btn(a, "💧 İÇTİM!", Th.water);
        root.addView(drink, U.lpMW());
        drink.setOnClickListener(v -> {
            if (!"💧 İÇTİM!".contentEquals(drink.getText())) { a.onboardingDone(); return; }
            long now = r.now();
            Ev.List ev = new Ev.List();
            Task water = null;
            for (Task x : r.s.tasksOf(r.e.today(now))) if (x.type == Habit.COUNTER && "ml".equals(x.unit)) water = x;
            if (water != null) ev = r.e.addProgress(water.id, water.step, now);
            else {
                Task first = null;
                for (Task x : r.s.tasksOf(r.e.today(now))) if (x.isOpen()) { first = x; break; }
                if (first != null && !first.isQuantity()) ev = r.e.complete(first.id, false, now);
            }
            fv.set(0, true);
            U.bounce(fv);
            Sound.play(a, Synth.STREAK);
            a.confetti.rain(180);
            q.setText("🔥 Alevin yandı! Serin başladı.\nHer gün bir adım, birlikte. ");
            drink.colors(Th.primary, 0xFFFFFFFF);
            drink.setText("UYGULAMAYA GEÇ");
            r.commit();
        });
        post(() -> { a.confetti.rain(120); Sound.play(a, Synth.BADGE); });
        return root;
    }

    void finishSetup() {
        State.Profile p = r.s.p;
        if (p.onboarded) return;
        long now0 = r.now();
        p.name = name;
        p.reasons = reasons;
        p.wakeMin = wake;
        p.sleepMin = sleep;
        p.identities.clear();
        p.identities.addAll(identities);
        p.companion = species;
        p.companionName = compName.isEmpty() ? "Pofuduk" : compName;
        p.coach = coach;
        p.promise = promise;
        p.signature = signature;
        p.onboarded = true;
        long now = r.now();
        int td = r.e.today(now);
        p.firstDay = td;
        p.lastClosedDay = td - 1;
        p.lastActiveDay = td;
        p.lastMorningShownDay = td;
        p.coins += 100;
        p.freezes = 1;
        p.freezeWeek = Tm.weekStart(td);
        // Saatleri güncel uyanma/uyku saatine göre yeniden hesapla
        ArrayList<Planner.Preset> fresh = Planner.presets(wake, sleep);
        for (int i = 0; i < presets.size(); i++) {
            if (!picks[i]) continue;
            Habit h = presets.get(i).h;
            Habit f = fresh.get(i).h;
            h.targetMin = f.targetMin;
            h.createdDay = td;
            r.e.addHabit(h, now);
        }
        r.s.generated.remove(Integer.valueOf(td));
        r.s.tasks.removeIf(t -> t.day == td);
        r.e.ensureDay(td, now);
        Quests.generate(r.s, td);
        Quests.generateWeekly(r.s, Tm.weekStart(td));
        Ev.List ev = new Ev.List();
        Badges.check(r.s, td, ev);
        r.save();
    }
}
