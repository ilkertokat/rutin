import com.ilker.rutin.core.*;

import java.util.ArrayList;

/** Çekirdek mantık testleri (JVM üzerinde, Android'siz). */
public class CoreTest {
    static int pass = 0, fail = 0;

    static void check(boolean c, String name) {
        if (c) pass++;
        else { fail++; System.out.println("  ✗ BAŞARISIZ: " + name); }
    }

    static void eq(long a, long b, String name) { check(a == b, name + " (beklenen " + b + ", gelen " + a + ")"); }

    // 2026-09-24 Perşembe
    static final int D0 = Tm.epochDay(2026, 9, 24);

    static long at(int day, int h, int m) { return day * 1440L + h * 60 + m; }

    static State fresh() {
        State s = new State();
        s.p.onboarded = true;
        s.p.name = "Test";
        s.p.wakeMin = 7 * 60 + 30;
        s.p.sleepMin = 23 * 60 + 30;
        s.p.firstDay = D0 - 10;
        s.p.lastClosedDay = D0 - 1;
        s.p.freezes = 0;
        s.p.freezeWeek = Tm.weekStart(D0);
        return s;
    }

    static Habit habit(Engine e, String title, int type, int target, int full, int mini, String unit, int step, long now) {
        Habit h = new Habit();
        h.title = title; h.type = type; h.targetMin = target; h.full = full; h.mini = mini; h.unit = unit; h.step = step;
        h.createdDay = D0 - 30; h.streak = Habit.defaultStreak(type);
        return e.addHabit(h, now);
    }

    static Task taskOf(State s, long habitId, int day) {
        for (Task t : s.tasks) if (t.habitId == habitId && t.day == day && t.fromDay < 0) return t;
        return null;
    }

    public static void main(String[] a) {
        System.out.println("== Rutin çekirdek testleri ==");
        testTime();
        testJson();
        testCompleteAndStreak();
        testFreezeBreakRepair();
        testTimedNotifications();
        testCounterNotifications();
        testBudgetGrouping();
        testDisruption();
        testBlocksQuestsBadges();
        testSynth();
        testDurationAndFocus();
        testFirstDayAndComeback();
        testRewards();
        System.out.println("Geçen: " + pass + "  Başarısız: " + fail);
        if (fail > 0) System.exit(1);
    }

    static void testTime() {
        eq(Tm.rollover(450, 1410), 210, "rollover 07:30/23:30 = 03:30");
        eq(Tm.rollover(540, 60), 300, "rollover 09:00/01:00 = 05:00");
        eq(Tm.weekday(D0), 3, "24 Eylül 2026 Perşembe");
        int[] ymd = Tm.ymd(D0);
        check(ymd[0] == 2026 && ymd[1] == 9 && ymd[2] == 24, "ymd dönüşümü");
        eq(Tm.epochDay(2024, 2, 29) + 1, Tm.epochDay(2024, 3, 1), "artık yıl");
        // 00:30'da yapılan iş dün sayılır
        eq(Tm.day(at(D0, 0, 30) + 1440, 210), D0, "gece 00:30 → önceki mantıksal gün");
        eq(Tm.dm(at(D0 + 1, 0, 30), 210), 1440 + 30, "gün içi dakika gece yarısını aşar");
        check(Tm.dateLong(D0).equals("24 Eylül Perşembe"), "Türkçe tarih: " + Tm.dateLong(D0));
    }

    static void testJson() {
        State s = fresh();
        Engine e = new Engine(s);
        long now = at(D0, 9, 0);
        habit(e, "Su \"iç\"\nçok", Habit.COUNTER, -1, 4000, 2000, "ml", 250, now);
        e.tick(now);
        s.p.hourSent[5] = 3;
        s.p.identities.add("Ben düzenli biriyim ğüşiöç");
        String js = Json.write(s);
        State b = Json.decode(Json.parse(js), State.class);
        check(b.habits.size() == 1 && b.habits.get(0).title.equals("Su \"iç\"\nçok"), "json: alışkanlık başlığı");
        check(b.tasks.size() == s.tasks.size(), "json: görevler");
        check(b.p.hourSent[5] == 3, "json: int dizi");
        check(b.p.identities.get(0).equals("Ben düzenli biriyim ğüşiöç"), "json: Türkçe karakterler");
        check(b.p.weekly != null && b.p.weekly.kind == s.p.weekly.kind, "json: iç nesne");
        check(Json.write(b).equals(js), "json: gidiş-dönüş aynı");
    }

    static void testCompleteAndStreak() {
        State s = fresh();
        Engine e = new Engine(s);
        long now = at(D0, 7, 0);
        Habit w = habit(e, "Su", Habit.COUNTER, -1, 4000, 2000, "ml", 250, now);
        Habit r = habit(e, "Kitap", Habit.REQUIRED, 21 * 60, 1, 0, "", 1, now);
        Habit an = habit(e, "Meditasyon", Habit.ANCHOR, 8 * 60, 1, 0, "", 1, now);
        e.tick(now);
        eq(s.tasksOf(D0).size(), 3, "3 görev üretildi");
        Task ta = taskOf(s, an.id, D0);
        Ev.List ev = e.complete(ta.id, false, at(D0, 7, 40));
        check(ta.early, "erken tamamlama işaretlendi");
        check(ev.has(Ev.EARLY) && ev.has(Ev.TASK_DONE), "erken + tamam olayları");
        check(ev.xp() >= 18, "erken bonuslu XP: " + ev.xp());
        check(!e.todayMet(at(D0, 8, 0)), "henüz seri uzamadı");
        Task tw = taskOf(s, w.id, D0);
        ev = e.addProgress(tw.id, 2000, at(D0, 12, 0));
        check(ev.has(Ev.HALFWAY), "yarı yol olayı");
        check(e.satisfied(tw), "mini hedef seriyi karşılar");
        Task tr = taskOf(s, r.id, D0);
        ev = e.complete(tr.id, false, at(D0, 21, 5));
        check(ev.has(Ev.STREAK_UP), "tüm zorunlular (su mini dahil) → seri uzadı");
        eq(e.displayStreak(at(D0, 21, 6)), 1, "görünen seri 1");
        check(!ev.has(Ev.PERFECT), "su tam değil → mükemmel gün değil");
        ev = e.addProgress(tw.id, 2000, at(D0, 21, 10));
        check(ev.has(Ev.COUNTER_FULL) && ev.has(Ev.PERFECT), "su tamam → mükemmel gün");
        check(s.chests.size() >= 1, "mükemmel gün sandığı");
        // Geri al
        ev = e.addProgress(tw.id, -250, at(D0, 21, 11));
        check(tw.status == Task.PENDING && tw.progress == 3750, "sayaç geri alma");
        e.addProgress(tw.id, 250, at(D0, 21, 12));
        // Ertesi gün kapanış
        ev = e.tick(at(D0 + 1, 8, 0));
        eq(s.p.streak, 1, "kapanış sonrası seri 1");
        eq(s.day(D0).state, DayRec.FULL, "gün FULL");
        check(s.island.size() == 1, "adaya yapı eklendi");
        eq(s.tasksOf(D0 + 1).size(), 3, "yeni gün görevleri");
        eq(e.displayStreak(at(D0 + 1, 8, 0)), 1, "yeni günde görünen seri 1 (henüz uzamadı)");
    }

    static void testFreezeBreakRepair() {
        State s = fresh();
        Engine e = new Engine(s);
        long now = at(D0, 7, 0);
        Habit r = habit(e, "Spor", Habit.REQUIRED, 18 * 60, 1, 0, "", 1, now);
        s.p.streak = 10; s.p.bestStreak = 10;
        s.p.freezes = 1;
        e.tick(now);
        // D0 kaçırıldı, dondurma var
        e.tick(at(D0 + 1, 8, 0));
        eq(s.day(D0).state, DayRec.FROZEN, "dondurma kullanıldı");
        eq(s.p.streak, 10, "seri korundu");
        eq(s.p.freezes, 0, "dondurma harcandı");
        check(!s.notices.isEmpty(), "dondurma bildirimi");
        // D0+1 kaçırıldı, dondurma yok → kırıldı, onarma açıldı
        e.tick(at(D0 + 2, 8, 0));
        eq(s.day(D0 + 1).state, DayRec.BROKEN, "seri kırıldı");
        eq(s.p.streak, 0, "seri 0");
        eq(s.p.repairOld, 10, "onarma: eski seri");
        // 2 gün tamamla → onarıldı
        e.complete(taskOf(s, r.id, D0 + 2).id, false, at(D0 + 2, 18, 0));
        e.tick(at(D0 + 3, 8, 0));
        e.complete(taskOf(s, r.id, D0 + 3).id, false, at(D0 + 3, 18, 0));
        Ev.List ev = e.tick(at(D0 + 4, 8, 0));
        check(ev.has(Ev.REPAIRED), "onarma olayı");
        eq(s.p.streak, 12, "onarılan seri = 10 + 2");
        check(s.hasBadge("anka", 1), "Anka Kuşu rozeti");
        java.util.HashSet<String> seen = new java.util.HashSet<>();
        boolean dup = false;
        for (String n : s.notices) if (!seen.add(n)) dup = true;
        check(!dup, "bildirim notları tekrarlanmaz");
        // Plan dışı olay (hastalık, günün geri kalanı) → ücretsiz koruma
        long t = at(D0 + 4, 12, 0);
        ArrayList<Planner.Change> ch = Planner.preview(e, t, Planner.REST_OF_DAY, 0);
        Planner.apply(e, ch, t, Planner.REST_OF_DAY, 0);
        e.tick(at(D0 + 5, 8, 0));
        eq(s.day(D0 + 4).state, DayRec.FROZEN, "hastalık günü ücretsiz donduruldu");
        eq(s.p.streak, 12, "seri hastalıkta korundu");
    }

    static ArrayList<Long> simulate(Engine e, NotifEngine ne, long from, long to) {
        ArrayList<Long> sent = new ArrayList<>();
        long now = from;
        int guard = 0;
        while (now < to && guard++ < 500) {
            e.tick(now);
            NotifEngine.Plan pl = ne.plan(now, false, 0);
            if (!pl.due.isEmpty()) { sent.add(now); ne.recordSent(pl.due, now); }
            for (int k : pl.sys) ne.recordSys(k, now);
            if (!pl.due.isEmpty() || !pl.sys.isEmpty()) pl = ne.plan(now, false, 0);
            if (pl.nextAt == NotifEngine.NONE) break;
            now = Math.max(now + 1, pl.nextAt);
        }
        return sent;
    }

    static void testTimedNotifications() {
        State s = fresh();
        s.p.budget = 99;
        Engine e = new Engine(s);
        NotifEngine ne = new NotifEngine(e);
        long now = at(D0, 7, 0);
        Habit an = habit(e, "Meditasyon", Habit.ANCHOR, 8 * 60, 1, 0, "", 1, now);
        ArrayList<Long> sent = simulate(e, ne, now, at(D0, 12, 0));
        StringBuilder sb = new StringBuilder();
        for (long v : sent) sb.append(Tm.hhmm((int) (v % 1440))).append(' ');
        System.out.println("  Çapa 08:00 (son 10:00) bildirimleri: " + sb);
        check(sent.size() >= 7, "yeterli sayıda kademeli bildirim");
        eq(sent.get(0), at(D0, 7, 50), "hazırlık 07:50");
        eq(sent.get(1), at(D0, 8, 0), "zamanı 08:00");
        eq(sent.get(2), at(D0, 8, 15), "tolerans sonrası 08:15");
        check(sent.get(sent.size() - 1) <= at(D0, 9, 50), "son saatten önce biter");
        // aralıklar giderek kısalır (dürtme sonrası)
        boolean ok = true;
        for (int i = 3; i < sent.size() - 1; i++) {
            long a1 = sent.get(i) - sent.get(i - 1), a2 = sent.get(i + 1) - sent.get(i);
            if (a2 > a1 + 1) ok = false;
        }
        check(ok, "zaman daraldıkça sıklık artar");
        check(ne.levelAt(taskOf(s, an.id, D0), at(D0, 9, 40)) == NotifEngine.L_CRIT, "son %20 kritik");
        // Erken tamamlanan görev için bildirim yok
        State s2 = fresh(); Engine e2 = new Engine(s2); NotifEngine n2 = new NotifEngine(e2);
        Habit h2 = habit(e2, "Kahvaltı", Habit.ANCHOR, 9 * 60, 1, 0, "", 1, now);
        e2.tick(now);
        e2.complete(taskOf(s2, h2.id, D0).id, false, at(D0, 8, 30));
        eq(n2.nextFor(taskOf(s2, h2.id, D0), at(D0, 8, 31)), NotifEngine.NONE, "erken bitince bildirim iptal");
        // Ertele
        State s3 = fresh(); Engine e3 = new Engine(s3); NotifEngine n3 = new NotifEngine(e3);
        Habit h3 = habit(e3, "Rapor", Habit.REQUIRED, 10 * 60, 1, 0, "", 1, now);
        e3.tick(now);
        Task t3 = taskOf(s3, h3.id, D0);
        t3.lastNotif = at(D0, 10, 0);
        e3.snooze(t3.id, 30, at(D0, 10, 1));
        eq(n3.nextFor(t3, at(D0, 10, 2)), at(D0, 10, 31), "erteleme saygı görür");
        // Uyku saatinden sonra bildirim yok
        Habit h4 = habit(e3, "Gece", Habit.ANCHOR, 23 * 60 + 20, 1, 0, "", 1, now);
        Task t4 = taskOf(s3, h4.id, D0);
        t4.lastNotif = at(D0, 23, 29);
        eq(n3.nextFor(t4, at(D0, 23, 29)), NotifEngine.NONE, "uyku saatinden sonra sessiz");
    }

    static void testCounterNotifications() {
        State s = fresh();
        s.p.budget = 99;
        Engine e = new Engine(s);
        NotifEngine ne = new NotifEngine(e);
        long now = at(D0, 7, 0);
        Habit w = habit(e, "Su", Habit.COUNTER, -1, 4000, 2000, "ml", 250, now);
        e.tick(now);
        Task t = taskOf(s, w.id, D0);
        long first = ne.nextFor(t, now);
        eq(first, at(D0, 8, 0), "ilk su hatırlatması uyanış + 30 dk");
        // Öndeyken bildirim yok: 12:00'de beklenen ~1.15 L, içilen 2 L
        t.progress = 2000; t.lastProgressAt = at(D0, 11, 55); t.lastNotif = at(D0, 8, 0);
        long n = ne.nextFor(t, at(D0, 12, 0));
        check(n > at(D0, 15, 0), "öndeyken uzun süre sessiz: " + Tm.hhmm((int) (n % 1440)));
        // Geride: 20:00, 1 L
        t.progress = 1000; t.lastProgressAt = at(D0, 13, 0); t.lastNotif = at(D0, 19, 50);
        long n2 = ne.nextFor(t, at(D0, 20, 0));
        check(n2 - at(D0, 19, 50) <= 12, "çok geride → 10 dk aralık: " + (n2 - at(D0, 19, 50)));
        check(ne.levelAt(t, at(D0, 20, 0)) >= NotifEngine.L_PUSH, "çok geride → ısrar seviyesi");
        // Hafif geride
        t.progress = 1500; t.lastNotif = at(D0, 13, 0); t.lastProgressAt = at(D0, 12, 0);
        long n3 = ne.nextFor(t, at(D0, 13, 1));
        check(n3 - at(D0, 13, 0) >= 30, "hafif geride → seyrek: " + (n3 - at(D0, 13, 0)));
        // Tamamlanınca bildirim yok
        e.addProgress(t.id, 3000, at(D0, 20, 5));
        eq(ne.nextFor(t, at(D0, 20, 6)), NotifEngine.NONE, "tamamlanınca susar");
    }

    static void testBudgetGrouping() {
        State s = fresh();
        s.p.budget = 4;
        Engine e = new Engine(s);
        NotifEngine ne = new NotifEngine(e);
        long now = at(D0, 7, 0);
        for (int i = 0; i < 6; i++) habit(e, "İş" + i, Habit.REQUIRED, 10 * 60, 1, 0, "", 1, now);
        e.tick(now);
        NotifEngine.Plan pl = ne.plan(at(D0, 10, 0), false, 0);
        eq(pl.due.size(), 6, "6 iş aynı anda → tek grup");
        ne.recordSent(pl.due, at(D0, 10, 0));
        eq(ne.sentLastHour(at(D0, 10, 1)), 1, "grup tek bildirim sayılır");
        // Bütçe doluyken kritik olmayanlar ertelenir
        s.p.sentTimes.add(at(D0, 10, 5)); s.p.sentTimes.add(at(D0, 10, 10)); s.p.sentTimes.add(at(D0, 10, 12));
        NotifEngine.Plan p2 = ne.plan(at(D0, 10, 15), false, 0);
        check(p2.due.isEmpty() && p2.deferredByBudget, "bütçe dolu → erteleme");
        // Odak sırasında kritik olmayanlar ertelenir
        s.p.sentTimes.clear();
        NotifEngine.Plan p3 = ne.plan(at(D0, 10, 15), true, at(D0, 10, 40));
        check(p3.due.isEmpty(), "odak sırasında sessiz");
    }

    static void testDisruption() {
        State s = fresh();
        Engine e = new Engine(s);
        long now = at(D0, 7, 0);
        Habit a1 = habit(e, "Yürüyüş", Habit.REQUIRED, 15 * 60, 1, 0, "", 1, now);
        Habit f1 = habit(e, "Toplama", Habit.FLEX, 15 * 60 + 30, 1, 0, "", 1, now);
        Habit d1 = habit(e, "Ders", Habit.DURATION, -1, 240, 60, "", 1, now);
        e.tick(now);
        long t = at(D0, 14, 0);
        ArrayList<Planner.Change> ch = Planner.preview(e, t, 120, 2);
        boolean retimed = false, moved = false;
        for (Planner.Change c : ch) {
            if (c.kind == Planner.Change.RETIME && c.t.habitId == a1.id) { retimed = true; eq(c.newTarget, 16 * 60 + 10, "yeni saat 16:10"); }
            if (c.kind == Planner.Change.MOVE_TOMORROW && c.t.habitId == f1.id) moved = true;
        }
        check(retimed, "saatli iş kaydırıldı");
        check(moved, "esnek iş yarına");
        Planner.apply(e, ch, t, 120, 2);
        check(s.p.shieldUntil == at(D0, 16, 0), "kalkan 16:00'ya kadar");
        NotifEngine ne = new NotifEngine(e);
        NotifEngine.Plan pl = ne.plan(at(D0, 15, 0), false, 0);
        check(pl.due.isEmpty(), "kalkan sırasında bildirim yok");
        check(taskOf(s, f1.id, D0).status == Task.MOVED, "esnek iş taşındı");
        boolean tomorrow = false;
        for (Task x : s.tasksOf(D0 + 1)) if (x.habitId == f1.id && x.fromDay == D0) tomorrow = true;
        check(tomorrow, "yarın kopyası oluştu");
        // Akşam 20:00'de 3 saatlik olay → ders hedefi düşer
        ArrayList<Planner.Change> ch2 = Planner.preview(e, at(D0, 19, 0), 120, 3);
        boolean reduced = false;
        for (Planner.Change c : ch2) if (c.kind == Planner.Change.REDUCE && c.t.habitId == d1.id) reduced = true;
        check(reduced, "süreli hedef kalan zamana göre düşürülür");
    }

    static void testBlocksQuestsBadges() {
        State s = fresh();
        Engine e = new Engine(s);
        long now = at(D0, 9, 0);
        Habit d = habit(e, "Ders", Habit.DURATION, -1, 240, 60, "", 1, now);
        habit(e, "Yürüyüş", Habit.REQUIRED, 12 * 60, 1, 0, "", 1, now);
        e.tick(now);
        Task t = taskOf(s, d.id, D0);
        ArrayList<Integer> bl = Planner.suggestBlocks(e, t, now, 50, 10);
        eq(bl.size(), 5, "240 dk için 5x50 dk blok");
        boolean clash = false;
        for (int b : bl) if (b > 11 * 60 + 50 - 50 && b < 12 * 60 + 25) clash = true;
        check(!clash, "bloklar diğer işlerle çakışmaz");
        eq(s.quests.size(), 3, "3 günlük görev");
        check(s.p.weekly != null, "haftalık meydan okuma");
        e.complete(taskOf(s, s.habits.get(1).id, D0).id, false, at(D0, 12, 0));
        check(s.hasBadge("start", 1), "Başlangıç rozeti");
        eq(Badges.totalTiers() >= 90 ? 1 : 0, 1, "90+ rozet kademesi (" + Badges.totalTiers() + ")");
        eq(Game.level(0), 1, "seviye 1");
        eq(Game.level(80), 2, "80 XP seviye 2");
        check(Game.title(12).contains("Kararlı"), "unvan");
    }

    static void testSynth() {
        for (int i = 0; i < Synth.COUNT; i++) for (int p = 0; p < 4; p++) {
            short[] pcm = Synth.render(i, p);
            check(pcm.length > 100, "ses " + i + "/" + p);
        }
        short[] buf = new short[4410];
        for (int k = 0; k < Synth.AMBIENT.length; k++) new Synth.Noise(k).fill(buf, 0.5f);
        check(true, "ambiyans");
    }

    static void testDurationAndFocus() {
        State s = fresh();
        Engine e = new Engine(s);
        NotifEngine ne = new NotifEngine(e);
        long now = at(D0, 9, 0);
        Habit d = habit(e, "Ders", Habit.DURATION, -1, 240, 60, "", 1, now);
        e.tick(now);
        Task t = taskOf(s, d.id, D0);
        long n = ne.nextFor(t, now);
        check(n > at(D0, 9, 0) && n < at(D0, 23, 0), "süreli hedef için hatırlatma var: " + Tm.hhmm((int) (n % 1440)));
        check(ne.pressure(t, at(D0, 21, 0)) > 0.9, "akşam baskı yüksek");
        Ev.List ev = e.focusDone(t.id, "Ders", 50, 50, true, 4, true, "Bölüm 3", "", at(D0, 10, 0), at(D0, 10, 50));
        eq(t.progress, 50, "odak ilerlemeye eklendi");
        check(ev.xp() > 0, "odak XP");
        e.focusDone(t.id, "Ders", 25, 25, true, 5, true, "", "", at(D0, 11, 0), at(D0, 11, 25));
        check(e.satisfied(t), "75 dk ≥ mini 60 dk");
        check(s.hasBadge("pomodoro", 1), "İlk Pomodoro rozeti");
    }

    static void testRewards() {
        State s = fresh();
        s.p.dailyGoal = 30;
        Engine e = new Engine(s);
        long now = at(D0, 7, 0);
        Habit a1 = habit(e, "A", Habit.REQUIRED, 9 * 60, 1, 0, "", 1, now);
        Habit a2 = habit(e, "B", Habit.REQUIRED, 10 * 60, 1, 0, "", 1, now);
        e.tick(now);
        Ev.List ev = e.complete(taskOf(s, a1.id, D0).id, false, at(D0, 8, 0));
        ev.addAll(e.complete(taskOf(s, a2.id, D0).id, false, at(D0, 8, 10)));
        check(ev.has(Ev.GOAL), "günlük XP hedefi olayı");
        check(s.day(D0).goalHit, "hedef işaretlendi");
        check(Rewards.canSpin(e, at(D0, 8, 11)), "seri tamam → çark hakkı");
        Ev.List ev2 = new Ev.List();
        int idx = Rewards.spin(e, at(D0, 8, 12), new java.util.Random(5), ev2);
        check(idx >= 0 && ev2.has(Ev.SPIN), "çark ödülü: " + (idx >= 0 ? Rewards.SPIN[idx] : "-"));
        check(!Rewards.canSpin(e, at(D0, 8, 13)), "günde bir çark");
        eq(Rewards.habitLevel(0), 0, "tohum");
        eq(Rewards.habitLevel(5), 1, "filiz");
        eq(Rewards.habitLevel(400), 7, "efsane");
        java.util.Random r = new java.util.Random(1);
        for (int i = 0; i < 400; i++) Rewards.grantCard(s, r, 3);
        check(s.cards.size() >= 30, "kartlar toplanır: " + s.cards.size());
        // Hafta kahramanı: 5 iyi gün sonra yeni hafta
        State h = fresh();
        Engine he = new Engine(h);
        int ws = Tm.weekStart(D0);
        for (int d = ws - 7; d < ws - 2; d++) h.day(d).state = DayRec.FULL;
        h.p.lastClosedDay = ws - 1;
        int chests = h.chests.size();
        he.onNewWeekPublic(ws, at(ws, 8, 0));
        check(h.chests.size() > chests && h.p.heroWeeks == 1, "Hafta Kahramanı sandığı");
        int[] g = Rewards.ghost(s, D0);
        check(g[0] > 0, "kendinle yarış: bu hafta XP");
    }

    static void testFirstDayAndComeback() {
        State s = new State();
        s.p.onboarded = true;
        s.p.wakeMin = 450; s.p.sleepMin = 1410;
        Engine e = new Engine(s);
        long now = at(D0, 15, 0);
        s.p.firstDay = D0;
        Habit a = habit(e, "Sabah", Habit.ANCHOR, 8 * 60, 1, 0, "", 1, now);
        a.createdDay = D0;
        Habit w = habit(e, "Su", Habit.COUNTER, -1, 4000, 2000, "ml", 250, now);
        w.createdDay = D0;
        s.generated.clear(); s.tasks.clear();
        e.onOpen(now);
        check(taskOf(s, a.id, D0) == null, "ilk gün geçmiş saatli iş üretilmez");
        check(taskOf(s, w.id, D0) != null, "ilk gün su üretilir");
        Ev.List ev = e.addProgress(taskOf(s, w.id, D0).id, 250, now);
        check(ev.has(Ev.STREAK_UP), "ilk gün: ilk adım seriyi yakar (verilmiş ilerleme)");
        // 4 gün sonra dönüş
        long later = at(D0 + 5, 9, 0);
        ev = e.onOpen(later);
        check(ev.has(Ev.COMEBACK), "geri dönüş algılandı");
        check(s.p.comebackDay == D0 + 5, "kolay başlangıç günü");
        Task ta = taskOf(s, a.id, D0 + 5);
        ev = e.complete(ta.id, false, at(D0 + 5, 9, 5));
        check(ev.has(Ev.STREAK_UP), "kolay günde çapa yeter");
        check(ta.xpGiven >= 20, "kolay günde 2x XP: " + ta.xpGiven);
    }
}
