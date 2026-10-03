package com.ilker.rutin.core;

import java.util.ArrayList;
import java.util.Collections;

/** Öğrenen öneriler (içgörüler) ve analiz hesapları. */
public final class Insights {
    private Insights() {}

    public static final int K_RETIME = 1, K_RAISE = 2, K_LOWER = 3, K_SOFT = 4, K_MINI_SUGGEST = 5;

    static boolean exists(State s, int kind, long habitId) {
        for (State.Insight i : s.insights) if (!i.dismissed && i.kind == kind && i.habitId == habitId) return true;
        return false;
    }

    static void add(State s, int kind, long habitId, int value, String text, int day) {
        if (exists(s, kind, habitId)) return;
        State.Insight i = new State.Insight();
        i.id = s.nextId(); i.kind = kind; i.habitId = habitId; i.value = value; i.text = text; i.day = day;
        s.insights.add(i);
        while (s.insights.size() > 30) s.insights.remove(0);
    }

    public static ArrayList<State.Insight> active(State s) {
        ArrayList<State.Insight> r = new ArrayList<>();
        for (State.Insight i : s.insights) if (!i.dismissed) r.add(i);
        return r;
    }

    static void onDayClosed(Engine e, int d) {
        State s = e.s;
        // Öğrenen zamanlama: hep başka saatte yapılıyorsa hedef saati güncellemeyi öner
        for (Habit h : s.activeHabits()) {
            if (h.targetMin < 0 || h.doneMinutes.size() < 7 || h.type == Habit.COUNTER || h.type == Habit.DURATION) continue;
            ArrayList<Integer> m = new ArrayList<>(h.doneMinutes.subList(h.doneMinutes.size() - 7, h.doneMinutes.size()));
            Collections.sort(m);
            int med = m.get(3);
            int tg = e.dmOf(h.targetMin);
            if (Math.abs(med - tg) >= 40) {
                int rounded = Tm.norm(Math.round(med / 15f) * 15);
                add(s, K_RETIME, h.id, rounded, h.emoji + " " + h.title + " işini genelde " + Tm.hhmm(rounded)
                        + " civarında yapıyorsun. Hedef saati " + Tm.hhmm(rounded) + " yapalım mı?", d);
            }
        }
        // Ruh hali düşüşü: 3 gün üst üste düşük
        int low = 0;
        for (int k = 0; k < 3; k++) {
            DayRec r = s.dayOrNull(d - k);
            if (r != null && r.mood > 0 && r.mood <= 2) low++;
        }
        if (low == 3 && !e.isSoft(d + 1)) {
            add(s, K_SOFT, 0, 3, "Son günler zor geçiyor gibi. Önümüzdeki 3 gün hedefleri mini versiyona düşürelim mi? Kendine nazik ol. 💛", d);
        }
    }

    static void onNewWeek(Engine e, int ws) {
        State s = e.s;
        for (Habit h : s.activeHabits()) {
            int tot = 0, full = 0, ok = 0;
            for (Task t : s.tasks) {
                if (t.habitId != h.id || t.day < ws - 7 || t.day >= ws || t.fromDay >= 0 || !t.counts()) continue;
                tot++;
                if (t.status == Task.DONE) full++;
                if (e.satisfied(t)) ok++;
            }
            if (tot < 4) continue;
            if (full >= tot * 0.95 && (h.type == Habit.COUNTER || h.type == Habit.DURATION) && !h.graduated) {
                int nv = h.type == Habit.DURATION ? h.full + 15 : (int) (Math.ceil(h.full * 1.1 / Math.max(1, h.step)) * Math.max(1, h.step));
                add(s, K_RAISE, h.id, nv, "Harika gidiyorsun! " + h.emoji + " " + h.title + " hedefini " + h.fmtAmount(h.full)
                        + " → " + h.fmtAmount(nv) + " yapalım mı? 🚀", ws);
            } else if (ok < tot * 0.5) {
                if (h.isQuantity()) {
                    int nv = Math.max(h.mini > 0 ? h.mini : 1, (int) (h.full * 0.7));
                    if (h.step > 1) nv = Math.max(h.step, nv / h.step * h.step);
                    add(s, K_LOWER, h.id, nv, h.emoji + " " + h.title + " geçen hafta zorladı (" + ok + "/" + tot
                            + "). Hedefi geçici olarak " + h.fmtAmount(nv) + " yapalım mı? Küçük adımlar kalıcıdır.", ws);
                } else if (!h.hasMini()) {
                    add(s, K_MINI_SUGGEST, h.id, 0, h.emoji + " " + h.title + " geçen hafta zorladı (" + ok + "/" + tot
                            + "). Ona 2 dakikalık bir mini versiyon tanımlayalım mı?", ws);
                }
            }
        }
    }

    /** İçgörüyü uygula. */
    public static void apply(Engine e, State.Insight i, long now) {
        State s = e.s;
        Habit h = s.habit(i.habitId);
        switch (i.kind) {
            case K_RETIME: if (h != null) h.targetMin = i.value; break;
            case K_RAISE: case K_LOWER: if (h != null) { h.full = i.value; if (h.mini >= h.full) h.mini = Math.max(0, h.full / 2); } break;
            case K_SOFT: e.p.softUntil = e.today(now) + i.value; break;
        }
        i.dismissed = true;
        e.updateHabit(h, now);
    }

    // ---------------- Analiz ----------------
    /** Son n günün tamamlanma oranları (eskiden yeniye). -1 = veri yok. */
    public static double[] ratios(State s, int today, int n) {
        double[] r = new double[n];
        for (int i = 0; i < n; i++) {
            int d = today - (n - 1 - i);
            DayRec x = s.dayOrNull(d);
            if (x == null || (x.total == 0 && x.done == 0 && !x.met)) r[i] = x != null && x.state == DayRec.FROZEN ? -2 : -1;
            else r[i] = x.state == DayRec.FROZEN ? -2 : Math.max(x.ratio, x.met ? 0.5 : 0);
        }
        return r;
    }

    public static final class HabitStat {
        public Habit h; public int scheduled, done, mini; public double rate; public int avgDelay; public boolean hasDelay;
    }

    public static ArrayList<HabitStat> habitStats(Engine e, int today, int days) {
        ArrayList<HabitStat> out = new ArrayList<>();
        for (Habit h : e.s.activeHabits()) {
            HabitStat hs = new HabitStat(); hs.h = h;
            long delaySum = 0; int delayN = 0;
            for (Task t : e.s.tasks) {
                if (t.habitId != h.id || t.day > today || t.day <= today - days || !t.counts()) continue;
                if (t.day == today && !t.isComplete()) continue;
                hs.scheduled++;
                if (t.status == Task.DONE) hs.done++;
                else if (e.satisfied(t)) hs.mini++;
                if (t.isComplete() && t.targetMin >= 0 && !t.isQuantity()) {
                    delaySum += Tm.dm(t.doneAt, e.ro()) - e.dmOf(t.targetMin); delayN++;
                }
            }
            hs.rate = hs.scheduled == 0 ? 0 : (hs.done + hs.mini) / (double) hs.scheduled;
            if (delayN >= 3) { hs.hasDelay = true; hs.avgDelay = (int) (delaySum / delayN); }
            out.add(hs);
        }
        return out;
    }

    /** Bir alışkanlığın yapıldığı ve yapılmadığı günlerdeki ortalama ruh hali farkı. */
    public static ArrayList<String> moodCorrelations(Engine e, int today) {
        ArrayList<String> out = new ArrayList<>();
        for (Habit h : e.s.activeHabits()) {
            double sy = 0, sn = 0; int ny = 0, nn = 0;
            for (Task t : e.s.tasks) {
                if (t.habitId != h.id || t.day >= today || !t.counts()) continue;
                DayRec r = e.s.dayOrNull(t.day);
                if (r == null || r.mood == 0) continue;
                if (e.satisfied(t)) { sy += r.mood; ny++; } else { sn += r.mood; nn++; }
            }
            if (ny >= 4 && nn >= 3) {
                double a = sy / ny, b = sn / nn;
                double diff = (a - b) / b;
                if (diff >= 0.12) out.add(h.emoji + " " + h.title + " yaptığın günler %" + Math.round(diff * 100) + " daha mutlusun");
                else if (diff <= -0.12) out.add(h.emoji + " " + h.title + " günlerinde ruh halin daha düşük; belki zamanlamasını değiştirmek iyi gelir");
            }
        }
        // Uyku ve odak ilişkisi
        double sOk = 0, sNo = 0; int nOk = 0, nNo = 0;
        for (DayRec r : e.s.days) {
            if (r.mood == 0 || r.day >= today) continue;
            DayRec prev = e.s.dayOrNull(r.day - 1);
            if (prev == null) continue;
            if (prev.sleepOk) { sOk += r.mood; nOk++; } else { sNo += r.mood; nNo++; }
        }
        if (nOk >= 4 && nNo >= 3) {
            double diff = (sOk / nOk - sNo / nNo) / (sNo / nNo);
            if (diff >= 0.1) out.add("😴 Zamanında uyuduğun gecelerin ertesi günü %" + Math.round(diff * 100) + " daha iyi hissediyorsun");
        }
        return out;
    }

    /** Saat bazında odak dakikası (0..23). */
    public static int[] focusByHour(State s, int fromDay) {
        int[] h = new int[24];
        for (State.FocusLog f : s.focus) {
            if (f.day < fromDay) continue;
            int hr = Tm.norm((int) (f.startAt % 1440)) / 60;
            h[hr] += f.minutes;
        }
        return h;
    }

    public static int bestHour(int[] byHour) {
        int b = -1, v = 0;
        for (int i = 0; i < 24; i++) if (byHour[i] > v) { v = byHour[i]; b = i; }
        return b;
    }

    /** Konu bazında odak (başlık -> dk), çoktan aza. */
    public static ArrayList<Object[]> focusBySubject(State s, int fromDay) {
        java.util.LinkedHashMap<String, Integer> m = new java.util.LinkedHashMap<>();
        for (State.FocusLog f : s.focus) {
            if (f.day < fromDay) continue;
            String k = f.subject == null || f.subject.isEmpty() ? "Serbest" : f.subject;
            Integer v = m.get(k);
            m.put(k, (v == null ? 0 : v) + f.minutes);
        }
        ArrayList<Object[]> r = new ArrayList<>();
        for (java.util.Map.Entry<String, Integer> en : m.entrySet()) r.add(new Object[]{en.getKey(), en.getValue()});
        Collections.sort(r, (a, b) -> Integer.compare((Integer) b[1], (Integer) a[1]));
        return r;
    }

    /** Yaşam alanı puanları (son 30 gün tamamlanan iş sayısı, 0..1 normalize). */
    public static double[] areaScores(State s, int today) {
        double[] a = new double[6];
        for (Task t : s.tasks) if (t.isComplete() && t.day > today - 30 && t.area >= 0 && t.area < 6) a[t.area] += t.status == Task.DONE ? 1 : 0.5;
        double mx = 1;
        for (double v : a) mx = Math.max(mx, v);
        for (int i = 0; i < 6; i++) a[i] = a[i] / mx;
        return a;
    }

    public static int[] areaCounts(State s) {
        int[] a = new int[6];
        for (Task t : s.tasks) if (t.isComplete() && t.area >= 0 && t.area < 6) a[t.area]++;
        return a;
    }

    public static final class Week {
        public int start, goodDays, perfectDays, focusMin, tasksDone, early, moodN; public double avgRatio, avgMood;
        public String bestDay = "", worstHabit = "", tip = "";
        public double prevAvgRatio;
    }

    public static Week week(Engine e, int weekStart) {
        Week w = new Week();
        w.start = weekStart;
        State s = e.s;
        double sum = 0; int n = 0; double best = -1; double moodSum = 0;
        for (int d = weekStart; d < weekStart + 7; d++) {
            DayRec r = s.dayOrNull(d);
            if (r == null) continue;
            if (r.good() || r.met) w.goodDays++;
            if (r.perfect) w.perfectDays++;
            w.focusMin += r.focusMin;
            w.early += r.early;
            w.tasksDone += r.done + r.mini;
            if (r.total > 0) { sum += r.ratio; n++; if (r.ratio > best) { best = r.ratio; w.bestDay = Tm.DAY_LONG[Tm.weekday(d)]; } }
            if (r.mood > 0) { moodSum += r.mood; w.moodN++; }
        }
        w.avgRatio = n == 0 ? 0 : sum / n;
        w.avgMood = w.moodN == 0 ? 0 : moodSum / w.moodN;
        double ps = 0; int pn = 0;
        for (int d = weekStart - 7; d < weekStart; d++) { DayRec r = s.dayOrNull(d); if (r != null && r.total > 0) { ps += r.ratio; pn++; } }
        w.prevAvgRatio = pn == 0 ? -1 : ps / pn;
        // En çok aksatılan
        int worstMiss = 0;
        for (Habit h : s.activeHabits()) {
            int miss = 0;
            for (Task t : s.tasks) if (t.habitId == h.id && t.day >= weekStart && t.day < weekStart + 7 && t.status == Task.MISSED) miss++;
            if (miss > worstMiss) { worstMiss = miss; w.worstHabit = h.emoji + " " + h.title; }
        }
        if (!w.worstHabit.isEmpty()) {
            Habit wh = null;
            for (Habit h : s.activeHabits()) if ((h.emoji + " " + h.title).equals(w.worstHabit)) wh = h;
            if (wh != null && !wh.hasMini()) w.tip = w.worstHabit + " için 2 dakikalık bir mini versiyon tanımla: başlamak işin yarısıdır.";
            else if (wh != null && wh.targetMin >= 0) w.tip = w.worstHabit + " için bir tetikleyici bağla: \"___ yaptıktan sonra " + wh.title + "\".";
            else w.tip = w.worstHabit + " işini günün daha erken bir saatine almayı dene.";
        } else if (w.avgRatio > 0.9) {
            w.tip = "Çok iyi gidiyorsun! Yeni küçük bir alışkanlık eklemenin tam zamanı olabilir.";
        } else {
            w.tip = "Pazar akşamı 5 dakika ayırıp gelecek haftayı planla; plan yapılan haftalar çok daha akıcı geçer.";
        }
        return w;
    }
}
