package com.ilker.rutin.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

/**
 * Uygulamanın saf iş mantığı: görev üretimi, tamamlama, XP, seri, gün kapanışı, görevler, odak.
 * Android'e bağımlı değildir; "now" her zaman yerel epoch dakikası olarak dışarıdan verilir.
 */
public final class Engine {
    public final State s;
    public final State.Profile p;

    public Engine(State s) { this.s = s; this.p = s.p; }

    // ---------------- Zaman ----------------
    public int ro() { return Tm.rollover(p.wakeMin, p.sleepMin); }
    public int today(long now) { return Tm.day(now, ro()); }
    public int nowDm(long now) { return Tm.dm(now, ro()); }
    public int wakeDm() { return Tm.toDm(p.wakeMin, ro()); }
    public int sleepDm() { return Tm.toDm(p.sleepMin, ro()); }
    public int dmOf(int calMin) { return calMin < 0 ? -1 : Tm.toDm(calMin, ro()); }
    public long absOf(int day, int dm) { return Tm.abs(day, dm); }

    public int targetDm(Task t) { return dmOf(t.targetMin); }

    public int deadlineDm(Task t) {
        int sl = sleepDm();
        int tg = targetDm(t);
        int d;
        if (t.deadlineMin >= 0) d = dmOf(t.deadlineMin);
        else {
            switch (t.type) {
                case Habit.ANCHOR: d = tg >= 0 ? Math.min(tg + 120, sl - 10) : sl - 30; break;
                case Habit.FLEX: d = tg >= 0 ? tg + 90 : sl - 30; break;
                default: d = sl - 30;
            }
        }
        if (tg >= 0 && d <= tg) d = tg + 30;
        return d;
    }

    public boolean isRestDay(int day) { return (p.restMask & (1 << Tm.weekday(day))) != 0; }
    public boolean isVacation(int day) { return p.vacationUntil >= 0 && day <= p.vacationUntil && day >= p.vacationUntil - 60; }
    public boolean isSick(int day) { return p.sickUntil >= 0 && day <= p.sickUntil && day >= p.sickUntil - 30; }
    public boolean isSoft(int day) { return p.softUntil >= 0 && day <= p.softUntil; }

    // ---------------- Görev üretimi ----------------
    public void ensureDay(int day, long now) {
        if (s.generated.contains(day)) return;
        s.generated.add(day);
        syncDay(day, now);
    }

    /** Alışkanlıklardan günün görevlerini üretir / günceller (kullanıcının elle değiştirdiği kopyalar korunur). */
    public void syncDay(int day, long now) {
        boolean vacation = isVacation(day);
        boolean rest = isRestDay(day);
        boolean sick = isSick(day);
        int nd = nowDm(now), td = today(now);
        for (Habit h : s.habits) {
            Task ex = null;
            for (Task t : s.tasks) if (t.day == day && t.habitId == h.id && t.fromDay < 0) { ex = t; break; }
            boolean sched = h.scheduledOn(day) && !vacation && (!rest || h.type == Habit.ANCHOR || h.type == Habit.COUNTER);
            if (sched && ex == null) {
                // İlk gün: saati geçmiş işleri üretme
                if (day == p.firstDay && day == td) {
                    int tg = dmOf(h.targetMin);
                    int dl = h.deadlineMin >= 0 ? dmOf(h.deadlineMin) : -1;
                    if ((dl >= 0 && dl < nd + 30) || (tg >= 0 && tg < nd - 30 && h.type != Habit.COUNTER && h.type != Habit.DURATION)) continue;
                }
                Task t = new Task();
                t.id = s.nextId();
                t.habitId = h.id;
                t.day = day;
                copy(h, t);
                if (rest && h.type == Habit.COUNTER) t.streak = false;
                if (sick) applySick(t);
                s.tasks.add(t);
            } else if (sched && ex.status == Task.PENDING && !ex.custom) {
                int keepFull = ex.origFull > 0 ? ex.full : -1;
                copy(h, ex);
                if (keepFull > 0) ex.full = keepFull;
                if (rest && h.type == Habit.COUNTER) ex.streak = false;
            } else if (!sched && ex != null && ex.status == Task.PENDING && ex.progress == 0 && !ex.custom) {
                s.tasks.remove(ex);
            }
        }
    }

    void applySick(Task t) {
        // Hastalık modu: hedefler iyileşme versiyonuna düşer
        if (t.isQuantity() && t.mini > 0) { t.origFull = t.full; t.full = t.mini; }
        t.miniOk = true;
    }

    static void copy(Habit h, Task t) {
        t.title = h.title; t.emoji = h.emoji; t.type = h.type; t.area = h.area;
        t.targetMin = h.targetMin; t.deadlineMin = h.deadlineMin; t.graceMin = h.graceMin;
        t.full = h.full; t.mini = h.mini; t.step = Math.max(1, h.step); t.unit = h.unit; t.miniText = h.miniText;
        t.priority = h.priority; t.streak = h.streak; t.graduated = h.graduated;
    }

    /** Alışkanlık değişince bugün ve ilerideki üretilmiş günleri yeniden eşitler. */
    public void resyncFrom(int day, long now) {
        ArrayList<Integer> ds = new ArrayList<>(s.generated);
        for (int d : ds) if (d >= day) syncDay(d, now);
    }

    public ArrayList<Task> todayTasks(long now) {
        int d = today(now);
        ensureDay(d, now);
        return s.tasksOf(d);
    }

    // ---------------- Tamamlama ----------------
    public Ev.List complete(long taskId, boolean mini, long now) {
        Ev.List ev = new Ev.List();
        Task t = s.task(taskId);
        if (t == null || !t.isOpen()) return ev;
        if (t.isQuantity()) {
            int target = mini ? Math.max(t.mini, 1) : t.full;
            if (t.progress < target) {
                int add = target - t.progress;
                if ("ml".equals(t.unit)) p.waterTotal += add;
                if (t.type == Habit.COUNTER && "ml".equals(t.unit) && nowDm(now) < 720) {
                    DayRec r = s.day(t.day); r.waterNoon = Math.max(r.waterNoon, target);
                }
                t.progress = target;
            }
            t.lastProgressAt = now;
            if (mini && t.progress < t.full) {
                // mini tamam: görev açık kalır ama seri güvende
                refreshDay(t.day, now, ev);
                ev.add(Ev.MINI_DONE, 0, t.title);
                return ev;
            }
        }
        markDone(t, mini, now, ev);
        return ev;
    }

    public Ev.List addProgress(long taskId, int amount, long now) {
        Ev.List ev = new Ev.List();
        Task t = s.task(taskId);
        if (t == null) return ev;
        int before = t.progress;
        t.progress = Math.max(0, t.progress + amount);
        int delta = t.progress - before;
        if ("ml".equals(t.unit)) p.waterTotal = Math.max(0, p.waterTotal + delta);
        if (delta > 0) t.lastProgressAt = now;
        DayRec r = s.day(t.day);
        if (t.type == Habit.COUNTER && "ml".equals(t.unit) && nowDm(now) < 720) r.waterNoon = Math.max(r.waterNoon, t.progress);
        if (t.status == Task.DONE && t.progress < t.full) {
            revert(t);
        }
        if (t.isOpen() && t.progress >= t.full) {
            markDone(t, false, now, ev);
            ev.add(Ev.COUNTER_FULL, t.full, t.title);
        } else {
            if (delta > 0 && before * 2 < t.full && t.progress * 2 >= t.full) ev.add(Ev.HALFWAY, t.progress, t.title);
            if (delta > 0 && t.mini > 0 && before < t.mini && t.progress >= t.mini && t.progress < t.full) ev.add(Ev.MINI_DONE, 0, t.title);
            ev.add(Ev.PROGRESS, t.progress, t.title);
            refreshDay(t.day, now, ev);
            updateQuests(t.day, now, ev);
        }
        if (delta > 0) touch(now);
        return ev;
    }

    void revert(Task t) {
        t.status = Task.PENDING;
        p.xp = Math.max(0, p.xp - t.xpGiven);
        p.coins = Math.max(0, p.coins - t.coinsGiven);
        s.day(t.day).xp -= t.xpGiven;
        t.xpGiven = 0; t.coinsGiven = 0;
        p.totalDone = Math.max(0, p.totalDone - 1);
        if (t.early) p.totalEarly = Math.max(0, p.totalEarly - 1);
        Habit h = s.habit(t.habitId);
        if (h != null) h.totalDone = Math.max(0, h.totalDone - 1);
        t.early = false;
    }

    /** Tamamlanmış görevi geri al. */
    public Ev.List undo(long taskId, long now) {
        Ev.List ev = new Ev.List();
        Task t = s.task(taskId);
        if (t == null) return ev;
        if (t.isComplete()) {
            revert(t);
            if (t.type == Habit.COUNTER) t.progress = Math.max(0, t.full - t.step);
            if (t.type == Habit.FLEX) p.flexDone = Math.max(0, p.flexDone - 1);
        } else if (t.status == Task.SKIPPED) t.status = Task.PENDING;
        refreshDay(t.day, now, ev);
        updateQuests(t.day, now, ev);
        return ev;
    }

    int baseXp(Task t, boolean mini) {
        if (mini) return 5;
        switch (t.type) {
            case Habit.ANCHOR: return t.onTime ? 15 : 10;
            case Habit.COUNTER: return 15;
            case Habit.DURATION: return 20;
            case Habit.FLEX: return 5;
            default: return 10;
        }
    }

    void markDone(Task t, boolean mini, long now, Ev.List ev) {
        int nd = nowDm(now);
        t.status = mini ? Task.MINI : Task.DONE;
        t.doneAt = now;
        int tg = targetDm(t);
        boolean timed = t.type == Habit.ANCHOR || t.type == Habit.REQUIRED || t.type == Habit.ONCE || t.type == Habit.FLEX;
        t.early = timed && !mini && tg >= 0 && t.day == today(now) && nd < tg - 5;
        t.onTime = tg < 0 || nd <= tg + t.graceMin;
        // Kombo
        if (p.lastCompleteAt > 0 && now - p.lastCompleteAt <= 60) p.combo++; else p.combo = 1;
        p.lastCompleteAt = now;
        p.bestCombo = Math.max(p.bestCombo, p.combo);
        DayRec r = s.day(t.day);
        r.combo = Math.max(r.combo, p.combo);
        // XP
        double mult = 1;
        if (t.early) mult *= 1.2;
        if (t.area == Area.monthArea(Tm.month(t.day))) mult *= 1.25;
        if (p.comebackDay == t.day) mult *= 2;
        if (p.boostUntil > now) mult *= 2;
        if (p.combo >= 3) mult *= 1.5;
        int xp = (int) Math.round(baseXp(t, mini) * mult);
        int coins = Math.max(1, Math.round(xp / 3f));
        t.xpGiven = xp; t.coinsGiven = coins;
        p.coins += coins;
        gainXp(xp, t.title, t.day, now, ev);
        // İstatistik
        p.totalDone++;
        if (t.early) { p.totalEarly++; r.early++; ev.add(Ev.EARLY, 0, t.title); }
        if (t.type == Habit.FLEX) p.flexDone++;
        Habit h = s.habit(t.habitId);
        if (h != null) {
            int lvBefore = Rewards.habitLevel(h.totalDone);
            h.totalDone++;
            int lvAfter = Rewards.habitLevel(h.totalDone);
            if (lvAfter > lvBefore) ev.add(Ev.HABIT_LEVEL, lvAfter, h.emoji + " " + h.title + " → " + Rewards.HABIT_LV_EMOJI[lvAfter] + " " + Rewards.HABIT_LV_NAME[lvAfter]);
            if (mini) h.totalMini++;
            h.lastDoneDay = t.day;
            h.doneMinutes.add(nd);
            while (h.doneMinutes.size() > 14) h.doneMinutes.remove(0);
        }
        ev.add(mini ? Ev.MINI_DONE : Ev.TASK_DONE, xp, t.title);
        if (p.combo >= 3) ev.add(Ev.COMBO, p.combo, "Kombo x" + p.combo);
        touch(now);
        refreshDay(t.day, now, ev);
        updateQuests(t.day, now, ev);
        Badges.check(s, t.day, ev);
    }

    public void gainXp(int xp, String reason, int day, long now, Ev.List ev) {
        int before = Game.level(p.xp);
        p.xp += xp;
        DayRec dr = s.day(day);
        dr.xp += xp;
        ev.add(Ev.XP, xp, reason);
        if (!dr.goalHit && dr.xp >= Math.max(10, p.dailyGoal) && day == today(now)) {
            dr.goalHit = true;
            p.goalDays++;
            p.coins += 20;
            ev.add(Ev.GOAL, p.dailyGoal, "Günlük hedef tamam: " + p.dailyGoal + " XP");
            addChest(0, day, "Günlük XP hedefi", ev);
        }
        int after = Game.level(p.xp);
        if (after > before) {
            ev.add(Ev.LEVEL_UP, after, Game.title(after));
            for (int lv = before + 1; lv <= after; lv++) addChest(lv % 5 == 0 ? 2 : 1, day, "Seviye " + lv, ev);
            Badges.check(s, day, ev);
        }
    }

    public void addChest(int minRarity, int day, String reason, Ev.List ev) {
        State.Chest c = new State.Chest();
        c.rarity = Game.rollRarity(new Random(p.nextId * 31 + day + s.chests.size()), minRarity);
        c.day = day; c.reason = reason;
        s.chests.add(c);
        if (ev != null) ev.add(Ev.CHEST, c.rarity, reason);
    }

    public void touch(long now) {
        p.lastActiveDay = today(now);
    }

    // ---------------- Gün değerlendirme ----------------
    public boolean satisfied(Task t) {
        if (t.isComplete()) return true;
        return t.isQuantity() && t.mini > 0 && t.progress >= t.mini;
    }

    /** Günün seriyi uzatan gereksinimleri. */
    public ArrayList<Task> required(int day) {
        ArrayList<Task> r = new ArrayList<>();
        boolean easy = p.comebackDay == day;
        for (Task t : s.tasksOf(day)) {
            if (!t.streak) continue;
            if (easy && t.type != Habit.ANCHOR) continue;
            r.add(t);
        }
        return r;
    }

    public boolean computeMet(int day) {
        ArrayList<Task> all = s.tasksOf(day);
        int doneAny = 0, touched = 0;
        for (Task t : all) { if (satisfied(t)) doneAny++; if (t.isComplete() || t.progress > 0) touched++; }
        if (day == p.firstDay) return touched > 0;
        ArrayList<Task> req = required(day);
        if (req.isEmpty()) return doneAny > 0;
        for (Task t : req) if (!satisfied(t)) return false;
        return true;
    }

    public boolean computePerfect(int day) {
        ArrayList<Task> req = new ArrayList<>();
        for (Task t : s.tasksOf(day)) if (t.streak) req.add(t);
        if (req.size() < 2) return false;
        for (Task t : req) {
            if (t.status != Task.DONE) return false;
            if (t.isQuantity() && t.progress < t.full) return false;
        }
        return true;
    }

    /** Günün canlı özetini günceller; seri/mükemmel gün geçişlerinde olay üretir. */
    public void refreshDay(int day, long now, Ev.List ev) {
        DayRec r = s.day(day);
        ArrayList<Task> all = s.tasksOf(day);
        ArrayList<Task> req = required(day);
        r.total = req.size();
        r.done = 0; r.mini = 0;
        double sum = 0;
        for (Task t : req) {
            if (t.status == Task.DONE) r.done++;
            else if (satisfied(t)) r.mini++;
            sum += satisfied(t) && !t.isQuantity() ? 1 : t.isQuantity() ? Math.min(1, t.progress / (double) Math.max(1, t.full)) : 0;
        }
        r.ratio = req.isEmpty() ? (all.isEmpty() ? 0 : 1) : sum / req.size();
        boolean met = computeMet(day);
        boolean closed = day <= p.lastClosedDay;
        if (!closed) {
            if (met && !r.met) {
                r.met = true;
                int ns = p.streak + 1;
                if (ev != null) {
                    ev.add(Ev.STREAK_UP, ns, "Seri " + ns + " gün!");
                    if (Game.isMilestone(ns)) {
                        ev.add(Ev.MILESTONE, ns, ns + " günlük seri!");
                        if (ns >= 7 && p.freezes < Game.MAX_FREEZES) { p.freezes++; ev.add(Ev.FREEZE_EARNED, p.freezes, "Kilometre taşı ödülü: 🧊 +1 Seri Dondurma"); }
                    }
                }
                if (ns > p.bestStreak) p.bestStreak = ns;
            } else if (!met && r.met) {
                r.met = false;
            }
            boolean perf = computePerfect(day);
            if (perf && !r.perfect) {
                r.perfect = true;
                p.perfectDays++;
                if (ev != null) {
                    ev.add(Ev.PERFECT, 0, "Mükemmel Gün!");
                    gainXp(50, "Mükemmel Gün", day, now, ev);
                    addChest(1, day, "Mükemmel Gün", ev);
                }
            }
        }
    }

    public int displayStreak(long now) {
        DayRec r = s.dayOrNull(today(now));
        return p.streak + (r != null && r.met ? 1 : 0);
    }

    public boolean todayMet(long now) {
        DayRec r = s.dayOrNull(today(now));
        return r != null && r.met;
    }

    // ---------------- Gün kapanışı ----------------
    /** Açılışta / alarmda çağrılır: geçmiş kapanmamış günleri kapatır, yeni günü hazırlar. */
    public Ev.List tick(long now) {
        Ev.List ev = new Ev.List();
        int td = today(now);
        if (p.firstDay < 0) p.firstDay = td;
        if (p.lastClosedDay == Integer.MIN_VALUE) p.lastClosedDay = td - 1;
        int from = Math.max(p.lastClosedDay + 1, td - 60);
        for (int d = from; d < td; d++) {
            int ws = Tm.weekStart(d + 1);
            closeDay(d, now, ev);
            if (Tm.weekday(d + 1) == 0) onNewWeek(ws, now, ev);
        }
        if (p.lastClosedDay < td - 1) p.lastClosedDay = td - 1;
        ensureDay(td, now);
        // Yarını da hazırla (hafta görünümü ve gece planlama için)
        if (p.weeklyWeek < 0 || p.weeklyWeek != Tm.weekStart(td)) Quests.generateWeekly(s, Tm.weekStart(td));
        if (p.questDay != td) Quests.generate(s, td);
        if (p.freezeWeek != Tm.weekStart(td)) {
            p.freezeWeek = Tm.weekStart(td);
            if (p.freezes < 1) p.freezes = 1;
        }
        refreshDay(td, now, ev);
        prune(td);
        return ev;
    }

    public void closeDay(int d, long now, Ev.List ev) {
        if (d <= p.lastClosedDay) return;
        final int evStart = ev.size();
        if (!s.generated.contains(d) && d >= p.firstDay) {
            // Uygulama o gün hiç açılmadıysa görevleri yine de üret (kaçırılanlar sayılsın)
            s.generated.add(d);
            syncDay(d, absOf(d, 0) + 0);
        }
        ArrayList<Task> all = s.tasksOf(d);
        for (Task t : all) {
            if (t.status != Task.PENDING) continue;
            if (t.type == Habit.FLEX) { t.status = Task.SKIPPED; continue; }
            if (t.isQuantity() && t.mini > 0 && t.progress >= t.mini) t.status = Task.MINI;
            else if (t.isQuantity() && t.progress >= t.full) t.status = Task.DONE;
            else t.status = Task.MISSED;
        }
        refreshDay(d, now, null);
        DayRec r = s.day(d);
        boolean met = computeMet(d);
        boolean anyReq = !required(d).isEmpty();
        int doneAny = 0;
        for (Task t : all) if (t.isComplete()) doneAny++;
        boolean free = isVacation(d) || isSick(d) || p.shieldDay == d && p.shieldType >= 0 && p.shieldType <= 1;
        int prevStreak = p.streak;
        if (met) {
            boolean allFull = r.mini == 0;
            r.state = allFull ? DayRec.FULL : DayRec.MINI;
            p.streak++;
            if (p.streak > p.bestStreak) p.bestStreak = p.streak;
            if (r.disrupted) p.stormSaves++;
            // ada
            int goodIdx = 0;
            for (DayRec x : s.days) if (x.good() && x.day != d) goodIdx++;
            s.island.add(Game.islandForDay(goodIdx, r.perfect, p.firstDay));
            // onarma görevi
            if (p.repairUntil >= d) {
                p.repairProgress++;
                if (p.repairProgress >= 2) {
                    p.streak = p.repairOld + p.streak;
                    if (p.streak > p.bestStreak) p.bestStreak = p.streak;
                    p.repairs++;
                    p.repairUntil = -1;
                    ev.add(Ev.REPAIRED, p.streak, "Seri onarıldı: " + p.streak + " gün!");
                    notice(Ev.REPAIRED, p.streak, "Serin geri geldi: " + p.streak + " gün! 🦅");
                }
            }
        } else if (!anyReq && doneAny == 0 && !isRestDayBroken(d)) {
            r.state = DayRec.NEUTRAL;
        } else if (free || (!anyReq && isVacation(d))) {
            r.state = DayRec.FROZEN; r.frozenFree = true;
            ev.add(Ev.FROZEN, p.streak, "Seri korundu");
        } else if (p.freezes > 0) {
            p.freezes--;
            r.state = DayRec.FROZEN;
            ev.add(Ev.FROZEN, p.streak, "Seri dondurma kullanıldı");
            notice(Ev.FROZEN, p.streak, "Dün tamamlanamadı ama 🧊 Seri Dondurma serini korudu. Kalan: " + p.freezes);
        } else {
            r.state = DayRec.BROKEN;
            if (p.repairUntil >= d) p.repairUntil = -1;
            if (prevStreak >= 3) {
                p.repairOld = prevStreak;
                p.repairUntil = d + 2;
                p.repairProgress = 0;
                ev.add(Ev.REPAIR_OPEN, prevStreak, "Seri onarma açıldı");
                notice(Ev.REPAIR_OPEN, prevStreak, prevStreak + " günlük serin kırıldı. Ama 48 saatin var: önümüzdeki 2 günü tamamla, serin geri gelsin! 🦅");
            } else if (prevStreak > 0) {
                notice(Ev.STREAK_BROKEN, prevStreak, "Olur böyle. Asıl önemli olan iki kez kaçırmamak. Bugün mini versiyonlarla dönelim mi? 💪");
            }
            ev.add(Ev.STREAK_BROKEN, prevStreak, "Seri kırıldı");
            p.streak = 0;
        }
        r.met = met;
        // Uyku serisi
        if (r.sleepOk) { p.sleepStreak++; p.bestSleepStreak = Math.max(p.bestSleepStreak, p.sleepStreak); }
        else if (r.state != DayRec.FROZEN) p.sleepStreak = 0;
        // Alışkanlık serileri
        for (Task t : all) {
            Habit h = s.habit(t.habitId);
            if (h == null || t.fromDay >= 0) continue;
            if (satisfied(t)) { h.curStreak++; h.bestStreak = Math.max(h.bestStreak, h.curStreak); }
            else if (r.state != DayRec.FROZEN && t.streak) h.curStreak = 0;
        }
        // Dakiklik
        boolean hasTimed = false, allOnTime = true;
        for (Task t : all) {
            if (t.targetMin >= 0 && t.streak && t.type != Habit.FLEX) {
                hasTimed = true;
                if (!(t.isComplete() && t.onTime)) allOnTime = false;
            }
        }
        if (hasTimed) {
            if (allOnTime) { p.punctualRun++; p.bestPunctual = Math.max(p.bestPunctual, p.punctualRun); }
            else p.punctualRun = 0;
        }
        // Hafta sonu, denge, sessiz güç, gece yarısı, sabah insanı
        if (Tm.weekday(d) == 6 && r.perfect) {
            DayRec sat = s.dayOrNull(d - 1);
            if (sat != null && sat.perfect) p.weekendPerfect++;
        }
        boolean[] areas = new boolean[6]; int ac = 0, morning = 0; boolean late = false;
        for (Task t : all) if (t.isComplete()) {
            if (t.area >= 0 && t.area < 6 && !areas[t.area]) { areas[t.area] = true; ac++; }
            int dm = Tm.dm(t.doneAt, ro());
            if (dm < 540) morning++;
            if (dm >= 1440 && t.streak) late = true;
        }
        if (ac >= 4) p.balanceDays++;
        if (morning >= 3) p.morningTriple++;
        if (met && late) p.lateSaves++;
        if (met && r.notifs == 0 && r.total >= 2) p.silentDays++;
        // Mezuniyet ve içgörüler
        checkGraduation(d, ev);
        Insights.onDayClosed(this, d);
        p.lastClosedDay = d;
        Badges.check(s, d, ev);
        for (int i = evStart; i < ev.size(); i++) { Ev e = ev.get(i); if (e.type == Ev.BADGE || e.type == Ev.GRADUATED) notice(e.type, e.value, e.text); }
    }

    boolean isRestDayBroken(int d) { return false; }

    void checkGraduation(int d, Ev.List ev) {
        for (Habit h : s.habits) {
            if (h.graduated || h.archived || h.type == Habit.ONCE || h.type == Habit.FLEX) continue;
            if (d - h.createdDay < 65) continue;
            int tot = 0, ok = 0;
            for (Task t : s.tasks) if (t.habitId == h.id && t.day > d - 66 && t.day <= d && t.fromDay < 0 && t.counts()) {
                tot++; if (satisfied(t)) ok++;
            }
            if (tot >= 20 && ok >= tot * 0.9) {
                h.graduated = true; h.graduatedDay = d; p.graduates++;
                ev.add(Ev.GRADUATED, 0, h.emoji + " " + h.title + " artık otomatik!");
            }
        }
    }

    public void onNewWeekPublic(int weekStart, long now) { onNewWeek(weekStart, now, new Ev.List()); }

    void onNewWeek(int weekStart, long now, Ev.List ev) {
        if (p.autoTemplate && p.activeTemplate != 0) {
            State.Template t = template(p.activeTemplate);
            if (t != null) applyTemplate(t, weekStart, now);
        }
        Insights.onNewWeek(this, weekStart);
        int good = 0;
        for (int d = weekStart - 7; d < weekStart; d++) { DayRec x = s.dayOrNull(d); if (x != null && x.good()) good++; }
        if (good >= 5) {
            p.heroWeeks++;
            addChest(good == 7 ? 3 : 2, weekStart, "Hafta Kahramanı", ev);
            notice(Ev.HERO_WEEK, good, "🦸 Hafta Kahramanı! Geçen hafta " + good + "/7 iyi gün yaşadın. " + (good == 7 ? "Efsanevi" : "Epik") + " sandık seni bekliyor!");
        }
        Quests.generateWeekly(s, weekStart);
    }

    public void notice(int type, int value, String text) {
        s.notices.add(type + "|" + value + "|" + text.replace('|', '/'));
        while (s.notices.size() > 12) s.notices.remove(0);
    }

    void prune(int td) {
        // 150 günden eski görev ayrıntılarını sil (gün özetleri kalır)
        Iterator<Task> it = s.tasks.iterator();
        while (it.hasNext()) if (it.next().day < td - 150) it.remove();
        Iterator<Integer> gi = s.generated.iterator();
        while (gi.hasNext()) if (gi.next() < td - 150) gi.remove();
        while (s.focus.size() > 3000) s.focus.remove(0);
    }

    // ---------------- Görevler ----------------
    public void updateQuests(int day, long now, Ev.List ev) {
        if (day != today(now)) return;
        ArrayList<Task> ts = s.tasksOf(day);
        DayRec r = s.day(day);
        int completed = 0, early = 0, morning = 0, flex = 0, anchorsOk = 0;
        boolean water20 = false;
        for (Task t : ts) {
            if (!t.isComplete()) continue;
            int dm = Tm.dm(t.doneAt, ro());
            if (t.type != Habit.FLEX) completed++;
            if (t.early) early++;
            if (dm < 600) morning++;
            if (t.type == Habit.FLEX) flex++;
            if (t.type == Habit.ANCHOR && t.onTime) anchorsOk++;
            if (t.type == Habit.COUNTER && "ml".equals(t.unit) && t.status == Task.DONE && dm < 1200) water20 = true;
        }
        int focusN = 0, focusM = 0;
        for (State.FocusLog f : s.focus) if (f.day == day) { if (f.completed) focusN++; focusM += f.minutes; }
        int doneCount = 0;
        for (State.Quest q : s.quests) {
            if (q.day != day) continue;
            int v = 0;
            switch (q.kind) {
                case Quests.Q_DONE5: v = completed; break;
                case Quests.Q_EARLY: v = early; break;
                case Quests.Q_COMBO: v = r.combo; break;
                case Quests.Q_FOCUS2: v = focusN; break;
                case Quests.Q_FOCUS45: v = focusM; break;
                case Quests.Q_WATER_NOON: v = r.waterNoon; break;
                case Quests.Q_WATER_20: v = water20 ? 1 : 0; break;
                case Quests.Q_MORNING: v = morning; break;
                case Quests.Q_FLEX: v = flex; break;
                case Quests.Q_ANCHORS: v = anchorsOk; break;
                case Quests.Q_REFLECT: v = r.mood > 0 ? 1 : 0; break;
                case Quests.Q_START: v = r.started ? 1 : 0; break;
            }
            q.progress = Math.min(v, q.target);
            if (!q.done && v >= q.target) {
                q.done = true;
                p.questsDone++;
                p.coins += 10;
                if (ev != null) {
                    ev.add(Ev.QUEST, 0, Quests.text(q));
                    gainXp(10, "Günlük görev", day, now, ev);
                }
            }
            if (q.done) doneCount++;
        }
        if (doneCount == 3 && s.quests.size() == 3 && !r.questsChest) {
            r.questsChest = true;
            if (ev != null) ev.add(Ev.QUESTS_ALL, 0, "3 görevin hepsi tamam!");
            addChest(1, day, "Günlük görevler", ev);
        }
        updateWeekly(day, now, ev);
    }

    public void updateWeekly(int day, long now, Ev.List ev) {
        State.Quest w = p.weekly;
        if (w == null || w.done) return;
        int ws = Tm.weekStart(day);
        if (w.day != ws) return;
        int v = 0;
        for (int d = ws; d < ws + 7; d++) {
            DayRec r = s.dayOrNull(d);
            if (r == null) continue;
            switch (w.kind) {
                case Quests.W_PERFECT: if (r.perfect) v++; break;
                case Quests.W_FOCUS: v += r.focusMin; break;
                case Quests.W_GOOD: if (r.met) v++; break;
                case Quests.W_EARLY: v += r.early; break;
                case Quests.W_REFLECT: if (r.mood > 0) v++; break;
                case Quests.W_WATER:
                    for (Task t : s.tasksOf(d)) if (t.type == Habit.COUNTER && "ml".equals(t.unit) && t.progress >= t.full) { v++; break; }
                    break;
            }
        }
        w.progress = Math.min(v, w.target);
        if (v >= w.target) {
            w.done = true;
            if (ev != null) {
                ev.add(Ev.WEEKLY_DONE, 0, Quests.text(w));
                gainXp(100, "Haftalık meydan okuma", day, now, ev);
                addChest(2, day, "Haftalık meydan okuma", ev);
            }
        }
    }

    // ---------------- Diğer eylemler ----------------
    public void snooze(long taskId, int minutes, long now) {
        Task t = s.task(taskId);
        if (t == null) return;
        t.snoozeUntil = now + minutes;
        t.snoozes++;
        s.day(t.day).snoozes++;
    }

    public void skip(long taskId) {
        Task t = s.task(taskId);
        if (t != null && t.isOpen()) t.status = Task.SKIPPED;
    }

    /** Görevi başka bir güne / saate taşır. */
    public Task move(long taskId, int toDay, int newTargetMin, long now) {
        Task t = s.task(taskId);
        if (t == null) return null;
        ensureDay(toDay, now);
        if (toDay == t.day) {
            t.targetMin = newTargetMin;
            if (t.deadlineMin >= 0 && newTargetMin >= 0 && dmOf(t.deadlineMin) <= dmOf(newTargetMin)) t.deadlineMin = -1;
            t.custom = true; t.lastNotif = 0; t.snoozeUntil = 0;
            return t;
        }
        Task c = new Task();
        c.id = s.nextId();
        c.habitId = t.habitId; c.day = toDay; c.fromDay = t.day;
        c.title = t.title; c.emoji = t.emoji; c.type = t.type; c.area = t.area;
        c.targetMin = newTargetMin; c.deadlineMin = -1; c.graceMin = t.graceMin;
        c.full = t.full; c.mini = t.mini; c.step = t.step; c.unit = t.unit; c.miniText = t.miniText;
        c.priority = t.priority; c.streak = t.streak; c.custom = true; c.progress = t.progress;
        s.tasks.add(c);
        t.status = Task.MOVED;
        return c;
    }

    public Task addOnce(String title, String emoji, int day, int targetMin, int deadlineMin, int area, int priority, long now) {
        ensureDay(day, now);
        Task t = new Task();
        t.id = s.nextId(); t.habitId = 0; t.day = day; t.title = title; t.emoji = emoji; t.type = Habit.ONCE;
        t.area = area; t.targetMin = targetMin; t.deadlineMin = deadlineMin; t.priority = priority; t.custom = true;
        t.streak = true;
        s.tasks.add(t);
        refreshDay(day, now, null);
        return t;
    }

    public Habit addHabit(Habit h, long now) {
        h.id = s.nextId();
        if (h.createdDay == 0) h.createdDay = today(now);
        s.habits.add(h);
        resyncFrom(today(now), now);
        return h;
    }

    public void updateHabit(Habit h, long now) {
        resyncFrom(today(now), now);
        refreshDay(today(now), now, null);
    }

    public void archiveHabit(Habit h, long now) {
        h.archived = true;
        resyncFrom(today(now), now);
    }

    /** Uyku öncesi "İyi geceler" (uyku serisi için). */
    public Ev.List goodnight(long now) {
        Ev.List ev = new Ev.List();
        int d = today(now), nd = nowDm(now), sl = sleepDm();
        DayRec r = s.day(d);
        if (nd >= sl - 60 && nd <= sl + 30 && !r.sleepOk) {
            r.sleepOk = true;
            ev.add(Ev.SLEEP_STREAK, p.sleepStreak + 1, "Uyku serisi " + (p.sleepStreak + 1));
            gainXp(10, "Zamanında uyku", d, now, ev);
        }
        return ev;
    }

    /** Günaydın ekranındaki "Güne Başla". */
    public Ev.List startDay(long now) {
        Ev.List ev = new Ev.List();
        int d = today(now);
        DayRec r = s.day(d);
        if (r.started) return ev;
        r.started = true;
        p.morningStarts++;
        int nd = nowDm(now);
        int bonus = nd <= wakeDm() + 30 ? 10 : 5;
        gainXp(bonus, "Taze başlangıç", d, now, ev);
        updateQuests(d, now, ev);
        Badges.check(s, d, ev);
        return ev;
    }

    public Ev.List reflect(int mood, String note, String intention, long now) {
        Ev.List ev = new Ev.List();
        int d = today(now);
        DayRec r = s.day(d);
        boolean first = r.mood == 0;
        r.mood = mood;
        if (note != null) r.reflection = note;
        if (intention != null) r.intention = intention;
        r.summaryDone = true;
        if (first) {
            p.reflections++;
            gainXp(10, "Gün sonu değerlendirmesi", d, now, ev);
        }
        updateQuests(d, now, ev);
        Badges.check(s, d, ev);
        return ev;
    }

    /** Odak oturumu bitti. */
    public Ev.List focusDone(long taskId, String subject, int minutes, int planned, boolean completed, int score,
                             boolean achieved, String intention, String note, long startAt, long now) {
        Ev.List ev = new Ev.List();
        int d = today(now);
        State.FocusLog f = new State.FocusLog();
        f.day = d; f.startAt = startAt; f.minutes = minutes; f.planned = planned; f.taskId = taskId;
        Task t = s.task(taskId);
        f.habitId = t != null ? t.habitId : 0;
        f.subject = subject == null ? "" : subject;
        f.score = score; f.completed = completed; f.achieved = achieved;
        f.intention = intention == null ? "" : intention; f.note = note == null ? "" : note;
        f.tree = completed ? (int) (Math.abs(startAt * 7 + minutes) % 5) : -1;
        s.focus.add(f);
        DayRec r = s.day(d);
        r.focusMin += minutes;
        p.totalFocusMin += minutes;
        p.maxFocusDay = Math.max(p.maxFocusDay, r.focusMin);
        if (completed) p.longestSession = Math.max(p.longestSession, minutes);
        int xp = completed ? Math.max(5, Math.round(minutes * 0.4f)) : Math.round(minutes * 0.2f);
        if (achieved) xp += 5;
        if (xp > 0) { gainXp(xp, "Odak " + minutes + " dk", d, now, ev); p.coins += Math.max(1, xp / 3); }
        if (t != null && t.type == Habit.DURATION && minutes > 0 && t.day == d) {
            ev.addAll(addProgress(t.id, minutes, now));
        }
        updateQuests(d, now, ev);
        Badges.check(s, d, ev);
        return ev;
    }

    public ArrayList<String> openChest(int index, long now) {
        if (index < 0 || index >= s.chests.size()) return new ArrayList<>();
        State.Chest c = s.chests.remove(index);
        ArrayList<String> out = Game.openChest(s, c, new Random(now * 13 + p.chestsOpened), now);
        Ev.List ev = new Ev.List();
        Badges.check(s, today(now), ev);
        for (Ev e : ev) notice(e.type, e.value, e.text);
        return out;
    }

    public boolean buy(String itemId, long now) {
        Game.Item it = Game.item(itemId);
        if (it == null) return false;
        if (Game.level(p.xp) < it.minLevel) return false;
        if (p.coins < it.price) return false;
        if (it.cat == Game.C_FREEZE) {
            if (p.freezes >= Game.MAX_FREEZES) return false;
            p.freezes++;
        } else {
            if (s.owns(it.id)) return false;
            p.owned.add(it.id);
            if (it.cat == Game.C_ISLAND) s.island.add(Game.islandCodeFor(it.id));
            if (it.cat == Game.C_HAT || it.cat == Game.C_ACC) p.bought++;
        }
        p.coins -= it.price;
        Ev.List ev = new Ev.List();
        Badges.check(s, today(now), ev);
        for (Ev e : ev) notice(e.type, e.value, e.text);
        return true;
    }

    public boolean redeem(long rewardId, long now) {
        for (State.Reward r : s.rewards) if (r.id == rewardId) {
            if (p.coins < r.cost) return false;
            p.coins -= r.cost; r.redeemed++; p.redeemed++;
            Ev.List ev = new Ev.List();
            Badges.check(s, today(now), ev);
            for (Ev e : ev) notice(e.type, e.value, e.text);
            return true;
        }
        return false;
    }

    /** Uygulama açıldığında: geri dönüş ve mektup kontrolü. */
    public Ev.List onOpen(long now) {
        Ev.List ev = tick(now);
        int td = today(now);
        if (p.onboarded && p.lastActiveDay >= 0 && td - p.lastActiveDay >= 3 && p.comebackDay != td) {
            p.comebackDay = td;
            p.comebacks++;
            s.day(td).easy = true;
            ev.add(Ev.COMEBACK, td - p.lastActiveDay, "Tekrar hoş geldin!");
            Badges.check(s, td, ev);
        }
        if (p.onboarded) p.lastActiveDay = td;
        if (p.letter != null && !p.letter.isEmpty() && p.firstDay >= 0) {
            int[] at = {30, 100, 365};
            for (int n : at) if (td - p.firstDay >= n && !p.lettersOpened.contains(n)) {
                p.lettersOpened.add(n);
                ev.add(Ev.LETTER, n, p.letter);
                break;
            }
        }
        return ev;
    }

    // ---------------- Şablonlar ----------------
    public State.Template template(long id) {
        for (State.Template t : s.templates) if (t.id == id) return t;
        return null;
    }

    public State.Template saveTemplate(String name, String emoji) {
        State.Template t = new State.Template();
        t.id = s.nextId(); t.name = name; t.emoji = emoji;
        for (Habit h : s.activeHabits()) {
            State.TItem i = new State.TItem();
            i.habitId = h.id; i.daysMask = h.daysMask; i.targetMin = h.targetMin;
            t.items.add(i);
        }
        s.templates.add(t);
        p.activeTemplate = t.id;
        return t;
    }

    public void applyTemplate(State.Template t, int fromDay, long now) {
        for (Habit h : s.activeHabits()) {
            State.TItem found = null;
            for (State.TItem i : t.items) if (i.habitId == h.id) found = i;
            if (found != null) { h.daysMask = found.daysMask; h.targetMin = found.targetMin; }
            else h.daysMask = 0; // şablonda yoksa bu hafta planlanmaz
        }
        p.activeTemplate = t.id;
        resyncFrom(Math.max(fromDay, today(now)), now);
    }

    // ---------------- Yardımcılar (arayüz için) ----------------
    /** Günün sıralı görev listesi: saatliler saate göre, sonra saatsizler. */
    public ArrayList<Task> sorted(int day) {
        ArrayList<Task> r = s.tasksOf(day);
        final int ro = ro();
        Collections.sort(r, (a, b) -> {
            int ta = a.targetMin >= 0 ? Tm.toDm(a.targetMin, ro) : 5000 + a.type;
            int tb = b.targetMin >= 0 ? Tm.toDm(b.targetMin, ro) : 5000 + b.type;
            if (ta != tb) return Integer.compare(ta, tb);
            return Long.compare(a.id, b.id);
        });
        return r;
    }

    public int level() { return Game.level(p.xp); }

    /** Görevin şu anki durum etiketi ("20 dk gecikti" gibi) için gecikme dakikası; negatif = kalan süre. */
    public int lateness(Task t, long now) {
        int tg = targetDm(t);
        if (tg < 0 || t.day != today(now)) return Integer.MIN_VALUE;
        return nowDm(now) - tg;
    }

    /** Günün toplam tahmini yükü (dk) — aşırı yüklenme uyarısı için. */
    public int estimatedLoad(int day) {
        int m = 0;
        for (Task t : s.tasksOf(day)) {
            if (t.isComplete()) continue;
            if (t.type == Habit.DURATION) m += Math.max(0, t.full - t.progress);
            else if (t.type == Habit.COUNTER) m += 0;
            else m += 20;
        }
        return m;
    }
}
