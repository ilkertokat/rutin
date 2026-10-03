package com.ilker.rutin.core;

import java.util.ArrayList;

/**
 * Akıllı bildirim motoru.
 * - Saatli işler: hazırlık → zamanı → nazik dürtme (30 dk) → ısrar (15 dk) → kritik (7 dk) kademeleri.
 * - Sayaçlı işler: gün içi beklenen ilerleme eğrisine göre açık büyüdükçe sıklaşır; öndeysen susar.
 * - Süreli işler: kalan iş / kalan zaman baskısına göre.
 * - Bütçe: saatte en fazla N bildirim (kritik hariç), gruplama, sessiz saatler, odak sırasında erteleme.
 * Motor yalnızca "bir sonraki" zamanı hesaplar; alarm tetiklenince durum yeniden değerlendirilir (zincirleme).
 */
public final class NotifEngine {
    public static final int L_PREP = 0, L_ON = 1, L_NUDGE = 2, L_PUSH = 3, L_CRIT = 4;
    public static final long NONE = Long.MAX_VALUE;

    public static final int SYS_MORNING = 1, SYS_EVENING_RISK = 2, SYS_WIND = 3, SYS_SUMMARY = 4, SYS_PLAN = 5, SYS_ROLLOVER = 6;

    public static final class Due {
        public Task t; public int level;
        Due(Task t, int level) { this.t = t; this.level = level; }
    }

    public static final class Plan {
        public final ArrayList<Due> due = new ArrayList<>();
        public final ArrayList<Integer> sys = new ArrayList<>();
        public long nextAt = NONE;
        public boolean deferredByBudget;
        void next(long at) { if (at < nextAt) nextAt = at; }
    }

    final Engine e;
    final State.Profile p;

    public NotifEngine(Engine e) { this.e = e; this.p = e.p; }

    double factor(Task t, int day) {
        double k = 1;
        if (t.priority == Habit.P_CRITICAL) k *= 0.7;
        if (t.priority == Habit.P_LOW) k *= 1.5;
        if (t.graduated) k *= 1.5;
        if (e.isSoft(day)) k *= 1.5;
        for (int i = 0; i < Math.min(t.snoozes, 4); i++) k *= 0.85;
        return k;
    }

    // ---------------- Saatli işler ----------------
    static final class Win { long tA, dA, prep, nudge, push, crit; boolean prepOn; }

    Win win(Task t) {
        Win w = new Win();
        long base = t.day * 1440L;
        int D = e.deadlineDm(t);
        int T = e.targetDm(t);
        if (T < 0) T = Math.max(e.wakeDm() + 60, D - 180);
        if (D <= T) D = T + 30;
        w.tA = base + T; w.dA = base + D;
        w.prep = w.tA - 10;
        w.prepOn = p.prepReminders && t.type == Habit.ANCHOR && !t.graduated && t.targetMin >= 0;
        w.nudge = w.tA + Math.max(5, t.graceMin);
        long span = w.dA - w.tA;
        w.push = Math.max(w.nudge, w.tA + span / 2);
        w.crit = Math.max(w.push, w.tA + span * 8 / 10);
        return w;
    }

    public int levelAt(Task t, long x) {
        if (t.type == Habit.COUNTER) return counterLevel(t, x);
        if (t.type == Habit.DURATION) return durationLevel(t, x);
        Win w = win(t);
        if (x < w.tA) return L_PREP;
        if (x < w.nudge) return L_ON;
        if (x < w.push) return L_NUDGE;
        if (x < w.crit) return L_PUSH;
        if (t.type == Habit.FLEX) return L_NUDGE;
        return t.priority == Habit.P_LOW ? L_PUSH : L_CRIT;
    }

    long timedNext(Task t, long now) {
        Win w = win(t);
        long L = t.lastNotif;
        double k = factor(t, t.day);
        long next;
        if (w.prepOn && L < w.prep && now < w.tA) next = w.prep;
        else if (L < w.tA) next = w.tA;
        else if (t.type == Habit.FLEX) {
            next = L < w.tA + 60 ? w.tA + 60 : NONE;
            if (next != NONE && t.notifCount >= 2) next = NONE;
        } else {
            int lvl = Math.max(levelAt(t, L), L_NUDGE);
            int iv = lvl == L_NUDGE ? 30 : lvl == L_PUSH ? 15 : 7;
            long cand = L < w.nudge ? w.nudge : L + Math.max(5, Math.round(iv * k));
            if (L < w.push && cand > w.push) cand = w.push;
            if (L < w.crit && cand > w.crit) cand = w.crit;
            if (cand >= w.dA - 10) cand = L < w.dA - 10 ? w.dA - 10 : NONE;
            next = cand;
        }
        if (t.snoozeUntil > L) next = t.snoozeUntil;
        if (next != NONE && next > w.dA) next = NONE;
        return next;
    }

    // ---------------- Sayaçlı işler ----------------
    long[] counterWindow(Task t) {
        long base = t.day * 1440L;
        long start = base + e.wakeDm();
        long end = base + e.sleepDm() - 60;
        if (t.deadlineMin >= 0) end = Math.min(end, base + e.deadlineDm(t));
        if (end <= start + 60) end = start + 60;
        return new long[]{start, end};
    }

    public double expected(Task t, long x) {
        long[] w = counterWindow(t);
        double f = (x - w[0]) / (double) (w[1] - w[0]);
        f = Math.max(0, Math.min(1, f));
        return t.full * f;
    }

    public double gapRatio(Task t, long x) {
        return Math.max(0, (expected(t, x) - t.progress) / Math.max(1.0, t.full));
    }

    int counterLevel(Task t, long x) {
        double r = gapRatio(t, x);
        int l = r < 0.08 ? L_ON : r < 0.2 ? L_NUDGE : r < 0.35 ? L_PUSH : L_CRIT;
        if (l == L_CRIT && t.priority != Habit.P_CRITICAL) l = L_PUSH;
        return l;
    }

    long counterNext(Task t, long now) {
        if (t.progress >= t.full) return NONE;
        long[] w = counterWindow(t);
        long start = w[0], end = w[1];
        long L = t.lastNotif;
        double k = factor(t, t.day);
        if (L < start && t.progress == 0 && now < start + 30) return start + 30;
        double stepFrac = Math.max(1, t.step) * 0.5;
        long behindAt = start + (long) Math.ceil(((t.progress + stepFrac) / (double) t.full) * (end - start));
        long anchor = Math.max(Math.max(L, t.lastProgressAt), start);
        long cand = Math.max(behindAt, anchor + 10);
        for (int i = 0; i < 4; i++) {
            double r = gapRatio(t, cand);
            long iv = Math.round(Math.max(10, Math.min(90, 90 * (1 - 2.5 * r))) * k);
            cand = Math.max(behindAt, anchor + Math.max(8, iv));
        }
        if (t.snoozeUntil > L) cand = Math.max(cand, t.snoozeUntil);
        return cand > end ? NONE : cand;
    }

    // ---------------- Süreli işler ----------------
    public double pressure(Task t, long x) {
        long endW = t.day * 1440L + e.sleepDm() - 30;
        int rem = Math.max(0, t.full - t.progress);
        return rem / (double) Math.max(1, endW - x);
    }

    int durationLevel(Task t, long x) {
        double pr = pressure(t, x);
        if (pr < 0.6) return L_NUDGE;
        if (pr < 0.9) return L_PUSH;
        return t.priority == Habit.P_CRITICAL ? L_CRIT : L_PUSH;
    }

    long durationNext(Task t, long now) {
        int rem = t.full - t.progress;
        if (rem <= 0) return NONE;
        long base = t.day * 1440L;
        long endW = base + e.sleepDm() - 30;
        long L = t.lastNotif;
        double k = factor(t, t.day);
        long x03 = endW - (long) Math.ceil(rem / 0.3);
        long anchor = Math.max(L, t.lastProgressAt);
        long cand = Math.max(x03, anchor + 20);
        for (int i = 0; i < 4; i++) {
            double pr = pressure(t, cand);
            long iv = pr < 0.3 ? 60 : pr < 0.6 ? 60 : pr < 0.9 ? 30 : 20;
            cand = Math.max(x03, anchor + Math.round(iv * k));
        }
        int T = e.targetDm(t);
        if (T >= 0 && L < base + T) cand = Math.min(cand, base + T);
        for (int b : t.blocks) {
            long bA = base + e.dmOf(b);
            if (bA > L && bA >= now - 5 && bA < cand) cand = bA;
        }
        if (t.snoozeUntil > L) cand = Math.max(cand, t.snoozeUntil);
        return cand >= endW ? NONE : cand;
    }

    // ---------------- Genel ----------------
    public long nextFor(Task t, long now) {
        if (!t.isOpen()) return NONE;
        if (t.day != e.today(now)) return NONE;
        if (e.isVacation(t.day)) return NONE;
        long next;
        switch (t.type) {
            case Habit.COUNTER: next = counterNext(t, now); break;
            case Habit.DURATION: next = durationNext(t, now); break;
            default: next = timedNext(t, now);
        }
        if (next == NONE) return NONE;
        long base = t.day * 1440L;
        long wakeA = base + e.wakeDm(), sleepA = base + e.sleepDm();
        if (next < wakeA) next = wakeA + (t.type == Habit.COUNTER ? 30 : 0);
        if (next >= sleepA) return NONE;
        return next;
    }

    public int sentLastHour(long now) {
        int n = 0;
        for (long v : p.sentTimes) if (v > now - 60) n++;
        return n;
    }

    /** Şu an gösterilmesi gerekenleri ve bir sonraki uyanma zamanını hesaplar. */
    public Plan plan(long now, boolean focusActive, long focusEndMin) {
        Plan pl = new Plan();
        int td = e.today(now);
        long base = td * 1440L;
        long wakeA = base + e.wakeDm(), sleepA = base + e.sleepDm();
        // Gün dönümü
        long rollA = (td + 1) * 1440L + e.ro();
        pl.next(rollA + 1);
        boolean shield = p.shieldUntil > now;
        ArrayList<Due> cands = new ArrayList<>();
        for (Task t : e.s.tasksOf(td)) {
            long n = nextFor(t, now);
            if (n == NONE) continue;
            if (n <= now + 3) cands.add(new Due(t, levelAt(t, now)));
            else pl.next(n);
        }
        if (shield) {
            if (!cands.isEmpty()) pl.next(p.shieldUntil);
            cands.clear();
        }
        if (focusActive) {
            ArrayList<Due> keep = new ArrayList<>();
            for (Due d : cands) if (d.level >= L_CRIT) keep.add(d); else pl.next(Math.max(now + 5, focusEndMin + 1));
            cands = keep;
        }
        if (!cands.isEmpty()) {
            int used = sentLastHour(now);
            boolean anyCrit = false;
            for (Due d : cands) if (d.level >= L_CRIT) anyCrit = true;
            if (used >= p.budget && !anyCrit) {
                long oldest = NONE;
                for (long v : p.sentTimes) if (v > now - 60 && v < oldest) oldest = v;
                pl.next(oldest == NONE ? now + 10 : oldest + 61);
                pl.deferredByBudget = true;
            } else {
                // En acil olan başa
                cands.sort((a, b) -> Integer.compare(b.level, a.level));
                pl.due.addAll(cands);
            }
        }
        // Sistem olayları
        if (!e.isVacation(td) && p.onboarded) {
            sys(pl, now, SYS_MORNING, wakeA, p.lastMorningDay != td, wakeA + 180);
            DayRec r = e.s.dayOrNull(td);
            boolean met = r != null && r.met;
            boolean risk = !met && (p.streak > 0) && !e.required(td).isEmpty();
            sys(pl, now, SYS_EVENING_RISK, sleepA - 150, risk && p.lastEveningWarnDay != td, sleepA - 60);
            sys(pl, now, SYS_WIND, sleepA - 60, p.lastWindDay != td, sleepA - 20);
            sys(pl, now, SYS_SUMMARY, sleepA - 15, p.lastSummaryDay != td, sleepA + 60);
            if (Tm.weekday(td) == 6) sys(pl, now, SYS_PLAN, sleepA - 100, p.lastPlanNotifWeek != Tm.weekStart(td + 1) && p.lastPlanWeek != Tm.weekStart(td + 1), sleepA - 30);
        }
        if (pl.nextAt <= now) pl.nextAt = now + 1;
        return pl;
    }

    void sys(Plan pl, long now, int kind, long at, boolean pending, long expire) {
        if (!pending) return;
        if (at <= now + 1) { if (now <= expire) pl.sys.add(kind); }
        else pl.next(at);
    }

    /** Bildirim gönderildikten sonra kayıt. */
    public void recordSent(ArrayList<Due> due, long now) {
        for (Due d : due) {
            d.t.lastNotif = now;
            d.t.notifCount++;
            e.s.day(d.t.day).notifs++;
        }
        if (!due.isEmpty()) {
            p.sentTimes.add(now);
            int hr = Tm.norm((int) (now % 1440)) / 60;
            p.hourSent[hr]++;
        }
        while (p.sentTimes.size() > 20) p.sentTimes.remove(0);
    }

    public void recordSys(int kind, long now) {
        int td = e.today(now);
        switch (kind) {
            case SYS_MORNING: p.lastMorningDay = td; break;
            case SYS_EVENING_RISK: p.lastEveningWarnDay = td; break;
            case SYS_WIND: p.lastWindDay = td; break;
            case SYS_SUMMARY: p.lastSummaryDay = td; break;
            case SYS_PLAN: p.lastPlanNotifWeek = Tm.weekStart(td + 1); break;
        }
    }

    public void recordActed(long now) {
        int hr = Tm.norm((int) (now % 1440)) / 60;
        p.hourActed[hr]++;
    }
}
