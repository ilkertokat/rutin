package com.ilker.rutin.core;

import java.util.ArrayList;

/** Plan dışı olay (Rutin Kalkanı) yeniden planlaması, odak bloğu yerleştirme, başlangıç paketi. */
public final class Planner {
    private Planner() {}

    public static final String[] DIS_NAME = {"Hastalık", "Acil durum", "Misafir", "Acil iş", "Yolculuk", "Yorgunluk", "Diğer"};
    public static final String[] DIS_EMOJI = {"🤒", "🚨", "👥", "💼", "🚗", "😮‍💨", "🌀"};
    public static final int REST_OF_DAY = -1;

    public static final class Change {
        public Task t; public int kind; public int newTarget = -1; public int newFull = -1; public String text = "";
        public static final int MOVE_TOMORROW = 1, RETIME = 2, REDUCE = 3, INFO = 4, MINI_ADVICE = 5;
    }

    /** Olayın etkisini önizler (henüz uygulamaz). */
    public static ArrayList<Change> preview(Engine e, long now, int durMin, int type) {
        ArrayList<Change> out = new ArrayList<>();
        int td = e.today(now);
        int nd = e.nowDm(now);
        int sl = e.sleepDm();
        boolean rest = durMin == REST_OF_DAY;
        int endDm = rest ? sl : Math.min(sl, nd + durMin);
        ArrayList<Task> open = new ArrayList<>();
        for (Task t : e.sorted(td)) if (t.isOpen()) open.add(t);
        int stagger = 0;
        int otherLoad = 0;
        for (Task t : open) if (t.type != Habit.DURATION && t.type != Habit.COUNTER) otherLoad += 20;
        for (Task t : open) {
            int tg = e.targetDm(t);
            Change c = new Change(); c.t = t;
            switch (t.type) {
                case Habit.FLEX:
                    if (rest || tg < 0 || tg < endDm) {
                        c.kind = Change.MOVE_TOMORROW;
                        c.text = t.emoji + " " + t.title + " → Yarın";
                        out.add(c);
                    }
                    break;
                case Habit.ANCHOR: case Habit.REQUIRED: case Habit.ONCE:
                    if (rest) {
                        if (t.streak) {
                            c.kind = Change.MINI_ADVICE;
                            c.text = t.emoji + " " + t.title + (t.hasMini() ? ": yapabilirsen mini versiyon (" + t.miniText + ") yeter" : ": yapabilirsen harika, yapamazsan sorun değil");
                            out.add(c);
                        }
                    } else if (tg >= 0 && tg < endDm) {
                        int nt = endDm + 10 + stagger * 15;
                        stagger++;
                        int dl = t.deadlineMin >= 0 ? e.dmOf(t.deadlineMin) : Integer.MAX_VALUE;
                        if (nt + 10 > dl || nt > sl - 20) {
                            c.kind = Change.MINI_ADVICE;
                            c.text = t.emoji + " " + t.title + ": son saate yetişmiyor olabilir, mini versiyonu yapmayı dene";
                        } else {
                            c.kind = Change.RETIME;
                            c.newTarget = Tm.norm(nt);
                            c.text = t.emoji + " " + t.title + "  " + Tm.hhmm(t.targetMin) + " → " + Tm.hhmm(c.newTarget);
                        }
                        out.add(c);
                    }
                    break;
                case Habit.DURATION: {
                    int rem = t.full - t.progress;
                    int capacity = Math.max(0, (sl - 30) - endDm - otherLoad);
                    if (rest) {
                        c.kind = Change.MINI_ADVICE;
                        c.text = t.emoji + " " + t.title + ": bugün mini hedef (" + Tm.dur(Math.max(t.mini, 0)) + ") yeter";
                        out.add(c);
                    } else if (rem > capacity * 0.6) {
                        int nf = t.progress + (int) (capacity * 0.6 / 25) * 25;
                        nf = Math.max(nf, Math.max(t.mini, 25));
                        if (nf < t.full) {
                            c.kind = Change.REDUCE; c.newFull = nf;
                            c.text = t.emoji + " " + t.title + " bugün " + Tm.dur(t.full) + " → " + Tm.dur(nf);
                            out.add(c);
                        }
                    }
                    break;
                }
                case Habit.COUNTER:
                    if (rest && t.mini > 0) {
                        c.kind = Change.MINI_ADVICE;
                        c.text = t.emoji + " " + t.title + ": mini hedef (" + t.amount(t.mini) + ") yeter";
                        out.add(c);
                    }
                    break;
            }
        }
        Change info = new Change(); info.kind = Change.INFO;
        info.text = rest ? "🔕 Bildirimler yatma saatine kadar susturulacak"
                : "🔕 Bildirimler " + Tm.hhmm(Tm.norm(endDm)) + "'e kadar susturulacak";
        out.add(0, info);
        if (rest) {
            Change f = new Change(); f.kind = Change.INFO;
            f.text = type <= 1 ? "🛡️ Gün tamamlanamazsa serin ÜCRETSİZ korunur" :
                    e.p.freezes > 0 ? "🧊 Gün tamamlanamazsa 1 Seri Dondurma kullanılır (" + e.p.freezes + " hakkın var)"
                            : "⚠️ Dondurma hakkın yok: mini versiyonlarla seriyi kurtarabilirsin";
            out.add(1, f);
        }
        return out;
    }

    public static void apply(Engine e, ArrayList<Change> changes, long now, int durMin, int type) {
        int td = e.today(now);
        int nd = e.nowDm(now);
        boolean rest = durMin == REST_OF_DAY;
        int sl = e.sleepDm();
        int endDm = rest ? sl : Math.min(sl, nd + durMin);
        for (Change c : changes) {
            if (c.t == null) continue;
            switch (c.kind) {
                case Change.MOVE_TOMORROW: e.move(c.t.id, td + 1, c.t.targetMin, now); break;
                case Change.RETIME:
                    c.t.targetMin = c.newTarget; c.t.custom = true; c.t.lastNotif = 0; c.t.snoozeUntil = 0; c.t.notifCount = 0;
                    break;
                case Change.REDUCE:
                    if (c.t.origFull == 0) c.t.origFull = c.t.full;
                    c.t.full = c.newFull; c.t.custom = true;
                    if (c.t.mini >= c.t.full) c.t.mini = Math.max(0, c.t.full / 2);
                    break;
            }
        }
        e.p.shieldUntil = td * 1440L + endDm;
        e.p.shieldType = type;
        if (rest) e.p.shieldDay = td;
        e.s.day(td).disrupted = true;
        e.refreshDay(td, now, null);
    }

    /** Birkaç günlük hastalık modu. */
    public static void sickDays(Engine e, int days, long now) {
        int td = e.today(now);
        e.p.sickUntil = td + days - 1;
        for (Task t : e.s.tasksOf(td)) if (t.isOpen()) e.applySick(t);
        e.resyncFrom(td + 1, now);
        e.s.day(td).disrupted = true;
        e.p.shieldDay = td; e.p.shieldType = 0;
    }

    // ---------------- Odak bloğu yerleştirme ----------------
    /** Kalan süreli hedef için bugünün boşluklarına blok başlangıçları önerir (takvim dakikası). */
    public static ArrayList<Integer> suggestBlocks(Engine e, Task t, long now, int blockMin, int gapMin) {
        ArrayList<Integer> out = new ArrayList<>();
        int rem = t.full - t.progress;
        if (rem <= 0) return out;
        int need = (int) Math.ceil(rem / (double) blockMin);
        int td = e.today(now);
        int start = Math.max(e.nowDm(now) + 5, e.wakeDm() + 30);
        int myTg = e.targetDm(t);
        if (myTg >= 0 && myTg > start) start = myTg;
        start = (start + 4) / 5 * 5;
        int end = e.sleepDm() - 45;
        // meşgul aralıklar
        ArrayList<int[]> busy = new ArrayList<>();
        for (Task o : e.s.tasksOf(td)) {
            if (o == t || !o.isOpen()) continue;
            int tg = e.targetDm(o);
            if (tg < 0 || o.type == Habit.COUNTER) continue;
            busy.add(new int[]{tg - 10, tg + (o.type == Habit.DURATION ? 50 : 25)});
            for (int b : o.blocks) { int bd = e.dmOf(b); busy.add(new int[]{bd, bd + blockMin}); }
        }
        int x = start;
        while (out.size() < need && x + blockMin <= end) {
            boolean clash = false;
            for (int[] b : busy) if (x < b[1] && x + blockMin > b[0]) { clash = true; x = b[1]; break; }
            if (clash) { x = (x + 4) / 5 * 5; continue; }
            out.add(Tm.norm(x));
            x += blockMin + gapMin;
        }
        return out;
    }

    // ---------------- Başlangıç paketi ----------------
    public static final class Preset {
        public final Habit h; public final int reasons; public final String blurb;
        Preset(Habit h, int reasons, String blurb) { this.h = h; this.reasons = reasons; this.blurb = blurb; }
    }

    public static final String[] REASONS = {"Daha düzenli olmak", "Ders / iş verimi", "Sağlık", "Uyku düzeni", "Kötü alışkanlık bırakmak"};
    public static final String[] REASON_EMOJI = {"🗂️", "📚", "❤️", "😴", "🚭"};

    static Habit h(String emoji, String title, int type, int area, int target, int full, int mini, String unit, int step, String miniText, int days, String why) {
        Habit h = new Habit();
        h.emoji = emoji; h.title = title; h.type = type; h.area = area; h.targetMin = target; h.full = full; h.mini = mini;
        h.unit = unit; h.step = step; h.miniText = miniText; h.daysMask = days; h.why = why; h.streak = Habit.defaultStreak(type);
        return h;
    }

    public static ArrayList<Preset> presets(int wake, int sleep) {
        ArrayList<Preset> r = new ArrayList<>();
        int all = 127, weekdays = 31, mwf = 1 | 4 | 16;
        r.add(new Preset(h("💧", "Su iç", Habit.COUNTER, Area.HEALTH, -1, 4000, 2000, "ml", 250, "", all, "Enerjim, cildim ve odağım için."), 31, "Günde 4 L, mini 2 L"));
        r.add(new Preset(h("⏱️", "Ders çalış", Habit.DURATION, Area.CAREER, Tm.norm(15 * 60), 240, 60, "", 1, "", all, "Hedeflerime ulaşmak için her gün biraz."), 2, "Günde 4 saat, mini 1 saat"));
        r.add(new Preset(h("🌅", "Günün 3 önceliğini yaz", Habit.ANCHOR, Area.CAREER, Tm.norm(wake + 20), 1, 0, "", 1, "1 öncelik yaz", all, "Güne ne yapacağımı bilerek başlamak için."), 1 | 2, "Sabah, 2 dakika"));
        r.add(new Preset(h("🛏️", "Yatağını topla", Habit.ANCHOR, Area.HOME, Tm.norm(wake + 10), 1, 0, "", 1, "", all, "Günün ilk zaferi."), 1, "Uyanınca ilk iş"));
        r.add(new Preset(h("📖", "Kitap oku (20 sayfa)", Habit.REQUIRED, Area.MIND, Tm.norm(sleep - 90), 1, 0, "", 1, "5 sayfa", all, "Her gün biraz daha bilge."), 1 | 2, "Mini: 5 sayfa"));
        r.add(new Preset(h("🚶", "Yürüyüş (30 dk)", Habit.REQUIRED, Area.HEALTH, Tm.norm(18 * 60), 1, 0, "", 1, "10 dk yürü", all, "Bedenim hareketle yaşar."), 4 | 8 | 16, "Mini: 10 dk"));
        r.add(new Preset(h("🏋️", "Spor", Habit.REQUIRED, Area.HEALTH, Tm.norm(18 * 60 + 30), 1, 0, "", 1, "10 şınav", mwf, "Güçlü bir beden, güçlü bir zihin."), 4, "Pzt-Çar-Cum, mini: 10 şınav"));
        r.add(new Preset(h("🧘", "Meditasyon (10 dk)", Habit.ANCHOR, Area.SOUL, Tm.norm(wake + 30), 1, 0, "", 1, "2 dk nefes", all, "Zihnimi sakinleştirmek için."), 8 | 16, "Mini: 2 dk nefes"));
        r.add(new Preset(h("💊", "Vitamin / ilaç", Habit.ANCHOR, Area.HEALTH, Tm.norm(wake + 60), 1, 0, "", 1, "", all, "Sağlığım ihmale gelmez."), 4, "Kritik öncelik"));
        r.add(new Preset(h("🥗", "Sağlıklı öğün", Habit.REQUIRED, Area.HEALTH, Tm.norm(13 * 60), 1, 0, "", 1, "1 porsiyon sebze", all, "Ne yersem oyum."), 4, "Mini: 1 porsiyon sebze"));
        r.add(new Preset(h("🗣️", "Dil pratiği", Habit.DURATION, Area.MIND, Tm.norm(20 * 60), 20, 5, "", 1, "", weekdays, "Yeni bir dil, yeni bir dünya."), 2, "20 dk, mini 5 dk"));
        r.add(new Preset(h("📵", "Ekranı bırak", Habit.ANCHOR, Area.SOUL, Tm.norm(sleep - 60), 1, 0, "", 1, "", all, "İyi uyku, iyi yarın."), 8 | 16, "Yatmadan 1 saat önce"));
        r.add(new Preset(h("🦷", "Diş fırçala & yatağa hazırlan", Habit.ANCHOR, Area.HEALTH, Tm.norm(sleep - 20), 1, 0, "", 1, "", all, "Güzel bir gece rutini."), 8, "Uyku öncesi"));
        r.add(new Preset(h("📝", "Günlük yaz", Habit.REQUIRED, Area.SOUL, Tm.norm(sleep - 45), 1, 0, "", 1, "1 cümle", all, "Kendimi tanımak için."), 1 | 16, "Mini: 1 cümle"));
        r.add(new Preset(h("🧹", "10 dk ortalık toplama", Habit.FLEX, Area.HOME, -1, 1, 0, "", 1, "", all, "Düzenli ev, düzenli zihin."), 1, "Esnek, boş zamanda"));
        r.add(new Preset(h("📞", "Sevdiğin birini ara", Habit.FLEX, Area.SOCIAL, -1, 1, 0, "", 1, "", 4 | 64, "İlişkiler emek ister."), 0, "Esnek, Çar & Paz"));
        return r;
    }

    /** Onboarding'de seçilen nedenlere göre önerilen varsayılanlar (en fazla 5). */
    public static boolean[] defaultPicks(ArrayList<Preset> ps, int reasons) {
        boolean[] pick = new boolean[ps.size()];
        pick[0] = true;
        int n = 1;
        for (int i = 1; i < ps.size() && n < 5; i++) {
            if ((ps.get(i).reasons & reasons) != 0 && ps.get(i).h.type != Habit.FLEX) { pick[i] = true; n++; }
        }
        if (n < 3) { pick[2] = true; pick[4] = true; }
        return pick;
    }
}
